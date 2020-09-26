package tsi.too.grade_control.controller;

import static tsi.too.grade_control.Constants.*;
import static tsi.too.message_dialog.InputDialog.InputValidator.DEFAULT_SUCCESS_MESSAGE;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import tsi.too.grade_control.Student;
import tsi.too.grade_control.Discipline;
import tsi.too.message_dialog.InputDialog;
import tsi.too.message_dialog.MessageDialog;
import tsi.too.message_dialog.InputDialog.InputValidator;

public class StudentController {

	private static StudentController instance;

	private StudentController() {}

	private final InputValidator<Double> gradeRangeValidator = InputDialog.createRangeValidator(
			Discipline.MIN_GRADE, 
			Discipline.MAX_GRADE,
			GRADE_MUST_BE_BETWEEN_ZERO_AND_TEN 
	);
	
	/**
	 * Ensures that only one instance of this class is created.
	 *
	 * @return an instance of this class.
	 */
	public static StudentController getInstance() {
		synchronized (StudentController.class) {
			if (instance == null)
				instance = new StudentController();

			return instance;
		}
	}

	public void registerStudent(final ArrayList<Student> students) {
		var s = readStudentData(students);

		if(s != null) {			
			students.add(s);
			Student.increaseStudentsCount();
			MessageDialog.showInformationDialog(STUDENT_REGISTRATION, STUDENT_SUCCESSFULY_REGISTERED);
		}
	}

	/**
	 * checks if a registration matches all requirements: 
	 * 
	 * cannot be null or empty, must be an alphanumeric value in the format SSSAAAA-DD where
	 * 
	 * SSS the course initials;
	 * AAAA the student's year of registration;
	 * DD integer, positive and sequential numeric value (maximum 2 digits).
	 * 
	 * @param registration
	 * @return 
	 */
	private static String isValidRegistration(final String registration, final ArrayList<Student> students) {	
		String regex = "[A-Z]{1,3}[0-9]{4}[-]{1}[01|12|23|34|45|56|67|78|89]{2}";
		var pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

		if(registration == null || registration.isEmpty())
			return REGISTRATION_NUMBER_CANOT_BE_BLANK;

		if(!pattern.matcher(registration).matches()) 
			return INVALID_REGISTRATION_NUMBER;

		if(students.stream().filter(s -> s.getRegistration().equals(registration)).count() > 0)
			return REGISTRATION_ALREADY_REGISTERED;

		return DEFAULT_SUCCESS_MESSAGE; 
	}

	/**
	 * Reads and validates a student info.
	 *  
	 * @param students
	 * @return the read data or null if reading is cancelled or the student data is already registered.
	 */
	private Student readStudentData(final ArrayList<Student> students) {
		String registration = InputDialog.showStringInputDialog(
				STUDENT_REGISTRATION, 
				REGISTRATION_NUMBER, 
				input -> isValidRegistration(input, students)
				);
		if(registration == null)
			return null;

		String name = InputDialog.showStringInputDialog(
				STUDENT_REGISTRATION, 
				NAME, 
				input -> {
					String message = InputDialog.createEmptyStringValidator(NAME_CANNOT_BE_BLANK).getErrorMessage(input);

					if(message == DEFAULT_SUCCESS_MESSAGE)
						return students.stream().filter(s -> s.getName().equals(input))
								.count() > 0 ? NAME_ALREADY_REGISTERED :message;

					return message;
				});
		if(name == null)		
			return null;

		String course = InputDialog.showStringInputDialog(
				STUDENT_REGISTRATION, 
				COURSE,
				InputDialog.createEmptyStringValidator(COURSE_CANNOT_BE_BLANK)
				);
		if(course == null)
			return null;

		var s = new Student(registration, name, course);
		
		do {
			enroll(s);
		}while(!s.isEnrolled() && 
				!MessageDialog.showConfirmationDialog(
						STUDENT_REGISTRATION, 
						STUDENT_MUST_BE_ENROLLED_IN_A_DISCIPLINE_AT_LEAST_CANCEL_REGISTRATION
				)
		);
		
		return s.isEnrolled() ? s : null;
	}

	private void enroll(Student s) {
		while(s.canEnrollInAnotherDiscipline()) {
			var subject = readDisciplineData(s);
			if(subject == null)
				break;
			
			s.addSubject(subject);
			
			if(s.canEnrollInAnotherDiscipline() && !MessageDialog.showConfirmationDialog(STUDENT_REGISTRATION, ENROLL_IN_ANOTHER_DISCIPLINE))
				break;
		}
	}
	
	private Discipline readDisciplineData(Student s) {
		String name = InputDialog.showStringInputDialog(
				DISCIPLINE_ENROLLMENT, 
				NAME, 
				input -> {
					var message = InputDialog.createEmptyStringValidator(NAME_CANNOT_BE_BLANK).getErrorMessage(input);
					if(message == DEFAULT_SUCCESS_MESSAGE) {
						return s.isEnrolled(input) ? STUDENT_ALREADY_ENROLLED_IN_THIS_DISCIPLINE : DEFAULT_SUCCESS_MESSAGE;
					}
					
					return message;
				}
		);
		if(name == null)
			return null;
				
		Double grade = InputDialog.showDoubleInputDialog(
				DISCIPLINE_ENROLLMENT, 
				GRADE,
				gradeRangeValidator
		);
		if(grade == null)
			return null;
		
		return new Discipline(name, grade);
	}

