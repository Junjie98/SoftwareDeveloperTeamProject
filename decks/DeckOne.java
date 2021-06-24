package decks;

import structures.basic.Card;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

import java.util.ArrayList;
import java.util.Collections;

public class DeckOne {
    private ArrayList<Card> cards = new ArrayList<>();
    // TODO: This is not a deck implementation. This is just written to test draw card.

    public DeckOne() {
        String[] deck1Cards = {
                StaticConfFiles.c_azure_herald,
                StaticConfFiles.c_azurite_lion,
                StaticConfFiles.c_comodo_charger,
                StaticConfFiles.c_fire_spitter,
                StaticConfFiles.c_hailstone_golem,
                StaticConfFiles.c_ironcliff_guardian,
                StaticConfFiles.c_pureblade_enforcer,
                StaticConfFiles.c_silverguard_knight,
                StaticConfFiles.c_sundrop_elixir,
                StaticConfFiles.c_truestrike
        };
        for (int idx = 0; idx < deck1Cards.length; idx++) {
            Card card1 = BasicObjectBuilders.loadCard(deck1Cards[idx], 2*idx, Card.class);
            cards.add(card1);
            Card card2 = BasicObjectBuilders.loadCard(deck1Cards[idx], 2*idx+1, Card.class);
            cards.add(card2);
        }
        Collections.shuffle(cards);
    }

    public Card nextCard() {
        Card temp = cards.get(cards.size() - 1);
        cards.remove(cards.size() - 1);
        return temp;
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }
}
