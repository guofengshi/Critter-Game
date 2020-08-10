package model;

import ast.Program;
import ast.Rule;
import interpret.State;
import java.util.ArrayList;
import java.util.Random;

/**
 * The critter class
 */
//MEMSIZE mem[0] DEFENSE mem[1] OFFENSE mem[2] SIZE mem[3] ENERGY mem[4] PASS mem[5] TAG mem[6] POSTURE mem[7]
public class Critter implements State {
   public String name;
   public int numChildren = 0;
   public int col;
   public int row;
   public int id = 1;
   public int creatorID;
   private int dir;
   private ArrayList<Integer> aheadAll = new ArrayList<>();
   private int[] surroundings = new int[6];
   public ArrayList<Integer> mem = new ArrayList<>();
   public Program program;
   public Rule lastRule;
   Random rand = new Random();
   public ShortestPath s;
   Location loc;


   /**
    * Initialize the critter
    * @param n the name of the critter
    * @param p the program of the critter
    * @param m the memory ArrayList of the critter
    */
   public Critter(String n, Program p, ArrayList<Integer> m) {
      name = n;
      program = p;
      mem.addAll(m);
   }

   public Critter(String n, Program p, ArrayList<Integer> m, int id, int dir, int rule) {
      name = n;
      program = p;
      mem.addAll(m);
      this.id = id;
      this.dir = dir;
      if (rule != -1) {
         lastRule = (Rule)program.getChildren().get(rule);
      }
   }

   public Critter(String n, ArrayList<Integer> m, int id, int dir) {
      name = n;
      mem.addAll(m);
      this.id = id;
      this.dir = dir;
   }

   public void setLastRule(Rule rule) {
      if (rule != null) {
         lastRule = rule;
      }
   }

   public String getLastRule() {
      if (lastRule != null) {
         return lastRule.prettyPrint(new StringBuilder()).toString();
      }
      else {
         return null;
      }
   }

   public Rule getRecentRule() {
      return lastRule;
   }

   public int getID() {
      return id;
   }

   public int getCreator() {
      return creatorID;
   }

   public int getCol() {
      return col;
   }

   public int getRow() {
      return row;
   }

   /**
    * Deep copy the memory ArrayList of the critter
    * @param list the memory ArrayList
    * @return the cloned list
    */
   public ArrayList<Integer> cloneList(ArrayList<Integer> list) {
      ArrayList<Integer> cloneList = new ArrayList<>(list.size());
      cloneList.addAll(list);
      return cloneList;
   }

   /**
    * @return the program of the critter
    */
   public Program getProgram() {
      return program;
   }

   /**
    * @return the memory ArrayList of the critter
    */
   public ArrayList<Integer> getMem() {
      return mem;
   }

   /**
    * Set the direction of the critter
    * @param dir the target direction
    */
   public void setDir(int dir) {
      this.dir = dir % 6;
   }

   /**
    * Get the direction of the critter
    * @return the direction of the critter
    */
   public int getDir() {
      return dir;
   }

   /**
    * @param index the index of the memory cell
    * @return the value of the mem cell of that index
    */
   @Override
   public int mem(int index) {
      if (index >= 0 && index < mem.size()) {
         return mem.get(index);
      }
      return 0;
   }

   /**
    * Set the value of the certain index memory cell
    * @param index the index of the memory cell
    * @param val the target value
    */
   @Override
   public void setMem(int index, int val) {
      if (index < mem.size()) {
         mem.set(index, val);
      }
   }

   /**
    * Store the surroundings of the critter used for sensing
    * @param i the ith direction
    * @param val the value of the hex in that surroundings with that direction
    */
   public void setSurroundings(int i, int val) {
      surroundings[i] = val;
   }

   /**
    * Store the ahead information in the world in the critter used for sensing
    * @param val the value of a certain hex in the front direction of the critter
    */
   public void setAheadAll(int val) {
      aheadAll.add(val);
   }

   /**
    * Remove the ahead information after one pass
    */
   public void removeAheadAll() {
      aheadAll.clear();
   }

   /**
    * @return the value of the appearance of the critter
    */
   //size ∗ 100,000 + tag ∗ 1,000 + posture ∗ 10 + direction
   public int getAppearance() {
      return mem(3) * 100000 + mem(6) * 1000 + mem(7) * 10 + dir;
   }

   /**
    * @return the value of the complexity of the critter
    */
   public int getComplexity() {
      int numRules = program.getChildren().size();
      return numRules * Constants.ruleCost + (mem(1) + mem(2)) * Constants.abilityCost;
   }

   /**
    * Mutations for the attributes of the critters
    */
   public void mutate() {
      int temp = rand.nextInt(3);
      if (temp == 0) {
         int newSize;
         if (mem(0) != 8) {
            newSize = rand.nextBoolean() ? mem(0) - 1 : mem(0) + 1;
         }
         else newSize = mem(0) + 1;
         setMem(0, newSize);
      }
      else if (temp == 1) {
         int newDefensive;
         if (mem(1) != 1) {
            newDefensive = rand.nextBoolean() ? mem(1) - 1 : mem(1) + 1;
         }
         else newDefensive = mem(1) + 1;
         setMem(1, newDefensive);
      }
      else {
         int newOffensive;
         if (mem(2) != 1) {
            newOffensive = rand.nextBoolean() ? mem(2) - 1 : mem(2) + 1;
         }
         else newOffensive = mem(2) + 1;
         setMem(2, newOffensive);
      }
   }