	private Student searchStudent(final List<Student> students, String name) {
		var found = students.stream().filter(s-> s.getName().equalsIgnoreCase(name)).collect(Collectors.toList());
		
		if(!found.isEmpty())
			return found.get(0);
		
		return null;
	}
	
	public void searchStudent(final List<Student> students) {
		var name = InputDialog.showStringInputDialog(
				SEARCH_STUDENT, 
				NAME, 
				InputDialog.createEmptyStringValidator(NAME_CANNOT_BE_BLANK)
		);
		
		if(name != null)
		{
			var student = searchStudent(students, name);
			if(student == null)
				MessageDialog.showAlertDialog(SEARCH_STUDENT, STUDENT_NOT_FOUND);
			else
				MessageDialog.showTextMessage(STUDENT_DATA, createStudentMessage(student));
		}
	}

	private String createStudentMessage(final Student student) {
		var message = new StringBuilder()
				.append(String.format("%s: %s", REGISTRATION_NUMBER, student.getRegistration()))
				.append(String.format("\n%s: %s", NAME, student.getName()))
				.append(String.format("\n%s: %s", COURSE, student.getCourse()))
				.append(String.format("\n\n%s:", DISCIPLINES))
				;
		
		for(Discipline s: student.getEnrolledDisciplines()) {
			if(s == null)
				break;
			
			message = message.append(String.format("\n\t%s: %s",NAME, s.getName()))
				.append(String.format("\n\t%s: %1.2f\n", GRADE, s.getGrade()));
		}
	
		return message.toString();
	}
	
	public void removeStudent(final ArrayList<Student> students) {
		var name = InputDialog.showStringInputDialog(
				SEARCH_STUDENT, 
				NAME, 
				InputDialog.createEmptyStringValidator(NAME_CANNOT_BE_BLANK)
		);
		
		if(name == null) 
			return;
		
		var student = searchStudent(students, name);
		
		if(student == null)
			MessageDialog.showAlertDialog(SEARCH_STUDENT, STUDENT_NOT_FOUND);
		else {
			var message = createStudentMessage(student) +String.format("\n%s", ARE_YOU_SURE_YOU_WANT_TO_DELETE_THIS_STUDENT);
			if(MessageDialog.showConfirmationDialog(DELETE_STUDENT, message))
			{
				students.remove(student);
				Student.decreaseStudentsCount();
				MessageDialog.showInformationDialog(DELETE_STUDENT, STUDENT_SUCCESSFULY_DELETED);
			}
		}
	}

	public void searchDiscipline(final List<Student> students) {
		var studentNameOrRegistration = InputDialog.showStringInputDialog(
				SEARCH_DISCIPLINE, 
				STUDENT_NAME_OR_REGISTRATION, 
				InputDialog.createEmptyStringValidator(THIS_FIELD_CANNOT_BE_EMPTY)
		);
		if(studentNameOrRegistration == null)
			return;
		
		var subject = InputDialog.showStringInputDialog(
				SEARCH_DISCIPLINE, 
				DISCIPLINE_NAME, 
				InputDialog.createEmptyStringValidator(THIS_FIELD_CANNOT_BE_EMPTY)
		);
		if(subject == null)
			return;
		
		var student = searchStudent(students, studentNameOrRegistration);
		
		if(student == null)
			student = searchDiscipline(students, studentNameOrRegistration);
		
		if(student == null)
		{
			MessageDialog.showAlertDialog(SEARCH_DISCIPLINE, STUDENT_NOT_FOUND);
			
			return;
		}
		
		var enrolledSubjects = searchDiscipline(student, subject);
		if(enrolledSubjects.length == 0)
		{
			MessageDialog.showAlertDialog(
					SEARCH_STUDENT, 
					String.format(THE_STUDENT_IS_NOT_ENROLLED_ON_THIS_SUBJECT, subject)
			);
			
			return;
		}
		
		var message = student.getName();
		
		for(Object obj : enrolledSubjects) {
			var s = ((Discipline) obj);
			message+= String.format("\n\t%s: %1.2f", s.getName(), s.getGrade());
		}
		
		MessageDialog.showInformationDialog(SEARCH_STUDENT, message);
	}

	private Student searchDiscipline(final List<Student> students, String registration) {
		var found = students.stream().filter(s-> s.getRegistration().equalsIgnoreCase(registration)).collect(Collectors.toList());
		
		if(!found.isEmpty())
			return found.get(0);

		return null;
	}
	
	private Object[] searchDiscipline(Student student, String subject) {
		return student.getDisciplines(subject).toArray();
	}

