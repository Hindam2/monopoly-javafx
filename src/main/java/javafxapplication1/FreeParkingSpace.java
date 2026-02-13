package javafxapplication1;

public class FreeParkingSpace extends Space {
    public FreeParkingSpace(String name) { this.name = name; }
    @Override public SpaceType getType() { return SpaceType.FREE_PARKING; }
    @Override public String onLand(Game game, Player p) { return "Free Parking"; }
}
