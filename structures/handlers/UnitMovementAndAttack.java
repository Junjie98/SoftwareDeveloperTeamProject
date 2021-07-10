package structures.handlers;

import akka.actor.Actor;
import akka.actor.ActorRef;
import commandbuilders.PlayerSetCommandsBuilder;
import commandbuilders.ProjectTileAnimationCommandBuilder;
import commandbuilders.UnitCommandBuilder;
import commandbuilders.enums.*;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.UnitAnimationType;
import structures.memento.ActionType;
import structures.memento.AttackInformation;
import structures.memento.GameMemento;
import structures.memento.MovementInformation;
import java.util.ArrayList;


public class UnitMovementAndAttack {
    Pair<Integer, Integer> activeUnit = null;
    private final GameState parent;

    ArrayList<Unit> moveAttackAndCounterAttack = new ArrayList<>();

    boolean unitsCanMove = true;
    int valueX = 0; int valueY = 0; //for flying and range unit provoke.

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
            if(initDirB[count]) {
                //if the previous one is clear
                parent.getHighlighter().checkTileHighlight(out, sd);                  //check for units then highlight
            }
            count++;
        }

        //for the inter tiles do some logic
        if(initDirB[0] || initDirB[1]) {
            parent.getHighlighter().checkTileHighlight(out, interDir.get(0));
        }
        if(initDirB[1] || initDirB[3]) {
            parent.getHighlighter().checkTileHighlight(out, interDir.get(1));
        }
        if(initDirB[2] || initDirB[0]) {
            parent.getHighlighter().checkTileHighlight(out, interDir.get(2));
        }
        if(initDirB[2] || initDirB[3]) {
            parent.getHighlighter().checkTileHighlight(out, interDir.get(3));
        }


        //basica attack highlight connected to normal movement highlight
        ArrayList<Pair<Integer, Integer>> atkTiles = getAllAtkTiles(x, y);
        

        for (Pair<Integer, Integer> pos : atkTiles) {
            parent.getHighlighter().checkAttackHighlight(out, pos);
        }

        //Checks the tile of length 1.
        provokeFunc(x,y);

    }

    //angry at nelson for amking me name something so ugly >=(
    public void movedButCanAttackHighlight(ActorRef out, int x, int y)
    {
        if(parent.getBoard().getTile(x,y) != null)
        {
            Unit temp = parent.getBoard().getTile(x, y).getUnit();
            if(temp.isRanged()) {
                flyingOrRangedMoveHighlight(out);
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
    public void moveHighlight(ActorRef out, int x, int y) {
        if (parent.getBoard().getTile(x, y) != null) {
            Unit temp = parent.getBoard().getTile(x, y).getUnit();
            if(temp.isFlying() || temp.isRanged()) {
                System.err.println("flyhighlight");
                flyingOrRangedMoveHighlight(out);
                valueX = x; valueY = y;

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
        provokeFunc(valueX,valueY);
        ArrayList<Pair<Integer, Integer>> units = new ArrayList<>();
        units.addAll(parent.player1UnitsPosition);
        units.addAll(parent.player2UnitsPosition);

        for (Pair<Integer, Integer> bl : units) {
            //Blocked tiles
            parent.getHighlighter().checkTileHighlight(out, bl);
        }
        

    }
    
    // To be used to highlight the tiles for units that can be summoned anywhere
    public void summonAnywhereHighlight(ActorRef out) {
	    for (Pair<Integer, Integer> ti : getFlyMoveTiles()) {
	        //available tiles
	        parent.getHighlighter().checkTileHighlight(out, ti);
	    }
    }

    public ArrayList<Pair<Integer, Integer>> getAllMoveTiles(int x, int y) {
        ArrayList<Pair<Integer, Integer>> output = parent.getMoveTiles(x, y, 1, 0);
        output.addAll(parent.getMoveTiles(x, y, 2, 0));
        output.addAll(parent.getMoveTiles(x, y, 1, 1));
        return output;
    }
    public ArrayList<Pair<Integer, Integer>> getAllAtkTiles(int x, int y) {
        ArrayList<Pair<Integer, Integer>> output = parent.getMoveTiles(x, y, 2, 1);
        output.addAll(parent.getMoveTiles(x, y, 2, 2));
        output.addAll(parent.getMoveTiles(x, y, 1, 2));
        output.addAll(parent.getMoveTiles(x, y, 1, 3));
        output.addAll(parent.getMoveTiles(x, y, 3, 0));
        output.addAll(parent.getMoveTiles(x, y, 3, 1));
        return output;
    }
    public ArrayList<Pair<Integer, Integer>> get1RAtkTiles(int x, int y) {
        if (activeUnit == null) { System.exit(1); }
        ArrayList<Pair<Integer, Integer>> output = parent.getMoveTiles(x, y, 1, 0);
        output.addAll(parent.getMoveTiles(x, y, 1, 1));
        return output;
    }
    public Pair<Integer,Integer> getMoveTileForAttack(int Ax, int Ay, int Ex, int Ey)
    {
        //ArrayList<Pair<Integer,Integer>> rangeTiles = get1RAtkTiles(x, y);
        int mx = Ax-Ex;
        int my = Ay-Ey; //my butterfly, suga baby //https://youtu.be/6FEDrU85FLE?t=7

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
            unitsCanMove = false;   // Prevent other units from moving.

            System.out.println("move valid : boolean = " + unitsCanMove); //debug //results found that within enemy tile range, the bool is set to true.

            new UnitCommandBuilder(out, parent.isSimulation())
                    .setMode(UnitCommandBuilderMode.MOVE)
                    .setTilePosition(x, y)
                    .setUnit(activatedTile.getUnit())
                    .issueCommand();

            parent.memento.add(new GameMemento(parent.getTurn(), ActionType.MOVE, new MovementInformation(activatedTile.getUnit(), activeUnit, new Pair<>(x, y))));
            System.out.println(parent.memento.get(parent.memento.size() - 1));

            // Update the units position in the stored position lists.
            ArrayList<Pair<Integer, Integer>> pool = parent.getUnitsPosition(parent.getTurn());
            parent.removeFromPool(pool, activeUnit);
            pool.add(new Pair<>(x, y));


          
            destinationTile.setUnit(activatedTile.getUnit());
            parent.getHighlighter().clearBoardHighlights(out);

            //System.out.println("unit attacking should be flagged: " + activatedTile.getUnit());

            destinationTile.getUnit().setHasMoved(true);
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
            System.out.println("attackLaunched");
            Tile enemyLocation = parent.getBoard().getTile(x, y);
            Tile attackerLocation =  parent.getBoard().getTile(activeUnit);
            Unit enemy = enemyLocation.getUnit();
            Unit attacker = attackerLocation.getUnit();
            
            if(attackCheck(x, y) || attacker.isRanged()) {
                System.err.println("attack check passed");
                boolean isRanged = attacker.isRanged();
                int enemyHealthAfterAttack = enemy.getHealth();

            if(enemy.getProvoker()&&attacker.getProvoked()) {
                provokeAttack(out, attackerLocation, enemyLocation, isRanged);
                return;
            }
            if(!attacker.getProvoked()){
                if(isRanged){
                    //do ranged attack
                }
                else if(!attack1RCheck(x, y)){
                    //if the unit is not within normal attack range 
                    //move to a range then attack
                    Pair<Integer,Integer> moveTile = getMoveTileForAttack(attackerLocation.getTilex(), attackerLocation.getTiley(), x, y);
                    System.out.println("move and attack activated");

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
                                    try {Thread.sleep(3000);} catch (InterruptedException e) {e.printStackTrace();}

                                    System.out.println(pair);
                                    attackerLocation =  parent.getBoard().getTile(pair);
                                    
                                    enemyHealthAfterAttack = attack(out, attackerLocation, enemy, attacker, x, y, isRanged);
                                    
                                    // Attack twice: attack-attack
                                    if(attacker.getType().equals(UnitType.AZURITE_LION) || attacker.getType().equals(UnitType.SERPENTI))
                                    	 enemyHealthAfterAttack = attack(out, attackerLocation, enemy, attacker, x, y, isRanged);
                                    
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
                    else {
                        highlightedMoveTileClicked(out, moveTile.getFirst(), moveTile.getSecond());
                        System.out.println("move tile: " + moveTile);
                        //try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
                        attackerLocation =  parent.getBoard().getTile(moveTile);
                        System.out.println("New location selected");
                        ArrayList<Pair<Integer,Integer>> provokers = new ArrayList<>();
                        ArrayList<Pair<Integer,Integer>> provokerEffectTiles = new ArrayList<>();
                        Pair<Integer,Integer> provokerPos = null;
                        for (Pair<Integer,Integer> pos : parent.getEnemyUnitsPosition(parent.getTurn()))
                        {
                            if(parent.getBoard().getTile(pos).getUnit().getProvoker())
                            {
                                System.out.println("found provoker");
                                provokers.add(pos);
                            }
                        }
                        if(provokers.size()>0)
                        {   ///CRASHES AROUND HERE SOMEWHERE
                            System.out.println("there is more than none provokers");
                            for (Pair<Integer,Integer> provoker : provokers) {
                                provokerEffectTiles.addAll(get1RAtkTiles(provoker.getFirst(), provoker.getSecond()));
                            }
                            System.out.println("Lets check the move tile");
                            if(provokerEffectTiles.contains(moveTile))
                            {   //This tile is no good it is within a provoke range.
                                System.out.println("There has been a provoke mid move-attack!");
                                // for (Pair<Integer,Integer> pos : get1RAtkTiles(moveTile.getFirst(), moveTile.getSecond())) {
                                //     if(parent.getBoard().getTile(pos).getUnit().getProvoker() && parent.getBoard().getTile(pos).getUnit().getPlayerID() != parent.getTurn())
                                //     {
                                //         provokerPos = pos;
                                //     }
                                // }
                                // Unit newTargetUnit = parent.getBoard().getTile(provokerPos).getUnit();
                                // enemyHealthAfterAttack = attack(out, attackerLocation, newTargetUnit, attacker, provokerPos.getFirst(), provokerPos.getSecond(), isRanged);
                        
                                // // Attack twice: move-attack-attack
                                // if(attacker.getType().equals(UnitType.AZURITE_LION) || attacker.getType().equals(UnitType.SERPENTI))
                                //     enemyHealthAfterAttack = attack(out, attackerLocation, newTargetUnit, attacker, provokerPos.getFirst(), provokerPos.getSecond(), isRanged);
                            }
                            else{
                                // enemyHealthAfterAttack = attack(out, attackerLocation, enemy, attacker, x, y, isRanged);
                        
                                // // Attack twice: move-attack-attack
                                // if(attacker.getType().equals(UnitType.AZURITE_LION) || attacker.getType().equals(UnitType.SERPENTI))
                                //     enemyHealthAfterAttack = attack(out, attackerLocation, enemy, attacker, x, y, isRanged);
                            }
                        }
                        else{
                            enemyHealthAfterAttack = attack(out, attackerLocation, enemy, attacker, x, y, isRanged);
                        
                            // Attack twice: move-attack-attack
                            if(attacker.getType().equals(UnitType.AZURITE_LION) || attacker.getType().equals(UnitType.SERPENTI))
                                enemyHealthAfterAttack = attack(out, attackerLocation, enemy, attacker, x, y, isRanged);
                        }
                        
                        
 
                    }
                }
                else{
                	   // Attack twice: attack-attack
                	   if(attacker.getType().equals(UnitType.AZURITE_LION) || attacker.getType().equals(UnitType.SERPENTI))
                		   enemyHealthAfterAttack = attack(out, attackerLocation, enemy, attacker, x, y, isRanged);
                      enemyHealthAfterAttack = attack(out, attackerLocation, enemy, attacker, x, y, isRanged);
                }
                parent.memento.add(new GameMemento(parent.getTurn(), ActionType.ATTACK, new AttackInformation(new Pair<>(attacker.getPosition().getTilex(), attacker.getPosition().getTiley()),
                        new Pair<>(x, y), attacker, enemy)));


                if (enemyHealthAfterAttack > 0) {
                	
                	int counterAttackResult;
                	if (!isRanged) {
	                    // Launch Counter Attack
	                    counterAttackResult = attack(out, enemyLocation, attacker, enemy, attacker.getPosition().getTilex(), attacker.getPosition().getTiley(), isRanged);
	                    
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
        //IS THIS NEEDED HERE? Having this activate per attack means that it always resets our bools instantly
        //resetMoveAttackAndCounterAttack(out);
    }

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

    

    // Ana: Counter attack, including ranged attack
    public int attack(ActorRef out, Tile attackerLocation, Unit enemy, Unit attacker, int x, int y, boolean isRanged) {
        UnitCommandBuilder enemyCommandBuilder = new UnitCommandBuilder(out, parent.isSimulation()).setUnit(enemy);
        int enemyHealth = enemy.getHealth();
        int healthAfterDamage = enemyHealth - attacker.getDamage();
        if (healthAfterDamage < 0)
            healthAfterDamage = 0;

        if(isRanged) {
			System.err.println("Ranged attack incoming!");
			new ProjectTileAnimationCommandBuilder(out, parent.isSimulation())
			.setSource(attackerLocation)
			.setDistination(parent.getBoard().getTile(x, y))
			.issueCommand();
		} else {
            System.out.println("Basic attack");
            new UnitCommandBuilder(out, parent.isSimulation()).setUnit(attacker)
                    .setMode(UnitCommandBuilderMode.ANIMATION)
                    .setAnimationType(UnitAnimationType.attack)
                    .issueCommand();
    		try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
        }

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

        parent.getSpecialEffect().unitIsDamaged(out, enemy);

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

    public boolean attackCheck(int x, int y) {
        if (activeUnit == null) { return false; }

        int[] acPos = {x, y};
        ArrayList<Pair<Integer, Integer>> tileActive = getAllMoveTiles(activeUnit.getFirst(), activeUnit.getSecond());
        tileActive.addAll(getAllAtkTiles(activeUnit.getFirst(), activeUnit.getSecond()));

        //Ana: for counter attack
        if (parent.getBoard().getTile(x, y).getUnit() != null && parent.getBoard().getTile(x, y).getUnit().getHasGotAttacked())
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
    
    public void provokeFunc(int x,int y){
        try{
            ArrayList<Pair<Integer, Integer>> tiles = get1RAtkTiles(activeUnit.getFirst(), activeUnit.getSecond());
            
            // ArrayList<Pair<Integer, Integer>> provokerTile;
            Boolean provokerNearYou = false;
            // for (Pair<Integer,Integer> pair : tiles) {
            //     if(parent.getBoard().getTile(pair).getUnit()!=null && parent.getBoard().getTile(pair).getUnit().getPlayerID()!=parent.getTurn()){ //not my unit
            //     System.out.println(parent.getBoard().getTile(pair).getUnit().getProvoker()+ " HELLLLLLLLLLLO IT IS TRUEEE>>>>><<<<");
            //         if(parent.getBoard().getTile(pair).getUnit().getProvoker()){
            //             provokerTile = tiles;
            //         }
            //     }
            // }//^ Testing or debugging
            
            if(parent.getBoard().getTile(x,y).getUnit().getProvoker()){//provoker. Set highlight unit to not able to move.
                for(Pair<Integer, Integer> tPair : tiles){
                    if(parent.getBoard().getTile(tPair).getUnit()!=null && parent.getBoard().getTile(tPair).getUnit().getPlayerID()!=parent.getTurn()){
                    parent.getBoard().getTile(tPair).getUnit().setProvoked(true);
                    //setProvokedMove is a reuse code, just for better illustration on what's going on.
                    parent.getBoard().getTile(tPair).getUnit().setProvokedMove(true); //blocks it from moving. It can only attack.
                   // previousRedTileUnit.add(t);
                    System.out.println(parent.getBoard().getTile(tPair).getUnit().getProvoked() + " nearby unit has been provoked! ");//debug purpose
                    //provokerTile = tiles;
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
                        //provokerTile = tiles;
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
                System.out.println("boom you fucked up"); //change to "" if needed
    
            }
    
    }

    private void provokeAttack(ActorRef out, Tile attackerLocation, Tile enemyLocation, boolean isRanged) {
        Unit enemy = enemyLocation.getUnit();
        Unit attacker = attackerLocation.getUnit();
        int enemyHealthAfterAttack = attack(out, attackerLocation, enemy, attacker, enemy.getPosition().getTilex(), enemy.getPosition().getTiley(), isRanged);
        System.out.println("Smack provoking thorfinn");

        if (enemyHealthAfterAttack > 0) {
            // Launch Counter Attack
            int counterAttackResult = attack(out, enemyLocation, attacker, enemy, attacker.getPosition().getTilex(), attacker.getPosition().getTiley(), isRanged);

            if (counterAttackResult <= 0) {
                parent.unitDied(out, attackerLocation, parent.getUnitsPosition(parent.getTurn()));
            }
        } else {
            parent.unitDied(out, enemyLocation, parent.getEnemyUnitsPosition(parent.getTurn()));
        }
    }

    // ===========================================================================
    // Setters, getters, and resetters
    // ===========================================================================
    public void resetMoveAttackAndCounterAttack(ActorRef out) {
        for (Unit unit: moveAttackAndCounterAttack) {
            unit.setHasMoved(false);
            unit.setHasAttacked(false);
            unit.setHasGotAttacked(false);
            
            try {Thread.sleep(30);} catch (InterruptedException e) {e.printStackTrace();}
            
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
