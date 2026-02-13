package javafxapplication1;

public class JailSpace extends Space {
    public JailSpace(String name) { this.name = name; }
    @Override public SpaceType getType() { return SpaceType.JAIL; }
    @Override public String onLand(Game game, Player p) { return "Just Visiting"; }
}
