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
    String cardname;
    private GameState parent;

    Pair<Card, Integer> activeCard = null;

    public CardPlayed(GameState parent) {
        this.parent = parent;
    }
    private UnitMovementAndAttack unitMovementAndAttack = new UnitMovementAndAttack(this.parent);


    public void moveCardToBoard(ActorRef out, int x, int y) {
        Card current = (parent.getTurn() == PLAYER1) ?
                parent.player1CardsInHand.get(activeCard.getSecond()) : parent.player2CardsInHand.get(activeCard.getSecond());
        this.cardname = current.getCardname();
        System.out.println(cardname);

        deleteCardFromHand(out, activeCard.getSecond());
        parent.decreaseManaPerCardPlayed(out, current.getManacost());
        parent.getHighlighter().clearBoardHighlights(out);

        if (current.isSpell()) {
            //Set the effect of the spell and call spellAction
            if (cardname.equals("Truestrike")) {    // Truestike does -1 damage to any
                // Highlight enemy units
                // Set a buff animation and the effects like this.
                new TileCommandBuilder(out)
                        .setMode(TileCommandBuilderMode.ANIMATION)
                        .setTilePosition(x, y)
                        .setEffectAnimation(TileEffectAnimation.INMOLATION)
                        .issueCommand();

                spellAction(out, x, y, -1);
            }

            if (cardname.equals("Entropic Decay")) {    //Reduces a non-avatar unit to 0 heath, rip
                // Highlight enemy units
                new TileCommandBuilder(out)
                        .setMode(TileCommandBuilderMode.ANIMATION)
                        .setTilePosition(x, y)
                        .setEffectAnimation(TileEffectAnimation.MARTYRDOM)
                        .issueCommand();

                spellAction(out, x, y, -20);
            }

            if (cardname.equals("Sundrop Elixir") || cardname.equals("Staff of Y'Kir'")) { //Staff +2 Attack for avatar,
                new TileCommandBuilder(out)
                        .setMode(TileCommandBuilderMode.ANIMATION)
                        .setTilePosition(x, y)
                        .setEffectAnimation(TileEffectAnimation.BUFF)
                        .issueCommand();
                if(cardname.equals("Sundrop Elixir")){
                    spellAction(out, x, y, 5);
                } else {
                    spellAction(out, x, y, 2);
                }
            }
        // if normal Unit
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

            try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();} // Unit will appear with effect

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
            }//Junjie: I suggest the else can be removed for code cleanup? Since setRange was false by default.
            //and only cards matched the if statement will be set to true? 

            //Junjie: Provoker
            if(cardname.equals("Silverguard Knight") || cardname.equals("Ironcliff Guardian") || cardname.equals("Rock Pulveriser")){
                unit.setProvoker(true);
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

    public void spellAction(ActorRef out, int x, int y, int strengthOfSpell) {
        Tile enemyLocation = Board.getInstance().getTile(x, y);
        Unit enemy = enemyLocation.getUnit();
        UnitCommandBuilder enemyCommandBuilder = new UnitCommandBuilder(out).setUnit(enemy);

        if(cardname.equals("Sundrop Elixir") || cardname.equals("Entropic Decay") || cardname.equals("Truestrike")) {
            int enemyHealth = enemy.getHealth();
            int healthAfterDamage = enemyHealth + strengthOfSpell;  //strengthOfSpell is negative if unit lose health
            if (healthAfterDamage < 0) { healthAfterDamage = 0; }

            enemyCommandBuilder
                    .setMode(UnitCommandBuilderMode.SET)
                    .setStats(UnitStats.HEALTH, healthAfterDamage)
                    .issueCommand();

            // delete unit if health <=0
            if (healthAfterDamage == 0) {
                deleteUnit(out, x, y, enemyLocation);
            }
        }else if (cardname.equals("Staff of Y'Kir'")){      //Avatar gains +2 Attack
            int enemyAvatarAttack = enemy.getDamage();
            System.out.println(enemyAvatarAttack);
            int attackAfterSpell = enemyAvatarAttack + strengthOfSpell;

            enemyCommandBuilder
                    .setMode(UnitCommandBuilderMode.SET)
                    .setStats(UnitStats.ATTACK, attackAfterSpell)
                    .issueCommand();
        }


        // update avatar health to UI player health.
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
    }

    public void deleteUnit(ActorRef out, int x, int y, Tile enemyLocation) {
        //Delete the enemy unit if dies
        try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();} // Unit will disappear with effect
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
}
