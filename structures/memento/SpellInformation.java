package structures.memento;

import structures.basic.Card;
import structures.handlers.Pair;

public class SpellInformation implements RelevantInformation {
    private Pair<Integer, Integer> target;
    private Card spellCard;
    public SpellInformation(Pair<Integer, Integer> target, Card spellCard) {
        this.target = target;
        this.spellCard = spellCard;
    }
    @Override
    public Pair<Integer, Integer> getSource() {
        return target;
    }
    @Override
    public Pair<Integer, Integer> getTarget() {
        return target;
    }
    public Card getSpellCard() {
        return spellCard;
    }

    @Override
    public String toString() {
        return "Spell ( "  + spellCard.getCardname() + " ) is used on Tile" + target;
    }
}
