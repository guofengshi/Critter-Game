package console;

import model.*;

import java.io.*;
import java.util.Scanner;
import java.util.Random;

/**
 * The console user interface for Assignment 5.
 */
public class Console {
   private Scanner scan;
   public boolean done;
   public PrintStream out;
   public World world;

   /* =========================== */
   /* DO NOT EDIT ABOVE THIS LINE */
   /* (except imports...) */
   /* =========================== */


   /**
    * Starts new random world simulation.
    */
   public void newWorld() {
      world = new World(Constants.columns, Constants.rows);
      System.out.println(world.H[0][0].getValue());
      Random rand = new Random();
      int r = rand.nextInt(Constants.columns * Constants.rows) + 1;
      int i = 0;
      while (i < r){
         int col_1 = rand.nextInt(Constants.columns);
         int row_1 = rand.nextInt(Constants.rows);
         if (world.H[col_1][row_1].isEmpty() && world.isValid(new Location(col_1, row_1))){
            world.H[col_1][row_1].setRock();
            i++;
         }
      }
   }

   /**
    * Starts new simulation with world specified in filename.
    *
    * @param filename
    */
   public void loadWorld(String filename) {
       world = new World(100, 150);
      try (FileReader f = new FileReader(filename)){
          BufferedReader reader = new BufferedReader(f);
//          world.init(reader, world);
      }catch (FileNotFoundException e) {
          out.println("World file" + filename + " not found");
      }catch (IOException e) {
          out.println("Error initializing world");
          done = true;
      }
   }


   /**
    * Loads critter definition from filename and randomly places n critters with
    * that definition into the world.
    *
    * @param filename
    * @param n
    */
   public void loadCritters(String filename, int n) {
      if (world == null){
          out.println("Please initialize the world");
          return;
      }
      world.addCrittersFromFile(filename, n);
      }

   /**
    * Advances the world by n time steps.
    *
    * @param n
    */
   public void advanceTime(int n) {
       if (world == null){
           out.println("Please initialize the world.");
           return;
       }
       for (int i = 0; i < n; i++){
           world.execute();
       }
   }

   /**
    * Prints current time step, number of critters, and world map of the
    * simulation.
    */
   public void worldInfo() {
      if (world == null) {
          out.println("Please initialize the world");
          return;
      }
      worldInfo(world.timeSteps, world.critterList.size());
      String[][] trans;
       if(world.getNumCols() % 2 == 0){
           trans = new String[2*(world.getNumRows() - world.getNumCols()/2) + 1][world.getNumCols()];
       }else{
           trans = new String[2*(world.getNumRows() - world.getNumCols()/2)][world.getNumCols()];
       }
       for(int j = 0; j < trans.length; j++){
           for(int k = 0; k < trans[0].length; k++){

               if(world.getNumCols() % 2 == 1 && ((j % 2 == 0 && k % 2 == 0) || (j % 2 == 1 && k % 2 == 1))){
                   trans[j][k] = " ";
               }else if(world.getNumCols() % 2 == 0 && ((j % 2 == 0 && k % 2 == 1) || (j % 2 == 1 && k % 2 == 0))){
                   trans[j][k] = " ";
               }else{
                   if(k % 2 == 0){
                       int r = (trans.length - j)/2 + k/2;
                       int c = k;
                       if(c < world.H.length && r < world.H[0].length && c >= 0 && r >= 0){
                           if(world.H[c][r].hasRock()){
                               trans[j][k] = "#" ;
                           }else if(world.H[c][r].hasFood()){
                               trans[j][k] = "F";
                           }else if(world.H[c][r].hasCritter()){
                               trans[j][k] = Integer.toString(world.H[c][r].getCritter().getDir());
                           }else if(world.H[c][r].getValue() == 0){
                               trans[j][k] = "-";
                           }
                       }else{
                           trans[j][k] = "-";
                       }
                   }else if(k % 2 == 1){
                       int r = (trans.length - j - 1)/2 + (k - 1)/2 + 1;
                       int c = k;
                       if(c < world.H.length && r < world.H[0].length && c >= 0 && r >= 0){
                           if(world.H[c][r].hasRock()){
                               trans[j][k] = "#";
                           }else if(world.H[c][r].hasFood()){
                               trans[j][k] = "F";
                           }else if(world.H[c][r].hasCritter()){
                               trans[j][k] = Integer.toString(world.H[c][r].getCritter().getDir());
                           }else if(world.H[c][r].getValue() == 0){
                               trans[j][k] = "-";
                           }
                       }else{
                           trans[j][k] = "-";
                       }
                   }
               }
           }
       }
       for (int m = 0; m < trans.length; m++){
          for(int n = 0; n <trans[0].length; n++){
              System.out.print(trans[m][n] + " ");
          }
          System.out.println();
      }
   }

