package view;

import ast.Program;
import clientserver.ClientRequestHandler;
import clientserver.HexInfo;
import clientserver.PositionBundle;
import clientserver.WorldInfoFull;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.*;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Critter;
import model.Location;
import model.World;
import parse.Parser;
import parse.ParserFactory;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class ClientGUI {
   @FXML public Canvas canvas;
   @FXML private Button loadWorld;
   @FXML private Slider slider;
   @FXML private Slider slider1;
   @FXML private Label rate;
   @FXML private ScrollPane scrollpane;
   @FXML private Button help;
   @FXML private Label species;
   @FXML private Label direction;
   @FXML private Label mem0;
   @FXML private Label mem1;
   @FXML private Label mem2;
   @FXML private Label mem3;
   @FXML private Label mem4;
   @FXML private Label mem5;
   @FXML private Label mem6;
   @FXML private Label mem7;
   @FXML private Label numCritters;
   @FXML private Label numSteps;
   @FXML private TextArea critterRules;
   @FXML private TextField num = new TextField();
   @FXML private TextArea lastRule;
   @FXML private TextField colN;
   @FXML private TextField rowN;

   private static final double MAX_SCALE = 500.0d;
   private static final double MIN_SCALE = .1d;
   private int side = 10;
   private final Desktop desktop = Desktop.getDesktop();
   private int session_id = Main.session_id;
   private String level = Main.userlevel;
   private int numRows = 0;
   private int numCols = 0;
   private HashMap<String, HexInfo> realtimeWorldInfo;
   private int version = 0;


   private GraphicsContext gc;

   //those are the numbers
   private static double BORDERS = 20;

   private static double s = 0;
   private static double t = 0;
   private static double r = 0;
   private static double h = 0;
   private World world = new World();
   private int stepsPerSec = 0;
   private ScheduledExecutorService timer;
   private boolean timerHasStarted = false;
   private int hexR = -1;
   private int hexC = -1;
   private HashMap<Integer, Integer> creatorMap = new HashMap<>();
   private Hashtable<String, Color> nameToColor = new Hashtable<>();

   //client-server new params
   private boolean localMode;
   private ClientRequestHandler handler = new ClientRequestHandler(Main.guiUrl);
   //      private ClientRequestHandler handler = new ClientRequestHandler(" http://critterworld-fa18.developersam.com:8080/hexworld/");

   @FXML @SuppressWarnings("Duplicates")
   public void initialize() {
      numCritters.setText(Integer.toString(0));
      numSteps.setText(Integer.toString(0));
      help.setStyle(
               "-fx-background-radius: 25em; " +
                        "-fx-min-width: 25px; " +
                        "-fx-min-height: 25px; " +
                        "-fx-max-width: 25px; " +
                        "-fx-max-height: 25px;"
      );
      critterRules.setEditable(false);
      critterRules.setWrapText(true);
      lastRule.setEditable(false);
      lastRule.setWrapText(true);
      final StackPane canvasContainer = new StackPane();
      canvasContainer.setStyle("-fx-background-color: #808080;");
      canvasContainer.getChildren().add(canvas);
      canvasContainer.setAlignment(Pos.CENTER);
      double canvasScaleX = canvas.getScaleX();
      double canvasScaleY = canvas.getScaleY();
      scrollpane.setContent(canvasContainer);
      gc = canvas.getGraphicsContext2D();
      gc.setFill(Color.GREY);
      gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

      slider.valueProperty().addListener(new ChangeListener<Number>() {
         @Override
         public void changed(ObservableValue<? extends Number> ov,
                  Number oldvalue, Number newvalue) {
            rate.setText(String.format("%d", newvalue.intValue()));
            stepsPerSec = newvalue.intValue();
            if(timerHasStarted) {
               timer.shutdown();
               handler.changeRate(0, session_id);
               if (stepsPerSec > 0) {
                  timerHasStarted = true;
                  timer = new ScheduledThreadPoolExecutor(2);
                  runTimer(timer);
               }
            }
         }
      });

      slider1.valueProperty().addListener(new ChangeListener<Number>() {
         @Override
         public void changed(ObservableValue<? extends Number> ov,
                  Number oldvalue, Number newvalue) {
            final double zoomFactor = newvalue.doubleValue() >= 0 ? (newvalue.doubleValue() + 1)
                     : 1 / Math.abs(newvalue.doubleValue() - 1);
            System.out.println(newvalue);
            canvas.setScaleX(zoomFactor * canvasScaleX);
            canvas.setScaleY(zoomFactor * canvasScaleY);
            scrollpane.layout();
            Platform.runLater(new Runnable() {
               @Override public void run() {
                  canvasContainer.setPrefSize(Math.max(canvasContainer.getBoundsInParent().getMaxX(),
                           scrollpane.getViewportBounds().getWidth()),
                           Math.max(canvasContainer.getBoundsInParent().getMaxY(),
                                    scrollpane.getViewportBounds().getHeight()));
                  scrollpane.setHvalue(scrollpane.getHmax() / 2);
                  scrollpane.setVvalue(scrollpane.getVmax() / 2);
               }
            });
         }
      });

      canvas.setOnMouseClicked(
               new EventHandler<MouseEvent>() {
                  @Override
                  public void handle(MouseEvent event) {
                     //mouse position
                     WorldInfoFull worldInfo = handler.getWorldState(version, session_id);
                     for (HexInfo hexInfo : worldInfo.state) {
                        Location loc = new Location(hexInfo.col, hexInfo.row);
                        System.out.print(loc.toString() + " ");
                        realtimeWorldInfo.put(loc.toString(), hexInfo);
                     }
                     int r = 0;
                     int y = (int) event.getY();
                     int c = (int) (event.getX() - 15)/15;
                     if(c%2 == 0){
                        r = (int) ((y - 20)/(10 * Math.sqrt(3)));
                     }else if(c%2 == 1){
                        r = (int) ((y - 20 - (5 * Math.sqrt(3)))/(10 *  Math.sqrt(3)));
                     }
                     //                         System.out.println(event.getX());
                     //                         System.out.println(event.getY());
                     //                         System.out.println(c + " " + r);
                     hexC = c;
                     hexR = numRows-numCols/2-r+c/2;
                     if (new World(numCols, numRows).isValid(new Location(hexC, hexR))) {
                        colN.setText(Integer.toString(hexC));
                        rowN.setText(Integer.toString(hexR));
                        if (realtimeWorldInfo.get(new Location(hexC, hexR).toString()) != null && realtimeWorldInfo.get(new Location(hexC, hexR).toString()).type.equals("critter")) {
                           getCritterInfo();
                           if (level.equals("read") || (level.equals("write") && session_id != creatorMap.get(realtimeWorldInfo.get(new Location(hexC, hexR).toString()).id))) {
                              critterRules.setText(null);
                              lastRule.setText(null);
                           }
                        }
                        else {
                           clearCritterInfo();
                        }
                     }
                  }
               }
      );


      // place the container in the scrollpane and adjust the pane's viewports as required.
      scrollpane.viewportBoundsProperty().addListener(new ChangeListener<Bounds>() {
         @Override public void changed(ObservableValue<? extends Bounds> observableValue, Bounds oldBounds, Bounds newBounds) {
            canvasContainer.setPrefSize(
                     Math.max(canvas.getBoundsInParent().getMaxX(), newBounds.getWidth()),
                     Math.max(canvas.getBoundsInParent().getMaxY(), newBounds.getHeight())
            );
         }
      });

      //need to specify the url
   }

   /**
    * starts the timer that continuously updates the world
    * @param timer the timer
    *              This timer changes whenever the steps per second slider changes value.
    */
   @SuppressWarnings("Duplicates")
   private void runTimer(ScheduledExecutorService timer) {
      System.out.println("rate: " + stepsPerSec);
      WorldInfoFull current = handler.getWorldState(session_id);
      version = current.current_version_number;
      handler.changeRate(stepsPerSec, session_id);
      System.out.println("step: " + stepsPerSec);
      if (stepsPerSec <= 30) {
         timer.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
               Platform.runLater(() -> {
                  WorldInfoFull worldInfo = handler.getWorldState(version, session_id);
                  System.out.println("server version: " + worldInfo.current_version_number);
                  System.out.println("old version: " + worldInfo.update_since);
                  System.out.println("GUI version: " + version);
                  for (HexInfo hexInfo : worldInfo.state) {
                     Location loc = new Location(hexInfo.col, hexInfo.row);
                     //                            System.out.print(loc.toString() + " ");
                     realtimeWorldInfo.put(loc.toString(), hexInfo);
                  }
                  drawWorldRealtime();
                  numCritters.setText(Integer.toString(worldInfo.population));
                  numSteps.setText(Integer.toString(worldInfo.current_timestep));
                  version = worldInfo.current_version_number;
                  if (hexC != -1 && hexR != -1) {
                     getCritterInfo();
                  }
               });
            }
         }, 0, 1000 / stepsPerSec, TimeUnit.MILLISECONDS);
      }
      else {
         timer.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
               Platform.runLater(() -> {
                  WorldInfoFull worldInfo = handler.getWorldState(version, session_id);
                  for (HexInfo hexInfo : worldInfo.state) {
                     Location loc = new Location(hexInfo.col, hexInfo.row);
                     System.out.print(loc.toString() + " ");
                     realtimeWorldInfo.put(loc.toString(), hexInfo);
                  }
                  numCritters.setText(Integer.toString(worldInfo.population));
                  numSteps.setText(Integer.toString(worldInfo.current_timestep));
                  version = worldInfo.current_version_number;
                  drawWorldRealtime();
                  if (hexC != -1 && hexR != -1) {
                     getCritterInfo();
                  }
               });
            }
         }, 0, 1000 / 30, TimeUnit.MILLISECONDS);
         //            timer.scheduleAtFixedRate(new Runnable() {
         //                @Override
         //                public void run() {
         //                    Platform.runLater(() -> {
         //                        numCritters.setText(Integer.toString(world.w.critterList.size()));
         //                        numSteps.setText(Integer.toString(world.w.timeSteps));
         //                        if (hexC != -1 && hexR != -1) {
         //                            getCritterInfo();
         //                        }
         //                        drawWorld();
         //                    });
         //                }
         //            },0, 1000 / 30, TimeUnit.MILLISECONDS);
      }
   }

   /**
    * Clears the critter information from the screen
    */
   @SuppressWarnings("Duplicates")
   private void clearCritterInfo() {
      species.setText(null);
      direction.setText(null);
      mem0.setText(null);
      mem1.setText(null);
      mem2.setText(null);
      mem3.setText(null);
      mem4.setText(null);
      mem5.setText(null);
      mem6.setText(null);
      mem7.setText(null);
      critterRules.setText(null);
      lastRule.setText(null);
   }


   /**
    * Gets the information of the critter and displays it on the screen
    */
   @SuppressWarnings("Duplicates")
   private void getCritterInfo() {
      if (realtimeWorldInfo.get(new Location(hexC, hexR).toString()).type.equals("critter")) {
         HexInfo target = realtimeWorldInfo.get(new Location(hexC, hexR).toString());
         species.setText(target.species_id);
         direction.setText(Integer.toString(target.direction));
         mem0.setText(Integer.toString(target.mem.get(0)));
         mem1.setText(Integer.toString(target.mem.get(1)));
         mem2.setText(Integer.toString(target.mem.get(2)));
         mem3.setText(Integer.toString(target.mem.get(3)));
         mem4.setText(Integer.toString(target.mem.get(4)));
         mem5.setText(Integer.toString(target.mem.get(5)));
         mem6.setText(Integer.toString(target.mem.get(6)));
         mem7.setText(Integer.toString(target.mem.get(7)));
         critterRules.setText(target.program);
         if (target.recently_executed_rule != -1) {
            BufferedReader br = new BufferedReader(new StringReader(target.program));
            Reader reader = new BufferedReader(br);
            Parser parser = ParserFactory.getParser();
            Program p = parser.parse(reader);
            lastRule.setText(p.getChildren().get(target.recently_executed_rule).prettyPrint(new StringBuilder()).toString());
         }
      }
   }


   /**
    * Draws the entire world using update_since, including the hex coordinate grid, the rocks, the food, and the critters.
    */
   private void drawWorldRealtime() {
      gc.clearRect(0,0,canvas.getWidth(), canvas.getHeight());
      for(int i = 0; i < numCols; i++){
         if(i % 2 == 0){
            for(int t = 1; t < numRows-numCols/2 + 1; t++){
               drawHex(i,t);
            }
         }
         if(i % 2 == 1){
            for(int t = 0; t < numRows-numCols/2; t++){
               drawHex(i,t);
            }
         }
      }
      for(String key : realtimeWorldInfo.keySet()) {
         HexInfo change = realtimeWorldInfo.get(key);
         System.out.println(new Location(change.col, change.row).toString());
         if (change.type.equals("critter") && change.program != null) {
            int i = (change).col;
            int j = (change).row;
            String name = (change).species_id;
            BufferedReader br = new BufferedReader(new StringReader((change).program));
            Reader reader = new BufferedReader(br);
            Parser parser = ParserFactory.getParser();
            Program p = parser.parse(reader);
            ArrayList<Integer> mem = (change).mem;
            int id = (change).id;
            int dir = (change).direction;
            int lastRule = (change).recently_executed_rule;
            Critter cri = new Critter(name, p, mem, id, dir, lastRule);
            if (i % 2 == 0) {
               drawCritter(i, numRows - j + i / 2 - numCols / 2, cri);
            } else {
               drawCritter(i, numRows - j + (i + 1) / 2 - 1 - numCols / 2, cri);
            }
         } else if (change.type.equals("critter")) {
            int i = (change).col;
            int j = (change).row;
            String name = (change).species_id;
            ArrayList<Integer> mem = (change).mem;
            int id = (change).id;
            int dir = (change).direction;
            Critter cri = new Critter(name, mem, id, dir);
            if (i % 2 == 0) {
               drawCritter(i, numRows - j + i / 2 - numCols / 2, cri);
            } else {
               drawCritter(i, numRows - j + (i + 1) / 2 - 1 - numCols / 2, cri);
            }
         } else if (change.type.equals("food")) {
            int i = (change).col;
            int j = (change).row;
            if (i % 2 == 0) {
               drawFood(i, numRows - j + i / 2 - numCols / 2, -(change).value);
            } else {
               drawFood(i, numRows - j + (i + 1) / 2 - 1 - numCols / 2, (change).value);
            }
         } else if (change.type.equals("rock")) {
            int i = (change).col;
            int j = (change).row;
            if (i % 2 == 0) {
               drawRock(i, numRows - j + i / 2 - numCols / 2);
            } else {
               drawRock(i, numRows - j + (i + 1) / 2 - 1 - numCols / 2);
            }
         }
      }
   }

   /**
    * Draws the entire world, including the hex coordinate grid, the rocks, the food, and the critters.
    * @param worldInfo the world info from the server
    */
   private void drawWorld(WorldInfoFull worldInfo){
      gc.clearRect(0,0,canvas.getWidth(), canvas.getHeight());
      for(int i = 0; i < worldInfo.cols; i++){
         if(i % 2 == 0){
            for(int t = 1; t < worldInfo.rows-worldInfo.cols/2 + 1; t++){
               drawHex(i,t);
            }
         }
         if(i % 2 == 1){
            for(int t = 0; t < worldInfo.rows-worldInfo.cols/2; t++){
               drawHex(i,t);
            }
         }
      }
      System.out.println("size: " + worldInfo.state.size());
      for(int n = 0; n < worldInfo.state.size(); n++){
         HexInfo change = worldInfo.state.get(n);
         if(change.type.equals("critter") && change.program != null){
            int i = (change).col;
            int j = (change).row;
            String name = (change).species_id;
            System.out.println("change pro: "+ change.program);
            BufferedReader br = new BufferedReader(new StringReader((change).program));
            Reader reader = new BufferedReader(br);
            Parser parser = ParserFactory.getParser();
            Program p = parser.parse(reader);
            ArrayList<Integer> mem = (change).mem;
            int id = (change).id;
            int dir = (change).direction;
            int lastRule = (change).recently_executed_rule;
            Critter cri = new Critter(name, p, mem, id, dir, lastRule);
            if(i % 2 == 0){
               drawCritter(i, worldInfo.rows-j+i/2-worldInfo.cols/2, cri);
            }else{
               drawCritter(i, worldInfo.rows-j+(i+1)/2-1-worldInfo.cols/2, cri);
            }
         } else if (change.type.equals("critter")) {
            int i = (change).col;
            int j = (change).row;
            String name = (change).species_id;
            ArrayList<Integer> mem = (change).mem;
            int id = (change).id;
            int dir = (change).direction;
            Critter cri = new Critter(name, mem, id, dir);
            if(i % 2 == 0){
               drawCritter(i, worldInfo.rows-j+i/2-worldInfo.cols/2, cri);
            }else{
               drawCritter(i, worldInfo.rows-j+(i+1)/2-1-worldInfo.cols/2, cri);
            }
         } else if(change.type.equals("food")) {
            int i = (change).col;
            int j = (change).row;
            if(i % 2 == 0){
               drawFood(i, worldInfo.rows-j+i/2-worldInfo.cols/2, - (change).value);
            }else{
               drawFood(i, worldInfo.rows-j+(i+1)/2-1-worldInfo.cols/2, (change).value);
            }
         } else if(change.type.equals("rock")) {
            int i = (change).col;
            int j = (change).row;
            if(i % 2 == 0){
               drawRock(i, worldInfo.rows-j+i/2-worldInfo.cols/2);
            } else{
               drawRock(i, worldInfo.rows-j+(i+1)/2-1-worldInfo.cols/2);
            }
         }
      }
   }

   /**
    * draws a rock in the world, color black.
    * @param row row of the rock
    * @param col col of the rock
    */
   @FXML
   private void drawRock(int row, int col){
      gc.setFill(Color.BLACK);
      drawHex(row, col);
      gc.setFill(Color.WHITE);
   }

   /**
    * draws food in the world, color green,
    * @param row row of the food
    * @param col col of the food
    * @param foodValue How much food is contained in the hex
    */
   @FXML
   private void drawFood(int row, int col, int foodValue){
      gc.setFont(new Font("Times New Roman", 8));
      gc.setFill(Color.rgb(0,255,0));
      drawHex(row, col);
      gc.setFill(Color.WHITE);
   }

   /**
    * Draws a critter and displays its size and species through color
    * @param row the row of the critter
    * @param col the col of the critter
    * @param c The critter itself, used to determine its species and gather all other required information for the display.
    */
   @FXML @SuppressWarnings("Duplicates")
   private void drawCritter(int row, int col, Critter c){
      gc.setFill(colorOfCritter(c));

      double centerx = row*(s+t)+s/2 + BORDERS;
      double centery = col * h + (row % 2) * h / 2+r + BORDERS;
      int endx = (int)centerx+(int)(r*Math.cos(Math.toRadians((c.getDir() - 2) * 60 + 30)));
      int endy = (int)centery+(int)(r*Math.sin(Math.toRadians((c.getDir() - 2) * 60 + 30)));

      //drawHex(row, col);
      gc.strokeLine(centerx, centery, endx, endy);
      double critterSize = (c.getMem().get(3)>=5)?r/5*c.getMem().get(3):r/5*5;
      gc.fillOval(row*(s+t)+s/2-(critterSize)/2 + BORDERS,
               col * h + (row % 2) * h / 2+r-(critterSize)/2 + BORDERS, (critterSize), (critterSize));
      gc.setFill(Color.WHITE);
   }

   /**
    * returns the color of the critter that should be displayed.
    * Critters with the same species name will have the same color
    * @param c the critter
    * @return a Color for the critter.
    */
   @SuppressWarnings("Duplicates")
   private Color colorOfCritter(Critter c){
      if(nameToColor.containsKey(c.name)){
         return nameToColor.get(c.name);
      }else{
         Random random = new Random();
         Color color = Color.rgb(random.nextInt(256),random.nextInt(256),random.nextInt(256));
         nameToColor.put(c.name, color);
         return color;
      }
   }


   /**
    * handles the "Load World" button
    * @param ae Mouse action event
    */
   @FXML @SuppressWarnings("Duplicates")
   private void handleButtonPressed(ActionEvent ae) throws IOException {
      try {
         final FileChooser fileChooser = new FileChooser();
         fileChooser.setInitialDirectory(new File(System.getProperty("user.home"))
         );
         fileChooser.getExtensionFilters().addAll(
                  new FileChooser.ExtensionFilter("text file (.txt)", "*.txt")
         );
         File file = fileChooser.showOpenDialog(loadWorld.getScene().getWindow());
         if (file != null) {
            String path = file.getAbsolutePath();
            handler.loadWorld(path, session_id);
            WorldInfoFull worldInfo = handler.getWorldState(session_id);
            if (worldInfo.cols <= 400 && worldInfo.rows <= 400) {
               realtimeWorldInfo = new HashMap<>();
               for (HexInfo hexInfo : worldInfo.state) {
                  Location loc = new Location(hexInfo.col, hexInfo.row);
                  realtimeWorldInfo.put(loc.toString(), hexInfo);
                  if (hexInfo.type.equals("critter")) {
                     creatorMap.put(hexInfo.id, session_id);
                  }
               }
               numCols = worldInfo.cols;
               numRows = worldInfo.rows;
               numSteps.setText(Integer.toString(0));
               gc.setFill(Color.GREY);
               gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
               setSide(side);
               gc.setFill(Color.WHITE);
               canvas.setHeight(worldInfo.rows * 35);
               canvas.setWidth(worldInfo.cols * 20);
               numCritters.setText(Integer.toString(worldInfo.population));
               if (timerHasStarted) {
                  timer.shutdown();
                  handler.changeRate(0, session_id);
                  timerHasStarted = false;
               }
               drawWorld(worldInfo);
               version = worldInfo.current_version_number;
            }
            else {
               Label errorMessage = new Label("The world you want to generate is too big. Please try a smaller world.");
               StackPane root = new StackPane(errorMessage);
               Scene scene = new Scene(root, 600, 80);
               Stage errorWindow = new Stage();
               errorWindow.setTitle("World Too Big!");
               errorWindow.setScene(scene);
               errorWindow.sizeToScene();
               errorWindow.setResizable(false);
               errorWindow.show();
            }
         }
      }
      catch (Exception e) {
         Label errorMessage = new Label("The world file is invalid. Please check the format and select a valid world file.");
         StackPane root = new StackPane(errorMessage);
         Scene scene = new Scene(root, 600, 80);
         Stage errorWindow = new Stage();
         errorWindow.setTitle("Invalid World File!");
         errorWindow.setScene(scene);
         errorWindow.sizeToScene();
         errorWindow.setResizable(false);
         errorWindow.show();
      }
   }

   /**
    * Handles the "New World" button.
    * @param ae Mouse action event
    */
   @FXML @SuppressWarnings("Duplicates")
   private void handleButtonPressed2(ActionEvent ae) {
      handler.loadWorld(session_id);
      WorldInfoFull worldInfo = handler.getWorldState(session_id);
      if (worldInfo.cols <= 400 && worldInfo.rows <= 400) {
         realtimeWorldInfo = new HashMap<>();
         for (HexInfo hexInfo : worldInfo.state) {
            Location loc = new Location(hexInfo.col, hexInfo.row);
            realtimeWorldInfo.put(loc.toString(), hexInfo);
            if (hexInfo.type.equals("critter")) {
               creatorMap.put(hexInfo.id, session_id);
            }
         }
         numCols = worldInfo.cols;
         numRows = worldInfo.rows;
         numSteps.setText(Integer.toString(0));
         gc.setFill(Color.GREY);
         gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
         setSide(side);
         gc.setFill(Color.WHITE);
         canvas.setHeight(worldInfo.rows * 35);
         canvas.setWidth(worldInfo.cols * 20);
         numCritters.setText(Integer.toString(worldInfo.population));
         if (timerHasStarted) {
            timer.shutdown();
            handler.changeRate(0, session_id);
            timerHasStarted = false;
         }
         drawWorld(worldInfo);
         version = worldInfo.current_version_number;
      }
   }

   /**
    * Handles the "Add critter" button
    * @param ae Mouse action event
    */
   @FXML @SuppressWarnings("Duplicates")
   private void handleButtonPressed3(ActionEvent ae) {
      try {
         final FileChooser fileChooser = new FileChooser();
         fileChooser.setInitialDirectory(new File(System.getProperty("user.home"))
         );
         fileChooser.getExtensionFilters().addAll(
                  new FileChooser.ExtensionFilter("text file (.txt)", "*.txt")
         );
         File file = fileChooser.showOpenDialog(loadWorld.getScene().getWindow());
         if (file != null) {
            String path = file.getAbsolutePath();
            int size = new World(numCols, numRows).numHex;
            int n;
            if(num.getText().trim().isEmpty()){
               n = 1;
            } else{
               n = Integer.parseInt(num.getText());
            }
            System.out.println("n " + n);
            if (n > 0 && n <= size) {
               Critter cri = World.addCritter(path);
               handler.addCritter(cri, n, session_id);
               System.out.println(cri.name);
               WorldInfoFull worldInfo = handler.getWorldState(session_id);
               numCritters.setText(Integer.toString(worldInfo.population));
               realtimeWorldInfo = new HashMap<>();
               for (HexInfo hexInfo : worldInfo.state) {
                  Location loc = new Location(hexInfo.col, hexInfo.row);
                  realtimeWorldInfo.put(loc.toString(), hexInfo);
                  if (hexInfo.type.equals("critter")) {
                     creatorMap.put(hexInfo.id, session_id);
                  }
               }
               version = worldInfo.current_version_number;
               drawWorld(worldInfo);
            }
            else if (n > size) {
               Label errorMessage = new Label("The number of critters exceeds the size of the world. Please input a smaller number.");
               StackPane root = new StackPane(errorMessage);
               Scene scene = new Scene(root, 600, 80);
               Stage errorWindow = new Stage();
               errorWindow.setTitle("Critters Number Too Big!");
               errorWindow.setScene(scene);
               errorWindow.sizeToScene();
               errorWindow.setResizable(false);
               errorWindow.show();
            }
            else {
               throw new NumberFormatException();
            }
         }
      }
      catch (NumberFormatException e) {
         Label errorMessage = new Label("The number of critters you chose is invalid. Please input a valid number.");
         StackPane root = new StackPane(errorMessage);
         Scene scene = new Scene(root, 600, 80);
         Stage errorWindow = new Stage();
         errorWindow.setTitle("Invalid Critters Number!");
         errorWindow.setScene(scene);
         errorWindow.sizeToScene();
         errorWindow.setResizable(false);
         errorWindow.show();
      }
      catch (Exception e) {
         Label errorMessage = new Label("The critter file is invalid or the world is not initialized. Please check the format, restart the world, and select a valid critter file.");
         StackPane root = new StackPane(errorMessage);
         Scene scene = new Scene(root, 600, 80);
         Stage errorWindow = new Stage();
         errorWindow.setTitle("Invalid Critter File!");
         errorWindow.setScene(scene);
         errorWindow.sizeToScene();
         errorWindow.setResizable(false);
         errorWindow.show();
      }
   }

   /**
    *
    * @param ae
    */
   @FXML @SuppressWarnings("Duplicates")
   private void handleButtonPressed4(ActionEvent ae){
      try {
         if(colN.getText() != null && rowN.getText() != null &&
                  new World(numCols, numRows).isValid(new Location(Integer.parseInt(colN.getText()), Integer.parseInt(rowN.getText())))) {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("text file (.txt)", "*.txt"));
            File file = fileChooser.showOpenDialog(loadWorld.getScene().getWindow());
            if (file != null) {
               String path = file.getAbsolutePath();
               int c;
               int r;
               c = Integer.parseInt(colN.getText());
               r = Integer.parseInt(rowN.getText());
               Critter cri = World.addCritter(path);
               //                ArrayList<PositionBundle> pos = new ArrayList<>();
               //                pos.add(new PositionBundle(r, c, cri.getDir()));

               PositionBundle[] pos = new PositionBundle[1];
               pos[0] = new PositionBundle(r, c, cri.getDir());

               handler.addCritter(cri, pos, session_id);
               System.out.println(cri.name);
               WorldInfoFull worldInfo = handler.getWorldState(session_id);
               realtimeWorldInfo = new HashMap<>();
               for (HexInfo hexInfo : worldInfo.state) {
                  Location loc = new Location(hexInfo.col, hexInfo.row);
                  realtimeWorldInfo.put(loc.toString(), hexInfo);
                  if (hexInfo.type.equals("critter")) {
                     creatorMap.put(hexInfo.id, session_id);
                  }
               }
               numCritters.setText(Integer.toString(worldInfo.population));
               version = worldInfo.current_version_number;
               drawWorld(worldInfo);
            }
         }
         else {
            Label errorMessage = new Label("The location you chose is invalid. Please select a valid location.");
            StackPane root = new StackPane(errorMessage);
            Scene scene = new Scene(root, 600, 80);
            Stage errorWindow = new Stage();
            errorWindow.setTitle("Invalid Location!");
            errorWindow.setScene(scene);
            errorWindow.sizeToScene();
            errorWindow.setResizable(false);
            errorWindow.show();
         }
      } catch (NumberFormatException e) {
         Label errorMessage = new Label("The location you chose is invalid. Please select a valid location.");
         StackPane root = new StackPane(errorMessage);
         Scene scene = new Scene(root, 600, 80);
         Stage errorWindow = new Stage();
         errorWindow.setTitle("Invalid Location!");
         errorWindow.setScene(scene);
         errorWindow.sizeToScene();
         errorWindow.setResizable(false);
         errorWindow.show();
      } catch (Exception e1) {
         Label errorMessage = new Label("The critter file is invalid the world is not initialized. Please check the format and select a valid critter file.");
         StackPane root = new StackPane(errorMessage);
         Scene scene = new Scene(root, 600, 80);
         Stage errorWindow = new Stage();
         errorWindow.setTitle("Invalid Critter File!");
         errorWindow.setScene(scene);
         errorWindow.sizeToScene();
         errorWindow.setResizable(false);
         errorWindow.show();
      }
   }

   /**
    * Handles the "Help message" button
    * @param ae Mouse action event
    */
   @FXML @SuppressWarnings("Duplicates")
   private void handleHelpMessage(ActionEvent ae) {
      try {
         URL r = getClass().getResource("helpmessage.fxml");
         if (r == null) throw new Exception("No FXML resource found.");
         FXMLLoader loader = new FXMLLoader(r);
         Pane root = loader.load();
         Scene scene = new Scene(new Group(root));
         Stage stage = new Stage();
         stage.setTitle("Critter World Tips");
         stage.setScene(scene);
         stage.sizeToScene();
         stage.setResizable(false);
         stage.show();
      } catch (Exception e) {
         System.out.println(e.getMessage());
         e.printStackTrace();
      }
   }

   /**
    * Handles the single step button
    * @param ae Mouse action event
    */
   @FXML @SuppressWarnings("Duplicates")
   private void handleSingleExecution(ActionEvent ae) {
      if (!timerHasStarted) {
         handler.worldStep(session_id, 1);
         WorldInfoFull worldInfo = handler.getWorldState(version, session_id);
         System.out.println("server version: " + worldInfo.current_version_number);
         System.out.println("old version: " + worldInfo.update_since);
         System.out.println("GUI version: " + version);
         for (HexInfo hexInfo : worldInfo.state) {
            Location loc = new Location(hexInfo.col, hexInfo.row);
            System.out.print(loc.toString() + " ");
            realtimeWorldInfo.put(loc.toString(), hexInfo);
         }
         System.out.println();
         numCritters.setText(Integer.toString(worldInfo.population));
         numSteps.setText(Integer.toString(worldInfo.current_timestep));
         version = worldInfo.current_version_number;
         drawWorldRealtime();
         if (hexC != -1 && hexR != -1) {
            getCritterInfo();
         }
      }
   }

   /**
    * Handles the continuous play button
    * @param ae Mouse action event
    */
   @FXML @SuppressWarnings("Duplicates")
   private void handlePlay(ActionEvent ae){
      if(stepsPerSec>0 && !timerHasStarted) {
         timerHasStarted = true;
         timer = new ScheduledThreadPoolExecutor(2);
         runTimer(timer);
      }
   }

   /**
    * Handles the pause/stop button
    * @param ae Mouse action event
    */
   @FXML
   private void handleStop(ActionEvent ae){
      handler.changeRate(0, session_id);
      timer.shutdown();
      timerHasStarted = false;
   }

   @FXML
   private void handleCompatible(ActionEvent ae) {
      if (world.w != null) {
         if (!world.w.compatiable) {
            world.w.compatiable = true;
         }
         else world.w.compatiable = false;
      }
   }


   /**
    * Draws a hexagon with given coordinate (i, j)
    * @param i the row value
    * @param j the column value
    */
   @SuppressWarnings("Duplicates")
   public void drawHex(int i, int j){
      GraphicsContext gc = canvas.getGraphicsContext2D();
      double x = i * (s + t) + BORDERS;
      double y = j * h + (i % 2) * h / 2 + BORDERS;
      double[] cx, cy;
      cx = new double[] {x, x+s, x+s+t, x+s, x, x-t};
      cy = new double[] {y, y, y+r, y+r+r, y+r+r, y+r};

      gc.fillPolygon(cx, cy, 6);
      gc.strokePolygon(cx, cy, 6);
   }


   public static void setSide(double side){
      s = side;
      t = s / 2;
      r = s * 0.8660254037844;
      h = 2 * r;
   }

   public static void setHeight(double height){
      h = height;
      r = h/2;
      s = h / 1.73205;
      t = r / 1.73205;
   }

   public static double clamp( double value, double min, double max) {

      if( Double.compare(value, min) < 0)
         return min;

      if( Double.compare(value, max) > 0)
         return max;

      return value;
   }
}
