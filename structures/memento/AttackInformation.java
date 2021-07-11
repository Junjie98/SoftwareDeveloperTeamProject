package structures.memento;

import structures.basic.Unit;
import structures.handlers.Pair;

public class AttackInformation implements RelevantInformation {
    final private Pair<Integer, Integer> source, target;
    final private Unit sourceUnit, targetUnit;
    public AttackInformation(Pair<Integer, Integer> source, Pair<Integer, Integer> target, Unit sourceUnit, Unit targetUnit) {
        this.source = source;
        this.target = target;
        this.sourceUnit = sourceUnit;
        this.targetUnit = targetUnit;
    }
    @Override
    public Pair<Integer, Integer> getSource() {
        return source;
    }
    @Override
    public Pair<Integer, Integer> getTarget() {
        return target;
    }
    public Unit getSourceUnit() {
        return sourceUnit;
    }
    public Unit getTargetUnit() {
        return targetUnit;
    }

    @Override
    public String toString() {
        return "Unit " + source + " attacked " + targetUnit.getName() + target;
    }
}
