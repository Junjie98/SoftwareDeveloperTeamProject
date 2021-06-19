package commandbuilders;

import akka.actor.ActorRef;
import commandbuilders.enums.MoveDirection;
import commandbuilders.enums.Players;
import commandbuilders.enums.UnitCommandBuilderMode;
import commandbuilders.enums.UnitStats;
import commands.BasicCommands;
import structures.Board;
import structures.basic.Player;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.UnitAnimationType;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

/**
 * This command builder is the most flexible command builder, with modes: DRAW, MOVE, SET, DELETE, ANIMATION.
 *
 * The initialisation step takes the ActorRef that is used to send/receive commands in the front-end side.
 *
 * You will first need to specify the mode by calling .setMode(UnitCommandBuilderMode).
 * You will need to specify a unit by calling .setUnit(Unit)
 *
 * For MOVE and DRAW commands, just specify .setTilePosition(x, y) to specify the target position.
 *
 * For MOVE, you can also use .setDirection to set the direction to HORIZONTAL or VERTICAL.
 *
 * For setting stats of a unit, use .setStats(UnitStats, int), to set the ATTACK or HEALTH to the following value.
 *
 * For ANIMATION, specify one of the UnitAnimationType by calling .setAnimationType(UnitAnimationType), to specify one of
 *      idle, death, attack, move, channel, hit.
 *
 * Each command issuing will only take effect once. Setting the non-required fields will be ignored.
 */

public class UnitCommandBuilder extends CommandBuilder{
    private final ActorRef reference;

    // For every command
    private Unit unit;
    private UnitCommandBuilderMode mode;

    // For Move and Draw
    int tileX = -1;
    int tileY = -1;
    private Players player;

    // For Move Only
    private MoveDirection direction = MoveDirection.HORIZONTAL;
    // This is added to support directions.
    // If it is not set, it defaults horizontal.

    // For Stats
    private UnitStats stats = UnitStats.ATTACK;
    private int value = 0;

    // For Animation
    private UnitAnimationType animationType = UnitAnimationType.attack;

    public UnitCommandBuilder(ActorRef out) {
        reference = out;
    }

    public UnitCommandBuilder setUnit(Unit unit) {
        this.unit = unit;
        return this;
    }

    public UnitCommandBuilder setTilePosition(int x, int y) {
        tileX = x;
        tileY = y;
        return this;
    }

    public UnitCommandBuilder setMode(UnitCommandBuilderMode mode) {
        this.mode = mode;
        return this;
    }

    public UnitCommandBuilder setStats(UnitStats stats, int value) {
        this.stats = stats;
        this.value = value;
        return this;
    }

    public UnitCommandBuilder setPlayerID(Players Player)
    {
        this.player = Player;
        return this;
    }

    public void setDirection(MoveDirection direction) {
        this.direction = direction;
    }

    public UnitCommandBuilder setAnimationType(UnitAnimationType animationType) {
        this.animationType = animationType;
        return this;
    }

    @Override
    public void issueCommand() {
        if (mode == UnitCommandBuilderMode.DRAW) {
            Tile tile = Board.getInstance().getTile(tileX, tileY);
            tile.setUnit(unit);
            unit.setPositionByTile(tile);
            unit.setPlayerID(this.player);
            BasicCommands.drawUnit(reference, unit, tile);
        } else if (mode == UnitCommandBuilderMode.MOVE) {
            Tile tile = Board.getInstance().getTile(tileX, tileY);
            tile.setUnit(unit);
            unit.setPositionByTile(tile);
            boolean dir = (direction == MoveDirection.VERTICAL);
            BasicCommands.moveUnitToTile(reference, unit, tile, dir);
        } else if (mode == UnitCommandBuilderMode.SET) {
            if (stats == UnitStats.ATTACK) {
                BasicCommands.setUnitAttack(reference, unit, value);
            } else {
                BasicCommands.setUnitHealth(reference, unit, value);
            }
        } else if (mode == UnitCommandBuilderMode.DELETE) {
            BasicCommands.deleteUnit(reference, unit);
        } else if (mode == UnitCommandBuilderMode.ANIMATION) {
            BasicCommands.playUnitAnimation(reference, unit, animationType);
        }
    }
}
