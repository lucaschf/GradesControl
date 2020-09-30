package tsi.too.grade_control.util;

public class Pair<E, T>{
	
	/**
     * The first element of this <code>Pair</code>
     */
    private E first;

    /**
     * The second element of this <code>Pair</code>
     */
    private T second;

    /**
     * Constructs a new <code>Pair</code> with the given values.
     * 
     * @param first  the first element
     * @param second the second element
     */
    public Pair(E first, T second) {

        this.first = first;
        this.second = second;
    }

    public E getFirst() {
		return first;
	}
    
    public T getSecond() {
		return second;
	}
}