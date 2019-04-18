package torres;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 6, 7 or 8 castles possible.
 */
public class Castle {
	
	/**
	 * Max build height = Fields covered.
	 */
	public ArrayList<Field> floor = new ArrayList<>();
	
	/**
	 * new Castle at this spot.
	 * @param f
	 */
	Castle(Field f) {
		this.floor.add(f);
		f.height = 1;
		f.castle = this;
	}
	
	/**
	 * Can only be triggered by {@link ReToken}.<br>
	 * The Field is empty and height > 0.
	 * @param f the field.
	 * @return the altered field.
	 */
	public Field remove(Field f) {
		f.height--;
		if (f.height == 0) {
			this.floor.remove(f);
			f.castle = null;
		}
		return f;
	}
	
	public ArrayList<Field> getEmptyFloor() {
		var tmp = new ArrayList<Field>(); 
		tmp.addAll(floor);
		
		for (Field f: tmp) {
			if (f.player != null) tmp.remove(f);
		}
		return tmp;
	}
	
	boolean isKingsCastle(Player king) {
		for (Field f: floor) {
			if (f.player != null && f.player.equals(king)) return true;
		}
		return false;
	}
	
	/**
	 * Given a start field, checks floor of this castle for tunnel exits<br>
	 * which are lower or equal the starting height.
	 * @param start a gield next to this castle, player should be present.
	 * @return list of viable fields to tunnel to.
	 */
	List<Field> tunnel(Field start, Manager m, boolean checkHeight) {
		if (start.player.equals(null)) return new ArrayList<Field>();
		return floor.stream()
					.map(f -> {
						for (Field cross : f.checkCross(m.board)) {
							if (f.height>cross.height && cross.player.equals(null)) {
								return cross; // won't list all exits, just 1 TODO:
							}
						}
						return null;
					})
					.distinct()
					.filter(f -> f.height <= start.height) // TODO: hier boolean prüfen
					.collect(Collectors.toList());
	}
}
