package javafxapplication1;

public class GetOutOfJailFreeCard extends Card {
    public GetOutOfJailFreeCard(String text) { super(text); }
    @Override
    public String apply(Game game, Player p) {
        p.addGetOutOfJailFree();
        return text;
    }
}
