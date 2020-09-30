package tsi.too.grade_control.controller;

import static tsi.too.grade_control.Constants.*;
import static tsi.too.message_dialog.InputDialog.createEmptyStringValidator;
import static tsi.too.message_dialog.InputDialog.showStringInputDialog;
import static tsi.too.message_dialog.InputDialog.InputValidator.DEFAULT_SUCCESS_MESSAGE;
import static tsi.too.message_dialog.MessageDialog.showAlertDialog;
import static tsi.too.message_dialog.MessageDialog.showConfirmationDialog;
import static tsi.too.message_dialog.MessageDialog.showInformationDialog;
import static tsi.too.message_dialog.MessageDialog.showTextMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import tsi.too.grade_control.model.Student;
import tsi.too.grade_control.util.Pair;
import tsi.too.message_dialog.InputDialog;
import tsi.too.message_dialog.InputDialog.InputValidator;
import tsi.too.message_dialog.MessageDialog;

public class StudentController {
	public static final double MAX_GRADE = 10;
	public static final double MIN_GRADE = 0;
	
	private static StudentController instance;

	private StudentController() {}

	private final InputValidator<Double> gradeRangeValidator = InputDialog.createRangeValidator(
			MIN_GRADE, 
			MAX_GRADE,
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
			showInformationDialog(STUDENT_REGISTRATION, STUDENT_SUCCESSFULY_REGISTERED);
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
		String registration = showStringInputDialog(
				STUDENT_REGISTRATION, 
				REGISTRATION_NUMBER, 
				input -> isValidRegistration(input, students)
				);
		if(registration == null)
			return null;

		String name = showStringInputDialog(
				STUDENT_REGISTRATION, 
				NAME, 
				input -> {
					var message = createEmptyStringValidator(NAME_CANNOT_BE_BLANK)
							.getErrorMessage(input);

					if(message == DEFAULT_SUCCESS_MESSAGE)
						return students.stream().filter(s -> s.getName().equals(input))
								.count() > 0 ? NAME_ALREADY_REGISTERED : message;

					return message;
				});
		if(name == null)		
			return null;

		String course = showStringInputDialog(
				STUDENT_REGISTRATION, 
				COURSE,
				createEmptyStringValidator(COURSE_CANNOT_BE_BLANK)
		);
		if(course == null)
			return null;

		var s = new Student(registration, name, course);
		
		do {
			enroll(s);
		}while(!s.isEnrolled() && 
				!showConfirmationDialog(
						STUDENT_REGISTRATION, 
						STUDENT_MUST_BE_ENROLLED_IN_A_DISCIPLINE_AT_LEAST_CANCEL_REGISTRATION
				)
		);
		
		return s.isEnrolled() ? s : null;
	}

	/** 
	 * Reads and adds the disciplines to the student until he reaches the maximum allowed or the user stops reading. 
	 * 
	 * @param student the student to be enrolled.
	 */
	private void enroll(Student student) {
		while(student.canEnrollInAnotherDiscipline()) {
			var discipline = readDisciplineData(student);
			if(discipline == null)
				break;
			
			student.addDiscipline(discipline.getFirst(), discipline.getSecond());
			
			if(student.canEnrollInAnotherDiscipline() && 
					!showConfirmationDialog(STUDENT_REGISTRATION, ENROLL_IN_ANOTHER_DISCIPLINE))
				break;
		}
	}
	
