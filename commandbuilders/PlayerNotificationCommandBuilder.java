package commandbuilders;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.BasicCommands;
import play.libs.Json;
import commandbuilders.enums.Players;

/**
 * This builder sends out notification in the specified seconds.
 *
 * The initialisation step takes the ActorRef that is used to send/receive commands in the front-end side.
 *
 * You will need to call .setMessage(String) to set the message, .setDisplaySeconds(int) to set the time to be displayed,
 *      and .setPlayer(Players) before calling .issueCommand().
 *
 * Although the user is defined as enum of PLAYER1 and PLAYER2,
 *      the PLAYER2 is not properly supported by the front end so it is strongly discouraged to use it.
 *
 * @author Yu-Sung Hsu (2540296h@student.gla.ac.uk)
 */

public class PlayerNotificationCommandBuilder extends CommandBuilder {
    private final ActorRef reference;
    private String message = "";
    private int seconds = 2;
    private Players player = Players.PLAYER1;
    private boolean simulation;

    public PlayerNotificationCommandBuilder(ActorRef out, boolean simulation) {
        reference = out;
        this.simulation = simulation;
    }

    public PlayerNotificationCommandBuilder setMessage(String message) {
        this.message = message;
        return this;
    }

    public PlayerNotificationCommandBuilder setDisplaySeconds(int seconds) {
        this.seconds = seconds;
        return this;
    }

    public PlayerNotificationCommandBuilder setPlayer(Players player) {
        this.player = player;
        return this;
    }

    @Override
    public void issueCommand() {
        if (simulation) {
            // Command blocked due to simulation.
            return;
        }
        if (player == Players.PLAYER1) {
            BasicCommands.addPlayer1Notification(reference, message, seconds);
        } else {
            // This should in theory work. However, it is not properly supported by the front-end.
            // Setting player to Player 2 is strongly discouraged.
            try {
                ObjectNode returnMessage = Json.newObject();
                returnMessage.put("messagetype", "addPlayer2Notification");
                returnMessage.put("text", message);
                returnMessage.put("seconds", seconds);
                reference.tell(returnMessage, reference);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
