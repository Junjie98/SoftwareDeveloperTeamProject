package structures.extractor;

import structures.Board;
import structures.GameState;

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

    @Override
    public Board getBoard() {
        return board;
    }

    @Override
    public String toString() {
        return "ExtractedGameState{" +
                "roundNumber=" + roundNumber +
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
