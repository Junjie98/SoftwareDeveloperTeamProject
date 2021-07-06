package structures.handlers;

import akka.actor.ActorRef;
import commandbuilders.PlayerSetCommandsBuilder;
import commandbuilders.ProjectTileAnimationCommandBuilder;
import commandbuilders.UnitCommandBuilder;
import commandbuilders.enums.*;
import commands.BasicCommands;
import structures.Board;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.UnitAnimationType;
import structures.memento.ActionType;
import structures.memento.AttackInformation;
import structures.memento.GameMemento;
import structures.memento.MovementInformation;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.HashSet;

public class UnitMovementAndAttack {
    Pair<Integer, Integer> activeUnit = null;
    private GameState parent;

    ArrayList<Unit> moveAttackAndCounterAttack = new ArrayList<>();

    boolean unitsCanMove = true;

    public UnitMovementAndAttack(GameState parent) {
        this.parent = parent;
    }

    // ===========================================================================
    // Highlight Logic
    // ===========================================================================
    public void unitClicked(ActorRef out, int x, int y) {
        Tile tile = parent.getBoard().getTile(x, y);
        if (tile.getUnit().getPlayerID() != parent.getTurn()) {
            // This is not your unit.
            return;
        }
        if(activeUnit != null) {
            Tile previousUnitLocation = parent.getBoard().getTile(activeUnit);

            //Unhighlight previously selected unit
            parent.getHighlighter().clearBoardHighlights(out);

            if (previousUnitLocation != tile) {
                // A new unit is clicked
                moveHighlight(out, x, y);
                activeUnit = new Pair<>(x, y);
            }
        } else if (unitsCanMove) {
            parent.getHighlighter().clearBoardHighlights(out);
            activeUnit = new Pair<>(x, y);
            moveHighlight(out, x, y);
        } else {
            System.err.println("Unit movement locked due to other units moving.");
        }
    }

    public void basicMoveHighlight(ActorRef out, int x, int y) {
        ArrayList<Pair<Integer, Integer>> initDir = parent.getMoveTiles(x, y, 1, 0);
        ArrayList<Pair<Integer, Integer>> secondDir = parent.getMoveTiles(x, y,2, 0);
        ArrayList<Pair<Integer, Integer>> interDir = parent.getMoveTiles(x, y, 1, 1);

        boolean[] initDirB = {true,true,true,true};

        int count = 0;
        for (Pair<Integer, Integer> is: initDir) {
            //for the initial directions you can move
            initDirB[count] = parent.getHighlighter().checkTileHighlight(out, is);       //if they are blocked record this
            count++;
        }

        count = 0;
        for (Pair<Integer, Integer> sd: secondDir) {
            //for the next tiles
            if(initDirB[count] == true) {
                //if the previous one is clear
                parent.getHighlighter().checkTileHighlight(out, sd);                  //check for units then highlight
            }
            count++;
        }

        //for the inter tiles do some logic
        if(initDirB[0] == true || initDirB[1] == true) {
            parent.getHighlighter().checkTileHighlight(out, interDir.get(0));
        }
        if(initDirB[1] == true || initDirB[3] == true) {
            parent.getHighlighter().checkTileHighlight(out, interDir.get(1));
        }
        if(initDirB[2] == true || initDirB[0] == true) {
            parent.getHighlighter().checkTileHighlight(out, interDir.get(2));
        }
        if(initDirB[2] == true || initDirB[3] == true) {
            parent.getHighlighter().checkTileHighlight(out, interDir.get(3));
        }
    }

    public void moveHighlight(ActorRef out, int x, int y) {
        if (parent.getBoard().getTile(x, y) != null) {
            Unit temp = parent.getBoard().getTile(x, y).getUnit();
            if(temp.isFlying() || temp.isRanged()) {
                System.err.println("flyhighlight");
                flyingOrRangedMoveHighlight(out);
            } else {
                basicMoveHighlight(out, x, y);
            }
        }
    }

