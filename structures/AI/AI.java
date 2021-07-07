package structures.AI;

import java.util.ArrayList;
import java.util.HashMap;
import akka.actor.ActorRef;

import commandbuilders.enums.Players;
import commandbuilders.enums.States;
import commandbuilders.enums.TileEffectAnimation;
import structures.GameState;
import structures.basic.Card;
import structures.basic.Unit;
import structures.handlers.Pair;
import structures.Board;

public class AI 
{
    static ArrayList<Pair<Integer, Integer>> friendlies = null;
    static Pair<Integer, Integer> enemyTarget= null;
    Pair<Integer,Integer> targPos = null;
    static int enemyXRange= 0;
    static int moveIndex = 0;
    static boolean attack = false;
    int mana = 0;

    public AI()
    {

    }

    public void TakeTurn(ActorRef out, GameState gs)
    {
        System.out.println("AI Started");
        moveInit(out, gs);
        castInit(out, gs);
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
        if(moveIndex < friendlies.size())
        {
            move(out, gs);
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
        mana = gs.getPlayer2().getMana();
        System.out.println("player 2 mana: " + mana);

        if(gs.player2CardsInHand.size()==0)
        {
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
            for(Card key : summonCards.keySet())
            {
                if(key.getManacost()>highCost)
                {
                    highCost = key.getManacost();
                    indexOfHigh = summonCards.get(key).intValue();
                }
            }

            ArrayList<Pair<Integer,Integer>> tiles = new ArrayList<>();

            for(int i =0; i < gs.player2UnitsPosition.size(); i++)
            {
                Pair<Integer,Integer> pos = gs.player2UnitsPosition.get(i);
                tiles.addAll(gs.getUnitMovementAndAttack().get1RAtkTiles(pos.getFirst(), pos.getSecond()));
            }

            for (Pair<Integer,Integer> pair : tiles) {
                if(!Board.getInstance().getTile(pair).hasUnit())
                {
                    targ = pair;
                }
            }


            AI_SummonUnit(out, gs, indexOfHigh, targ);
        }
 


 
    }

    // ===========================================================================
    // Move Methods
    // ===========================================================================

    public void moveInit(ActorRef out, GameState gs)
    {
        friendlies = gs.player2UnitsPosition;
        enemyTarget = findEnemyTarget(gs.player1UnitsPosition, gs);
        enemyXRange = enemyTarget.getFirst() + (gs.getTurn() == Players.PLAYER2 ? 2 : -2);    //add two for move and attack range
        moveIndex = 0;
        move(out, gs);

    }

    public void move(ActorRef out, GameState gs)
    {
        if(friendlies == null)
        {   //cant move if everything is dead
            return;
        }

        int friendliesNum = friendlies.size();
        // for (Pair<Integer,Integer> pair : friendlies) {
        //     if(Board.getInstance().getTile(pair).getUnit().getDoubleAttack())
        //     {
        //         friendliesNum = friendlies.size() + 1;
        //     }
        // }

        if(moveIndex >= friendliesNum)
        {   //If we've moved more than we have units big nope
            return;
        }
        

        Pair<Integer,Integer> unitPos = new Pair<>(friendlies.get(0).getFirst(),friendlies.get(0).getSecond());

        Unit temp = Board.getInstance().getTile(unitPos).getUnit();

        if(temp.getHasAttacked() || temp.getHasMoved())
        {
            return;
        }

        if(temp.isFlying()){
            //do flying stuff
        }

        boolean moved = false;
        targPos = enemyTarget;
        System.out.println("init targ pos: " + targPos);
        System.out.println("init targ state: " + Board.getInstance().getTile(targPos).getTileState());

        if(gs.getUnitMovementAndAttack().getAllAtkTiles(unitPos.getFirst(), unitPos.getSecond()).contains(targPos)
            && Board.getInstance().getTile(unitPos).hasUnit())
        {   //if we can attack and move then go for it and let that logic take care of itself
            moved = true; 

            moveIndex++;
            AI_AtkUnit(out, gs, unitPos, targPos);
            System.out.println("Attack from range AI using move-to-attack");
        }
        
        if(moved ==false)
        {
            //if we cant directly attack it then we need to find a new tile to move to that helps us
                
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

            if(gs.getUnitMovementAndAttack().moveBlockCheck(unitPos.getFirst(), unitPos.getSecond(), xMove, yMove)){
                if(Math.abs(yMove) >= 2 || Math.abs(xMove) >= 2){
                    if(gs.getUnitMovementAndAttack().moveBlockCheck(unitPos.getFirst(), unitPos.getSecond(), xMove/2, yMove/2)){
                        return;
                    }
                    else{
                        targPos = new Pair<>(unitPos.getFirst() + xMove/2, unitPos.getSecond() + yMove/2);
                    }

                }
                else{
                    return;
                }

            }
            else{
                targPos = new Pair<>(unitPos.getFirst() + xMove, unitPos.getSecond() + yMove);

            }


            System.err.println("Tiles selected by ai: ");
            System.err.println(unitPos);
            System.err.println(targPos);

            System.err.println("enemy targ pos: ");
            System.err.println(enemyTarget);

            moveIndex++;

            AI_MoveUnit(out, gs, unitPos, targPos);
        }
      
            


    

    //    if(targPos == enemyTarget)
    //    {
    //        //we are attacking
    //        move(out, gs);
    //    }
 
    }


    // ===========================================================================
    // Helper Methods
    // ===========================================================================

    public void moveCheck(ActorRef out, GameState gs)
    {
        System.err.println("recieve pulse");
        if(friendlies != null && moveIndex < friendlies.size())
        {
            move(out, gs);
        }
    }

    public Pair<Integer, Integer> findEnemyTarget(ArrayList<Pair<Integer, Integer>> units, GameState gs)
    {
        int x = -1;
        for (Pair<Integer,Integer> pos : units) 
        {
            if(x<pos.getFirst())
            {
                System.err.println(pos.getFirst());
                x = pos.getFirst();
                enemyTarget = pos;
            }
        }
        return enemyTarget;
    }

}
