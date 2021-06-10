package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import commandbuilders.TileDrawCommandBuilder;
import commandbuilders.enums.States;
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
				new TileDrawCommandBuilder(out)
						.setX(idx).setY(jdx).setMode(States.NORMAL)
						.issueCommand();
			}
		}

		gameState.drawInitialCards(out);
	}
}


