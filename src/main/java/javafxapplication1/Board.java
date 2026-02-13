package javafxapplication1;

import java.util.*;

public class Board {
    private final List<Space> spaces = new ArrayList<>();

    public Space getSpace(int i) { return spaces.get(i); }
    public int size() { return spaces.size(); }

    public List<String> getOwnedNames(Player p) {
        List<String> names = new ArrayList<>();
        for (Space s : spaces) {
            if (s instanceof Ownable o && o.getOwner() == p) {
                names.add(s.getName());
            }
        }
        return names;
    }

    public int indexOf(Space s) {
        for (int i = 0; i < spaces.size(); i++) if (spaces.get(i) == s) return i;
        return -1;
    }

    public Map<String, List<Property>> propertiesByColorOwnedBy(Player p) {
        Map<String, List<Property>> map = new HashMap<>();
        for (Space s : spaces) {
            if (s instanceof Property prop && prop.getOwner() == p) {
                map.computeIfAbsent(prop.getColorGroup(), k -> new ArrayList<>()).add(prop);
            }
        }
        return map;
    }

    public boolean playerOwnsFullSet(Player p, String colorGroup) {
        int totalInGroup = 0;
        int ownedByPlayer = 0;
        for (Space s : spaces) {
            if (s instanceof Property prop && prop.getColorGroup().equals(colorGroup)) {
                totalInGroup++;
                if (prop.getOwner() == p) ownedByPlayer++;
            }
        }
        return totalInGroup > 0 && totalInGroup == ownedByPlayer;
    }

    public void initStandard() {
        spaces.clear();
        spaces.add(new StartSpace("GO"));
        spaces.add(new Property("Mediterranean Avenue", 60, 10, "Brown"));
        spaces.add(new CommunityChestSpace("Community Chest"));
        spaces.add(new Property("Baltic Avenue", 60, 10, "Brown"));
        spaces.add(new TaxSpace("Income Tax", 200));
        spaces.add(new Railroad("Reading Railroad", 200));
        spaces.add(new Property("Oriental Avenue", 100, 20, "Light Blue"));
        spaces.add(new ChanceSpace("Chance"));
        spaces.add(new Property("Vermont Avenue", 100, 20, "Light Blue"));
        spaces.add(new Property("Connecticut Avenue", 120, 24, "Light Blue"));
        spaces.add(new JailSpace("Jail / Just Visiting"));

        spaces.add(new Property("St. Charles Place", 140, 28, "Pink"));
        spaces.add(new Utility("Electric Company", 150));
        spaces.add(new Property("States Avenue", 140, 28, "Pink"));
        spaces.add(new Property("Virginia Avenue", 160, 32, "Pink"));
        spaces.add(new Railroad("Pennsylvania Railroad", 200));
        spaces.add(new Property("St. James Place", 180, 36, "Orange"));
        spaces.add(new CommunityChestSpace("Community Chest"));
        spaces.add(new Property("Tennessee Avenue", 180, 36, "Orange"));
        spaces.add(new Property("New York Avenue", 200, 40, "Orange"));
        spaces.add(new FreeParkingSpace("Free Parking"));

        spaces.add(new Property("Kentucky Avenue", 220, 44, "Red"));
        spaces.add(new ChanceSpace("Chance"));
        spaces.add(new Property("Indiana Avenue", 220, 44, "Red"));
        spaces.add(new Property("Illinois Avenue", 240, 48, "Red"));
        spaces.add(new Railroad("B&O Railroad", 200));
        spaces.add(new Property("Atlantic Avenue", 260, 52, "Yellow"));
        spaces.add(new Property("Ventnor Avenue", 260, 52, "Yellow"));
        spaces.add(new Utility("Water Works", 150));
        spaces.add(new Property("Marvin Gardens", 280, 56, "Yellow"));
        spaces.add(new GoToJailSpace("Go To Jail"));

        spaces.add(new Property("Pacific Avenue", 300, 60, "Green"));
        spaces.add(new Property("North Carolina Avenue", 300, 60, "Green"));
        spaces.add(new CommunityChestSpace("Community Chest"));
        spaces.add(new Property("Pennsylvania Avenue", 320, 64, "Green"));
        spaces.add(new Railroad("Short Line", 200));
        spaces.add(new ChanceSpace("Chance"));
        spaces.add(new Property("Park Place", 350, 70, "Dark Blue"));
        spaces.add(new TaxSpace("Luxury Tax", 100));
        spaces.add(new Property("Boardwalk", 400, 80, "Dark Blue"));
    }
}
