package torres;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import torres.cards.*;

/*
 * Pro-Version: Reih um setzen, Abstand beachten.
 * 
 * 3 Phasen a 4 Runden
 * Für 2 Spieler: 4x3 Steine
 * 
 * - Nach Wertung einer Phase darf letzter Spieler 
 * 			König auf freies Feld einer Burg setzten. 
 * 
 */
public class Manager {
	public Field[][] board = new Field[8][8];
	public static Player king;
	static List<Player> players = new ArrayList<>();
	public static List<Castle> castles = new ArrayList<>(8);	// 6..8 can only change with card "move tile".
	int token[][] = new int[3][4];							// placeholder for the starting size of stones.
	
	public static Scanner scan = new Scanner(System.in);
	static boolean confirm = false;
	static boolean masterMode = false;
	
	private static final String t0 = "[0] Help 'Number'";
	private static final String t1 = "[1] Place Token  [1 AP]";
	private static final String t2 = "[2] Spawn Knight [2 AP]";
	private static final String t3 = "[3] Move Knight  [1 AP]";
	private static final String t4 = "[4] Draw Card    [1 AP]";
	private static final String t5 = "[5] Play Card    [0 AP]";
	private static final String t6 = "[6] End: AP  ->  Points";
	private List<String> tList = List.of(t0, t1, t2, t3, t4, t5, t6, "");
	
	/**
	 * @param players List of Strings - names
	 */
	public Manager(ArrayList<String> players) {
		 king = new Player(this);
		// tokens setup:
		switch (players.size()) {
		case 2: {
			for (int i = 0; i < 3; i++) {
//				token[i] = new int[4];
				for (int j = 0; j < 4; j++) {
					token[i][j] = 3;
				}
			}
		}
		case 3: {}
		case 4: {}
		default:{
			System.out.println(" No: Name:");
			for (int i = 0; i < players.size(); i++) {
				Manager.players.add(new Player(players.get(i), i, this));
			}
		}
		}
	}
	
	/**
	 * prompt the player to set his name.<br>
	 * If 'exit' set name to playerX
	 * @param player Index in Players List
	 * @return the name string
	 */
	private static String setName(int player) {
		if (!confirm) return "Player"+(player+1);
		System.out.println("Player "+(player+1)+" choose your name:");
		String name = scan.next();
		if (name.equals("exit")) return "player"+(player+1);
		if (!confirm(null)) {
			// the entered name was not confirmed with yes.
			// -> Enter new name or exit.
			return setName(player);
		}
		return name;
	}
	
	/**
	 * TODO: where do we want this?
	 * adds all 10 cards to the list.
	 * @param cards a list of cards.
	 * @return the list with one of each 10 kinds added to it.
	 */
	List<Card> getDeck(List<Card> cards) {
		cards.add(new Ladder("ladder", "Move knight to free, adjacent field 2 tiles above current."));
		cards.add(new Stack("stack", "Place a token below your knight, if token can be placed."));
		cards.add(new Token("token", "You can play an extra token this turn."));
		cards.add(new Tunnel("tunnel", "Exit one tunnel on any level."));
		cards.add(new Diagonal("diagonal", "Move knight diagonal, max +1 level."));
		cards.add(new ReToken());
		cards.add(new ReKnight("reKnight", "Replace your knight for free on rules."));
		cards.add(new Jump("jump", "Move 2 fields, if in front of enemy knight, max +1 level."));
		cards.add(new Ap1("AP1", "You get 1 extra AP this turn."));
		cards.add(new Ap2("AP2", "You get 2 extra AP this turn."));
		return cards;
	}
	
	/**
	 * Asks the player for confirmation if the global flag is set to true.
	 * @param message - null to not print.
	 * @return true for 'yes' or 'y', anything else: false
	 */
	static boolean confirm(String message) {
		if (!confirm) return true;
		if (message != null) System.out.println(message);
		System.out.println("Type 'yes' or 'y' to confirm.");
		String line = scan.next().toLowerCase();
		return (line.equals("yes") || line.equals("y"));
	}
	
	public static void main(String[] args) {
		int playerCount = setup();
		
		ArrayList<String> names = new ArrayList<>();
		for (int i = 0; i < playerCount; i++) names.add(i, setName(i));
		Manager m = new Manager(names);
		m.prepBoard();
		m.firstMove();
		for (int round = 1; round <= 3; round++) {
			m.printField();
			m.evaluate(round);
			players.sort(compPlayers);
			players.get(0).placeKing(castles, king);
		}
		System.out.println("And the winner is: "+players.get(players.size()-1));
		scan.close();
	}
	
	/**
	 * each player places one knight,<br>
	 * last player also places the king.
	 */
	private void firstMove() {
		for (Player p: players) {
			this.printField();
			p.firstKnight(castles);
		}
		this.printField();
		var p = players.get(players.size()-1);
		p.placeKing(castles, king);
	}

