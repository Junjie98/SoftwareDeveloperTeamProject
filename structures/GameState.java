package structures;

import java.lang.reflect.Array;

import akka.actor.ActorRef;
import commandbuilders.CardInHandCommandBuilder;
import commandbuilders.PlayerSetCommandsBuilder;
import commandbuilders.UnitCommandBuilder;
import commandbuilders.TileCommandBuilder;
import commandbuilders.enums.CardInHandCommandMode;
import commandbuilders.enums.PlayerStats;
import commandbuilders.enums.Players;
import commandbuilders.enums.States;
import commandbuilders.enums.UnitCommandBuilderMode;
import commands.BasicCommands;
import decks.*;
import structures.basic.Card;
import structures.basic.Player;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

/**
 * This class can be used to hold information about the on-going game.
 * Its created with the GameActor.
 * 
 * @author Dr. Richard McCreadie
 *
 */

public class GameState {
    private final int MAX_CARD_COUNT_IN_HAND = 6;
    private final int INITIAL_CARD_COUNT = 3;

    private Players turn = Players.PLAYER1;
    // TODO: This should be randomised according to game loop.

    private Player player1, player2;

    private Card[] player1CardsInHand = new Card[MAX_CARD_COUNT_IN_HAND];
    private int player1CardsInHandCount = 0;
    private Card[] player2CardsInHand = new Card[MAX_CARD_COUNT_IN_HAND];
    private int player2CardsInHandCount = 0;

    private DeckOne deck1 = new DeckOne();
    private DeckTwo deck2 = new DeckTwo();

    //////////////////////////////////////////////////////////////////////////////

    private boolean preMove = false;
    private Tile previousUnitLocation = null;
    private Tile[][] board = new Tile[9][5];

    public void loadBoardFromTile(int x, int y)
    {
        board[x][y] = BasicObjectBuilders.loadTile(x, y);
    }
    //////////////////////////////////////////////////////////////////////////////
    public void nextTurn() {
        if (turn == Players.PLAYER1) {
            turn = Players.PLAYER2;
        } else {
            turn = Players.PLAYER1;
        }
    }

    public Players getTurn() {
        return turn;
    }

    public void setTurn(Players player) {
        turn = player;
    }

