package model;

import ast.*;
import clientserver.PositionBundle;
import interpret.Interpreter;
import interpret.Result;
import parse.Parser;
import parse.ParserFactory;
import parse.TokenType;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 * The model for the world used to do the simulation
 */
public class World {
   public String name;
   public int version = 0;
   public HashMap<Integer, World> worldPool = new HashMap<>();
   public float rate = 0;
   private int col;
   private int row;
   private int critterID = 1;
   public Hex[][] H;
   public int timeSteps;
   public boolean isRuning = false;
   public ArrayList<Location> critterList = new ArrayList<>();
   private Random rand = new Random();
   public World w;
   public PrintStream out;
   public int numHex;
   public boolean compatiable = false;
   public ArrayList<Integer> deadIDs = new ArrayList<>();
   public HashMap<Integer, Critter> critters;
   private HashSet<String> isOccupied = new HashSet<>();

   /**
    * Initialize the world. Store the information in a Hex array.
    * @param c the number of columns of the world
    * @param r the number of rows of the world
    */
   public World(int c, int r) {
      col = c;
      row = r;
      H = new Hex[c][r];
      for (int i = 0; i < c; i ++) {
         for (int j = 0; j < r; j ++) {
            H[i][j] = new Hex();
            H[i][j].setLoc(new Location(i, j));
            if (isValid(new Location(i, j))) {
               numHex ++;
            }
         }
      }
   }

   public World(){
       H = new Hex[1][1];
   }

   public int getNumEmpty() {
      int count = 0;
      for (int i = 0; i < getNumCols(); i ++) {
         for (int j = 0; j < getNumRows(); j ++) {
            if (H[i][j].isEmpty() && isValid(new Location(i, j))) {
               count ++;
            }
         }
      }
      return count;
   }

   public void init() {
      w = new World(Constants.columns, Constants.rows);
      w.version = 0;
      w.worldPool.put(w.version, w.clone());
      int numEmpty = w.getNumEmpty();
      Random rand = new Random();
      int r = rand.nextInt(numEmpty) + 1;
      int i = 0;
      while (i < r){
         int col_1 = rand.nextInt(Constants.columns);
         int row_1 = rand.nextInt(Constants.rows);
         if (w.H[col_1][row_1].isEmpty() && w.isValid(new Location(col_1, row_1))){
            w.H[col_1][row_1].setRock();
            i++;
         }
          i++;
      }
      w.version = 1;
      w.worldPool.put(1, w.clone());
   }

   public void init(String filename) throws IOException {
      BufferedReader reader;
      String s = "";
      reader = new BufferedReader(new FileReader(filename));
      String line = reader.readLine();
      readWorldInfo(reader, s, line);
   }

   public void initFromDescription(String description) throws IOException{
      BufferedReader reader;
      String s = "";
      reader = new BufferedReader(new StringReader(description));
      String line = reader.readLine();
      readWorldInfo(reader, s, line);
   }

   public World clone() {
      World worldCloned = new World(getNumCols(), getNumRows());
      worldCloned.deadIDs.addAll(deadIDs);
      for (int i = 0; i < critterList.size(); i++) {
         Location loc = critterList.get(i);
         if (H[loc.getCol()][loc.getRow()].hasCritter() && H[loc.getCol()][loc.getRow()].getValue() > 0) {
            Critter cri = H[loc.getCol()][loc.getRow()].getCritter();
            Critter criCloned = new Critter(cri.name, (Program)cri.getProgram().clone(), cri.getMem());
            criCloned.id = cri.getID();
            criCloned.creatorID = cri.getCreator();
            criCloned.setDir(cri.getDir());
            criCloned.col = cri.getCol();
            criCloned.row = cri.getRow();
            worldCloned.H[loc.getCol()][loc.getRow()].setCritter(cri);
         }
      }
      for (int i = 0; i < getNumCols(); i ++) {
         for (int j = 0; j < getNumRows(); j ++) {
            if (isValid(new Location(i, j)) && !H[i][j].isEmpty() && H[i][j].getValue() < 0) {
               worldCloned.H[i][j].setValue(H[i][j].getValue());
            }
         }
      }
      return worldCloned;
   }


