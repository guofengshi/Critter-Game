package clientserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Critter;
import model.Location;
import model.World;
import spark.Request;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static spark.Spark.*;

/**
 * Server for the critter world
 */
public class Server {
   private World world = new World();
   private HashMap<String, String> auth = new HashMap<>();
   private HashMap<Integer, String> session = new HashMap<>();
   private HashMap<Integer, Boolean> hasWorld = new HashMap<>();
   private ScheduledExecutorService timer;
   private int port;
   Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
   private final ReadWriteLock rwlock = new ReentrantReadWriteLock();

   /**
    * @param readPassword the password with read access
    * @param writePassword the password with write access
    * @param adminPassword the password with admin access
    * @param port the port number
    */
   public Server(String readPassword, String writePassword, String adminPassword, int port) {
      auth.put("read", readPassword);
      auth.put("write", writePassword);
      auth.put("admin", adminPassword);
      this.port = port;
   }

   /**
    * Get the session_id when log in and store it
    */
   private class GetID {
      private int session_id;
      public GetID(String level) {
         Random rand = new Random();
         StringBuilder sb = new StringBuilder(10);
         for(int i = 0; i < 10; i++) {
            sb.append((char)('0' + rand.nextInt(10)));
         }
         session_id = Math.abs(new BigInteger(sb.toString()).intValue());
         session.put(session_id, level);
         hasWorld.put(session_id, false);
      }
   }

   /**
    * Get the list of critters ids and the species id and put them into the world
    */
   public class SetCritter {
      String species_id;
      ArrayList<Integer> ids;

      public SetCritter(Request request, int session_id) {
         String json = request.body();
         CritterBundle newCritter = gson.fromJson(json, CritterBundle.class);
         System.out.println("id: " + newCritter.species_id);
         if (newCritter.species_id == null) {
            String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            Random random = new Random();
            StringBuffer sb = new StringBuffer();
            for(int i = 0; i < 7; i ++) {
               int number = random.nextInt(62);
               sb.append(str.charAt(number));
            }
            newCritter.species_id = sb.toString();
         }

         int[] m = newCritter.mem;
         ArrayList<Integer> mem = IntStream.of(m).boxed().collect(Collectors.toCollection(ArrayList::new));

         species_id = newCritter.species_id;
         if (newCritter.num == 0) {
            ids = world.w.addCritter(newCritter.species_id, newCritter.program, newCritter.mem, newCritter.positions, session_id);
         }
         else {
            ids = world.w.addCritter(newCritter.species_id, newCritter.program, newCritter.mem, newCritter.num, session_id);
         }
         //         System.out.println("Critter ID: " + world.w.getCritterID());
         //         System.out.println(world.w.critterList.size());
      }
   }

