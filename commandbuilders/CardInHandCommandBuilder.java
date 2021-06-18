package commandbuilders;

import akka.actor.ActorRef;
import commandbuilders.enums.CardInHandCommandMode;
import commandbuilders.enums.States;
import commands.BasicCommands;
import structures.basic.Card;

/**
 * This builder sends out specified DRAW or DELETE command.
 *
 * The initialisation step takes the ActorRef that is used to send/receive commands in the front-end side.
 *
 * You will first need to use .setMode(CardInHandCommandMode) to decide the mode to be used. The description is as following:
 *
 * - mode DRAW - You will need to call .setCard(Card), .setPosition(int), .setState(States) to set the card to be added,
 *      the position of the card in the hand, and the NORMAL or HIGHLIGHTED state of the card before called .issueCommand().
 *      Card state is defined by States, including HIGHLIGHTED (RED will be interpreted as HIGHLIGHTED too), NORMAL.
 *
 * - mode DELETE - When set to delete, you will need to call .setPosition(int) to indicate the position of card to be removed.
 *      Notice any other fields set will be ignored by the .issueCommand() function.
 */

public class CardInHandCommandBuilder extends CommandBuilder {
    private final ActorRef reference;
    private Card card = null;
    private CardInHandCommandMode command = CardInHandCommandMode.DRAW;
    private States state = States.NORMAL;
    private int position = 0;

    public CardInHandCommandBuilder(ActorRef out) {
        reference = out;
    }

    public CardInHandCommandBuilder setCommandMode(CardInHandCommandMode mode) {
        this.command = mode;
        return this;
    }

    public CardInHandCommandBuilder setCard(Card card) {
        this.card = card;
        return this;
    }

    public CardInHandCommandBuilder setPosition(int position) {
        this.position = position;
        return this;
    }

    public CardInHandCommandBuilder setState(States state) {
        this.state = state;
        return this;
    }

    @Override
    public void issueCommand() {
        if (command == CardInHandCommandMode.DRAW) {
            int mode = (this.state == States.NORMAL) ? 0 : 1;
            BasicCommands.drawCard(reference, card, position, mode);
        } else if (command == CardInHandCommandMode.DELETE) {
            BasicCommands.deleteCard(reference, position);
        }
    }
}