    public void flyingOrRangedMoveHighlight(ActorRef out) {
        for (Pair<Integer, Integer> ti : getFlyMoveTiles()) {
            //available tiles
            parent.getHighlighter().checkTileHighlight(out, ti);
        }

        ArrayList<Pair<Integer, Integer>> units = new ArrayList<>();
        units.addAll(parent.player1UnitsPosition);
        units.addAll(parent.player2UnitsPosition);

        for (Pair<Integer, Integer> bl : units) {
            //Blocked tiles
            parent.getHighlighter().checkTileHighlight(out, bl);
        }
    }

    public ArrayList<Pair<Integer, Integer>> getAllMoveTiles(int x, int y) {
        ArrayList<Pair<Integer, Integer>> output = parent.getMoveTiles(x, y, 1, 0);
        output.addAll(parent.getMoveTiles(x, y, 2, 0));
        output.addAll(parent.getMoveTiles(x, y, 1, 1));
        return output;
    }

    public ArrayList<Pair<Integer, Integer>> getFlyMoveTiles() {
        int[][] maxContainer = new int[45][2];
        int count = 0 ;

        for(int x = 0; x < 9; x++ ) {
            for(int y = 0; y < 5; y ++) {
                if(!parent.getBoard().getTile(x, y).hasUnit()) {
                    int[] temp = {x, y};
                    System.err.println("tile: " + x + "," + y);
                    maxContainer[count++] = temp;
                }
            }
        }

        ArrayList<Pair<Integer, Integer>> output = new ArrayList<>();
        for(int i = 0; i < count; i++) {
            output.add(new Pair<>(maxContainer[i][0], maxContainer[i][1]));
        }
        return output;
    }

    public void highlightedMoveTileClicked(ActorRef out, int x, int y) {
        Tile activatedTile = parent.getBoard().getTile(activeUnit);

        if (activatedTile.getUnit().getHasAttacked()) {
            // Units that has attacked should not be able to move.
            parent.getHighlighter().clearBoardHighlights(out);
            return;
        }

        if (activatedTile.getUnit().getHasMoved()) {
            // Units cannot move twice in the same turn.
            parent.getHighlighter().clearBoardHighlights(out);
            return;
        }

        Tile destinationTile = parent.getBoard().getTile(x, y);

        if (destinationTile.getTileState() == States.NORMAL) {
            parent.getHighlighter().clearBoardHighlights(out);
        } else if (destinationTile.getTileState() == States.HIGHLIGHTED) {
            unitsCanMove = false;   // Prevent other units from moving.

            System.out.println("move valid");

            new UnitCommandBuilder(out, parent.isSimulation())
                    .setMode(UnitCommandBuilderMode.MOVE)
                    .setTilePosition(x, y)
                    .setUnit(activatedTile.getUnit())
                    .issueCommand();

            parent.memento.add(new GameMemento(parent.getTurn(), ActionType.MOVE, new MovementInformation(activatedTile.getUnit(), activeUnit, new Pair<>(x, y))));

            // Update the units position in the stored position lists.
            ArrayList<Pair<Integer, Integer>> pool = (parent.getTurn() == Players.PLAYER1) ?
                    parent.player1UnitsPosition : parent.player2UnitsPosition;
            for (Pair<Integer, Integer> position: pool) {
                if (position.equals(activeUnit)) {
                    pool.remove(position);
                    break;
                }
            }
            pool.add(new Pair<>(x, y));

            parent.getHighlighter().clearBoardHighlights(out);
            activatedTile.getUnit().setHasMoved(true);
            moveAttackAndCounterAttack.add(activatedTile.getUnit());
            activatedTile.setUnit(null);
        } else {
            // RED should be redirected to attack so should be here.
            parent.getHighlighter().clearBoardHighlights(out);
            System.err.println("Something went wrong");
        }
    }


