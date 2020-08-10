package clientserver;

import model.Critter;


/**
 * this bundle is just part of the information of the critters
 */
public class CritterInfoPart extends CritterInfo {

   public CritterInfoPart(Critter cri) {
      id = cri.getID();
      species_id = cri.name;
      row = cri.getRow();
      col = cri.getCol();
      direction = cri.getDir();
      mem = cri.getMem();
   }

}
