package commandbuilders;

import commandbuilders.enums.UnitType;
import scala.runtime.Static;
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
            case SERPENTI:
                conf = StaticConfFiles.u_serpenti;
                break;
            case PYROMANCER:
                conf = StaticConfFiles.u_pyromancer;
                break;
            case WINDSHRIKE:
                conf = StaticConfFiles.u_windshrike;
                break;
            case BLAZE_HOUND:
                conf = StaticConfFiles.u_blaze_hound;
                break;
            case AZURE_HERALD:
                conf = StaticConfFiles.u_azure_herald;
                break;
            case AZURITE_LION:
                conf = StaticConfFiles.u_azurite_lion;
                break;
            case FIRE_SPITTER:
                conf = StaticConfFiles.u_fire_spitter;
                break;
            case PLANAR_SCOUT:
                conf = StaticConfFiles.u_planar_scout;
                break;
            case COMODO_CHARGER:
                conf = StaticConfFiles.u_comodo_charger;
                break;
            case HAILSTONE_GOLEM:
                conf = StaticConfFiles.u_hailstone_golem;
                break;
            case ROCK_PULVERISER:
                conf = StaticConfFiles.u_rock_pulveriser;
                break;
            case BLOODSHARD_GOLEM:
                conf = StaticConfFiles.u_bloodshard_golem;
                break;
            case HAILSTONE_GOLEM_R:
                conf = StaticConfFiles.u_hailstone_golemR;
                break;
            case IRONCLIFF_GUARDIAN:
                conf = StaticConfFiles.u_ironcliff_guardian;
                break;
            case PUREBLADE_ENFORCER:
                conf = StaticConfFiles.u_pureblade_enforcer;
                break;
            case SILVERGUARD_KNIGHT:
                conf = StaticConfFiles.u_silverguard_knight;
                break;
            case ENTROPIC_DECAY:                            /// Extra the spells
                conf = StaticConfFiles.c_entropic_decay;
                break;
            case STAFF_OF_YKIR:
                conf = StaticConfFiles.c_staff_of_ykir;
                break;
            case TRUESTRIKE:
                conf = StaticConfFiles.c_truestrike;
                break;
            case SUNDROP_ELIXIR:
                conf = StaticConfFiles.c_sundrop_elixir;
                break;
        }
        return BasicObjectBuilders.loadUnit(conf, counter++, Unit.class);
    }
}
