package structures.memento;

import structures.basic.Unit;
import structures.handlers.Pair;

public class MovementInformation implements RelevantInformation {
    private Pair<Integer, Integer> source, target;
    private Unit unit;
    public MovementInformation(Unit unit, Pair<Integer, Integer> source, Pair<Integer, Integer> target) {
        this.source = source;
        this.target = target;
        this.unit = unit;
    }
    @Override
    public Pair<Integer, Integer> getSource() {
        return source;
    }
    @Override
    public Pair<Integer, Integer> getTarget() {
        return target;
    }

    public Unit getUnit() {
        return unit;
    }

    @Override
    public String toString() {
        return "Unit " + unit.getName() + " moved from " + source + " to " + target;
    }
}
