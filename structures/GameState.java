package structures;

import java.util.ArrayList;
import akka.actor.ActorRef;
import commandbuilders.*;
import commandbuilders.enums.*;
import structures.basic.*;
import structures.extractor.GameStateExtractor;
import structures.handlers.*;
import structures.memento.GameMemento;

/**
 * This class can be used to hold information about the on-going game.
 * Its created with the GameActor.
 * 
 * @author Dr. Richard McCreadie
 * @author Anamika Maurya (2570847M@student.gla.ac.uk)
 * @author Yu-Sung Hsu (2540296h@student.gla.ac.uk)
 */

public class GameState {
    protected int roundNumber = 3;
    private Players turn = Players.PLAYER1;
    protected Player player1, player2;
    protected Unit human, aiAvatar;
    public Pair<Card, Integer> currentHighlightedCard;
    public ArrayList<Card> player1CardsInHand = new ArrayList<>();
    public ArrayList<Card> player2CardsInHand = new ArrayList<>();
    public ArrayList<Pair<Integer, Integer>> player1UnitsPosition = new ArrayList<>();
    public ArrayList<Pair<Integer, Integer>> player2UnitsPosition = new ArrayList<>();
    public ArrayList<GameMemento> memento = new ArrayList<>();

    protected boolean simulation = false;
    final private GameStateExtractor extractor = new GameStateExtractor(this);
    final private SmartBoy smartBoy = new SmartBoy(this);

    // ===========================================================================
    // Handler Classes
    // ===========================================================================
    final private UnitMovementAndAttack unitMovementAndAttack = new UnitMovementAndAttack(this);
    final private CardDrawing cardDrawing = new CardDrawing(this);
    final private CardPlayed cardPlayed = new CardPlayed(this);
    final private Highlighter highlighter = new Highlighter(this);
    final private SpecialAbilities specialAbilities = new SpecialAbilities(this);

	// ===========================================================================
    // Game Initialisation
    // ===========================================================================
    //Creates the two players
    public void generateTwoUsers(ActorRef out) {
    	player1 = new Player(20,2); //set players health and mana to 20
        player2 = new Player(20,2); //player start with 2 mana in round 1.

        new PlayerSetCommandsBuilder(out, isSimulation())
                .setPlayer(Players.PLAYER1)
                .setStats(PlayerStats.ALL)
                .setInstance(player1)
                .issueCommand();
        new PlayerSetCommandsBuilder(out, isSimulation())
                .setPlayer(Players.PLAYER2)
                .setStats(PlayerStats.ALL)
                .setInstance(player2)
                .issueCommand();
    }

