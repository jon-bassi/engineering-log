package io.github.jon_bassi.view;

import java.util.Optional;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;

/**
 * 
 * @author jon-bassi
 *
 */
public class ScanningHandler
{

   /**
    * allows for scanning or input of item
    * @return
    */
   public static String scan()
   {
      Alert alert = new Alert(AlertType.INFORMATION);
      alert.setTitle("Scan");
      alert.setHeaderText("Please scan your item now...");
      
      GridPane grid = new GridPane();
      grid.setHgap(10);
      grid.setVgap(10);
      grid.setPadding(new Insets(20, 150, 10, 10));

      TextField id = new TextField();
      id.setPromptText("id");
      
      grid.add(id, 0, 0);
      
      alert.getDialogPane().setContent(grid);
      Platform.runLater(() -> id.requestFocus());
      Optional<ButtonType> result = alert.showAndWait();
      
      if (result.isPresent() && result.get() == ButtonType.OK)
      {
         return id.getText().toUpperCase();
         
      }  
      return "";
      }
   }
