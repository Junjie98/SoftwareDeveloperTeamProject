package structures.AI;

import java.util.ArrayList;
import java.util.HashMap;


import akka.actor.ActorRef;

import commandbuilders.enums.Players;
import structures.GameState;
import structures.basic.Card;
import structures.basic.Unit;
import structures.handlers.Pair;
import structures.Board;

public class AI 
{
    static ArrayList<Pair<Integer, Integer>> friendlies = null;
    static Pair<Integer, Integer> enemyTarget= null;
    static Pair<Integer,Integer> targPos = null;
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
        {
            return;
        }

        if(moveIndex >= friendlies.size())
        {
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

        int xMove = enemyTarget.getFirst() - unitPos.getFirst();
        int yMove = enemyTarget.getSecond() - unitPos.getSecond();

        boolean forwardLean = Math.abs(xMove) > Math.abs(yMove) ? true : false;



        Pair<Integer,Integer> secPos1 = null;
        Pair<Integer,Integer> secPos2 = null;

        //Prioritise vertical movement - safer
        if(Math.abs(yMove) >= 2){
            yMove = yMove > 0 ? 2 : -2;
            xMove = 0;
        }
        else if(Math.abs(xMove) >= 2){
            xMove = xMove > 0 ? 2 : -2;
            yMove = 0;
        }

       targPos = new Pair<>(unitPos.getFirst() + xMove, unitPos.getSecond() + yMove);

       //SCARY CODE INCOMING AAAAAAAAAAAAAAAAAAAAHHHHHHHHHHHHHHHHHH SAVE ME YU :(
       if(yMove == 2){
            secPos1 = new Pair<>(unitPos.getFirst() + 1, unitPos.getSecond() + 1);
            secPos2 = new Pair<>(unitPos.getFirst() - 1, unitPos.getSecond() + 1);
       }
       else if(yMove == -2){
            secPos1 = new Pair<>(unitPos.getFirst() + 1, unitPos.getSecond() - 1);
            secPos2 = new Pair<>(unitPos.getFirst() - 1, unitPos.getSecond() - 1);
       }
       else if(xMove == 2){
            secPos1 = new Pair<>(unitPos.getFirst() + 1, unitPos.getSecond() + 1);
            secPos2 = new Pair<>(unitPos.getFirst() + 1, unitPos.getSecond() - 1);
       }
       else if(xMove == -2){
            secPos1 = new Pair<>(unitPos.getFirst() - 1, unitPos.getSecond() - 1);
            secPos2 = new Pair<>(unitPos.getFirst() - 1, unitPos.getSecond() + 1);
       }
       else if(xMove == 0 || yMove == 0)
       {
            secPos1 = new Pair<>(unitPos.getFirst(), unitPos.getSecond());
            secPos2 = new Pair<>(unitPos.getFirst(), unitPos.getSecond());
       }    
       else   
       {
            secPos1 = new Pair<>(unitPos.getFirst() + xMove, unitPos.getSecond());
            secPos2 = new Pair<>(unitPos.getFirst(), unitPos.getSecond() + yMove);
       }
       //help pls lol

       boolean blocked = false;

       if(Math.abs(xMove) == 2){
            blocked = Board.getInstance().getTile(new Pair<>(unitPos.getFirst() + xMove/2, unitPos.getSecond())).hasUnit();
        }
       else if(Math.abs(yMove) == 2){
            blocked = Board.getInstance().getTile(new Pair<>(unitPos.getFirst(), unitPos.getSecond() + yMove/2)).hasUnit();
        }

        if (!blocked)
        {
            if(Board.getInstance().getTile(targPos).hasUnit() 
                && Board.getInstance().getTile(targPos).getUnit().getPlayerID() != Players.PLAYER1){
                //then it is occupied by a friendly and we must adjust
                if(!Board.getInstance().getTile(secPos1).hasUnit()){
                    targPos = secPos1;
                }
                else if(!Board.getInstance().getTile(secPos2).hasUnit()){
                    targPos = secPos2;
                }
            }
        }
        else
        {
            if(!Board.getInstance().getTile(secPos1).hasUnit()){
                targPos = secPos1;
            }
            else if(!Board.getInstance().getTile(secPos2).hasUnit()){
                targPos = secPos2;
            }
            else{
                targPos = unitPos;
            }
        }



      

        System.err.println("Tiles selected by ai: ");
        System.err.println(unitPos);
        System.err.println(targPos);
        System.err.println("Number of friendlies: " + friendlies.size());

        System.err.println("enemy targ pos: ");
        System.err.println(enemyTarget);

        gs.tileClicked(out, unitPos.getFirst(), unitPos.getSecond());
        //System.err.println("Units selected for move waiting...");
        try {Thread.sleep(2500);} catch (InterruptedException e) {e.printStackTrace();}


        gs.tileClicked(out, targPos.getFirst(), targPos.getSecond());
        //System.err.println("Units moving for target waiting...");

        try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
       // System.err.println("Number of friendlies: " + friendlies.size());

       moveIndex++;

       if(targPos == enemyTarget)
       {
           //we are attacking
           move(out, gs);
       }

    }

