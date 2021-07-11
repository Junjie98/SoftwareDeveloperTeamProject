package decks;

import structures.basic.Card;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

import java.util.ArrayList;
import java.util.Collections;

public class DeckTwo {
    private ArrayList<Card> cards = new ArrayList<>();

    public DeckTwo() {
        String[] deck2Cards = {
                StaticConfFiles.c_blaze_hound,
                StaticConfFiles.c_bloodshard_golem,
                StaticConfFiles.c_entropic_decay,
                StaticConfFiles.c_hailstone_golem,
                StaticConfFiles.c_planar_scout,
                StaticConfFiles.c_pyromancer,
                StaticConfFiles.c_serpenti,
                StaticConfFiles.c_rock_pulveriser,
                StaticConfFiles.c_staff_of_ykir,
                StaticConfFiles.c_windshrike,
        };
        for (int idx = 0; idx < deck2Cards.length; idx++) {
            Card card1 = BasicObjectBuilders.loadCard(deck2Cards[idx], 20 + 2*idx, Card.class);
            cards.add(card1);
            Card card2 = BasicObjectBuilders.loadCard(deck2Cards[idx], 20 + 2*idx+1, Card.class);
            cards.add(card2);
        }
        Collections.shuffle(cards);
    }

    public Card nextCard() {
        Card temp = cards.get(cards.size() - 1);
        cards.remove(cards.size() - 1);
        return temp;
    }

    public int getSize() {
        return cards.size();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }
}
