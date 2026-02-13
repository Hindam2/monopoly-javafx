package javafxapplication1;

public class GoToJailSpace extends Space {
    public GoToJailSpace(String name) { this.name = name; }
    @Override public SpaceType getType() { return SpaceType.GOTO_JAIL; }
    @Override public String onLand(Game game, Player p) {
        p.setInJail(true);
        p.setPosition(10);
        p.setGoPassCounterAtJail(game.getTotalGoPasses());
        return "Go directly to Jail!";
    }
}
