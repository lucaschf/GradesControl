package tsi.too.grade_control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Student {
	private final int SUBJECTS = 5;
	private static int students = 0;

	private String registration;
	private String course;
	private String name;
	private Subject[] subjects = new Subject[SUBJECTS];

	private int registeredSubjects = 0;

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

	public static int getStudents() {
		return students;
	}

	public boolean addSubject(Subject subject) {
		if(registeredSubjects >= SUBJECTS - 1)
			return false;

		subjects[registeredSubjects] = subject;
		registeredSubjects++;
		
		return true;
	}

	public boolean isAproved() {
		for(int i = 0; i < registeredSubjects; i++){
			if(!subjects[i].isAproved())
				return false;
		}

		return true;
	}
	
	public boolean canRegisterSubject() {
		return registeredSubjects < SUBJECTS -1;
	}

	public boolean isEnrolled(String subjectName) {
		if(registeredSubjects == 0)
			return false;
		
		for(int i = 0; i < registeredSubjects; i++) {
			if(subjects[i].getName().equalsIgnoreCase(subjectName))
				return true;
		}
		
		return false;
	}
	
	public List<Subject> getSubjects(){
		return new ArrayList<Subject>();
	}
	
	@Override
	public String toString() {
		return "Student {registration= " + registration +
				", course= " + course + 
				", name= " + name + 
				", subjects= " + Arrays.toString(subjects) + 
				", registeredSubjects= " + registeredSubjects 
				+ "}";
	}
}