   private void readWorldInfo(BufferedReader reader, String s, String line) throws IOException {
      while(line != null){
         String[] tokens = line.split(" ");
         switch(tokens[0]){
         case "name": {
            for(int i = 1; i < tokens.length; i++){
               s += tokens[i] + " ";
            }
            line = reader.readLine();
            break;
         }
         case "size": {
            w = new World(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
            w.version = 0;
            w.worldPool.put(w.version, w.clone());
            w.name = s;
            line = reader.readLine();
            break;
         }
         case "food": {
            w.H[Integer.parseInt(tokens[1])][Integer.parseInt(tokens[2])].setFood(-1 * (Integer.parseInt(tokens[3]) + 1));
//            w.addFood(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3]));
            line = reader.readLine();
            break;
         }
         case "rock": {
            w.H[Integer.parseInt(tokens[1])][Integer.parseInt(tokens[2])].setRock();
//            w.addRock(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
            line = reader.readLine();
            break;
         }
         case "critter": {
            if (w.isValid(new Location(Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3])))) {
               Critter cri = addCritter(tokens[1]);
               cri.setDir(Integer.parseInt(tokens[4]));
               cri.col = Integer.parseInt(tokens[2]);
               cri.row = Integer.parseInt(tokens[3]);
               cri.id = w.critterID;
               w.H[Integer.parseInt(tokens[2])][Integer.parseInt(tokens[3])].setCritter(cri);
               w.addCritterLoc(new Location(Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3])));
               w.critterID ++;
//               w.version ++;
//               w.worldPool.put(w.version, w.clone());
               //what else??
               line = reader.readLine();
            }
            break;
         }
         default:
            line = reader.readLine();
         }
      }
      w.version = 1;
      w.worldPool.put(1, w.clone());
   }

    public static Critter addCritter(String filename){
        BufferedReader reader;
        String name = "";
        ArrayList<Integer> m = new ArrayList<>();
        ArrayList<String> coll = new ArrayList<String>();
        try{
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            while(line != null){
                String[] tokens = line.split(" ");
                switch(tokens[0]){
                    case "species:": {
                        name = tokens[1];
                        line = reader.readLine();
                        break;
                    }
                    case "memsize:": {
                        m.add(Integer.parseInt(tokens[1]));
                        line = reader.readLine();
                        break;
                    }
                    case "defense:": {
                        m.add(Integer.parseInt(tokens[1]));
                        line = reader.readLine();
                        break;
                    }
                    case "offense:": {
                        m.add(Integer.parseInt(tokens[1]));
                        line = reader.readLine();
                        break;
                    }
                    case "size:": {
                        m.add(Integer.parseInt(tokens[1]));
                        line = reader.readLine();
                        break;
                    }
                    case "energy:": {
                        m.add(Integer.parseInt(tokens[1]));
                        m.add(0);
                        m.add(0);
                        line = reader.readLine();
                        break;
                    }
                    case "posture:": {
                        m.add(Integer.parseInt(tokens[1]));
                        line = reader.readLine();
                        break;
                    }
                    default:
                        coll.add(line);
                        line = reader.readLine();
                }

            }
        }catch (IOException e){
            e.printStackTrace();
        }

        StringBuilder buffer = new StringBuilder();
        for(String str : coll){
            buffer.append(str).append("\n");
        }

        BufferedReader br = new BufferedReader(new StringReader(buffer.toString()));
        Reader r = new BufferedReader(br);
        Parser parser = ParserFactory.getParser();
        Program p;
        try {
           p = parser.parse(r);
        }catch (Throwable error){
           return null;
        }
        return new Critter(name, p, m);
    }

    public void addCrittersFromFile(String filename, int n) {
        Random rand = new Random();
        int k = 0;
        while (k < n) {
            Critter cri = addCritter(filename);
            int a = rand.nextInt(getNumCols());
            int b = rand.nextInt(getNumRows());
            while (H[a][b].isEmpty() && isValid(new Location(a, b))) {
                H[a][b].setCritter(cri);
                addCritterLoc(new Location(a, b));
                k++;
            }
        }
    }

    public ArrayList<Integer> addCritter(String s, String pro, int[] m, PositionBundle[] pos, int session_id) {
       ArrayList<Integer> ids = new ArrayList<>();
       for (int i = 0; i < pos.length; i ++) {
          int c = pos[i].col;
          int r = pos[i].row;
          int dir = pos[i].direction;
          if (isValid(new Location(c, r))) {
             BufferedReader br = new BufferedReader(new StringReader(pro));
             Reader reader = new BufferedReader(br);
             Parser parser = ParserFactory.getParser();
             Program p;
             try {
                p = parser.parse(reader);
             }catch (Throwable error){
                return null;
             }
             ArrayList<Integer> m2 = new ArrayList<>(m.length);
             for(int k2 = 0; k2 < m.length; k2++){
                m2.add(m[k2]);
             }
             Critter cri = new Critter(s, p, m2);
             cri.col = c;
             cri.row = r;
             cri.setDir(dir);
             cri.id = critterID;
             cri.creatorID = session_id;
             H[c][r].setCritter(cri);
             addCritterLoc(new Location(c, r));
             ids.add(critterID);
             critterID ++;
             version ++;
             worldPool.put(version, clone());
          }
       }
       return ids;
    }

   public ArrayList<Integer> addCritter(String s, String pro, int[] m, int n, int session_id) {
      ArrayList<Integer> ids = new ArrayList<>();
      Random rand = new Random();
      int k = 0;
      while (k < n) {
         int a = rand.nextInt(getNumCols());
         int b = rand.nextInt(getNumRows());
         if (isValid(new Location(a, b))) {
            BufferedReader br = new BufferedReader(new StringReader(pro));
            Reader r = new BufferedReader(br);
            Parser parser = ParserFactory.getParser();
            Program p;
            try {
               p = parser.parse(r);
            }catch (Throwable error){
               return null;
            }
            ArrayList<Integer> m2 = new ArrayList<>(m.length);
            for(int k2 = 0; k2 < m.length; k2++){
               m2.add(m[k2]);
            }
            Critter cri = new Critter(s, p, m2);
            cri.col = a;
            cri.row = b;
            cri.setDir(rand.nextInt(6));
            cri.id = critterID;
            cri.creatorID = session_id;
            ids.add(critterID);
            critterID ++;
            H[a][b].setCritter(cri);
            addCritterLoc(new Location(a, b));
            version ++;
            worldPool.put(version, clone());
            k++;
         }
      }
      return ids;
   }

   public void addRock(int c, int r) {
      H[c][r].setRock();
      version ++;
      worldPool.put(version, clone());
   }

   public void addFood(int c, int r, int value) {
      H[c][r].setFood(-1 * value - 1);
      version ++;
      worldPool.put(version, clone());
   }

   /**
    * Do one turn for each critter in the world (One step)
    */
   public void execute() {
      for (int i = 0; i < critterList.size(); i ++) {
         Location loc = getCritterLoc(i);
         if (isValid(loc)) {
            Hex hex = H[loc.getCol()][loc.getRow()];
            if (hex.hasCritter()) {
               try {
                  System.out.println("loc: " + loc.toString());
                  hex.getCritter().removeAheadAll();
                  getSurroundings(hex);
                  getAllFront(hex);
                  hex.getCritter().s = new ShortestPath(loc, hex.getCritter().getDir(), this);
//                  System.out.println(hex.getCritter().getProgram().prettyPrint(new StringBuilder()).toString());
                  Result result = new Interpreter().getInterpret(hex.getCritter().getProgram(), hex.getCritter());
                  System.out.println("result: " + result);
                  if (result == null) {
                     critterWait(i);
                  }
                  else {
                     hex.getCritter().setLastRule(result.getRule());
                     whichAction(result.getAction(), i);
                  }
                  if (hex.hasCritter() && hex.getCritter().isDead()) {
                     deadIDs.add(hex.getCritter().getID());
                     int foodEnergy = hex.getCritter().toFood();
                     H[loc.getCol()][loc.getRow()].setEmpty();
                     H[loc.getCol()][loc.getRow()].setFood(foodEnergy);
                  }
                  else if (!hex.hasCritter()) {
                     if (isValid(getFront(hex)) && H[getFront(hex).getCol()][getFront(hex).getRow()].hasCritter() && H[getFront(hex).getCol()][getFront(hex).getRow()].getCritter().isDead()) {
                        deadIDs.add(H[getFront(hex).getCol()][getFront(hex).getRow()].getCritter().getID());
                        int foodEnergy = H[getFront(hex).getCol()][getFront(hex).getRow()].getCritter().toFood();
                        H[getFront(hex).getCol()][getFront(hex).getRow()].setEmpty();
                        H[getFront(hex).getCol()][getFront(hex).getRow()].setFood(foodEnergy);
                     }
                     else if (isValid(getBack(hex)) && H[getBack(hex).getCol()][getBack(hex).getRow()].hasCritter() && H[getBack(hex).getCol()][getBack(hex).getRow()].getCritter().isDead()) {
                        deadIDs.add(H[getBack(hex).getCol()][getBack(hex).getRow()].getCritter().getID());
                        int foodEnergy = H[getBack(hex).getCol()][getBack(hex).getRow()].getCritter().toFood();
                        H[getBack(hex).getCol()][getBack(hex).getRow()].setEmpty();
                        H[getBack(hex).getCol()][getBack(hex).getRow()].setFood(foodEnergy);
                     }
                  }
                  version ++;
                  worldPool.put(version, clone());
               } catch (Exception e) {
                  e.printStackTrace();
                  System.out.println(hex.getCritter().getProgram().prettyPrint(new StringBuilder()));
               }
            }
         }
      }
      removeDeadCritter();
      timeSteps++;
   }

   public int getCritterID() {
      return critterID;
   }

   /**
    * @return the number of columns of the world
    */
   public int getNumCols() {
      return col;
   }

   /**
    * @return the numebr of rows of the world
    */
   public int getNumRows() {
      return row;
   }

   /** Add the location of the critters in a list stored in the world
    * @param loc the location of the critter
    */
   public void addCritterLoc(Location loc) {
      if (!isOccupied.contains(loc.toString())) {
         isOccupied.add(loc.toString());
         critterList.add(loc);
      }
      else {
         for (int i = 0; i < critterList.size(); i ++) {
            if (critterList.get(i).equals(loc)) {
               critterList.remove(i);
               break;
            }
         }
         critterList.add(loc);
      }
   }

