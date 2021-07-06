package structures;

import commandbuilders.UnitFactory;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.handlers.Pair;
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

    private Board(boolean copy) { }

    public static Board getInstance() {
        return instance;
    }

    public Tile getTile(int x, int y) {
        if ((x < 0 || y < 0) || (x > 8 || y > 8)) {
            return null;
        }

        return board[x][y];
    }

    public Tile getTile(Pair<Integer, Integer> position) {
        return getTile(position.getFirst(), position.getSecond());
    }
    public void setUnitToTile(Unit unit, int x, int y) {
        board[x][y].setUnit(unit);
    }

    public static void reloadBoard() {
        instance = new Board();
    }
    public static Board getCopy() {
        Board copy = new Board();
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 5; y++) {
                if (instance.board[x][y].hasUnit()) {
                    Unit cp = new UnitFactory().generateUnitByUnitConfig(instance.board[x][y].getUnit());
                    copy.board[x][y].setUnit(cp);
                }
            }
        }
        return copy;
    }
}
