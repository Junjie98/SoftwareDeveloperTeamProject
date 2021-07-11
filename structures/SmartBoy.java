package structures;

import akka.actor.ActorRef;
import commandbuilders.enums.States;
import structures.basic.Card;
import structures.basic.Tile;
import structures.extractor.ExtractedGameState;
import structures.handlers.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class SmartBoy {
    enum ActionType { MOVEMENTORATTACK, SUMMON }

    private GameState parent;

    public SmartBoy(GameState parent) {
        this.parent = parent;
    }

    public void react(ActorRef out) {
        if (out == null) { return; }
        boolean action = false;
        ArrayList<ActionType> possibleActions = new ArrayList<>(Arrays.asList(ActionType.values()));
        Random random = new Random();
        while (!action) {
            ActionType act = possibleActions.get(random.nextInt(possibleActions.size()));
            if (act == ActionType.MOVEMENTORATTACK) {
                action = movementOrAttackHandler(out);
            } else if (act == ActionType.SUMMON) {
                action = summon(out);
            }
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        parent.endTurnClicked(out);
    }

    private boolean movementOrAttackHandler(ActorRef out) {
        ArrayList<Pair<Integer, Integer>> units = parent.getUnitsPosition(parent.getTurn());
        Random random = new Random();

        boolean flag = false;

        while (!flag) {
            int idx = random.nextInt(units.size());
            parent.tileClicked(out, units.get(idx).getFirst(), units.get(idx).getSecond());

            while (parent.getAllHighlightedTiles().isEmpty()) {
                idx = random.nextInt(units.size());
                parent.tileClicked(out, units.get(idx).getFirst(), units.get(idx).getSecond());
            }

            List<Tile> highlighted = parent.getAllHighlightedTiles();
            List<Tile> reds = highlighted.stream()
                    .filter(tile->tile.getTileState() == States.RED)
                    .collect(Collectors.toList());


            if (reds.size() > 0) {
                flag = attack(out, reds);
            } else {
                flag = move(out, highlighted);
            }
        }

        return true;
    }

    private boolean summon(ActorRef out) {
        List<Card> cards = parent.getCardsInHand(parent.getTurn())
                .stream()
                .filter(card -> card.getManacost() <= parent.getPlayer(parent.getTurn()).getMana())
                .collect(Collectors.toList());

        if (cards.isEmpty()) { return false; }

        Random random = new Random();
        int pos = random.nextInt(cards.size());
        ArrayList<Card> cardsInHand = parent.getCardsInHand(parent.getTurn());

        int handPos = -1;

        for (int idx = 0; idx < cardsInHand.size(); idx++) {
            if (cardsInHand.get(idx) == cards.get(pos)) {
                handPos = idx;
                break;
            }
        }

        parent.cardClicked(out, handPos);

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ArrayList<Tile> highlighted = parent.getAllHighlightedTiles();

        int picked = random.nextInt(highlighted.size());

        highlighted.get(picked);

        parent.tileClicked(out, highlighted.get(picked).getTilex(), highlighted.get(picked).getTiley());

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;
    }

    private boolean attack(ActorRef out, List<Tile> highlighted) {
        Random random = new Random();
        Tile tile = highlighted.get(random.nextInt(highlighted.size()));
        parent.tileClicked(out, tile.getTilex(), tile.getTiley());

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;
    }

    private boolean move(ActorRef out, List<Tile> highlighted) {
        Random random = new Random();
        Tile tile = highlighted.get(random.nextInt(highlighted.size()));
        parent.tileClicked(out, tile.getTilex(), tile.getTiley());

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;
    }

    private ExtractedGameState getExtractedGameState() {
        return parent.getExtractor().extract();
    }

}