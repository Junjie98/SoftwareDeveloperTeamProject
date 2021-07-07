package structures.memento;

import structures.basic.Card;
import structures.basic.Unit;
import structures.handlers.Pair;

public class SpellInformation implements RelevantInformation {
    final private Pair<Integer, Integer> target;
    final private Card spellCard;
    final private Unit spellTarget;
    public SpellInformation(Unit spellTarget, Pair<Integer, Integer> target, Card spellCard) {
        this.spellTarget = spellTarget;
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
    public Unit getSpellTarget() {
        return spellTarget;
    }

    @Override
    public String toString() {
        return "Spell ("  + spellCard.getCardname() + ") is used on " + spellTarget.getName() + target;
    }
}
