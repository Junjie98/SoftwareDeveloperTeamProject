package structures.extractor;

import commandbuilders.enums.Players;
import structures.Board;
import structures.GameState;
import structures.basic.Card;
import structures.basic.Player;
import structures.handlers.Pair;

import java.util.ArrayList;


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
        for (Pair<Integer, Integer> pair: getTurn() == Players.PLAYER2?player2UnitsPosition:player1UnitsPosition ) {
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
        for (Pair<Integer, Integer> pair:  getTurn() == Players.PLAYER2?player2UnitsPosition:player1UnitsPosition) {
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
        for (Pair<Integer, Integer> pair:  getTurn() == Players.PLAYER2?player2UnitsPosition:player1UnitsPosition) {
            if (getBoard().getTile(pair).getUnit().isRanged()) {
                output.add(pair);
            }
        }
        return output;
    }

    public ArrayList<Pair<Integer, Integer>> getAllFlyingUnits() {
        ArrayList<Pair<Integer, Integer>> output = new ArrayList<>();
        for (Pair<Integer, Integer> pair:  getTurn() == Players.PLAYER2?player2UnitsPosition:player1UnitsPosition) {
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
        ArrayList<Card> hand = (getTurn()==Players.PLAYER2) ? player2CardsInHand : player1CardsInHand;
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
        for (Pair<Integer,Integer> unit : 
            ((getTurn()==Players.PLAYER2) ? player2UnitsPosition : player1UnitsPosition)) {
            if(!board.getTile(unit).getUnit().getHasAttacked())
            {
                ArrayList<Pair<Integer,Integer>> allAtkTilesR = getUnitMovementAndAttack().getAllAtkTiles(unit.getFirst(), unit.getSecond());
                allAtkTilesR.addAll(getUnitMovementAndAttack().getAllMoveTiles(unit.getFirst(), unit.getSecond()));
            
                for (Pair<Integer,Integer> enemy : (getTurn()==Players.PLAYER2) ? player1UnitsPosition : player2UnitsPosition) {
                    if(allAtkTilesR.contains(enemy))
                    {
                        int movex = enemy.getFirst()-unit.getFirst();
                        int movey = enemy.getFirst() - unit.getFirst();
                        if(!getUnitMovementAndAttack().moveBlockCheck(unit.getFirst(), unit.getSecond(), movex, movey))
                        {
                            Pair<Integer,Integer> attaker = unit;
                            Pair<Integer,Integer> attackee = enemy;

                            out.add(new Pair<>(attaker, attackee));
                        }
                    }
                }
            }
        }
        return out;
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
