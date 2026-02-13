package javafxapplication1;

public class Utility extends Space implements Ownable {
    private final int price;
    private Player owner;

    public Utility(String name, int price) { this.name = name; this.price = price; }

    @Override public SpaceType getType() { return SpaceType.UTILITY; }
    @Override public int getPrice() { return price; }
    @Override public Player getOwner() { return owner; }
    @Override public void setOwner(Player p) { owner = p; }

    public int ownedCount(Board board, Player owner) {
        int cnt = 0;
        for (int i = 0; i < board.size(); i++) {
            Space s = board.getSpace(i);
            if (s instanceof Utility ut && ut.owner == owner) cnt++;
        }
        return cnt;
    }

    @Override
    public String onLand(Game game, Player p) {
        if (owner == null) {
            return "Unowned – price $" + price + " (Choose Buy or Skip)";
        } else if (owner == p) {
            return "Owned";
        } else {
            int n = ownedCount(game.getBoard(), owner);
            int[] lastDice = game.rollDice();
            int rollSum = lastDice[0] + lastDice[1];
            int multiplier = (n >= 2) ? 10 : 4;
            int rent = rollSum * multiplier;
            p.subtractBalance(rent);
            owner.addBalance(rent);
            return "Utility rent $" + rent + " to " + owner.getName();
        }
    }
}