	public void report(final List<Student> students)
	{
		var message = new StringBuilder();
		var studentsCount = Student.getStudentsCount();
		var reprovedCount = 0;
		var approvedCount = 0;
		
		if(studentsCount == 0) {
			MessageDialog.showInformationDialog(REPORT, NO_DATA_FOUND);
			return;
		}
		
		for(Student s : students)
		{
			message.append(generateStudentApprovalStatus(s))
				.append("\n");
			
			if(s.isAproved())
				approvedCount++;
			else
				reprovedCount++;
		}
		
		var percentageOfApproved = calculatePercentage(approvedCount, studentsCount);
		var percentageOfReproved = calculatePercentage(reprovedCount, studentsCount);
		
		message.append(generateSeparator(RESUME))
			.append(String.format("\n%s = %d", NUMBER_OF_STUDENTS, studentsCount))
			.append(String.format("\n%s = %d", NUMBER_OF_APPROVED, approvedCount ))
			.append(String.format("\n%s = %d", NUMBER_OF_REPROVEDS, reprovedCount ))
			.append(String.format("\n%s = %1.2f", PERCENTAGE_OF_APPROVED, percentageOfApproved ))
			.append(String.format("\n%s = %1.2f", PERCENTAGE_OF_REPROVED, percentageOfReproved))
		;
		
		MessageDialog.showTextMessage(REPORT, message.toString());
	}

	private String generateStudentApprovalStatus(Student student) {
		if(student == null)
			return "";
		
		return new StringBuilder()
				.append(getRegistrationSequencialNumber(student.getRegistration()))
				.append(" " + student.getName())
				.append("\t" + student.getCourse())
				.append(String.format("\t%s", student.isAproved() ? APPROVED : REPROVED))
				.toString();
	}

	private String generateSeparator(String header) {
		var charCount = 20;
		
		var message = "\n";
		
		for(int i = 0; i < charCount; i++) {
			message+="-";
		}
		
		message += " " + header + " ";
		
		for(int i = 0; i < charCount; i++) {
			message+="-";
		}
		
		return message;
	}
	
	private double calculatePercentage(int part, int total) {
		return (part / total) * 100;
	}

	private String getRegistrationSequencialNumber(final String registration) {
		try{
			return registration.split("-")[1];
		}catch (Exception e) {
			return "";
		}
	}

	public void updateStudentData(final ArrayList<Student> students) {
		var name = InputDialog.showStringInputDialog(
				UPDATE_STUDENT_DATA, 
				STUDENT_NAME, 
				InputDialog.createEmptyStringValidator(NAME_CANNOT_BE_BLANK)
			);
		
		if(name == null)
			return;
		
		var student = searchStudent(students, name);
		
		if(student == null)
		{
			MessageDialog.showAlertDialog(UPDATE_STUDENT_DATA, STUDENT_NOT_FOUND);
			return;
		}
		
		performReadAndUpdate(students, student);
	}

	private void performReadAndUpdate(ArrayList<Student> students, Student student) {
		var name = InputDialog.showStringInputDialog(
				UPDATE_STUDENT_DATA, 
				NAME, 
				InputDialog.createEmptyStringValidator(NAME_CANNOT_BE_BLANK)
			);
		if(name == null)
			return;
		
		String course;
		
		do{
			course = InputDialog.showStringInputDialog(
						UPDATE_STUDENT_DATA, 
						COURSE, 
						InputDialog.createEmptyStringValidator(NAME_CANNOT_BE_BLANK)
					);
				
		}while(course == null && !MessageDialog.showConfirmationDialog(
				UPDATE_STUDENT_DATA, 
				YOUR_CHANGES_WILL_BE_DISCARDED_ARE_YOU_SURE_YOU_WANT_TO_CANCEL
				)
		);
		if(course == null)
			return;
		
		student.setCourse(course);
		student.setName(name);
		
		if(MessageDialog.showConfirmationDialog(
				UPDATE_STUDENT_DATA,
				RECORD_UPDATED_SUCCESSFULLY + "\n" + UPDATE_GRADES + "?"
		)) {
			updateStudentGrades(student);
		}
	}
	
	private void updateStudentGrades(Student student) {
		do {
			var disciplineName = InputDialog.showStringInputDialog(
					UPDATE_STUDENT_DATA, 
					DISCIPLINE_NAME, 
					InputDialog.createEmptyStringValidator(NAME_CANNOT_BE_BLANK)
				);
			
			var grade = InputDialog.showDoubleInputDialog(UPDATE_GRADES, GRADE, gradeRangeValidator);
			
			if(grade != null) {
				var d = new Discipline(disciplineName, grade);
				d.setGrade(grade);
				
				if(student.updateGrade(d)) {
					MessageDialog.showInformationDialog(UPDATE_GRADES, RECORD_UPDATED_SUCCESSFULLY);
				}else {
					MessageDialog.showAlertDialog(UPDATE_GRADES, STUDENT_NOT_ENROLLED_IN_THIS_DISCIPLINE);
				}
			}
		}while(MessageDialog.showConfirmationDialog(UPDATE_STUDENT_DATA, UPDATE_ANOTHER_GRADE));
	}
}