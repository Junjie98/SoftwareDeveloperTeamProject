package structures.handlers;

/**
 * A generic pair class that makes it easier to bundle two values.
 * @param <T>
 * @param <U>
 * @author Yu-Sung Hsu (2540296h@student.gla.ac.uk)
 */

public class Pair<T, U> {
    private T first;
    private U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public U getSecond() {
        return second;
    }

    public void setFirst(T first) {
        this.first = first;
    }

    public void setSecond(U second) {
        this.second = second;
    }

    public boolean equals(Pair<T, U> another) {
        return this.toString().equals(another.toString());
    }

    public static Pair<Integer, Integer> copyIntegerPair(Pair<Integer, Integer> source) {
        return new Pair<>(source.getFirst(), source.getSecond());
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
