package structures;

/**
 * This class can be used to hold information about the on-going game.
 * Its created with the GameActor.
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class GameState {
    Players turn = Players.PLAYER1;     // TODO: This should not be initialised this way.
                                            // This is done this way for testing.
    boolean turnChanged = true;

    public void nextTurn() {
        if (turn == Players.PLAYER1) {
            turn = Players.PLAYER2;
        } else {
            turn = Players.PLAYER1;
        }
        turnChanged = true;
    }

    public Players getTurn() {
        return turn;
    }

    public boolean isTurnChanged() {
        boolean temp = turnChanged;
        turnChanged = false;
        return temp;
    }
}