   /**
    * Prints description of the contents of hex (c,r).
    *
    * @param c
    *           column of hex
    * @param r
    *           row of hex
    */
   public void hexInfo(int c, int r) {
       if (world.H[c][r].hasCritter()){
           Critter ch = world.H[c][r].getCritter();
           int s = ch.getMem().size();
           int[] m = new int[s];
           for (int i = 0; i < s; i++){
               m[i] = ch.getMem().get(i);
           }
           critterInfo(ch.name, m, ch.getProgram().prettyPrint(new StringBuilder()).toString(), ch.getLastRule());
       }else{
           terrainInfo(world.H[c][r].getValue());
       }
   }

   /* =========================== */
   /* DO NOT EDIT BELOW THIS LINE */
   /* =========================== */

   /**
    * Be sure to call this function, we will override it to grade.
    *
    * @param numSteps
    *           The number of steps that have passed in the world.
    * @param crittersAlive
    *           The number of critters currently alive.
    */
   protected void worldInfo(int numSteps, int crittersAlive) {
      out.println("steps: " + numSteps);
      out.println("critters: " + crittersAlive);
   }

   /**
    * Be sure to call this function, we will override it to grade.
    *
    * @param species
    *           The species of the critter.
    * @param mem
    *           The memory of the critter.
    * @param program
    *           The program of the critter pretty printed as a String. This
    *           should be able to be parsed back to the same AST.
    * @param lastrule
    *           The last rule executed by the critter pretty printed as a
    *           String. This should be able to be parsed back to the same AST.
    *           If no rule has been executed, this parameter should be null.
    */
   protected void critterInfo(String species, int[] mem, String program, String lastrule) {
      out.println("Species: " + species);
      StringBuilder sbmem = new StringBuilder();
      for (int i : mem) {
         sbmem.append(" ").append(i);
      }
      out.println("Memory:" + sbmem.toString());
      out.println("Program: " + program);
      out.println("Last rule: " + lastrule);
   }

   /**
    * Be sure to call this function, we will override it to grade.
    *
    * @param terrain
    *           0 is empty, -1 is rock, -X is (X-1) food
    */
   protected void terrainInfo(int terrain) {
      if (terrain == 0) {
         out.println("Empty");
      } else if (terrain == -1) {
         out.println("Rock");
      } else {
         out.println("Food: " + (-terrain - 1));
      }
   }

   /**
    * Prints a list of possible commands to the standard output.
    */
   public void printHelp() {
      out.println("new: start a new simulation with a random world");
      out.println("load <world_file>: start a new simulation with the world loaded from world_file");
      out.println("critters <critter_file> <n>: add n critters defined by critter_file randomly into the world");
      out.println("step <n>: advance the world by n timesteps");
      out.println("info: print current timestep, number of critters living, and map of world");
      out.println("hex <c> <r>: print contents of hex at column c, row r");
      out.println("exit: exit the program");
   }

   /**
    * Constructs a new Console capable of reading a given input.
    */
   public Console(InputStream in, PrintStream out) {
      this.out = out;
      scan = new Scanner(in);
      done = false;
   }

   /**
    * Constructs a new Console capable of reading the standard input.
    */
   public Console() {
      this(System.in, System.out);
   }

   /**
    * Processes a single console command provided by the user.
    */
   public void handleCommand() {
      out.print("Enter a command or \"help\" for a list of commands.\n> ");
      String command = scan.next();
      switch (command) {
      case "new": {
         newWorld();
         break;
      }
      case "load": {
         String filename = scan.next();
         loadWorld(filename);
         break;
      }
      case "critters": {
         String filename = scan.next();
         int n = scan.nextInt();
         loadCritters(filename, n);
         break;
      }
      case "step": {
         int n = scan.nextInt();
         advanceTime(n);
         break;
      }
      case "info": {
         worldInfo();
         break;
      }
      case "hex": {
         int c = scan.nextInt();
         int r = scan.nextInt();
         hexInfo(c, r);
         break;
      }
      case "help": {
         printHelp();
         break;
      }
      case "exit": {
         done = true;
         break;
      }
      default:
         out.println(command + " is not a valid command.");
      }
   }

   public static void main(String[] args) {
      Console console = new Console();
      while (!console.done) {
         console.handleCommand();
      }

   }

}
