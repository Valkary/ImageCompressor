package tools;

public class Tuple<X, Y> {
    public final X x;
    public final Y y;

    public Tuple(X x, Y y) {
        this.x = x;
        this.y = y;
    }

    public void print() {
        System.out.println("Tup: {\n  x: " + x + "\n  y: " + y + "\n}");
    }
}
