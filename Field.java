package torres;

import java.util.ArrayList;
import java.util.List;

public class Field {
	public int height = 0;
	public Player player;	// king, 1..4 players. null = free
	public int x, y;		// redundant absolute pos on board.
	public Castle castle = null;

	Field(Castle c) {
		this.height = 1;
		this.castle = c;
	}

	Field(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * needed for stupid map in Manager
	 * @return
	 */
	Player player() {
		return player;
	}
	
	/**
	 * creates a new Tile.
	 * TODO: Fix this mess.
	 * @param c
	 * @return 1
	 */
	int addTile(Castle c) {
		this.height = 1;
		this.castle = c;
		c.floor.add(this);
		return 1;
	}
	
	/**
	 * checks up, down, left and right for this field(x,y)
	 * @return list of accessible spots. inBounds
	 */
	List<Field> checkCross(Field[][] board) {
		List<Field> lst = new ArrayList<>();
		for (int xDir = -1; xDir < 2; xDir++) {
			for (int yDir = -1; yDir < 2; yDir++) {
				if ((Math.abs(xDir) + Math.abs(yDir)) != 1) {					
					continue;
				}
				try {
					lst.add(board[x + xDir][y + yDir]);					
				} catch (IndexOutOfBoundsException e) {
					continue;
				}
			}
		}	
		return lst;
	}

	/**
	 * @param board
	 * @param offset
	 * @return List of Fields, lower than (this + offset).
	 */
	public List<Field> checkLowerCross(Field[][] board, int offset) {
		List<Field> tmp = new ArrayList<Field>();
		for (Field f: checkCross(board)) {
			if (f.height < (height+offset)) tmp.add(f);
		}
		return tmp;
	}
	
	/**
	 * Returns the surrounding castles as a list
	 * @param board
	 * @return
	 */
	public List<Castle> getCastles(Field[][] board) {
		var castleList = new ArrayList<Castle>();
		for (Field f: checkCross(board)) {
			if (castleList.indexOf(f.castle) < 0) 
				castleList.add(f.castle);
		}
		return castleList;
	}
	
	public String toString() {
		return (this.x+1)+","+(this.y+1);
	}
}
