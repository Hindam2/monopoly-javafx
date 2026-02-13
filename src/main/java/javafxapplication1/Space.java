package javafxapplication1;

public abstract class Space {
    protected String name;
    public String getName() { return name; }
    public abstract SpaceType getType();
    public abstract String onLand(Game game, Player p);
}
