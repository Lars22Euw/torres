package turrets;

public class Knight {
	int yPos;
	int xPos;
	Player player;	// 1..4 oder 0 für König?
//	int lvl;	// möglich aber redundant.
	
	
	/**
	 * Can only be used at the start of the game.<br>
	 * Since new knights are placed next to old ones.
	 */
	Knight(Player p, int x, int y) {
		this.xPos = x;
		this.yPos = y;
		this.player = p;
		System.out.println("Placed Knight at "+x+", "+y);
	}
	
	/**
	 * TODO: Gültigkeit wird hier nicht geprüft.
	 * Eins der 4 Felder um Knight p.
	 * |x+y| = 1;
	 * @param x relativ offset in x
	 * @param y relativ offset in y
	 */
	Knight(Knight p, int x, int y) {
		Knight k = new Knight(p.player, p.xPos+x, p.yPos+y);
		p.player.usedKnights++;
		p.player.knights.add(k);
	}
}
