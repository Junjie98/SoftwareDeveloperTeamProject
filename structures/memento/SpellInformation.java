package structures.memento;

import structures.basic.Card;
import structures.basic.Unit;
import structures.handlers.Pair;

public class SpellInformation implements RelevantInformation {
    private Pair<Integer, Integer> source, target;
    private Unit summonedUnit;
    private Card spellCard;
    public SpellInformation(Pair<Integer, Integer> target, Card spellCard) {
        this.source = target;
        this.target = target;
        this.spellCard = spellCard;
    }
    @Override
    public Pair<Integer, Integer> getSource() {
        return null;
    }
    @Override
    public Pair<Integer, Integer> getTarget() {
        return null;
    }
    public Unit getSummonedUnit() {
        return summonedUnit;
    }
    public Card getSpellCard() {
        return spellCard;
    }
}
