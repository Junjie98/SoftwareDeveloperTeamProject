package structures.extractor;

import commandbuilders.enums.Players;
import structures.Board;
import structures.GameState;
import structures.basic.Card;
import structures.basic.Player;
import structures.handlers.Pair;

import java.util.ArrayList;

public class ExtractedGameState extends GameState {
    protected Board board = null;

    public ExtractedGameState() {
        super();
    }

    public void setSimulation(boolean value) {
        this.simulation = value;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public void setRoundNumber(int value) {
        this.roundNumber = value;
    }

    public void setPlayer(Players playerNumber, Player item) {
        switch (playerNumber) {
            case PLAYER1:
                this.player1 = item;
            case PLAYER2:
                this.player2 = item;
        }
    }

    public Pair<Integer, Integer> getAIAvatarPosition() {
        for (Pair<Integer, Integer> pair: player2UnitsPosition) {
            if (getBoard().getTile(pair).getUnit().isAvatar()) {
                return pair;
            }
        }
        return null;
    }

    public ArrayList<Pair<Integer, Integer>> getAllRangedUnits() {
        ArrayList<Pair<Integer, Integer>> output = new ArrayList<>();
        for (Pair<Integer, Integer> pair: player2UnitsPosition) {
            if (getBoard().getTile(pair).getUnit().isRanged()) {
                output.add(pair);
            }
        }
        return output;
    }

    public ArrayList<Pair<Integer, Integer>> getAllFlyingUnits() {
        ArrayList<Pair<Integer, Integer>> output = new ArrayList<>();
        for (Pair<Integer, Integer> pair: player2UnitsPosition) {
            if (getBoard().getTile(pair).getUnit().isFlying()) {
                output.add(pair);
            }
        }
        return output;
    }

    // This should only be used for unit testing.
    public void testSummon(Card card, Players player, Pair<Integer, Integer> position) {
        boolean flag = false;

        if (getTurn() != player) {
            endTurnClicked(null);
            flag = true;
        }


        Player myPlayer = getPlayer(player);
        myPlayer.setMana(9);

        ArrayList<Card> hand = getCardsInHand(player);
        hand.clear();
        hand.add(card);

        cardClicked(null, 0);
        getCardPlayed().moveCardToBoard(null, position.getFirst(), position.getSecond());

        if (flag) {
            endTurnClicked(null);
        }
    }

    @Override
    public Board getBoard() {
        return board;
    }

    @Override
    public String toString() {
        return "ExtractedGameState{" +
                "roundNumber=" + roundNumber +
                ", player1=" + player1 +
                ", player2=" + player2 +
                ", player1CardsInHand=" + player1CardsInHand +
                ", player2CardsInHand=" + player2CardsInHand +
                ", player1UnitsPosition=" + player1UnitsPosition +
                ", player2UnitsPosition=" + player2UnitsPosition +
                ", memento=" + memento +
                ", simulation=" + simulation +
                ", board=" + board +
                '}';
    }
}
