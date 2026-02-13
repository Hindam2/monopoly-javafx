package javafxapplication1;

import java.util.Random;

public class Dice {
    private final Random r = new Random();
    public int roll() { return r.nextInt(6) + 1; }
}
