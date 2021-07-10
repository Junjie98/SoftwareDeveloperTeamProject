package structures.handlers;

import akka.actor.ActorRef;
import commandbuilders.enums.Players;
import commandbuilders.enums.UnitType;
import structures.GameState;
import structures.basic.Unit;

public class SpecialEffect {
    GameState parent;

    public SpecialEffect(GameState gameState) {
        parent = gameState;
    }

    public void unitDidDie(ActorRef out, Unit unit) {
        if (unit.getType() != UnitType.WINDSHRIKE) {
            return;
        }
        // Windshrike: When this unit dies, its owner draws a card.
        Players player = (parent.getTurn() == Players.PLAYER1) ? Players.PLAYER2 : Players.PLAYER1;
        parent.getCardDrawing().drawNewCardFor(out, player);
    }
}