    // ===========================================================================
    // Attack Logic
    // ===========================================================================
    public void launchAttack(ActorRef out, int x, int y) {
        if (activeUnit == null) { return; }
        if (parent.getBoard().getTile(x, y).getUnit().getPlayerID() != parent.getTurn()) {
        
            Tile enemyLocation = parent.getBoard().getTile(x, y);
            Tile attackerLocation =  parent.getBoard().getTile(activeUnit);
            Unit enemy = enemyLocation.getUnit();
            Unit attacker = attackerLocation.getUnit();
            
            if(attackCheck(x, y) || attacker.isRanged()) {
                boolean isRanged = attacker.isRanged();
                parent.memento.add(new GameMemento(parent.getTurn(), ActionType.ATTACK, new AttackInformation(activeUnit, new Pair<>(x, y), attacker, enemy)));
                int enemyHealthAfterAttack = attack(out, attackerLocation, enemy, attacker, x, y, isRanged);
                if (enemyHealthAfterAttack > 0) {
                    // Launch Counter Attack
                    int counterAttackResult = attack(out, enemyLocation, attacker, enemy, attacker.getPosition().getTilex(), attacker.getPosition().getTiley(), isRanged);
                    
                    if (counterAttackResult <= 0) {
                        // Handle unit died of counter attack
                    	BasicCommands.playUnitAnimation(out, attacker, UnitAnimationType.death);
            			try {Thread.sleep(3000);} catch (InterruptedException e) {e.printStackTrace();}
                    	
                    	new UnitCommandBuilder(out, parent.isSimulation())
	                        .setMode(UnitCommandBuilderMode.DELETE)
	                        .setUnit(attackerLocation.getUnit())
	                        .issueCommand();

                        attackerLocation.setUnit(null);
                        ArrayList<Pair<Integer, Integer>> pool = (parent.getTurn() == Players.PLAYER1) ?
                                parent.player1UnitsPosition : parent.player2UnitsPosition;
                        Pair<Integer, Integer> positionToRemove = new Pair<>(attackerLocation.getTilex(), attackerLocation.getTiley());
                        for (Pair<Integer, Integer> position: pool) {
                            if (position.equals(positionToRemove)) {
                                pool.remove(position);
                                break;
                            }
                        }
                    }
                } else {
                	BasicCommands.playUnitAnimation(out, enemy, UnitAnimationType.death);
        			try {Thread.sleep(3000);} catch (InterruptedException e) {e.printStackTrace();}
                	
                	new UnitCommandBuilder(out, parent.isSimulation())
	                    .setMode(UnitCommandBuilderMode.DELETE)
	                    .setUnit(enemyLocation.getUnit())
	                    .issueCommand();

                    enemyLocation.setUnit(null);
                    ArrayList<Pair<Integer, Integer>> pool = (parent.getTurn() == Players.PLAYER1) ?
                            parent.player2UnitsPosition : parent.player1UnitsPosition;
                    Pair<Integer, Integer> positionToRemove = new Pair<>(x, y);
                    for (Pair<Integer, Integer> position: pool) {
                        if (position.equals(positionToRemove)) {
                            pool.remove(position);
                            break;
                        }
                    }
                }
            }
        }
    }

