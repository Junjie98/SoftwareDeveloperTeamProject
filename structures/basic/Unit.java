package structures.basic;
import commandbuilders.enums.Players;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This is a representation of a Unit on the game board.
 * A unit has a unique id (this is used by the front-end.
 * Each unit has a current UnitAnimationType, e.g. move,
 * or attack. The position is the physical position on the
 * board. UnitAnimationSet contains the underlying information
 * about the animation frames, while ImageCorrection has
 * information for centering the unit on the tile. 
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class Unit {

	@JsonIgnore
	protected static ObjectMapper mapper = new ObjectMapper(); // Jackson Java Object Serializer, is used to read java objects from a file
	
	int id;
	UnitAnimationType animation;
	Position position;
	UnitAnimationSet animations;
	ImageCorrection correction;
	Players owningPlayer;
	int unitHealth = 0;
	int unitDamage =  0;

	boolean isFlying = false;
	boolean isAvatar = false;
	//Ana: for counter attack
	boolean hasGotAttacked = false;
	//JJ: for attack logic. If attacked without move, it forfeits the move ability
	boolean hasMoved = false; //moved this for visibility
	boolean hasAttacked = false;
	public Unit() {}
	
	public Unit(int id, UnitAnimationSet animations, ImageCorrection correction) {
		super();
		this.id = id;
		this.animation = UnitAnimationType.idle;
		
		position = new Position(0,0,0,0);
		this.correction = correction;
		this.animations = animations;
	}
	
	public Unit(int id, UnitAnimationSet animations, ImageCorrection correction, Tile currentTile) {
		super();
		this.id = id;
		this.animation = UnitAnimationType.idle;
		
		position = new Position(currentTile.getXpos(),currentTile.getYpos(),currentTile.getTilex(),currentTile.getTiley());
		this.correction = correction;
		this.animations = animations;
	}

	public Unit(int id, UnitAnimationType animation, Position position, UnitAnimationSet animations,
			ImageCorrection correction) {
		super();
		this.id = id;
		this.animation = animation;
		this.position = position;
		this.animations = animations;
		this.correction = correction;
	}
	
	public void setHealth(int health) {
		if(health<=0) {
			unitHealth = 0;
		} else {
			unitHealth = health;
		}
	}

	public int getHealth()
	{
		return unitHealth;
	}
	public void setDamage(int health) 
	{
		unitDamage = health;
	}
	public int getDamage()
	{
		return unitDamage;
	}

	public boolean getHasMoved()
	{
		return hasMoved;
	}
	public void resetHasMoved()
	{
		hasMoved = false;
	}
	public void setHasMoved(boolean newValue) {
		hasMoved = newValue;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public UnitAnimationType getAnimation() {
		return animation;
	}
	public void setAnimation(UnitAnimationType animation) {
		this.animation = animation;
	}

	public ImageCorrection getCorrection() {
		return correction;
	}

	public void setCorrection(ImageCorrection correction) {
		this.correction = correction;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public UnitAnimationSet getAnimations() {
		return animations;
	}

	public void setAnimations(UnitAnimationSet animations) {
		this.animations = animations;
	}

	/**
	 * This command sets the position of the Unit to a specified
	 * tile.
	 * @param tile
	 */
	@JsonIgnore
	public void setPositionByTile(Tile tile) {
		position = new Position(tile.getXpos(),tile.getYpos(),tile.getTilex(),tile.getTiley());
	}

	public void setPlayerID(Players Player) 
	{
		this.owningPlayer = Player;
	}
	public Players getPlayerID()
	{
		return this.owningPlayer;
	}
	public boolean getHasGotAttacked() {
		return hasGotAttacked;
	}
	public void setHasGotAttacked(boolean hasGotAttacked) {
		this.hasGotAttacked = hasGotAttacked;
	}
	public void setHasAttacked(boolean hasAttacked) {
		this.hasAttacked = hasAttacked;
	}
	public boolean getHasAttacked() {
		return hasAttacked;
	}
    public void setFlying(boolean bool) {
		isFlying = bool;
    }
	public boolean isFlying() {
		return isFlying;
	}
	public boolean isAvatar() {
		return isAvatar;
	}
	public void setAvatar(boolean avatar) {
		isAvatar = avatar;
	}

	public Unit getCopy() {
		Unit copy = new Unit(id, animation, position, animations, correction);
		copy.setPlayerID(owningPlayer);
		copy.setHasGotAttacked(hasGotAttacked);
		copy.setHasAttacked(hasAttacked);
		copy.setFlying(isFlying);
		copy.setHealth(unitHealth);
		copy.setDamage(unitDamage);
		copy.setAvatar(isAvatar);
		return copy;
	}
}
