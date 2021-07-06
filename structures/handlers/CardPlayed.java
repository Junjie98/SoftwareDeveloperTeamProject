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
import java.util.HashMap;

import static commandbuilders.enums.Players.PLAYER1;

public class CardPlayed {
    String cardname;
    private GameState parent;
    private HashMap<Integer, Integer> unitsOriginalHealth = new HashMap<Integer, Integer>();
    Pair<Card, Integer> activeCard = null;
    public CardPlayed(GameState parent) { this.parent = parent; }
    private UnitMovementAndAttack unitMovementAndAttack = new UnitMovementAndAttack(this.parent);


    // ===========================================================================
    // Move card to the Board and logic behind it.
    // ===========================================================================
    public void moveCardToBoard(ActorRef out, int x, int y) {
        Card current = (parent.getTurn() == PLAYER1) ?
                parent.player1CardsInHand.get(activeCard.getSecond()) : parent.player2CardsInHand.get(activeCard.getSecond());
        this.cardname = current.getCardname();
        System.out.println(cardname);
        int tempCardToDelete = activeCard.getSecond();      //Need that if we want to have beautiful effects, and not a bug
        parent.getHighlighter().clearBoardHighlights(out);  // which decrease mana and deletes a card even when clicked at red tile

        if (current.isSpell()) {
            //Set the effect of the spell and call spellAction
            if (cardname.equals("Truestrike")) {    // Truestrike does -2 damage to any
                // Highlight enemy units
                // Set a buff animation and the effects like this.
                new TileCommandBuilder(out)
                        .setMode(TileCommandBuilderMode.ANIMATION)
                        .setTilePosition(x, y)
                        .setEffectAnimation(TileEffectAnimation.INMOLATION)
                        .issueCommand();

                spellAction(out, x, y, -2);
            }

            if (cardname.equals("Entropic Decay")) {    //Reduces a non-avatar unit to 0 heath, rip
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
                    //Calculate how much health the spell will recover based on the original health
                    Unit unit = Board.getInstance().getTile(x,y).getUnit();
                    int currentHealth = unit.getHealth();
                    int originalHealth = unitsOriginalHealth.get(unit.getId());
                    int gap = originalHealth - currentHealth;
                    int totalRecovery = (gap <= 5) ? gap : 5;
                    spellAction(out, x, y, totalRecovery);
                } else {
                    spellAction(out, x, y, 2);
                }
            }
        
        // If normal Unit
        } else {
        	
        	if (current.isSpecialCard())
        		specialAction(out, current, x, y);
        	
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

            unitsOriginalHealth.put(unit.getId(),current.getBigCard().getHealth());
//            System.out.println("the health of "+ unit +" is " + unitsOriginalHealth.get(unit.getId()));

            if (parent.getTurn() == PLAYER1) {
                parent.player1UnitsPosition.add(new Pair<>(x, y));
            } else {
                parent.player2UnitsPosition.add(new Pair<>(x, y));
            }
        }
        deleteCardFromHand(out, tempCardToDelete);      //these two have to take place at the end to avoid a bug.
        parent.decreaseManaPerCardPlayed(out, current.getManacost());

    }
    
    public void spellAction(ActorRef out, int x, int y, int strengthOfSpell) {
        Tile targetLocation = Board.getInstance().getTile(x, y);
        Unit target = targetLocation.getUnit();
        UnitCommandBuilder targetCommandBuilder = new UnitCommandBuilder(out).setUnit(target);

        if(cardname.equals("Sundrop Elixir") || cardname.equals("Entropic Decay") || cardname.equals("Truestrike")) {
            int targetHealth = target.getHealth();
            int healthAfterDamage = targetHealth + strengthOfSpell;  //strengthOfSpell is negative if unit lose health
            if (healthAfterDamage < 0) { healthAfterDamage = 0; }

            targetCommandBuilder
                    .setMode(UnitCommandBuilderMode.SET)
                    .setStats(UnitStats.HEALTH, healthAfterDamage)
                    .issueCommand();

            // delete unit if health <=0
            if (healthAfterDamage == 0) {
                deleteUnit(out, x, y, targetLocation);
            }
        }else if (cardname.equals("Staff of Y'Kir'")){      //Avatar gains +2 Attack
            int enemyAvatarAttack = target.getDamage();
            System.out.println(enemyAvatarAttack);
            int attackAfterSpell = enemyAvatarAttack + strengthOfSpell;

            targetCommandBuilder
                    .setMode(UnitCommandBuilderMode.SET)
                    .setStats(UnitStats.ATTACK, attackAfterSpell)
                    .issueCommand();
        }

        // update avatar health to UI player health.
        if(target.isAvatar() && target.getPlayerID() == Players.PLAYER1) {
            parent.getPlayer1().setHealth(target.getHealth());
            new PlayerSetCommandsBuilder(out)
                    .setPlayer(Players.PLAYER1)
                    .setStats(PlayerStats.HEALTH)
                    .setInstance(parent.getPlayer1())
                    .issueCommand();
        } else if(target.isAvatar() && target.getPlayerID()== Players.PLAYER2) {
            parent.getPlayer2().setHealth(target.getHealth());
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
    
    public void specialAction(ActorRef out, Card current, int x, int y) {
    	switch (current.getCardname()) {
    		case "Azure Herald":
    			boolean isPlayer1 = false;
    			if (parent.getTurn() == PLAYER1)
    				isPlayer1 = true;
    			
    			if (isPlayer1) {
    				int newHealth = parent.getPlayer1().getHealth() + 3;
    				if(newHealth > 20)
    					newHealth = 20;
	               parent.getPlayer1().setHealth(newHealth);
    			   new PlayerSetCommandsBuilder(out)
	                   .setPlayer(Players.PLAYER1)
	                   .setStats(PlayerStats.HEALTH)
	                   .setInstance(parent.getPlayer1())
	                   .issueCommand();
    			}
                else {
            		int newHealth = parent.getPlayer2().getHealth() + 3;
    				if(newHealth > 20)
    					newHealth = 20;
    				parent.getPlayer2().setHealth(newHealth);
                	new PlayerSetCommandsBuilder(out)
	                    .setPlayer(Players.PLAYER2)
	                    .setStats(PlayerStats.HEALTH)
	                    .setInstance(parent.getPlayer2())
	                    .issueCommand();
                }
    	}
    }

    // ===========================================================================
    // Delete Cards and Units
    // ===========================================================================
    public void deleteCardFromHand(ActorRef out, int pos) {
        ArrayList<Card> current = (parent.getTurn() == PLAYER1) ? parent.player1CardsInHand : parent.player2CardsInHand;
        current.remove(pos);
        parent.getCardDrawing().displayCardsOnScreenFor(out, parent.getTurn());
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

    // ===========================================================================
    // Setters, getters, and resetters
    // ===========================================================================

    public Pair<Card, Integer> getActiveCard() {
        return activeCard;
    }

    public void setActiveCard(Card activeCard, int idx) {
        this.activeCard = new Pair<>(activeCard, idx);
    }

    public void setUnitsOriginalHealth(int id, int health) {
        unitsOriginalHealth.put(id,health);
    }

    public void clearActiveCard() {
        this.activeCard = null;
    }
}
