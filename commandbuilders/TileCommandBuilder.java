package commandbuilders;

import akka.actor.ActorRef;
import akka.japi.Effect;
import commandbuilders.enums.States;
import commandbuilders.enums.TileCommandBuilderMode;
import commandbuilders.enums.TileEffectAnimation;
import commands.BasicCommands;
import structures.basic.EffectAnimation;
import structures.basic.Tile;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

/**
 * This class can be used to send out DRAW or ANIMATION commands for Tiles.
 *
 * The initialisation step takes the ActorRef that is used to send/receive commands in the front-end side.
 *
 * You will need to specified the mode with .setMode(TileCommandBuilderMode), with the following descriptions:
 *
 * You will need to call .setTilePosition(x, y) to specify the position of the tile.
 *
 * - mode DRAW: Use .setState(States) to decide NORMAL, HIGHLIGHTED, or RED state of the command.
 *
 * - mode ANIMATION: You will need to call .setEffectAnimation(TileEffectAnimation) to set animation.
 *      It is defined with an enum containing INMOLATION, BUFF, MARTYRDOM, SUMMON options.
 *
 * @author Yu-Sung Hsu (2540296h@student.gla.ac.uk)
 */

public class TileCommandBuilder extends CommandBuilder {
    private final ActorRef reference;
    private TileCommandBuilderMode mode = TileCommandBuilderMode.DRAW;
    private States state = States.NORMAL;
    private int x = 0;
    private int y = 0;
    private TileEffectAnimation effectAnimation;

    // For Interval
    // Defaults to 30 ms. Change dynamically use setDrawInterval.
    private int drawInterval = 30;
    private boolean simulation;

    public TileCommandBuilder(ActorRef out, boolean simulation) {
        reference = out;
        this.simulation = simulation;
    }

    public TileCommandBuilder setMode(TileCommandBuilderMode mode) {
        this.mode = mode;
        return this;
    }

    public TileCommandBuilder setTilePosition(int x, int y) {
        this.x = x;
        this.y = y;
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

    public TileCommandBuilder setDrawInterval(int drawInterval) {
        this.drawInterval = drawInterval;
        return this;
    }

    @Override
    public void issueCommand() {
        if (simulation) {
            // Command blocked due to simulation.
            return;
        }
        if (mode == TileCommandBuilderMode.DRAW) {
            Tile tile = BasicObjectBuilders.loadTile(x, y);
            int drawMode = 0;

            switch (state) {
                case HIGHLIGHTED:
                    drawMode = 1;
                    break;
                case RED:
                    drawMode = 2;
                    break;
            }

            BasicCommands.drawTile(reference, tile, drawMode);
            try {Thread.sleep(drawInterval);} catch (InterruptedException e) {e.printStackTrace();}
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
