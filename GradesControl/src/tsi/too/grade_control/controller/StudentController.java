package tsi.too.grade_control.controller;

import static tsi.too.grade_control.Constants.*;
import static tsi.too.message_dialog.InputDialog.InputValidator.DEFAULT_SUCCESS_MESSAGE;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import tsi.too.grade_control.Student;
import tsi.too.grade_control.Subject;
import tsi.too.message_dialog.InputDialog;
import tsi.too.message_dialog.MessageDialog;

public class StudentController {

	private static StudentController instance;

	private StudentController() {}

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

		if(s != null)
			students.add(s);
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
				GRADES_CONTROLLER, 
				REGISTRATION_NUMBER, 
				input -> isValidRegistration(input, students)
				);
		if(registration == null)
			return null;

		String name = InputDialog.showStringInputDialog(
				GRADES_CONTROLLER, 
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
				GRADES_CONTROLLER, 
				COURSE,
				InputDialog.createEmptyStringValidator(COURSE_CANNOT_BE_BLANK)
				);
		if(course == null)
			return null;

		var s = new Student(registration, name, course);
		
		while(s.canRegisterSubject()) {
			var subject = readSubjectData(s);
			if(subject == null)
				break;
			
			s.addSubject(subject);
		}
		
		return s;
	}

	private Subject readSubjectData(Student s) {
		String name = InputDialog.showStringInputDialog(
				GRADES_CONTROLLER, 
				NAME, 
				input -> {
					var message = InputDialog.createEmptyStringValidator(NAME_CANNOT_BE_BLANK).getErrorMessage(input);
					if(message == DEFAULT_SUCCESS_MESSAGE) {
						return s.isEnrolled(input) ? STUDENT_ALREADY_ENROLLED_ON_THIS_SUBJECT : DEFAULT_SUCCESS_MESSAGE;
					}
					
					return message;
				}
		);
		if(name == null)
			return null;
				
		Double grade = InputDialog.showDoubleInputDialog(
				GRADES_CONTROLLER, 
				GRADE,
				InputDialog.createRangeValidator(0, 10, GRADE_MUST_BE_BETWEEN_ZERO_AND_TEN )
		);
		if(grade == null)
			return null;
		
		return new Subject(name, grade);
	}

	private Student searchStudent(final List<Student> students, String name) {
		var found = students.stream().filter(s-> s.getName() == name).collect(Collectors.toList());
		
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
				MessageDialog.showInformationDialog(STUDENT_DATA, createStudentMessage(student));
		}
	}

	private String createStudentMessage(Student student) {
		var message = new StringBuilder()
				.append(String.format("\n%s: %s", REGISTRATION_NUMBER, student.getRegistration()))
				.append(String.format("\n%s: %s", NAME, student.getName()))
				.append(String.format("\n%s: %s", COURSE, student.getCourse()))
				.append("\n"+SUBJECTS)
				;
		
		for(Subject s: student.getSubjects()) {
			message = message.append(String.format("\n\n\t%s: %s",NAME, s.getName()))
				.append(String.format("\nt%s: %1.2f", GRADE, s.getGrade()));
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
				MessageDialog.showInformationDialog(DELETE_STUDENT, STUDENT_SUCCESSFULY_DELETED);
			}
		}
	}
}