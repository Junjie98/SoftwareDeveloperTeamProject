package structures;

import structures.basic.Tile;
import structures.basic.Unit;
import utils.BasicObjectBuilders;

public class Board {
    private Tile[][] board = new Tile[9][5];
    private static Board instance = new Board();

    private Board() {
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 5; y++) {
                board[x][y] = BasicObjectBuilders.loadTile(x, y);
            }
        }
    }

    public static Board getInstance() {
        return instance;
    }

    public Tile getTile(int x, int y) {
        return board[x][y];
    }

    public void setUnitToTile(Unit unit, int x, int y) {
        board[x][y].setUnit(unit);
    }

    public void removeUnitFromTile(int x, int y) {
        board[x][y].setUnit(null);
    }

    public static void reloadBoard() {
        instance = new Board();
    }
}
