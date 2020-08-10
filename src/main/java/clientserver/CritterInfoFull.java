package clientserver;

import model.Critter;


/**
 * this bundle is the full information of critters
 */
public class CritterInfoFull extends CritterInfo {

   public CritterInfoFull(Critter cri) {
      id = cri.getID();
      species_id = cri.name;
      program = cri.getProgram().prettyPrint(new StringBuilder()).toString();
      row = cri.getRow();
      col = cri.getCol();
      direction = cri.getDir();
      mem = cri.getMem();
      recently_executed_rule = cri.getProgram().getChildren().indexOf(cri.getRecentRule());
   }
}
