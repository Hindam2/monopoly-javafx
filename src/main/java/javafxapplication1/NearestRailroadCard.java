package javafxapplication1;

public class NearestRailroadCard extends Card {
    public NearestRailroadCard(String text) { super(text); }
    @Override
    public String apply(Game game, Player p) {
        int[] rrIndices = {5, 15, 25, 35};
        int pos = p.getPosition();
        int next = -1;
        for (int rr : rrIndices) { if (rr > pos) { next = rr; break; } }
        if (next == -1) next = 5;
        if (next < pos) { p.addBalance(200); game.markPassGo(); }
        p.setPosition(next);
        Space s = game.getSpace(next);
        return s.onLand(game, p) + " (moved to nearest Railroad)";
    }
}
