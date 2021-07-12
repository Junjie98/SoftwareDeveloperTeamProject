package structures.handlers;

import akka.actor.ActorRef;
import commandbuilders.PlayerSetCommandsBuilder;
import commandbuilders.ProjectTileAnimationCommandBuilder;
import commandbuilders.UnitCommandBuilder;
import commandbuilders.enums.*;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.UnitAnimationType;
import structures.extractor.ExtractedGameState;
import structures.memento.ActionType;
import structures.memento.AttackInformation;
import structures.memento.GameMemento;
import structures.memento.MovementInformation;

import java.util.ArrayList;

/**
 * This class consists of a unit's movement and attack logic.
 * @author Anamika Maurya (2570847M@student.gla.ac.uk)
 * @author Thorfinn Manson (2604495M@student.gla.ac.uk)
 * @author Yu-Sung Hsu (2540296h@student.gla.ac.uk)
 * @author Jun Jie Low (2600104L@student.gla.ac.uk/nelsonlow_88@hotmail.com)
 */

public class UnitMovementAndAttack {
    Pair<Integer, Integer> activeUnit = null;
    private final GameState parent;

    ArrayList<Unit> moveAttackAndCounterAttack = new ArrayList<>();

    boolean unitsCanMove = true;
   // boolean provokerAround = false;
    ArrayList<Unit> provokedUnitForEndReset = new ArrayList<>();
    int valueX = 0; int valueY = 0; //for flying and range unit provoke.
    public UnitMovementAndAttack(GameState parent) {
        this.parent = parent;
    }

    // ===========================================================================
    // Highlight Logic
    // ===========================================================================
    // @author William T Manson (2604495m@student.gla.ac.uk)
    public void unitClicked(ActorRef out, int x, int y) {
        Tile tile = parent.getBoard().getTile(x, y);
        if (tile.getUnit().getPlayerID() != parent.getTurn()) {
            // This is not your unit.
            System.out.println(tile.getUnit().getPlayerID());
            return;
        }
        if(activeUnit != null) {
            Tile previousUnitLocation = parent.getBoard().getTile(activeUnit);
            //Unhighlight previously selected unit
            parent.getHighlighter().clearBoardHighlights(out);

            if (previousUnitLocation != tile) {
                // A new unit is clicked
                activeUnit = new Pair<>(x, y);
                if(parent.getBoard().getTile(activeUnit).getUnit().getHasMoved())
                {
                    movedButCanAttackHighlight(out, x, y);

                }
                else{
                    moveHighlight(out, x, y);
                }
            }
        } else if (unitsCanMove) {
            parent.getHighlighter().clearBoardHighlights(out);
            activeUnit = new Pair<>(x, y);
            if(parent.getBoard().getTile(activeUnit).getUnit().getHasMoved())
            {
                movedButCanAttackHighlight(out, x, y);
            }
            else{
                moveHighlight(out, x, y);
            }
        } else {
            System.err.println("Unit movement locked due to other units moving.");
        }
    }

    // @author William T Manson (2604495m@student.gla.ac.uk)
    public void basicMoveHighlight(ActorRef out, int x, int y, boolean redOnly, boolean whiteOnly) {
        ArrayList<Pair<Integer, Integer>> initDir = parent.getMoveTiles(x, y, 1, 0);
        ArrayList<Pair<Integer, Integer>> secondDir = parent.getMoveTiles(x, y,2, 0);
        ArrayList<Pair<Integer, Integer>> interDir = parent.getMoveTiles(x, y, 1, 1);

        boolean[] initDirB = {true,true,true,true};

        int count = 0;
        for (Pair<Integer, Integer> is: initDir) {
            //for the initial directions you can move
            initDirB[count] = parent.getHighlighter().checkTileHighlight(out, is, false, redOnly);       //if they are blocked record this
            count++;
        }

        count = 0;
        for (Pair<Integer, Integer> sd: secondDir) {
            //for the next tiles
            if(initDirB[count]) {
                //if the previous one is clear
                parent.getHighlighter().checkTileHighlight(out, sd, false, redOnly);                  //check for units then highlight
            }
            count++;
        }

        //for the inter tiles do some logic
        if(initDirB[0] || initDirB[1]) {
            parent.getHighlighter().checkTileHighlight(out, interDir.get(0), false, redOnly);
        }
        if(initDirB[1] || initDirB[3]) {
            parent.getHighlighter().checkTileHighlight(out, interDir.get(1), false, redOnly);
        }
        if(initDirB[2] || initDirB[0]) {
            parent.getHighlighter().checkTileHighlight(out, interDir.get(2), false, redOnly);
        }
        if(initDirB[2] || initDirB[3]) {
            parent.getHighlighter().checkTileHighlight(out, interDir.get(3), false, redOnly);
        }
        if (!whiteOnly && !redOnly) {
            //basica attack highlight connected to normal movement highlight
            ArrayList<Pair<Integer, Integer>> atkTiles = getAllAtkTiles(x, y);

            for (Pair<Integer, Integer> pos : atkTiles) {
                Pair<Integer,Integer> moveTile = getMoveTileForAttack(x, y, pos.getFirst(), pos.getSecond());
                if (parent.getBoard().getTile(moveTile) != null) {
                    if (!parent.getBoard().getTile(moveTile).hasUnit()) {
                        parent.getHighlighter().checkAttackHighlight(out, pos);
                    }
                }
            }
        }
        //Checks the tile of length 1.
        provokeFunc(x,y);
    }

