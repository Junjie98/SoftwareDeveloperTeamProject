package commandbuilders;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.BasicCommands;
import play.libs.Json;
import structures.Players;

public class PlayerNotificationCommandBuilder extends CommandBuilder {
    ActorRef reference;
    String message = "";
    int seconds = 2;
    Players player = Players.PLAYER1;

    public PlayerNotificationCommandBuilder(ActorRef out) {
        reference = out;
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

    public void issueCommand() {
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
