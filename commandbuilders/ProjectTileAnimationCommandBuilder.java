package commandbuilders;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.basic.EffectAnimation;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.UnitAnimationType;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

public class ProjectTileAnimationCommandBuilder extends CommandBuilder {
    private final ActorRef reference;
    private Unit unit1, unit2;
    private Tile tile1, tile2;

    public ProjectTileAnimationCommandBuilder(ActorRef out) {
        reference = out;
    }

    public ProjectTileAnimationCommandBuilder setUnit1(Unit unit, Tile tile) {
        this.unit1 = unit;
        this.tile1 = tile;
        return this;
    }

    public ProjectTileAnimationCommandBuilder setUnit2(Unit unit, Tile tile) {
        this.unit2 = unit;
        this.tile2 = tile;
        return this;
    }

    @Override
    public void issueCommand() {
        EffectAnimation projectile = BasicObjectBuilders.loadEffect(StaticConfFiles.f1_projectiles);
        BasicCommands.playUnitAnimation(reference, unit1, UnitAnimationType.attack);
        try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
        BasicCommands.playProjectileAnimation(reference, projectile, 0, tile1, tile2);
        try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
        BasicCommands.playUnitAnimation(reference, unit2, UnitAnimationType.death);
    }
}
