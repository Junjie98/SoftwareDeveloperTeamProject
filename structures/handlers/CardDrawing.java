package structures.handlers;

import akka.actor.ActorRef;
import commandbuilders.CardInHandCommandBuilder;
import commandbuilders.enums.CardInHandCommandMode;
import commandbuilders.enums.Players;
import commandbuilders.enums.States;
import decks.DeckOne;
import decks.DeckTwo;
import structures.GameState;
import structures.basic.Card;

import java.util.ArrayList;

public class CardDrawing {
    final private GameState parent;

    private final int MAX_CARD_COUNT_IN_HAND = 6;

    final private DeckOne deck1 = new DeckOne();
    final private DeckTwo deck2 = new DeckTwo();

    public CardDrawing(GameState parent) {
        this.parent = parent;
    }

    public void drawNewCardFor(ActorRef out, Players player) {
        if (out != null) {  // Block simulation check for tests
            if (parent.isSimulation()) {
                // Block card drawing for simulation.
                return;
            }
        }

        ArrayList<Card> current = parent.getCardsInHand(player);

        // Protect the program so it will not throw exception if deck has no more card.
        if (player == Players.PLAYER1 && deck1.isEmpty()) {
            return;
        } else if (player == Players.PLAYER2 && deck2.isEmpty()) {
            return;
        }

        Card temp = (player == Players.PLAYER1) ? deck1.nextCard() : deck2.nextCard();
        current.add(temp);

        if (current.size() > MAX_CARD_COUNT_IN_HAND) {
            // Remove the last card drawn.
            current.remove(current.size()-1);
        }

        //Print the size of deck of cards
        int deckSize = (player == Players.PLAYER1) ? deck1.getSize() : deck2.getSize();
        System.out.println(player + " has another " + deckSize + " cards.");
    }
    public void displayCardsOnScreenFor(ActorRef out, Players player) {
        if (parent.isSimulation()) {
            return;
        }
        ArrayList<Card> currentCardInHand = parent.getCardsInHand(player);
        for (int idx = 0; idx < MAX_CARD_COUNT_IN_HAND; idx++) {
            if (idx < currentCardInHand.size()) {
                new CardInHandCommandBuilder(out, parent.isSimulation())
                        .setCommandMode(CardInHandCommandMode.DRAW)
                        .setCard(currentCardInHand.get(idx))
                        .setPosition(idx)
                        .setState(States.NORMAL)
                        .issueCommand();
            } else {
                new CardInHandCommandBuilder(out, parent.isSimulation())
                        .setCommandMode(CardInHandCommandMode.DELETE)
                        .setPosition(idx)
                        .issueCommand();
            }
        }
    }

    public boolean isDeckOneEmpty() {
        return deck1.isEmpty();
    }

    public boolean isDeckTwoEmpty() {
        return deck2.isEmpty();
    }
}
