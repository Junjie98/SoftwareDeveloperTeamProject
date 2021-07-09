package structures.AI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import commandbuilders.enums.Players;
import structures.Board;
import structures.GameState;
import structures.SmartBoy;
import structures.basic.Card;
import structures.extractor.ExtractedGameState;
import structures.handlers.Pair;
import structures.memento.GameMemento;

public class AiNode {
    AiNode parent = null;
    int depth = 0;
    ExtractedGameState gameState = null;
    Map<ArrayList<GameMemento>, AiNode> childPaths = new HashMap<>();
    int goodness = 0;

    AiNode(AiNode parent, GameState gs, int depth)
    {
        this.parent = parent;
        this.depth = ++depth;

        gameState = new SmartBoy(gs).getExtractedGameState();
        goodness = evaulate();
    }

    public void updateMap(Map<ArrayList<GameMemento>, AiNode> childPaths)
    {
        this.childPaths.putAll(childPaths);
    }

    public int evaulate()
    {
        final int UnitCost = 100;
        final int cardCost = 50;
        
        final int healthMutliplier = 50;


        int totalScore = 0;

        ArrayList<Pair<Integer,Integer>> friendlies = new ArrayList<>();
        ArrayList<Pair<Integer,Integer>> enemies = new ArrayList<>();
        ArrayList<Card> ourHand = new ArrayList<>();
        ArrayList<Card> enemyHand = new ArrayList<>();

        int ourHealth=0;
        int enemyHealth=0;






        if(gameState.getTurn() == Players.PLAYER2){
            friendlies = gameState.player2UnitsPosition;
            enemies = gameState.player1UnitsPosition;

            ourHand = gameState.player2CardsInHand;
            enemyHand = gameState.player1CardsInHand;

            ourHealth = gameState.getPlayer(Players.PLAYER2).getHealth();
            enemyHealth = gameState.getPlayer(Players.PLAYER1).getHealth();
        }
        else{
            friendlies = gameState.player1UnitsPosition;
            enemies = gameState.player2UnitsPosition;

            ourHand = gameState.player1CardsInHand;
            enemyHand = gameState.player2CardsInHand;

            ourHealth = gameState.getPlayer(Players.PLAYER1).getHealth();
            enemyHealth = gameState.getPlayer(Players.PLAYER2).getHealth();
        }

        for (Pair<Integer,Integer> unit : friendlies) {
            totalScore+=gameState.getBoard().getTile(unit).getUnit().getHealth();
            totalScore+=UnitCost;
        }        
        for (Pair<Integer,Integer> unit : enemies) {
            totalScore-=gameState.getBoard().getTile(unit).getUnit().getHealth();
            totalScore-=UnitCost;
        }        
        for (Card card : ourHand) {
            totalScore+=UnitCost;
        }        
        for (Card card : enemyHand) {
            totalScore-=cardCost;
        }

        totalScore += ourHealth * healthMutliplier;
        totalScore -= enemyHealth * healthMutliplier;

        return totalScore;
    }
}
