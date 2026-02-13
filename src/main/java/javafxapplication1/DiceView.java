package javafxapplication1;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class DiceView {
    private final StackPane d1 = makeDie();
    private final StackPane d2 = makeDie();
    public StackPane die1() { return d1; }
    public StackPane die2() { return d2; }
    public void show(int a, int b) { setDieFace(d1, a); setDieFace(d2, b); }
    private StackPane makeDie() {
        StackPane pane = new StackPane();
        pane.setPrefSize(64, 64);
        Rectangle r = new Rectangle(64, 64);
        r.setArcWidth(12); r.setArcHeight(12);
        r.setFill(Color.WHITE); r.setStroke(Color.BLACK);
        pane.getChildren().add(r);
        setDieFace(pane, 1);
        return pane;
    }
    private void setDieFace(StackPane pane, int val) {
        pane.getChildren().removeIf(n -> n instanceof Circle);
        double[][] pos = switch (val) {
            case 1 -> new double[][]{{0,0}};
            case 2 -> new double[][]{{-15,-15},{15,15}};
            case 3 -> new double[][]{{-15,-15},{0,0},{15,15}};
            case 4 -> new double[][]{{-15,-15},{-15,15},{15,-15},{15,15}};
            case 5 -> new double[][]{{-15,-15},{-15,15},{0,0},{15,-15},{15,15}};
            case 6 -> new double[][]{{-15,-15},{-15,0},{-15,15},{15,-15},{15,0},{15,15}};
            default -> new double[][]{{0,0}};
        };
        for (double[] p : pos) {
            Circle c = new Circle(5, Color.BLACK);
            c.setTranslateX(p[0]); c.setTranslateY(p[1]);
            pane.getChildren().add(c);
        }
    }
}