    // @author William T Manson (2604495m@student.gla.ac.uk)
    public void movedButCanAttackHighlight(ActorRef out, int x, int y)
    {
        if(parent.getBoard().getTile(x,y) != null)
        {
            Unit temp = parent.getBoard().getTile(x, y).getUnit();
            if(temp.isRanged()) {
                flyingOrRangedMoveHighlight(out, temp);
                provokeFunc(x, y);
            }
            else{
                ArrayList<Pair<Integer,Integer>> tiles = get1RAtkTiles(x, y);
                for (Pair<Integer,Integer> pair : tiles) {
                    parent.getHighlighter().checkAttackHighlight(out, pair);
                    provokeFunc(x, y);
                }
            }

        }
    }

    // @author William T Manson (2604495m@student.gla.ac.uk)
    public void moveHighlight(ActorRef out, int x, int y) {
        if (parent.getBoard().getTile(x, y) != null) {
            Unit temp = parent.getBoard().getTile(x, y).getUnit();
            if(temp.isFlying() || temp.isRanged()) {
                System.err.println("flyhighlight");
                flyingOrRangedMoveHighlight(out, temp);
                valueX = x; valueY = y;
            } else {
                basicMoveHighlight(out, x, y, false, false);
            }
        }
    }

    /**
     * This method consists logic to highlight tiles for flying or ranged attacks.
     * @author Anamika Maurya (2570847M@student.gla.ac.uk)
     */
    public void flyingOrRangedMoveHighlight(ActorRef out, Unit unit) {
        if (unit.isFlying()) {
            for (Pair<Integer, Integer> ti : getFlyMoveTiles()) {
                //available tiles
                parent.getHighlighter().checkTileHighlight(out, ti, false, false);
            }
        } else {
            basicMoveHighlight(out, unit.getPosition().getTilex(), unit.getPosition().getTiley(), false, true);
        }
        provokeFunc(valueX,valueY);

        if (!unit.isRanged()) {
            basicMoveHighlight(out, unit.getPosition().getTilex(), unit.getPosition().getTiley(), true, false);
        } else {
            ArrayList<Pair<Integer, Integer>> units = new ArrayList<>();
            units.addAll(parent.player1UnitsPosition);
            units.addAll(parent.player2UnitsPosition);

            for (Pair<Integer, Integer> bl : units) {
                //Blocked tiles
                parent.getHighlighter().checkTileHighlight(out, bl, false, false);
            }
        }
    }

    /**
     * This method is used to highlight the tiles for units that
     * can be summoned anywhere.
     * @author Anamika Maurya (2570847M@student.gla.ac.uk)
     */
    public void summonAnywhereHighlight(ActorRef out) {
        for (Pair<Integer, Integer> ti : getFlyMoveTiles()) {
            //available tiles
            parent.getHighlighter().checkTileHighlight(out, ti, true, false);
        }
    }

    // @author William T Manson (2604495m@student.gla.ac.uk)
    public ArrayList<Pair<Integer, Integer>> getAllMoveTiles(int x, int y) {
        ArrayList<Pair<Integer, Integer>> output = parent.getMoveTiles(x, y, 1, 0);
        output.addAll(parent.getMoveTiles(x, y, 2, 0));
        output.addAll(parent.getMoveTiles(x, y, 1, 1));
        return output;
    }

