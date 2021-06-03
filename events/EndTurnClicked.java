package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.Players;

/**
 * Indicates that the user has clicked an object on the game canvas, in this case
 * the end-turn button.
 * 
 * { 
 *   messageType = “endTurnClicked”
 * }
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class EndTurnClicked implements EventProcessor{

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
		gameState.nextTurn();
		processChangedTurns(out, gameState);
	}

	private void processChangedTurns(ActorRef out, GameState gameState) {
		if (gameState.getTurn() == Players.PLAYER1) {
			BasicCommands.addPlayer1Notification(out, "Player 1's turn", 2);
			// TODO: Perform things that should be done on Player 1's turn.
		} else {
			BasicCommands.addPlayer1Notification(out, "Player 2's turn", 2);
			// TODO: Perform things that should be done on Player 2's turn.
		}
	}

}
