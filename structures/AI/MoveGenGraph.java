package structures.AI;

import java.util.ArrayList;

import javax.lang.model.util.ElementScanner14;

import commandbuilders.enums.ActionEnum;
import commandbuilders.enums.Players;
import structures.Board;
import structures.GameState;
import structures.handlers.Pair;


public class MoveGenGraph 
{
    ArrayList<Vertex> verts = new ArrayList<>();
	int iteration;


	public Vertex generateActionsPerVert(Vertex v1, int vertDepth)
	{
        //find what actions are possible, create every option, act on the sequences


            ArrayList<Pair<Pair<Integer,Integer>,Pair<Integer, Integer>>> moveDat = new ArrayList<>();

            ArrayList<Pair<ActionEnum, Vertex>> actions = new ArrayList<>();
            						
			GameState l_state = new GameState(v1.state);

			GameState moved_state = moveUnits(l_state, moveDat);
			
			if(moved_state != null)
			{
				Vertex newVert = new Vertex(moved_state, vertDepth+1, v1.label+"m");
				
                for(int i = 0; i < moveDat.size(); i++)
                {
                    newVert.addMoveData(moveDat.get(i).getFirst(), moveDat.get(i).getSecond());

                }


				verts.add(newVert);
				
				actions.add(new Pair<> (ActionEnum.MOVE, newVert));
				
			}

			GameState cast_state = castSpells(l_state);
			
			if(moved_state != null)
			{
				Vertex newVert = new Vertex(moved_state, vertDepth+1, v1.label+"m");
				
                for(int i = 0; i < moveDat.size(); i++)
                {
                    newVert.addMoveData(moveDat.get(i).getFirst(), moveDat.get(i).getSecond());

                }


				verts.add(newVert);
				
				actions.add(new Pair<> (ActionEnum.MOVE, newVert));
				
			}






            v1.actions = actions;
		
			return v1;

		
	}


    public GameState castSpells(GameState gs)
    {
        ArrayList<ArrayList<Pair<Integer, Integer>>> units = getUnits(gs);
        ArrayList<Pair<Integer, Integer>> enemies = units.get(1);
        Pair<Integer, Integer> enemyTarget = null;
        ArrayList<Pair<Integer, Integer>> friendlies = units.get(0);
        Pair<Integer, Integer> friendlyTarget = null;

        for (Pair<Integer,Integer> pair : enemies) {
            if(Board.getInstance().getTile(pair).getUnit().getHealth() <= 2)
            {
                enemyTarget = pair;
            }
        }
        
        if(enemyTarget == null)
        {
            findEnemyTarget(units, gs);
        }

        int lowHealth = 20;

        for (Pair<Integer,Integer> pair : friendlies) {
            if(Board.getInstance().getTile(pair).getUnit().getHealth() < lowHealth)
            {
                lowHealth = Board.getInstance().getTile(pair).getUnit().getHealth();
                friendlyTarget = pair;
            }
        }

        for (int i = 0; i < gs.player2CardsInHand.size(); i++)
        {
            int id = gs.player2CardsInHand.get(i).getId();
            //17 18 = sundrop +2 staff + 2
            //19 20 = truestrike -2 decay -2
            if(id == 19 || id == 20)
            {
                //health +2 spell
            }
            else if(id == 17 || id == 18)
            {
                //dmg -2 spell
            }
        }
    }

    ///returns Friendlies on index 0 and enemies on 1
    public ArrayList<ArrayList<Pair<Integer, Integer>>> getUnits(GameState gs)
    {
        ArrayList<ArrayList<Pair<Integer, Integer>>> out = new ArrayList();
        if(gs.getTurn() == Players.PLAYER2)                             
        {   //For the AI turn, set the units
            out.add(gs.player1UnitsPosition);                              
            out.add(gs.player2UnitsPosition);                              
        }
        else
        {
            out.add(gs.player2UnitsPosition);                              
            out.add(gs.player1UnitsPosition);    
        }
        return out;

    }