	/**
	 * Reads a discipline info validating if student is already enrolled.
	 * 
	 * @param student the target student.
	 * @return the read discipline as an <code>Pair<String, Double></code>
	 */
	private Pair<String, Double> readDisciplineData(Student student) {
		String name = showStringInputDialog(
				DISCIPLINE_ENROLLMENT, 
				NAME, 
				input -> {
					var message = createEmptyStringValidator(NAME_CANNOT_BE_BLANK)
							.getErrorMessage(input);
					
					if(message == DEFAULT_SUCCESS_MESSAGE) {
						return student.isEnrolled(input) 
								? STUDENT_ALREADY_ENROLLED_IN_THIS_DISCIPLINE : DEFAULT_SUCCESS_MESSAGE;
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
		
		return new Pair<String, Double>(name, grade);
	}

	/**
	 * Searches for a student based on their name.
	 * 
	 * @param students where to search.
	 * @param name the target name.
	 * @return the found student or null.
	 */
	private Student searchStudent(final List<Student> students, String name) {
		var found = students.stream()
				.filter(s-> s.getName().equalsIgnoreCase(name))
				.collect(Collectors.toList());
		
		if(!found.isEmpty())
			return found.get(0);
		
		return null;
	}
	
	/**
	 * Searches for a student and displays his information in a dialog box.
	 * 
	 * @param students where to search.
	 */
	public void searchStudent(final List<Student> students) {
		if(isClassEmpty())
		{
			MessageDialog.showAlertDialog(SEARCH_STUDENT, NO_STUDENTS_REGISTERED);
			return;
		}
		
		var name = showStringInputDialog(
				SEARCH_STUDENT, 
				NAME, 
				createEmptyStringValidator(NAME_CANNOT_BE_BLANK)
		);
		
		if(name != null)
		{
			var student = searchStudent(students, name);
			
			if(student == null)
				showAlertDialog(SEARCH_STUDENT, STUDENT_NOT_FOUND);
			else
				showTextMessage(STUDENT_DATA, student.toReportString());
		}
	}
	
	/**
	 * Reads a student name and remove it if found.   
	 * 
	 * @param students where to look/remove.
	 */
	public void removeStudent(final ArrayList<Student> students) {
		if(isClassEmpty())
		{
			MessageDialog.showAlertDialog(DELETE_STUDENT, NO_STUDENTS_REGISTERED);
			return;
		}
		
		var name = showStringInputDialog(
				SEARCH_STUDENT, 
				NAME, 
				createEmptyStringValidator(NAME_CANNOT_BE_BLANK)
		);
		
		if(name == null) 
			return;
		
		var student = searchStudent(students, name);
		
		if(student == null)
			showAlertDialog(SEARCH_STUDENT, STUDENT_NOT_FOUND);
		else {
			var message = student.toReportString() + 
					String.format("\n%s", ARE_YOU_SURE_YOU_WANT_TO_DELETE_THIS_STUDENT);
			
			if(showConfirmationDialog(DELETE_STUDENT, message)){
				students.remove(student);
				Student.decreaseStudentsCount();
				showInformationDialog(DELETE_STUDENT, STUDENT_SUCCESSFULY_DELETED);
			}
		}
	}

	/**
	 * Searches and displays the discipline info for a student based on user input.  
	 * 
	 * @param students the data source.
	 */
	public void searchDiscipline(final List<Student> students) {
		if(isClassEmpty())
		{
			MessageDialog.showAlertDialog(SEARCH_DISCIPLINE, NO_STUDENTS_REGISTERED);
			return;
		}
		
		var studentNameOrRegistration = showStringInputDialog(
				SEARCH_DISCIPLINE, 
				STUDENT_NAME_OR_REGISTRATION, 
				createEmptyStringValidator(THIS_FIELD_CANNOT_BE_EMPTY)
		);
		if(studentNameOrRegistration == null)
			return;
		
		var discipline = showStringInputDialog(
				SEARCH_DISCIPLINE, 
				DISCIPLINE_NAME, 
				createEmptyStringValidator(THIS_FIELD_CANNOT_BE_EMPTY)
		);
		if(discipline == null)
			return;
		
		var student = searchStudent(students, studentNameOrRegistration);
		
		if(student == null)
			student = searchDiscipline(students, studentNameOrRegistration);
		
		if(student == null)
		{
			showAlertDialog(SEARCH_DISCIPLINE, STUDENT_NOT_FOUND);
			
			return;
		}
		
		var grade = searchDiscipline(student, discipline);
		if(grade == null)
		{
			showAlertDialog(
					SEARCH_STUDENT, 
					String.format(THE_STUDENT_IS_NOT_ENROLLED_ON_THIS_SUBJECT, studentNameOrRegistration)
			);
			
			return;
		}
		
		var message = String.format("%s\n\t%s: %1.2f",student.getName(), discipline, grade);
		
		
		showInformationDialog(SEARCH_STUDENT, message);
	}

	/**
	 * Searches a discipline based on the student's registration number
	 * 
	 * @param students where to search.
	 * @param registration 
	 * @return the student found or null if not found.
	 */
	private Student searchDiscipline(final List<Student> students, String registration) {
		var found = students.stream()
				.filter(s-> s.getRegistration().equalsIgnoreCase(registration))
				.collect(Collectors.toList());
		
		if(!found.isEmpty())
			return found.get(0);

		return null;
	}
	
	/**
	 * Searches a grade for an student in a specific discipline.
	 * 
	 * @param student the target student.
	 * @param discipline the target discipline.
	 * @return the grade if student is enrolled in that discipline or null if not enrolled.
	 */
	private Float searchDiscipline(Student student, String discipline) {
		return student.getGrade(discipline);
	}

	/**
	 * Prepares a general report containing the list of approved and failed students, the count of failed and
	 * approved students and their percentages
	 *
	 * @param students the data source.
	 */
	public void report(final List<Student> students) {
		if(isClassEmpty()) {
			showInformationDialog(REPORT, NO_DATA_FOUND);
			return;
		}
		
		var message = new StringBuilder();
		var studentsCount = Student.getStudentsCount();
		var reprovedCount = 0;
		var approvedCount = 0;
		
		for(Student s : students){
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
			.append(String.format("\n%s = %1.1f%%", PERCENTAGE_OF_APPROVED, percentageOfApproved ))
			.append(String.format("\n%s = %1.1f%%", PERCENTAGE_OF_REPROVED, percentageOfReproved))
		;
		
		showTextMessage(REPORT, message.toString());
	}

	/**
	 * Generates a message containing the student's basic information and approval status.
	 * 
	 * @param student the target student.
	 * @return the generated message.
	 */
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

	/**
	 * Generates a default header with separators chars.
	 * 
	 * @param header the header title.
	 * @return the generated string.
	 */
	private String generateSeparator(String header) {
		final int charCount = 20;
		var separatorChars="";
		
		for(int i = 0; i < charCount; i++) {
			separatorChars+="-";
		}
		
		return String.format("\n%s %s %s", separatorChars, header, separatorChars);
	}
	
	/**
	 * Calculates the equivalent percentage of one value over another.
	 * 
	 * @param value the partial value.
	 * @param from the total value.
	 * @return the equivalent percentage.
	 */
	private double calculatePercentage(double value, double from) {
		return value / from * 100;
	}

	private String getRegistrationSequencialNumber(final String registration) {
		try{
			return registration.split("-")[1];
		}catch (Exception e) {
			return "";
		}
	}

	public void updateStudentData(final ArrayList<Student> students) {
		if(isClassEmpty())
		{
			MessageDialog.showAlertDialog(UPDATE_STUDENT_DATA, NO_STUDENTS_REGISTERED);
			return;
		}
		
		var name = showStringInputDialog(
				UPDATE_STUDENT_DATA, 
				STUDENT_NAME, 
				createEmptyStringValidator(NAME_CANNOT_BE_BLANK)
			);
		
		if(name == null)
			return;
		
		var student = searchStudent(students, name);
		
		if(student == null)
		{
			showAlertDialog(UPDATE_STUDENT_DATA, STUDENT_NOT_FOUND);
			return;
		}
		
		performReadAndUpdate(students, student);
	}

	private void performReadAndUpdate(ArrayList<Student> students, Student student) {
		var name = showStringInputDialog(
				UPDATE_STUDENT_DATA, 
				NAME, 
				createEmptyStringValidator(NAME_CANNOT_BE_BLANK)
			);
		if(name == null)
			return;
		
		String course;
		
		do{
			course = showStringInputDialog(
						UPDATE_STUDENT_DATA, 
						COURSE, 
						createEmptyStringValidator(NAME_CANNOT_BE_BLANK)
					);
				
		}while(
				course == null && 
				!showConfirmationDialog(
					UPDATE_STUDENT_DATA, 
					YOUR_CHANGES_WILL_BE_DISCARDED_ARE_YOU_SURE_YOU_WANT_TO_CANCEL
				)
		);
		if(course == null)
			return;
		
		student.setCourse(course);
		student.setName(name);
		
		if(showConfirmationDialog(
				UPDATE_STUDENT_DATA,
				RECORD_UPDATED_SUCCESSFULLY + "\n" + UPDATE_GRADES + "?"
		)) {
			updateStudentGrades(student);
		}
	}
	
	private void updateStudentGrades(Student student) {
		do {
			var disciplineName = showStringInputDialog(
					UPDATE_STUDENT_DATA, 
					DISCIPLINE_NAME, 
					createEmptyStringValidator(NAME_CANNOT_BE_BLANK)
				);
			
			var grade = InputDialog.showDoubleInputDialog(UPDATE_GRADES, GRADE, gradeRangeValidator);
			
			if(grade != null) {	
				if(student.updateGrade(disciplineName, grade)) {
					showInformationDialog(UPDATE_GRADES, RECORD_UPDATED_SUCCESSFULLY);
				}else {
					showAlertDialog(UPDATE_GRADES, STUDENT_NOT_ENROLLED_IN_THIS_DISCIPLINE);
				}
			}
		}while(showConfirmationDialog(UPDATE_STUDENT_DATA, UPDATE_ANOTHER_GRADE));
	}

	private boolean isClassEmpty() {
		return Student.getStudentsCount() == 0;
	}
}