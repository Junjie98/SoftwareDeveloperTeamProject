package commandbuilders;

import akka.actor.ActorRef;
import commandbuilders.enums.UnitCommandBuilderMode;
import commandbuilders.enums.UnitStats;
import commands.BasicCommands;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.UnitAnimationType;

public class UnitCommandBuilder extends CommandBuilder{
    private final ActorRef reference;

    // For every command
    private Unit unit;
    private UnitCommandBuilderMode mode;

    // For Move and Draw
    private Tile tile;

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

    public UnitCommandBuilder setTile(Tile tile) {
        this.tile = tile;
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

    public UnitCommandBuilder setAnimationType(UnitAnimationType animationType) {
        this.animationType = animationType;
        return this;
    }

    @Override
    public void issueCommand() {
        if (mode == UnitCommandBuilderMode.DRAW) {
            BasicCommands.drawUnit(reference, unit, tile);
        } else if (mode == UnitCommandBuilderMode.MOVE) {
            BasicCommands.moveUnitToTile(reference, unit, tile);
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
