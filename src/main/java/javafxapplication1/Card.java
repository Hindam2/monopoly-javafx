package javafxapplication1;

public abstract class Card {
    protected final String text;
    public Card(String text) { this.text = text; }
    public abstract String apply(Game game, Player p);
    public String getText() { return text; }
}
