package torres;

import java.util.ArrayList;
import java.util.List;

import torres.cards.Card;

public class Player {
	String name = "";
	Manager manager;
	int score = 0;		// used to determin the winner.
	ArrayList<Knight> knights = new ArrayList<>(6);

	int[] tokens = new int[4];	// Updated by Manager each round.
	ArrayList<Card> hand = new ArrayList<>();
	private boolean playedCard;

	/**
	 * Constructor. Sets the players name.
	 * @param name
	 * @param player
	 */
	public Player(String name, int player, Manager m) {
		System.out.println("  "+(player+1)+"  "+name);
		this.name = name;
		this.manager = m;
	}

	/**
	 * ONLY used for king.
	 */
	public Player(Manager m) {
		this.name = "king";
		this.manager = m;
	}

	/**
	 * 
	 * 5 AP pro Zug
	 * 2: Spawn Ritter auf 1/4 freie Felder neben eigenem Ritter, wenn Lvl <= currentLvl
	 * 1: Move  Ritter auf freies Feld mit Lvl <= currentLvl +1
	 * 	  Tunnel, wenn currentLvl < 1/4 Nachbarn 
	 * 			  -> freies Feld an/auf dieser Burg mit newLvl < Lvl
	 * 1: Ettage bauen (max 3 pro Runde - nur wenn Grundfläche >= newLvl)
	 * 1: Karte Ziehen (max 2 ziehen pro Runde; kann nicht sofort eingesetzt werden)
	 * 0: Karte spielen (max 1 pro Runde)
	 * Rest: 1 SiegPunkt pro left AP
	 */
	void takeTurn() {
		var AP = 5;
		playedCard = false;
		List<Card> tmpHand = new ArrayList<Card>();
		System.out.println(this.name+": "
				+ (6-knights.size())+" knights, "
				+ hand.size() + " cards.");

		while (AP > 0) {

			switch (Manager.scan.next().toLowerCase().trim()) {
			case "end": {
				if (!Manager.confirm("No cards played. End turn?")) takeTurn();
				this.score += AP;
				AP = 0;
				/* TODO: if possible stack unused Tokens on remaining turns 
				 [max 3 per turn] (bei 2 Spielern nie der Fall) */
				break;
			}
			case "help": help();
			case "board": manager.printField();
			case "place knight": {
				try {
					AP = placeKnight(AP);
				} catch (Exit e) {
					
				}
			}
			case "move knight": ;
			case "draw card": {
				try {
					tmpHand = drawCard(AP, tmpHand);
					AP -= 1;
				} catch (Exit e) {
					
				}
			}
			case "play card": {
				try {
					playCard(select(hand));
				} catch (Exit e) {
					
				}
			}
			default: help();
			}
		}
		if (!playedCard && hand.size() > 0) {
			System.out.println("You have not played a card this turn, but there are still cards in your hand.");
			
		}
		hand.addAll(tmpHand);
	}

	/**
	 * Takes a card and execute it.
	 * @param cardName
	 * @return true or throws Exit
	 */
	boolean playCard(Card card) throws Exit{
		if (playedCard) throw new Exit("Already played a card this turn.");
		if (card.play(manager, this)) {
			this.playedCard = hand.remove(card);
			return true;
		}
		throw new Exit("Card cancelled");
	}

	
	private List<Card> drawCard(int AP, List<Card> tmpHand) throws Exit {
		if (AP < 1) throw new Exit("Expected 1+ AP left, but was "+AP);
		if (tmpHand.size() > 2) throw new Exit("You have already drawn 2 cards this turn.");

		return tmpHand;
	}

	private int placeKnight(int AP) throws Exit {
		if (AP < 2) {
			System.out.println("Expected 2+ AP left, but was "+AP);
		}
		var fields = new ArrayList<Field>();
		for (Knight k: knights) {
			for (Field f: (manager.board[k.xPos][k.yPos].checkLowerCross(manager.board, 1))) {
				if (fields.indexOf(f) < 0 && f.player == null) fields.add(f);
			}
		}
		try {
			if (fields == null || fields.size() == 0)
				throw new Exit("Found no viable fields.");
			Field f = select(fields);
			if (!checkKnight(f)) {
				throw new Exit("Expected empty field next to allied knight.");
			}
			knights.add(new Knight(this, f));
		} catch (Exit e) {
			takeTurn();
		}
		return AP - 2;
	}
	
