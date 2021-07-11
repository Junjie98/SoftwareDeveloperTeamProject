package structures.basic;

import commandbuilders.enums.Players;
import commandbuilders.enums.UnitType;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import structures.handlers.Pair;

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
	int unitIdentifier = 0; //0==unit & 1==player avatar
	UnitType type = null;

	boolean provoked = false;
	boolean provoker =false;
	ArrayList<Unit> unitProvoked = new ArrayList<>();

	String configFile = ""; // For copies
	String name = ""; // For memento

	boolean isFlying = false;
	boolean isAvatar = false;
	//Ana: for counter attack
	ArrayList<Unit> attackedBy = new ArrayList<>();

	//Ana : For ranged
	boolean isRanged = false;
	//JJ: for attack logic. If attacked without move, it forfeits the move ability
	boolean hasMoved = false; //moved this for visibility

	int attackCount = 0;
	int attackLimit = 1;

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
	
	public void setIdentifier(int num) 
	{
		if(num > 1 || num < 0) 
		{
			System.err.println("Please insert 1 for avatar. or 0 for unit.");
			// you dont need to specific 0 for unit as I only need this for avatar to update the UI.
			}else {
				this.unitIdentifier = num;
			}
	}
	
	public int getIdentifer() 
	{
		return this.unitIdentifier;
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
	
	public void setProvoker(boolean value){
		this.provoker = value;
	}
	public boolean getProvoker(){
		return this.provoker;
	}
	public void setProvoked(boolean value) {
		this.provoked = value;
	}
	public boolean getProvoked() {
		return this.provoked;
	}
	public void setProvokedMove(boolean value){
		hasMoved = value;
	}
	public void setUnitProvoked(Unit value){
		unitProvoked.add(value);
	}
	public ArrayList<Unit> getUnitProvoked(){
		return unitProvoked;
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

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}
	public String getConfigFile() {
		return configFile;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}

	public void setPlayerID(Players Player) 
	{
		this.owningPlayer = Player;
	}
	public Players getPlayerID()
	{
		return this.owningPlayer;
	}
	public void setHasAttacked() {
		attackCount++;
	}
	public boolean getHasAttacked() {
		return attackCount >= attackLimit;
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
	public void setRanged(boolean bool) {
 		this.isRanged = bool;
    }
	public boolean isRanged() {
		return isRanged;
	}

	public UnitType getType() {
		return type;
	}

	public void setType(UnitType type) {
		this.type = type;
	}

	public Pair<Integer, Integer> getLocationPair() {
		return new Pair<>(position.tilex, position.tiley);
	}

	public void addAttacker(Unit attacker) {
		attackedBy.add(attacker);
	}

	public boolean hasBeenAttackedBy(Unit attacker) {
		if (attacker.type == UnitType.AZURITE_LION || attacker.type == UnitType.SERPENTI) {
			int counter = 0;
			for (Unit unit: attackedBy) {
				if (unit == attacker) {
					counter++;
				}
			}
			return counter >= 2;
		}
		return attackedBy.contains(attacker);
	}

	public void clearAttackers() {
		attackedBy.clear();
	}

	public void resetAttackCount() {
		attackCount = 0;
	}

	public void setAttackLimit(int value) {
		attackLimit = value;
	}
}
