package torres.cards;

import java.util.ArrayList;

import torres.Castle;
import torres.Exit;
import torres.Field;
import torres.Manager;
import torres.Player;

/**
 * Moves a token already on the board to a new location,<br>
 * as long as there are still 6..8 castles and none are merged.<br>
 * Obviously the Field must be without knights or king.
 */
public class ReToken extends Card {

	public ReToken() {
		// "Move a free token, add/remove 1er-Castle if there are 6 to 8."
		super("reToken", 
			"Moves a token already on the board to a new location,\n" + 
			"as long as there are still 6..8 castles and none are merged.\n" + 
			"Obviously the Field must be without knights or king.");
	}

	public boolean play(Manager m, Player player) throws Exit {
		var emptyCastleFields = new ArrayList<Field>();
		for (Castle c: Manager.castles) {
			var tmp = c.getEmptyFloor();
			for (Field f: tmp) {
				if(emptyCastleFields.indexOf(f) < 0)
					emptyCastleFields.add(f);
			}
		}
		var f = player.select(emptyCastleFields);
		f.castle.remove(f);
		
		var listOfListOfFields = m.getBoardCastled(true);
		var options = listOfListOfFields.get(1);
		if (Manager.castles.size() < 8) {
			options.addAll(listOfListOfFields.get(0));
		}
		
		var f2 = player.select(options);

		if (f2.equals(f)) {
			// TODO: place Token on old spot again.
			throw new Exit("Cancelled card by selecting old pos.");
		} else 
			if (listOfListOfFields.get(0).indexOf(f2) >= 0) {
			// TODO: else: add it to new one.
			// TODO: new castle spawned.
		} else {
			// TODO: update castle.
		}
		return true;
	}

}
