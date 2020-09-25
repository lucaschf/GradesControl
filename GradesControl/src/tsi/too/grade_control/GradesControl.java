package tsi.too.grade_control;

import java.util.ArrayList;

import tsi.too.grade_control.controller.MenuController;

public class GradesControl {
	private ArrayList<Student> students;
	
	private ArrayList<Student>createClass(){
		return new ArrayList<>();
	}
	
	public GradesControl() {
		students = createClass();
		controller = MenuController.getInstance(students);
	}
	
	private MenuController controller;
	
	public void execute() {
		controller.showMenu();
	}
	
	public static void main(String[] args) {
		new GradesControl().execute();
	}
}
