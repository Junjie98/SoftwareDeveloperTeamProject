package structures.memento;

import structures.basic.Unit;
import structures.handlers.Pair;

/**
 * The summon information that implements the Relevant information interface.
 * If needed, cast the interface to specific type to get even more detailed information.
 * @author Yu-Sung Hsu (2540296h@student.gla.ac.uk)
 */

public class SummonInformation implements RelevantInformation {
    final private Pair<Integer, Integer> target;
    final private Unit summonedUnit;
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
