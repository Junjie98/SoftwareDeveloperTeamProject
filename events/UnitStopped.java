package events;


import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import structures.GameState;

/**
 * Indicates that a unit instance has stopped moving. 
 * The event reports the unique id of the unit.
 * 
 * { 
 *   messageType = “unitStopped”
 *   id = <unit id>
 * }
 *
 * @author Dr. Richard McCreadie
 * @author Yu-Sung Hsu (2540296h@student.gla.ac.uk)
 */
public class UnitStopped implements EventProcessor{
	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
		int unitid = message.get("id").asInt();
		gameState.getUnitMovementAndAttack().setUnitsCanMove(true);
	}
}
