package io.github.jon_bassi.view;

import java.io.PrintWriter;
import java.io.StringWriter;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class ExceptionHandler
{
   /**
    * displays the stack trace for an exception and exits the program once the user presses OK.
    * Allows useful information to be sent to me if there is an issue.
    * Since this method takes the initiative to exit the program and other logic after an exception is
    * caught using this method will not be executed
    * @param e Exception to display
    */
   public static void displayException(Exception e)
   {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Exception");
      alert.setHeaderText("An Exception has occurred, please send the exception stacktrace to the developer");
      alert.setContentText("Press show more to view exception stacktrace");

      StringWriter string = new StringWriter();
      PrintWriter writer = new PrintWriter(string);
      e.printStackTrace(writer);
      String text = string.toString();

      Label label = new Label("The exception stacktrace was:");

      TextArea textArea = new TextArea(text);
      textArea.setEditable(false);
      textArea.setWrapText(true);

      textArea.setMaxWidth(Double.MAX_VALUE);
      textArea.setMaxHeight(Double.MAX_VALUE);
      GridPane.setVgrow(textArea, Priority.ALWAYS);
      GridPane.setHgrow(textArea, Priority.ALWAYS);

      GridPane grid = new GridPane();
      grid.setMaxWidth(Double.MAX_VALUE);
      grid.add(label, 0, 0);
      grid.add(textArea, 0, 1);

      alert.getDialogPane().setExpandableContent(grid);
      alert.showAndWait();
   }
}