    //Spawns Avatars in starting positions at init
    public void spawnAvatars(ActorRef out) {
        //Avatar1
        human = new UnitFactory().generateUnit(UnitType.HUMAN);
        human.setAvatar(true);
        human.setName("Human Avatar");
        human.setPlayerID(Players.PLAYER1);

        human.setDamage(2);
        human.setHealth(player1.getHealth());

        Tile tile = getBoard().getTile(1, 2);
        tile.setUnit(human);
        human.setPositionByTile(tile);

        UnitCommandBuilder humanCommands = new UnitCommandBuilder(out, isSimulation())
                .setUnit(human);

        humanCommands.setMode(UnitCommandBuilderMode.DRAW)
                .setTilePosition(1, 2)
                .setPlayerID(Players.PLAYER1)
                .issueCommand();

        // setting health & attack to board. *They doesn't stack*
        // uses the health that has been initialised earlier with the player constructor

        try {Thread.sleep(30);} catch (InterruptedException e) {e.printStackTrace();}

        humanCommands.setMode(UnitCommandBuilderMode.SET)
                .setStats(UnitStats.HEALTH, player1.getHealth())
                .issueCommand();

        try {Thread.sleep(30);} catch (InterruptedException e) {e.printStackTrace();}

        humanCommands.setMode(UnitCommandBuilderMode.SET)
                .setStats(UnitStats.ATTACK, 2)
                .issueCommand();

        player1UnitsPosition.add(new Pair<>(1, 2));

        try {Thread.sleep(30);} catch (InterruptedException e) {e.printStackTrace();}

        //Avatar2
        aiAvatar = new UnitFactory().generateUnit(UnitType.AI);
        aiAvatar.setAvatar(true);
        aiAvatar.setName("AI Avatar");
        aiAvatar.setPlayerID(Players.PLAYER2);

        aiAvatar.setDamage(2);
        aiAvatar.setHealth(player2.getHealth());

        Tile tile2 = getBoard().getTile(7, 2);
        tile2.setUnit(aiAvatar);
        aiAvatar.setPositionByTile(tile2);

        UnitCommandBuilder aiCommands = new UnitCommandBuilder(out, isSimulation())
                .setUnit(aiAvatar);

        aiCommands.setMode(UnitCommandBuilderMode.DRAW)
                .setTilePosition(7, 2)
                .setPlayerID(Players.PLAYER2)
                .issueCommand();

        try {Thread.sleep(30);} catch (InterruptedException e) {e.printStackTrace();}

        //uses the health that has been initialised earlier with the player constructor

        aiCommands.setMode(UnitCommandBuilderMode.SET)
                .setStats(UnitStats.HEALTH, player2.getHealth())
                .issueCommand();

        try {Thread.sleep(30);} catch (InterruptedException e) {e.printStackTrace();}

        aiCommands.setMode(UnitCommandBuilderMode.SET)
                .setStats(UnitStats.ATTACK, 2)
                .issueCommand();

        try {Thread.sleep(30);} catch (InterruptedException e) {e.printStackTrace();}

        player2UnitsPosition.add(new Pair<>(7, 2));

        //Save the original health state to a hashmap. Used for calculations.
        cardPlayed.setUnitsOriginalHealth(human.getId(),player1.getHealth());
        cardPlayed.setUnitsOriginalHealth(human.getId(),player2.getHealth());
    }

    // This method add 3 cards to both Players as part of initialisation.
    public void drawInitialCards(ActorRef out) {
        int INITIAL_CARD_COUNT = 3;
        for (int idx = 0; idx < INITIAL_CARD_COUNT; idx++) {
            cardDrawing.drawNewCardFor(out, Players.PLAYER1);
            cardDrawing.drawNewCardFor(out, Players.PLAYER2);
        }
        cardDrawing.displayCardsOnScreenFor(out, turn);
    }

    // ===========================================================================
    // Handlers
    // ===========================================================================
    public void endTurnClicked(ActorRef out) {
        highlighter.clearBoardHighlights(out);
        turn = (turn == Players.PLAYER1) ? Players.PLAYER2 : Players.PLAYER1;
        unitMovementAndAttack.resetMoveAttackAndCounterAttack(out);
        setManaByRound(out);
        if (roundNumber > 3) {
            cardDrawing.drawNewCardFor(out, turn);
        }
        cardDrawing.displayCardsOnScreenFor(out, turn);
        specialAbilities.turnDidEnd(out);
        ++roundNumber; // Divide this by 2 when we are going to use this.
        if (turn == Players.PLAYER2) {
            smartBoy.tester(out);
        }
    }

    /**
     * This method implements the actions to be performed when a card is clicked.
     * @author Anamika Maurya (2570847M@student.gla.ac.uk)
     * @author Theodoros Vrakas (2593566v@student.gla.ac.uk)
     */

