package tsi.too.grade_control.model;

public class Discipline implements Cloneable{
	private final double MIN_FOR_APPROVAL = 6;

	private String name;
	private double grade;

	public Discipline(final String name, final double grade) {
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

	@Override
	public Discipline clone(){ 
		return new Discipline(name, grade);
	}
}