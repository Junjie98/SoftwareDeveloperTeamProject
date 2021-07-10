package structures.AI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.util.ElementScanner14;

import akka.actor.ActorRef;

import commandbuilders.enums.Players;
import commandbuilders.enums.States;
import commandbuilders.enums.TileEffectAnimation;
import structures.GameState;
import structures.basic.Card;
import structures.basic.Unit;
import structures.extractor.ExtractedGameState;
import structures.handlers.Pair;
import structures.Board;
import structures.SmartBoy;
import structures.memento.*;

public class AI 
{
    //static ArrayList<Pair<Integer, Integer>> friendlies = null;
    //static Pair<Integer, Integer> enemyTarget= null;
    //Pair<Integer,Integer> targPos = null;
    //static int enemyXRange= 0;
    static int moveIndex = 0;
    static boolean attack = false;
    int mana = 0;

    public AI()
    {

    }

    public void TakeTurn(ActorRef out, GameState gs)
    {
        //think method that runs sim returns list of actions with info in order for this turn
        //act on the methods return info

        
        System.out.println("AI Started");
        findBestMoves(out, gs);
        //moveInit(out, gs);
        //castInit(out, gs);
    }

    
    public void findBestMoves(ActorRef out, GameState gameState)
    {
        //Get simstate from gameState, this will act as the root state.
        //Find all possible moves to be made initally in the root state
        //Vary these in some ways like MOVE->ATTACK->SUMMON, ATTACK->SUMMON->MOVE etc,
        //set these actions in the resulting states
        //Use the top 3 resulting states as root state for the opposing player 
        //find the lowest 3 of these resulting states and repeat
        //...
        //Of all final states evaulate and find the best result for us,
        //trace the info path back to find the inital action from the root state.
        //use this as the optimum action, return as list of game mementos

        ArrayList<AiNode> nodes = new ArrayList<>();
        SmartBoy sBoy = new SmartBoy(gameState);

        ExtractedGameState rootState = sBoy.getExtractedGameState();
        int depth = 0;
        nodes.add(new AiNode(null, rootState, depth));

        generateMoves(out, nodes, depth, 5);
        System.out.println("number of nodes created: "+nodes.size());

    }

    public void generateMoves(ActorRef out, ArrayList<AiNode> nodes, int depth, int limit)
    {
        if(depth>=limit)
        {
            return;
        }
        for (AiNode aiNode : nodes){
            if(aiNode.depth == depth){
                generateMovesForNode(out, nodes, aiNode, depth);
            }
        }
        generateMoves(out, nodes, ++depth, limit);
    }

