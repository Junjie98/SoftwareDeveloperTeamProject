package commandbuilders;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.basic.Tile;
import utils.BasicObjectBuilders;

public class TileDrawCommandBuilder extends CommandBuilder {
    private int x = 0;
    private int y = 0;
    private TileModes mode = TileModes.NORMAL;
    ActorRef reference;

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

    public TileDrawCommandBuilder setMode(TileModes selected) {
        mode = selected;
        return this;
    }

    @Override
    public void issueCommand() {
        Tile tile = BasicObjectBuilders.loadTile(x, y);
        int drawMode = (mode == TileModes.HIGHLIGHTED) ? 1 : 0;
        BasicCommands.drawTile(reference, tile, drawMode);
    }
}
