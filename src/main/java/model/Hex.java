package model;

/**
 * The Hex used to store the critters, rock, and food for the world
 */
public class Hex {
   private Critter critter;
   private int value = 0;
   private Location loc;

   /**
    * Check whether a hex is empty
    * @return true or false
    */
   public boolean isEmpty() {
      return value == 0 && critter == null;
   }

   /**
    * Set the hex location to a target location
    * @param loc the target location of the hex
    */
   public void setLoc(Location loc) {
      this.loc = loc;
   }

   /**
    * Get the hex's location
    * @return the hex's location
    */
   public Location getLoc() {
      return loc;
   }

   /**
    * Check whether the hex contains a critter
    * @return true or false
    */
   public boolean hasCritter() {
      return critter != null;
   }

   /**
    * Check whether the hex contains a rock
    * @return true or false
    */
   public boolean hasRock() {
      return value == -1;
   }

   /**
    * Check whether the hex contains food
    * @return true or false
    */
   public boolean hasFood() {
      return value < -1;
   }

   /**
    * @return the value of the hex
    */
   public int getValue() {
      return value;
   }

   public void setValue(int value) {
      this.value = value;
   }

   /**
    * Put a critter into the hex. Return whether this operation is successful
    * @param c target critter
    * @return true or false
    */
   public void setCritter(Critter c) {
      critter = new Critter(c.name, c.getProgram(), c.cloneList(c.getMem()));
      critter.setDir(c.getDir());
      value = c.getAppearance();
      critter.id = c.getID();
      critter.row = c.getRow();
      critter.col = c.getCol();
      critter.creatorID = c.getCreator();
   }

   /**
    * @return the critter stored in the hex
    */
   public Critter getCritter() {
      if (hasCritter()) {
         return critter;
      }
      return null;
   }

   /**
    * Put a rock into the hex. Return whether this operation is successful
    * @return true or false
    */
   public boolean setRock() {
      if (value == 0) {
         value = -1;
         return true;
      }
      return false;
   }

   /**
    * Put certain amount of food into the hex. Return whether this operation is successful
    * @param energy the food value
    * @return true or false
    */
   public boolean setFood(int energy) {
      if (value == 0 || value < -1) {
         value = energy;
         return true;
      }
      return false;
   }

   /**
    * Set a hex back to empty
    */
   public void setEmpty() {
      value = 0;
      if (hasCritter()) {
         critter = null;
      }
   }

   /**
    * Determine whether two hexes are equal
    * @param hex the Hex to compare with
    * @return 0 if they are equal; -1 if the current hex is empty but the compared hex
    * is not; 1 if they are different and the current hex is not empty
    */
   public int equals(Hex hex) {
      if (isEmpty() && !hex.isEmpty()) {
         return -1;
      }
      else if (isEmpty() && hex.isEmpty()) {
         return 0;
      }
      else {
         if (!(hasCritter() == hex.hasCritter())) {
            return 1;
         }
         else {
            if (!hasCritter()) {
               if (getValue() == hex.getValue()) {
                  return 0;
               }
               else {
                  return 1;
               }
            }
            else {
               Critter cri = getCritter();
               Critter criHex = hex.getCritter();
               if (cri.equals(criHex)) {
                  return 0;
               }
               else {
                  return 1;
               }
            }
         }
      }
   }
}
