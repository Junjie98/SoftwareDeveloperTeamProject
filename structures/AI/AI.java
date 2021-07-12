package structures.AI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


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
    ArrayList<GameMemento> mems = new ArrayList<>();
    int memInd = 0;
    public AI()
    {

    }

    // @author William Manson (26044995M@student.gla.ac.uk)
    //
    //This is where the AI starts, triggered from the end turn button click.
    public void TakeTurn(ActorRef out, GameState gs)
    {
        //think method that runs sim returns list of actions with info in order for this turn
        //act on the methods return info
        mems = new ArrayList<>();
        if(gs.getTurn()==Players.PLAYER2)
        {
            System.out.println("AI Started");
            findBestMoves(out, gs);
            //moveInit(out, gs);
            //castInit(out, gs);
        }

    }

    // @author William Manson (26044995M@student.gla.ac.uk)
    //
    //Overall function that selects the best moves after they have been sorted by goodness score.
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

        int depthLimit = 5;
        generateMoves(out, nodes, depth, depthLimit);
        Collections.sort(nodes);

        System.out.println("First node goodness: " +nodes.get(0).goodness);
        System.out.println("First node scan: " +nodes.get(0).gameState.getBoard().scanForUnits());
        System.out.println("First node pmem: " +nodes.get(0).gameState.parentMemento);
        System.out.println("First node mem: " +nodes.get(0).gameState.memento);
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("second node goodness: " +nodes.get(1).goodness);
        System.out.println("second node mem: " +nodes.get(1).gameState.memento);

        //System.out.println("number of nodes created: "+nodes.size());

        mems= nodes.get(0).gameState.memento;
        
        System.out.println("mems size : " + mems.size());
        //actionAIStack(out, gameState);
        for (GameMemento mem : mems) {
            AI_Action(out, gameState, mem);
        }
        gameState.endTurnClicked(out);
    }


    // @author William Manson (26044995M@student.gla.ac.uk)
    //
    //Possible fix to the async issues where only one unit can move per turn
    public void actionAIStack(ActorRef out, GameState gs)
    {
        // if(memInd < mems.size())
        // {
        //     System.out.println("mem action");
        //     int temp = memInd;
        //     memInd++;
        //     AI_Action(out, gs, mems.get(temp));
        //     try {Thread.sleep(3000);} catch (InterruptedException e) {e.printStackTrace();}
        // }
        // else{
        //     System.out.println("mems done");
        //     gs.endTurnClicked(out);
        // }

    }

    // @author William Manson (26044995M@student.gla.ac.uk)
    //
    //Wrapper function to carry out all the generations per node.
    public void generateMoves(ActorRef out, ArrayList<AiNode> nodes, int depth, int limit)
    {
        System.out.println("AI depth gen at: "+depth);
        System.out.println("number of nodes created: "+nodes.size());

        if(depth>=limit)
        {
            return;
        }
        for (int i = 0; i < nodes.size(); i++){
            AiNode aiNode = nodes.get(i);
            if(aiNode.depth == depth){
                generateMovesForNode(out, nodes, aiNode, depth);
            }

        }
        System.out.println("END:AI depth gen at: "+depth);
        System.out.println("END:number of nodes created: "+nodes.size());

        generateMoves(out, nodes, ++depth, limit);
    }

    // @author William Manson (26044995M@student.gla.ac.uk)
    //
    //This function creates new nodes and movesets for each node created so far, it also passes all information to the gameMementos
    public void generateMovesForNode(ActorRef out, ArrayList<AiNode> nodes, AiNode aiNode, int depth)
    {

        System.out.println("Generating at depth: " + depth);
        //set depth for this iteration
        int ldepth = ++depth;
        //set the gamestate in the smartboy
        //SmartBoy temp_sBoy = new SmartBoy(aiNode.gameState);
        System.out.println( "Scsn gamesstae for units before generation : " + aiNode.gameState.getBoard().scanForUnits());

        //Right well then, lets go through a list of nodes, creating new nodes for each action that can be performed
        //split this into attacking casting summoning and moving
        
        //Attack Logic
        //get attack state for manipulation
        //ExtractedGameState atkState = temp_sBoy.getExtractedGameState();
        ExtractedGameState atkState = aiNode.gameState;
        atkState.setSimulation(true);

        ArrayList<Pair<Pair<Integer,Integer>,Pair<Integer,Integer>>> attackers = atkState.canStillAttack();
        //do a check to see if there are any valid attacks that can be actioned.
        if(attackers.size()>0){
            System.out.println("calculating attack for node at depth: " + depth);

            //if so then action all these attacks and collect the data
            ArrayList<GameMemento> attacks = new ArrayList<>();
            for (Pair<Pair<Integer,Integer>,Pair<Integer,Integer>> attackMove : attackers) {
                Pair<Integer,Integer> attacker = attackMove.getFirst();
                Pair<Integer,Integer> attackee = attackMove.getSecond();
                Unit unitLocator = atkState.getBoard().getTile(attacker).getUnit();
                //System.out.println(atkState.getBoard().getTile(attacker).getUnit().getName());

                atkState.tileClicked(out, attacker.getFirst(), attacker.getSecond());
                atkState.tileClicked(out, attackee.getFirst(), attackee.getSecond());
                System.out.println("~~~~~~~~~~~~AttackUnit location " + atkState.locateUnit(unitLocator));
                
                Pair<Integer,Integer> attackerPosChecked = atkState.locateUnit(unitLocator) ==null? attacker:atkState.locateUnit(unitLocator);

                AttackInformation atkInfo = new AttackInformation(attacker, attackee, atkState.getBoard().getTile(attacker).getUnit(),atkState.getBoard().getTile(attackee).getUnit());
                attacks.add(new GameMemento(atkState.getTurn(), ActionType.ATTACK, atkInfo));
            }           
            

            //once the data is collected and the gamestate has been actioned 
            //we update the state with a record of the actions performed
            if(aiNode.parent != null)
            {
                if(aiNode.parent.parent!= null)
                {
                    atkState.parentMemento.addAll(aiNode.parent.gameState.parentMemento);
                }
                atkState.parentMemento.addAll(aiNode.parent.gameState.memento);
            }
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
        //ExtractedGameState moveState = temp_sBoy.getExtractedGameState();
        ExtractedGameState moveState = aiNode.gameState;
        moveState.setSimulation(true);
        ArrayList<Pair<Integer,Integer>> movers = moveState.canStillMove();

        if(movers.size()>0){
            System.out.println("calculating move for node at depth: " + depth);

            //System.out.println("mover at: " + moveState.getMyAvatarPosition());
            Pair<Integer,Integer> target = findEnemyTarget(moveState);
            System.out.println("enemy at: " + target);
            ArrayList<GameMemento> moves = new ArrayList<>();

            for (Pair<Integer,Integer> unitPos : movers) {
                Unit tempUnit = moveState.getBoard().getInstance().getTile(unitPos).getUnit();
                Pair<Integer, Integer> targPos = findGoodMove(moveState, unitPos, target);
                if(targPos != null)
                {
                    moveState.tileClicked(out, unitPos.getFirst(), unitPos.getSecond());
                    moveState.tileClicked(out, targPos.getFirst(), targPos.getSecond());
                    moveState.getBoard().getTile(targPos).getUnit().setHasMoved(true);
                    MovementInformation moveInfo = new MovementInformation(moveState.getBoard().getTile(targPos).getUnit(), unitPos, targPos);
                    moves.add(new GameMemento(moveState.getTurn(), ActionType.MOVE, moveInfo));
                }

            }
            if(aiNode.parent != null)
            {
                System.out.println("~~~~~~~~adding p mem");
                if(aiNode.parent.parent!= null)
                {
                    System.out.println("~~~~~~~~~~concat p mem");

                    moveState.parentMemento.addAll(aiNode.parent.gameState.parentMemento);
                }
                moveState.parentMemento.addAll(aiNode.parent.gameState.memento);
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


        // System.out.println("mover at: " + moveState.getMyAvatarPosition());
        // Pair<Integer,Integer> ttarget = findEnemyTarget(moveState);
        // System.out.println("enemy at: " + ttarget);
        // ArrayList<GameMemento> moves = new ArrayList<>();




        //Summon Logic
        //ExtractedGameState summonState = temp_sBoy.getExtractedGameState();
        ExtractedGameState summonState = aiNode.gameState;

        summonState.setSimulation(true);
        ArrayList<Pair<Integer, Card>> summons = summonState.canStillSummon();


        if(summons.size()>0){
            System.out.println("first summonable card name: " + summons.get(0).getSecond().getCardname());
            System.out.println("calculating summon for node at depth: " + depth);
            Pair<Integer,Integer> target = findEnemyTarget(summonState);
            ArrayList<GameMemento> summonActions = new ArrayList<>();

            int indexOfHigh = 0;
            int highCost = 0;
            if(summons.size()>1){   //lets prioritise summons and do them first since game pieces are more efficient than spells
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
                indexOfHigh = summons.get(0).getFirst();
            }

            System.out.println("Found High cost card at ind: " + indexOfHigh);
            ArrayList<Pair<Integer, Integer>> summonTiles = getSummonTiles(summonState);
            ArrayList<Pair<Integer, Integer>> goodSTiles = new ArrayList<>();
            ArrayList<Card> hand = new ArrayList<>();
            System.out.println("Found card summon spots");

            if(summonTiles.size()>0){   //if there are spaces to summon
                int range;
                if(summonState.getTurn()==Players.PLAYER2){
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
                System.out.println("Found some good tiles");

                if(goodSTiles.size()> 0){   //if there are good tiles out of range then use any one
                    summonState.cardClicked(out, indexOfHigh);
                    target = goodSTiles.get(0);
                    summonState.tileClicked(out, goodSTiles.get(0).getFirst(), goodSTiles.get(0).getSecond());
        
                }
                else{
                    //otherwise we just use one of the basic summoning tiles (might be getting pressured)
                    summonState.cardClicked(out, indexOfHigh);
                    target = summonTiles.get(0);
                    summonState.tileClicked(out, summonTiles.get(0).getFirst(), summonTiles.get(0).getSecond());
                }
                System.out.println("Summoned!");

            }
            else{
                System.out.println("nowhere to summon");
            }

            System.out.println("making mementos");

            //Make the new memento and 
            SummonInformation sumInfo = new SummonInformation(target, indexOfHigh, summonState.getBoard().getTile(target).getUnit());
            summonActions.add(new GameMemento(summonState.getTurn(), ActionType.SUMMON, sumInfo));

            if(aiNode.parent != null)
            {
                if(aiNode.parent.parent!= null)
                {
                    summonState.parentMemento.addAll(aiNode.parent.gameState.parentMemento);
                }
                summonState.parentMemento.addAll(aiNode.parent.gameState.memento);
            }
            summonState.memento.addAll(summonActions);
            Map<ArrayList<GameMemento>,AiNode> output = new HashMap<>();
            AiNode newNode = new AiNode(aiNode, summonState, ldepth);
            output.put(summonActions, newNode);
            aiNode.updateMap(output);
            nodes.add(newNode);
            System.out.println("done making mementos");

        }
        else{
            System.out.println("no summons available AI depth: " + ldepth);
        }


        
        //Cast Logic
        //ExtractedGameState castState = temp_sBoy.getExtractedGameState();
        ExtractedGameState castState = aiNode.gameState;

        castState.setSimulation(true);
        ArrayList<Pair<Integer, Card>> casts = castState.canStillCast();

        if(casts.size()>0){
            System.out.println("calculating cast for node at depth: " + depth);

            ArrayList<GameMemento> castActions = new ArrayList<>();

            Card cardSelected = new Card();
            int indexOfHighManaCard = 0;
            int highCost = 0;
            if(casts.size()>1){   
                for(Pair<Integer, Card> info : casts){
                    if(info.getSecond().getManacost()> highCost){   //lets find the index of the highest cost card for efficiency 
                        highCost = info.getSecond().getManacost();
                        indexOfHighManaCard = info.getFirst();
                        cardSelected=info.getSecond();
                    }
                }
            }
            else{
                //otherwise any card will do if they are the same or only one
                indexOfHighManaCard = casts.get(0).getFirst();
                cardSelected=casts.get(0).getSecond();

            }

            Pair<Integer,Integer> target= new Pair<Integer,Integer>(0,0);
            String cardName = "";
            cardName =castState.getCardsInHand(castState.getTurn()).get(indexOfHighManaCard).getCardname();
            if(cardName.equals("Staff of Y'Kir'"))
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
            SpellInformation spellInfo = new SpellInformation(indexOfHighManaCard, castState.getBoard().getTile(target).getUnit(),target,cardSelected );
            castActions.add(new GameMemento(castState.getTurn(), ActionType.SPELL, spellInfo));

            if(aiNode.parent != null)
            {
                if(aiNode.parent.parent!= null)
                {
                    castState.parentMemento.addAll(aiNode.parent.gameState.parentMemento);
                }
                castState.parentMemento.addAll(aiNode.parent.gameState.memento);
            }
            castState.memento.addAll(castActions);
            Map<ArrayList<GameMemento>,AiNode> output = new HashMap<>();
            AiNode newNode = new AiNode(aiNode, castState, ldepth);
            output.put(castActions, newNode);
            aiNode.updateMap(output);
            nodes.add(newNode);

        }
        else{
            System.out.println("no casts available AI depth: " + ldepth);
        }

        System.out.println("done generating for node at depth: " + depth);

    }



    // ===========================================================================
    // Control Methods
    // ===========================================================================


    //Part of the AI control "interface"
    // @author William Manson (26044995M@student.gla.ac.uk)
    //
    //Simple set of controls from which to edit controller options
    public void AI_Action(ActorRef out, GameState gs, GameMemento mem)
    {
        System.out.println("[AI ACTION OUT]");
        ActionType type = mem.getActionType();

        switch(type)
        {
            case ATTACK:
                AttackInformation aInfo =(AttackInformation) mem.getInformation();
                AI_AtkUnit(out, gs, aInfo.getSource(), aInfo.getTarget());
                break;

            case MOVE:
                MovementInformation mInfo =(MovementInformation) mem.getInformation();
                AI_MoveUnit(out, gs, mInfo.getSource(), mInfo.getTarget());
                break;

            case SUMMON:
                SummonInformation sInfo =(SummonInformation) mem.getInformation();
                AI_SummonUnit(out, gs, sInfo.getIndexInHand(), sInfo.getTarget());
                break;

            case SPELL:
                SpellInformation cinfo =(SpellInformation) mem.getInformation();
                AI_CastSpell(out, gs, cinfo.getIndexInHand(), cinfo.getTarget());
                break;
            
            
            default:
                System.err.println("What happened here? Memento fail from AI_Action Switch");
                break;
        }
    }
    public void AI_SummonUnit(ActorRef out, GameState gs, int cardIndex, Pair<Integer, Integer> pos)
    {
        gs.cardClicked(out, cardIndex);
        try {Thread.sleep(3000);} catch (InterruptedException e) {e.printStackTrace();}
        gs.tileClicked(out, pos.getFirst(), pos.getSecond());
        try {Thread.sleep(3000);} catch (InterruptedException e) {e.printStackTrace();}
        moreUnitsToMoveAtk(out, gs);

    }
    public void AI_MoveUnit(ActorRef out, GameState gs, Pair<Integer, Integer> friendlyPosition, Pair<Integer, Integer> moveToPosition)
    {
        gs.tileClicked(out, friendlyPosition.getFirst(), friendlyPosition.getSecond());
        try {Thread.sleep(3000);} catch (InterruptedException e) {e.printStackTrace();}
        gs.tileClicked(out, moveToPosition.getFirst(), moveToPosition.getSecond());
        try {Thread.sleep(3000);} catch (InterruptedException e) {e.printStackTrace();}
    }
    public void AI_AtkUnit(ActorRef out, GameState gs, Pair<Integer, Integer> friendlyPosition, Pair<Integer, Integer> enemyPosition)
    {
        gs.tileClicked(out, friendlyPosition.getFirst(), friendlyPosition.getSecond());
        try {Thread.sleep(3000);} catch (InterruptedException e) {e.printStackTrace();}
        gs.tileClicked(out, enemyPosition.getFirst(), enemyPosition.getSecond());
        try {Thread.sleep(3000);} catch (InterruptedException e) {e.printStackTrace();}
        moreUnitsToMoveAtk(out, gs);
    }

    public void AI_CastSpell(ActorRef out, GameState gs, int handInd, Pair<Integer,Integer> target)
    {
        gs.cardClicked(out, handInd);
        try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
        gs.tileClicked(out, target.getFirst(), target.getSecond());
        try {Thread.sleep(1500);} catch (InterruptedException e) {e.printStackTrace();}
        moreUnitsToMoveAtk(out, gs);
    }
    public void moreUnitsToMoveAtk(ActorRef out, GameState gs)
    {
        if(moveIndex < (gs.getTurn()==Players.PLAYER2? gs.player2UnitsPosition:gs.player1UnitsPosition).size())
        {
            System.out.print("more moving to happen");
        }
    }

    // @author William Manson (26044995M@student.gla.ac.uk)
    //
    //Gets all the tiles that could be used to summon a unit, leans towards diagonals at the avatar        
    public ArrayList<Pair<Integer,Integer>> getSummonTiles(ExtractedGameState gs)
    {
        System.out.println("AI: getting summon tiles");
        ArrayList<Pair<Integer,Integer>> tiles = new ArrayList<>();
        ArrayList<Pair<Integer,Integer>> avTiles = new ArrayList<>();
        System.out.println("AI: about to get all friendlies");

        ArrayList<Pair<Integer,Integer>> friendlies = gs.getUnitsPosition(gs.getTurn());
        System.out.println("AI: got all friendlies");

        tiles.addAll(gs.getUnitMovementAndAttack().get1RAtkTiles(gs.getMyAvatarPosition().getFirst(),gs.getMyAvatarPosition().getSecond()));

        for(int i =0; i < friendlies.size(); i++){
            System.out.println("AI: got a friend");

            Pair<Integer,Integer> pos = friendlies.get(i);
            tiles.addAll(gs.getUnitMovementAndAttack().get1RAtkTiles(pos.getFirst(), pos.getSecond()));
   
        }
        System.out.println("AI: found all summon tiles");


        for(Iterator<Pair<Integer,Integer>> tile = tiles.iterator(); tile.hasNext();){            
            if(gs.getBoard().getTile(tile.next()).hasUnit()){
                tile.remove(); // right call
            }
        }
        System.out.println("AI: removed all the friendlies from summon tiles");

        return tiles;
    }

    // @author William Manson (26044995M@student.gla.ac.uk)
    //
    //Finds a good move in case of being blocked
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

        if(gs.getBoard().getTile(unitPos).getUnit().isRanged())
        {
            if(gs.getUnitMovementAndAttack().getFlyMoveTiles().contains(targPos)
                && gs.getBoard().getTile(targPos).hasUnit())
            {
                moved = true; 

                moveIndex++;
                System.out.println("Attack from range AI using move-to-attack");
                return targPos;
            }
        }
        else if(gs.getUnitMovementAndAttack().getAllAtkTiles(unitPos.getFirst(), unitPos.getSecond()).contains(targPos)
            && gs.getBoard().getTile(targPos).hasUnit()
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

    // @author William Manson (26044995M@student.gla.ac.uk)
    //
    //Find the enemy target for the ai to either move to or attack or cast etc
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