    public void generateMovesForNode(ActorRef out, ArrayList<AiNode> nodes, AiNode aiNode, int depth)
    {

        //set depth for this iteration
        int ldepth = ++depth;
        //set the gamestate in the smartboy
        SmartBoy temp_sBoy = new SmartBoy(aiNode.gameState);


        //Right well then, lets go through a list of nodes, creating new nodes for each action that can be performed
        //split this into attacking casting summoning and moving
        
        //Attack Logic
        //get attack state for manipulation
        ExtractedGameState atkState = temp_sBoy.getExtractedGameState();
        atkState.setSimulation(true);

        ArrayList<Pair<Pair<Integer,Integer>,Pair<Integer,Integer>>> attackers = atkState.canStillAttack();
        //do a check to see if there are any valid attacks that can be actioned.
        if(attackers.size()>0){
            //if so then action all these attacks and collect the data
            ArrayList<GameMemento> attacks = new ArrayList<>();
            for (Pair<Pair<Integer,Integer>,Pair<Integer,Integer>> attackMove : attackers) {
                Pair<Integer,Integer> attacker = attackMove.getFirst();
                Pair<Integer,Integer> attackee = attackMove.getSecond();
                atkState.tileClicked(out, attacker.getFirst(), attacker.getSecond());
                atkState.tileClicked(out, attackee.getFirst(), attackee.getSecond());
                
                AttackInformation atkInfo = new AttackInformation(attacker, attackee, atkState.getBoard().getTile(attacker).getUnit(),atkState.getBoard().getTile(attackee).getUnit());
                attacks.add(new GameMemento(atkState.getTurn(), ActionType.ATTACK, atkInfo));
            }           
            

            //once the data is collected and the gamestate has been actioned 
            //we update the state with a record of the actions performed
            atkState.memento.addAll(attacks);

            //we then create a new node that has the new state in it
            //as well as recording the parent node and the local depth per iteration
            Map<ArrayList<GameMemento>,AiNode> output = new HashMap<>();
            AiNode newNode = new AiNode(aiNode, atkState, ldepth);
            //this is then added to an output list, which is concatenated with the 
            //current parent child map this is the mechanism that gives us the tree structure
            output.put(attacks, newNode);
            aiNode.updateMap(output);
            nodes.add(newNode);

        }
        else{
            //there were no attackers available
            System.out.println("No attackers available AI depth: " + ldepth);
        }

        
        //Move Logic
        ExtractedGameState moveState = temp_sBoy.getExtractedGameState();
        moveState.setSimulation(true);
        ArrayList<Pair<Integer,Integer>> movers = moveState.canStillMove();

        if(movers.size()>0){
            Pair<Integer,Integer> target = findEnemyTarget(moveState);
            ArrayList<GameMemento> moves = new ArrayList<>();

            for (Pair<Integer,Integer> unitPos : movers) {
                Unit tempUnit = moveState.getBoard().getInstance().getTile(unitPos).getUnit();
                Pair<Integer, Integer> targPos = findGoodMove(moveState, unitPos, target);
                if(targPos != null)
                {
                    moveState.tileClicked(out, unitPos.getFirst(), unitPos.getSecond());
                    moveState.tileClicked(out, targPos.getFirst(), targPos.getSecond());
                    
                    MovementInformation moveInfo = new MovementInformation(moveState.getBoard().getTile(unitPos).getUnit(), unitPos, targPos);
                    moves.add(new GameMemento(moveState.getTurn(), ActionType.MOVE, moveInfo));
                }

            }
            moveState.memento.addAll(moves);
            Map<ArrayList<GameMemento>,AiNode> output = new HashMap<>();
            AiNode newNode = new AiNode(aiNode, moveState, ldepth);
            output.put(moves, newNode);
            aiNode.updateMap(output);
            nodes.add(newNode);

        }
        else{
            System.out.println("no movers available AI depth: " + ldepth);
        }


        //Summon Logic
        ExtractedGameState summonState = temp_sBoy.getExtractedGameState();
        summonState.setSimulation(true);
        ArrayList<Pair<Integer, Card>> summons = summonState.canStillSummon();

        if(summons.size()>0){
            Pair<Integer,Integer> target = findEnemyTarget(summonState);
            ArrayList<GameMemento> summonActions = new ArrayList<>();

            int indexOfHigh = 0;
            int highCost = 0;
            if(summons.size()>1)
            {   //lets prioritise summons and do them first since game pieces are more efficient than spells
                for(Pair<Integer, Card> info : summons){
                    if(info.getSecond().getManacost()> highCost)
                    {   //lets find the index of the highest cost card for efficiency 
                        highCost = info.getSecond().getManacost();
                        indexOfHigh = info.getFirst();
                    }
                }
            }
            else{
                //otherwise any card will do if they are the same or only one
                indexOfHigh = 0;
            }

            ArrayList<Pair<Integer, Integer>> summonTiles = getSummonTiles(summonState);
            ArrayList<Pair<Integer, Integer>> goodSTiles = new ArrayList<>();
            ArrayList<Card> hand = new ArrayList<>();
            if(summonTiles.size()>0)
            {   //if there are spaces to summon
                int range;
                if(summonState.getTurn()==Players.PLAYER2)
                {
                    hand = summonState.player2CardsInHand;
                    range = target.getFirst()+3;
                    //check to see if they are out of range of the enemy front line
                    for (Pair<Integer,Integer> sTile : summonTiles) {
                        if(sTile.getFirst() > range)
                        {
                            goodSTiles.add(sTile);
                        }
                    }
                }
                else{
                    hand = summonState.player1CardsInHand;

                    range = target.getFirst()-3;
                    //check to see if they are out of range of the enemy front line
                    for (Pair<Integer,Integer> sTile : summonTiles) {
                        if(sTile.getFirst() < range)
                        {
                            goodSTiles.add(sTile);
                        }
                    }
                }
                if(goodSTiles.size()> 0)
                {   //if there are good tiles out of range then use any one
                    summonState.cardClicked(out, indexOfHigh);
                    target = goodSTiles.get(0);
                    summonState.tileClicked(out, goodSTiles.get(0).getFirst(), goodSTiles.get(0).getSecond());
        
                }
                else{
                    //otherwise we just use one of the basic summoning tiles (might be getting pressured)
                    summonState.cardClicked(out, indexOfHigh);
                    target = goodSTiles.get(0);
                    summonState.tileClicked(out, summonTiles.get(0).getFirst(), summonTiles.get(0).getSecond());
                }
          
            }
            else{
                System.out.println("nowhere to summon");
            }


            //Make the new memento and 
            SummonInformation sumInfo = new SummonInformation(target, summonState.getBoard().getTile(target).getUnit());
            summonActions.add(new GameMemento(summonState.getTurn(), ActionType.SUMMON, sumInfo));

            summonState.memento.addAll(summonActions);
            Map<ArrayList<GameMemento>,AiNode> output = new HashMap<>();
            AiNode newNode = new AiNode(aiNode, summonState, ldepth);
            output.put(summonActions, newNode);
            aiNode.updateMap(output);
            nodes.add(newNode);

        }
        else{
            System.out.println("no summons available AI depth: " + ldepth);
        }


        
        //Cast Logic
        ExtractedGameState castState = temp_sBoy.getExtractedGameState();
        castState.setSimulation(true);
        ArrayList<Pair<Integer, Card>> casts = castState.canStillCast();

        if(casts.size()>0){
            ArrayList<GameMemento> castActions = new ArrayList<>();

            Card cardSelected = new Card();
            int indexOfHighManaCard = 0;
            int highCost = 0;
            if(casts.size()>1)
            {   
                for(Pair<Integer, Card> info : casts){
                    if(info.getSecond().getManacost()> highCost)
                    {   //lets find the index of the highest cost card for efficiency 
                        highCost = info.getSecond().getManacost();
                        indexOfHighManaCard = info.getFirst();
                        cardSelected=info.getSecond();
                    }
                }
            }
            else{
                //otherwise any card will do if they are the same or only one
                indexOfHighManaCard = 0;
            }

            Pair<Integer,Integer> target= new Pair<Integer,Integer>(0,0);
            String cardName = "";
            cardName =cardSelected.getCardname();
            if(cardName.equals("Staff of Y'Kir"))
            {
                //+2 attack to avatar
                Pair<Integer,Integer> avatarPos = castState.getMyAvatarPosition();
                castState.cardClicked(out, indexOfHighManaCard);
                target = avatarPos;
                castState.tileClicked(out, avatarPos.getFirst(), avatarPos.getSecond());

            }
            else if(cardName.equals("Entropic Decay"))
            {
                //reduce any unit bar avatar to 0HP
                ArrayList<Pair<Integer,Integer>> enemies = castState.getAllEnemiesBarAvatar();
                int maxHealth = 0;
                Pair<Integer,Integer> posOfMax  = new Pair<Integer,Integer>(0,0);
                if(!enemies.isEmpty())
                {
                    for(int i = 0; i < enemies.size(); i++)
                    {
                        int tempHealth =castState.getBoard().getTile(enemies.get(i)).getUnit().getHealth();
                        if(tempHealth>maxHealth)
                        {
                            maxHealth=tempHealth;
                            posOfMax=enemies.get(i);
                        }
                    }
                    
                    castState.cardClicked(out, indexOfHighManaCard);
                    target = posOfMax;
                    castState.tileClicked(out, posOfMax.getFirst(), posOfMax.getSecond());
                }
            }
            else if(cardName.equals("Truestrike"))
            {
                //deal 2 dmg to any target
                ArrayList<Pair<Integer,Integer>> enemies = castState.getTurn()==Players.PLAYER2? castState.player1UnitsPosition:castState.player2UnitsPosition;
                int highestHealth = 0;
                Pair<Integer,Integer> lowTarg = null;
                Pair<Integer,Integer> hiTarg = null;
                if(!enemies.isEmpty()){
                    for (int i = 0; i < enemies.size(); i++) {
                        if(castState.getBoard().getTile(enemies.get(i)).getUnit().getHealth() < 3){
                            lowTarg = enemies.get(i);
                        }
                        //Maybe add in a high check so that if this fails we damage the highest health thing
                        if(castState.getBoard().getTile(enemies.get(i)).getUnit().getHealth()>highestHealth){
                            highestHealth= castState.getBoard().getTile(enemies.get(i)).getUnit().getHealth();
                            hiTarg=enemies.get(i);
                        }
                    }
                    if(lowTarg==null){
                        target=hiTarg;
                    }
                    else{
                        target=lowTarg;
                    }
                }
                castState.cardClicked(out, indexOfHighManaCard);
                castState.tileClicked(out, target.getFirst(), target.getSecond());
            }
            else if(cardName.equals("Sundrop Elixir"))
            {
                //add 5 health to a unit
                ArrayList<Pair<Integer,Integer>> friendlies = castState.getTurn()==Players.PLAYER2? castState.player2UnitsPosition:castState.player1UnitsPosition;
                int lowHealth = 20;
                for (Pair<Integer,Integer> unit : friendlies) {
                    if(castState.getBoard().getTile(unit).getUnit().getHealth()<lowHealth)
                    {
                        lowHealth = castState.getBoard().getTile(unit).getUnit().getHealth();
                        target=unit;
                    }
                }
                castState.cardClicked(out, indexOfHighManaCard);
                castState.tileClicked(out, target.getFirst(), target.getSecond());
            }
            else{
                System.out.println("yikes trying to use a spellcard that isnt a spellcard");
            }




            //Make the new memento and add it to the new node
            SpellInformation sumInfo = new SpellInformation(castState.getBoard().getTile(target).getUnit(),target,cardSelected );
            castActions.add(new GameMemento(castState.getTurn(), ActionType.MOVE, sumInfo));

            castState.memento.addAll(castActions);
            Map<ArrayList<GameMemento>,AiNode> output = new HashMap<>();
            AiNode newNode = new AiNode(aiNode, castState, ldepth);
            output.put(castActions, newNode);
            aiNode.updateMap(output);
            nodes.add(newNode);

        }
        else{
            System.out.println("no summons available AI depth: " + ldepth);
        }


    }



