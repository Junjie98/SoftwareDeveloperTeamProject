package structures.basic;


/**
 * This is the base representation of a Card which is rendered in the player's hand.
 * A card has an id, a name (cardname) and a manacost. A card then has a large and mini
 * version. The mini version is what is rendered at the bottom of the screen. The big
 * version is what is rendered when the player clicks on a card in their hand.
 * 
 * @author Dr. Richard McCreadie
 *
 * Getters and properties are added when needed:
 *
 * @author Theodoros Vrakas (2593566v@student.gla.ac.uk)
 * @author William T Manson (2604495m@student.gla.ac.uk)
 * @author Anamika Maurya (2570847M@student.gla.ac.uk)
 * @author Yu-Sung Hsu (2540296h@student.gla.ac.uk)
 * @author Jun Jie Low (2600104L@student.gla.ac.uk/nelsonlow_88@hotmail.com)
 */
public class Card {

	static final String[] spellcards = {"Entropic Decay", "Staff of Y'Kir'", "Truestrike", "Sundrop Elixir"};
	static final String[] specialCards = {"Azure Herald", "Pureblade Enforcer", "Azurite Lion", "Planar Scout", "Blaze Hound", "Serpenti"};

	int id;
	String cardname;
	int manacost;
	MiniCard miniCard;
	BigCard bigCard;
	
	public Card() {};
	
	public Card(int id, String cardname, int manacost, MiniCard miniCard, BigCard bigCard) {
		super();
		this.id = id;
		this.cardname = cardname;
		this.manacost = manacost;
		this.miniCard = miniCard;
		this.bigCard = bigCard;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCardname() {
		return cardname;
	}
	public void setCardname(String cardname) {
		this.cardname = cardname;
	}
	public int getManacost() {
		return manacost;
	}
	public void setManacost(int manacost) {
		this.manacost = manacost;
	}
	public MiniCard getMiniCard() {
		return miniCard;
	}
	public void setMiniCard(MiniCard miniCard) {
		this.miniCard = miniCard;
	}
	public BigCard getBigCard() {
		return bigCard;
	}
	public void setBigCard(BigCard bigCard) {
		this.bigCard = bigCard;
	}
	public boolean isSpell() {
		for (String item: spellcards) {
			if (item.equals(cardname)) {
				return true;
			}
		}
		return false;
	}
	public boolean isSpecialCard() {
		for (String item: specialCards) {
			if (item.equals(cardname)) {
				return true;
			}
		}
		return false;
	}
}
