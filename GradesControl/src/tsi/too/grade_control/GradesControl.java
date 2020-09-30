package tsi.too.grade_control;

import java.util.ArrayList;

import tsi.too.grade_control.controller.MenuController;
import tsi.too.grade_control.model.Student;

public class GradesControl {
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