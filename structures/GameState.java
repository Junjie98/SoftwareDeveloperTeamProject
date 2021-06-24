package structures;

import java.util.ArrayList;
import akka.actor.ActorRef;
import commandbuilders.*;
import commandbuilders.enums.*;
import commands.BasicCommands;
import structures.basic.Card;
import structures.basic.Player;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.handlers.*;

import static commandbuilders.enums.Players.*;

/**
 * This class can be used to hold information about the on-going game.
 * Its created with the GameActor.
 * 
 * @author Dr. Richard McCreadie
 *
 */

// TODO: only move -> attack and attack is allowed. No attack move or move move.

public class GameState {
    private final int INITIAL_CARD_COUNT = 3;
    private int roundNumber = 3;
    private Players turn = Players.PLAYER1;
    private Player player1, player2;
    public ArrayList<Card> player1CardsInHand = new ArrayList<>();
    public ArrayList<Card> player2CardsInHand = new ArrayList<>();
    public ArrayList<Pair<Integer, Integer>> player1UnitsPosition = new ArrayList<>();
    public ArrayList<Pair<Integer, Integer>> player2UnitsPosition = new ArrayList<>();

    // ===========================================================================
    // Handler Classes
    // ===========================================================================
    private UnitMovementAndAttack unitMovementAndAttack = new UnitMovementAndAttack(this);
    private CardDrawing cardDrawing = new CardDrawing(this);
    private CardPlayed cardPlayed = new CardPlayed(this);

    // Ana: counter attack
    // private Tile currentUnitLocation = null;

	// ===========================================================================
    // Game Initialisation
    // ===========================================================================
    //Creates the two players
    public void generateTwoUsers(ActorRef out) {
    	player1 = new Player(20,2); //set players health and mana to 20
        player2 = new Player(20,2); //player start with 2 mana in round 1.

        new PlayerSetCommandsBuilder(out)
                .setPlayer(Players.PLAYER1)
                .setStats(PlayerStats.ALL)
                .setInstance(player1)
                .issueCommand();
        new PlayerSetCommandsBuilder(out)
                .setPlayer(Players.PLAYER2)
                .setStats(PlayerStats.ALL)
                .setInstance(player2)
                .issueCommand();
    }

    //Spawns Avatars in starting positions at init
    public void spawnAvatars(ActorRef out)  {
        Unit human = new UnitFactory().generateUnit(UnitType.HUMAN);
        human.setIdentifier(1);
        
        Unit ai = new UnitFactory().generateUnit(UnitType.AI);
        ai.setIdentifier(1);

        new UnitCommandBuilder(out)
                    .setMode(UnitCommandBuilderMode.DRAW)
                    .setTilePosition(1, 2)
                    .setPlayerID(Players.PLAYER1)
                    .setUnit(human)
                    .issueCommand();

        player1UnitsPosition.add(new Pair<>(1, 2));

        //setting health & attack to board. *They doesn't stack*

        //Avatar1
        new UnitCommandBuilder(out)
        	.setMode(UnitCommandBuilderMode.SET)
        	.setUnit(human) 
        	//uses the health that has been initialised earlier with the player constructor
        	.setStats(UnitStats.HEALTH, player1.getHealth())
        	.issueCommand();
        
        new UnitCommandBuilder(out)
    	.setMode(UnitCommandBuilderMode.SET)
    	.setUnit(human)
    	.setStats(UnitStats.ATTACK, 2)
    	.issueCommand();

        new UnitCommandBuilder(out)
                    .setMode(UnitCommandBuilderMode.DRAW)
                    .setTilePosition(7, 2)
                    .setPlayerID(Players.PLAYER2)
                    .setUnit(ai)
                    .issueCommand();

        player2UnitsPosition.add(new Pair<>(7, 2));

        //Avatar2
        new UnitCommandBuilder(out)
        	.setMode(UnitCommandBuilderMode.SET)
        	.setUnit(ai) 
        	//uses the health that has been initialised earlier with the player constructor
        	.setStats(UnitStats.HEALTH, player2.getHealth())
        	.issueCommand();
        
        new UnitCommandBuilder(out)
    	.setMode(UnitCommandBuilderMode.SET)
    	.setUnit(ai)
    	.setStats(UnitStats.ATTACK, 2)
    	.issueCommand();
    }

    // This method add 3 cards to both Players as part of initialisation.
    public void drawInitialCards(ActorRef out) {
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
        clearBoardHighlights(out);
        turn = (turn == Players.PLAYER1) ? PLAYER2 : PLAYER1;
        unitMovementAndAttack.resetMoveAttackAndCounterAttack();
        setManaByRound(out);
        if (roundNumber > 3) {
            cardDrawing.drawNewCardFor(out, turn);
        }
        cardDrawing.displayCardsOnScreenFor(out, turn);
        ++roundNumber; // Divide this by 2 when we are going to use this.
    }

