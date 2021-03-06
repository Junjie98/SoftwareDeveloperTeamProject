package structures.handlers;

import akka.actor.ActorRef;

/**
 * This class consists of the logic related to events after a card 
 * has been played.
 * Implementation of the actions that take place after a Card
 * and a Tile are selected in order for the card to move the the board
 * and what actions should trigger based in what type of card it is.
 * @author Anamika Maurya (2570847M@student.gla.ac.uk)
 * @author Theodoros Vrakas (2593566v@student.gla.ac.uk)
 * @author William T Manson (2604495m@student.gla.ac.uk)
 * @author Jun Jie Low (2600104L@student.gla.ac.uk/nelsonlow_88@hotmail.com)
 */

import commandbuilders.*;
import commandbuilders.enums.*;
import structures.GameState;
import structures.basic.Card;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.memento.ActionType;
import structures.memento.GameMemento;
import structures.memento.SpellInformation;
import structures.memento.SummonInformation;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Handling moving cards to board, including summon and spell
 *
 * @author Dr. Richard McCreadie
 * @author Theodoros Vrakas (2593566v@student.gla.ac.uk)
 * @author William T Manson (2604495m@student.gla.ac.uk)
 * @author Anamika Maurya (2570847M@student.gla.ac.uk)
 * @author Yu-Sung Hsu (2540296h@student.gla.ac.uk)
 * @author Jun Jie Low (2600104L@student.gla.ac.uk/nelsonlow_88@hotmail.com)
 */

public class CardPlayed {
    String cardname;
    private final GameState parent;
    private HashMap<Integer, Integer> unitsOriginalHealth = new HashMap<>();
    Pair<Card, Integer> activeCard = null;
    public CardPlayed(GameState parent) { this.parent = parent; }

