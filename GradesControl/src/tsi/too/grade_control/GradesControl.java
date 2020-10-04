package tsi.too.grade_control;

import java.util.ArrayList;

import tsi.too.grade_control.controller.MenuController;
import tsi.too.grade_control.model.Student;

public class GradesControl {
	/* We can remove this here and define it as an instance variable of the StudentController class.
	 * I left it here because it is requested in the statement of the problem.
	 */
	private ArrayList<Student> students; 
	private MenuController controller;
	
	public GradesControl() {
		students = createClass();
		controller = MenuController.getInstance(students);
	}
	
	private ArrayList<Student>createClass(){
		return new ArrayList<>();
	}
	
	public void execute() {
		controller.showMenu();
	}
	
	public static void main(String[] args) {
		new GradesControl().execute();
	}
}