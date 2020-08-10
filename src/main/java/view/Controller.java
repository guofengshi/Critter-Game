package view;

import clientserver.ClientRequestHandler;
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
import model.*;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.Hashtable;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Controller {
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
   @FXML private TextField password;
   @FXML private TextField level;

   private static final double MAX_SCALE = 500.0d;
   private static final double MIN_SCALE = .1d;
   private int side = 10;
   private final Desktop desktop = Desktop.getDesktop();


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
   private Hashtable<String, Color> nameToColor = new Hashtable<>();

    //client-server new params
    private boolean localMode;
    private int session_id;
    private ClientRequestHandler handler;
    private String url = "http://localhost:8080/CritterWorld";

   @FXML
   public void initialize() {
      handler = new ClientRequestHandler(url);
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
               if (stepsPerSec > 0) {
                  timerHasStarted = true;
                  world.w.isRuning = true;
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
                     Critter cri;
                     int r = 0;
                     int y = (int) event.getY();
                     int c = (int) (event.getX() - 15)/15;
                     if(c%2 == 0){
                        r = (int) ((y - 20)/(10 * Math.sqrt(3)));
                     }else if(c%2 == 1){
                        r = (int) ((y - 20 - (5 * Math.sqrt(3)))/(10 *  Math.sqrt(3)));
                     }
                     if (world.w != null) {
                        if(world.w.isValid(new Location(c, world.w.getNumRows()-world.w.getNumCols()/2-r+c/2))){
                           //                        if(world.w.H[c][world.w.getNumRows()-world.w.getNumCols()/2-r+c/2].getCritter() != null){
                           //                           cri = world.w.H[c][world.w.getNumRows()-world.w.getNumCols()/2-r+c/2].getCritter();
                           //                           System.out.println();
                           //                        }
                           //                        if(world.w.H[c][world.w.getNumRows()-world.w.getNumCols()/2-r+c/2].hasRock()){
                           //                           System.out.println("R");
                           //                        }
                           System.out.print(world.w.H[c][world.w.getNumRows()-world.w.getNumCols()/2-r+c/2].getValue());
                        }
                        System.out.println(event.getX());
                        System.out.println(event.getY());
                        System.out.println(c + " " + r);
                        System.out.println(world.w.getNumRows()-world.w.getNumCols()/2-r+c/2);
                        hexC = c;
                        hexR = world.w.getNumRows()-world.w.getNumCols()/2-r+c/2;
                        if (world.w.isValid(new Location(hexC, hexR))) {
                           colN.setText(Integer.toString(hexC));
                           rowN.setText(Integer.toString(hexR));
                           if (world.w.H[hexC][hexR].hasCritter()) {
                              getCritterInfo();
                           }
                           else {
                              clearCritterInfo();
                           }
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
      handler = new ClientRequestHandler("http://localhost:8080/CritterWorld");
   }

   /**
    * starts the timer that continuously updates the world
    * @param timer the timer
    *              This timer changes whenever the steps per second slider changes value.
    */
   private void runTimer(ScheduledExecutorService timer) {
      if (world.w != null) {
         System.out.println("rate: " + stepsPerSec);
         if (stepsPerSec <= 30) {
            timer.scheduleAtFixedRate(new Runnable() {
               public void run() {
                  Platform.runLater(() -> {
                     world.w.execute();
                     drawWorld();
                     numCritters.setText(Integer.toString(world.w.critterList.size()));
                     numSteps.setText(Integer.toString(world.w.timeSteps));
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
                     world.w.execute();
                  });
               }
            }, 0, 1000 / stepsPerSec, TimeUnit.MILLISECONDS);
            timer.scheduleAtFixedRate(new Runnable() {
               @Override
               public void run() {
                  Platform.runLater(() -> {
                     numCritters.setText(Integer.toString(world.w.critterList.size()));
                     numSteps.setText(Integer.toString(world.w.timeSteps));
                     if (hexC != -1 && hexR != -1) {
                        getCritterInfo();
                     }
                     drawWorld();
                  });
               }
            },0, 1000 / 30, TimeUnit.MILLISECONDS);
         }
      }
   }

   /**
    * Clears the critter information from the screen
    */
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
   private void getCritterInfo() {
      if (world.w.H[hexC][hexR].hasCritter()) {
         Critter target = world.w.H[hexC][hexR].getCritter();
         species.setText(target.name);
         direction.setText(Integer.toString(target.getDir()));
         mem0.setText(Integer.toString(target.mem(0)));
         mem1.setText(Integer.toString(target.mem(1)));
         mem2.setText(Integer.toString(target.mem(2)));
         mem3.setText(Integer.toString(target.mem(3)));
         mem4.setText(Integer.toString(target.mem(4)));
         mem5.setText(Integer.toString(target.mem(5)));
         mem6.setText(Integer.toString(target.mem(6)));
         mem7.setText(Integer.toString(target.mem(7)));
         critterRules.setText(target.getProgram().prettyPrint(new StringBuilder()).toString());
         lastRule.setText(target.getLastRule());
      }
   }

   /**
    * Draws the entire world, including the hex coordinate grid, the rocks, the food, and the critters.
    */
   private void drawWorld(){
      gc.clearRect(0,0,canvas.getWidth(), canvas.getHeight());
      for(int i = 0; i < world.w.getNumCols(); i++){
         if(i % 2 == 0){
            for(int t = 1; t < world.w.getNumRows()-world.w.getNumCols()/2 + 1; t++){
               drawHex(i,t);
            }
         }
         if(i % 2 == 1){
            for(int t = 0; t < world.w.getNumRows()-world.w.getNumCols()/2; t++){
               drawHex(i,t);
            }
         }
      }
      for(int i = 0; i < world.w.getNumCols(); i++){
         for(int j = 0; j < world.w.getNumRows(); j++){
            Hex cur = world.w.H[i][j];
            if(cur.hasCritter()){
               if(i % 2 == 0){
                  drawCritter(i, world.w.getNumRows()-j+i/2-world.w.getNumCols()/2, cur.getCritter());
               }else{
                  drawCritter(i, world.w.getNumRows()-j+(i+1)/2-1-world.w.getNumCols()/2, cur.getCritter());
               }
            }else if(cur.hasFood()){
               if(i % 2 == 0){
                  drawFood(i, world.w.getNumRows()-j+i/2-world.w.getNumCols()/2, -cur.getValue());
               }else{
                  drawFood(i, world.w.getNumRows()-j+(i+1)/2-1-world.w.getNumCols()/2, -cur.getValue());
               }
            }else if(cur.hasRock()){
               if(i % 2 == 0){
                  drawRock(i, world.w.getNumRows()-j+i/2-world.w.getNumCols()/2);
               }else{
                  drawRock(i, world.w.getNumRows()-j+(i+1)/2-1-world.w.getNumCols()/2);
               }
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
   @FXML
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
   @FXML
   private void handleButtonPressed(ActionEvent ae) {
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
            world.w = null;
            world.init(path);
            if (world.w.getNumCols() <= 400 && world.getNumRows() <= 400) {
               numSteps.setText(Integer.toString(0));
               gc.setFill(Color.GREY);
               gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
               setSide(side);
               gc.setFill(Color.WHITE);
               canvas.setHeight(world.w.getNumRows() * 35);
               canvas.setWidth(world.w.getNumCols() * 20);
               numCritters.setText(Integer.toString(world.w.critterList.size()));
               if (timerHasStarted) {
                  timer.shutdown();
                  timerHasStarted = false;
                  world.w.isRuning = false;
               }
               drawWorld();
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
   @FXML
   private void handleButtonPressed2(ActionEvent ae) {
      world.init();
      numSteps.setText(Integer.toString(0));
      gc.setFill(Color.GREY);
      gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
      setSide(side);
      gc.setFill(Color.WHITE);
      for(int i = 0; i < Constants.columns; i++){
         if(i % 2 == 0){
            for(int t = 1; t < Constants.rows-world.w.getNumCols()/2 + 1; t++){
               drawHex(i,t);
            }
         }
         if(i % 2 == 1){
            for(int t = 0; t < Constants.rows-world.w.getNumCols()/2; t++){
               drawHex(i,t);
            }
         }
      }
      numCritters.setText(Integer.toString(world.w.critterList.size()));
      canvas.setHeight(world.w.getNumRows() * 35);
      canvas.setWidth(world.w.getNumCols() * 20);
      if (timerHasStarted) {
         timer.shutdown();
         timerHasStarted = false;
         world.w.isRuning = false;
      }
      drawWorld();
   }

   /**
    * Handles the "Add critter" button
    * @param ae Mouse action event
    */
   @FXML
   private void handleButtonPressed3(ActionEvent ae) {
      if (world.w != null) {
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
               Random rand = new Random();
               int a;
               int b;
               int n;
               int i = 0;
               System.out.println(num.getText());
               if(num.getText().trim().isEmpty()){
                  n = 1;
               } else{
                  n = Integer.parseInt(num.getText());
               }
               if (n <= world.w.numHex && n > 0) {
                  while(i < n){
                     do {
                        a = rand.nextInt(world.w.getNumCols());
                        b = rand.nextInt(world.w.getNumRows());
                     } while (!world.w.isValid(new Location(a, b)) || !world.w.H[a][b].isEmpty());
                     world.w.H[a][b].setCritter(world.w.addCritter(path));
                     world.w.addCritterLoc(new Location(a, b));
                     i++;
                  }
                  
                  numCritters.setText(Integer.toString(world.w.critterList.size()));
                  drawWorld();
               }
               else if (n > world.w.numHex) {
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
            Label errorMessage = new Label("The critter file is invalid. Please check the format, restart the world, and select a valid critter file.");
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
      else {
         Label errorMessage = new Label("No world is found. Please generate a world first.");
         StackPane root = new StackPane(errorMessage);
         Scene scene = new Scene(root, 600, 80);
         Stage errorWindow = new Stage();
         errorWindow.setTitle("No World Found!");
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
   @FXML
   private void handleButtonPressed4(ActionEvent ae){
      if (world.w != null) {
         try {
            if(colN.getText() != null && rowN.getText() != null &&
                     world.w.isValid(new Location(Integer.parseInt(colN.getText()), Integer.parseInt(rowN.getText())))) {
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
                  world.w.H[c][r].setCritter(world.w.addCritter(path));
                  world.w.addCritterLoc(new Location(c, r));
                  numCritters.setText(Integer.toString(world.w.critterList.size()));
                  drawWorld();
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
            Label errorMessage = new Label("The critter file is invalid. Please check the format and select a valid critter file.");
            StackPane root = new StackPane(errorMessage);
            Scene scene = new Scene(root, 600, 80);
            Stage errorWindow = new Stage();
            errorWindow.setTitle("Invalid Critter File!");
            errorWindow.setScene(scene);
            errorWindow.sizeToScene();
            errorWindow.setResizable(false);
            errorWindow.show();
         }
      } else {
         Label errorMessage = new Label("No world is found. Please generate a world first.");
         StackPane root = new StackPane(errorMessage);
         Scene scene = new Scene(root, 600, 80);
         Stage errorWindow = new Stage();
         errorWindow.setTitle("No World Found!");
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
   @FXML
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
   @FXML
   private void handleSingleExecution(ActionEvent ae) {
      if (!timerHasStarted) {
         if (world.w != null) {
            world.w.execute();
            drawWorld();
            numCritters.setText(Integer.toString(world.w.critterList.size()));
            numSteps.setText(Integer.toString(world.w.timeSteps));
            if (hexC != -1 && hexR != -1) {
               getCritterInfo();
            }
         }
      }
   }

   /**
    * Handles the continuous play button
    * @param ae Mouse action event
    */
   @FXML
   private void handlePlay(ActionEvent ae){
      if (world.w != null) {
         if(stepsPerSec>0 && !timerHasStarted) {
            timerHasStarted = true;
            world.w.isRuning = true;
            timer = new ScheduledThreadPoolExecutor(2);
            runTimer(timer);
         }
      }
   }

   /**
    * Handles the pause/stop button
    * @param ae Mouse action event
    */
   @FXML
   private void handleStop(ActionEvent ae){
      if (world.w != null) {
         timer.shutdown();
         timerHasStarted = false;
         world.w.isRuning = false;
      }
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
