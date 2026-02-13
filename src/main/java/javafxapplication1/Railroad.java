package javafxapplication1;

public class Railroad extends Space implements Ownable {
    private final int price;
    private Player owner;

    public Railroad(String name, int price) { this.name = name; this.price = price; }

    @Override public SpaceType getType() { return SpaceType.RAILROAD; }
    @Override public int getPrice() { return price; }
    @Override public Player getOwner() { return owner; }
    @Override public void setOwner(Player p) { owner = p; }

    public int ownedCount(Board board, Player owner) {
        int cnt = 0;
        for (int i = 0; i < board.size(); i++) {
            Space s = board.getSpace(i);
            if (s instanceof Railroad rr && rr.owner == owner) cnt++;
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
            int rent = switch (n) {
                case 1 -> 25;
                case 2 -> 50;
                case 3 -> 100;
                case 4 -> 200;
                default -> 25;
            };
            p.subtractBalance(rent);
            owner.addBalance(rent);
            return "Paid railroad rent $" + rent + " to " + owner.getName();
        }
    }
}
