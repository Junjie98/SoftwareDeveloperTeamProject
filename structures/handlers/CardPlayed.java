package structures.handlers;

import akka.actor.ActorRef;
import commandbuilders.PlayerNotificationCommandBuilder;
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
    private GameState parent;

    Pair<Card, Integer> activeCard = null;

    public CardPlayed(GameState parent) {
        this.parent = parent;
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
            Tile tile = Board.getInstance().getTile(x, y);
            if (tile.hasUnit()) {
                // Cannot deal a card to a Tile that has unit.
                return;
            }

            Unit unit = new UnitFactory().generateUnitByCard(current);
            if (cardname.equals("WindShrike")) {
                unit.setFlying(true);
            } else {
                unit.setFlying(false);
            }
            
            // Ana: Ranged Attack
            if(cardname.equals("Pyromancer") || cardname.equals("Fire Spitter")) {
                unit.setRanged(true);
            } else {
                unit.setRanged(false);
            }
            
            new UnitCommandBuilder(out)
                    .setMode(UnitCommandBuilderMode.DRAW)
                    .setTilePosition(x, y)
                    .setPlayerID(parent.getTurn())
                    .setUnit(unit)
                    .issueCommand();
            
            //updates the UI from bigCard stats
            new UnitCommandBuilder(out)
            .setMode(UnitCommandBuilderMode.SET)
            .setUnit(unit)
            .setStats(UnitStats.HEALTH, current.getBigCard().getHealth())
            .issueCommand();
            
            new UnitCommandBuilder(out)
            .setMode(UnitCommandBuilderMode.SET)
            .setUnit(unit)
            .setStats(UnitStats.ATTACK, current.getBigCard().getAttack())
            .issueCommand();
            

            if (parent.getTurn() == PLAYER1) {
                parent.player1UnitsPosition.add(new Pair<>(x, y));
            } else {
                parent.player2UnitsPosition.add(new Pair<>(x, y));
            }
        }

        deleteCardFromHand(out, activeCard.getSecond());
        parent.decreaseManaPerCardPlayed(out, current.getManacost());
        parent.getHighlighter().clearBoardHighlights(out);
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
