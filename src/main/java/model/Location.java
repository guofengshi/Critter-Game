package model;

/**
 * The location that can be used in the world to find critter
 */
public class Location {
   private int col;
   private int row;

   public Location(int c, int r) {
      col = c;
      row = r;
   }

   /**
    * @return the column of the location
    */
   public int getCol() {
      return col;
   }

   /**
    * @return the row of the location
    */
   public int getRow() {
      return row;
   }

   /**
    * @param dir the direction of the critter in current location
    * @return the location in front of the critter determined by critter's direction
    */
   public Location addDir(int dir) {
      if (dir == 0) {
         return new Location(getCol(), getRow() + 1);
      }
      if (dir == 1) {
         return new Location(getCol() + 1, getRow() + 1);
      }
      if (dir == 2) {
         return new Location(getCol() + 1, getRow());
      }
      if (dir == 3) {
         return new Location(getCol(), getRow() - 1);
      }
      if (dir == 4) {
         return new Location(getCol() - 1, getRow() - 1);
      }
      if (dir == 5) {
         return new Location(getCol() - 1, getRow());
      }
      return null;
   }

   /**
    * Check whether two location are equal
    * @param loc the target location
    * @return true or false
    */
   public boolean equals(Location loc) {
      return loc != null && loc.getCol() == getCol() && loc.getRow() == getRow();
   }

   /**
    * Represent a location as an double
    * @return the double representation of the location
    */
   public String toString() {
      int c = getCol();
      int r = getRow();
      return Integer.toString(c) + "." + Integer.toString(r);
   }

}