    public void cardClicked(ActorRef out, int idx) {
        Card current = (turn == PLAYER1) ? player1CardsInHand.get(idx) : player2CardsInHand.get(idx);
        System.out.println("Card Clicked: " + current.getCardname());

        clearBoardHighlights(out);

        if (cardPlayed.getActiveCard() == null || cardPlayed.getActiveCard().getSecond() != idx) {
            cardPlayed.setActiveCard(current, idx);
            ArrayList<Pair<Integer, Integer>> friendlyUnits =
                    (turn == PLAYER1) ? player1UnitsPosition : player2UnitsPosition;
            for (Pair<Integer, Integer> position: friendlyUnits) {
                cardPlayed.cardTileHighlight(out, position.getFirst(), position.getSecond());
            }
        }
    }

    public void tileClicked(ActorRef out, int x, int y) {
        Tile tile = Board.getInstance().getTile(x, y);
        if (tile.getTileState() == States.RED) {
            if (cardPlayed.getActiveCard() != null) {
                // Handle spell
                cardPlayed.moveCardToBoard(out, x ,y);
            }
            if (unitMovementAndAttack.getActiveUnit() != null) {
                // Handle attack
                unitMovementAndAttack.launchAttack(out, x, y);
            }
        } else if (tile.getTileState() == States.HIGHLIGHTED) {
            if (cardPlayed.getActiveCard() != null) {
                // Card Played or spell played
                cardPlayed.moveCardToBoard(out, x, y);
            } else {
                // Must be a move
                unitMovementAndAttack.highlightedMoveTileClicked(out, x, y);
            }
        } else {
            if (unitMovementAndAttack.getActiveUnit() == null) {
                // Player clicked on a unit
                if (tile != null && tile.hasUnit()) {
                    if (tile.getUnit().getHasAttacked() && tile.getUnit().getHasMoved()) {
                        return;
                    } else if (tile.getUnit().getHasAttacked()) {
                        return;
                    }
                }
                unitMovementAndAttack.unitClicked(out, x ,y);
            } else {
                if (tile != null && tile.hasUnit()) {
                    unitMovementAndAttack.unitClicked(out, x, y);
                }
            }
        }
    }

    public void endGame(ActorRef out) {
        Player winner = null;
        // Win condition: should be moved to a method where we are checking player's health
        // If any of the decks run out of card, the player loses.
        if (player1.getHealth() < 1) {
            winner = player1;
        } else if (player2.getHealth() < 1) {
            winner = player2;
        } else if (cardDrawing.isDeckOneEmpty()) {
            winner = player2;
        } else if (cardDrawing.isDeckTwoEmpty()) {
            winner = player1;
        }
        if (winner != null) {
            String message = "";
            if (winner == player1) {
                message = "Player 1 won!";
            } else {
                message = "Player 2 won!";
            }
            new PlayerNotificationCommandBuilder(out)
                    .setMessage(message)
                    .setPlayer(PLAYER1)
                    .setDisplaySeconds(4)
                    .issueCommand();
        }
    }

    private void setManaByRound(ActorRef out) {
        int mana = (getRound() + 1 > 9) ? 9 : getRound() + 1;

        if(turn == Players.PLAYER1) {
            player1.setMana(mana);
            new PlayerSetCommandsBuilder(out)
                    .setPlayer(Players.PLAYER1)
                    .setStats(PlayerStats.MANA)
                    .setInstance(player1)
                    .issueCommand();
        } else {
            player2.setMana(mana);
            new PlayerSetCommandsBuilder(out)
                    .setPlayer(Players.PLAYER2)
                    .setStats(PlayerStats.MANA)
                    .setInstance(player2)
                    .issueCommand();
        }
    }

    // ===========================================================================
    // Shared Highlighting Functions
    // ===========================================================================
    public ArrayList<Pair<Integer, Integer>> getMoveTiles(int x, int y, int depth, int diag) {
        ArrayList<Pair<Integer, Integer>> output = new ArrayList<>();
        output.add(new Pair<>(x-diag, y-depth));
        output.add(new Pair<>(x-depth, y+diag));
        output.add(new Pair<>(x+depth, y-diag));
        output.add(new Pair<>(x+diag, y+depth));
        return output;
    }

    // ===========================================================================
    // TODO: Class Highlighter
    // ===========================================================================
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
            new TileCommandBuilder(out)
                    .setTilePosition(pos.getFirst(), pos.getSecond())
                    .setState(States.HIGHLIGHTED)
                    .issueCommand();

            tile.setTileState(States.HIGHLIGHTED);

