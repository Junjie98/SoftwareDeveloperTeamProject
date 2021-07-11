package structures.extractor;

import commandbuilders.enums.Players;
import structures.Board;
import structures.GameState;
import structures.basic.Card;
import structures.basic.Player;
import structures.handlers.Pair;
import structures.memento.GameMemento;

import java.util.ArrayList;

public class GameStateExtractor {
    private final GameState parent;

    public GameStateExtractor(GameState parent) {
        this.parent = parent;
    }

    // ===========================================================================
    // Extractor
    // ===========================================================================
    public ExtractedGameState extract() {
        ExtractedGameState output = new ExtractedGameState();
        output.setSimulation(true);       // Simulation will be used to block draw board and prevent changes to affect the main GameState, including card drawing.

        output.setBoard(parent.getBoard().getCopy());
        System.out.println("Extracted gamestate time: ");
        System.out.println(output.getBoard().scanForUnits());
        output.player1CardsInHand = cloneCardList(parent.player1CardsInHand);
        output.player2CardsInHand = cloneCardList(parent.player2CardsInHand);
        output.player1UnitsPosition = clonePairList(parent.player1UnitsPosition);
        output.player2UnitsPosition = clonePairList(parent.player2UnitsPosition);
        output.memento = cloneMementoList(parent.memento);

        output.setRoundNumber(parent.getRoundNumber());
        output.setPlayer(Players.PLAYER1, clonePlayer(parent.getPlayer(Players.PLAYER1)));
        output.setPlayer(Players.PLAYER2, clonePlayer(parent.getPlayer(Players.PLAYER2)));

        output.setTurn(parent.getTurn());

        return output;
    }

    private ArrayList<Card> cloneCardList(ArrayList<Card> input) {
        return new ArrayList<>(input);
    }

    private ArrayList<Pair<Integer, Integer>> clonePairList(ArrayList<Pair<Integer, Integer>> input) {
        ArrayList<Pair<Integer, Integer>> output = new ArrayList<>();
        for (Pair<Integer, Integer> pair: input) {
            output.add(Pair.copyIntegerPair(pair));
        }
        return output;
    }

    private Player clonePlayer(Player player) {
        return new Player(player.getHealth(), player.getMana());
    }

    private ArrayList<GameMemento> cloneMementoList(ArrayList<GameMemento> mementos) {
        return new ArrayList<>(mementos);
    }
}
