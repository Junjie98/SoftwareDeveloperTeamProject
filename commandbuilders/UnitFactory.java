package commandbuilders;

import commandbuilders.enums.UnitType;
import scala.runtime.Static;
import structures.basic.Card;
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
        }
        return BasicObjectBuilders.loadUnit(conf, counter++, Unit.class);
    }

    public Unit generateUnitByCard(Card card){
        Unit unit = null;
        String cardname = card.getCardname();
        if(cardname.equals("Pureblade Enforcer")) {
            unit = this.generateUnit(UnitType.PUREBLADE_ENFORCER);
        } else if(cardname.equals("Azure Herald")) {
            unit = this.generateUnit(UnitType.AZURE_HERALD);
        } else if(cardname.equals("Azurite Lion")) {
            unit = this.generateUnit(UnitType.AZURITE_LION);
        } else if(cardname.equals("Comodo Charger")) {
            unit = this.generateUnit(UnitType.COMODO_CHARGER);
        } else if(cardname.equals("Fire Spitter")) {
            unit = this.generateUnit(UnitType.FIRE_SPITTER);
        } else if(cardname.equals("Hailstone Golem")) {
            unit = this.generateUnit(UnitType.HAILSTONE_GOLEM);
        } else if(cardname.equals("Ironcliff Guardian")) {
            unit = this.generateUnit(UnitType.IRONCLIFF_GUARDIAN);
        } else if(cardname.equals("Pudeblade Enforcer")) {
            unit = this.generateUnit(UnitType.PUREBLADE_ENFORCER);
        } else if(cardname.equals("Silverguard Knight")) {
            unit = this.generateUnit(UnitType.SILVERGUARD_KNIGHT);
        } else if(cardname.equals("Blaze Hound")) {
            unit = this.generateUnit(UnitType.BLAZE_HOUND);
        } else if(cardname.equals("Bloodshard Golem")) {
            unit = this.generateUnit(UnitType.BLOODSHARD_GOLEM);
        } else if(cardname.equals("Hailstone Golder R")){
            unit = this.generateUnit(UnitType.HAILSTONE_GOLEM_R);
        } else if(cardname.equals("Planar Scout")){
            unit = this.generateUnit(UnitType.PLANAR_SCOUT);
        } else if(cardname.equals("Pyromancer")){
            unit = this.generateUnit(UnitType.PYROMANCER);
        } else if(cardname.equals("Rock_Pulveriser")){
            unit = this.generateUnit(UnitType.ROCK_PULVERISER);
        } else if(cardname.equals("Serpenti")){
            unit = this.generateUnit(UnitType.SERPENTI);
        } else if(cardname.equals("Windshrike")){
            unit = this.generateUnit(UnitType.WINDSHRIKE);
        } else if(cardname.equals("Staff Of Ykir")) {
            unit = this.generateUnit(UnitType.STAFF_OF_YKIR);
        } else if(cardname.equals("Entropic Decay")) {
            unit = this.generateUnit(UnitType.ENTROPIC_DECAY);
        } else if(cardname.equals("Sundrop Elixer")){
            unit = this.generateUnit(UnitType.SUNDROP_ELIXIR);
        }  else if(cardname.equals("Truestrike")) {
            unit = this.generateUnit(UnitType.TRUESTRIKE);
        }else {
            unit = this.generateUnit(UnitType.WINDSHRIKE);
        }
        return unit;
    }
}
