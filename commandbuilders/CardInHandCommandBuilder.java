package commandbuilders;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.basic.Card;

public class CardInHandCommandBuilder extends CommandBuilder {
    Card card = null;
    CardInHandCommandMode command = CardInHandCommandMode.DRAW;
    int position = 0;
    States mode = States.NORMAL;
    ActorRef reference;

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

    public CardInHandCommandBuilder setMode(States mode) {
        this.mode = mode;
        return this;
    }

    @Override
    public void issueCommand() {
        if (command == CardInHandCommandMode.DRAW) {
            int mode = (this.mode == States.NORMAL) ? 0 : 1;
            BasicCommands.drawCard(reference, card, position, mode);
        } else if (command == CardInHandCommandMode.DELETE) {
            BasicCommands.deleteCard(reference, position);
        }
    }
}