    /**
     * This method moves a card to board.
     *
     * Move card to the Board and logic behind it. Considers if the card
     * is a spell card or a normal Unit card for different actions.
     *
     * @author Theodoros Vrakas (2593566v@student.gla.ac.uk)
     * @author Anamika Maurya (2570847M@student.gla.ac.uk)
     */
    public void moveCardToBoard(ActorRef out, int x, int y) {
        Card current = parent.getCardsInHand(parent.getTurn()).get(activeCard.getSecond());
        this.cardname = current.getCardname();
        System.out.println("Card's name is " + cardname);
        int tempCardToDelete = activeCard.getSecond();      //Need that if we want to have beautiful effects, and not a bug
        parent.getHighlighter().clearBoardHighlights(out);  // which decrease mana and deletes a card even when clicked at red tile

        if (current.isSpell()) {
        	
        	// Checking if enemy casted a spell for Pureblade Enforcer
        	ArrayList<Pair<Integer, Integer>> enemyUnits = parent.getEnemyUnitsPosition(parent.getTurn());
			for (Pair<Integer, Integer> position : enemyUnits) {
                Tile enemyLocation = parent.getBoard().getTile(position);
              
                if(enemyLocation.getUnit().getType() == UnitType.PUREBLADE_ENFORCER) {
                	int newHealth = enemyLocation.getUnit().getHealth() + 1;
                	int newDamage = enemyLocation.getUnit().getDamage() + 1;
                	enemyLocation.getUnit().setHealth(enemyLocation.getUnit().getHealth() + 1);
                	enemyLocation.getUnit().setDamage(enemyLocation.getUnit().getDamage() + 1);
                	
                	new UnitCommandBuilder(out, parent.isSimulation())
                    .setMode(UnitCommandBuilderMode.SET)
                    .setUnit(enemyLocation.getUnit())
                    .setStats(UnitStats.HEALTH, newHealth)
                    .issueCommand();

                    new UnitCommandBuilder(out, parent.isSimulation())
                    .setMode(UnitCommandBuilderMode.SET)
                    .setUnit(enemyLocation.getUnit())
                    .setStats(UnitStats.ATTACK, newDamage)
                    .issueCommand();

                }
            }
        	
        	
            //Set the effect of the spell and call spellAction
            Unit targetUnit = parent.getBoard().getTile(x, y).getUnit();
            if (cardname.equals("Truestrike")) {    // Truestike does -2 damage to any
                // Highlight enemy units
                // Set a buff animation and the effects like this.
                new TileCommandBuilder(out, parent.isSimulation())
                        .setMode(TileCommandBuilderMode.ANIMATION)
                        .setTilePosition(x, y)
                        .setEffectAnimation(TileEffectAnimation.INMOLATION)
                        .issueCommand();

                spellAction(out, x, y, -2);
            }

            if (cardname.equals("Entropic Decay")) {    //Reduces a non-avatar unit to 0 heath, rip
                new TileCommandBuilder(out, parent.isSimulation())
                        .setMode(TileCommandBuilderMode.ANIMATION)
                        .setTilePosition(x, y)
                        .setEffectAnimation(TileEffectAnimation.MARTYRDOM)
                        .issueCommand();

                spellAction(out, x, y, -20);
            }

            if (cardname.equals("Sundrop Elixir") || cardname.equals("Staff of Y'Kir'")) { //Staff +2 Attack for avatar,
                new TileCommandBuilder(out, parent.isSimulation())
                        .setMode(TileCommandBuilderMode.ANIMATION)
                        .setTilePosition(x, y)
                        .setEffectAnimation(TileEffectAnimation.BUFF)
                        .issueCommand();

                if(cardname.equals("Sundrop Elixir")){
                    //Calculate how much health the spell will recover based on the original health
                    Unit unit = parent.getBoard().getTile(x,y).getUnit();
                    int currentHealth = unit.getHealth();
                    int originalHealth = unitsOriginalHealth.get(unit.getId());
                    int gap = originalHealth - currentHealth;
                    int totalRecovery = Math.min(gap, 5);
                    spellAction(out, x, y, totalRecovery);
                } else {
                    spellAction(out, x, y, 2);
                }
            }
            // Track this action in the memento.
            parent.memento.add(new GameMemento(parent.getTurn(), ActionType.SPELL, new SpellInformation(targetUnit, new Pair<>(x, y), current)));
        // If normal Unit
        } else {
            Tile tile = parent.getBoard().getTile(x, y);
            if (tile.hasUnit()) {
                // Cannot deal a card to a Tile that has unit.
                return;
            }

            //Play summon effect each time a player cast a card
            new TileCommandBuilder(out, parent.isSimulation())
                    .setMode(TileCommandBuilderMode.ANIMATION)
                    .setTilePosition(x, y)
                    .setEffectAnimation(TileEffectAnimation.SUMMON)
                    .issueCommand();

            if (out != null) {
                try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();} // Unit will appear with effect
            }



            Unit unit = new UnitFactory().generateUnitByCard(current);
            unit.setFlying(cardname.equals("WindShrike"));
            // Ana: Ranged Attack
            unit.setRanged(cardname.equals("Pyromancer") || cardname.equals("Fire Spitter"));
            // set to true
            /////////////////////////////////////////////////////////////////////////////////////////

            /**
            * set selected card as provoker.
            * @author Jun Jie Low (2600104L@student.gla.ac.uk/nelsonlow_88@hotmail.com)
            */
            unit.setProvoker((cardname.equals("Silverguard Knight") || cardname.equals("Ironcliff Guardian") || cardname.equals("Rock Pulveriser")));
            ////////////////////////////////////////////////////////////////////////////////////////
            if (unit.getType() == UnitType.AZURITE_LION || unit.getType().equals(UnitType.SERPENTI))
                unit.setAttackLimit(2);

            unit.setPlayerID(parent.getTurn());
            tile.setUnit(unit);
            unit.setPositionByTile(tile);
        
            parent.getSpecialAbilities().unitIsSummoned(out, unit);

            UnitCommandBuilder builder = new UnitCommandBuilder(out, parent.isSimulation())
                    .setUnit(unit);

            builder.setMode(UnitCommandBuilderMode.DRAW)
                    .setTilePosition(x, y)
                    .setPlayerID(parent.getTurn())
                    .issueCommand();

            if (out != null) {
                try {Thread.sleep(30);} catch (InterruptedException e) {e.printStackTrace();}
            }

            unit.setHealth(current.getBigCard().getHealth());
            unit.setDamage(current.getBigCard().getAttack());
            //updates the UI from bigCard stats
            builder.setMode(UnitCommandBuilderMode.SET)
                    .setStats(UnitStats.HEALTH, current.getBigCard().getHealth())
                    .issueCommand();

            if (out != null) {
                try {Thread.sleep(30);} catch (InterruptedException e) {e.printStackTrace();}
            }

            builder.setMode(UnitCommandBuilderMode.SET)
                    .setStats(UnitStats.ATTACK, current.getBigCard().getAttack())
                    .issueCommand();

            unitsOriginalHealth.put(unit.getId(),current.getBigCard().getHealth());
//            System.out.println("the health of "+ unit +" is " + unitsOriginalHealth.get(unit.getId()));

            parent.getUnitsPosition(parent.getTurn()).add(new Pair<>(x, y));

            parent.memento.add(new GameMemento(parent.getTurn(), ActionType.SUMMON, new SummonInformation(new Pair<>(x, y), unit)));
        }
        deleteCardFromHand(out, tempCardToDelete);      //these two have to take place at the end to avoid a bug.
        parent.decreaseManaPerCardPlayed(out, current.getManacost());

    }

    // Include the actions that takes place by the Spell Cards.
    // @author Theodoros Vrakas (2593566v@student.gla.ac.uk)
    public void spellAction(ActorRef out, int x, int y, int strengthOfSpell) {
        Tile targetLocation = parent.getBoard().getTile(x, y);
        Unit target = targetLocation.getUnit();
        UnitCommandBuilder targetCommandBuilder = new UnitCommandBuilder(out, parent.isSimulation()).setUnit(target);

        if(cardname.equals("Sundrop Elixir") || cardname.equals("Entropic Decay") || cardname.equals("Truestrike")) {
            int targetHealth = target.getHealth();
            int healthAfterDamage = targetHealth + strengthOfSpell;  //strengthOfSpell is negative if unit lose health
            if (healthAfterDamage < 0) { healthAfterDamage = 0; }

            targetCommandBuilder
                    .setMode(UnitCommandBuilderMode.SET)
                    .setStats(UnitStats.HEALTH, healthAfterDamage)
                    .issueCommand();

            target.setHealth(healthAfterDamage);

            // delete unit if health <=0
            if (healthAfterDamage == 0) {
                parent.unitDied(out, targetLocation, parent.getEnemyUnitsPosition(parent.getTurn()));
            }
        }else if (cardname.equals("Staff of Y'Kir'")){      //Avatar gains +2 Attack
            int friendlyAvatarAttack = target.getDamage();
            int attackAfterSpell = friendlyAvatarAttack + strengthOfSpell;

            targetCommandBuilder
                    .setMode(UnitCommandBuilderMode.SET)
                    .setStats(UnitStats.ATTACK, attackAfterSpell)
                    .issueCommand();

            target.setDamage(attackAfterSpell);
        }

         ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
         /**
          * This is just to keep the board health linked with the avatar's health on the side of the board.
          * @author Jun Jie Low (2600104L@student.gla.ac.uk/nelsonlow_88@hotmail.com)
          */
        // update avatar health to UI player health.
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        if(target.isAvatar() && target.getPlayerID() == Players.PLAYER1) {

            parent.getPlayer(Players.PLAYER1).setHealth(target.getHealth());
            new PlayerSetCommandsBuilder(out, parent.isSimulation())
                    .setPlayer(Players.PLAYER1)
                    .setStats(PlayerStats.HEALTH)
                    .setInstance(parent.getPlayer(Players.PLAYER1))
                    .issueCommand();
        } else if(target.isAvatar() && target.getPlayerID()== Players.PLAYER2) {
            parent.getPlayer(Players.PLAYER2).setHealth(target.getHealth());
            new PlayerSetCommandsBuilder(out, parent.isSimulation())
                    .setPlayer(Players.PLAYER2)
                    .setStats(PlayerStats.HEALTH)
                    .setInstance(parent.getPlayer(Players.PLAYER2))
                    .issueCommand();
        }

        // Win condition: should be moved to a method where we are checking player's health
        for (Players player: Players.values()) {
            if (parent.getPlayer(player).getHealth() < 1) {
                parent.endGame(out);
            }
        }
    }

    // ===========================================================================
    // Delete Cards and Units
    // ===========================================================================
    // @author Theodoros Vrakas (2593566v@student.gla.ac.uk)
    public void deleteCardFromHand(ActorRef out, int pos) {
        ArrayList<Card> current = parent.getCardsInHand(parent.getTurn());
        current.remove(pos);
        parent.getCardDrawing().displayCardsOnScreenFor(out, parent.getTurn());
        parent.currentHighlightedCard = null;
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
