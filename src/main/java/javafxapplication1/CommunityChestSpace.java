package javafxapplication1;

public class CommunityChestSpace extends Space {
    public CommunityChestSpace(String name) { this.name = name; }
    @Override public SpaceType getType() { return SpaceType.COMMUNITY_CHEST; }
    @Override public String onLand(Game game, Player p) {
        Card card = game.drawChest();
        return card.apply(game, p);
    }
}
