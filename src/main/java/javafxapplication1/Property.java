package javafxapplication1;

public class Property extends Space implements Ownable {
    private final int price;
    private final int baseRent;
    private final String colorGroup;
    private Player owner;
    private int houses = 0;
    private boolean hotel = false;

    public Property(String name, int price, int baseRent, String colorGroup) {
        this.name = name; this.price = price; this.baseRent = baseRent; this.colorGroup = colorGroup;
    }

    @Override public SpaceType getType() { return SpaceType.PROPERTY; }
    @Override public int getPrice() { return price; }
    public int getBaseRent() { return baseRent; }
    public String getColorGroup() { return colorGroup; }
    @Override public Player getOwner() { return owner; }
    @Override public void setOwner(Player p) { owner = p; }
    public int getHouses() { return houses; }
    public boolean hasHotel() { return hotel; }
    public void setHouses(int h) { houses = Math.max(0, Math.min(4, h)); }
    public void setHotel(boolean h) { hotel = h; }
    public int getHouseCost() { return houseCostFor(colorGroup); }
    public int getHotelCost() { return getHouseCost() * 5; }

    public static int houseCostFor(String colorGroup) {
        return switch (colorGroup) {
            case "Brown", "Light Blue" -> 50;
            case "Pink", "Orange" -> 100;
            case "Red", "Yellow" -> 150;
            case "Green", "Dark Blue" -> 200;
            default -> 100;
        };
    }

    public int getCurrentRent() { return baseRent + (houses * 10) + (hotel ? 50 : 0); }

    public boolean buildHouse(Player p) {
        if (owner != p) return false;
        if (hotel) return false;
        if (houses >= 4) return false;
        int cost = getHouseCost();
        if (p.getBalance() < cost) return false;
        p.subtractBalance(cost);
        houses++;
        return true;
    }

    public boolean buildHotel(Player p) {
        if (owner != p) return false;
        if (hotel) return false;
        if (houses < 4) return false;
        int cost = getHotelCost();
        if (p.getBalance() < cost) return false;
        p.subtractBalance(cost);
        hotel = true;
        return true;
    }

    @Override
    public String onLand(Game game, Player p) {
        if (owner == null) {
            return "Unowned – price $" + price + " (Choose Buy or Skip)";
        } else if (owner == p) {
            return "Owned";
        } else {
            int rent = getCurrentRent();
            p.subtractBalance(rent);
            owner.addBalance(rent);
            return "Paid rent $" + rent + " to " + owner.getName();
        }
    }
}
