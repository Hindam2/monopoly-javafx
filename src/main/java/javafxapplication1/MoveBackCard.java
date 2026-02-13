package javafxapplication1;

public class MoveBackCard extends Card {
    private final int steps;
    public MoveBackCard(String text, int steps) { super(text); this.steps = steps; }
    public int getSteps() { return steps; }
    @Override
    public String apply(Game game, Player p) {
        int pos = p.getPosition();
        int N = game.getBoard().size();
        for (int i = 0; i < steps; i++) { pos = (pos - 1 + N) % N; }
        p.setPosition(pos);
        return text;
    }
}