    // ===========================================================================
    // Control Methods
    // ===========================================================================


    //Part of the AI control "interface"
    public void AI_SummonUnit(ActorRef out, GameState gs, int cardIndex, Pair<Integer, Integer> pos)
    {
        gs.cardClicked(out, cardIndex);
        try {Thread.sleep(2500);} catch (InterruptedException e) {e.printStackTrace();}
        gs.tileClicked(out, pos.getFirst(), pos.getSecond());
        try {Thread.sleep(2500);} catch (InterruptedException e) {e.printStackTrace();}
        moreUnitsToMoveAtk(out, gs);

    }
    public void AI_MoveUnit(ActorRef out, GameState gs, Pair<Integer, Integer> friendlyPosition, Pair<Integer, Integer> moveToPosition)
    {
        gs.tileClicked(out, friendlyPosition.getFirst(), friendlyPosition.getSecond());
        try {Thread.sleep(2500);} catch (InterruptedException e) {e.printStackTrace();}
        gs.tileClicked(out, moveToPosition.getFirst(), moveToPosition.getSecond());
        try {Thread.sleep(2500);} catch (InterruptedException e) {e.printStackTrace();}
    }
    public void AI_AtkUnit(ActorRef out, GameState gs, Pair<Integer, Integer> friendlyPosition, Pair<Integer, Integer> enemyPosition)
    {
        gs.tileClicked(out, friendlyPosition.getFirst(), friendlyPosition.getSecond());
        try {Thread.sleep(2500);} catch (InterruptedException e) {e.printStackTrace();}
        gs.tileClicked(out, enemyPosition.getFirst(), enemyPosition.getSecond());
        try {Thread.sleep(2500);} catch (InterruptedException e) {e.printStackTrace();}
        moreUnitsToMoveAtk(out, gs);
    }

