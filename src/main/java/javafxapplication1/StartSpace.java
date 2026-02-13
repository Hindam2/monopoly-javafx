package javafxapplication1;

public class StartSpace extends Space {
    public StartSpace(String name) { this.name = name; }
    @Override public SpaceType getType() { return SpaceType.START; }
    @Override public String onLand(Game game, Player p) {
        p.addBalance(200);
        game.markPassGo();
        return "Collect $200";
    }
}
