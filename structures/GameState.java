package structures;

import akka.actor.ActorRef;
import commandbuilders.CardInHandCommandBuilder;
import commandbuilders.PlayerSetCommandsBuilder;
import commandbuilders.UnitCommandBuilder;
import commandbuilders.enums.CardInHandCommandMode;
import commandbuilders.enums.PlayerStats;
import commandbuilders.enums.Players;
import commandbuilders.enums.States;
import commandbuilders.enums.UnitCommandBuilderMode;
import commands.BasicCommands;
import decks.*;
import structures.basic.Card;
import structures.basic.Player;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

/**
 * This class can be used to hold information about the on-going game.
 * Its created with the GameActor.
 * 
 * @author Dr. Richard McCreadie
 *
 */

public class GameState {
    private final int MAX_CARD_COUNT_IN_HAND = 6;
    private final int INITIAL_CARD_COUNT = 3;

    private Players turn = Players.PLAYER1;
    // TODO: This should be randomised according to game loop.

    private Player player1, player2;

    private Card[] player1CardsInHand = new Card[MAX_CARD_COUNT_IN_HAND];
    private int player1CardsInHandCount = 0;
    private Card[] player2CardsInHand = new Card[MAX_CARD_COUNT_IN_HAND];
    private int player2CardsInHandCount = 0;

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

    public void generateTwoUsers(ActorRef out) {
        player1 = new Player();
        player2 = new Player();

        new PlayerSetCommandsBuilder(out)
                .setPlayer(Players.PLAYER1)
                .setStats(PlayerStats.ALL)
                .setInstance(player1)
                .issueCommand();
        new PlayerSetCommandsBuilder(out)
                .setPlayer(Players.PLAYER2)
                .setStats(PlayerStats.ALL)
                .setInstance(player2)
                .issueCommand();
    }

    // This method add 3 cards to both Players as part of initialisation.
    public void drawInitialCards(ActorRef out) {
        for (int idx = 0; idx < INITIAL_CARD_COUNT; idx++) {
            drawNewCardFor(Players.PLAYER1);
            drawNewCardFor(Players.PLAYER2);
        }
        displayCardsOnScreenFor(out, turn);
    }

    public void drawCard(ActorRef out, Players player) {
        drawNewCardFor(player);
        displayCardsOnScreenFor(out, player);
    }

    private void drawNewCardFor(Players player) {
        // TODO: This does not protect anything regarding end game logic.
        if (player == Players.PLAYER1) {
            if (player1CardsInHandCount < MAX_CARD_COUNT_IN_HAND) {
                Card temp = deck1.nextCard();
                player1CardsInHand[player1CardsInHandCount++] = temp;
            }
        } else {
            if (player2CardsInHandCount < MAX_CARD_COUNT_IN_HAND) {
                Card temp = deck2.nextCard();
                player2CardsInHand[player2CardsInHandCount++] = temp;
            }
        }
    }

    private void displayCardsOnScreenFor(ActorRef out, Players player) {
        Card[] currentCardInHand = (player == Players.PLAYER1) ? player1CardsInHand : player2CardsInHand;
        int currentCardInHandCount = (player == Players.PLAYER2) ? player1CardsInHandCount : player2CardsInHandCount;
        for (int idx = 0; idx < currentCardInHand.length; idx++) {
            if (idx < currentCardInHandCount) {
                new CardInHandCommandBuilder(out)
                        .setCommandMode(CardInHandCommandMode.DRAW)
                        .setCard(currentCardInHand[idx])
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


    public void spawnAvatars(ActorRef out)
    {
        Unit unit = BasicObjectBuilders.loadUnit(StaticConfFiles.humanAvatar, 0, Unit.class);
        Unit unit2 = BasicObjectBuilders.loadUnit(StaticConfFiles.aiAvatar, 0, Unit.class);

		Tile tile = BasicObjectBuilders.loadTile(1, 2);
		Tile tile2 = BasicObjectBuilders.loadTile(7, 2);

        new UnitCommandBuilder(out)
                    .setMode(UnitCommandBuilderMode.DRAW)
                    .setTile(tile)
                    .setUnit(unit)
                    .issueCommand();

        new UnitCommandBuilder(out)
                    .setMode(UnitCommandBuilderMode.DRAW)
                    .setTile(tile2)
                    .setUnit(unit2)
                    .issueCommand();
                    
    }

}
