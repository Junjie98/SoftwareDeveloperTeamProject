package structures.handlers;

import akka.actor.ActorRef;
import commandbuilders.*;
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
    private UnitMovementAndAttack unitMovementAndAttack = new UnitMovementAndAttack(this.parent);


    public void moveCardToBoard(ActorRef out, int x, int y) {
        Card current = (parent.getTurn() == PLAYER1) ?
                parent.player1CardsInHand.get(activeCard.getSecond()) : parent.player2CardsInHand.get(activeCard.getSecond());
        String cardname = current.getCardname();
        System.out.println(cardname);

        if (current.isSpell()) {
            if (cardname.equals("Truestrike")) {
                // Highlight enemy units
                // Set a buff animation and the effects like this.
                new TileCommandBuilder(out)
                        .setMode(TileCommandBuilderMode.ANIMATION)
                        .setTilePosition(x, y)
                        .setEffectAnimation(TileEffectAnimation.INMOLATION)
                        .issueCommand();

                spellAttack(out, x, y, -1);

            }
            if (cardname.equals("Entropic Decay")) {
                // Highlight enemy units
                new TileCommandBuilder(out)
                        .setMode(TileCommandBuilderMode.ANIMATION)
                        .setTilePosition(x, y)
                        .setEffectAnimation(TileEffectAnimation.MARTYRDOM) //<- Choose your animation here
                        .issueCommand();
                spellAttack(out, x, y, -20);

            }

            if (cardname.equals("Sundrop Elixir") || cardname.equals("Staff of Y'Kir'")) {
                // Highlight friendly units
                // After player selected a square to play highlight. // I did the highlight on cardTileHighlight
                // Set a buff animation and the effects like this.
                new TileCommandBuilder(out)
                        .setMode(TileCommandBuilderMode.ANIMATION)
                        .setTilePosition(x, y)
                        .setEffectAnimation(TileEffectAnimation.BUFF) //<- Choose your animation here
                        .issueCommand();

                spellAttack(out, x, y, 2);

            }

        } else {
            Tile tile = Board.getInstance().getTile(x, y);
            if (tile.hasUnit()) {
                // Cannot deal a card to a Tile that has unit.
                return;
            }

            //Play summon effect each time a player cast a card
            new TileCommandBuilder(out)
                    .setMode(TileCommandBuilderMode.ANIMATION)
                    .setTilePosition(x, y)
                    .setEffectAnimation(TileEffectAnimation.SUMMON)
                    .issueCommand();

            Unit unit = new UnitFactory().generateUnitByCard(current);
            if (cardname.equals("WindShrike")) {
                unit.setFlying(true);
            } else {
                unit.setFlying(false);
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

    public int spellAttack(ActorRef out, int x, int y, int strengthOfSpell) {
        Tile enemyLocation = Board.getInstance().getTile(x, y);
        Unit enemy = enemyLocation.getUnit();
        UnitCommandBuilder enemyCommandBuilder = new UnitCommandBuilder(out).setUnit(enemy);
        int enemyHealth = enemy.getHealth();
        int healthAfterDamage = enemyHealth + strengthOfSpell;  //strengthOfSpell is negative if unit lose health

        if (healthAfterDamage < 0)
            healthAfterDamage = 0;

        // TODO: Do this when far away only. Use a normal attack animation if close.



        enemyCommandBuilder
                .setMode(UnitCommandBuilderMode.SET)
                .setStats(UnitStats.HEALTH, healthAfterDamage)
                .issueCommand();


        //update avatar health to UI player health.
        if(enemy.isAvatar() && enemy.getPlayerID() == Players.PLAYER1) {
            parent.getPlayer1().setHealth(enemy.getHealth());
            new PlayerSetCommandsBuilder(out)
                    .setPlayer(Players.PLAYER1)
                    .setStats(PlayerStats.HEALTH)
                    .setInstance(parent.getPlayer1())
                    .issueCommand();
        } else if(enemy.isAvatar() && enemy.getPlayerID()== Players.PLAYER2) {
            parent.getPlayer2().setHealth(enemy.getHealth());
            new PlayerSetCommandsBuilder(out)
                    .setPlayer(Players.PLAYER2)
                    .setStats(PlayerStats.HEALTH)
                    .setInstance(parent.getPlayer2())
                    .issueCommand();
        }

        //update avatar health to UI player health.
        if(enemy.isAvatar() && enemy.getPlayerID() == Players.PLAYER1) {
            parent.getPlayer1().setHealth(enemy.getHealth());
            new PlayerSetCommandsBuilder(out)
                    .setPlayer(Players.PLAYER1)
                    .setStats(PlayerStats.HEALTH)
                    .setInstance(parent.getPlayer1())
                    .issueCommand();
        } else if(enemy.isAvatar() && enemy.getPlayerID()== Players.PLAYER2) {
            parent.getPlayer2().setHealth(enemy.getHealth());
            new PlayerSetCommandsBuilder(out)
                    .setPlayer(Players.PLAYER2)
                    .setStats(PlayerStats.HEALTH)
                    .setInstance(parent.getPlayer2())
                    .issueCommand();
        }

        // Win condition: should be moved to a method where we are checking player's health
        if (parent.getPlayer1().getHealth() < 1 || parent.getPlayer2().getHealth() < 1) {
            parent.endGame(out);
        }

        if(healthAfterDamage == 0) {
            new UnitCommandBuilder(out)
                    .setMode(UnitCommandBuilderMode.DELETE)
                    .setUnit(enemyLocation.getUnit())
                    .issueCommand();
            enemyLocation.setUnit(null);
            ArrayList<Pair<Integer, Integer>> pool = (parent.getTurn() == Players.PLAYER1) ?
                    parent.player2UnitsPosition : parent.player1UnitsPosition;
            Pair<Integer, Integer> positionToRemove = new Pair<>(x, y);
            for (Pair<Integer, Integer> position: pool) {
                if (position.equals(positionToRemove)) {
                    pool.remove(position);
                    break;
                }
            }
        }

        return enemy.getHealth();
    }
}
