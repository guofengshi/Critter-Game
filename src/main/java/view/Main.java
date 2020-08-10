package view;

import clientserver.ClientRequestHandler;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import java.net.URL;

/**
 * Model/view/controller demo.
 *
 * The view is specified by the FXML file scene.fxml,
 * which was created by SceneBuilder. The main method loads the
 * view and displays it. This also initializes the controller.
 * The controller is specified in the FXML file (bottom left
 * in SceneBuilder).
 */
public class Main extends Application {
   @FXML

   protected static int session_id;
   protected static String userlevel;
   protected static String guiUrl;
//   private ClientRequestHandler handler = new ClientRequestHandler("http://critterworld-fa18.developersam.com:8080/hexworld/");
   private ClientRequestHandler handler;


   @Override
   public void start(Stage primaryStage) {
      primaryStage.setTitle("Critter World Login");
      guiUrl = "http://critterworld-fa18.developersam.com:8080/hexworld/";
      handler = new ClientRequestHandler(guiUrl);
      GridPane grid = new GridPane();
      grid.setAlignment(Pos.CENTER);
      grid.setHgap(10);
      grid.setVgap(10);
      grid.setPadding(new Insets(25, 25, 25, 25));
      Scene scene = new Scene(grid, 450, 375);
      primaryStage.setScene(scene);
      primaryStage.show();

      Text scenetitle = new Text("Critter World");
      //        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
      grid.add(scenetitle, 1, 0, 2, 1);

      Label userName = new Label("Level:");
      grid.add(userName, 0, 1);

      TextField userTextField = new TextField();
      grid.add(userTextField, 1, 1);

      Label pw = new Label("Password:");
      grid.add(pw, 0, 2);

      final PasswordField pwBox = new PasswordField();
      grid.add(pwBox, 1, 2);

      Button btn = new Button("Login");
      HBox hbBtn = new HBox(10);
      hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
      hbBtn.getChildren().add(btn);
      grid.add(hbBtn, 1, 4);

      btn.setOnAction(new EventHandler<ActionEvent>() {
         @Override
         public void handle(ActionEvent ae) {
            String level = userTextField.getText();
            userlevel = level;
            String password = pwBox.getText();
            int response = handler.login(level, password);
            if (response != -1) {
               try {
                  URL r = getClass().getResource("scene.fxml");
                  if (r == null) throw new Exception("No FXML resource found.");
                  FXMLLoader loader = new FXMLLoader(r);
                  session_id = response;
                  System.out.println(session_id);
                  Pane root = loader.load();
                  Scene scene = new Scene(new Group(root));
                  primaryStage.setTitle("Critter World GUI");
                  primaryStage.setScene(scene);
                  primaryStage.sizeToScene();
                  primaryStage.show();
                  letterbox(scene, root);
                  primaryStage.setFullScreen(false);
               } catch (Exception e) {
                  System.out.println(e.getMessage());
                  e.printStackTrace();
               }
            }
         }
      });
   }


   private void letterbox(final Scene scene, final Pane contentPane) {
      final double initWidth  = scene.getWidth();
      final double initHeight = scene.getHeight();
      final double ratio      = initWidth / initHeight;

      SceneSizeChangeListener sizeListener = new SceneSizeChangeListener(scene, ratio, initHeight, initWidth, contentPane);
      scene.widthProperty().addListener(sizeListener);
      scene.heightProperty().addListener(sizeListener);
   }

   private static class SceneSizeChangeListener implements ChangeListener<Number> {
      private final Scene scene;
      private final double ratio;
      private final double initHeight;
      private final double initWidth;
      private final Pane contentPane;

      public SceneSizeChangeListener(Scene scene, double ratio, double initHeight, double initWidth, Pane contentPane) {
         this.scene = scene;
         this.ratio = ratio;
         this.initHeight = initHeight;
         this.initWidth = initWidth;
         this.contentPane = contentPane;
      }

      @Override
      public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
         final double newWidth  = scene.getWidth();
         final double newHeight = scene.getHeight();

         double scaleFactor =
                  newWidth / newHeight > ratio
                           ? newHeight / initHeight
                           : newWidth / initWidth;

         if (scaleFactor >= 1) {
            Scale scale = new Scale(scaleFactor, scaleFactor);
            scale.setPivotX(0);
            scale.setPivotY(0);
            scene.getRoot().getTransforms().setAll(scale);

            contentPane.setPrefWidth (newWidth  / scaleFactor);
            contentPane.setPrefHeight(newHeight / scaleFactor);
         } else {
            contentPane.setPrefWidth (Math.max(initWidth,  newWidth));
            contentPane.setPrefHeight(Math.max(initHeight, newHeight));
         }
      }
   }

}