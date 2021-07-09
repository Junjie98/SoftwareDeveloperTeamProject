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
	

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
		processChangedTurns(out, gameState);
		gameState.endTurnClicked(out);
	}

	private void processChangedTurns(ActorRef out, GameState gameState) {
		String msg = (gameState.getTurn() == Players.PLAYER1) ? "Player 1's turn" : "Player 2's turn";
		new PlayerNotificationCommandBuilder(out, gameState.isSimulation())
				.setMessage(msg)
				.setDisplaySeconds(2)
				// Setting Player to PLAYER2 should in theory work.
				// However, it is not properly supported by the front-end so it is strongly discouraged.
				.setPlayer(Players.PLAYER1)
				.issueCommand();
	}

}
