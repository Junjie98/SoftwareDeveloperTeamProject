package commandbuilders;

import akka.actor.ActorRef;
import commandbuilders.enums.CardInHandCommandMode;
import commandbuilders.enums.States;
import commands.BasicCommands;
import structures.basic.Card;

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