	/**
	 * rules, settings and player names can be set.
	 * @return playerCount.
	 */
	static int setup() {
		System.out.println("Type rules, start #PLAYERS or settings");
		String current[] = scan.nextLine().split(" +");
		switch (current[0]) {
		case "rules": {
			rules();
			return setup();
		}
		case "start": {
			if (current.length > 1) {
				var tmp = start(current[1].trim());
				System.out.println("Players: "+ tmp);
				return tmp;
			}
			else {
				usage("start");
				return setup();
			}
		}
		case "settings": {
			settings();
			return setup();
		}
		default: {
			usage(null);
			return setup();
		}
		}
	}
	
	private static void usage(String s) {
		if (s == null) return;
		switch (s) {
		default: 	{
			System.out.println("Usage: rules, start #PLAYERS or settings");
			return;
		}
		case "start":	{
			System.out.println("Usage: start #PLAYERS, must be 2, 3 or 4.");
			return;
		}
		case "settings": {
			System.out.println("Usage: settings - TODO");	// TODO:
			return;
		}
		}
	}

	/**
	 * Settings. reset with each game.
	 * TODO: Master Mode. Sonderregeln.
	 */
	private static void settings() {
		confirm = true;
		masterMode = confirm("Activate Master mode? Currently: "+masterMode);
		confirm = confirm("Confirm everything? Currently: "+confirm);
	}

	/**
	 * Set playerCount. repeats until 2,3 or 4 is entered.
	 * @param line String playerCount
	 * @return playerCount. 2, 3 or 4.
	 */
	private static int start(String line) {
		while (true) {
			try {
				int players = Integer.parseInt(line);
				if (inRange(1, 5, players)) return players;
				throw new NumberFormatException();
			} catch (NumberFormatException e) {
				System.out.println("#Players: Enter 2, 3 or 4.");
				line = scan.nextLine();
			}
		}
	}

	private static void rules() {
		System.out.println("");	// TODO: copy paste backstory.
		System.out.println("There are 3 Phases with 3 or 4 turns each.");
		System.out.println("You get points at the end of each phase");
		System.out.println("Per turn, every player gets to use 5 AP");
		System.out.println("Type help when it's your turn for a full list.");
	}

	void prepBoard() {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8 ; j++) {
				board[i][j] = new Field(i, j);
			}
		}
		// default config: TODO: Meister setup.
		if (!masterMode) {
			castles.add(new Castle(board[0][3]));
			castles.add(new Castle(board[2][2]));
			castles.add(new Castle(board[2][5]));
			castles.add(new Castle(board[3][7]));
			castles.add(new Castle(board[4][0]));
			castles.add(new Castle(board[5][2]));
			castles.add(new Castle(board[5][5]));
			castles.add(new Castle(board[7][4]));
		}
	}
	
	void printField() {
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				Field f = board[i][j];
				System.out.print(f.height+" "); // TODO: something with color.
			}
			System.out.println(tList.get(i));
		}
	}
	
	static Comparator<Player> compPlayers = (Player p1, Player p2) -> p1.score - p2.score;

	/**
	 * Wertung:
	 * Pro Burg: Grundfläche x max Höhe eigener Ritter in der Burg
	 * + 5/10/15 Punkte, wenn in Burg mit König und auf Ebene 1/2/3
	 * (else Punkte über Rest in Runden)
	 * @return list of all players.
	 */
	public List<Player> evaluate(int round) {
		for (Player p: players) {
			p.evaluate(this);
		}
		// add Castle points to all players on round-lvl in correct castle.
		castles.stream().filter(c -> c.isKingsCastle(king))
		.findFirst().get().floor.stream()
		.filter(f-> f.height == round)
		.filter(f -> !(f.player.equals(null) || f.player.equals(king)))
		.map(Field::player).distinct().map(p -> p.score += round*5).close();
		// TODO: sort players. Done in main loop.
		return players;
	}
	
	public static boolean inRange(int low, int high, int arg) {
		return (low <= arg && arg < high);
	}

	/**
	 * @return nested field-List sorted by amount of sourrounding castles.
	 */
	public List<List<Field>> getBoardCastled(boolean isEmpty) {
		List<Field> lst0 = new ArrayList<Field>();
		List<Field> lst1 = new ArrayList<Field>();
		List<Field> lst2 = new ArrayList<Field>();
		var all = List.of(lst0, lst1, lst2);
		
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				var tmp = board[i][j];
				if (isEmpty && tmp.player != null) continue;
				var amount = tmp.getCastles(board).size();
				if (amount > 1) amount = 2;
				var lst = all.get(amount);
				if (lst.indexOf(tmp) < 0) lst.add(tmp);
			}
		}
		return all;
	}
}
