package structures;

/**
 * This class can be used to hold information about the on-going game.
 * Its created with the GameActor.
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class GameState {
    private Players turn = Players.PLAYER1;

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
}
