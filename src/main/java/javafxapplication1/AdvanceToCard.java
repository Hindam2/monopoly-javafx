package javafxapplication1;

public class AdvanceToCard extends Card {
    private final int targetIndex;
    private final boolean collectGOOnPass;
    public AdvanceToCard(String text, int targetIndex, boolean collectGOOnPass) {
        super(text); this.targetIndex = targetIndex; this.collectGOOnPass = collectGOOnPass;
    }
    public int getTargetIndex() { return targetIndex; }
    public boolean isCollectGOOnPass() { return collectGOOnPass; }
    @Override
    public String apply(Game game, Player p) {
        int pos = p.getPosition();
        int N = game.getBoard().size();
        while (pos != targetIndex) {
            pos = (pos + 1) % N;
            if (collectGOOnPass && pos == 0) { p.addBalance(200); game.markPassGo(); }
        }
        p.setPosition(targetIndex);
        if (targetIndex == 0) { game.markPassGo(); }
        return text;
    }
}
