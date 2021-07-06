package structures.handlers;

import akka.actor.ActorRef;
import commandbuilders.TileCommandBuilder;
import commandbuilders.enums.States;
import commandbuilders.enums.TileCommandBuilderMode;
import structures.Board;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;

import java.util.ArrayList;

import static commandbuilders.enums.Players.PLAYER1;

public class Highlighter {
    private ArrayList<Tile> highlightedTiles = new ArrayList<>();
    private GameState parent;

    public Highlighter(GameState parent) {
        this.parent = parent;
    }

    public boolean checkTileHighlight(ActorRef out, Pair<Integer, Integer> pos)  {
        int x = pos.getFirst();
        int y = pos.getSecond();

        //limiting input
        if(x < 0 || x > 8) {
            return false;
        }
        if(y < 0 || y > 4) {
            return false;
        }

        Tile tile = Board.getInstance().getTile(x, y);

        if(!tile.hasUnit()) {
            // empty so highlight
            new TileCommandBuilder(out, parent.isSimulation())
                    .setTilePosition(pos.getFirst(), pos.getSecond())
                    .setState(States.HIGHLIGHTED)
                    .issueCommand();

            highlightedTiles.add(tile);
            tile.setTileState(States.HIGHLIGHTED);
            return true;
        } else {
            if(Board.getInstance().getTile(pos.getFirst(), pos.getSecond()).getUnit().getPlayerID() != parent.getTurn()) {
                // Tile has enemy
                new TileCommandBuilder(out, parent.isSimulation())
                        .setTilePosition(x, y)
                        .setState(States.RED)
                        .issueCommand();
                highlightedTiles.add(tile);
                tile.setTileState(States.RED);
            } else {
                // Tile has friendly
                new TileCommandBuilder(out, parent.isSimulation())
                        .setTilePosition(x, y)
                        .setState(States.NORMAL)
                        .issueCommand();
                highlightedTiles.add(tile);
                tile.setTileState(States.NORMAL);
            }
            return false;

        }
    }
    public void cardTileHighlight(ActorRef out, int x, int y) {
        // if spell card
        if (parent.getCardPlayed().getActiveCard().getFirst().getCardname().equals("Truestrike") || parent.getCardPlayed().getActiveCard().getFirst().getCardname().equals("Entropic Decay")) {
            ArrayList<Pair<Integer, Integer>> enemyUnits = (parent.getTurn() == PLAYER1) ?
                    parent.player2UnitsPosition : parent.player1UnitsPosition;
            if(parent.getCardPlayed().getActiveCard().getFirst().getCardname().equals("Entropic Decay")) {
                for (Pair<Integer, Integer> position : enemyUnits) {
                    Tile enemyLocation = Board.getInstance().getTile(position);
                    if (!enemyLocation.getUnit().isAvatar()) {
                        new TileCommandBuilder(out, parent.isSimulation())
                                .setTilePosition(position.getFirst(), position.getSecond())
                                .setState(States.RED)
                                .issueCommand();
                        Tile tile = Board.getInstance().getTile(position);
                        tile.setTileState(States.RED);
                        highlightedTiles.add(tile);
                    }
                }
            } else {        //if Truestike, highlight avatar & units
                for (Pair<Integer, Integer> position : enemyUnits) {
                    new TileCommandBuilder(out, parent.isSimulation())
                            .setTilePosition(position.getFirst(), position.getSecond())
                            .setState(States.RED)
                            .issueCommand();
                    Tile tile = Board.getInstance().getTile(position);
                    tile.setTileState(States.RED);
                    highlightedTiles.add(tile);
                }
            }
        } else if (parent.getCardPlayed().getActiveCard().getFirst().getCardname().equals("Sundrop Elixir") || parent.getCardPlayed().getActiveCard().getFirst().getCardname().equals("Staff of Y'Kir'")) {
            ArrayList<Pair<Integer, Integer>> friendlyUnits = (parent.getTurn() == PLAYER1) ?
                    parent.player1UnitsPosition : parent.player2UnitsPosition;
            if (parent.getCardPlayed().getActiveCard().getFirst().getCardname().equals("Staff of Y'Kir'")) {
                for (Pair<Integer, Integer> position : friendlyUnits) {
                    Tile friendlyLocation = Board.getInstance().getTile(position);
                    if (friendlyLocation.getUnit().isAvatar()) {
                        new TileCommandBuilder(out, parent.isSimulation())
                                .setTilePosition(position.getFirst(), position.getSecond())
                                .setState(States.HIGHLIGHTED)
                                .issueCommand();
                        Tile tile = Board.getInstance().getTile(position);
                        tile.setTileState(States.HIGHLIGHTED);
                        highlightedTiles.add(tile);
                    }
                }
            } else {    //if card Sundrop Elixir then should highlight all units & avatar
                for (Pair<Integer, Integer> position : friendlyUnits) {
                    new TileCommandBuilder(out, parent.isSimulation())
                            .setTilePosition(position.getFirst(), position.getSecond())
                            .setState(States.HIGHLIGHTED)
                            .issueCommand();
                    Tile tile = Board.getInstance().getTile(position);
                    tile.setTileState(States.HIGHLIGHTED);
                    highlightedTiles.add(tile);
                }
            }
        //if normal non-spell card
		} else {
            ArrayList<Pair<Integer, Integer>> initDir = parent.getMoveTiles(x, y, 1, 0);
            ArrayList<Pair<Integer, Integer>> interDir = parent.getMoveTiles(x, y, 1, 1);
            boolean[] initDirB = {true, true, true, true};

            int count = 0;
            for (Pair<Integer, Integer> is: initDir) {
                initDirB[count] = parent.getHighlighter().checkTileHighlight(out, is);
                count++;
            }

            if (initDirB[0] == true || initDirB[1] == true) {
                checkTileHighlight(out, interDir.get(0));
            }
            if (initDirB[1] == true || initDirB[3] == true) {
                checkTileHighlight(out, interDir.get(1));
            }
            if (initDirB[2] == true || initDirB[0] == true) {
                checkTileHighlight(out, interDir.get(2));
            }
            if (initDirB[2] == true || initDirB[3] == true) {
                checkTileHighlight(out, interDir.get(3));
            }
        }
    }

    public void clearBoardHighlights(ActorRef out) {
        parent.getUnitMovementAndAttack().setActiveUnit(null);
        parent.getCardPlayed().clearActiveCard();
        for (Tile tile: highlightedTiles) {
            new TileCommandBuilder(out, parent.isSimulation())
                    .setTilePosition(tile.getTilex(), tile.getTiley())
                    .setState(States.NORMAL)
                    .setMode(TileCommandBuilderMode.DRAW)
                    .issueCommand();
            tile.setTileState(States.NORMAL);
        }
        highlightedTiles.clear();

    }
}
