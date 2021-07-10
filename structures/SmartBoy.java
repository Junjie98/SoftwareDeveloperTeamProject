package structures;

import java.util.ArrayList;

import akka.actor.ActorRef;
import structures.AI.AI;
import structures.basic.Tile;
import structures.extractor.ExtractedGameState;
import structures.handlers.Pair;
import structures.memento.GameMemento;

public class SmartBoy {
    private GameState parent;

    public SmartBoy(GameState parent) {
        this.parent = parent;
    }

    public void tester(ActorRef out) {
        ExtractedGameState information = getExtractedGameState();
        information.setSimulation(true);

    
        
     

//        System.out.println(information);

//        information.tileClicked(out, information.player2UnitsPosition.get(0).getFirst(), information.player2UnitsPosition.get(0).getSecond());
//        information.cardClicked(out, 0);

//        System.out.println(information.getAllHighlightedTiles().size());
//        System.out.println(parent.getAllHighlightedTiles().size());

//        System.out.println(information.getMyAvatarPosition());
    }

    public ExtractedGameState getExtractedGameState() {
        return parent.getExtractor().extract();
    }

   
}