    public void cardClicked(ActorRef out, int idx) {
        Card current = getCardsInHand(turn).get(idx);
        System.out.println("Card Clicked: " + current.getCardname());
        Pair<Card, Integer> card = cardPlayed.getActiveCard();

        // Decrease Mana
        int manaCost = current.getManacost();
        int playersMana = (turn == Players.PLAYER1) ? player1.getMana() : player2.getMana();
        boolean enoughMana = playersMana >= manaCost;   //if enough mana then true

        // if enough mana, then highlight and play the card, else drop a notification
        if(enoughMana) {
            highlighter.clearBoardHighlights(out);
            
            // For unit Planar Scout
        	if (current.getCardname().equals("Planar Scout")) {
        	    if (currentHighlightedCard != null && currentHighlightedCard.getFirst().getCardname().equals("Planar Scout")) {
                    highlighter.clearBoardHighlights(out);
                } else {
                    cardPlayed.setActiveCard(current, idx);
                    unitMovementAndAttack.summonAnywhereHighlight(out);
                }
        	}
        	else if (card == null || card.getSecond() != idx) {
                cardPlayed.setActiveCard(current, idx);
                ArrayList<Pair<Integer, Integer>> friendlyUnits = getUnitsPosition(turn);
                for (Pair<Integer, Integer> position : friendlyUnits) {
                    highlighter.cardTileHighlight(out, position.getFirst(), position.getSecond());
                }
            }
        } else {
            highlighter.clearBoardHighlights(out);
            new PlayerNotificationCommandBuilder(out, isSimulation())
                    .setMessage("Insufficient Mana")
                    .setPlayer(Players.PLAYER1)
                    .setDisplaySeconds(2)
                    .issueCommand();
        }

        // Highlight clicked card and unhighlight when clicked again
        if (enoughMana && (currentHighlightedCard == null || currentHighlightedCard.getFirst() != current)) {
            highlightCard(out, current, idx);
        } else {
            dehighlightCard(out);
        }
    }

    public void tileClicked(ActorRef out, int x, int y) {
        Tile tile = getBoard().getTile(x, y);
        if (tile.getTileState() == States.RED) {
            System.out.println("A");
            if (cardPlayed.getActiveCard() != null) {
                // Handle spell
                cardPlayed.moveCardToBoard(out, x ,y);
            }
            if (unitMovementAndAttack.getActiveUnit() != null) {
                // Handle attack
                unitMovementAndAttack.launchAttack(out, x, y);
            }
        } else if (tile.getTileState() == States.HIGHLIGHTED) {
            System.out.println("B");
            if (cardPlayed.getActiveCard() != null) {
                // Card Played or spell played
                cardPlayed.moveCardToBoard(out, x, y);
            } else {
                // Must be a move
                unitMovementAndAttack.highlightedMoveTileClicked(out, x, y);
            }
        } else {
            if (unitMovementAndAttack.getActiveUnit() == null) {
                if (tile != null && tile.hasUnit()) {
                    // User clicked on a unit.
                    System.out.println("Shouldnt move cos its tired?");
                    System.out.println("attacked: " + tile.getUnit().getHasAttacked());
                    System.out.println("moved: " + tile.getUnit().getHasMoved());
                    if (tile.getUnit().getHasAttacked() && tile.getUnit().getHasMoved()) {
                        // Block hasAttacked -> hasMoved
                        System.out.println("Shouldnt move cos its tired A+M");
                        return;
                    } else if (tile.getUnit().getHasAttacked()) {
                        // Block hasAttacked
                        System.out.println("Shouldnt move cos its tired A");

                        return;
                    }
                    unitMovementAndAttack.unitClicked(out, x ,y);
                }
            } else {
                if (tile != null && tile.hasUnit()) {
                    System.out.println("G");
                    // If player clicked on a unit and clicked on another.
                    unitMovementAndAttack.unitClicked(out, x, y);
                } else if (tile != null && tile.getTileState() == States.NORMAL) {
                    System.out.println("H");
                    // Click on another unit or the activated unit will cancel the board highlight.
                    highlighter.clearBoardHighlights(out);
                }
            }
        }
        if (cardPlayed.getActiveCard() != null && !tile.hasUnit()) {
            // Cancel the highlights on clicking on a not highlighted cell, excluding the unit.
            highlighter.clearBoardHighlights(out);
        }
    }

