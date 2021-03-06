package structures.memento;

import commandbuilders.enums.Players;

/**
 * A game memento class to keep track of vital actions.
 * This can support debugging and potentially smarter AI implementation.
 * @author Yu-Sung Hsu (2540296h@student.gla.ac.uk)
 */

public class GameMemento {
    private Players player;
    private ActionType actionType;
    private RelevantInformation information;

    public GameMemento(Players player, ActionType actionType, RelevantInformation information) {
        this.player = player;
        this.actionType = actionType;
        this.information = information;
    }

    public Players getPlayer() {
        return player;
    }
    public ActionType getActionType() {
        return actionType;
    }

    public RelevantInformation getInformation() {
        return information;
    }

    @Override
    public String toString() {
        return "GameMemento" +
                "(" + player +
                "): " + information;
    }
}
