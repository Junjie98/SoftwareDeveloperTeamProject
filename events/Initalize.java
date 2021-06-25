package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import commandbuilders.TileCommandBuilder;
import commandbuilders.enums.States;
import structures.Board;
import structures.GameState;

/**
 * Indicates that both the core game loop in the browser is starting, meaning
 * that it is ready to recieve commands from the back-end.
 * 
 * { 
 *   messageType = “initalize”
 * }
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class Initalize implements EventProcessor{
	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
		// CommandDemo.executeDemo(out); // this executes the command demo, comment out this when implementing your solution

		// Setup the board
		for (int idx = 0; idx < 9; idx++) {
			for (int jdx = 0; jdx < 5; jdx++) {
				new TileCommandBuilder(out)
						.setTilePosition(idx, jdx).setState(States.NORMAL)
						.issueCommand();
			}
		}

		Board.reloadBoard();

		// Create the two users
		gameState.generateTwoUsers(out);

		// Deal initial cards
		gameState.drawInitialCards(out);

		//Spawn avatars
		gameState.spawnAvatars(out);
	}
}


