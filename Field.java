package turrets;

public class Field {
	public int height = 0;	// Zahl beschreibt H�he.
	public int player = 0;	// -1 K�nig, 1..4 Spieler. 0 = frei
	public Castle castle = null;

	Field() {
	}
	
	int addTile(Castle c) {
		this.height = 1;
		this.castle = c;
		return 1;
	}
}