    // @author William T Manson (2604495m@student.gla.ac.uk)
    public ArrayList<Pair<Integer, Integer>> getAllAtkTiles(int x, int y) {
        ArrayList<Pair<Integer, Integer>> output = parent.getMoveTiles(x, y, 2, 1);
        output.addAll(parent.getMoveTiles(x, y, 2, 2));
        output.addAll(parent.getMoveTiles(x, y, 1, 2));
        output.addAll(parent.getMoveTiles(x, y, 1, 3));
        output.addAll(parent.getMoveTiles(x, y, 3, 0));
        output.addAll(parent.getMoveTiles(x, y, 3, 1));
        return output;
    }

    // @author William T Manson (2604495m@student.gla.ac.uk)
    public ArrayList<Pair<Integer, Integer>> get1RAtkTiles(int x, int y) {
        ArrayList<Pair<Integer, Integer>> output = parent.getMoveTiles(x, y, 1, 0);
        output.addAll(parent.getMoveTiles(x, y, 1, 1));
        return output;
    }

    // @author William T Manson (2604495m@student.gla.ac.uk)
    public Pair<Integer,Integer> getMoveTileForAttack(int Ax, int Ay, int Ex, int Ey)
    {
        //ArrayList<Pair<Integer,Integer>> rangeTiles = get1RAtkTiles(x, y);
        int mx = Ax-Ex;
        int my = Ay-Ey;

        if(Math.abs(mx)>0)
        {
            mx = mx > 0 ? 1 : -1;
        }
        if(Math.abs(my) > 0)
        {
            my = my > 0 ? 1 : -1;
        }
        Pair<Integer, Integer> out = new Pair<>(Ex + mx, Ey + my);
        return out;
    }

