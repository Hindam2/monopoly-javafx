package javafxapplication1;

public class MoneyCard extends Card {
    private final int amount;
    public MoneyCard(String text, int amount) { super(text); this.amount = amount; }
    public int getAmount() { return amount; }
    @Override
    public String apply(Game game, Player p) {
        if (amount >= 0) p.addBalance(amount); else p.subtractBalance(-amount);
        return text + " (" + (amount >= 0 ? "+" : "-") + "$" + Math.abs(amount) + ")";
    }
}
