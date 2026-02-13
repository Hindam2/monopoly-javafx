package javafxapplication1;
import javafx.scene.paint.Color;
import java.io.Serializable;
import java.util.*;

public class SaveData implements Serializable {
    static final long serialVersionUID = 1L;

    List<PlayerData> players = new ArrayList<>();
    int currentIndex;

    Map<String, PropertyData> properties = new HashMap<>();
    Map<String, OwnableData> ownables  = new HashMap<>();

    List<CardData> chanceDeck = new ArrayList<>();
    List<CardData> chestDeck  = new ArrayList<>();

    String textLog;
    int totalGoPasses;

    static SaveData fromGame(Game game, String textLog) {
        SaveData sd = new SaveData();
        sd.currentIndex = game.currentIndex;
        sd.textLog = textLog;
        sd.totalGoPasses = game.getTotalGoPasses();

        for (Player p : game.getPlayers()) {
            PlayerData pd = new PlayerData();
            pd.name = p.getName();
            pd.colorHex = colorToHex(p.getColor());
            pd.balance = p.getBalance();
            pd.position = p.getPosition();
            pd.inJail = p.isInJail();
            pd.jailTurns = p.getJailTurns();
            pd.goojfCount = p.getGoojfCount();
            pd.goPassCounterAtJail = p.getGoPassCounterAtJail();
            sd.players.add(pd);
        }

        int N = game.getBoard().size();
        for (int i = 0; i < N; i++) {
            Space s = game.getBoard().getSpace(i);
            if (s instanceof Property prop) {
                PropertyData pd = new PropertyData();
                pd.name = prop.getName();
                pd.ownerName = prop.getOwner() == null ? null : prop.getOwner().getName();
                pd.houses = prop.getHouses();
                pd.hotel = prop.hasHotel();
                sd.properties.put(pd.name, pd);
            } else if (s instanceof Railroad rr) {
                OwnableData od = new OwnableData();
                od.name = rr.getName();
                od.ownerName = rr.getOwner() == null ? null : rr.getOwner().getName();
                sd.ownables.put(od.name, od);
            } else if (s instanceof Utility ut) {
                OwnableData od = new OwnableData();
                od.name = ut.getName();
                od.ownerName = ut.getOwner() == null ? null : ut.getOwner().getName();
                sd.ownables.put(od.name, od);
            }
        }

        sd.chanceDeck.clear();
        for (Card c : new ArrayList<>(game.chanceDeck)) sd.chanceDeck.add(CardData.fromCard(c));
        sd.chestDeck.clear();
        for (Card c : new ArrayList<>(game.chestDeck))  sd.chestDeck.add(CardData.fromCard(c));

        return sd;
    }

    static class PlayerData implements Serializable {
        String name;
        String colorHex;
        int balance;
        int position;
        boolean inJail;
        int jailTurns;
        int goojfCount;
        int goPassCounterAtJail;
    }

    static class PropertyData implements Serializable {
        String name;
        String ownerName;
        int houses;
        boolean hotel;
    }

    static class OwnableData implements Serializable {
        String name;
        String ownerName;
    }
    
    static class CardData implements Serializable {
        String type;
        int a, b;
        static CardData fromCard(Card c) {
            CardData cd = new CardData();
            cd.type = c.getClass().getSimpleName();
            if (c instanceof MoneyCard mc) {
                cd.a = mc.getAmount();
            } else if (c instanceof AdvanceToCard atc) {
                cd.a = atc.getTargetIndex();
                cd.b = atc.isCollectGOOnPass() ? 1 : 0;
            } else if (c instanceof MoveBackCard mbc) {
                cd.a = mbc.getSteps();
            }
            return cd;
        }
        Card toCard() {
            return switch (type) {
                case "MoneyCard" -> new MoneyCard("Money", a);
                case "AdvanceToCard" -> new AdvanceToCard("Advance", a, b == 1);
                case "GoToJailCard" -> new GoToJailCard("Go to Jail");
                case "GetOutOfJailFreeCard" -> new GetOutOfJailFreeCard("Get Out of Jail Free");
                case "NearestRailroadCard" -> new NearestRailroadCard("Nearest Railroad");
                case "MoveBackCard" -> new MoveBackCard("Move Back", a);
                default -> new MoneyCard("Money", 0);
            };
        }
    }

    static String colorToHex(Color c) {
        int r = (int) Math.round(c.getRed() * 255.0);
        int g = (int) Math.round(c.getGreen() * 255.0);
        int b = (int) Math.round(c.getBlue() * 255.0);
        return String.format("#%02X%02X%02X", r, g, b);
    }

    static Color colorFromHex(String hex) { return Color.web(hex); }
}
