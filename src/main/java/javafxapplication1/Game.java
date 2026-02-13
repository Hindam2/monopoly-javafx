package javafxapplication1;

import java.util.*;

public class Game {
    private final List<Player> players;
    private final Board board;
    private final Dice dice = new Dice();
    int currentIndex = 0;

    final Deque<Card> chanceDeck = new ArrayDeque<>();
    final Deque<Card> chestDeck  = new ArrayDeque<>();

    // global counter for GO passes
    private int totalGoPasses = 0;

    public Game(List<Player> players) {
        this.players = players;
        this.board = new Board();
        board.initStandard();
        buildChanceDeck();
        buildChestDeck();
    }

    public Player getCurrentPlayer() { return players.get(currentIndex); }
    public List<Player> getPlayers() { return players; }
    public Space getSpace(int i) { return board.getSpace(i); }
    public Board getBoard() { return board; }

    public void advanceTurn() { currentIndex = (currentIndex + 1) % players.size(); }
    public int[] rollDice() { return new int[]{dice.roll(), dice.roll()}; }

    public void setCurrentIndex(int idx) { if (idx >= 0 && idx < players.size()) currentIndex = idx; }

    public int getTotalGoPasses() { return totalGoPasses; }
    public void setTotalGoPasses(int v) { totalGoPasses = Math.max(0, v); }
    public void markPassGo() { totalGoPasses++; }

    public TurnPath computePath(Player p, int steps) {
        List<Integer> path = new ArrayList<>();
        int pos = p.getPosition();
        boolean passedGo = false;
        int N = board.size();
        for (int i = 0; i < steps; i++) {
            pos = (pos + 1) % N;
            path.add(pos);
            if (pos == 0) passedGo = true;
        }
        if (passedGo) { p.addBalance(200); markPassGo(); }
        return new TurnPath(path);
    }

    private void buildChanceDeck() {
        List<Card> cards = new ArrayList<>(Arrays.asList(
                new AdvanceToCard("Advance to GO", 0, true),
                new GoToJailCard("Go to Jail"),
                new MoneyCard("Bank pays you dividend of $50", 50),
                new NearestRailroadCard("Advance to nearest Railroad"),
                new GetOutOfJailFreeCard("Get Out of Jail Free (Chance)"),
                new MoneyCard("Pay poor tax of $15", -15),
                new MoveBackCard("Go back 3 spaces", 3)
        ));
        Collections.shuffle(cards);
        chanceDeck.clear();
        chanceDeck.addAll(cards);
    }

    private void buildChestDeck() {
        List<Card> cards = new ArrayList<>(Arrays.asList(
                new AdvanceToCard("Advance to GO (Chest)", 0, true),
                new MoneyCard("Doctor's fees – Pay $50", -50),
                new MoneyCard("Income tax refund – Collect $20", 20),
                new GetOutOfJailFreeCard("Get Out of Jail Free (Chest)"),
                new GoToJailCard("Go to Jail (Chest)"),
                new MoneyCard("Receive $200 from sale of stock", 200),
                new MoneyCard("Life insurance matures – Collect $100", 100)
        ));
        Collections.shuffle(cards);
        chestDeck.clear();
        chestDeck.addAll(cards);
    }

    public Card drawChance() {
        Card c = chanceDeck.pollFirst();
        if (c == null) { buildChanceDeck(); c = chanceDeck.pollFirst(); }
        chanceDeck.addLast(c);
        return c;
    }

    public Card drawChest() {
        Card c = chestDeck.pollFirst();
        if (c == null) { buildChestDeck(); c = chestDeck.pollFirst(); }
        chestDeck.addLast(c);
        return c;
    }

    public void applyOwnershipFromSave(SaveData data) {
        Map<String, Player> byName = new HashMap<>();
        for (Player p : players) byName.put(p.getName(), p);
        int N = board.size();
        for (int i = 0; i < N; i++) {
            Space s = board.getSpace(i);
            if (s instanceof Property prop) {
                SaveData.PropertyData pd = data.properties.get(prop.getName());
                if (pd != null) {
                    prop.setOwner(pd.ownerName == null ? null : byName.get(pd.ownerName));
                    prop.setHouses(pd.houses);
                    prop.setHotel(pd.hotel);
                }
            } else if (s instanceof Railroad rr) {
                SaveData.OwnableData od = data.ownables.get(rr.getName());
                if (od != null) rr.setOwner(od.ownerName == null ? null : byName.get(od.ownerName));
            } else if (s instanceof Utility ut) {
                SaveData.OwnableData od = data.ownables.get(ut.getName());
                if (od != null) ut.setOwner(od.ownerName == null ? null : byName.get(od.ownerName));
            }
        }
    }

    public void applyDecksFromSave(SaveData data) {
        chanceDeck.clear();
        for (SaveData.CardData cd : data.chanceDeck) chanceDeck.addLast(cd.toCard());
        chestDeck.clear();
        for (SaveData.CardData cd : data.chestDeck) chestDeck.addLast(cd.toCard());
    }

    public TurnPath computePathBackward(Player p, int steps) {
        List<Integer> path = new ArrayList<>();
        int pos = p.getPosition();
        int N = board.size();
        for (int i = 0; i < steps; i++) { pos = (pos - 1 + N) % N; path.add(pos); }
        return new TurnPath(path);
    }
}
