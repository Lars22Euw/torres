package turrets;

import java.util.ArrayList;
import java.util.Scanner;

/*
 * 8x8 Feld - 8 Startfelder fest	 0xxx0
 * Pro-Version: Reih um setzen,		0xx0xx0
 * 				Abstand beachten	 0xxx0
 * 				x blockiert.		  0x0
 * 				0 erlaubt.			   0
 * 2..4 Spieler + 1 König
 * Je 6 Ritter und ein Wertungstein
 * Nur 1 Entity pro Feld
 * 
 * 3 Phasen a 4 Runden
 * Spieler:
 * für 2: 4x3 Steine
 * - Nach Wertung einer Phase darf letzter Spieler König auf freies Feld einer Burg setzten. 
 * 
 * 5 Mana pro Zug (aktionspunkte AP)
 * 2: Spawn Ritter auf 1/4 freie Felder neben eigenem Ritter, wenn Lvl <= currentLvl
 * 1: Move Ritter auf freies Feld mit Lvl <= currentLvl +1
 * 	  tunnel, wenn currentLvl < 1/4 Nachbar -> freies Feld an/auf dieser Burg mit newLvl < Lvl
 * 1: Ettage bauen (max 3 pro Runde - nur wenn Grundfläche >= newLvl)
 * 1: Karte Ziehen (max 2 ziehen pro Runde; kann nicht sofort eingesetzt werden)
 * 0: Karte spielen (max 1 pro Runde)
 * Rest: 1 Punkt pro Rest Mana
 * 
 * Wertung:
 * Pro Burg: Grundfläche x max Höhe eigener Ritter in der Burg
 * + 5/10/15 Punkte, wenn in Burg mit König und auf Ebene 1/2/3
 * (else Punkte über Rest in Runden)
 * 
 * 
 * Karten:
 * +1/2 Mana -> play more cards?
 * move Ettage (as long as there are 6 castles left)
 */
public class Manager {
	public static Field[][] board;
	ArrayList<Castle> castles = new ArrayList<>(8);	//6..8 can only change with card "move tile".
	int turn = 0;						// 0..3
	int round = 0;						// 0..2
	int stones[][] = new int[3][4];		// placeholder for the starting size of stones.
	static ArrayList<Player> players = new ArrayList<>();
	
	public Manager(int players, ArrayList<String> names) {
		// stones setup:
		switch (players) {
		case 2: {
			for (int i = 0; i < 3; i++) {
				stones[i] = new int[4];
				for (int j = 0; j < 4; j++) {
					stones[i][0] = 3;
				}
			}
		}
		case 3: {}
		case 4: {}
		default:{
			for (int i = 0; i < players; i++) {
				Manager.players.add(new Player(names.get(i), i));
			}
		}
		}
	}

	/**
	 * Construct a player from one line.<br>
	 * May override existing players.
	 * @return the player
	 */
	public static Player setPlayer(Scanner scan) throws Exit{
		System.out.println("Enter: playerNUMBER ; NAME ; (COLOR)");
		String line = scan.next();
		String[] tmp = line.split(";");
		if (tmp.length < 2) {
			throw new Exit("Expected: NUMBER ; NAME ; (COLOR)");
		}
		int num = Integer.parseInt(tmp[0]);
		if (num < players.size()) {
			if (!confirm("This player already exists. Override?", scan)) {
				System.out.println("Canceled override.");
				return null;
			}
			players.get(num).name = tmp[1];
			if (!tmp[2].equals("")) {
				players.get(num).color = tmp[2];
			}
			return players.get(num);
		}
		Player p = new Player(tmp[1], num);
		if (confirm("You are about to add a new Player. Continue?", scan)) {
			players.add(p);
		}
		return p;
	}
	
	public static String setName(Scanner scan, int player) throws Exit {
		System.out.println("Player "+(player+1)+" choose name:");
		String name = "player"+(player+1);
		name = scan.next();
		if (name.equals("exit")) {
			throw new Exit("exit name selection");
		}
		boolean conf = false;
		conf = confirm(null, scan); 
		if (!conf) {
			return setName(scan, player);
		} else return name;
	}
	
