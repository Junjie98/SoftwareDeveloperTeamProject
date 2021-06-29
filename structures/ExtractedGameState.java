package structures;

public class ExtractedGameState extends GameState {
    public ExtractedGameState() {
        super();
        board = Board.getCopy();
    }
}
