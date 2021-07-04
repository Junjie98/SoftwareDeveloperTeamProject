package structures.memento;

import structures.handlers.Pair;

public class MovementInformation implements RelevantInformation {
    private Pair<Integer, Integer> source, target;
    public MovementInformation(Pair<Integer, Integer> source, Pair<Integer, Integer> target) {
        this.source = source;
        this.target = target;
    }
    @Override
    public Pair<Integer, Integer> getSource() {
        return source;
    }
    @Override
    public Pair<Integer, Integer> getTarget() {
        return target;
    }
}