    public void moreUnitsToMoveAtk(ActorRef out, GameState gs)
    {
        if(moveIndex < (gs.getTurn()==Players.PLAYER2? gs.player2UnitsPosition:gs.player1UnitsPosition).size())
        {
            System.out.print("more moving to happen");
            //move(out, gs);
        }
    }

    // ===========================================================================
    // Cast Methods
    // ===========================================================================

    public void castInit(ActorRef out, GameState gs)
    {
        HashMap<Card,Integer> summonCards = new HashMap<Card,Integer>();
        HashMap<Card,Integer> spellCards = new HashMap<Card,Integer>();

        //check mana
        //check spells
        //prioritise and cast
        mana = gs.getPlayer(Players.PLAYER2).getMana();
        System.out.println("player 2 mana: " + mana);

        if(gs.player2CardsInHand.size()==0){
            return;
        }
        for (int i = 0; i < gs.player2CardsInHand.size(); i++) {
            if(gs.player2CardsInHand.get(i).getManacost() <= mana){
                System.out.println("manacost: " + gs.player2CardsInHand.get(i).getManacost());
                //for each castable spell this round split into summons and spells
                if(gs.player2CardsInHand.get(i).isSpell()){
                    spellCards.put(gs.player2CardsInHand.get(i),i);
                    System.out.println("spell card");
                }
                else{
                    summonCards.put(gs.player2CardsInHand.get(i),i);
                    System.out.println("unit card");

                }
            }
        }
        int indexOfHigh = 0;
        int highCost = 0;
        Pair<Integer,Integer> targ = null;
        if(summonCards.size()>0)
        {   //lets prioritise summons and do them first since game pieces are more efficient than spells
            for(Card key : summonCards.keySet()){
                if(key.getManacost()>highCost){
                    highCost = key.getManacost();
                    indexOfHigh = summonCards.get(key).intValue();
                }
            }

            ArrayList<Pair<Integer,Integer>> tiles = new ArrayList<>();

            for(int i =0; i < gs.player2UnitsPosition.size(); i++){
                Pair<Integer,Integer> pos = gs.player2UnitsPosition.get(i);
                tiles.addAll(gs.getUnitMovementAndAttack().get1RAtkTiles(pos.getFirst(), pos.getSecond()));
            }

            for (Pair<Integer,Integer> pair : tiles) {
                if(!Board.getInstance().getTile(pair).hasUnit()){
                    targ = pair;
                }
            }


            AI_SummonUnit(out, gs, indexOfHigh, targ);
        }
 


 
    }


    
    public ArrayList<Pair<Integer,Integer>> getSummonTiles(GameState gs)
    {
        ArrayList<Pair<Integer,Integer>> tiles = new ArrayList<>();

        ArrayList<Pair<Integer,Integer>> friendlies = gs.getTurn()==Players.PLAYER2?gs.player2UnitsPosition:gs.player1UnitsPosition;
        for(int i =0; i < friendlies.size(); i++){
 
            Pair<Integer,Integer> pos = friendlies.get(i);
            tiles.addAll(gs.getUnitMovementAndAttack().get1RAtkTiles(pos.getFirst(), pos.getSecond()));
   
        }

        for (Pair<Integer,Integer> friend : friendlies) {
            if(tiles.contains(friend)){
                tiles.remove(friend);
            }
        }
        return tiles;
    }

