package structures.AI;

import java.util.ArrayList;
import java.util.HashMap;

import javax.lang.model.util.ElementScanner14;

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
        moveInit(out, gs);
        castInit(out, gs);
    }

    public void castInit(ActorRef out, GameState gs)
    {
        ArrayList<Pair<Integer, Integer>> cardPriority = new ArrayList<>();

        //check mana
        //check spells
        //prioritise and cast
        mana = gs.getPlayer1().getMana();
        for (int i = 0; i < gs.player2CardsInHand.size(); i++) {
            if(gs.player2CardsInHand.get(i).getManacost() <= mana){
                int priority = 1 + gs.player2CardsInHand.get(i).getManacost();
                if(gs.player2CardsInHand.get(i).isSpell()){
                    priority = 0;
                }
                cardPriority.add(new Pair<Integer, Integer>(i, priority));
            }
        }

        for (Pair<Integer,Integer> cardInfo : cardPriority) {
            
        }
    }

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


    public void moveInit(ActorRef out, GameState gs)
    {
        friendlies = gs.player2UnitsPosition;
        enemyTarget = findEnemyTarget(gs.player1UnitsPosition, gs);
        enemyXRange = enemyTarget.getFirst() + (gs.getTurn() == Players.PLAYER2 ? 2 : -2);    //add two for move and attack range
        moveIndex = 0;
        move(out, gs);

    }


    public  void getSecondaryTiles(int x, int y)
    {

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

        gs.tileClicked(out, unitPos.getFirst(), unitPos.getSecond());
        try {Thread.sleep(2500);} catch (InterruptedException e) {e.printStackTrace();}


        if(Board.getInstance().getTile(targPos).getTileState() == States.RED)
        {   //if we can attack and move then go for it and let that logic take care of itself
            moved = true; 

            gs.tileClicked(out, targPos.getFirst(), targPos.getSecond());
            try {Thread.sleep(2500);} catch (InterruptedException e) {e.printStackTrace();}
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
            //System.err.println("Number of friendlies: " + friendlies.size());

            System.err.println("enemy targ pos: ");
            System.err.println(enemyTarget);

            // gs.tileClicked(out, unitPos.getFirst(), unitPos.getSecond());
            // //System.err.println("Units selected for move waiting...");
            // try {Thread.sleep(2500);} catch (InterruptedException e) {e.printStackTrace();}


            gs.tileClicked(out, targPos.getFirst(), targPos.getSecond());
            //System.err.println("Units moving for target waiting...");

            try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
            // System.err.println("Number of friendlies: " + friendlies.size());


        }
        else
        {
            moveIndex++;
        }
            


    

       if(targPos == enemyTarget)
       {
           //we are attacking
           move(out, gs);
       }
 
    }

}
