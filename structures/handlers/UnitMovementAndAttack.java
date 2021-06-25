package structures.handlers;

import akka.actor.ActorRef;
import commandbuilders.PlayerSetCommandsBuilder;
import commandbuilders.ProjectTileAnimationCommandBuilder;
import commandbuilders.UnitCommandBuilder;
import commandbuilders.enums.*;
import structures.Board;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;

import java.util.ArrayList;

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
        Tile tile = Board.getInstance().getTile(x, y);
        if (tile.getUnit().getPlayerID() != parent.getTurn()) {
            // This is not your unit.
            return;
        }
        if(activeUnit != null) {
            Tile previousUnitLocation = Board.getInstance().getTile(activeUnit);

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
        if (Board.getInstance().getTile(x, y) != null) {
            Unit temp = Board.getInstance().getTile(x, y).getUnit();
            if(temp.isFlying()) {
                System.err.println("flyhighlight");
                flyingMoveHighlight(out);
            } else {
                basicMoveHighlight(out, x, y);
            }
        }
    }

    public void flyingMoveHighlight(ActorRef out) {
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
                if(!Board.getInstance().getTile(x, y).hasUnit()) {
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
        Tile activatedTile = Board.getInstance().getTile(activeUnit);

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

        Tile destinationTile = Board.getInstance().getTile(x, y);

        if (destinationTile.getTileState() == States.NORMAL) {
            parent.getHighlighter().clearBoardHighlights(out);
        } else if (destinationTile.getTileState() == States.HIGHLIGHTED) {
            unitsCanMove = false;   // Prevent other units from moving.

            System.out.println("move valid");

            new UnitCommandBuilder(out)
                    .setMode(UnitCommandBuilderMode.MOVE)
                    .setTilePosition(x, y)
                    .setUnit(activatedTile.getUnit())
                    .issueCommand();

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
        if (Board.getInstance().getTile(x, y).getUnit().getPlayerID() != parent.getTurn()) {
            if(attackCheck(x, y)) {
                Tile enemyLocation = Board.getInstance().getTile(x, y);
                Tile attackerLocation =  Board.getInstance().getTile(activeUnit);
                Unit enemy = enemyLocation.getUnit();
                Unit attacker = attackerLocation.getUnit();

                int enemyHealthAfterAttack = attack(out, attackerLocation, enemy, attacker, x, y);
                if (enemyHealthAfterAttack > 0) {
                    // Launch Counter Attack
                    int counterAttackResult = attack(out, enemyLocation, attacker, enemy, attacker.getPosition().getTilex(), attacker.getPosition().getTiley());
                    if (counterAttackResult <= 0) {
                        System.out.println("Hello");
                        // Handle unit died of counter attack
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

    // Ana: Counter attack, ranged attack not included
    public int attack(ActorRef out, Tile attackerLocation, Unit enemy, Unit attacker, int x, int y) {
        UnitCommandBuilder enemyCommandBuilder = new UnitCommandBuilder(out).setUnit(enemy);
        int enemyHealth = enemy.getHealth();
        int healthAfterDamage =  enemyHealth - attacker.getDamage();

        new ProjectTileAnimationCommandBuilder(out)
                .setSource(attackerLocation)
                .setDistination(Board.getInstance().getTile(x, y))
                .issueCommand();

        // TODO: Do this when far away only. Use a normal attack animation if close.

        enemyCommandBuilder
                .setMode(UnitCommandBuilderMode.SET)
                .setStats(UnitStats.HEALTH, healthAfterDamage)
                .issueCommand();

        //unhighlight all the tiles
        parent.getHighlighter().clearBoardHighlights(out);

        //restrict human player to attack again
        enemy.setHasGotAttacked(true);
        moveAttackAndCounterAttack.add(enemy);

        //restrict player to move after attack
        attacker.setHasAttacked(true);
        moveAttackAndCounterAttack.add(attacker);

        //update avatar health to UI player health.
        if(enemy.getIdentifer() == 1 && enemy.getPlayerID() == Players.PLAYER1) {
            parent.getPlayer1().setHealth(enemy.getHealth());
            new PlayerSetCommandsBuilder(out)
                    .setPlayer(Players.PLAYER1)
                    .setStats(PlayerStats.HEALTH)
                    .setInstance(parent.getPlayer1())
                    .issueCommand();
        } else if(enemy.getIdentifer() == 1 && enemy.getPlayerID()== Players.PLAYER2) {
            parent.getPlayer2().setHealth(enemy.getHealth());
            new PlayerSetCommandsBuilder(out)
                    .setPlayer(Players.PLAYER2)
                    .setStats(PlayerStats.HEALTH)
                    .setInstance(parent.getPlayer2())
                    .issueCommand();
        } else if(attacker.getIdentifer() == 1 && attacker.getPlayerID()== Players.PLAYER1) {
            parent.getPlayer1().setHealth(attacker.getHealth());

            new PlayerSetCommandsBuilder(out)
                    .setPlayer(Players.PLAYER1)
                    .setStats(PlayerStats.HEALTH)
                    .setInstance(parent.getPlayer1())
                    .issueCommand();
        } else if(attacker.getIdentifer() == 1 && attacker.getPlayerID()== Players.PLAYER2) {
            parent.getPlayer2().setHealth(attacker.getHealth());
            new PlayerSetCommandsBuilder(out)
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
        if (Board.getInstance().getTile(x, y).getUnit() != null && Board.getInstance().getTile(x, y).getUnit().getHasGotAttacked())
            return false;

        for (Pair<Integer, Integer> ip: tileActive) {
            if(ip.getFirst()== acPos[0] && ip.getSecond() == acPos[1]) {
                if(Board.getInstance().getTile(x, y).getUnit() != null) {
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
    public void resetMoveAttackAndCounterAttack() {
        for (Unit unit: moveAttackAndCounterAttack) {
            unit.setHasMoved(false);
            unit.setHasAttacked(false);
            unit.setHasGotAttacked(false);
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