package structures.memento;

import structures.basic.Unit;
import structures.handlers.Pair;

public class SummonInformation implements RelevantInformation {
    final private Pair<Integer, Integer> target;
    final private Unit summonedUnit;
    final private int indexInHand;
    public SummonInformation(Pair<Integer, Integer> target, int indexInHand, Unit summonedUnit) {
        this.target = target;
        this.summonedUnit = summonedUnit;
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
    public int getIndexInHand()
    {
        return indexInHand;
    }
    public Unit getSummonedUnit() {
        return summonedUnit;
    }

    @Override
    public String toString() {
        return "Unit "+ summonedUnit.getName() +" is summoned on Tile" + target;
    }
}
