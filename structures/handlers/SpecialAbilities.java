package structures.handlers;

import akka.actor.ActorRef;
import commandbuilders.PlayerSetCommandsBuilder;
import commandbuilders.UnitCommandBuilder;
import commandbuilders.enums.*;
import structures.GameState;
import structures.basic.Unit;

import java.util.ArrayList;

/**
 * Handler of the special abilities of codes.
 * @author Yu-Sung Hsu (2540296h@student.gla.ac.uk)
 * @author Anamika Maurya (2570847m@student.gla.ac.uk)
 */

public class SpecialAbilities {
    GameState parent;
    ArrayList<Unit> knights = new ArrayList<>();
    Players angryKnight = null;

    public SpecialAbilities(GameState gameState) {
        parent = gameState;
    }

    // @author Yu-Sung Hsu (2540296h@student.gla.ac.uk)
    public void unitDidDie(ActorRef out, Unit unit) {
        if (unit.getType() == UnitType.SILVERGUARD_KNIGHT) {
            knights.remove(unit);
        } else if (unit.getType() != UnitType.WINDSHRIKE) {
            return;
        }
        // Windshrike: When this unit dies, its owner draws a card.
        Players player = (parent.getTurn() == Players.PLAYER1) ? Players.PLAYER2 : Players.PLAYER1;
        parent.getCardDrawing().drawNewCardFor(out, player);
    }


    // @author Anamika Maurya (2570847m@student.gla.ac.uk)
    public void unitIsSummoned(ActorRef out, Unit unit) {
        switch (unit.getType()) {
            case AZURE_HERALD:
                int newHealth = parent.getPlayer(unit.getPlayerID()).getHealth() + 3;
                if(newHealth > 20)
                    newHealth = 20;
                parent.getPlayer(unit.getPlayerID()).setHealth(newHealth);
                new PlayerSetCommandsBuilder(out, parent.isSimulation())
                        .setPlayer(unit.getPlayerID())
                        .setStats(PlayerStats.HEALTH)
                        .setInstance(parent.getPlayer(unit.getPlayerID()))
                        .issueCommand();

               new UnitCommandBuilder(out, parent.isSimulation())
                       .setMode(UnitCommandBuilderMode.SET)
                       .setStats(UnitStats.HEALTH, parent.getPlayer(unit.getPlayerID()).getHealth())
                       .setUnit(parent.getAvatar(unit.getPlayerID()))
                       .issueCommand();

                break;
            case BLAZE_HOUND:
                parent.getCardDrawing().drawNewCardFor(out, Players.PLAYER1);
                parent.getCardDrawing().drawNewCardFor(out, Players.PLAYER2);
                break;
            case SILVERGUARD_KNIGHT:
                knights.add(unit);
                break;
        }
    }

    // @author Yu-Sung Hsu (2540296h@student.gla.ac.uk)
    public void unitIsDamaged(ActorRef out, Unit unit) {
        // Silverguard Knight: If your avatar is dealt damage this unit gains +2 attack.
        if (unit.isAvatar() && !knights.isEmpty() && unit.getPlayerID() == knights.get(0).getPlayerID()) {
            if (angryKnight == null) {
                for (Unit knight : knights) {
                    if (unit.getPlayerID() == knight.getPlayerID()) {
                        updateUnitAttack(out, knight, knight.getDamage() + 2);
                    }
                }
            }
            angryKnight = parent.getTurn();
        }
    }

    // @author Yu-Sung Hsu (2540296h@student.gla.ac.uk)
    public void turnDidEnd(ActorRef out) {
        // Remove buff after 1 turn.
        if (angryKnight != null && parent.getTurn() == angryKnight) {
            for (Unit knight: knights) {
                updateUnitAttack(out, knight, knight.getDamage() - 2);
            }
            angryKnight = null;
        }
    }

    // @author Yu-Sung Hsu (2540296h@student.gla.ac.uk)
    private void updateUnitAttack(ActorRef out, Unit unit, int newValue) {
        unit.setDamage(newValue);
        new UnitCommandBuilder(out, parent.isSimulation())
                .setUnit(unit)
                .setMode(UnitCommandBuilderMode.SET)
                .setStats(UnitStats.ATTACK, unit.getDamage())
                .issueCommand();
    }
}