    // ===========================================================================
    // Move Methods
    // ===========================================================================

    public void moveInit(ActorRef out, GameState gs)
    {
       // friendlies = gs.player2UnitsPosition;
        //enemyTarget = findEnemyTarget(gs);
        //enemyXRange = enemyTarget.getFirst() + (gs.getTurn() == Players.PLAYER2 ? 2 : -2);    //add two for move and attack range
        moveIndex = 0;
        //move(out, gs);

    }


    public Pair<Integer,Integer> findGoodMove(GameState gs,Pair<Integer,Integer> unitPos, Pair<Integer,Integer> enemyTarget)
    {
        
        boolean moved = false;
        Pair<Integer,Integer> targPos = enemyTarget;
        System.out.println("init targ pos: " + targPos);

        int xMove = enemyTarget.getFirst() - unitPos.getFirst();
        int yMove = enemyTarget.getSecond() - unitPos.getSecond();

        //Prioritise vertical movement - safer
        if(Math.abs(yMove) >= 2){
            yMove = yMove > 0 ? 2 : -2;
            xMove = 0;
        }
        else if(Math.abs(xMove) >= 2){
            xMove = xMove > 0 ? 2 : -2;
            yMove = 0;
        }

        if(Board.getInstance().getTile(unitPos).getUnit().isRanged())
        {
            if(gs.getUnitMovementAndAttack().getFlyMoveTiles().contains(targPos)
                && Board.getInstance().getTile(targPos).hasUnit())
            {
                moved = true; 

                moveIndex++;
                System.out.println("Attack from range AI using move-to-attack");
                return targPos;
            }
        }
        else if(gs.getUnitMovementAndAttack().getAllAtkTiles(unitPos.getFirst(), unitPos.getSecond()).contains(targPos)
            && Board.getInstance().getTile(targPos).hasUnit()
            && gs.getUnitMovementAndAttack().moveBlockCheck(unitPos.getFirst(), unitPos.getSecond(),xMove,yMove))
        {   //if we can attack and move then go for it and let that logic take care of itself
            moved = true; 

            moveIndex++;
            System.out.println("Attack from range AI using move-to-attack");
            return targPos;
        }
        
        if(moved ==false)
        {
            //if we cant directly attack it then we need to find a new tile to move to that helps us
                


            if(gs.getUnitMovementAndAttack().moveBlockCheck(unitPos.getFirst(), unitPos.getSecond(), xMove, yMove)){
                if(Math.abs(yMove) >= 2 || Math.abs(xMove) >= 2){
                    if(gs.getUnitMovementAndAttack().moveBlockCheck(unitPos.getFirst(), unitPos.getSecond(), xMove/2, yMove/2)){
                        System.out.println("Tile prior blocked2");
                        return null;
                    }
                    else{
                        targPos = new Pair<>(unitPos.getFirst() + xMove/2, unitPos.getSecond() + yMove/2);
                    }

                }
                else{
                    System.out.println("Tile prior blocked1");

                    return null;
                }

            }
            else{
                targPos = new Pair<>(unitPos.getFirst() + xMove, unitPos.getSecond() + yMove);
                return targPos;
            }
        }
        return null;

    }
    // public void autoMove(ActorRef out, GameState gs)
    // {
    //     if(friendlies == null)
    //     {   //cant move if everything is dead
    //         return;
    //     }

