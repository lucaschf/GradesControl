package tsi.too.grade_control.model;

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
	
	public boolean addDiscipline(String disciplineName, double grade) {
		if(enrolledDisciplinesCount >= MAX_SUBSCRIPTION_ALLWOED - 1)
			return false;

		enrolledDisciplines[enrolledDisciplinesCount] = new Discipline(disciplineName, grade);
		enrolledDisciplinesCount++;
		
		return true;
	}

	/**
	 * The student will be considered approved if his grades in the <code>MAX_SUBSCRIPTION_ALLWOED</code>
	 * subjects are equal to or higher than <code>Discipline.MIN_FOR_APPROVAL</code>.
	 *  
	 * @return the approval status
	 */
	public boolean isAproved() {
		return getEnrolledDisciplines().stream()
				.filter(d -> d!= null && !d.isAproved()).count() == 0;
	}
	
	/**
	 * Checks if student can enroll in another discipline or if is already enrolled in the max allowed. 
	 * @return true if can enroll, false otherwise.
	 */
	public boolean canEnrollInAnotherDiscipline() {
		return enrolledDisciplinesCount < MAX_SUBSCRIPTION_ALLWOED;
	}
	
	/**
	 * Checks if student is enrolled in at least one discipline.
	 * 
	 * @return the checking result.
	 */
	public boolean isEnrolled() {
		return enrolledDisciplinesCount > 0;
	}

	/**
	 * Checks if student is enrolled in a specific discipline.
	 * 
	 * @param discipline the name of discipline to be checked
	 * @return true or false
	 */
	public boolean isEnrolled(String discipline) {
		return getEnrolledDisciplines().stream()
				.filter(d -> d != null && d.getName().equalsIgnoreCase(discipline))
				.count() > 0;
	}
	
	/**
	 * Recovers all registered disciplines, cloning them without breaking the composition
	 * 
	 * @return all enrolled disciplines as a list.
	 */
	public List<Discipline> getEnrolledDisciplines(){
		return retriveEnrolledDisciplines().stream().map(d -> d.clone()).collect(Collectors.toList());
	}

	private List<Discipline> retriveEnrolledDisciplines(){
		var l = new ArrayList<Discipline>(Arrays.asList(enrolledDisciplines));		
		l.removeAll(Collections.singleton(null));
		
		return l;
	}
	
	/**
	 * Gets all enrolled disciplines based on the name of a discipline
	 * 
	 * @param name the discipline to be searched
	 * @return all disciplines found 
	 */
	private List<Discipline> getDisciplines(String name){
		return getEnrolledDisciplines()
				.stream().filter(s-> s.getName().equalsIgnoreCase(name))
				.collect(Collectors.toList());	
	}
	
	/**
	 * Gets the grade based on a specific discipline
	 * 
	 * @param discipline
	 * @return the grade found or null if student not enrolled in the discipline.
	 */
	public Float getGrade(final String discipline) {
		var disciplinesFound = getDisciplines(discipline).stream().mapToDouble(Discipline::getGrade).findFirst();
		
		return disciplinesFound == null || disciplinesFound.isEmpty() ? null : (float)disciplinesFound.getAsDouble();
	}
	
	/**
	 * Tries to update the student grade for a discipline.
	 *  
	 * @param disciplineName the target discipline name.
	 * @param grade the new grade value.
	 * @return true if success, false otherwise.
	 */
	public boolean updateGrade(String disciplineName, double grade ) {
		var discipline = retriveEnrolledDisciplines().stream()
				.filter(d -> d!= null && d.getName().equalsIgnoreCase(disciplineName))
				.collect(Collectors.toList());
		
		if(discipline.isEmpty())
			return false;
		
		discipline.get(0).setGrade(grade);
		
		return true;	
	}	
	
	@Override
	public String toString() {
		return "Student {registration= " + registration +
				", course= " + course + 
				", name= " + name + 
				", disciplines= " + getEnrolledDisciplines() 
				+ "}";
	}	
}