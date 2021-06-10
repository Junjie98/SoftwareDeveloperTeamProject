package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import commandbuilders.TileDrawCommandBuilder;
import commandbuilders.States;
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

		final int INITIAL_CARD_COUNT = 3;

		// Setup the board
		for (int idx = 0; idx < 9; idx++) {
			for (int jdx = 0; jdx < 5; jdx++) {
				// TODO: Decide whether we want CommandBuilders
				// 1 - This uses the builder created by the professor
				// Tile tile = BasicObjectBuilders.loadTile(idx, jdx);
				// BasicCommands.drawTile(out, tile, 0);

				// 2 - This is the same thing using the command builder in the commandbuilders package.
				new TileDrawCommandBuilder(out)
						.setX(idx).setY(jdx).setMode(States.NORMAL)
						.issueCommand();
			}
		}

		// Draw Card
		// Player (turn)
		for (int idx = 0; idx < INITIAL_CARD_COUNT; idx++) {
			gameState.drawCard(out);
		}
	}

}


