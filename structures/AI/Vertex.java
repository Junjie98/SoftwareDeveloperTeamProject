import java.util.ArrayList;

import commandbuilders.enums.ActionEnum;
import commandbuilders.enums.Players;
import structures.GameState;
import structures.handlers.Pair;

public class Vertex implements Comparable<Vertex>
{
	String label;
	int depth;
	int iteration;
	Vertex parent;
    ArrayList<Pair<ActionEnum, Vertex>> actions = new ArrayList<>();
    ArrayList<Pair<Pair<Integer,Integer>,Pair<Integer,Integer>>> moveData = new ArrayList<>();

	Players turn = Players.PLAYER1;
	boolean swappedPlayers = false;
	int goodness;

	GameState state;
	
	public Vertex(GameState state, int depth, String label)
	{
		this.state = state;
		this.depth = depth;
		this.label = label;
		goodness = evaluate();
	}
	
	public void addActions(ActionEnum action, Vertex newChild)
	{
		this.actions.add(new Pair<>(action, newChild));
	}	
    
      
    public void addMoveData(int xf, int yf, int tx, int ty)
	{
        addMoveData(new Pair<>(xf,yf), new Pair<>(tx,ty));
    }
    public void addMoveData(Pair<Integer,Integer> from, Pair<Integer,Integer>  to)
	{
		this.moveData.add(new Pair<>(from, to));
	}
	
	public int evaluate()
	{
		int totalPoints = 0;
		
		for (int i = 0; i < this.state.player1UnitsPosition.size(); i++)
		{
			totalPoints += 100;				//100 points per unit on the board
		}
		
		for (int i = 0; i < this.state.player2UnitsPosition.size(); i++)
		{
			totalPoints -= 100;				//-100 points per enemy on the board
		}
		
		for(int i = 0; i < this.depth; i++)
		{
			totalPoints += 70;				//+70 points for be able to do more in a turn
		}
		
		char[] tempLabel = this.label.toCharArray();
		
		for(int i = 0; i< tempLabel.length; i++)
		{
			if(tempLabel[i] == 'D')
			{
				totalPoints+=50;			//50 points for a damage cast spell
			}
		}
		
		if(swappedPlayers == true)
		{
			totalPoints = -totalPoints;
		}
		
		return totalPoints;
	}

	@Override
	public int compareTo(Vertex o) 
	{
		if(this.goodness>o.goodness)
		{
			return (swappedPlayers ? 1 : -1); 
		}
		else if(this.goodness<o.goodness)
		{
			return (swappedPlayers ? -1 : 1);
		}
		else
		{
			return 0;
		}
	}
	
	public void swap()
	{
		swappedPlayers = !swappedPlayers;
		//state.swap();
		
		
	}
}
