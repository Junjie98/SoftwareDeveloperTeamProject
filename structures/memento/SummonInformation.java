package structures.memento;

import structures.basic.Unit;
import structures.handlers.Pair;

public class SummonInformation implements RelevantInformation {
    private Pair<Integer, Integer> target;
    private Unit summonedUnit;
    public SummonInformation(Pair<Integer, Integer> target, Unit summonedUnit) {
        this.target = target;
        this.summonedUnit = summonedUnit;
    }
    @Override
    public Pair<Integer, Integer> getSource() {
        return target;
    }
    @Override
    public Pair<Integer, Integer> getTarget() {
        return target;
    }
    public Unit getSummonedUnit() {
        return summonedUnit;
    }

    @Override
    public String toString() {
        return "Unit "+ summonedUnit.getName() +" is summoned on Tile" + target;
    }
}
