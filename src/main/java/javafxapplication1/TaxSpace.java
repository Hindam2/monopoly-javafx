package javafxapplication1;

public class TaxSpace extends Space {
    private final int amount;
    public TaxSpace(String name, int amount) { this.name = name; this.amount = amount; }
    @Override public SpaceType getType() { return SpaceType.TAX; }
    @Override public String onLand(Game game, Player p) {
        p.subtractBalance(amount);
        return "Paid tax $" + amount;
    }
}