    /**
     * End game implements the logic for the game end.
     * @author Anamika Maurya (2570847M@student.gla.ac.uk)
     */
    public void endGame(ActorRef out) {
        Player winner = null;
        // Win condition: should be moved to a method where we are checking player's health

        if (player1.getHealth() < 1) {
            winner = player2;
        } else if (player2.getHealth() < 1) {
            winner = player1;
        }
        
        if (winner != null) {
            String message = (winner == player1) ? "Player 1 won!" : "Player 2 won!" ;
            new PlayerNotificationCommandBuilder(out, isSimulation())
                .setMessage(message)
                .setPlayer(Players.PLAYER1)
                .setDisplaySeconds(4)
                .issueCommand();
        }
    }

    private int getCurrentRoundMana() {
        // This is separated for the potential use in simulation.
        return Math.min(getRound() + 1, 9);
    }

    /**
     * Method to reset mana per turn.
     * @author Anamika Maurya (2570847M@student.gla.ac.uk)
     */
    private void setManaByRound(ActorRef out) {
        int mana = getCurrentRoundMana();

        if(turn == Players.PLAYER1) {
            player1.setMana(mana);
            new PlayerSetCommandsBuilder(out, isSimulation())
                    .setPlayer(Players.PLAYER1)
                    .setStats(PlayerStats.MANA)
                    .setInstance(player1)
                    .issueCommand();
        } else {
            player2.setMana(mana);
            new PlayerSetCommandsBuilder(out, isSimulation())
                    .setPlayer(Players.PLAYER2)
                    .setStats(PlayerStats.MANA)
                    .setInstance(player2)
                    .issueCommand();
        }
    }

    /**
     * Method to decrease the mana whenever a card is played.
     * @author Anamika Maurya (2570847M@student.gla.ac.uk)
     * @author Theodoros Vrakas (2593566v@student.gla.ac.uk)
     */
    public void decreaseManaPerCardPlayed(ActorRef out, int manaCost) {
        int previousMana = (turn == Players.PLAYER1) ? player1.getMana() : player2.getMana();
        int currentMana = previousMana - manaCost;      // We check beforehand that currentMana always >=0
        if(turn == Players.PLAYER1) {
            player1.setMana(currentMana);
            new PlayerSetCommandsBuilder(out, isSimulation())
                    .setPlayer(Players.PLAYER1)
                    .setStats(PlayerStats.MANA)
                    .setInstance(player1)
                    .issueCommand();
        } else {
            player2.setMana(currentMana);
            new PlayerSetCommandsBuilder(out, isSimulation())
                    .setPlayer(Players.PLAYER2)
                    .setStats(PlayerStats.MANA)
                    .setInstance(player2)
                    .issueCommand();
        }
    }

    // ===========================================================================
    // Shared Functions
    // ===========================================================================
    public ArrayList<Pair<Integer, Integer>> getMoveTiles(int x, int y, int depth, int diag) {
        ArrayList<Pair<Integer, Integer>> output = new ArrayList<>();
        output.add(new Pair<>(x-diag, y-depth));
        output.add(new Pair<>(x-depth, y+diag));
        output.add(new Pair<>(x+depth, y-diag));
        output.add(new Pair<>(x+diag, y+depth));
        return output;
    }

    public void unitDied(ActorRef out, Tile unitLocaltion, ArrayList<Pair<Integer, Integer>> pool) {
        specialAbilities.unitDidDie(out, unitLocaltion.getUnit());

        UnitCommandBuilder builder = new UnitCommandBuilder(out, isSimulation())
                .setUnit(unitLocaltion.getUnit());

        builder.setMode(UnitCommandBuilderMode.ANIMATION)
                .setAnimationType(UnitAnimationType.death)
                .issueCommand();

        try {Thread.sleep(3000);} catch (InterruptedException e) {e.printStackTrace();}

        builder.setMode(UnitCommandBuilderMode.DELETE)
                .issueCommand();

        unitLocaltion.setUnit(null);

        Pair<Integer, Integer> item = unitLocaltion.getLocationPair();
        removeFromPool(pool, item);
    }

