package structures.basic;

/**
 * A basic representation of of the Player. A player
 * has health and mana.
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class Player {
	int health;
	int mana;
	
	public Player() {
		super();
		this.health = 20;
		this.mana = 0;
	}
	public Player(int health, int mana) {
		super();
		this.health = health;
		this.mana = mana;
	}

	public Player(Player inPlayer)
	{
		this.health = inPlayer.health;
		this.mana = inPlayer.mana;
	}

	public int getHealth() {
		return health;
	}
	public void setHealth(int health) {
		if(health > 20) { //sets the health cap.
			this.health = 20;
		}
		this.health = health;
	}
	public int getMana() {
		return mana;
	}
	public void setMana(int mana) {
		if(mana > 9) { //sets the mana cap.
			this.mana = 9;
		}else {
			this.mana = mana;
		}
		
	}
}
