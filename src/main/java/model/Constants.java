package model;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class Constants {
   
   /**
    * World constants
    */
   public static final int baseDamage = 100; // multiplier for all damage done by attacking
   public static final double damageInc = .2; // controls how quickly increased offensive or defensive ability affects damage
   public static final int energyPerSize = 500; // how much energy a critter can have per point of size
   public static final int foodPerSize = 200; // how much food is created per point of size when a critter dies
   public static final int maxSmellDistance = 10; // maximum distance at which food can be sensed
   public static final int rockValue = -1; // value reported when a rock is sensed
   public static final int columns = 50; // default number of columns in the world map
   public static final int rows = 68; // default number of rows in the world map
   public static final int maxRulesPerTurn = 999; // maximum number of rules that can be run per critter turn
   public static final int solarFlux = 1; // energy gained from sun by doing nothing
   public static final int moveCost = 3; // energy cost of moving (per unit size)
   public static final int attackCost = 5; // energy cost of attacking (per unit size)
   public static final int growCost = 1; // energy cost of growing (per size and complexity)
   public static final int budCost = 9; // energy cost of budding (per unit complexity)
   public static final int mateCost = 5; // energy cost of successful mating (per unit complexity)
   public static final int ruleCost = 2; // complexity cost of having a rule
   public static final int abilityCost = 25; // complexity cost of having an ability point
   public static final int initialEnergy = 250; // energy of a newly birthed critter
   public static final int minMemory = 8; // minimum number of memory entries in a critter
   public static final int MAX_SMELL_DISTANCE = 10;

}
