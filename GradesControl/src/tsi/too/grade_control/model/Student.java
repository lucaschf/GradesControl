package tsi.too.grade_control.model;

import static tsi.too.grade_control.Constants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class Student {
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
	
	public boolean addDiscipline(String disciplineName, double grade) {
		if(enrolledDisciplinesCount >= MAX_SUBSCRIPTION_ALLWOED - 1)
			return false;

		enrolledDisciplines[enrolledDisciplinesCount] = new Discipline(disciplineName, grade);
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
	
	private List<Discipline> getEnrolledDisciplines(){
		var l = new ArrayList<Discipline>(Arrays.asList(enrolledDisciplines));
		l.removeAll(Collections.singleton(null));
		
		return l;
	}

	private List<Discipline> getDisciplines(String name){
		return getEnrolledDisciplines()
				.stream().filter(s-> s.getName().equalsIgnoreCase(name))
				.collect(Collectors.toList());	
	}
	
	public Float getGrade(final String discipline) {
		var disciplinesFound = getDisciplines(discipline);
		
		if(disciplinesFound.isEmpty())
			return null;
		
		return (float) disciplinesFound.get(0).getGrade();
	}
	
	public boolean updateGrade(String disciplineName, double grade ) {
		for(Discipline d : enrolledDisciplines) {
			if(d == null)
				return false;
			
			if(disciplineName.equalsIgnoreCase(d.getName()))
			{
				d.setGrade(grade);
				return true;
			}
		}
		
		return false;
	}
	
	public String toReportString() {
		var message = new StringBuilder()
				.append(String.format("%s: %s", REGISTRATION_NUMBER, getRegistration()))
				.append(String.format("\n%s: %s", NAME, getName()))
				.append(String.format("\n%s: %s", COURSE, getCourse()))
				.append(String.format("\n\n%s:", DISCIPLINES))
				;
		
		if(enrolledDisciplinesCount == 0)
			message.append(String.format("\n%s", NO_DATA_FOUND));
		else {
			for(Discipline s: enrolledDisciplines) {
				if(s == null)
					break;
				
				message = message.append(String.format("\n\t%s: %s",NAME, s.getName()))
					.append(String.format("\n\t%s: %1.2f\n", GRADE, s.getGrade()));
			}
		}
		
		return message.toString();
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
	
	private class Discipline {
		private final double MIN_FOR_APPROVAL = 6;
		
		private String name;
		private double grade;
		
		private Discipline(final String name, final double grade) {
			super();
			this.name = name;
			this.grade = grade;
		}

		public String getName() {
			return name;
		}
		
		public double getGrade() {
			return grade;
		}
		
		public void setGrade(double grade) {
			this.grade = grade;
		}
		
		public boolean isAproved() {
			return grade >= MIN_FOR_APPROVAL;
		}
		
		@Override
		public String toString() {
			return "name= " + name + ", grade= " + grade + "";
		}
	}
}