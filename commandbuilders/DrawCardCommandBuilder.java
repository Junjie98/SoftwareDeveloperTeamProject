package commandbuilders;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.basic.Card;

public class DrawCardCommandBuilder extends CommandBuilder {
    Card card = null;
    int position = 0;
    States mode = States.NORMAL;
    ActorRef reference;

    public DrawCardCommandBuilder(ActorRef out) {
        reference = out;
    }

    public DrawCardCommandBuilder setCard(Card card) {
        this.card = card;
        return this;
    }

    public DrawCardCommandBuilder setPosition(int position) {
        this.position = position;
        return this;
    }

    public DrawCardCommandBuilder setMode(States mode) {
        this.mode = mode;
        return this;
    }

    @Override
    public void issueCommand() {
        int mode = (this.mode == States.NORMAL) ? 0 : 1;
        BasicCommands.drawCard(reference, card, position, mode);
    }
}