//   public void storeCritterLoc(int index, Location loc) {
//      critterList.set(index, loc);
//   }

   /**
    * @param index the index of the critter
    * @return the location of the critter
    */
   public Location getCritterLoc(int index) {
      return critterList.get(index);
   }

   /**
    * Check whether a location is valid in the world
    * @param loc
    * @return true or false
    */
   public boolean isValid(Location loc) {
      if (loc == null || loc.getCol() >= getNumCols() || loc.getRow() >= getNumRows() || loc.getCol() < 0 || loc.getRow() < 0) {
         return false;
      }
      return 2 * loc.getRow() - loc.getCol() >= 0 && 2 * loc.getRow() - loc.getCol() < 2 * getNumRows() - getNumCols();
//       return loc.getRow() >= loc.getCol() && loc.getRow() <= loc.getCol() + getNumRows() - 1;
   }

   /**
    * Update the surroundings of a critter used for sensing
    * @param hex the hex that stores the critter
    */
   public void getSurroundings(Hex hex) {
      for (int i = 0; i < 6; i ++) {
         if (isValid(hex.getLoc().addDir(i))) {
            hex.getCritter().setSurroundings(i, H[hex.getLoc().addDir(i).getCol()][hex.getLoc().addDir(i).getRow()].getValue());
         }
         else {
            hex.getCritter().setSurroundings(i, -1);
         }
      }
   }

   /**
    * Get the front location of the critter
    * @param hex the hex that stores the target critter
    * @return the location in front of the hex
    */
   public Location getFront(Hex hex) {
      if (isValid(hex.getLoc())) {
         Location loc = hex.getLoc();
         if (hex.hasCritter()) {
            Critter c = hex.getCritter();
            return loc.addDir(c.getDir());
         }
      }
      return null;
   }

   /**
    * Get the back location of the critter
    * @param hex the hex that stores the target critter
    * @return the location behind the hex
    */
   public Location getBack(Hex hex) {
      if (isValid(hex.getLoc())) {
         Location loc = hex.getLoc();
         if (H[loc.getCol()][loc.getRow()].hasCritter()) {
            Critter c = H[loc.getCol()][loc.getRow()].getCritter();
            return loc.addDir((c.getDir() + 3) % 6);
         }
      }
      return null;
   }

   /**
    * Update the ahead environment for the critters used for sensing
    * @param hex the hex that stores the target critter
    */
   public void getAllFront(Hex hex) {
      if (isValid(hex.getLoc()) && hex.hasCritter()) {
         Critter c = hex.getCritter();
         c.setAheadAll(hex.getValue());
         Location frontLoc = new Location(getFront(hex).getCol(), getFront(hex).getRow());
         while (isValid(frontLoc)) {
            c.setAheadAll(H[frontLoc.getCol()][frontLoc.getRow()].getValue());
            frontLoc = frontLoc.addDir(c.getDir());
         }
      }
   }

   /**
    * Decide which action to perform based on the result of the interpreter
    * @param action action returned by the interpreter
    * @param index the index of the critter in the world
    */
   public void whichAction(Action action, int index) {
      String actionType = action.getValue();
      if (actionType.equals(TokenType.WAIT.toString())) {
         critterWait(index);
      }
      else if (actionType.equals(TokenType.FORWARD.toString())) {
         critterForward(index);
      }
      else if (actionType.equals(TokenType.BACKWARD.toString())) {
         critterBackward(index);
      }
      else if (actionType.equals(TokenType.LEFT.toString())) {
         critterTurnLeft(index);
      }
      else if (actionType.equals(TokenType.RIGHT.toString())) {
         critterTurnRight(index);
      }
      else if (actionType.equals(TokenType.ATTACK.toString())) {
         critterAttack(index);
      }
      else if (actionType.equals(TokenType.EAT.toString())) {
         critterEat(index);
      }
      else if (actionType.equals(TokenType.GROW.toString())) {
         critterGrow(index);
      }
      else if (actionType.equals(TokenType.BUD.toString())) {
         critterBud(index);
      }
      else if (actionType.equals(TokenType.MATE.toString())) {
         critterMate(index);
      }
      else if (actionType.equals(TokenType.SERVE.toString())) {
         critterServe(index, new Interpreter().interpretExpression((Expr)action.getChildren().get(0), H[getCritterLoc(index).getCol()][getCritterLoc(index).getRow()].getCritter()));
      }
      else if (actionType.equals(TokenType.TAG.toString())) {
         critterTag(index, new Interpreter().interpretExpression((Expr)action.getChildren().get(0), H[getCritterLoc(index).getCol()][getCritterLoc(index).getRow()].getCritter()));
      }
   }

   /**
    * Check whether the critter is allowed to move the target location
    * @param hex the hex that stores the target critter
    * @param loc the location to that the critter want to move to
    * @return true or false
    */
   public boolean movable(Hex hex, Location loc) {
      return !hex.getCritter().isDead() && isValid(loc) && (getFront(hex).equals(loc) || getBack(hex).equals(loc)) && H[loc.getCol()][loc.getRow()].isEmpty();
   }

   /**
    * Make the critter execute move action.
    * @param loc the location to that the critter want to move to
    * @param index the index of the critter in the world
    */
   public void critterMove(Location loc, int index) {
      Hex hex = H[getCritterLoc(index).getCol()][getCritterLoc(index).getRow()];
      Critter c = hex.getCritter();
      c.move();
      if (movable(hex, loc)) {
         H[loc.getCol()][loc.getRow()].setCritter(c);
         isOccupied.remove(critterList.get(index).toString());
         critterList.set(index, loc);
         isOccupied.add(critterList.get(index).toString());
         hex.setEmpty();
      }
   }

   /**
    * Make the critter move forward
    * @param index the index of the critter in the world
    */
   public void critterForward(int index) {
      critterMove(getFront(H[getCritterLoc(index).getCol()][getCritterLoc(index).getRow()]), index);
   }

   /**
    * Make the critter move backward
    * @param index the index of the critter in the world
    */
   public void critterBackward(int index) {
      critterMove(getBack(H[getCritterLoc(index).getCol()][getCritterLoc(index).getRow()]), index);
   }

   /**
    * Execute wait action of the critter
    * @param index the index of the critter in the world
    */
   public void critterWait(int index) {
      H[getCritterLoc(index).getCol()][getCritterLoc(index).getRow()].getCritter().waitAction();
   }

   /**
    * Make the critter turn left (turn action)
    * @param index the index of the critter in the world
    */
   public void critterTurnLeft(int index) {
      H[getCritterLoc(index).getCol()][getCritterLoc(index).getRow()].getCritter().turnLeft();
   }

   /**
    * Make the critter turn right (turn action)
    * @param index the index of the critter in the world
    */
   public void critterTurnRight(int index) {
      H[getCritterLoc(index).getCol()][getCritterLoc(index).getRow()].getCritter().turnRight();
   }

   /**
    * Execute the tag action of the critter
    * @param index the index of the critter in the world
    * @param n the value that the critter want to tag
    */
   public void critterTag(int index, int n) {
      Hex hex = H[getCritterLoc(index).getCol()][getCritterLoc(index).getRow()];
      Critter c = hex.getCritter();
      int tagValue = c.tag(n);
      if (c.mem(4) >= 0 && tagValue != -1 && isValid(getFront(hex)) && H[getFront(hex).getCol()][getFront(hex).getRow()].hasCritter()) {
         H[getFront(hex).getCol()][getFront(hex).getRow()].getCritter().setMem(6, n);
      }
   }

   /**
    * Execute the serve action of the critter
    * @param index the index of the critter in the world
    * @param energy the energy that the critter want to serve
    */
   public void critterServe(int index, int energy) {
      Hex hex = H[getCritterLoc(index).getCol()][getCritterLoc(index).getRow()];
      Critter c = hex.getCritter();
      int energyValue = c.serve(energy);
      if (isValid(getFront(hex))) {
         if (H[getFront(hex).getCol()][getFront(hex).getRow()].isEmpty()) {
            H[getFront(hex).getCol()][getFront(hex).getRow()].setFood(-energyValue - 1);
         } else if (H[getFront(hex).getCol()][getFront(hex).getRow()].hasFood()) {
            int foodValue = H[getFront(hex).getCol()][getFront(hex).getRow()].getValue();
            H[getFront(hex).getCol()][getFront(hex).getRow()].setFood(foodValue - energyValue);
         }
      }
   }

   /**
    * Execute the bud action of the critter
    * @param index the index of the critter in the world
    */
   public void critterBud(int index) {
      Hex hex = H[getCritterLoc(index).getCol()][getCritterLoc(index).getRow()];
      Critter c = hex.getCritter();
      c.bud();
      if (c.mem(4) >= 0 && isValid(getBack(hex))) {
         Hex back = H[getBack(hex).getCol()][getBack(hex).getRow()];
         if (back.isEmpty()) {
            back.setCritter(new Critter(c.name + "_child_" + Integer.toString(c.numChildren), (Program)c.getProgram().clone(), c.getMem()));
            c.numChildren ++;
            Critter child = back.getCritter();
            while (rand.nextInt(4) < 1) {
               // Equally chance of choosing rule set mutations or attributes mutations
               if (rand.nextInt(2) == 0) {
                  if (!compatiable) {
                     child.getProgram().mutate();
                  }
                  else child.getProgram().mutateCompatible();
               }
               else child.mutate();
            }
            child.setMem(3, 1);
            child.setMem(4, Constants.initialEnergy);
            for (int i = 5; i < child.getMem().size(); i ++) {
               child.setMem(i, 0);
            }
            critterList.add(getBack(hex));
         }
      }
   }

   /**
    * Execute the mate action of the critter
    * @param index the index of the critter in the world
    */
   public void critterMate(int index) {
      Hex hex = H[getCritterLoc(index).getCol()][getCritterLoc(index).getRow()];
      Critter c = hex.getCritter();
      if (isValid(getFront(hex))) {
         Hex front = H[getFront(hex).getCol()][getFront(hex).getRow()];
         if (front.hasCritter()) {
            Critter partner = front.getCritter();
            if (getFront(hex).addDir(partner.getDir()).equals(getCritterLoc(index))) {
               Location backLoc = getBack(hex);
               Location partnerBackLoc = getFront(hex).addDir(c.getDir());
               if (isValid(backLoc) || isValid(partnerBackLoc)) {
                  boolean temp;
                  Location chosenLoc = new Location(0, 0);
                  if (isValid(backLoc) && isValid(partnerBackLoc)) {
                     if (H[backLoc.getCol()][backLoc.getRow()].isEmpty() || H[partnerBackLoc.getCol()][partnerBackLoc.getRow()].isEmpty()) {
                        temp = true;
                        if (H[backLoc.getCol()][backLoc.getRow()].isEmpty() && H[partnerBackLoc.getCol()][partnerBackLoc.getRow()].isEmpty()) {
                           chosenLoc = rand.nextBoolean() ? backLoc : partnerBackLoc;
                        }
                        else {
                           chosenLoc = H[backLoc.getCol()][backLoc.getRow()].isEmpty() ? backLoc : partnerBackLoc;
                        }
                     }
                     else temp = false;
                  }
                  else {
                     chosenLoc = isValid(backLoc) ? backLoc : partnerBackLoc;
                     temp = H[chosenLoc.getCol()][chosenLoc.getRow()].isEmpty();
                  }
                  if (temp) {
                     c.mate();
                     partner.mate();
                     if (c.mem(4) >= 0 && partner.mem(4) >= 0) {
                        Hex childHex = H[chosenLoc.getCol()][chosenLoc.getRow()];
                        ArrayList<Node> childRule = new ArrayList<>();
                        int ruleNum = rand.nextBoolean() ? c.getProgram().getChildren().size() : partner.getProgram().getChildren().size();
                        for (int i = 0; i < ruleNum; i ++) {
                           if (i < c.getProgram().getChildren().size() && i < partner.getProgram().getChildren().size()) {
                              childRule.add(rand.nextBoolean() ? c.getProgram().getChildren().get(i).clone() : partner.getProgram().getChildren().get(i).clone());
                           }
                           else {
                              childRule.add(i < c.getProgram().getChildren().size() ? c.getProgram().getChildren().get(i).clone() : partner.getProgram().getChildren().get(i).clone());
                           }
                        }
                        Program childProgram = new ProgramImpl(null, childRule);
                        childHex.setCritter(new Critter(c.name + "_child_" + Integer.toString(c.numChildren), childProgram, c.getMem()));
                        Critter child = childHex.getCritter();
                        while (rand.nextInt(4) < 1) {
                           // Equally chance of choosing rule set mutations or attributes mutations
                           if (rand.nextInt(2) == 0) {
                              if (!compatiable) {
                                 child.getProgram().mutate();
                              }
                              else child.getProgram().mutateCompatible();
                           }
                           else child.mutate();
                        }
                        for (int i = 0; i < 3; i ++) {
                           child.setMem(i, rand.nextBoolean() ? c.mem(i) : partner.mem(i));
                        }
                        child.setMem(3, 1);
                        child.setMem(4, Constants.initialEnergy);
                        for (int i = 5; i < child.getMem().size(); i ++) {
                           child.setMem(i, 0);
                        }
                        c.numChildren ++;
                        critterList.add(chosenLoc);
                        return;
                     }
                  }
               }
            }
         }
      }
      c.setMem(4, c.mem(4) - c.mem(3));
   }

   /**
    * Execute the grow action of the critter
    * @param index the index of the critter in the world
    */
   public void critterGrow(int index) {
      H[getCritterLoc(index).getCol()][getCritterLoc(index).getRow()].getCritter().grow();
   }

   /**
    * Execute the attack action of the critter
    * @param index the index of the critter in the world
    */
   public void critterAttack(int index) {
      Hex hex = H[getCritterLoc(index).getCol()][getCritterLoc(index).getRow()];
      Location loc = getFront(hex);
      Critter c = hex.getCritter();
      c.attack();
      if (c.mem(4) >= 0 && isValid(loc) && H[loc.getCol()][loc.getRow()].hasCritter()) {
         Critter enemy = H[loc.getCol()][loc.getRow()].getCritter();
         int damage = (int) Math.round((double)Constants.baseDamage * (double)c.mem(3) / (1 + Math.exp(-(Constants.damageInc * (double)(c.mem(3) * c.mem(2) - enemy.mem(3) * enemy.mem(1))))));
         enemy.setMem(4, enemy.mem(4) - damage);
         if (enemy.isDead()) {
            int foodEnergy = c.toFood();
            H[loc.getCol()][loc.getRow()].setEmpty();
            H[loc.getCol()][loc.getRow()].setFood(foodEnergy);
         }
      }
   }

   /**
    * Execute the eat action of the critter
    * @param index the index of the critter in the world
    */
   public void critterEat(int index) {
      Hex hex = H[getCritterLoc(index).getCol()][getCritterLoc(index).getRow()];
      Critter c = hex.getCritter();
      int energyLeft = c.eat();
      if (!c.isDead() && H[getFront(hex).getCol()][getFront(hex).getRow()].hasFood()) {
         H[getFront(hex).getCol()][getFront(hex).getRow()].setFood(energyLeft);
      }
   }

   /**
    * Remove the location that contains a dead critter from the list of critter locations stored in the world after each step
    */
   //Update the critterList after all critters got one passed
   public void removeDeadCritter() {
      for (int i = critterList.size() - 1; i >= 0; i--) {
         Location loc = critterList.get(i);
         if (!H[loc.getCol()][loc.getRow()].hasCritter()) {
               critterList.remove(loc);
               isOccupied.remove(loc.toString());
         }
      }
   }

//   public void update(HexBundle[] HexList){
//       for(HexBundle hb : HexList){
//           if(hb.type.equals("rock")){
//               w.H[hb.col][hb.row].setRock();
//           }else if(hb.type.equals("food")){
//               w.H[hb.col][hb.row].setFood(hb.value);
//           }else if(hb.type.equals("critter")){
//               int critter_id = hb.id;
//               Critter cri = critters.get(critter_id);
//               if(cri == null){
//                   cri = new Critter(hb.id, hb.species_id, hb.row, hb.col, hb.direction, hb.mem);
//                   critters.put(cri.id, cri);
//               }else{
//                   cri.name = String.valueOf(hb.species_id);
//                   cri.setDir(hb.direction);
//                   cri.mem = hb.mem;
//               }
//               if(hb.program != null){
//                   BufferedReader br = new BufferedReader(new StringReader(hb.program));
//                   Reader r = new BufferedReader(br);
//                   Parser parser = ParserFactory.getParser();
//                   Program p = parser.parse(r);
//                   cri.program = p;
//                   if(hb.recently_executed_rule == -1){
//                       cri.lastRule = null;
//                   }else{
//                      //update the lastRule given the number
//                   }
//               }
//           }
//       }
//   }
}
