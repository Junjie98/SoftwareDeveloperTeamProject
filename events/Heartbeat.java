package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.Players;

/**
 * In the user’s browser, the game is running in an infinite loop, where there is around a 1 second delay 
 * between each loop. Its during each loop that the UI acts on the commands that have been sent to it. A 
 * heartbeat event is fired at the end of each loop iteration. As with all events this is received by the Game 
 * Actor, which you can use to trigger game logic.
 * 
 * { 
 *   String messageType = “heartbeat”
 * }
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class Heartbeat implements EventProcessor{

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
		if (gameState.isTurnChanged()) {
			processChangedTurns(out, gameState);
		}
	}

	private void processChangedTurns(ActorRef out, GameState gameState) {
		if (gameState.getTurn() == Players.PLAYER1) {
			BasicCommands.addPlayer1Notification(out, "Player 1's turn", 2);
			// Perform things that should be done on Player 1's turn.
		} else {
			BasicCommands.addPlayer1Notification(out, "Player 2's turn", 2);
			// Perform things that should be done on Player 2's turn.
		}
	}
}
