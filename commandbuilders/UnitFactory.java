package commandbuilders;

import commandbuilders.enums.UnitType;
import structures.basic.Unit;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

public class UnitFactory {
    private static int counter = 0;
    public Unit generateUnit(UnitType type) {
        String conf = "";
        switch (type) {
            case HUMAN:
                conf = StaticConfFiles.humanAvatar;
                break;
            case AI:
                conf = StaticConfFiles.aiAvatar;
                break;
        }
        return BasicObjectBuilders.loadUnit(conf, counter++, Unit.class);
    }
}
