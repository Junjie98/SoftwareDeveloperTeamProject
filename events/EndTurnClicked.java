package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import commandbuilders.PlayerNotificationCommandBuilder;
import structures.GameState;
import commandbuilders.enums.Players;
import structures.basic.Tile;
import structures.basic.Unit;

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
		if (gameState.getPreMove() == true) {
			Tile prev = gameState.getPreviousUnitLocation();
			int[][] active = gameState.getAllMoveTiles(prev.getTilex(), prev.getTiley());
			gameState.TileUnhighlight(out, active);
		}

		if (gameState.getPreClickCard() == true) {
			gameState.cardUnhighlight(out);
		}

		gameState.nextTurn();
		processChangedTurns(out, gameState);
	}

	private void processChangedTurns(ActorRef out, GameState gameState) {
		if (gameState.getTurn() == Players.PLAYER1) {
			new PlayerNotificationCommandBuilder(out)
					.setMessage("Player 1's turn")
					.setDisplaySeconds(2)
					.setPlayer(Players.PLAYER1)
					.issueCommand();
			
			// Ana: reset mana before incrementing
			gameState.resetMana(out);
			
			//mana increment after endturn
			gameState.ManaIncrementPerRound(out);
			// TODO: Perform things that should be done on Player 1's turn.
			gameState.drawCard(out, Players.PLAYER1);
			
		} else {
			
			// Setting Player to PLAYER2 should in theory work.
			// However, it is not properly supported by the front-end so it is strongly discouraged.
			new PlayerNotificationCommandBuilder(out)
					.setMessage("Player 2's turn")
					.setDisplaySeconds(2)
					.setPlayer(Players.PLAYER1)
					.issueCommand();
			
			//mana increment after endturn
			if(gameState.getRound() > 1) { //Checks if it is round 0. If it is, dont increment the mana of Player2

				// Ana: reset mana before incrementing
				gameState.resetMana(out);
				
				gameState.ManaIncrementPerRound(out);
			}

			// TODO: Perform things that should be done on Player 2's turn.
			gameState.drawCard(out, Players.PLAYER2);
		}
		
		// Ana: counter attack
		// Resetting hasGotAttacked of the players after an attack
		Unit attacker1 = gameState.getPreviousUnitLocation().getUnit();
		Unit attacker2 = gameState.getCurrentUnitLocation().getUnit();
		
		if (attacker1 != null)
			attacker1.setHasGotAttacked(false);
		
		if (attacker2 != null)
			attacker2.setHasGotAttacked(false);
	}

}
