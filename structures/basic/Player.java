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

	public int getHealth() {
		return health;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	                /*
					*Sets the cap of health and mana.
                    *@author Jun Jie Low (2600104L@student.gla.ac.uk/nelsonlow_88@hotmail.com)
                    */
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
//////////////////////////////////////////////////////////////////////////////////////////		
	}
}
