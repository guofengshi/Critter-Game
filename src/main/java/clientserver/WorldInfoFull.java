package clientserver;

import model.World;

import java.util.ArrayList;

public class WorldInfoFull {
   public int current_timestep;
   public int current_version_number;
   public int update_since;
   public float rate;
   public String name;
   public int population;
   public int rows, cols;
   public ArrayList<Integer> dead_critters = new ArrayList<>();
   public ArrayList<HexInfo> state = new ArrayList<>();

   public WorldInfoFull(World world, int oldVersion) {
      current_timestep = world.timeSteps;
      current_version_number = world.version;
      update_since = oldVersion;
      rate = world.rate;
      name = world.name;
      population = world.critterList.size();
      rows = world.getNumRows();
      cols = world.getNumCols();
   }

   protected ArrayList<Integer> getDeadCritters(World world) {
      World oldWorld = world.worldPool.get(update_since);
      if (world.deadIDs.size() == oldWorld.deadIDs.size()) {
         return dead_critters;
      }
      else {
         for (int i = oldWorld.deadIDs.size(); i < world.deadIDs.size(); i ++) {
            dead_critters.add(world.deadIDs.get(i));
         }
         return dead_critters;
      }
   }
}