	/**
	 * 
	 * @param message null if no print.
	 * @param scan Scanner
	 * @return
	 */
	public static boolean confirm(String message, Scanner scan) {
		if (message != null) System.out.println(message);
		System.out.println("'yes', 'exit' or *");
		String line = scan.next().toLowerCase();
		if (line.equals("yes") || line.equals("y")) {
			return true;
		} else {
			return false;
		}
	}
	
	
	public static void main(String[] args) {
		board = new Field[8][8];
		
		Scanner scan = new Scanner(System.in);
		int playerCount = 0;
		System.out.println("How many players? 2, 3 or 4.");
		while (playerCount < 2 || playerCount > 4) {
//			scan.nextLine();
			if (scan.hasNextInt()) {
				playerCount = scan.nextInt();
				if (playerCount < 2 || playerCount > 4) {
					System.out.println("Input out of bounds!");
				}
			} else {
				System.out.println("Input must be from 2 to 4.");
				scan.nextLine();
			}
		}
		System.out.println("Players: "+playerCount);
		ArrayList<String> names = new ArrayList<>();
		for (int i = 0; i < playerCount; i++) {
			try {
				names.add(i, setName(scan, i));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Manager m = new Manager(playerCount, names);
		
		try {
			setPlayer(scan);
		} catch (Exit e) {
			System.out.println("Stuff");
		}
		scan.close();
		
		m.prepBoard(board);
		// der jüngste Spieler beginnt.
		m.printField(board);
	}
	
	void prepBoard(Field[][] board) {
		// castles:
		for (int j = 0; j < 8; j++) {
			castles.add(new Castle());
		}
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8 ; j++) {
				board[i][j] = new Field();
			}
		}
		// default config: TODO: Meister setup.
		int i = 0;
		i += board[0][3].addTile(castles.get(i));
		i += board[2][2].addTile(castles.get(i));
		i += board[2][5].addTile(castles.get(i));
		i += board[3][7].addTile(castles.get(i));
		i += board[4][0].addTile(castles.get(i));
		i += board[5][2].addTile(castles.get(i));
		i += board[5][5].addTile(castles.get(i));
		i += board[7][4].addTile(castles.get(i));
	}
	
	void printField(Field[][] board) {
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				Field f = board[i][j];
				System.out.print(f.height+" "); // TODO: somthing with color.
			}
			System.out.println("");
		}
	}
	
	public boolean placeKnight(Player p, Scanner scan, boolean card) {
		// TODO: extract the 6 check.
		if (p.usedKnights < 6) {
			if (p.availableAP < 2 || !card) {
				System.out.println("sry you cannot afford to place a knight.");
				// TODO: card allows for free placement. (work around with extra AP?)
				return false;
			}
			System.out.println("Select a Knight: 1.."+(p.usedKnights+1));
			int knum = scan.nextInt();
			Knight k = p.knights.get(knum);
			int[] slots = new int[4];
			if (k.xPos > 0 && board[k.xPos-1][k.yPos].player == 0) {
				System.out.println("x-1 is free");
				slots[0]++;
			}
			if (k.xPos < 8 && board[k.xPos+1][k.yPos].player == 0) {
				System.out.println("x+1 is free");
				slots[1]++;
			}
			if (k.yPos > 0 && board[k.xPos][k.yPos-1].player == 0) {
				System.out.println("y-1 is free");
				slots[2]++;
			}
			if (k.yPos < 8 && board[k.xPos][k.yPos+1].player == 0) {
				System.out.println("y+1 is free");
				slots[3]++;
			}
			
			if (slots[0]+slots[1]+slots[2]+slots[3] == 0) {
				System.out.println("There are no empty slots around this knight.");
				// TODO: return or ask again?
			} else {
				System.out.println("Enter position: x+ x- y+ y-");
				String line = scan.next().toLowerCase().trim();
				boolean x = false;
				boolean y = false;
				Knight k2 = null;
				if ((slots[0] + slots[1] > 0) && line.startsWith("x")) {
					x = true;
				}
				else if ((slots[2] + slots[3] > 0) && line.startsWith("y")) {
					y = true;
				} else {
					System.out.println("Wrong input. Start x or y expected.");
				}
				
				if ((slots[0] + slots[2] > 0) && line.endsWith("+")) {
					if (x) k2 = new Knight(k,1,0);
					if (y) k2 = new Knight(k,0,1);
				}
				else if ((slots[1] + slots[3] > 0) && line.endsWith("-")) {
					if (x) k2 = new Knight(k,-1,0);
					if (y) k2 = new Knight(k,0,-1);
				} else {
					System.out.println("Wrong input. Ending + or - expected.");
					return false;
				}
				board[k2.xPos][k2.yPos].player = Manager.players.indexOf(k2.player);
				return true;
			}
		} else {
			System.out.println("You already used all your knights.");
			return false;
		}
		return true;
	}
	
	/**
	 * TODO: prüfen, ob Spieler sich platzieren leisten kann.
	 * @param p
	 * @return
	 */
	public boolean placePiece(Player p, int x, int y, boolean card) {
		Field f = board[x][y];
		if (f.player != 0) {
			if (card && f.player == players.indexOf(p)) {
				// stack up field with your player.
				// TODO: prüfe ob Größe der Burg hier zu prüfen ist.
				f.height++;
			}
			System.out.println("The field is blocked by a player (or the king)");
			return false;
		}
		if (f.height > 0) {
			Castle c = f.castle;
			if (f.height < c.floor) {
				f.height++;
				System.out.println("Extended field"+x+", "+y+" to "+f.height);
				return true;
			}
			// TODO: Prüfe umliegende Felder, ob damit Burgen verbunden werden.
			// Je 4 Sektionen: [dabei nie OutOfBounds] gleiches Castle oder leer. 
			// x+	 1,-1  2, 0  1, 1
			// x-	-1,-1 -2, 0 -1, 1
			// y+	-1, 1  0, 2  1, 1
			// y-	-1,-1  0,-2  1,-1
		}
		
		return false;
	}
}
