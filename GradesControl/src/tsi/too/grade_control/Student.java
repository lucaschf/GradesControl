package tsi.too.grade_control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Student implements Cloneable{
	private final int MAX_SUBSCRIPTION_ALLWOED = 5;
	private static int studentsCount = 0;

	private String registration;
	private String course;
	private String name;
	private Discipline[] enrolledDisciplines = new Discipline[MAX_SUBSCRIPTION_ALLWOED];

	private int enrolledDisciplinesCount = 0;

	public Student(String registration, String name, String course) {
		super();
		this.registration = registration.toUpperCase();
		this.course = course;
		this.name = name;
	}

	public String getRegistration() {
		return registration;
	}

	public String getCourse() {
		return course;
	}

	public void setCourse(String course) {
		this.course = course;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static int getStudentsCount() {
		return studentsCount;
	}
	
	public static void increaseStudentsCount() {
		studentsCount++;
	}
	
	public static void decreaseStudentsCount() {
		if(studentsCount > 0)
			studentsCount--;
	}
	
	public boolean addSubject(Discipline subject) {
		if(enrolledDisciplinesCount >= MAX_SUBSCRIPTION_ALLWOED - 1)
			return false;

		enrolledDisciplines[enrolledDisciplinesCount] = subject;
		enrolledDisciplinesCount++;
		
		return true;
	}

	public boolean isAproved() {
		for(int i = 0; i < enrolledDisciplinesCount; i++){
			if(!enrolledDisciplines[i].isAproved())
				return false;
		}

		return true;
	}
	
	public boolean canEnrollInAnotherDiscipline() {
		return enrolledDisciplinesCount < MAX_SUBSCRIPTION_ALLWOED;
	}
	
	public boolean isEnrolled() {
		return enrolledDisciplinesCount > 0;
	}

	public boolean isEnrolled(String subjectName) {
		if(enrolledDisciplinesCount == 0)
			return false;
		
		for(int i = 0; i < enrolledDisciplinesCount; i++) {
			if(enrolledDisciplines[i].getName().equalsIgnoreCase(subjectName))
				return true;
		}
		
		return false;
	}
	
	public List<Discipline> getEnrolledDisciplines(){
		var l = new ArrayList<Discipline>(Arrays.asList(enrolledDisciplines));
		l.removeAll(Collections.singleton(null));
		
		return l;
	}

	public List<Discipline> getDisciplines(String name){
		return getEnrolledDisciplines()
				.stream().filter(s-> s.getName().equalsIgnoreCase(name))
				.collect(Collectors.toList());	
	}
	
	public boolean updateGrade(Discipline discipline) {
		for(Discipline d : enrolledDisciplines) {
			if(d == null)
				return false;
			
			if(discipline.getName().equalsIgnoreCase(d.getName()))
			{
				d.setGrade(discipline.getGrade());
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return "Student {registration= " + registration +
				", course= " + course + 
				", name= " + name + 
				", subjects= " + getEnrolledDisciplines() + 
				", registeredSubjects= " + enrolledDisciplinesCount 
				+ "}";
	}
}