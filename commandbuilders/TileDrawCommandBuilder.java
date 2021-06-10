package commandbuilders;

import akka.actor.ActorRef;
import commandbuilders.enums.States;
import commands.BasicCommands;
import structures.basic.Tile;
import utils.BasicObjectBuilders;

public class TileDrawCommandBuilder extends CommandBuilder {
    private final ActorRef reference;
    private States mode = States.NORMAL;
    private int x = 0;
    private int y = 0;

    public TileDrawCommandBuilder(ActorRef out) {
        reference = out;
    }

    public TileDrawCommandBuilder setX(int value) {
        x = value;
        return this;
    }
    public TileDrawCommandBuilder setY(int value) {
        y = value;
        return this;
    }

    public TileDrawCommandBuilder setMode(States selected) {
        mode = selected;
        return this;
    }

    @Override
    public void issueCommand() {
        Tile tile = BasicObjectBuilders.loadTile(x, y);
        int drawMode = (mode == States.HIGHLIGHTED) ? 1 : 0;
        BasicCommands.drawTile(reference, tile, drawMode);
    }
}