    public void generateTwoUsers(ActorRef out) {
        player1 = new Player();
        player2 = new Player();

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

    // This method add 3 cards to both Players as part of initialisation.
    public void drawInitialCards(ActorRef out) {
        for (int idx = 0; idx < INITIAL_CARD_COUNT; idx++) {
            drawNewCardFor(Players.PLAYER1);
            drawNewCardFor(Players.PLAYER2);
        }
        displayCardsOnScreenFor(out, turn);
    }

    public void drawCard(ActorRef out, Players player) {
        drawNewCardFor(player);
        displayCardsOnScreenFor(out, player);
    }

    private void drawNewCardFor(Players player) {
        // TODO: This does not protect anything regarding end game logic.
        if (player == Players.PLAYER1) {
            if (player1CardsInHandCount < MAX_CARD_COUNT_IN_HAND) {
                Card temp = deck1.nextCard();
                player1CardsInHand[player1CardsInHandCount++] = temp;
            }
        } else {
            if (player2CardsInHandCount < MAX_CARD_COUNT_IN_HAND) {
                Card temp = deck2.nextCard();
                player2CardsInHand[player2CardsInHandCount++] = temp;
            }
        }
    }

    private void displayCardsOnScreenFor(ActorRef out, Players player) {
        Card[] currentCardInHand = (player == Players.PLAYER1) ? player1CardsInHand : player2CardsInHand;
        int currentCardInHandCount = (player == Players.PLAYER2) ? player1CardsInHandCount : player2CardsInHandCount;
        for (int idx = 0; idx < currentCardInHand.length; idx++) {
            if (idx < currentCardInHandCount) {
                new CardInHandCommandBuilder(out)
                        .setCommandMode(CardInHandCommandMode.DRAW)
                        .setCard(currentCardInHand[idx])
                        .setPosition(idx)
                        .setState(States.NORMAL)
                        .issueCommand();
            } else {
                new CardInHandCommandBuilder(out)
                        .setCommandMode(CardInHandCommandMode.DELETE)
                        .setPosition(idx)
                        .issueCommand();
            }
        }
    }


    public void spawnAvatars(ActorRef out)
    {
        Unit unit = BasicObjectBuilders.loadUnit(StaticConfFiles.humanAvatar, 0, Unit.class);
        Unit unit2 = BasicObjectBuilders.loadUnit(StaticConfFiles.aiAvatar, 0, Unit.class);

		Tile tile = board[1][2];        //you can enum this if you want but honestly is it worth it?
		Tile tile2 = board[7][2];
        tile.setUnit(unit);
        tile2.setUnit(unit2);

        new UnitCommandBuilder(out)
                    .setMode(UnitCommandBuilderMode.DRAW)
                    .setTile(tile)
                    .setPlayerID(Players.PLAYER1)
                    .setUnit(unit)
                    .issueCommand();

        new UnitCommandBuilder(out)
                    .setMode(UnitCommandBuilderMode.DRAW)
                    .setTile(tile2)
                    .setPlayerID(Players.PLAYER2)
                    .setUnit(unit2)
                    .issueCommand();
                    
    }

    
    public void highlightedMoveTileClicked(ActorRef out,int x, int y)       //test for selectex
    {
        if(preMove==true)                                       //if about to move
        { 
            System.out.println("move logic");
                                                                  //what were the prev valid move squares?
            int[][] activeTiles = getAllMoveTiles(previousUnitLocation.getTilex(), previousUnitLocation.getTiley());
            int[] test = {x,y};                                 //what we testing?
            for (int[] ip : activeTiles) 
            {
                if(ip[0] == test[0] && ip[1] == test[1])        //valid move 
                {
                    if(board[x][y].getUnit()!=null)             //this space is occupied
                    {
                        return;
                    }
                    System.out.println("move probs");

                    new UnitCommandBuilder(out)
                            .setMode(UnitCommandBuilderMode.MOVE)
                            .setTile(board[x][y])
                            .setUnit(previousUnitLocation.getUnit())
                            .issueCommand();

                    board[x][y].setUnit(previousUnitLocation.getUnit());
                    previousUnitLocation.setUnit(null);

                    TileUnhighlight(out, activeTiles);
                    preMove=false;
                }

            }
        }
    }

    public void unitClicked(ActorRef out,int x, int y)
    {
        if(board[x][y].getUnit() != null)
        { 
            if(board[x][y].getUnit().getPlayerID() != turn) //you dont own this unit!
            {
                return;
            }
            previousUnitLocation = board[x][y];
            System.out.println("activates");
            if(preMove == true)
            {
                System.out.println("working");
                int[][] activeTiles = getAllMoveTiles(x, y);
                TileUnhighlight(out, activeTiles);
                preMove = false;
                return;
            }

            preMove = true;
            basicMoveHighlight(out, x, y);
        }
    }

    public void basicMoveHighlight(ActorRef out,int x, int y)
    {
            int[][] initDir = getInitMoveTiles(x, y);
            
            boolean[] initDirB = {true,true,true,true};

            int[][] secondDir = getSecondayMoveTiles(x, y);
            int[][] interDir = getInterMoveTiles(x, y);

            int count = 0;
            for (int[] is : initDir)                                    //for the inital directions you can move
            {
                initDirB[count] = checkTileHighlight(out, is, turn);       //if they are blocked record this
                count++;
            }

            count = 0;
            for (int[] sd : secondDir)                                  //for the next tiles
            {
                if(initDirB[count] == true)                             //if the previous one is clear
                {
                    checkTileHighlight(out, sd, turn);                  //check for units then highlight
                }
                count++;
            }

            
            if(initDirB[0] == true || initDirB[1] == true)              //for the inter tiles do some logic
            {
                checkTileHighlight(out, interDir[0], turn);
            }
            if(initDirB[1] == true || initDirB[3] == true)
            {
                checkTileHighlight(out, interDir[1], turn);
            } 
            if(initDirB[2] == true || initDirB[0] == true)
            {
                checkTileHighlight(out, interDir[2], turn);
            }
            if(initDirB[2] == true || initDirB[3] == true)
            {
                checkTileHighlight(out, interDir[3], turn);
            }
        
    }

    public void TileUnhighlight(ActorRef out, int[][] activeTiles)
    {
        for (int[] at : activeTiles) 
        {
            if(at[0] < 0 || at[0] > 8)
            {
                continue;
            }  
            if(at[1] < 0 || at[1] > 4)
            {
                continue;
            }

            new TileCommandBuilder(out)
                .setX(at[0])
                .setY(at[1])
                .setState(States.NORMAL)
                .issueCommand();
        }
    }

    private int[][] getInitMoveTiles(int x, int y)
    {
        int[] up = {x, y-1};
        int[] left = {x-1, y};
        int[] right = {x+1, y};
        int[] down = {x, y+1};
        int[][] initDir = {up, left, right, down};
        return initDir;
    }

    private int[][] getSecondayMoveTiles(int x, int y)
    {
        int[] up2 = {x, y-2};
        int[] left2 = {x-2, y};
        int[] right2 = {x+2, y};
        int[] down2 = {x, y+2};
        int[][] secondDir = {up2, left2, right2, down2};
        return secondDir;
    }

    private int[][] getInterMoveTiles(int x, int y)
    {
        int[] upL = {x-1, y-1};
        int[] leftD = {x-1, y+1};
        int[] rightU = {x+1, y-1};
        int[] downR = {x+1, y+1};
        int[][] interDir = {upL, leftD, rightU, downR};
        return interDir;  
    }

    private int[][] getAllMoveTiles(int x, int y)
    {
        int[][]a = getInitMoveTiles(x, y);
        int[][]b = getSecondayMoveTiles(x, y);
        int[][]c = getInterMoveTiles(x, y);
        a=concatenate(a, b);
        a=concatenate(a, c);
        return a;
        
    }

    public <T> T[] concatenate(T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;
    
        @SuppressWarnings("unchecked")
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
    
        return c;
    }


    public boolean checkTileHighlight(ActorRef out, int[] pos, Players playerID)
    {
        if(pos[0] < 0 || pos[0] > 8)    //limiting input
        {
            return false;
        }  
        if(pos[1] < 0 || pos[1] > 4)
        {
            return false;
        }

        if(!board[pos[0]][pos[1]].hasUnit())    //empty so highlight
        {
            new TileCommandBuilder(out)
		 				.setX(pos[0]).setY(pos[1]).setState(States.HIGHLIGHTED)
		 				.issueCommand();
                         return true;
        }
        else
        {
            if(board[pos[0]][pos[1]].getUnit().getPlayerID() != turn) //enemy
            {
                new TileCommandBuilder(out)
                .setX(pos[0]).setY(pos[1]).setState(States.NORMAL)    //RED give red, make red. @YU
                .issueCommand();
            }
            return false;

        }
    }
}
