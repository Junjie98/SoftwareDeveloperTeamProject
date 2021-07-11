package structures.memento;

import structures.basic.Unit;
import structures.handlers.Pair;

/**
 * The movement information that implements the Relevant information interface.
 * If needed, cast the interface to specific type to get even more detailed information.
 * @author Yu-Sung Hsu (2540296h@student.gla.ac.uk)
 */

public class MovementInformation implements RelevantInformation {
    final private Pair<Integer, Integer> source, target;
    final private Unit unit;
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
