package commandbuilders;

import akka.actor.ActorRef;
import akka.japi.Effect;
import commandbuilders.enums.States;
import commandbuilders.enums.TileAnimationEffect;
import commandbuilders.enums.TileCommandBuilderMode;
import commandbuilders.enums.TileEffectAnimation;
import commands.BasicCommands;
import structures.basic.EffectAnimation;
import structures.basic.Tile;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

public class TileCommandBuilder extends CommandBuilder {
    private final ActorRef reference;
    private TileCommandBuilderMode mode = TileCommandBuilderMode.DRAW;
    private States state = States.NORMAL;
    private int x = 0;
    private int y = 0;
    private TileEffectAnimation effectAnimation;

    public TileCommandBuilder(ActorRef out) {
        reference = out;
    }

    public TileCommandBuilder setMode(TileCommandBuilderMode mode) {
        this.mode = mode;
        return this;
    }

    public TileCommandBuilder setX(int value) {
        x = value;
        return this;
    }
    public TileCommandBuilder setY(int value) {
        y = value;
        return this;
    }

    public TileCommandBuilder setEffectAnimation(TileEffectAnimation effectAnimation) {
        this.effectAnimation = effectAnimation;
        return this;
    }

    public TileCommandBuilder setState(States selected) {
        state = selected;
        return this;
    }

    @Override
    public void issueCommand() {
        if (mode == TileCommandBuilderMode.DRAW) {
            Tile tile = BasicObjectBuilders.loadTile(x, y);
            int drawMode = (state == States.HIGHLIGHTED) ? 1 : 0;
            BasicCommands.drawTile(reference, tile, drawMode);
        } else if (mode == TileCommandBuilderMode.ANIMATION) {
            Tile tile = BasicObjectBuilders.loadTile(x, y);
            String temp = StaticConfFiles.f1_inmolation;
            switch (effectAnimation) {
                case BUFF:
                    temp = StaticConfFiles.f1_buff;
                    break;
                case SUMMON:
                    temp = StaticConfFiles.f1_summon;
                    break;
                case MARTYRDOM:
                    temp = StaticConfFiles.f1_martyrdom;
                    break;
                default:
                    break;
            }
            EffectAnimation animation = BasicObjectBuilders.loadEffect(temp);
            BasicCommands.playEffectAnimation(reference, animation, tile);
        }
    }
}
