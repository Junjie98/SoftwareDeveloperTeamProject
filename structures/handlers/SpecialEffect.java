package structures.handlers;

import akka.actor.ActorRef;
import commandbuilders.UnitCommandBuilder;
import commandbuilders.enums.Players;
import commandbuilders.enums.UnitCommandBuilderMode;
import commandbuilders.enums.UnitStats;
import commandbuilders.enums.UnitType;
import structures.GameState;
import structures.basic.Unit;

import java.util.ArrayList;

public class SpecialEffect {
    GameState parent;
    ArrayList<Unit> knights = new ArrayList<>();
    Players angryKnight = null;

    public SpecialEffect(GameState gameState) {
        parent = gameState;
    }

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

    public void unitIsSummoned(Unit unit) {
        if (unit.getType() != UnitType.SILVERGUARD_KNIGHT) {
            return;
        }
        knights.add(unit);
    }

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

    public void turnDidEnd(ActorRef out) {
        // Remove buff after 1 turn.
        if (angryKnight != null && parent.getTurn() == angryKnight) {
            for (Unit knight: knights) {
                updateUnitAttack(out, knight, knight.getDamage() - 2);
            }
            angryKnight = null;
        }
    }


    private void updateUnitAttack(ActorRef out, Unit unit, int newValue) {
        unit.setDamage(newValue);
        new UnitCommandBuilder(out, parent.isSimulation())
                .setUnit(unit)
                .setMode(UnitCommandBuilderMode.SET)
                .setStats(UnitStats.ATTACK, unit.getDamage())
                .issueCommand();
    }
}