    public void dmove(ActorRef out, GameState gs)
    {
        if(gs.getTurn() != Players.PLAYER2)
        {
            return;
        }
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
        
            
        //System.err.println("index: " + 0); //we always want the top of the stack eg 0

        Pair<Integer,Integer> friendlyUnit = new Pair<>(friendlies.get(0).getFirst(),friendlies.get(0).getSecond());
        Pair<Integer,Integer> friendlyTarget = null;
        //System.err.println("friendly unit mem check");
        //System.err.println(friendlyUnit);
       // System.err.println(friendlyTarget);

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
            else if(Math.abs(yMove) ==1 && Math.abs(xMove) > 0)
            {
                xMove = xMove > 0 ? 1 : -1;               
            }
            
            if(Board.getInstance().getTile(friendlyUnit.getFirst() + xMove, friendlyUnit.getSecond() + yMove).hasUnit())
            {
                if(Board.getInstance().getTile(friendlyUnit.getFirst() + xMove, friendlyUnit.getSecond() + yMove).getUnit().getPlayerID() != Players.PLAYER2)
                {   //if not a friendly occupied space then attack or similar
                    int tempX = 0;
                    int tempY = 0;

                    if(Math.abs(xMove) == 2)
                    {   //if either x or y move magnitude is 2 then get the half value to test path
                        tempX = xMove/2;
                        if(!Board.getInstance().getTile(friendlyUnit.getFirst() + tempX, friendlyUnit.getSecond() + tempY).hasUnit()
                            || new Pair<Integer,Integer>(friendlyUnit.getFirst() + tempX, friendlyUnit.getSecond() + tempY) != friendlyUnit)
                            {   //if there isnt a unit in the way, and we arent testing the same square we are stood on, 
                                //eg 1/2 = 0 xmovee = 0, eg this square, so obviously occupied but not blocking
                                friendlyTarget= new Pair<>(friendlyUnit.getFirst() + xMove, friendlyUnit.getSecond() + yMove);
                            }
                    else
                    {   //if we cant move to this square because the path is occupied then try to move around them
                        if(Board.getInstance().getTile(friendlyUnit.getFirst() - tempX, friendlyUnit.getSecond()-1) != null 
                            && Board.getInstance().getTile(friendlyUnit.getFirst() - tempX, friendlyUnit.getSecond()-1).hasUnit() == false)
                        {
                            friendlyTarget= new Pair<>(friendlyUnit.getFirst() - tempX, friendlyUnit.getSecond()-1);
                        }
                        else if(Board.getInstance().getTile(friendlyUnit.getFirst() - tempX, friendlyUnit.getSecond()+1) != null 
                            && Board.getInstance().getTile(friendlyUnit.getFirst() - tempX, friendlyUnit.getSecond()+1).hasUnit() == false)
                        {
                            friendlyTarget= new Pair<>(friendlyUnit.getFirst() - tempX, friendlyUnit.getSecond()+1);

                        }
                    }
                    }
                    else if(Math.abs(yMove) == 2)
                    {
                        tempY = yMove/2;
                        if(!Board.getInstance().getTile(friendlyUnit.getFirst() + tempX, friendlyUnit.getSecond() + tempY).hasUnit()
                            || new Pair<Integer,Integer>(friendlyUnit.getFirst() + tempX, friendlyUnit.getSecond() + tempY) != friendlyUnit)
                            {   //if there isnt a unit in the way, and we arent testing the same square we are stood on, 
                                //eg 1/2 = 0 xmovee = 0, eg this square, so obviously occupied but not blocking
                                friendlyTarget= new Pair<>(friendlyUnit.getFirst() + xMove, friendlyUnit.getSecond() + yMove);
                            }
                        else
                        {   //if we cant move to this square because the path is occupied then try to move around them
                            if(Board.getInstance().getTile(friendlyUnit.getFirst() - 1, friendlyUnit.getSecond()+ tempY) != null 
                                && Board.getInstance().getTile(friendlyUnit.getFirst() - 1, friendlyUnit.getSecond()+ tempY).hasUnit() == false)
                            {
                                friendlyTarget= new Pair<>(friendlyUnit.getFirst() - 1, friendlyUnit.getSecond()+ tempY);
                            }
                            else if(Board.getInstance().getTile(friendlyUnit.getFirst() + 1, friendlyUnit.getSecond()+ tempY) != null 
                                && Board.getInstance().getTile(friendlyUnit.getFirst() + 1, friendlyUnit.getSecond()+ tempY).hasUnit() == false)
                            {
                                friendlyTarget= new Pair<>(friendlyUnit.getFirst() + 1, friendlyUnit.getSecond()+ tempY);

                            }
                        }
                    }

                   

                }
                else
                {
                    if(Math.abs(xMove) >= 1)
                    {
                        xMove = xMove/2;
                    }
                    else if(Math.abs(xMove) == 1)
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
            else
            {
                friendlyTarget= new Pair<>(friendlyUnit.getFirst() + xMove, friendlyUnit.getSecond() + yMove);
            }

        }


        System.err.println("Tiles selected by ai: ");
        System.err.println(friendlyUnit.getFirst() + " " + friendlyUnit.getSecond());
        System.err.println(friendlyTarget.getFirst() + " " + friendlyTarget.getSecond());
        System.err.println("Number of friendlies: " + friendlies.size());

        System.err.println("enemy targ pos: ");
        System.err.println(enemyTarget);

        gs.tileClicked(out, friendlyUnit.getFirst(), friendlyUnit.getSecond());
        //System.err.println("Units selected for move waiting...");
        try {Thread.sleep(2500);} catch (InterruptedException e) {e.printStackTrace();}


        gs.tileClicked(out, friendlyTarget.getFirst(), friendlyTarget.getSecond());
        //System.err.println("Units moving for target waiting...");

        try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
       // System.err.println("Number of friendlies: " + friendlies.size());

        if(enemyTarget == friendlyTarget)
        {
            attack = true;
            System.err.println("attacking true");
            move(out, gs);
        }

        
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