   public void run() {
      port(port);

      post("/CritterWorld/login", (request, response) -> {
         rwlock.writeLock().lock();
         try {
            response.header("Content-Type", "application/json");
            String json = request.body();
            LoginBundle newLogin = gson.fromJson(json, LoginBundle.class);
            String level = newLogin.level;
            String password = newLogin.password;
            if (auth.get(level) != null && auth.get(level).equals(password)) {
               response.status(200);
               return new GetID(level);
            }
            else {
               response.status(401);
               return "Unauthorized";
            }
         } catch (Exception e) {
            e.printStackTrace();
         } finally {
            rwlock.writeLock().unlock();
         }
         return null;
      }, gson::toJson);

      get("/CritterWorld/critters", (request, response) -> {
         rwlock.readLock().lock();
         try {
            response.header("Content-Type", "application/json");
            int session_id = new BigInteger(request.queryParams("session_id")).intValue();
            ArrayList<CritterInfo> critterInfos = new ArrayList<>();
            if (session.get(session_id) != null && session.get(session_id).equals("admin")) {
               for (int i = 0; i < world.w.critterList.size(); i ++) {
                  critterInfos.add(new CritterInfoFull(world.w.H[world.w.critterList.get(i).getCol()][world.w.critterList.get(i).getRow()].getCritter()));
               }
            }
            else if (session.get(session_id) != null && session.get(session_id).equals("write")) {
               for (int i = 0; i < world.w.critterList.size(); i ++) {
                  Critter cri = world.w.H[world.w.critterList.get(i).getCol()][world.w.critterList.get(i).getRow()].getCritter();
                  if (cri.getCreator() == session_id) {
                     critterInfos.add(new CritterInfoFull(cri));
                  }
                  else {
                     critterInfos.add(new CritterInfoPart(cri));
                  }
               }
            }
            else if (session.get(session_id) != null){
               for (int i = 0; i < world.w.critterList.size(); i ++) {
                  critterInfos.add(new CritterInfoPart(world.w.H[world.w.critterList.get(i).getCol()][world.w.critterList.get(i).getRow()].getCritter()));
               }
            }
            response.status(200);
            return critterInfos;
         } catch (Exception e) {
            e.printStackTrace();
         } finally {
            rwlock.readLock().unlock();
         }
         return null;
      }, gson::toJson);

      post("/CritterWorld/critters", (request, response) -> {
         rwlock.writeLock().lock();
         try {
            response.header("Content-Type", "application/json");
            int session_id = new BigInteger(request.queryParams("session_id")).intValue();
            System.out.println("ts " + session_id);
            if (session.get(session_id) != null && !session.get(session_id).equals("read")) {
               response.status(201);
               return new SetCritter(request, session_id);
            }
            else {
               response.status(401);
               return "Unauthorized";
            }
         } catch (Exception e) {
            e.printStackTrace();
         } finally {
            rwlock.writeLock().unlock();
         }
         return null;
      }, gson::toJson);

      get("/CritterWorld/critter/:id", (request, response) -> {
         rwlock.readLock().lock();
         try {
            response.header("Content-Type", "application/json");
            int id = Integer.parseInt(request.params(":id"));
            int session_id = new BigInteger(request.queryParams("session_id")).intValue();
            if (session.get(session_id) != null && session.get(session_id).equals("admin")) {
               response.status(200);
               for (int i = 0; i < world.w.critterList.size(); i ++) {
                  Critter cri = world.w.H[world.w.critterList.get(i).getCol()][world.w.critterList.get(i).getRow()].getCritter();
                  if (cri.getID() == id) {
                     return new CritterInfoFull(cri);
                  }
               }
               return "the critter with the corresponding id does not exist";
            }
            else if (session.get(session_id) != null && session.get(session_id).equals("write")) {
               response.status(200);
               for (int i = 0; i < world.w.critterList.size(); i ++) {
                  Critter cri = world.w.H[world.w.critterList.get(i).getCol()][world.w.critterList.get(i).getRow()].getCritter();
                  if (cri.getID() == id) {
                     if (cri.getCreator() == session_id) {
                        return new CritterInfoFull(cri);
                     }
                     else {
                        return new CritterInfoPart(cri);
                     }
                  }
               }
               return "the critter with the corresponding id does not exist";
            }
            else if (session.get(session_id) != null) {
               response.status(200);
               for (int i = 0; i < world.w.critterList.size(); i ++) {
                  Critter cri = world.w.H[world.w.critterList.get(i).getCol()][world.w.critterList.get(i).getRow()].getCritter();
                  if (cri.getID() == id) {
                     return new CritterInfoPart(cri);
                  }
               }
               return "the critter with the corresponding id does not exist";
            }
            else {
               response.status(401);
               return "Unauthorized";
            }
         } catch (Exception e) {
            e.printStackTrace();
         } finally {
            rwlock.readLock().unlock();
         }
         return null;
      }, gson::toJson);

      delete("/CritterWorld/critter/:id", (request, response) -> {
         rwlock.writeLock().lock();
         try {
            response.header("Content-Type", "application/json");
            int id = Integer.parseInt(request.params(":id"));
            int session_id = new BigInteger(request.queryParams("session_id")).intValue();
            if (session.get(session_id) != null && session.get(session_id).equals("admin")) {
               response.status(204);
               for (Location loc : world.w.critterList) {
                  if (world.w.H[loc.getCol()][loc.getRow()].getCritter().getID() == id) {
                     world.w.critterList.remove(loc);
                     return 204;
                  }
               }
               return "the critter with the corresponding id does not exist";
            }
            else if (session.get(session_id) != null && session.get(session_id).equals("write")) {
               for (Location loc : world.w.critterList) {
                  Critter cri = world.w.H[loc.getCol()][loc.getRow()].getCritter();
                  if (cri.getID() == id) {
                     if (cri.getCreator() == session_id) {
                        response.status(204);
                        world.w.critterList.remove(loc);
                        return 204;
                     }
                     else {
                        response.status(401);
                        return "Unauthorized";
                     }
                  }
               }
               return "the critter with the corresponding id does not exist";
            }
            else {
               response.status(401);
               return "Unauthorized";
            }
         } catch (Exception e) {
            e.printStackTrace();
         } finally {
            rwlock.writeLock().unlock();
         }
         return null;
      }, gson::toJson);

      post("/CritterWorld/world", (request, response) -> {
         rwlock.writeLock().lock();
         try {
            response.header("Content-Type", "application/json");
            int session_id = new BigInteger(request.queryParams("session_id")).intValue();
            if (session.get(session_id) != null && session.get(session_id).equals("admin")) {
               String json = request.body();
               WorldBundle newWorld = gson.fromJson(json, WorldBundle.class);
               // the empty description is corresponding to the new world function of the gui
               if (newWorld.description.equals("")) {
                  world.init();
               }
               else {
                  world.initFromDescription(newWorld.description);
                  //               world.init(newWorld.description);
                  for (Location loc : world.w.critterList) {
                     world.w.H[loc.getCol()][loc.getRow()].getCritter().creatorID = session_id;
                  }
               }
               response.status(201);
               hasWorld.put(session_id, true);
               return "Ok";
            }
            else {
               response.status(401);
               return "Unauthorized";
            }
         } catch (Exception e) {
            e.printStackTrace();
         } finally {
            rwlock.writeLock().unlock();
         }
         return null;
      }, gson::toJson);

      get("/CritterWorld/world", (request, response) -> {
         rwlock.readLock().lock();
         try {
            response.header("Content-Type", "application/json");
            int session_id = new BigInteger(request.queryParams("session_id")).intValue();
            int update_since = Integer.parseInt(request.queryParamOrDefault("update_since", "0"));
            int from_row = Integer.parseInt(request.queryParamOrDefault("from_row", "0"));
            int to_row = Integer.parseInt(request.queryParamOrDefault("to_row", "0"));
            int from_col = Integer.parseInt(request.queryParamOrDefault("from_col", "0"));
            int to_col = Integer.parseInt(request.queryParamOrDefault("to_col", "0"));
            if (from_col < 0 || from_row < 0 || to_col >= world.w.getNumCols() || to_row >= world.w.getNumRows()) {
               response.status(406);
               return "the rows or cols are invalid";
            }
            else {
               response.status(200);
               WorldInfoFull worldInfoFull = new WorldInfoFull(world.w, update_since);
               if (session.get(session_id) != null) {
                  System.out.println("update_since: " + update_since);
                  World oldWorld = world.w.worldPool.get(update_since);
                  worldInfoFull.getDeadCritters(world.w);
                  System.out.println("oldworld rows: " + oldWorld.getNumRows());
                  to_col = to_col == 0 ? worldInfoFull.cols : to_col + 1;
                  to_row = to_row == 0 ? worldInfoFull.rows : to_row + 1;
                  for (int c = from_col; c < to_col; c ++) {
                     for (int r = from_row; r < to_row; r ++) {
                        if (world.w.isValid(new Location(c, r))) {
                           if (world.w.H[c][r].equals(oldWorld.H[c][r]) == -1) {
                              worldInfoFull.state.add(new HexInfoNullOrRock(r, c, "nothing"));
                           }
                           else if (world.w.H[c][r].equals(oldWorld.H[c][r]) == 1) {
                              if (world.w.H[c][r].hasRock()) {
                                 worldInfoFull.state.add(new HexInfoNullOrRock(r, c, "rock"));
                              }
                              else if (world.w.H[c][r].hasFood()) {
                                 worldInfoFull.state.add(new HexInfoFood(r, c, "food", world.w.H[c][r].getValue()));
                              }
                              else {
                                 Critter cri = world.w.H[c][r].getCritter();
                                 if (session.get(session_id) != null && session.get(session_id).equals("admin")) {
                                    worldInfoFull.state.add(new HexInfoCritterFull("critter", cri.getID(), cri.name,
                                             cri.getProgram().prettyPrint(new StringBuilder()).toString(),
                                             r, c, cri.getDir(), cri.getMem(), cri.getProgram().getChildren().indexOf(cri.getRecentRule())));
                                 }
                                 else if (session.get(session_id) != null && session.get(session_id).equals("write")) {
                                    if (session_id == cri.getCreator()) {
                                       worldInfoFull.state.add(new HexInfoCritterFull("critter", cri.getID(), cri.name,
                                                cri.getProgram().prettyPrint(new StringBuilder()).toString(),
                                                r, c, cri.getDir(), cri.getMem(), cri.getProgram().getChildren().indexOf(cri.getRecentRule())));
                                    }
                                    else {
                                       worldInfoFull.state.add(new HexInfoCritterPart("critter", cri.getID(), cri.name, r, c, cri.getDir(), cri.getMem()));
                                    }
                                 }
                                 else if (session.get(session_id) != null) {
                                    worldInfoFull.state.add(new HexInfoCritterPart("critter", cri.getID(), cri.name, r, c, cri.getDir(), cri.getMem()));
                                 }
                              }
                           }
                        }
                     }
                  }
               }
               return worldInfoFull;
            }
         } catch (Exception e) {
            e.printStackTrace();
         } finally {
            rwlock.readLock().unlock();
         }
         return null;
      }, gson::toJson);

      post("/CritterWorld/world/create_entity", (request, response) -> {
         rwlock.writeLock().lock();
         try {
            response.header("Content-Type", "application/json");
            int session_id = new BigInteger(request.queryParams("session_id")).intValue();
            if (session.get(session_id) != null && (session.get(session_id).equals("admin") || session.get(session_id).equals("write"))) {
               String json = request.body();
               ObjectBundle newObject = gson.fromJson(json, ObjectBundle.class);
               if (world.w.isValid(new Location(newObject.col, newObject.row))) {
                  response.status(201);
                  if (newObject.type.equals("rock")) {
                     world.w.addRock(newObject.col, newObject.row);
                  }
                  else if (newObject.type.equals("food")) {
                     world.w.addFood(newObject.col, newObject.row, newObject.amount);
                  }
                  return "Ok";
               }
               else {
                  response.status(406);
                  return "Not Acceptable";
               }
            }
            else {
               response.status(401);
               return "Unauthorized";
            }
         } catch (Exception e) {
            e.printStackTrace();
         } finally {
            rwlock.writeLock().unlock();
         }
         return null;
      }, gson::toJson);

      post("/CritterWorld/step", (request, response) -> {
         rwlock.writeLock().lock();
         try {
            response.header("Content-Type", "application/json");
            int session_id = new BigInteger(request.queryParams("session_id")).intValue();
            if (session.get(session_id) != null && !session.get(session_id).equals("read")) {
               String json = request.body();
               WorldStepBundle newStep = gson.fromJson(json, WorldStepBundle.class);
               if (!world.w.isRuning) {
                  response.status(200);
                  // world simulate in server
                  for (int i = 0; i < newStep.count; i ++) {
                     world.w.execute();
                  }
                  return "Ok";
               }
               else {
                  response.status(406);
                  return "Not Acceptable";
               }
            }
            else {
               response.status(401);
               return "Unauthorized";
            }
         } catch (Exception e) {
            e.printStackTrace();
         } finally {
            rwlock.writeLock().unlock();
         }
         return null;
      }, gson::toJson);

      post("/CritterWorld/run", (request, response) -> {
         response.header("Content-Type", "application/json");
         int session_id = new BigInteger(request.queryParams("session_id")).intValue();
         if (session.get(session_id) != null && !session.get(session_id).equals("read")) {
            String json = request.body();
            RunBundle newRun = gson.fromJson(json, RunBundle.class);
            if (newRun.rate >= 0) {
               response.status(200);
               float rate = newRun.rate;
               if (rate == 0 && world.w.isRuning) {
                  rwlock.writeLock().lock();
                  try {
                     world.w.isRuning = false;
                     world.w.rate = 0;
                     timer.shutdown();
                  } catch (Exception e) {
                     e.printStackTrace();
                  } finally {
                     rwlock.writeLock().unlock();
                  }
               }
               else if (rate != 0) {
                  // world simulate in server
                  world.w.isRuning = true;
                  world.w.rate = rate;
                  timer = new ScheduledThreadPoolExecutor(1);
                  timer.scheduleAtFixedRate(new Runnable() {
                     public void run() {
                        rwlock.writeLock().lock();
                        try {
                           world.w.execute();
                        } catch (Exception e) {
                           e.printStackTrace();
                        } finally {
                           rwlock.writeLock().unlock();
                        }
                     }
                  }, 0, (int)(1000 / rate), TimeUnit.MILLISECONDS);
               }
               return "Ok";
            }
            else {
               response.status(406);
               return "Not Acceptable";
            }
         }
         else {
            response.status(401);
            return "Unauthorized";
         }
      }, gson::toJson);

   }
}
