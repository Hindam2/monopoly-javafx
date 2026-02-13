package javafxapplication1;

public class GoToJailCard extends Card {
    public GoToJailCard(String text) { super(text); }
    @Override
    public String apply(Game game, Player p) {
        p.setInJail(true);
        p.setPosition(10);
        p.setGoPassCounterAtJail(game.getTotalGoPasses());
        return text;
    }
}