    // @author William T Manson (2604495m@student.gla.ac.uk)
    public ArrayList<Pair<Integer, Integer>> getFlyMoveTiles() {
        int[][] maxContainer = new int[45][2];
        int count = 0 ;

        for(int x = 0; x < 9; x++ ) {
            for(int y = 0; y < 5; y++) {
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

    // @author William T Manson (2604495m@student.gla.ac.uk)
    public void highlightedMoveTileClicked(ActorRef out, int x, int y) {
        Tile activatedTile = parent.getBoard().getTile(activeUnit);
        System.out.println("move activated to: " +x + ":" + y);
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
        System.out.println("chekcing move");
        if (destinationTile.getTileState() == States.NORMAL) {
            parent.getHighlighter().clearBoardHighlights(out);
        } else if (destinationTile.getTileState() == States.HIGHLIGHTED && unitsCanMove) { //added another condition to check
            if (!(parent instanceof ExtractedGameState)) {
                unitsCanMove = false;   // Prevent other units from moving.
            }

            System.out.println("move valid : boolean = " + unitsCanMove); //debug //results found that within enemy tile range, the bool is set to true.

            Unit unit = activatedTile.getUnit();

            new UnitCommandBuilder(out, parent.isSimulation())
                    .setMode(UnitCommandBuilderMode.MOVE)
                    .setTilePosition(x, y)
                    .setUnit(unit)
                    .issueCommand();

            Tile tile = parent.getBoard().getTile(x, y);
            unit.setPositionByTile(tile);

            parent.memento.add(new GameMemento(parent.getTurn(), ActionType.MOVE, new MovementInformation(unit, activeUnit, new Pair<>(x, y))));
            System.out.println(parent.memento.get(parent.memento.size() - 1));

            // Update the units position in the stored position lists.
            ArrayList<Pair<Integer, Integer>> pool = parent.getUnitsPosition(parent.getTurn());
            parent.removeFromPool(pool, activeUnit);
            pool.add(new Pair<>(x, y));

            destinationTile.setUnit(unit);
            parent.getHighlighter().clearBoardHighlights(out);

            destinationTile.getUnit().setHasMoved(true);
            activatedTile.setUnit(null);
            moveAttackAndCounterAttack.add(unit);
        } else {
            // RED should be redirected to attack so should be here.
            parent.getHighlighter().clearBoardHighlights(out);
            System.err.println("Something went wrong");
        }
    }


    // ===========================================================================
    // Attack Logic
    // ===========================================================================

    /**
     * This method is used to write attack logic for a unit.
     *
     * This method is initiated by Yu-Sung Hsu,
     * Rewritten by William T Manson (2604495m@student.gla.ac.uk),
     * Refactored by
     * @author Anamika Maurya (2570847M@student.gla.ac.uk)
     * @author Yu-Sung Hsu (2540296h@student.gla.ac.uk)
     */
    public void launchAttack(ActorRef out, int x, int y) {
        try{
            if (activeUnit == null) { return; }
            if (parent.getBoard().getTile(x, y).getUnit().getPlayerID() != parent.getTurn()) {
                System.out.println("attackLaunched");
                Tile enemyLocation = parent.getBoard().getTile(x, y);
                Tile attackerLocation =  parent.getBoard().getTile(activeUnit);
                Unit enemy = enemyLocation.getUnit();
                Unit attacker = attackerLocation.getUnit();

                if(attackCheck(x, y, attacker) || attacker.isRanged()) {
                    System.err.println("attack check passed");
                    boolean isRanged = attacker.isRanged();
                    int enemyHealthAfterAttack = enemy.getHealth();

                ////////////////////////////////////////////////////////////////////////////////////////
                /**
                * This implementation below checks if unit has been provoked.
                * if it is, it can only attack provoker unit. Else, if not provoked,
                * it can attack anyone that is within the range. (checked by attackCheck above ^)
                * @author Jun Jie Low (2600104L@student.gla.ac.uk/nelsonlow_88@hotmail.com)
                */
                if(enemy.getProvoker()&&attacker.getProvoked()) {
                    provokeAttack(out, attackerLocation, enemyLocation, isRanged);
                    return;
                }
                if(!attacker.getProvoked()){
                    if(isRanged){
                        //do ranged attack
                        enemyHealthAfterAttack = attack(out, attackerLocation, enemyLocation, isRanged);

                        if (enemyHealthAfterAttack > 0) {
                            if (enemy.isRanged()) {
                                int counter = attack(out, enemyLocation, attackerLocation, isRanged);
                                if (counter <= 0) {
                                    parent.unitDied(out, attackerLocation, parent.getUnitsPosition(parent.getTurn()));
                                }
                            }
                        /////////////////////////////////////////////////////////////////////////////////////////
                        } else {
                            parent.unitDied(out, enemyLocation, parent.getEnemyUnitsPosition(parent.getTurn()));
                        }
                        resetAnimations(out);
                        return;
                    }

                    if(!attack1RCheck(x, y)){
                        //if the unit is not within normal attack range
                        //move to a range then attack
                        Pair<Integer,Integer> moveTile = getMoveTileForAttack(attackerLocation.getTilex(), attackerLocation.getTiley(), x, y);
                        System.out.println(moveTile);
                        //boolean provokerNearYou = provokeFuncMoveAttackCheck(x, y);
                        if(parent.getBoard().getTile(moveTile).hasUnit()){
                            //if this tile is blocked then we need to see if any other tiles in the atk range are within our move range
                            ArrayList<Pair<Integer,Integer>> rangeTiles = get1RAtkTiles(x, y);
                            ArrayList<Pair<Integer,Integer>> moveTiles = getAllMoveTiles(attackerLocation.getTilex(), attackerLocation.getTiley());
                            ArrayList<Pair<Integer,Integer>> goodMoves = new ArrayList<>();
                            for (Pair<Integer,Integer> pair : rangeTiles) {
                                for (Pair<Integer,Integer> pair2 : moveTiles) {
                                    if(pair.equals(pair2))
                                    {   //do any of these tiles match
                                        goodMoves.add(pair);
                                    }

                                }
                            }

                            if(goodMoves.size()>0)
                            {   //otherwise the first good move is fine
                               boolean found = false;

                                for (Pair<Integer,Integer> pair : goodMoves) { //mx =move x

                                    if(!parent.getBoard().getTile(pair).hasUnit()){
                                        int tx = pair.getFirst() - attackerLocation.getTilex();
                                        int ty = pair.getSecond() - attackerLocation.getTiley();
                                        System.out.println(tx + " : " + ty);

                                        if(moveBlockCheck(attackerLocation.getTilex(), attackerLocation.getTiley(), tx, ty))
                                        {
                                            continue;
                                        }

                                        highlightedMoveTileClicked(out, pair.getFirst(), pair.getSecond());
                                        if (!(parent instanceof ExtractedGameState)) {
                                            try {Thread.sleep(3000);} catch (InterruptedException e) {e.printStackTrace();}
                                        }

                                        System.out.println(pair);
                                        attackerLocation =  parent.getBoard().getTile(pair);

                                        enemyHealthAfterAttack = attack(out, attackerLocation, enemyLocation, isRanged);

                                        System.out.println("found new pos using selective method");
                                        found = true;
                                    }


                                }
                                if (!found)
                                {
                                    return;
                                }

                            }
                            else {
                                return;
                            }

                        }

                        /**
                        * Implementation of walk and attack logic for provoke. (Checks before attack)
                        * @author Jun Jie Low (2600104L@student.gla.ac.uk/nelsonlow_88@hotmail.com)
                        */
                        boolean provokerAround = provokeFuncMoveAttackCheck(moveTile.getFirst(), moveTile.getSecond());
                        if(!provokerAround) {
                            // Within attack range
                            System.out.println("no provoker near you" + provokerAround);

                            highlightedMoveTileClicked(out, moveTile.getFirst(), moveTile.getSecond());
                            if (!(parent instanceof ExtractedGameState)) {
                                try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            attackerLocation =  parent.getBoard().getTile(moveTile);

                            enemyHealthAfterAttack = attack(out, attackerLocation, enemyLocation, isRanged);
                        }if(provokerAround){

                            System.out.println("provoker is around");
                            highlightedMoveTileClicked(out, moveTile.getFirst(), moveTile.getSecond());
                            try {Thread.sleep(3000);} catch (InterruptedException e) {e.printStackTrace();}
                            if(parent.getBoard().getTile(x,y).getUnit().getProvoker()){
                                //System.out.println("Icameinhere"); //debug purpose
                                attackerLocation =  parent.getBoard().getTile(moveTile);
                                enemyHealthAfterAttack = attack(out, attackerLocation, enemyLocation, isRanged);
                            }
                        }
                    }
                    else if(!provokeFuncMoveAttackCheck(x, y)){
                        enemyHealthAfterAttack = attack(out, attackerLocation, enemyLocation, isRanged);
                    }
                    parent.memento.add(new GameMemento(parent.getTurn(), ActionType.ATTACK, new AttackInformation(new Pair<>(attacker.getPosition().getTilex(), attacker.getPosition().getTiley()),
                            new Pair<>(x, y), attacker, enemy)));


                    if (enemyHealthAfterAttack > 0) {

                        int counterAttackResult;
                        if (!isRanged) {
                            // Launch Counter Attack
                            counterAttackResult = attack(out, enemyLocation, attackerLocation, isRanged);

                            if (counterAttackResult <= 0) {
                                // Handle unit died of counter attack
                                parent.unitDied(out, attackerLocation, parent.getUnitsPosition(parent.getTurn()));
                            }
                        }
                    } else {
                        parent.unitDied(out, enemyLocation, parent.getEnemyUnitsPosition(parent.getTurn()));
                    }
                 } else {
                    System.out.println("Nonono, you are provoked!");
                 }
              }
            }
            resetAnimations(out);
        } catch(NullPointerException e) {
            System.out.println("Invoked the catch nullexception");
            //Because checking empty tile invokes the .getHealth()
        }
    }

    // @author William T Manson (2604495m@student.gla.ac.uk)
    public boolean moveBlockCheck(int x, int y, int movex, int movey)
    {

        if(Math.abs(movex)==2)
        {
            int mx = movex > 0 ? 1 : -1;
            return parent.getBoard().getTile(x+mx , y).hasUnit();

        }
        else if(Math.abs(movey)==2)
        {
            int my = movey > 0 ? 1 : -1;
            return parent.getBoard().getTile(x , y+ my).hasUnit();
        }
        else if (Math.abs(movex)==1 && Math.abs(movey)==1)
        {
            return (parent.getBoard().getTile(x+(movex > 0 ? 1 : -1), y).hasUnit() && parent.getBoard().getTile(x, y+(movey > 0 ? 1 : -1)).hasUnit());

        }
        else if(Math.abs(movex)==1 || Math.abs(movey)==1)
        {
            return true;
        }
        else{
            return false;
        }
    }

    /**
    * Implementation of the attack method. After all checks above,
    * this method attacks the unit, decrease their health and updates
    * the health on the user interface.
    *
    * This method is used for the actions to be performed after
    * attacking a unit.
    *
    * @author Jun Jie Low (2600104L@student.gla.ac.uk/nelsonlow_88@hotmail.com)
    * @author Anamika Maurya (2570847M@student.gla.ac.uk)
    */
    public int attack(ActorRef out, Tile attackerLocation, Tile enemyLocation, boolean isRanged) {
        Unit attacker = attackerLocation.getUnit();
        Unit enemy = enemyLocation.getUnit();

        UnitCommandBuilder enemyCommandBuilder = new UnitCommandBuilder(out, parent.isSimulation()).setUnit(enemy);

        int enemyHealth = enemy.getHealth();
        int healthAfterDamage = enemyHealth - attacker.getDamage();
        if (healthAfterDamage < 0)
            healthAfterDamage = 0;

        if(isRanged) {
            System.err.println("Ranged attack incoming!");
            new ProjectTileAnimationCommandBuilder(out, parent.isSimulation())
                    .setSource(attackerLocation)
                    .setDistination(enemyLocation)
                    .issueCommand();
        } else {
            System.out.println("Basic attack");
            new UnitCommandBuilder(out, parent.isSimulation()).setUnit(attacker)
                    .setMode(UnitCommandBuilderMode.ANIMATION)
                    .setAnimationType(UnitAnimationType.attack)
                    .issueCommand();
                    if (!(parent instanceof ExtractedGameState)) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
        }

        enemyCommandBuilder
            .setMode(UnitCommandBuilderMode.SET)
            .setStats(UnitStats.HEALTH, healthAfterDamage)
            .issueCommand();

        enemy.setHealth(healthAfterDamage);

        //unhighlight all the tiles
        parent.getHighlighter().clearBoardHighlights(out);

        //restrict human player to attack again
        enemy.addAttacker(attacker);
        moveAttackAndCounterAttack.add(enemy);

        //restrict player to move after attack
        attacker.setHasAttacked();
        moveAttackAndCounterAttack.add(attacker);

        parent.getSpecialAbilities().unitIsDamaged(out, enemy);

        //update avatar health to UI player health.
        if(enemy.isAvatar()) {
            parent.getPlayer(enemy.getPlayerID()).setHealth(enemy.getHealth());
            new PlayerSetCommandsBuilder(out, parent.isSimulation())
                    .setPlayer(enemy.getPlayerID())
                    .setStats(PlayerStats.HEALTH)
                    .setInstance(parent.getPlayer(enemy.getPlayerID()))
                    .issueCommand();
        }

        // Win condition: should be moved to a method where we are checking player's health
        for (Players player: Players.values()) {
            if (parent.getPlayer(player).getHealth() < 1) {
                parent.endGame(out);
                break;
            }
        }
        return enemy.getHealth();
    }

    // @author William T Manson (2604495m@student.gla.ac.uk)
    public boolean attack1RCheck(int x, int y)
    {
        Pair<Integer, Integer> test = new Pair<>(x, y);
        ArrayList<Pair<Integer, Integer>> tiles = get1RAtkTiles(activeUnit.getFirst(), activeUnit.getSecond());
        for (Pair<Integer,Integer> pair : tiles) {
            if(pair.equals(test))
            {
                return true;
            }
        }
        return false;
    }

    /**
    * Implementation of attackCheck method. This method checks
    * the tile clicked to see if it is an available tile to launch attack
    * @author Jun Jie Low (2600104L@student.gla.ac.uk/nelsonlow_88@hotmail.com)
    * @author Anamika Maurya (2570847M@student.gla.ac.uk)
    */
    public boolean attackCheck(int x, int y, Unit attacker) {
        if (activeUnit == null) { return false; }

        int[] acPos = {x, y};
        ArrayList<Pair<Integer, Integer>> tileActive = getAllMoveTiles(activeUnit.getFirst(), activeUnit.getSecond());
        tileActive.addAll(getAllAtkTiles(activeUnit.getFirst(), activeUnit.getSecond()));

        if (parent.getBoard().getTile(x, y).getUnit() != null && parent.getBoard().getTile(x, y).getUnit().hasBeenAttackedBy(attacker))
            return false;

        for (Pair<Integer, Integer> ip: tileActive) {
            System.out.println("ip stuff: "+ip.getFirst() + ip.getSecond());
            if(ip.getFirst()== acPos[0] && ip.getSecond() == acPos[1]) {
                //enemy is in this tile
                return parent.getBoard().getTile(x, y).hasUnit();
            }
        }
        return false;
    }


    ////////////////////////Provoke Method/////////////////////////////////
    /**
    * This method is used to check when tiles are highlighted. It is implemented in all movetile methods.
    * It checks if the unit's tile 1 radius away has any provoker. If a provoker exist, it sets the current player
    * unit to moved = true. This is to prevent the unit to be able to move after provoked.
    * the provoker attack logic is implemented in provokeAttack method, and in launchAttack
    * @author Jun Jie Low (2600104L@student.gla.ac.uk/nelsonlow_88@hotmail.com)
    */
    public void provokeFunc(int x,int y){
        try{
            ArrayList<Pair<Integer, Integer>> tiles = get1RAtkTiles(activeUnit.getFirst(), activeUnit.getSecond());

            Boolean provokerNearYou = false;


            if(parent.getBoard().getTile(x,y).getUnit().getProvoker()){//provoker. Set highlight unit to not able to move.
                for(Pair<Integer, Integer> tPair : tiles){
                    if(parent.getBoard().getTile(tPair).getUnit()!=null && parent.getBoard().getTile(tPair).getUnit().getPlayerID()!=parent.getTurn()){
                    parent.getBoard().getTile(tPair).getUnit().setProvoked(true);
                    //setProvokedMove is a reuse code, just for better illustration on what's going on.
                    parent.getBoard().getTile(tPair).getUnit().setProvokedMove(true); //blocks it from moving. It can only attack.
                   // previousRedTileUnit.add(t);
                    System.out.println(parent.getBoard().getTile(tPair).getUnit().getProvoked() + " nearby unit has been provoked! ");//debug purpose
                    //provokerTile = tiles;
                    provokedUnitForEndReset.add(parent.getBoard().getTile(tPair).getUnit());
                    provokerNearYou=true;
                    }
                }

            }

            if(!parent.getBoard().getTile(x,y).getUnit().getProvoker()){ //if current turn player is not the provoker
                Unit nonProvokerUnit = parent.getBoard().getTile(x,y).getUnit();
                System.out.println("Not provoker unit");
                for(Pair<Integer,Integer> tPair : tiles){
                    if(parent.getBoard().getTile(tPair).getUnit()!=null && parent.getBoard().getTile(tPair).getUnit().getPlayerID()!=parent.getTurn()
                    && parent.getBoard().getTile(tPair).getUnit().getProvoker()){ //tile not empty, not my unit, & is provoker
                    //boolean provokerExist = parent.getHighlighter().getRedTile().get(i).getUnit().getProvoker();
                    //System.out.println("PROVOKER EXIST? : " + provokerExist);
                    Unit provokerUnit = parent.getBoard().getTile(tPair).getUnit();
                    //if(provokerExist){
                        nonProvokerUnit.setProvoked(true);
                        nonProvokerUnit.setProvokedMove(true); //blocks it from moving. It can only attack.
                        nonProvokerUnit.setUnitProvoked(provokerUnit);
                        System.out.println(parent.getBoard().getTile(x,y).getUnit().getProvoked());//debug purpose

                        provokedUnitForEndReset.add(nonProvokerUnit);
                        provokerNearYou=true;
                    //}

                    }
                }
            }

            if(parent.getBoard().getTile(x,y).getUnit().getProvoked() && !provokerNearYou){ //check if unit can be resetted.
                parent.getBoard().getTile(x,y).getUnit().setProvoked(false);
                parent.getBoard().getTile(x,y).getUnit().setProvokedMove(false);
                System.out.println(parent.getBoard().getTile(x,y).getUnit().getProvoked() + " Should be false here now.");

            }
            }catch(NullPointerException |ArrayIndexOutOfBoundsException e){
                System.out.println("Caught something!"); //change to "" if needed

            }

    }

    /**
    * Works along with launchAttack. After checking, it calls this method
    * to attack and counter attack.
    *
    * This method is used to launch counter attack.
    * @author Jun Jie Low (2600104L@student.gla.ac.uk/nelsonlow_88@hotmail.com)
    * @author Anamika Maurya (2570847M@student.gla.ac.uk)
    */
    private void provokeAttack(ActorRef out, Tile attackerLocation, Tile enemyLocation, boolean isRanged) {
        Unit enemy = enemyLocation.getUnit();
        Unit attacker = attackerLocation.getUnit();
        int enemyHealthAfterAttack = attack(out, attackerLocation, enemyLocation, isRanged);
        System.out.println("Attacking provoking unit");

        if (enemyHealthAfterAttack > 0) {
            // Launch Counter Attack
            int counterAttackResult = attack(out, enemyLocation, attackerLocation, isRanged);

            if (counterAttackResult <= 0) {
                parent.unitDied(out, attackerLocation, parent.getUnitsPosition(parent.getTurn()));
            }
        } else {
            parent.unitDied(out, enemyLocation, parent.getEnemyUnitsPosition(parent.getTurn()));
        }
    }

    /**
    * This method is called during move&attack. It checks the tile that the unit will land before
    * the attack. Then, it checks that specific tile 1 radius away to see if there is any provoker.
    * If there is not provoker, unit will be able to move and attack. Else, move & attack only applies
    * to the provoker unit. If attempt of walk and attack an enemy unit with enemy provoker 1 tile away,
    * it will walk, stop at the landing tile checked earlier without attack.
    * @author Jun Jie Low (2600104L@student.gla.ac.uk/nelsonlow_88@hotmail.com)
    */
    public boolean provokeFuncMoveAttackCheck(int x,int y){
        try{
           ArrayList<Pair<Integer, Integer>> tiles = get1RAtkTiles(x, y);
           // System.out.println(realX + " and " + realY + "Debug for check provokeMovetile");
            boolean provokerNearby = false;


            if(!parent.getBoard().getTile(activeUnit.getFirst(),activeUnit.getSecond()).getUnit().getProvoker()){ //if current turn player is not the provoker
                Unit nonProvokerUnit = parent.getBoard().getTile(activeUnit.getFirst(),activeUnit.getSecond()).getUnit();
                System.out.println("Not provoker unit");
                for(Pair<Integer,Integer> tPair : tiles){
                    if(parent.getBoard().getTile(tPair).getUnit()!=null && parent.getBoard().getTile(tPair).getUnit().getPlayerID()!=parent.getTurn()
                    && parent.getBoard().getTile(tPair).getUnit().getProvoker()){ //tile not empty, not my unit, & is provoker

                    Unit provokerUnit = parent.getBoard().getTile(tPair).getUnit();
                    //if(provokerExist){
                        nonProvokerUnit.setProvoked(true);
                        //nonProvokerUnit.setProvokedMove(true); //blocks it from moving. It can only attack.
                        nonProvokerUnit.setUnitProvoked(provokerUnit);
                        System.out.println(parent.getBoard().getTile(activeUnit.getFirst(),activeUnit.getSecond()).getUnit().getProvoked());//debug purpose
                        //provokerTile = tiles;
                        System.out.println(tiles + " Debug"); //checked.
                        provokerNearby = true;
                        return true; //for check provoke while move
                    //}

                    }
                }
            }

            if(parent.getBoard().getTile(activeUnit.getFirst(),activeUnit.getSecond()).getUnit().getProvoked() && !provokerNearby){ //check if unit can be resetted.
                parent.getBoard().getTile(x,y).getUnit().setProvoked(false);
                parent.getBoard().getTile(x,y).getUnit().setProvokedMove(false);
                System.out.println(parent.getBoard().getTile(x,y).getUnit().getProvoked() + " Should be false here now.");

            }return false;

            }catch(NullPointerException |ArrayIndexOutOfBoundsException e){
                System.out.println("caught something!"); //change to "" if needed

            }
            return false;
    }


    // ===========================================================================
    // Setters, getters, and resetters
    // ===========================================================================

    /**
     * This method is used to reset the units to idle after attack
     * and counter attack.
     * @author Anamika Maurya (2570847M@student.gla.ac.uk)
     * @author Yu-Sung Hsu (2540296h@student.gla.ac.uk)
     */
    public void resetMoveAttackAndCounterAttack(ActorRef out) {
        for (Unit unit: moveAttackAndCounterAttack) {
            unit.setHasMoved(false);
            unit.resetAttackCount();
            unit.clearAttackers();
            System.out.println("Resetting");

            if (!(parent instanceof ExtractedGameState)) {
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            new UnitCommandBuilder(out, parent.isSimulation())
                .setUnit(unit)
                .setMode(UnitCommandBuilderMode.ANIMATION)
                .setAnimationType(UnitAnimationType.idle)
                .issueCommand();
        }
        //////////////////////////////////////////////////////////////
         /**
         * Included this to reset the provoked unit each turn.
         * @author Jun Jie Low (2600104L@student.gla.ac.uk/nelsonlow_88@hotmail.com)
         */
        //////////////////////////////////////////////////////////////
        for(Unit unit : provokedUnitForEndReset){
            unit.setHasMoved(false);
        }
        moveAttackAndCounterAttack.clear();
    }

    public void resetAnimations(ActorRef out) {
        for (Unit unit: moveAttackAndCounterAttack) {
            new UnitCommandBuilder(out, parent.isSimulation())
                    .setUnit(unit)
                    .setMode(UnitCommandBuilderMode.ANIMATION)
                    .setAnimationType(UnitAnimationType.idle)
                    .issueCommand();
        }
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