	/**
	 * Check if field is free and lower than a knight of same player on cross.
	 * @param f Field on the board
	 * @return wheter this player can place a new Knight on this field or not.
	 */
	private boolean checkKnight(Field f) {
		if (f.player != null) return false;
		for (Field n: f.checkCross(manager.board)) {
			if (n.player.equals(this) && n.height >= f.height) return true;
		}
		return false;
	}

	/**
	 * Displays commands. who needs a map?
	 */
	private void help() {
		System.out.println(" end   \t* AP your turn, score += AP");
		System.out.println(" card  \t1 AP Play one of your cards.");
		System.out.println(" knight\t2 AP Place a knight.");
		System.out.println(" move  \t1 AP knight 1 field or tunnel.");
		System.out.println(" place \t1 AP Token on/next to castle.");
		System.out.println(" board \t0 AP Display the current board");
		System.out.println(" hand  \t0 AP Lists all cards on your hand.");

	}


	/**
	 * Place the king on any empty token.
	 */
	Field placeKing(List<Castle> castles, Player king) {
		System.out.println("Player "+name+" gets to place the king:");
		System.out.println("Select a castle to place the king onto.");
		System.out.println("You get "+"5/10/15"+" points for each round you\n"
				+ "are on 'round' level in the kings castle.");

		var fields = new ArrayList<Field>();
		for (Castle c: castles) {
			fields.addAll(c.getEmptyFloor());
		}

		try {
			Field f = this.select(fields);
			f.player = king;
			return f;
		} catch (Exit e) {
			return placeKing(castles, king);
		}
	}

	/** Prompt player to place his first <br> 
	 * knight on an empty castle.
	 * @return the selected field
	 */
	public Field firstKnight(List<Castle> castles) {
		System.out.println("Player "+name+" choose starting castle:");

		// 1..emptyCastles
		List<Field> fields = new ArrayList<>();
		for (Castle c: castles) {
			var tmp = c.floor.get(0);
			if (tmp.player == null) fields.add(tmp);
		}
		// You think you could cancel placing your first knight?
		while (true) {
			try {
				Field field = select(fields);
				field.player = this;
				knights.add(new Knight(this, field.x, field.y));
				return field;
			} catch (Exit e) {
				continue;
			}
		}
	}

	/**
	 * Wertung:
	 * Pro Burg: Grundfläche x max Höhe eigener Ritter in der Burg
	 * + 5/10/15 Punkte, wenn in Burg mit König und auf Ebene 1/2/3
	 * (else Punkte über Rest in Runden)
	 */
	public int evaluate(Manager m) {
		System.out.println("Pos h,s   Points");
		int sum = 0;
		for (Knight k: knights) {
			Field f = m.board[k.xPos][k.yPos];
			int tmp = f.height * f.castle.floor.size();
			System.out.println(f.toString()+"\t "+f.height+" "+f.castle.floor+"\t "+tmp);
			sum += tmp;
		}
		// TODO: Königsburg
		System.out.println(this.name+" "+ sum +" points.");
		return this.score += sum;
	}

	/**
	 * Let the player select a field from a given List.
	 * @param fields
	 * @return 
	 * @return field at userInput
	 */
	public <T> T select(List<T> fields) throws Exit{
		
		if (fields == null || fields.size() == 0) 
			throw new Exit("Argument 'fields' was null or empty.");
		
		var index = 0;
		System.out.println("Select field from 1 to "+ fields.size());
		while (index < 1 || index > fields.size()) {
			while (!Manager.scan.hasNextInt()) {
				Manager.scan.next();
			}
			index = Manager.scan.nextInt();			
		}
		if (!Manager.confirm("Select field "+index+"?")) {
			throw new Exit("You cancelled field selection.");
		}
		return fields.get(index-1);
	}
}
