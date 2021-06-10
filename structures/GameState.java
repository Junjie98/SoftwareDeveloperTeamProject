package structures;

import akka.actor.ActorRef;
import commandbuilders.DrawCardCommandBuilder;
import commandbuilders.States;
import decks.*;

/**
 * This class can be used to hold information about the on-going game.
 * Its created with the GameActor.
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class GameState {
    private Players turn = Players.PLAYER1;
    private int player1CardsInHand = 0;
    private int player2CardsInHand = 0;  // TODO: Player2 card drawing not yet implemented
    private final int MAX_CARD_COUNT_IN_HAND = 6;

    private DeckOne deck1 = new DeckOne();
    private DeckTwo deck2 = new DeckTwo();

    public void nextTurn() {
        if (turn == Players.PLAYER1) {
            turn = Players.PLAYER2;
        } else {
            turn = Players.PLAYER1;
        }
    }

    public Players getTurn() {
        return turn;
    }

    public void setTurn(Players player) {
        turn = player;
    }

    public void drawCard(ActorRef out) {
        if (turn == Players.PLAYER1) {
            if (player1CardsInHand < MAX_CARD_COUNT_IN_HAND) {
                new DrawCardCommandBuilder(out)
                        .setCard(deck1.nextCard())
                        .setPosition(player1CardsInHand)
                        .setMode(States.NORMAL)
                        .issueCommand();
                player1CardsInHand++;
            }
        } else if (turn == Players.PLAYER2) {
            // TODO: Player2 card drawing not yet implemented
            player2CardsInHand++;
        }
    }
}