   /**
    * The wait action of the critter. Certain amount of energy is deducted after executing the action.
    */
   public void waitAction() {
      setMem(4, mem(4) + Constants.solarFlux);
      isEnergyMax();
   }

   /**
    * The eat action of the critter. Certain amount of energy is deducted after executing the action.
    * @return the energy left in the food if the eat is successful. Return 0 if not.
    */
   public int eat() {
      setMem(4, mem(4) - mem(3));
      if (ahead(1) < -1) {
         if (mem(4) >= 0) {
            if (mem(4) - ahead(1) - 1 > mem(3) * Constants.energyPerSize) {
               int energyLeft = -(-ahead(1) - mem(3) * Constants.energyPerSize + mem(4));
               setMem(4, Constants.energyPerSize * mem(3));
               return energyLeft;
            }
            else {
               setMem(4, mem(4) - ahead(1) - 1);
               return 0;
            }
         }
      }
      return 0;
   }

   /**
    * The turn right action of the critter. Certain amount of energy is deducted after executing the action.
    */
   public void turnRight() {
      setDir((getDir() + 1) % 6);
      setMem(4, mem(4) - mem(3));
   }

   /**
    * The turn left action of the critter. Certain amount of energy is deducted after executing the action.
    */
   public void turnLeft() {
      setDir((getDir() + 6 - 1) % 6);
      setMem(4, mem(4) - mem(3));
   }

   /**
    * The move action of the critter. Certain amount of energy is deducted after executing the action.
    */
   public void move() {
      setMem(4, mem(4) - mem(3));
   }

   /**
    * The tag action of the critter. Certain amount of energy is deducted after executing the action.
    * @param n the value that the critter want to tag
    * @return the value that the critter want to tag if the value is valid. Return -1 otherwise.
    */
   public int tag(int n) {
      setMem(4, mem(4) - mem(3));
      if (n > 0 && n <= 99) {
         return n;
      }
      return -1; // if the return value if -1, tag is unsuccessful
   }

   /**
    * The grow action of the critter. Certain amount of energy is deducted after executing the action.
    */
   public void grow() {
      setMem(4, mem(4) - mem(3) * getComplexity() * Constants.growCost);
      setMem(3, mem(3) + 1);
   }

   /**
    * The serve action of the critter. Certain amount of energy is deducted after executing the action.
    * @param energy the amount of the energy that the critter want to serve
    * @return the valid amount of the energy that the critter want to serve
    */
   public int serve(int energy) {
      if (energy <= mem(4) - mem(3) && energy >= 0) {
         setMem(4, mem(4) - mem(3) - energy);
         return energy;
      }
      else if (energy < 0) {
         setMem(4, mem(4) - mem(3));
         return 0;
      }
      else {
         setMem(4, 0);
         return mem(4) - mem(3);
      }
   }

   /**
    * The attack action of the critter. Certain amount of energy is deducted after executing the action.
    */
   public void attack() {
      setMem(4, mem(4) - mem(3) * Constants.attackCost);
   }

   /**
    * The bud action of the critter. Certain amount of energy is deducted after executing the action.
    */
   public void bud() {
      setMem(4, mem(4) - Constants.budCost * getComplexity());
   }

   /**
    * The mate action of the critter. Certain amount of energy is deducted after successfully executing the energy.
    * Otherwise the energy deducted operation is done to the critter in the world class.
    */
   public void mate() {
      setMem(4, mem(4) - Constants.mateCost * getComplexity());
   }

   /**
    * Check whether a critter is dead
    * @return true or false
    */
   public boolean isDead() {
      return mem(4) <= 0;
   }

   /**
    * Convert the critter to food when it is dead
    * @return the value of food the critter can be converted into when it is dead
    */
   public int toFood() {
      return -Constants.foodPerSize * mem(3) - 1;
   }

   /**
    * Check whether a critter reached its maximum energy after gaining energy. If so, set the energy to the maximum.
    */
   public void isEnergyMax() {
      if (mem(4) > Constants.energyPerSize * mem(3)) {
         setMem(4, Constants.energyPerSize * mem(3));
      }
   }

   /**
    * @param dir the direction 0 <= dir <= 5, clockwise relative to the current
    *            orientation (0 = straight ahead).
    * @return the value of the surroundings in that direction
    */
   @Override
   public int nearby(int dir) {
      return surroundings[Math.abs((getDir() + dir) % 6)];
   }

   /**
    * @param dist the distance (0 = the hex occupied by this critter)
    * @return the value of the hex in the front within that distance
    */
   @Override
   public int ahead(int dist) {
      if (dist < 0) {
         return aheadAll.get(0);
      }
      else if (dist < aheadAll.size()) {
         return aheadAll.get(dist);
      }
      return -1;
   }

   /**
    * Reset the pass of the critter to 0
    */
   @Override
   public void reset() {
      setMem(5, 0);
   }

   /**
    * Increase the pass of the critter when one step is executed
    */
   @Override
   public void step() {
      setMem(5, mem(5) + 1);
   }

   public int smell(){
      //System.out.println(this.name + ": smell value: "  +s.computeValue() + " Location: " + s.getFoodLoc().getCol() + " , " + s.getFoodLoc().getRow());
      return s.computeValue();
   }

   /**
    * Determine whether two critters are equal
    * @param cri the target critter
    * @return true or false
    */
   public boolean equals(Critter cri) {
      if (id != cri.getID() || mem.size() != cri.mem.size()) {
         return false;
      }
      else {
         boolean result = true;
         for (int i = 0; i < mem.size(); i ++) {
            if (mem(i) != cri.mem(i)) {
               result = false;
               break;
            }
         }
         return result;
      }
   }
}
