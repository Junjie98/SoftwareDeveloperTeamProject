package structures.handlers;

import akka.actor.ActorRef;
import commandbuilders.TileCommandBuilder;
import commandbuilders.UnitCommandBuilder;
import commandbuilders.UnitFactory;
import commandbuilders.enums.*;
import structures.Board;
import structures.GameState;
import structures.basic.Card;
import structures.basic.Tile;
import structures.basic.Unit;

import java.util.ArrayList;

import static commandbuilders.enums.Players.PLAYER1;

public class CardPlayed {
    GameState parent;

    Pair<Card, Integer> activeCard = null;

    public CardPlayed(GameState parent) {
        this.parent = parent;
    }

    public void cardTileHighlight(ActorRef out, int x, int y) {
        if (activeCard.getFirst().getCardname().equals("Truestrike") || activeCard.getFirst().getCardname().equals("Entropic Decay")) {
            ArrayList<Pair<Integer, Integer>> enemyUnits = (parent.getTurn() == PLAYER1) ?
                    parent.player2UnitsPosition : parent.player1UnitsPosition;
            for (Pair<Integer, Integer> position: enemyUnits) {
                // TODO: Move this to the highlighter to do the store
                new TileCommandBuilder(out)
                        .setTilePosition(position.getFirst(), position.getSecond())
                        .setState(States.RED)
                        .issueCommand();
            }
        } else if (activeCard.getFirst().getCardname().equals("Sundrop Elixir") || activeCard.getFirst().getCardname().equals("Staff of Y'Kir'")) {
            ArrayList<Pair<Integer, Integer>> friendlyUnits = (parent.getTurn() == PLAYER1) ?
                    parent.player1UnitsPosition : parent.player2UnitsPosition;
            for (Pair<Integer, Integer> position: friendlyUnits) {
                new TileCommandBuilder(out)
                        .setTilePosition(position.getFirst(), position.getSecond())
                        .setState(States.HIGHLIGHTED)
                        .issueCommand();
            }
        } else {
            ArrayList<Pair<Integer, Integer>> initDir = parent.getMoveTiles(x, y, 1, 0);
            ArrayList<Pair<Integer, Integer>> interDir = parent.getMoveTiles(x, y, 1, 1);
            boolean[] initDirB = {true, true, true, true};

            int count = 0;
            for (Pair<Integer, Integer> is: initDir) {
                initDirB[count] = parent.checkTileHighlight(out, is);
                count++;
            }

            if (initDirB[0] == true || initDirB[1] == true) {
                parent.checkTileHighlight(out, interDir.get(0));
            }
            if (initDirB[1] == true || initDirB[3] == true) {
                parent.checkTileHighlight(out, interDir.get(1));
            }
            if (initDirB[2] == true || initDirB[0] == true) {
                parent.checkTileHighlight(out, interDir.get(2));
            }
            if (initDirB[2] == true || initDirB[3] == true) {
                parent.checkTileHighlight(out, interDir.get(3));
            }
        }
    }
    public void moveCardToBoard(ActorRef out, int x, int y) {
        Card current = (parent.getTurn() == PLAYER1) ?
                parent.player1CardsInHand.get(activeCard.getSecond()) : parent.player2CardsInHand.get(activeCard.getSecond());
        String cardname = current.getCardname();
        System.out.println(cardname);

        if (current.isSpell()) {
            if (cardname.equals("Truestrike") || cardname.equals("Entropic Decay")) {
                // Set a buff animation and the effects like this.
                new TileCommandBuilder(out)
                        .setMode(TileCommandBuilderMode.ANIMATION)
                        .setTilePosition(x, y)
                        .setEffectAnimation(TileEffectAnimation.INMOLATION)
                        .issueCommand();

            }
            if (cardname.equals("Sundrop Elixir") || cardname.equals("Staff of Y'Kir'")) {
                // Highlight friendly units
                // After player selected a square to play highlight. // I did the highlight on cardTileHighlight
                // Set a buff animation and the effects like this.
                new TileCommandBuilder(out)
                        .setMode(TileCommandBuilderMode.ANIMATION)
                        .setTilePosition(x, y)
                        .setEffectAnimation(TileEffectAnimation.MARTYRDOM) //<- Choose your animation here
                        .issueCommand();
            }
        } else {
            Unit unit = new UnitFactory().generateUnitByCard(current);
            unit.setFlying(true);
            new UnitCommandBuilder(out)
                    .setMode(UnitCommandBuilderMode.DRAW)
                    .setTilePosition(x, y)
                    .setPlayerID(parent.getTurn())
                    .setUnit(unit)
                    .issueCommand();

            if (parent.getTurn() == PLAYER1) {
                parent.player1UnitsPosition.add(new Pair<>(x, y));
            } else {
                parent.player2UnitsPosition.add(new Pair<>(x, y));
            }

//            Tile tile = Board.getInstance().getTile(x, y);
//            tile.setUnit(unit);
        }
        deleteCardFromHand(out, activeCard.getSecond());
        parent.clearBoardHighlights(out);
    }

    public void deleteCardFromHand(ActorRef out, int pos) {
        ArrayList<Card> current = (parent.getTurn() == PLAYER1) ? parent.player1CardsInHand : parent.player2CardsInHand;
        current.remove(pos);
        parent.getCardDrawing().displayCardsOnScreenFor(out, parent.getTurn());
    }

    public Pair<Card, Integer> getActiveCard() {
        return activeCard;
    }

    public void setActiveCard(Card activeCard, int idx) {
        this.activeCard = new Pair<>(activeCard, idx);
    }

    public void clearActiveCard() {
        this.activeCard = null;
    }
}
