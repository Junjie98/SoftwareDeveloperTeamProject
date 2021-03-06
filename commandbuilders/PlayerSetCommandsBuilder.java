package commandbuilders;

import akka.actor.ActorRef;
import commandbuilders.enums.PlayerStats;
import commandbuilders.enums.Players;
import commands.BasicCommands;
import structures.basic.Player;

/**
 * This builder sends out commands to set stats of the Player.
 *
 * The initialisation step takes the ActorRef that is used to send/receive commands in the front-end side.
 *
 * You will need to call .setPlayer(Players) to decide the player to set is PLAYER1 or PLAYER2,
 *      .setStats(PlayerStats) to decide to set MANA, HEALTH, or ALL, and
 *      .setInstance(Player) to provide an instance whose specified Stats will be set to the UI.
 *
 * @author Yu-Sung Hsu (2540296h@student.gla.ac.uk)
 */

public class PlayerSetCommandsBuilder extends CommandBuilder {
    private final ActorRef reference;
    private Players player;
    private PlayerStats stats;
    private Player instance;
    private boolean simulation;

    public PlayerSetCommandsBuilder(ActorRef out, boolean simulation) {
        reference = out;
        this.simulation = simulation;
    }

    public PlayerSetCommandsBuilder setPlayer(Players player) {
        this.player = player;
        return this;
    }

    public PlayerSetCommandsBuilder setStats(PlayerStats stats) {
        this.stats = stats;
        return this;
    }

    public PlayerSetCommandsBuilder setInstance(Player instance) {
        this.instance = instance;
        return this;
    }

    @Override
    public void issueCommand() {
        if (simulation) {
            // Command blocked due to simulation.
            return;
        }
        if (player == Players.PLAYER1) {
            if (stats == PlayerStats.HEALTH) {
                BasicCommands.setPlayer1Health(reference, instance);
            } else if (stats == PlayerStats.MANA) {
                BasicCommands.setPlayer1Mana(reference, instance);
            } else {
                BasicCommands.setPlayer1Health(reference, instance);
                BasicCommands.setPlayer1Mana(reference, instance);
            }
        } else if (player == Players.PLAYER2) {
            if (stats == PlayerStats.HEALTH) {
                BasicCommands.setPlayer2Health(reference, instance);
            } else if (stats == PlayerStats.MANA) {
                BasicCommands.setPlayer2Mana(reference, instance);
            } else {
                BasicCommands.setPlayer2Health(reference, instance);
                BasicCommands.setPlayer2Mana(reference, instance);
            }
        }
    }
}
