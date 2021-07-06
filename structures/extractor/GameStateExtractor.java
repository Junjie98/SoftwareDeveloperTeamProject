package structures.extractor;

import structures.Board;
import structures.GameState;
import structures.basic.Card;
import structures.handlers.Pair;
import structures.memento.GameMemento;

import java.util.ArrayList;

public class GameStateExtractor {
    private GameState parent;

    public GameStateExtractor(GameState parent) {
        this.parent = parent;
    }

    // ===========================================================================
    // Extractor
    // ===========================================================================
    public ExtractedGameState extract() {
        ExtractedGameState output = new ExtractedGameState();
        output.setSimulation(true);       // Simulation will be used to block draw board and prevent changes to affect the main GameState, including card drawing.

        output.setBoard(Board.getCopy());
        output.player1CardsInHand = cloneCardList(parent.player1CardsInHand);
        output.player2CardsInHand = cloneCardList(parent.player2CardsInHand);
        output.player1UnitsPosition = clonePairList(parent.player1UnitsPosition);
        output.player2UnitsPosition = clonePairList(parent.player2UnitsPosition);
        output.memento = cloneMementoList(parent.memento);

        output.setRoundNumber(parent.getRoundNumber());

        return output;
    }

    private ArrayList<Card> cloneCardList(ArrayList<Card> input) {
        ArrayList<Card> output = new ArrayList<>();
        for (Card card: input) {
            // If we will not change the content of the card, we will not need to clone it.
            output.add(card);
        }
        return output;
    }

    private ArrayList<Pair<Integer, Integer>> clonePairList(ArrayList<Pair<Integer, Integer>> input) {
        ArrayList<Pair<Integer, Integer>> output = new ArrayList<>();
        for (Pair<Integer, Integer> pair: input) {
            output.add(Pair.copyIntegerPair(pair));
        }
        return output;
    }

    private ArrayList<GameMemento> cloneMementoList(ArrayList<GameMemento> mementos) {
        ArrayList<GameMemento> output = new ArrayList<>();
        for (GameMemento memento: mementos) {
            output.add(memento);
        }
        return output;
    }

}
