package commandbuilders;

import akka.actor.ActorRef;
import commandbuilders.enums.PlayerStats;
import commandbuilders.enums.Players;
import commands.BasicCommands;
import structures.basic.Player;

public class PlayerSetCommandsBuilder extends CommandBuilder {
    private final ActorRef reference;
    private Players player;
    private PlayerStats stats;
    private Player instance;

    public PlayerSetCommandsBuilder(ActorRef out) {
        reference = out;
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
