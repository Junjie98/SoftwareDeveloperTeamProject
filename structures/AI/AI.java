package structures.AI;

import java.util.ArrayList;
import akka.actor.ActorRef;

import commandbuilders.enums.Players;
import structures.GameState;
import structures.handlers.Pair;
import structures.Board;

public class AI 
{
    static ArrayList<Pair<Integer, Integer>> friendlies = null;
    static Pair<Integer, Integer> enemyTarget= null;
    static int enemyXRange= 0;
    static int moveIndex = 0;

    public AI()
    {

    }

    public void TakeTurn(ActorRef out, GameState gs)
    {
        moveInit(out, gs);
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

    static public void move(ActorRef out, GameState gs)
    {

        //Todo Make a response factor so that we can tell what needs to happen next, eg if we move then we wait for move response, if we attack we wait for attack response etc
        //move and attack seems to work okay but only for units that can arrange by ranged attack
        //try with 3 enemy units to see what happens
        
        if(moveIndex >= friendlies.size())
        {
            return;
        }
        moveIndex++;

        System.err.println("Number of friendlies: " + friendlies.size());

        //For each friendly
        
            
        System.err.println("index: " + 0); //we always want the top of the stack eg 0

        Pair<Integer,Integer> friendlyUnit = new Pair<>(friendlies.get(0).getFirst(),friendlies.get(0).getSecond());
        Pair<Integer,Integer> friendlyTarget = null;
        System.err.println("friendly unit mem check");
        System.err.println(friendlyUnit);
        System.err.println(friendlyTarget);

        if(friendlyUnit.getFirst() > enemyXRange + 1)             
        {   //if that friendly is out of attack range then move forwards
            friendlyTarget = simpleMove(friendlyUnit);

        }
        else
        {   //if the friendly is about to or has entered attack range
            int yMove = enemyTarget.getSecond() - friendlyUnit.getSecond();
            int xMove = enemyTarget.getFirst() - friendlyUnit.getFirst();

            yMove = yMove>2?2:yMove;        //bounds are awful 
            xMove = xMove>2?2:xMove;

            yMove = yMove<-2?-2:yMove;
            xMove = xMove<-2?-2:xMove;

            if(Math.abs(yMove) > 1)
            {
                xMove=0;
            }
            else if(Math.abs(yMove) ==1)
            {
                if(Math.abs(xMove) > 0 )
                {
                    xMove = xMove > 0 ? 1 : -1;
                }
                
            }
            
            if(Board.getInstance().getTile(friendlyUnit.getFirst() + xMove, friendlyUnit.getSecond() + yMove).getUnit().getPlayerID() != Players.PLAYER2)
            {
                friendlyTarget= new Pair<>(friendlyUnit.getFirst() + xMove, friendlyUnit.getSecond() + yMove);

            }
            else
            {
                
                if(Math.abs(xMove) == 1)
                {
                    if(Math.abs(yMove) ==1)
                    {
                        xMove = 0;

                    }
                }
                else if(Math.abs(yMove) ==1)
                {
                    yMove = 0;
                    
                }
                friendlyTarget= new Pair<>(friendlyUnit.getFirst() + xMove, friendlyUnit.getSecond() + yMove);

            }


        }

        System.err.println("Tiles selected by ai: ");
        System.err.println(friendlyUnit.getFirst() + " " + friendlyUnit.getSecond());
        System.err.println(friendlyTarget.getFirst() + " " + friendlyTarget.getSecond());
        System.err.println("Number of friendlies: " + friendlies.size());

        gs.tileClicked(out, friendlyUnit.getFirst(), friendlyUnit.getSecond());
        System.err.println("Units selected for move waiting...");
        try {Thread.sleep(2500);} catch (InterruptedException e) {e.printStackTrace();}


        gs.tileClicked(out, friendlyTarget.getFirst(), friendlyTarget.getSecond());
        System.err.println("Units moving for target waiting...");

        try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
        System.err.println("Number of friendlies: " + friendlies.size());

        

        
    }
    
    public Pair<Integer,Integer> simpleMove(Pair<Integer,Integer> friendlyUnit)
    {
        Pair<Integer,Integer> friendlyTarget = null;
        if(Board.getInstance().getTile(friendlyUnit.getFirst() - 2, friendlyUnit.getSecond()).hasUnit())    
        {   //if that space has a unit
            if(Board.getInstance().getTile(friendlyUnit.getFirst() - 2, friendlyUnit.getSecond()).getUnit().getPlayerID() != Players.PLAYER2)
            {   //and its not our unit then move attack
                friendlyTarget= new Pair<>(friendlyUnit.getFirst() - 2, friendlyUnit.getSecond());
                System.err.println("simple 2 forward");
            }
            else
            {
                
                if(Board.getInstance().getTile(friendlyUnit.getFirst() - 1, friendlyUnit.getSecond()).hasUnit())
                {
                    if(Board.getInstance().getTile(friendlyUnit.getFirst() -1, friendlyUnit.getSecond()).getUnit().getPlayerID() != Players.PLAYER2)
                    {
                        friendlyTarget= new Pair<>(friendlyUnit.getFirst() - 1, friendlyUnit.getSecond());
                    }
                    else
                    {
                        if(Board.getInstance().getTile(friendlyUnit.getFirst() - 1, friendlyUnit.getSecond()-1) != null)
                        {
                            friendlyTarget= new Pair<>(friendlyUnit.getFirst() - 1, friendlyUnit.getSecond()-1);

                        }
                        else
                        {
                            friendlyTarget= new Pair<>(friendlyUnit.getFirst() - 1, friendlyUnit.getSecond()+1);

                        }

                    }
                }
            }
        }
        else
        {
            friendlyTarget= new Pair<>(friendlyUnit.getFirst() - 2, friendlyUnit.getSecond());
            System.err.println("simple 2 forward");

        }
        return friendlyTarget;
    }

}