    public Pair<Integer, Integer> findEnemyTarget(ArrayList<ArrayList<Pair<Integer, Integer>>> units, GameState gs)
    {
        int x = 0;
        Pair<Integer, Integer> enemyTarget = null;
        for (Pair<Integer,Integer> pos : units.get(1)) 
        {
            if(gs.getTurn() == Players.PLAYER2 ? pos.getFirst() > x : pos.getFirst() < x )
            {   //find the furthest forward unit
                //its a long board not a wide board 
                //so vertical movement is considered less risky
                x = pos.getFirst();
                enemyTarget = Pair.copy(pos);
            }
        }
        return enemyTarget;
    }

    public GameState moveUnits(GameState gs, ArrayList<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> moveData)
    {
        //find enemies
        //find units
        //find closest?
        //check range
        //attack/move

        ArrayList<ArrayList<Pair<Integer, Integer>>> units =getUnits(gs);
        ArrayList<Pair<Integer, Integer>> enemies = units.get(1);
        ArrayList<Pair<Integer, Integer>> friendlies = units.get(0);

        

        Pair<Integer,Integer> enemyTarget = findEnemyTarget(units, gs);

    
        int enemyXRange = enemyTarget.getFirst() + (gs.getTurn() == Players.PLAYER2 ? 2 : -2);    //add two for move and attack range


        for (int i = 0; i < friendlies.size() ; i++)        
        {   //For each friendly

            Pair<Integer,Integer> friendlyUnit = Pair.copy(friendlies.get(i));
            Pair<Integer,Integer> friendlyTarget = friendlies.get(i);
            if(friendlyTarget.getFirst() > enemyXRange + 1)             
            {   //if that friendly is out of attack range then move forwards
                friendlyTarget.setFirst(friendlyTarget.getFirst() - 2);
            }
            else
            {   //if the friendly is about to or has entered attack range
                int yMove = enemyTarget.getSecond() - friendlyTarget.getSecond();
                int xMove = enemyTarget.getFirst() - friendlyTarget.getFirst();

                yMove = yMove>2?2:yMove;        //bounds are awful 
                xMove = xMove>2?2:xMove;

                yMove = yMove<-2?-2:yMove;
                xMove = xMove<-2?-2:xMove;


                if(Math.abs(yMove) > 1)
                {   //if we moving by two in one way then the other way must be zero
                    friendlyTarget.setSecond(friendlyTarget.getSecond() + yMove);
                    xMove = 0;
                }
                else if(Math.abs(yMove) == 1)
                {   //if we moving one in one way
                    if(Math.abs(xMove)>1)
                    {   //and the other way is more than 1 then limit 
                        //it to 1 as we only have 2 total moves
                        xMove = xMove>0?1:-1;
                    }
                    friendlyTarget.setFirst(friendlyTarget.getFirst() - xMove);
                    friendlyTarget.setSecond(friendlyTarget.getSecond() + yMove);
                }
                else
                {
                    friendlyTarget.setFirst(friendlyTarget.getFirst() - xMove);
                }

                if(friendlyTarget == enemyTarget)
                {
                    //we ontop of them ohno ¬.¬
                    //dont move, attack ++points
                }
                else
                {
                    if(friendlies.contains(friendlyTarget))
                    {   //If the new position is already occupied then try take a step backwards again

                        if(Math.abs(xMove) > 1) //if we move by two then a step back is just -1 in that direction
                        {
                            xMove = xMove/2;
                        }
                        else if(Math.abs(yMove) > 1) 
                        {
                            yMove = yMove/2;
                        }
                        else if(Math.abs(xMove) > 0)   
                        {   //if we've moved one in a direction and need to take a step back then we just -1 in that dir
                            //this also works if we move 1,1 and then make one of the dirs 0
                            xMove = 0;
                        }
                        else  
                        {
                            yMove = 0;
                        }
                        
                        if(!friendlies.contains(new Pair<>(friendlyTarget.getFirst() - xMove, friendlyTarget.getSecond() + yMove)))
                        {   //If the spot is not occupied then we can use it as the new spot
                            friendlyTarget = new Pair<>(friendlyTarget.getFirst() - xMove, friendlyTarget.getSecond() + yMove);
                        }
                        else
                        {
                            //dont move i guess?
                        }
                    
                    }
                    //successfull move add to list
                    moveData.add(new Pair<>(friendlyUnit, friendlyTarget));
                }
            }
        }

        return gs;
        
        
        
    }
}
