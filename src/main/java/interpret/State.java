package interpret;

/**
 * Critter state
 * 
 * This interface represents the current information about a critter necessary
 * to evaluate the critter's program. It has getters and setters for the
 * critter's memory bank, some sensing methods, and methods to reset and step
 * the critter's pass counter.
 */
public interface State {

   /**
    * Retrieve the contents of a memory location
    * 
    * @param index
    *           the index of the memory cell
    * @return the contents of the cell
    */
   int mem(int index);

   /**
    * Set a memory location to the given value
    * 
    * @param index
    *           the index of the memory cell
    * @param val
    *           the value to set it to
    */
   void setMem(int index, int val);

   /**
    * Report the contents of the adjacent hex in direction dir.
    * 
    * @param dir
    *           the direction 0 <= dir <= 5, clockwise relative to the current
    *           orientation (0 = straight ahead).
    * @return the contents of the hex (See sec. 7 of the Project Specification
    *         for the coding of hex contents).
    */
   int nearby(int dir);

   /**
    * Report the contents of the adjacent hex straight ahead at distance dist.
    * 
    * @param dist
    *           the distance (0 = the hex occupied by this critter)
    * @return the contents of the hex (See sec. 7 of the Project Specification
    *         for the coding of hex contents).
    */
   int ahead(int dist);

   /**
    * Reset pass counter to 0
    */
   void reset();

   /**
    * Increment pass counter
    */
   void step();

   int smell();

}
