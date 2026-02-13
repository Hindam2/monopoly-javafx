package javafxapplication1;

public class ChanceSpace extends Space {
    public ChanceSpace(String name) { this.name = name; }
    @Override public SpaceType getType() { return SpaceType.CHANCE; }
    @Override public String onLand(Game game, Player p) {
        Card card = game.drawChance();
        return card.apply(game, p);
    }
}
