package structures.extractor;

import commandbuilders.enums.Players;
import structures.Board;
import structures.GameState;
import structures.basic.Card;
import structures.basic.Player;
import structures.basic.Unit;
import structures.handlers.Pair;

import java.util.ArrayList;

import javax.lang.model.util.ElementScanner14;


public class ExtractedGameState extends GameState {
    protected Board board = null;

    public ExtractedGameState() {
        super();
    }

    public void setSimulation(boolean value) {
        this.simulation = value;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public void setRoundNumber(int value) {
        this.roundNumber = value;
    }

    public void setPlayer(Players playerNumber, Player item) {
        switch (playerNumber) {
            case PLAYER1:
                this.player1 = item;
            case PLAYER2:
                this.player2 = item;
        }
    }

    public Pair<Integer, Integer> getAIAvatarPosition() {
        for (Pair<Integer, Integer> pair: player2UnitsPosition) {
            if (getBoard().getTile(pair).getUnit().isAvatar()) {
                return pair;
            }
        }
        return null;
    }

    
    public Pair<Integer, Integer> getMyAvatarPosition() {
        for (Pair<Integer, Integer> pair: player2UnitsPosition) {
            if (getBoard().getTile(pair).getUnit().isAvatar()) {
                return pair;
            }
        }
        return null;
    }
 
 
    public Pair<Integer, Integer> getEnemyAvatarPosition() {
        for (Pair<Integer, Integer> pair:  getTurn() == Players.PLAYER2?player1UnitsPosition:player2UnitsPosition) {
            if (getBoard().getTile(pair).getUnit().isAvatar()) {
                return pair;
            }
        }
        return null;
    }
    
    public ArrayList<Pair<Integer,Integer>> getAllUnitsBarAvatar()
    {
        ArrayList<Pair<Integer, Integer>> output = new ArrayList<>();
        for (Pair<Integer, Integer> pair: getUnitsPosition(getTurn())) {
            if (!getBoard().getTile(pair).getUnit().isAvatar()) {
                output.add(pair);
            }
        }
        return output;

    }
    public ArrayList<Pair<Integer,Integer>> getAllEnemiesBarAvatar()
    {
        ArrayList<Pair<Integer, Integer>> output = new ArrayList<>();
        for (Pair<Integer, Integer> pair: getTurn() == Players.PLAYER2?player1UnitsPosition:player2UnitsPosition ) {
            if (!getBoard().getTile(pair).getUnit().isAvatar()) {
                output.add(pair);
            }
        }
        return output;

    }
    
    
    public ArrayList<Pair<Integer, Integer>> getAllOtherUnits() {
        ArrayList<Pair<Integer, Integer>> output = new ArrayList<>();
        for (Pair<Integer, Integer> pair:  getUnitsPosition(getTurn())) {
            if (!getBoard().getTile(pair).getUnit().isRanged()
                && !getBoard().getTile(pair).getUnit().isFlying()
                && !getBoard().getTile(pair).getUnit().isAvatar()) {
                output.add(pair);
            }
        }
        return output;
    }

    public ArrayList<Pair<Integer, Integer>> getAllRangedUnits() {
        ArrayList<Pair<Integer, Integer>> output = new ArrayList<>();
        for (Pair<Integer, Integer> pair:  getUnitsPosition(getTurn())) {
            if (getBoard().getTile(pair).getUnit().isRanged()) {
                output.add(pair);
            }
        }
        return output;
    }

    public ArrayList<Pair<Integer, Integer>> getAllFlyingUnits() {
        ArrayList<Pair<Integer, Integer>> output = new ArrayList<>();
        for (Pair<Integer, Integer> pair:  getUnitsPosition(getTurn())) {
            if (getBoard().getTile(pair).getUnit().isFlying()) {
                output.add(pair);
            }
        }
        return output;
    }

    public ArrayList<Pair<Integer,Integer>> canStillMove(){
        ArrayList<Pair<Integer,Integer>> out = new ArrayList<>();
        for (Pair<Integer,Integer> unit : 
            ((getTurn()==Players.PLAYER2) ? player2UnitsPosition : player1UnitsPosition)) {
            if(!board.getTile(unit).getUnit().getHasMoved() &&!board.getTile(unit).getUnit().getHasAttacked())
            {
                out.add(unit);
            }
        }
        return out;
    }

    public ArrayList<Pair<Integer, Card>> canStillSummon(){
        ArrayList<Pair<Integer, Card>> out = new ArrayList<>();
        int totalMana = getPlayer(getTurn()).getMana();
        ArrayList<Card> hand = (getTurn()==Players.PLAYER2) ? player2CardsInHand : player1CardsInHand;
        for (int i=0; i < hand.size(); i++) {
            if(!hand.get(i).isSpell() && hand.get(i).getManacost()<=totalMana){
                out.add(new Pair<>(i,hand.get(i)));
            }
        }
        return out;
    }

    public ArrayList<Pair<Integer, Card>> canStillCast(){
        ArrayList<Pair<Integer, Card>> out = new ArrayList<>();
        int totalMana = getPlayer(getTurn()).getMana();
        ArrayList<Card> hand = getCardsInHand(getTurn());
        for (int i=0; i < hand.size(); i++) {
            if(hand.get(i).isSpell() && hand.get(i).getManacost()<=totalMana){
                out.add(new Pair<>(i,hand.get(i)));
            }
        }
        return out;
    }

    public  ArrayList<Pair<Pair<Integer,Integer>,Pair<Integer,Integer>>> canStillAttack()
    {
        ArrayList<Pair<Pair<Integer,Integer>,Pair<Integer,Integer>>> out = new ArrayList<>();
        for (Pair<Integer,Integer> unit : getUnitsPosition(getTurn())) {
            if(!board.getTile(unit).getUnit().getHasAttacked() && !board.getTile(unit).getUnit().getHasMoved())
            {
                
                if(board.getTile(unit).getUnit().isRanged())
                {
                    out.add(new Pair<>(unit, getEnemyAvatarPosition()));
                }
                else{
                    System.out.println("~~~~Attack calculation triggered");
                    ArrayList<Pair<Integer,Integer>> allAtkTilesR = getUnitMovementAndAttack().getAllAtkTiles(unit.getFirst(), unit.getSecond());
                    allAtkTilesR.addAll(getUnitMovementAndAttack().getAllMoveTiles(unit.getFirst(), unit.getSecond()));
                
                    for (Pair<Integer,Integer> enemy : (getTurn()==Players.PLAYER2) ? player1UnitsPosition : player2UnitsPosition) {
                        for (Pair<Integer,Integer> pair : allAtkTilesR) {
                            if(pair.equals(enemy))
                            {
                                System.out.println("~~~enemy in range! do the maths");
                                int movex = enemy.getFirst() - unit.getFirst();
                                int movey = enemy.getSecond() - unit.getSecond();
                                if(!moveBlockCheckAI(unit, movex, movey))
                                {
                                    System.out.println("notblocked");
                                    Pair<Integer,Integer> attaker = unit;
                                    Pair<Integer,Integer> attackee = enemy;
        
                                    out.add(new Pair<>(attaker, attackee));
                                }
                                else{
                                    System.out.println("blocked");
                                }
                            }
                        }
                        
            
                    }
                }
               
            }
        }
        return out;
    }

    public Pair<Integer,Integer> locateUnit(Unit locate)
    {
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 5; y++) {
                if(getBoard().getTile(x, y).hasUnit() && getBoard().getTile(x, y).getUnit().equals(locate))
                {
                    return new Pair<>(x,y);
                }
            }
        }
        return null;
    }

    public boolean moveBlockCheckAI(Pair<Integer,Integer> source,int xMove, int yMove)
    {
        if(Math.abs(xMove) == 1 &&Math.abs(yMove) == 1)
        {
            return false;
        }
        else if(Math.abs(xMove) == 2) {
            if(getBoard().getTile(source.getFirst() + xMove/2, source.getSecond()).hasUnit())
            {
                return true;
            }
            else{
                return false;
            }
        }else if(Math.abs(yMove) == 2) {
            if(getBoard().getTile(source.getFirst() , source.getSecond()+ yMove/2).hasUnit())
            {
                return true;
            }
            else{
                return false;
            }
        }else if((Math.abs(xMove)==1 && yMove==0) ||(Math.abs(yMove)==1 && xMove==0))
        {
            return false;
        }
        else{
            System.out.println("!ERROR CHECK! Error check this move");
            System.out.println(source);
            System.out.println(xMove + " : "+ yMove);
            return false;
        }
    }
    
    // This should only be used for unit testing.
    public void testSummon(Card card, Players player, Pair<Integer, Integer> position) {
        Player myPlayer = getPlayer(player);
        myPlayer.setMana(9);
        ArrayList<Card> hand = getCardsInHand(player);
        hand.clear();
        hand.add(card);
        cardClicked(null, 0);
        getCardPlayed().moveCardToBoard(null, position.getFirst(), position.getSecond());
    }

    @Override
    public Board getBoard() {
        return board;
    }

    @Override
    public String toString() {
        return "ExtractedGameState{" +
                "roundNumber=" + roundNumber +
                ", player1=" + player1 +
                ", player2=" + player2 +
                ", player1CardsInHand=" + player1CardsInHand +
                ", player2CardsInHand=" + player2CardsInHand +
                ", player1UnitsPosition=" + player1UnitsPosition +
                ", player2UnitsPosition=" + player2UnitsPosition +
                ", memento=" + memento +
                ", simulation=" + simulation +
                ", board=" + board +
                '}';
    }
}
