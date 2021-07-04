package structures.memento;

import structures.basic.Unit;
import structures.handlers.Pair;

public class SummonInformation implements RelevantInformation {
    private Pair<Integer, Integer> source, target;
    private Unit summonedUnit;
    public SummonInformation(Pair<Integer, Integer> source, Pair<Integer, Integer> target, Unit summonedUnit) {
        this.source = source;
        this.target = target;
        this.summonedUnit = summonedUnit;
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
}