    //     int friendliesNum = friendlies.size();
    //     // for (Pair<Integer,Integer> pair : friendlies) {
    //     //     if(Board.getInstance().getTile(pair).getUnit().getDoubleAttack())
    //     //     {
    //     //         friendliesNum = friendlies.size() + 1;
    //     //     }
    //     // }

    //     if(moveIndex >= friendliesNum)
    //     {   //If we've moved more than we have units big nope
    //         return;
    //     }
        

    //     Pair<Integer,Integer> unitPos = new Pair<>(friendlies.get(0).getFirst(),friendlies.get(0).getSecond());

    //     Unit temp = Board.getInstance().getTile(unitPos).getUnit();

    //     if(temp.getHasAttacked() || temp.getHasMoved())
    //     {
    //         return;
    //     }

    //     if(temp.isFlying()){
    //         //do flying stuff
    //     }

    //     boolean moved = false;
    //     targPos = enemyTarget;
    //     System.out.println("init targ pos: " + targPos);

    //     if(Board.getInstance().getTile(unitPos).getUnit().isRanged())
    //     {
    //         if(gs.getUnitMovementAndAttack().getFlyMoveTiles().contains(targPos)
    //             && Board.getInstance().getTile(targPos).hasUnit())
    //         {
    //             moved = true; 

