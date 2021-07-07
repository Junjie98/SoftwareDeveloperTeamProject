package structures;

import akka.actor.ActorRef;
import structures.basic.Tile;
import structures.extractor.ExtractedGameState;
import structures.handlers.Pair;

public class SmartBoy {
    private GameState parent;

    public SmartBoy(GameState parent) {
        this.parent = parent;
    }

    public void tester(ActorRef out) {
        ExtractedGameState information = getExtractedGameState();
//        information.tileClicked(out, information.player2UnitsPosition.get(0).getFirst(), information.player2UnitsPosition.get(0).getSecond());
//        information.cardClicked(out, 0);

//        System.out.println(information.getAllHighlightedTiles().size());
//        System.out.println(parent.getAllHighlightedTiles().size());

//        System.out.println(information.getMyAvatarPosition());
    }

    private ExtractedGameState getExtractedGameState() {
        return parent.getExtractor().extract();
    }

}
