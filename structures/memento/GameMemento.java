package structures.memento;

import commandbuilders.enums.Players;

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
}
