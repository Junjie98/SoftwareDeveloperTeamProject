package commandbuilders;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.basic.EffectAnimation;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.UnitAnimationType;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

/**
 * This builder is to display a ProjectTile animation.
 *
 * The initialisation step takes the ActorRef that is used to send/receive commands in the front-end side.
 *
 * You will need to call .setSource(Tile) and .setDestination(Tile) before you issue the command.
 *
 * Notice the issueCommand code is now a generalised version of the Demo code. Further testing may reveal some refinements needed.
 *  @author Yu-Sung Hsu (2540296h@student.gla.ac.uk)
 *  @author Jun Jie Low (2600104L@student.gla.ac.uk(Minor assisted))
 */


public class ProjectTileAnimationCommandBuilder extends CommandBuilder {
    private final ActorRef reference;
    private Tile tile1, tile2;
    private boolean simulation;

    public ProjectTileAnimationCommandBuilder(ActorRef out, boolean simulation) {
        reference = out;
        this.simulation = simulation;
    }

    public ProjectTileAnimationCommandBuilder setSource(Tile tile) {
        this.tile1 = tile;
        return this;
    }

    public ProjectTileAnimationCommandBuilder setDistination(Tile tile) {
        this.tile2 = tile;
        return this;
    }

    @Override
    public void issueCommand() {
        if (simulation) {
            // Command blocked due to simulation.
            return;
        }
        EffectAnimation projectile = BasicObjectBuilders.loadEffect(StaticConfFiles.f1_projectiles);
        BasicCommands.playUnitAnimation(reference, tile1.getUnit(), UnitAnimationType.attack);
        try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
        BasicCommands.playProjectileAnimation(reference, projectile, 0, tile1, tile2);
        
        if(tile2.getUnit().getHealth() <= 0) { //when unit is dead
        	try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
        	BasicCommands.playUnitAnimation(reference, tile2.getUnit(), UnitAnimationType.death);
        	try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
        	BasicCommands.deleteUnit(reference, tile2.getUnit());
        }else {
        	try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
            BasicCommands.playUnitAnimation(reference, tile2.getUnit(), UnitAnimationType.hit);
        }
    }
}
