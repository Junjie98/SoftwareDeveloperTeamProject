package structures.memento;

import structures.handlers.Pair;

/**
 * The common interface of the Information Classes.
 * @author Yu-Sung Hsu (2540296h@student.gla.ac.uk)
 */

public interface RelevantInformation {
    Pair<Integer, Integer> getSource();
    Pair<Integer, Integer> getTarget();
}
