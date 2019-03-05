package torres.cards;

import torres.Exit;
import torres.Manager;
import torres.Player;

/**
 * Can be used by a player.
 * In Master Mode, each player has each card exactly once.
 */
public abstract class Card {
	String name;
	String description;
	int amount = 1;			// may exist twice.
	
	public Card(String name, String desc) {
		this.name = name;
		this.description = desc;
	}
	
	public abstract boolean play(Manager manager, Player player) throws Exit;
	
}
