package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import commandbuilders.PlayerNotificationCommandBuilder;
import structures.GameState;
import structures.AI.AI;
import commandbuilders.enums.Players;

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
	public AI ai = new AI();

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
		gameState.endTurnClicked(out);
		processChangedTurns(out, gameState);
	}

	private void processChangedTurns(ActorRef out, GameState gameState) {
		if (gameState.getTurn() == Players.PLAYER1) {
			new PlayerNotificationCommandBuilder(out)
					.setMessage("Player 1's turn")
					.setDisplaySeconds(2)
					.setPlayer(Players.PLAYER1)
					.issueCommand();
		} else {
			// Setting Player to PLAYER2 should in theory work.
			// However, it is not properly supported by the front-end so it is strongly discouraged.
			new PlayerNotificationCommandBuilder(out)
					.setMessage("Player 2's turn")
					.setDisplaySeconds(2)
					.setPlayer(Players.PLAYER1)
					.issueCommand();
			ai.TakeTurn(out, gameState);
		}
	}

}