    //             moveIndex++;
    //             AI_AtkUnit(out, gs, unitPos, targPos);
    //             System.out.println("Attack from range AI using move-to-attack");
    //         }
    //     }
    //     else if(gs.getUnitMovementAndAttack().getAllAtkTiles(unitPos.getFirst(), unitPos.getSecond()).contains(targPos)
    //         && Board.getInstance().getTile(targPos).hasUnit())
    //     {   //if we can attack and move then go for it and let that logic take care of itself
    //         moved = true; 

    //         moveIndex++;
    //         AI_AtkUnit(out, gs, unitPos, targPos);
    //         System.out.println("Attack from range AI using move-to-attack");
    //     }
        
    //     if(moved ==false)
    //     {
    //         //if we cant directly attack it then we need to find a new tile to move to that helps us
                
    //         int xMove = enemyTarget.getFirst() - unitPos.getFirst();
    //         int yMove = enemyTarget.getSecond() - unitPos.getSecond();

    //         //Prioritise vertical movement - safer
    //         if(Math.abs(yMove) >= 2){
    //             yMove = yMove > 0 ? 2 : -2;
    //             xMove = 0;
    //         }
    //         else if(Math.abs(xMove) >= 2){
    //             xMove = xMove > 0 ? 2 : -2;
    //             yMove = 0;
    //         }

    //         if(gs.getUnitMovementAndAttack().moveBlockCheck(unitPos.getFirst(), unitPos.getSecond(), xMove, yMove)){
    //             if(Math.abs(yMove) >= 2 || Math.abs(xMove) >= 2){
    //                 if(gs.getUnitMovementAndAttack().moveBlockCheck(unitPos.getFirst(), unitPos.getSecond(), xMove/2, yMove/2)){
    //                     System.out.println("Tile prior blocked2");
    //                     return;
    //                 }
    //                 else{
    //                     targPos = new Pair<>(unitPos.getFirst() + xMove/2, unitPos.getSecond() + yMove/2);
    //                 }

    //             }
    //             else{
    //                 System.out.println("Tile prior blocked1");

    //                 return;
    //             }

    //         }
    //         else{
    //             targPos = new Pair<>(unitPos.getFirst() + xMove, unitPos.getSecond() + yMove);

    //         }


    //         System.err.println("Tiles selected by ai: ");
    //         System.err.println(unitPos);
    //         System.err.println(targPos);

    //         System.err.println("enemy targ pos: ");
    //         System.err.println(enemyTarget);

    //         moveIndex++;

    //         AI_MoveUnit(out, gs, unitPos, targPos);
    //     }
      
            


    

    // //    if(targPos == enemyTarget)
    // //    {
    // //        //we are attacking
    // //        move(out, gs);
    // //    }
 
    // }


    // // ===========================================================================
    // // Helper Methods
    // // ===========================================================================

    // public void moveCheck(ActorRef out, GameState gs)
    // {
    //     System.err.println("recieve pulse");
    //     if(moveIndex < (gs.getTurn()==Players.PLAYER2? gs.player2UnitsPosition:gs.player1UnitsPosition).size())
    //     {
    //         move(out, gs);
    //     }
    // }

    public static Pair<Integer, Integer> findEnemyTarget( GameState gs)
    {
        ArrayList<Pair<Integer,Integer>> enemyUnits = new ArrayList<>();
        int x;
        Pair<Integer,Integer> enemyTarget= new Pair<Integer,Integer>(0,0);

        if(gs.getTurn()==Players.PLAYER2){
            enemyUnits = gs.player1UnitsPosition;
            x = -1;
            for (Pair<Integer,Integer> pos : enemyUnits) 
            {
                if(x<pos.getFirst())
                {
                    System.err.println(pos.getFirst());
                    x = pos.getFirst();
                    enemyTarget = pos;
                }
            }
        }
        else{
            enemyUnits = gs.player2UnitsPosition;
            x = 9;
            for (Pair<Integer,Integer> pos : enemyUnits) 
            {
                if(x > pos.getFirst())
                {
                    System.err.println(pos.getFirst());
                    x = pos.getFirst();
                    enemyTarget = pos;
                }
            }
        }
        
        return enemyTarget;
    }

}