    public void removeFromPool(ArrayList<Pair<Integer, Integer>> pool, Pair<Integer, Integer> item) {
        for (Pair<Integer, Integer> position: pool) {
            if (position.equals(item)) {
                pool.remove(position);
                break;
            }
        }
    }
    
    /**
     * Method to highlight a card at hand when clicked.
     * @author Anamika Maurya (2570847M@student.gla.ac.uk)
     * @author Yu-Sung Hsu (2540296h@student.gla.ac.uk)
     */
    // Highlighting the clicked card at hand
    public void highlightCard(ActorRef out, Card current, int idx) {

        dehighlightCard(out);

    	// Highlight clicked card
        new CardInHandCommandBuilder(out, isSimulation())
	        .setCommandMode(CardInHandCommandMode.DRAW)
	        .setCard(current)
	        .setPosition(idx)
	        .setState(States.HIGHLIGHTED)
	        .issueCommand();
        
        currentHighlightedCard = new Pair<>(current, idx);
    }

    /**
     * Method to un-highlight a card at hand when clicked again.
     * @author Anamika Maurya (2570847M@student.gla.ac.uk)
     * @author Yu-Sung Hsu (2540296h@student.gla.ac.uk)
     */
    private void dehighlightCard(ActorRef out) {
        if (currentHighlightedCard == null) { return; }
        new CardInHandCommandBuilder(out, isSimulation())
                .setCommandMode(CardInHandCommandMode.DRAW)
                .setCard(currentHighlightedCard.getFirst())
                .setPosition(currentHighlightedCard.getSecond())
                .setState(States.NORMAL)
                .issueCommand();
        currentHighlightedCard = null;
    }


    // ===========================================================================
    // Getters & Setters
    // ===========================================================================
    public UnitMovementAndAttack getUnitMovementAndAttack() {
        return unitMovementAndAttack;
    }

    public SpecialAbilities getSpecialAbilities() {
        return specialAbilities;
    }

    public CardDrawing getCardDrawing() {
        return cardDrawing;
    }

    public CardPlayed getCardPlayed() {
        return cardPlayed;
    }

    public Highlighter getHighlighter() {
        return highlighter;
    }

    public Player getPlayer(Players player) {
        switch (player) {
            case PLAYER1: return player1;
            case PLAYER2: return player2;
        }
        return null;
    }

    public ArrayList<Pair<Integer, Integer>> getUnitsPosition(Players player) {
        switch (player) {
            case PLAYER1: return player1UnitsPosition;
            case PLAYER2: return player2UnitsPosition;
        }
        return null;
    }

    public ArrayList<Pair<Integer, Integer>> getEnemyUnitsPosition(Players player) {
        switch (player) {
            case PLAYER1: return player2UnitsPosition;
            case PLAYER2: return player1UnitsPosition;
        }
        return null;
    }

    public Unit getAvatar(Players player) {
        switch (player) {
            case PLAYER1: return human;
            case PLAYER2: return aiAvatar;
        }
        return null;
    }

    public ArrayList<Card> getCardsInHand(Players player) {
        switch (player) {
            case PLAYER1: return player1CardsInHand;
            case PLAYER2: return player2CardsInHand;
        }
        return null;
    }

    public int getRound() {
        // Increment on every turn change.
        // Just / 2 to get the correct turn number.
        return this.roundNumber / 2;
    }

    public ArrayList<Tile> getAllHighlightedTiles() {
        return highlighter.getHighlightedTiles();
    }

    public Players getTurn() {
        return turn;
    }

    public void setTurn(Players player) {
        turn = player;
    }

    public Board getBoard() {
        return Board.getInstance();
    }

    public boolean isSimulation() {
        return simulation;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public GameStateExtractor getExtractor() {
        return extractor;
    }
}
