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
    private GameState parent;

    private final int MAX_CARD_COUNT_IN_HAND = 6;

    private DeckOne deck1 = new DeckOne();
    private DeckTwo deck2 = new DeckTwo();

    public CardDrawing(GameState parent) {
        this.parent = parent;
    }

    public void drawNewCardFor(ActorRef out,Players player) {
        ArrayList<Card> current = (player == Players.PLAYER1) ? parent.player1CardsInHand : parent.player2CardsInHand;
        if (current.isEmpty()) {
            if (player == Players.PLAYER1 && deck1.isEmpty()) {
                parent.endGame(out);
            }
            if (player == Players.PLAYER2 && deck2.isEmpty()) {
                parent.endGame(out);
            }
        }
        if (current.size() < MAX_CARD_COUNT_IN_HAND) {
            Card temp = (player == Players.PLAYER1) ? deck1.nextCard() : deck2.nextCard();
            current.add(temp);
        }
    }
    public void displayCardsOnScreenFor(ActorRef out, Players player) {
        ArrayList<Card> currentCardInHand = (player == Players.PLAYER1) ? parent.player1CardsInHand : parent.player2CardsInHand;
        for (int idx = 0; idx < MAX_CARD_COUNT_IN_HAND; idx++) {
            if (idx < currentCardInHand.size()) {
                new CardInHandCommandBuilder(out)
                        .setCommandMode(CardInHandCommandMode.DRAW)
                        .setCard(currentCardInHand.get(idx))
                        .setPosition(idx)
                        .setState(States.NORMAL)
                        .issueCommand();
            } else {
                new CardInHandCommandBuilder(out)
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
