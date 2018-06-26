package turrets;

import java.util.ArrayList;

public class Player {

	
	String name = "";
	String color;		// unique color for chat.

	int points = 0;
	int usedKnights = 0;
	ArrayList<Knight> knights = new ArrayList<>(6);
	
	int availableAP;
	int AP = 5;			// AktionsPunkte
	int pieces = 3;		// TODO: nur für 2 Spieler konstant!
	boolean usedCard;	// wurde diesen Zug eine Karte gespielt?
	
	boolean playCard(String cardName) {
		
		if (this.usedCard) return false;
		usedCard = true;
		return true;
	}

	public Player(String name, int player) {
		System.out.println("player "+(player+1)+"s name: "+name);
		this.name = name;
	}
}
