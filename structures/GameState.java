package structures;

import java.lang.reflect.Array;
import java.util.ArrayList;

import akka.actor.ActorRef;
import commandbuilders.*;
import commandbuilders.enums.*;
import decks.*;
import structures.basic.Card;
import structures.basic.Player;
import structures.basic.Tile;
import structures.basic.Unit;

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
    private int roundNumber = 1;
    
    private Players turn = Players.PLAYER1;
    // TODO: This should be randomised according to game loop.

    private Player player1, player2;

    private Card[] player1CardsInHand = new Card[MAX_CARD_COUNT_IN_HAND];
    private int player1CardsInHandCount = 0;
    private Card[] player2CardsInHand = new Card[MAX_CARD_COUNT_IN_HAND];
    private int player2CardsInHandCount = 0;


    private DeckOne deck1 = new DeckOne();
    private DeckTwo deck2 = new DeckTwo();

    private boolean preMove = false;
    private boolean preClickCard = false;
    private Tile previousUnitLocation = null;
    private Card previousClickedCard = null;

    private int[][] friendlyUnits = null;

    //////////////////////////////////////////////////////////////////////////////
                ///Initalisation and functions related to such///
    //////////////////////////////////////////////////////////////////////////////
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
    public void spawnAvatars(ActorRef out)
    {
        Unit human = new UnitFactory().generateUnit(UnitType.HUMAN);
        Unit ai = new UnitFactory().generateUnit(UnitType.AI);
        // Unit flyer = new UnitFactory().generateUnit(UnitType.WINDSHRIKE);

        new UnitCommandBuilder(out)
                    .setMode(UnitCommandBuilderMode.DRAW)
                    .setTilePosition(1, 2)
                    .setPlayerID(Players.PLAYER1)
                    .setUnit(human)
                    .issueCommand();

        new UnitCommandBuilder(out)
                    .setMode(UnitCommandBuilderMode.DRAW)
                    .setTilePosition(7, 2)
                    .setPlayerID(Players.PLAYER2)
                    .setUnit(ai)
                    .issueCommand();

        //  new UnitCommandBuilder(out)
        //             .setMode(UnitCommandBuilderMode.DRAW)
        //             .setTilePosition(1, 1)
        //             .setPlayerID(Players.PLAYER1)
        //             .setUnit(flyer)
        //             .issueCommand();
                    
    }


      // This method add 3 cards to both Players as part of initialisation.
    public void drawInitialCards(ActorRef out) {
        for (int idx = 0; idx < INITIAL_CARD_COUNT; idx++) {
            drawNewCardFor(Players.PLAYER1);
            drawNewCardFor(Players.PLAYER2);
        }
        displayCardsOnScreenFor(out, turn);
    }
    ////////////////////////////////////End///////////////////////////////////////


    //////////////////////////////////////////////////////////////////////////////
                            ///Board and turn logic///
    //////////////////////////////////////////////////////////////////////////////
    //iterates turn
    public void nextTurn() {
        if (turn == Players.PLAYER1) {
            turn = Players.PLAYER2;
            
        } else {
            turn = Players.PLAYER1;

            ++roundNumber;//new round when player2 has finished their turn
            resetUnitMoves();
        }
    }

    public void resetUnitMoves()
    {
        int[][] unitsOnBoard = scanBoardForUnits();

        for (int[] is : unitsOnBoard) 
        {
            Board.getInstance().getTile(is[0], is[1]).getUnit().resetHasMoved();    
        }
    }
    public Players getTurn() {
        return turn;
    }

    public void setTurn(Players player) {
        turn = player;
    }
    ////////////////////////////////////end///////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////////
            ///Drawing a New card, playing cards, card click logic///
    //////////////////////////////////////////////////////////////////////////////
    ////// Card methods
    public void cardClicked(ActorRef out, int idx)
    {
        System.out.println("Card Clicked");
        Card[] temp = (turn == Players.PLAYER1) ? player1CardsInHand : player2CardsInHand;

        clearBoardHighlights(out);

        if (previousClickedCard != null && previousClickedCard == temp[idx])
        {
            // Do not redraw the highlights if player clicks on the same card.
            previousClickedCard = null;
        } else {
            // Redraw the highlights.
            friendlyUnits = scanBoardForFriendlyUnits(out);
            for (int[] unit: friendlyUnits)
            {
                cardTileHighlight(out, unit[0], unit[1]);
            }
            preClickCard = true;
            previousClickedCard = temp[idx];
        }
    }
    

    public void cardTileHighlight(ActorRef out,int x, int y)
    {
        int[][] initDir = getMoveTiles(x, y, 1, 0);

        boolean[] initDirB = {true,true,true,true};

        int[][] interDir = getMoveTiles(x, y, 1, 1);

        int count = 0;
        for (int[] is : initDir)                                    //for the inital directions you can move
        {
            initDirB[count] = checkTileHighlight(out, is);       //if they are blocked record this
            count++;
        }

        if(initDirB[0] == true || initDirB[1] == true)              //for the inter tiles do some logic
        {
            checkTileHighlight(out, interDir[0]);
        }
        if(initDirB[1] == true || initDirB[3] == true)
        {
            checkTileHighlight(out, interDir[1]);
        }
        if(initDirB[2] == true || initDirB[0] == true)
        {
            checkTileHighlight(out, interDir[2]);
        }
        if(initDirB[2] == true || initDirB[3] == true)
        {
            checkTileHighlight(out, interDir[3]);
        }
    }

    public boolean getPreClickCard() {
        return preClickCard;
    }

    public void cardUnhighlight(ActorRef out)
    {
        for (int[] array: friendlyUnits) {
            int[][] setA = getMoveTiles(array[0], array[1], 1, 0);
            int[][] setB = getMoveTiles(array[0], array[1], 1, 1);

            for (int[] res: concatenate(setA, setB)) {
                if (res[0] >= 0 && res[0] <= 8 && res[1] >= 0 && res[1] <= 4) {
                    new TileCommandBuilder(out)
                            .setTilePosition(res[0], res[1])
                            .setState(States.NORMAL)
                            .setMode(TileCommandBuilderMode.DRAW)
                            .issueCommand();
                }
            }
        }

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
    ////////////////////////////////////end///////////////////////////////////////




    //////////////////////////////////////////////////////////////////////////////
                ///Unit selection, unit moving, unit logic///
    //////////////////////////////////////////////////////////////////////////////
    public void tileClicked(ActorRef out, int x, int y)
    {
        if (preMove && Board.getInstance().getTile(x, y).getUnit() == null)
        {
            highlightedMoveTileClicked(out, x, y);
        }
        else if (Board.getInstance().getTile(x, y).getUnit() != null && Board.getInstance().getTile(x, y).getUnit().getHasMoved()!=true)
        {
            unitClicked(out, x, y);
        }
        else
        {
            clearBoardHighlights(out);
        }
    }

    public Tile getPreviousUnitLocation() {
        return previousUnitLocation;
    }
    
    public boolean getPreMove() {
        return preMove;
    }

   
    public boolean checkMoveValidity(ActorRef out, int x, int y, boolean flying)
    {
        int[] test = {x,y};                                 //what we testing?

        if(!flying)
        {
            int[][] activeTiles = getAllMoveTiles(previousUnitLocation.getTilex(), previousUnitLocation.getTiley());
            for (int[] ip : activeTiles)
            {
                if(ip[0] == test[0] && ip[1] == test[1])        //valid move 
                {
                    if(Board.getInstance().getTile(x, y).getUnit()!=null)             //this space is occupied
                    {
                        return false;
                    }
                    return true;
                }
            }
            return false;
        }
        else
        {
            for (int[] is : getFlyMoveTiles(out))                   //check the fly tiles for validity
            {
                if(is[0] == test[0] && is[1] == test[1])
                {
                    if(Board.getInstance().getTile(x, y).getUnit()!=null)             //this space is occupied
                    {
                        return false;
                    }
                    return true;
                }
            }
            return false;
        }
    }


    public void highlightedMoveTileClicked(ActorRef out, int x, int y)       //test for selectex
    {
    
        System.out.println("move logic");
        if(checkMoveValidity(out, x, y, previousUnitLocation.getUnit().getId()==2?true:false)) //@YU another here
        {

        
            System.out.println("move valid");

            new UnitCommandBuilder(out)
                    .setMode(UnitCommandBuilderMode.MOVE)
                    .setTilePosition(x, y)
                    .setUnit(previousUnitLocation.getUnit())
                    .issueCommand();

            previousUnitLocation.getUnit().hasMoved();
            clearBoardHighlights(out);
            previousUnitLocation.setUnit(null);

        }
        else
        {
            clearBoardHighlights(out);

        }
        preMove = false;
        
    }

    public void unitClicked(ActorRef out,int x, int y)
    {
        System.out.println(x +"," + y + " Clicked");
        System.out.println(Board.getInstance().getTile(x, y).getUnit().getId());
       
        if(Board.getInstance().getTile(x, y).getUnit().getPlayerID() != turn) //you dont own this unit!
        {
            System.err.println("you dont own this unit");
            return;
        }

        //Unhighlight previously selected unit
        if(preMove == true)
        {

            if (previousUnitLocation !=  Board.getInstance().getTile(x, y))
            {
                //if new unit
                clearBoardHighlights(out);
                
                preMove = true;
                moveHighlight(out, x, y);

                previousUnitLocation = Board.getInstance().getTile(x, y);
            }
            else
            {
                clearBoardHighlights(out);

                preMove=false;
            }
        }
        else
        {
            preMove = true;
            moveHighlight(out, x, y);
          
            previousUnitLocation = Board.getInstance().getTile(x, y);
        }   
    }

    public void moveHighlight(ActorRef out, int x, int y)
    {
        if(Board.getInstance().getTile(x, y).getUnit().getId() == 2) //@YU this is another place
        {
            System.err.println("flyhighlight");
            flyingMoveHighlight(out);
        }
        else
        {
            basicMoveHighlight(out, x, y);
        }
    }


    public void basicMoveHighlight(ActorRef out,int x, int y)
    {
            int[][] initDir = getMoveTiles(x, y, 1, 0);
            
            boolean[] initDirB = {true,true,true,true};

            int[][] secondDir = getMoveTiles(x, y,2, 0);
            int[][] interDir = getMoveTiles(x, y, 1, 1);

            int count = 0;
            for (int[] is : initDir)                                    //for the inital directions you can move
            {
                initDirB[count] = checkTileHighlight(out, is);       //if they are blocked record this
                count++;
            }

            count = 0;
            for (int[] sd : secondDir)                                  //for the next tiles
            {
                if(initDirB[count] == true)                             //if the previous one is clear
                {
                    checkTileHighlight(out, sd);                  //check for units then highlight
                }
                count++;
            }
//
//
            if(initDirB[0] == true || initDirB[1] == true)              //for the inter tiles do some logic
            {
                checkTileHighlight(out, interDir[0]);
            }
            if(initDirB[1] == true || initDirB[3] == true)
            {
                checkTileHighlight(out, interDir[1]);
            }
            if(initDirB[2] == true || initDirB[0] == true)
            {
                checkTileHighlight(out, interDir[2]);
            }
            if(initDirB[2] == true || initDirB[3] == true)
            {
                checkTileHighlight(out, interDir[3]);
            }
        
    }


    private int[][] getMoveTiles(int x, int y, int depth, int inter)
    {
        int[] up = {x-inter, y-depth};
        int[] left = {x-depth, y+inter};
        int[] right = {x+depth, y-inter};
        int[] down = {x+inter, y+depth};
        int[][] dir = {up, left, right, down};
        return dir;
    }

    public int[][] getAllMoveTiles(int x, int y)
    {
        int[][]a = getMoveTiles(x, y, 1, 0);
        int[][]b = getMoveTiles(x, y, 2, 0);
        int[][]c = getMoveTiles(x, y, 1, 1);
        a=concatenate(a, b);
        a=concatenate(a, c);
        return a;
        
    }

    public void flyingMoveHighlight(ActorRef out)
    {
        for (int[] ti : getFlyMoveTiles(out))           //available tiles
        {
            //System.err.println("print fly tile" + ti[0] + " "+ ti[1]);
            checkTileHighlight(out, ti);            
        }
        for (int[] bl : scanBoardForUnits()) //Blocked tiles
        {
            checkTileHighlight(out, bl);            
        } 

    }

    public int[][] getFlyMoveTiles(ActorRef out)
    {
        int[][] maxContainer = new int[45][2];
        int count = 0 ;

        for(int x = 0; x < 9; x++ )
        {
            for(int y = 0; y < 5; y ++)
            {
                if(!Board.getInstance().getTile(x, y).hasUnit())
                {
                    int[] temp = {x,y};
                    System.err.println("tile: " + x + "," + y);
                    maxContainer[count++] = temp;
                }
            }
        }

        int[][] outArr = new int[count][2];
        for(int i = 0; i < count; i++)
        {
            outArr[i][0] = maxContainer[i][0];
            outArr[i][1] = maxContainer[i][1];
        }
        return outArr;
    }    
    
    public void clearFlyingHighlight(ActorRef out)
    {
        for(int x = 0; x < 9; x++ )
        {
            for(int y = 0; y < 5; y ++)
            {
                new TileCommandBuilder(out)
                .setTilePosition(x, y)
                .setState(States.NORMAL)
                .issueCommand();            
            }
        }
    }
    ////////////////////////////////////end///////////////////////////////////////




    //////////////////////////////////////////////////////////////////////////////
            ///Helper Methods, methods used in multiple logics etc///
    //////////////////////////////////////////////////////////////////////////////


    
    public int[][] scanBoardForUnits()
    {
        int[][] maxContainer = new int[45][2];
        int count = 0 ;

        for(int x = 0; x < 9; x++ )
        {
            for(int y = 0; y < 5; y ++)
            {
                if(Board.getInstance().getTile(x, y).hasUnit())
                {
                    int[] temp = {x,y};
                    maxContainer[count++] = temp;
                }
            }
        }

        int[][] outArr = new int[count][2];
        for(int i = 0; i < count; i++)
        {
            outArr[i][0] = maxContainer[i][0];
            outArr[i][1] = maxContainer[i][1];
        }
        return outArr;
    }

    private void clearBoardHighlights(ActorRef out)
    {
        if (preMove == true)
        {
            if(previousUnitLocation.getUnit().getId() == 2) //@YU this is where you might need to change
            {
                clearFlyingHighlight(out);
            }
            else
            {
                TileUnhighlight(out, getAllMoveTiles(previousUnitLocation.getTilex(), previousUnitLocation.getTiley()));
            }
            preMove = false;
        }
        if(preClickCard == true)
        {
            cardUnhighlight(out);
            preClickCard = false;
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
                .setTilePosition(at[0], at[1])
                .setState(States.NORMAL)
                .issueCommand();
        }

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

    //overload
    public boolean checkTileHighlight(ActorRef out, int x, int y)
    {
        int[] pos = {x,y};
        return checkTileHighlight(out, pos);
    }

    public boolean checkTileHighlight(ActorRef out, int[] pos)
    {
        if(pos[0] < 0 || pos[0] > 8)    //limiting input
        {
            return false;
        }  
        if(pos[1] < 0 || pos[1] > 4)
        {
            return false;
        }

        if(!Board.getInstance().getTile(pos[0], pos[1]).hasUnit())    //empty so highlight
        {
            System.err.println("highlight");
            new TileCommandBuilder(out)
                    .setTilePosition(pos[0], pos[1])
                    .setState(States.HIGHLIGHTED)
                    .issueCommand();
            return true;
        }
        else
        {
            if(Board.getInstance().getTile(pos[0], pos[1]).getUnit().getPlayerID() != turn) //enemy
            {
                new TileCommandBuilder(out)
                        .setTilePosition(pos[0], pos[1])
                        .setState(States.RED)
                        .issueCommand();
            }
            else    //friendly
            {
                new TileCommandBuilder(out)
                .setTilePosition(pos[0], pos[1])
                .setState(States.NORMAL)
                .issueCommand();
            }
            return false;

        }
    }

    public int[][] scanBoardForFriendlyUnits(ActorRef out)
    {
        //System.err.println("scan out board");
        int[][] friendlyUnitLocations = new int[45][2];
        int count = 0;


        for(int x = 0; x < 9; x++ )
        {
            for(int y = 0; y < 5; y ++)
            {
                if(Board.getInstance().getTile(x,y).hasFriendlyUnit(turn))
                {
                    //save ints into array x y
                    System.err.println("Found friendly go for highlight");
                    friendlyUnitLocations[count][0] = x;
                    friendlyUnitLocations[count][1] = y;
                    count++;
                }
            }
        }

        int [][] output = new int[count][2];

        for (int i=0; i<count;i++) {
            System.out.println("Unit " + count + ", x: " + friendlyUnitLocations[i][0] + " y: " + friendlyUnitLocations[i][1]);
            output[i] = friendlyUnitLocations[i];
        }
        //System.out.println(turn + " has " + count + " of friendly units");

        return output;
    }
    ////////////////////////////////////end///////////////////////////////////////


    //////////////////////////////////////////////////////////////////////////////
                               ///Mana incrementation///
    //////////////////////////////////////////////////////////////////////////////
    public void ManaIncrementPerRound(ActorRef out) {
    	if(this.turn==Players.PLAYER1) {
    		int player1Mana = player1.getMana();
    		player1.setMana(player1Mana + roundNumber + 1);
    		new PlayerSetCommandsBuilder(out)
        		.setPlayer(Players.PLAYER1)
        		.setStats(PlayerStats.MANA)
        		.setInstance(player1)
        		.issueCommand();
    	}else {
        	int player2Mana = player2.getMana();
        	player2.setMana(player2Mana + roundNumber + 1);
        	new PlayerSetCommandsBuilder(out)
            	.setPlayer(Players.PLAYER2)
            	.setStats(PlayerStats.MANA)
            	.setInstance(player2)
            	.issueCommand();
    	}
    }
    
    public int getRound() { //uses for validation. So round 0 will not increment the mana for player 2 after
        //player1 ends his turn.
        return this.roundNumber;

    }

    ////////////////////////////////////end///////////////////////////////////////
}
