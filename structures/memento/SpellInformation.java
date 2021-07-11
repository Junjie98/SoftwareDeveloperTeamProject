package structures.memento;

import structures.basic.Card;
import structures.basic.Unit;
import structures.handlers.Pair;

public class SpellInformation implements RelevantInformation {
    final private Pair<Integer, Integer> target;
    final private Card spellCard;
    final private int indexInHand;
    final private Unit spellTarget;
    public SpellInformation(int indexInHand, Unit spellTarget, Pair<Integer, Integer> target, Card spellCard) {
        this.spellTarget = spellTarget;
        this.target = target;
        this.spellCard = spellCard;
        this.indexInHand = indexInHand;
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
    public Unit getSpellTarget() {
        return spellTarget;
    }

    public int getIndexInHand()
    {
        return indexInHand;
    }
    @Override
    public String toString() {
        return "Spell ("  + spellCard.getCardname() + ") is used on " +  target;
    }
}