    // Ana: Counter attack, including ranged attack
    public int attack(ActorRef out, Tile attackerLocation, Unit enemy, Unit attacker, int x, int y, boolean isRanged) {
        UnitCommandBuilder enemyCommandBuilder = new UnitCommandBuilder(out, parent.isSimulation()).setUnit(enemy);
        int enemyHealth = enemy.getHealth();
        int healthAfterDamage = enemyHealth - attacker.getDamage();

        if (healthAfterDamage < 0)
            healthAfterDamage = 0;

        if (healthAfterDamage < 0)
            healthAfterDamage = 0;
        
//        if(isRanged) {
//			System.err.println("Ranged attack incoming!");
//			new ProjectTileAnimationCommandBuilder(out)
//			.setSource(attackerLocation)
//			.setDistination(parent.getBoard().getTile(x, y))
//			.issueCommand();
//		}
        
		new ProjectTileAnimationCommandBuilder(out, parent.isSimulation())
			.setSource(attackerLocation)
			.setDistination(parent.getBoard().getTile(x, y))
			.issueCommand();
        
//        BasicCommands.playUnitAnimation(out, attacker, UnitAnimationType.attack);
//		try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}

        enemyCommandBuilder
            .setMode(UnitCommandBuilderMode.SET)
            .setStats(UnitStats.HEALTH, healthAfterDamage)
            .issueCommand();

        //unhighlight all the tiles
        parent.getHighlighter().clearBoardHighlights(out);

        
//        // To be added if needed
//        //restrict human player to attack again
//        enemy.setHasGotAttacked(true);
//        moveAttackAndCounterAttack.add(enemy);
//
//        //restrict player to move after attack
//        attacker.setHasAttacked(true);
//        moveAttackAndCounterAttack.add(attacker);
//        //
        
        //update avatar health to UI player health.
        if(enemy.isAvatar() && enemy.getPlayerID() == Players.PLAYER1) {
            parent.getPlayer1().setHealth(enemy.getHealth());
            new PlayerSetCommandsBuilder(out, parent.isSimulation())
                    .setPlayer(Players.PLAYER1)
                    .setStats(PlayerStats.HEALTH)
                    .setInstance(parent.getPlayer1())
                    .issueCommand();
        } else if(enemy.isAvatar() && enemy.getPlayerID()== Players.PLAYER2) {
            parent.getPlayer2().setHealth(enemy.getHealth());
            new PlayerSetCommandsBuilder(out, parent.isSimulation())
                    .setPlayer(Players.PLAYER2)
                    .setStats(PlayerStats.HEALTH)
                    .setInstance(parent.getPlayer2())
                    .issueCommand();
        } else if(attacker.isAvatar() && attacker.getPlayerID()== Players.PLAYER1) {
            parent.getPlayer1().setHealth(attacker.getHealth());
            new PlayerSetCommandsBuilder(out, parent.isSimulation())
                    .setPlayer(Players.PLAYER1)
                    .setStats(PlayerStats.HEALTH)
                    .setInstance(parent.getPlayer1())
                    .issueCommand();
        } else if(attacker.isAvatar() && attacker.getPlayerID()== Players.PLAYER2) {
            parent.getPlayer2().setHealth(attacker.getHealth());
            new PlayerSetCommandsBuilder(out, parent.isSimulation())
                    .setPlayer(Players.PLAYER2)
                    .setStats(PlayerStats.HEALTH)
                    .setInstance(parent.getPlayer2())
                    .issueCommand();
        }

        // Win condition: should be moved to a method where we are checking player's health
        if (parent.getPlayer1().getHealth() < 1 || parent.getPlayer2().getHealth() < 1) {
            parent.endGame(out);
        }

        return enemy.getHealth();
    }

    public boolean attackCheck(int x, int y) {
        if (activeUnit == null) { return false; }

        int[] acPos = {x, y};
        ArrayList<Pair<Integer, Integer>> tileActive = getAllMoveTiles(activeUnit.getFirst(), activeUnit.getSecond());

        //Ana: for counter attack
        if (parent.getBoard().getTile(x, y).getUnit() != null && parent.getBoard().getTile(x, y).getUnit().getHasGotAttacked())
            return false;

        for (Pair<Integer, Integer> ip: tileActive) {
            if(ip.getFirst()== acPos[0] && ip.getSecond() == acPos[1]) {
                if(parent.getBoard().getTile(x, y).getUnit() != null) {
                    //enemy is in this tile
                    return true;
                }
                return false;
            }
        }

        return false;
    }

    // ===========================================================================
    // Setters, getters, and resetters
    // ===========================================================================
    public void resetMoveAttackAndCounterAttack(ActorRef out) {
        for (Unit unit: moveAttackAndCounterAttack) {
            unit.setHasMoved(false);
            unit.setHasAttacked(false);
            unit.setHasGotAttacked(false);
            
            new UnitCommandBuilder(out, parent.isSimulation())
	            .setUnit(unit)
	            .setMode(UnitCommandBuilderMode.ANIMATION)
	            .setAnimationType(UnitAnimationType.idle)
	            .issueCommand();
        }
        moveAttackAndCounterAttack.clear();
    }

    public Pair<Integer, Integer> getActiveUnit() {
        return activeUnit;
    }

    public void setActiveUnit(Pair<Integer, Integer> activeUnit) {
        this.activeUnit = activeUnit;
    }

    public void setUnitsCanMove(boolean unitsCanMove) {
        this.unitsCanMove = unitsCanMove;
    }
}