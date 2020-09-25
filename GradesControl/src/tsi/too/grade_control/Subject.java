package tsi.too.grade_control;

public class Subject {
	private final double MIN_FOR_APPROVAL = 6;
	private static final double MAX_GRADE = 10;
	private static final double MIN_GRADE = 0;
	
	private String name;
	private double grade;
	
	public Subject(String name, double grade) {
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
	
	public boolean isAproved() {
		return grade >= MIN_FOR_APPROVAL;
	}
	
	public static boolean isValidGrade(double grade) {
		return grade <= MAX_GRADE && grade >= MIN_GRADE;
	}
	
	@Override
	public String toString() {
		return "Subject {name= " + name + ", grade= " + grade + "}";
	}
}