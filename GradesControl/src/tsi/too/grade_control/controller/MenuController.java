package tsi.too.grade_control.controller;

import static tsi.too.grade_control.Constants.DELETE_STUDENT;
import static tsi.too.grade_control.Constants.EXIT;
import static tsi.too.grade_control.Constants.GRADES_CONTROLLER;
import static tsi.too.grade_control.Constants.REGISTER_STUDENT;
import static tsi.too.grade_control.Constants.REPORT;
import static tsi.too.grade_control.Constants.SEARCH_STUDENT;
import static tsi.too.grade_control.Constants.SEARCH_SUBJECT;
import static tsi.too.grade_control.Constants.UPDATE_STUDENT_DATA;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tsi.too.grade_control.Student;
import tsi.too.message_dialog.InputDialog;

public class MenuController {
	private static MenuController instance;
	private StudentController studentController = StudentController.getInstance();

	private ArrayList<Student> students;

	private final List<String> OPTIONS = Arrays.asList(
			REGISTER_STUDENT,
			SEARCH_STUDENT,
			SEARCH_SUBJECT,
			UPDATE_STUDENT_DATA,
			DELETE_STUDENT,
			REPORT
			);

	private MenuController (ArrayList<Student> students){
		this.students = students;
	}

	/**
	 * Ensures that only one instance of this class is created.
	 *
	 * @return an instance of this class.
	 */
	public static MenuController getInstance(ArrayList<Student> students) {
		synchronized (MenuController.class) {
			if (instance == null)
				instance = new MenuController(students);

			return instance;
		}
	}

	public void showMenu() {
		InputDialog.showMenuDialog(
				GRADES_CONTROLLER, 
				"", 
				OPTIONS, 
				EXIT, 
				this::execute
		);
	}

	private void execute(String action) {
		if(action == null)
			return;
		
		switch (action) {
			case REGISTER_STUDENT:
				studentController.registerStudent(students);
				break;
			case SEARCH_STUDENT:
				studentController.searchStudent(students);
				break;
			case UPDATE_STUDENT_DATA:
				break;
			case SEARCH_SUBJECT:
				break;
			case DELETE_STUDENT:
				studentController.removeStudent(students);
				break;
			case REPORT:
				break;
			default:
				break;
		}
	}
}