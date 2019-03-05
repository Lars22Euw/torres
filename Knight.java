package torres;

public class Knight {
	int yPos;
	int xPos;
	Player player;
	
	Knight(Player p, int x, int y) {
		this.xPos = x;
		this.yPos = y;
		this.player = p;
		System.out.println("Placed Knight at "+x+", "+y);
	}
	
	/**
	 * @param dx relativ offset in x
	 * @param dy relativ offset in y
	 * TODO: check if useful at all.
	 */
	Knight(Knight k, int dx, int dy) {
		this.xPos = k.xPos+dx;
		this.yPos = k.yPos+dy;
		this.player = k.player;
		System.out.println("Placed Knight at "+(k.xPos+dx)+", "+(k.yPos+dy));
	}
	
	/**
	 * @param p
	 * @param f
	 */
	Knight(Player p, Field f) {
		this.xPos = f.x;
		this.yPos = f.y;
		this.player = p;
		f.player = p;
	}
}
