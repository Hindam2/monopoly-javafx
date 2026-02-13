package javafxapplication1;

import javafx.scene.paint.Color;

public class Player {
    private final String name;
    private final Color color;
    private int balance = 1500;
    private int position = 0;
    private boolean inJail = false;
    private int jailTurns = 0;
    private int goojfCount = 0;

    private int goPassCounterAtJail = 0;

    public Player(String name, Color color) { this.name = name; this.color = color; }
    public String getName() { return name; }
    public Color getColor() { return color; }
    public int getBalance() { return balance; }
    public void addBalance(int amt) { balance += amt; }
    public void subtractBalance(int amt) { balance -= amt; }
    public int getPosition() { return position; }
    public void setPosition(int pos) { this.position = pos; }

    public boolean isInJail() { return inJail; }
    public void setInJail(boolean v) { inJail = v; if (!v) jailTurns = 0; }
    public int getJailTurns() { return jailTurns; }
    public void incrementJailTurns() { jailTurns++; }
    public void resetJailTurns() { jailTurns = 0; }

    public boolean hasGetOutOfJailFree() { return goojfCount > 0; }
    public void addGetOutOfJailFree() { goojfCount++; }
    public void useGetOutOfJailFree() { if (goojfCount > 0) goojfCount--; }
    public int getGoojfCount() { return goojfCount; }

    public int getGoPassCounterAtJail() { return goPassCounterAtJail; }
    public void setGoPassCounterAtJail(int v) { goPassCounterAtJail = v; }
}