            return true;
        } else {
            if(Board.getInstance().getTile(pos.getFirst(), pos.getSecond()).getUnit().getPlayerID() != turn) {
                // Tile has enemy
                // friendly
                new TileCommandBuilder(out)
                        .setTilePosition(x, y)
                        .setState(States.RED)
                        .issueCommand();

                tile.setTileState(States.RED);
            } else {
                // Tile has friendly
                new TileCommandBuilder(out)
                        .setTilePosition(x, y)
                        .setState(States.NORMAL)
                        .issueCommand();

                tile.setTileState(States.NORMAL);
            }
            return false;

        }
    }
    public void clearBoardHighlights(ActorRef out) {
        if (unitMovementAndAttack.getActiveUnit() != null) {
            unitsUnhighlight(out);
            unitMovementAndAttack.setActiveUnit(null);
        }
        if(cardPlayed.getActiveCard() != null) {
            cardUnhighlight(out);
            cardPlayed.clearActiveCard();
        }
    }

    private void unitsUnhighlight(ActorRef out) {
        int x = unitMovementAndAttack.getActiveUnit().getFirst();
        int y = unitMovementAndAttack.getActiveUnit().getSecond();
        Tile previousUnitLocation = Board.getInstance().getTile(x, y);
        Unit temp = previousUnitLocation.getUnit();

        if(temp.isFlying()) {
            clearFlyingHighlight(out);
        } else {
            TileUnhighlight(out, unitMovementAndAttack.getAllMoveTiles(x, y));
        }
    }
    public void cardUnhighlight(ActorRef out) {
        ArrayList<Pair<Integer, Integer>> units = (turn == PLAYER1) ? player1UnitsPosition : player2UnitsPosition;

        for (Pair<Integer, Integer> array: units) {
            ArrayList<Pair<Integer, Integer>> allHighlighted = getMoveTiles(array.getFirst(), array.getSecond(), 1, 0);
            allHighlighted.addAll(getMoveTiles(array.getFirst(), array.getSecond(), 1, 1));
            for (Pair<Integer, Integer> res: allHighlighted) {
                if (res.getFirst() >= 0 && res.getFirst() <= 8 && res.getSecond() >= 0 && res.getSecond() <= 4) {
                    new TileCommandBuilder(out)
                            .setTilePosition(res.getFirst(), res.getSecond())
                            .setState(States.NORMAL)
                            .setMode(TileCommandBuilderMode.DRAW)
                            .issueCommand();
                    Tile tile = Board.getInstance().getTile(res);
                    tile.setTileState(States.NORMAL);
                }
            }
        }
        unhighlightUnits(out);
    }

    public void clearFlyingHighlight(ActorRef out)
    {
        for(int x = 0; x < 9; x++ ) {
            for(int y = 0; y < 5; y ++) {
                Tile tile = Board.getInstance().getTile(x, y);
                new TileCommandBuilder(out)
                        .setTilePosition(x, y)
                        .setState(States.NORMAL)
                        .issueCommand();

                tile.setTileState(States.NORMAL);
            }
        }
    }

    public void unhighlightUnits(ActorRef out) {
        for (Pair<Integer, Integer> array: player1UnitsPosition) {
            new TileCommandBuilder(out)
                    .setTilePosition(array.getFirst(), array.getSecond())
                    .setState(States.NORMAL)
                    .setMode(TileCommandBuilderMode.DRAW)
                    .issueCommand();
        }

        for (Pair<Integer, Integer> array : player2UnitsPosition) {
            new TileCommandBuilder(out)
                    .setTilePosition(array.getFirst(), array.getSecond())
                    .setState(States.NORMAL)
                    .setMode(TileCommandBuilderMode.DRAW)
                    .issueCommand();
        }
    }

    public void TileUnhighlight(ActorRef out, ArrayList<Pair<Integer, Integer>> activeTiles) {
        for (Pair<Integer, Integer> at : activeTiles) {
            int x = at.getFirst();
            int y = at.getSecond();

            Tile tile = Board.getInstance().getTile(x, y);

            if(x < 0 || x > 8) {
                continue;
            }

            if(y < 0 || y > 4) {
                continue;
            }

            new TileCommandBuilder(out)
                .setTilePosition(x, y)
                .setState(States.NORMAL)
                .issueCommand();

            tile.setTileState(States.NORMAL);
        }
    }

    // ===========================================================================
    // Getters & Setters
    // ===========================================================================
    public UnitMovementAndAttack getUnitMovementAndAttack() {
        return unitMovementAndAttack;
    }

    public CardDrawing getCardDrawing() {
        return cardDrawing;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public int getRound() {
        // Increment on every turn change.
        // Just / 2 to get the correct turn number.
        return this.roundNumber / 2;
    }

    public Players getTurn() {
        return turn;
    }

    public void setTurn(Players player) {
        turn = player;
    }
}
