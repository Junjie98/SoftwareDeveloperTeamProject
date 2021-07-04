package structures.memento;

import structures.handlers.Pair;

public interface RelevantInformation {
    Pair<Integer, Integer> getSource();
    Pair<Integer, Integer> getTarget();
}
