package io.github.jon_bassi;

import io.github.jon_bassi.db.objects.Equipment;
import io.github.jon_bassi.db.objects.Job;

import java.sql.Date;
import java.sql.SQLException;
import java.util.Optional;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;


/**
 * class to handle popups and keep code organized
 * @author jon-bassi
 *
 */
public class WindowHandler
{
   
   // recreate the login box in FX
   
   /**
    * main login panel
    * @return {user choice as String, user name as String}
    */
   protected static String[] displayLoginPane()
   {
      Alert alert = new Alert(AlertType.NONE);
      alert.setTitle("Login");
      alert.setHeaderText("Please login below or create a new user account...");
      
      GridPane grid = new GridPane();
      grid.setHgap(10);
      grid.setVgap(10);
      grid.setPadding(new Insets(20, 150, 10, 10));

      TextField username = new TextField();
      username.setPromptText("Username:");
      
      if (System.getProperty("os.name").contains("Windows"))
         username.setText(System.getProperty("user.name"));
      
      grid.add(new Label("Username:"), 0, 0);
      grid.add(username, 1, 0);
      
      alert.getDialogPane().setContent(grid);
      
      Platform.runLater(() -> username.requestFocus());
      
      ButtonType login = new ButtonType("Login",ButtonData.OK_DONE);
      ButtonType create = new ButtonType("New User");
      ButtonType cancel = new ButtonType("Cancel",ButtonData.CANCEL_CLOSE);
      
      alert.getButtonTypes().setAll(login,create,cancel);
      Optional<ButtonType> result = alert.showAndWait();
      
      // login pressed
      if (result.get() == login)
      {
         String[] toRet = {"0",username.getText()};
         return toRet;
      }
      // new user pressed
      if (result.get() == create)
      {
         String[] toRet = {"1",username.getText()};
         return toRet;
      }
      // cancel pressed or frame closed
      String[] toRet = {"2",""};
      return toRet;
   }
   
   /**
    * new user creation panel
    * @return {user choice as String, username as String, full name as String, email as String}
    */
   protected static String[] displayNewUserPane()
   {
      Alert alert = new Alert(AlertType.NONE);
      alert.setTitle("Create New User");
      alert.setHeaderText("Please fill in the following fields to create a new account...");
      
      GridPane grid = new GridPane();
      grid.setHgap(10);
      grid.setVgap(10);
      grid.setPadding(new Insets(20, 150, 10, 10));

      TextField username = new TextField();
      username.setPromptText("Username:");
      TextField fullname = new TextField();
      fullname.setPromptText("First Last");
      TextField email = new TextField();
      email.setPromptText("First.Last@HDRinc.com");
      
      grid.add(new Label("Username:"), 0, 0);
      grid.add(username, 1, 0);
      grid.add(new Label("Full Name:"),0,1);
      grid.add(fullname, 1, 1);
      grid.add(new Label("Email:"), 0, 2);
      grid.add(email, 1, 2);
      
      alert.getDialogPane().setContent(grid);
      
      Platform.runLater(() -> username.requestFocus());
      
      ButtonType submit = new ButtonType("Submit");
      ButtonType cancel = new ButtonType("Cancel",ButtonData.CANCEL_CLOSE);
      
      alert.getButtonTypes().setAll(submit,cancel);
      Optional<ButtonType> result = alert.showAndWait();
      
      String[] values = new String[4];
      // submit pressed
      if (result.get() == submit)
      {
         values[0] = "1";
         values[1] = username.getText();
         values[2] = fullname.getText();
         values[3] = email.getText();
         return values;
         
      }
      // cancel pressed or frame closed
      values[0] = "0";
      return values;
   }
   
   /**
    * 
    * @param info an array containing an int in index 0 and the username, full name, and 
    * email in the following indices
    * @return user's choice of button, 1 = yes, 2 = no, anything else = cancel
    */
   protected static int displayConfirmUserPane(String[] info)
   {
      Alert alert = new Alert(AlertType.NONE);
      alert.setTitle("Create New User");
      alert.setHeaderText("Is the following information correct?");
      
      GridPane grid = new GridPane();
      grid.setHgap(10);
      grid.setVgap(10);
      grid.setPadding(new Insets(20, 150, 10, 10));
      
      grid.add(new Label("Username:"), 0, 0);
      grid.add(new Label(info[1]), 1, 0);
      grid.add(new Label("Full Name:"), 0, 1);
      grid.add(new Label(info[2]), 1, 1);
      grid.add(new Label("Email:"), 0, 2);
      grid.add(new Label(info[3]), 1, 2);
      
      alert.getDialogPane().setContent(grid);
      
      ButtonType yes = new ButtonType("Yes");
      ButtonType no = new ButtonType("No");
      ButtonType cancel = new ButtonType("Cancel",ButtonData.CANCEL_CLOSE);
      
      alert.getButtonTypes().setAll(yes,no,cancel);
      Optional<ButtonType> result = alert.showAndWait();
      
      // yes pressed
      if (result.get() == yes)
         return 1;
      if (result.get() == no)
         return 2;
      // cancel pressed or frame closed
      return 0;
   }
   
   
   protected static String displayUserChooser() throws SQLException
   {
      Alert alert = new Alert(AlertType.NONE);
      alert.setTitle("Create New User");
      alert.setHeaderText("Please select a username from the list");
      
      GridPane grid = new GridPane();
      grid.setHgap(10);
      grid.setVgap(10);
      grid.setPadding(new Insets(20, 150, 10, 10));
      
      ComboBox<String> usernames = new ComboBox<>();
      usernames.getItems().addAll(Main.database.getAllUsers());
      usernames.getItems().remove("admin");
      usernames.getSelectionModel().select(0);
      grid.add(usernames, 0, 0);
      
      alert.getDialogPane().setContent(grid);
      
      ButtonType submit = new ButtonType("Submit");
      ButtonType cancel = new ButtonType("Cancel",ButtonData.CANCEL_CLOSE);
      
      alert.getButtonTypes().setAll(submit,cancel);
      Optional<ButtonType> result = alert.showAndWait();
      
      if (result.get() == submit)
         return usernames.getSelectionModel().getSelectedItem();
      
      else
         return "";
   }
   
   /**
    * Allows user to input new information to create a new job or edit an old one
    * @param toEdit
    * @return
    */
   protected static Job displayNewJobPane(Job toEdit)
   {
      Job toCreate = new Job();
      
      if (toEdit.getDbrefnum() != -1)
      {
         toCreate.setDbrefnum(toEdit.getDbrefnum());
         toCreate.setEquipment(toEdit.getEquipment());
         toCreate.setReturnDate(toEdit.getReturnDate());
      }
      
      Alert alert = new Alert(AlertType.NONE);
      alert.setTitle("Create New Job");
      alert.setHeaderText("Please fill in the following fields to create a new job...");
      
      GridPane grid = new GridPane();
      grid.setHgap(10);
      grid.setVgap(10);
      grid.setPadding(new Insets(20, 150, 10, 10));

      TextField projname = new TextField();
      projname.setPromptText("Project Name");
      TextField projnum = new TextField();
      projnum.setPromptText("Project Number");
      TextField act = new TextField();
      act.setText("001");
      TextField dept = new TextField();
      dept.setText("043");
      TextField destination = new TextField();
      destination.setPromptText("Destination");
      
      if (toEdit.getDbrefnum() != -1)
      {
         projname.setText(toEdit.getProjname());
         projnum.setText(toEdit.getProjectnumber());
         act.setText(toEdit.getActivity());
         dept.setText(toEdit.getDept_client());
         destination.setText(toEdit.getLocation());
      }
      
      grid.add(new Label("Project Name:"), 0, 0);
      grid.add(projname, 1, 0);
      grid.add(new Label("Project Number:"),0,1);
      grid.add(projnum, 1, 1);
      grid.add(new Label("Activity:"), 0, 2);
      grid.add(act, 1, 2);
      grid.add(new Label("Department:"), 0, 3);
      grid.add(dept, 1, 3);
      grid.add(new Label("Destination:"), 0, 4);
      grid.add(destination, 1, 4);
      
      alert.getDialogPane().setContent(grid);
      
      Platform.runLater(() -> projname.requestFocus());
      
      ButtonType submit = new ButtonType("Submit");
      ButtonType cancel = new ButtonType("Cancel",ButtonData.CANCEL_CLOSE);
      
      alert.getButtonTypes().setAll(submit,cancel);
      Optional<ButtonType> result = alert.showAndWait();
      
      // submit pressed
      if (result.get() == submit)
      {
         toCreate.setReady(true);
         toCreate.setProjname(projname.getText());
         toCreate.setProjectnumber(projnum.getText());
         toCreate.setActivity(act.getText());
         toCreate.setDept_client(dept.getText());
         toCreate.setLocation(destination.getText());
         if (!toCreate.checkFields())
         {
            toCreate.setReady(false);
            displayAlert("Error","Not all fields filled", "Not all the fields on the form"
                  + " are filled, please try again");
            return toCreate;
         }
      }
      else
      {
         toCreate.setReady(false);
         return toCreate;
      }
      
      // amount of time the equipment will be used for
      alert = new Alert(AlertType.NONE);
      alert.setTitle("Create New Job");
      alert.setHeaderText("How long will you be checking these items out for?");
      
      grid = new GridPane();
      grid.setHgap(10);
      grid.setVgap(10);
      grid.setPadding(new Insets(20, 150, 10, 10));
      
      ComboBox<String> numbers = new ComboBox<>();
      for (Integer i = 1; i < 32; i++)
         numbers.getItems().add(i.toString());
      ComboBox<String> units = new ComboBox<>();
      units.getItems().add("day(s)");
      units.getItems().add("week(s)");
      units.getItems().add("month(s)");
      units.getItems().add("year(s)");
      
      grid.add(numbers, 0, 0);
      grid.add(units, 1, 0);
      
      if (toEdit.getReturnDate() != null && toEdit.getDbrefnum() != -1)
      {
         Long time = toEdit.getReturnDate().getTime() - toEdit.getDatetime().getTime();
         time /= 86400000L;
         
         if (time >= 365)
         {
            time /= 365;
            numbers.getSelectionModel().select(time.toString());
            units.getSelectionModel().select("year(s)");
         }
         else if (time >= 30)
         {
            time /= 30;
            numbers.getSelectionModel().select(time.toString());
            units.getSelectionModel().select("month(s)");
         }
         else if (time >= 7)
         {
            time /= 7;
            numbers.getSelectionModel().select(time.toString());
            units.getSelectionModel().select("week(s)");
         }
         else
         {
            numbers.getSelectionModel().select(time.toString());
            units.getSelectionModel().select("day(s)");
         }
      }
      
      alert.getDialogPane().setContent(grid);
      
      submit = new ButtonType("Submit");
      cancel = new ButtonType("Cancel",ButtonData.CANCEL_CLOSE);
      
      alert.getButtonTypes().setAll(submit,cancel);
      result = alert.showAndWait();
      
      if (result.get() == submit)
      {
         Long seconds = Long.parseLong(numbers.getSelectionModel().getSelectedItem());
         switch(units.getSelectionModel().getSelectedItem())
         {
            case "day(s)" : seconds *= 86400L;
               break;
            case "week(s)" : seconds *= 86400L * 7L;
               break;
            case "month(s)" : seconds *= 86400L * 30L;
               break;
            case "year(s)" : seconds *= 86400L * 365L;
               break;
            default: toCreate.setReady(false);
               return toCreate;
         }
         
         // number of seconds to add to current time
         seconds *= 1000;
         toCreate.setReturnDate(new Date(seconds + System.currentTimeMillis()));
      }
      else
      {
         toCreate.setReady(false);
         return toCreate;
      }
      
      // comments
      alert = new Alert(AlertType.NONE);
      alert.setTitle("Create New Job");
      alert.setHeaderText("Please add any comments you have for the job...");
      
      grid = new GridPane();
      grid.setHgap(10);
      grid.setVgap(10);
      grid.setPadding(new Insets(20, 150, 10, 10));
      
      TextArea comments = new TextArea("");
      comments.wrapTextProperty().set(true);
      grid.add(comments, 0, 0);
      
      if (toEdit.getDbrefnum() != -1)
      {
         comments.setText(toEdit.getComments());
      }
      
      alert.getDialogPane().setContent(grid);
      
      submit = new ButtonType("Submit");
      cancel = new ButtonType("Cancel",ButtonData.CANCEL_CLOSE);
      
      alert.getButtonTypes().setAll(submit,cancel);
      result = alert.showAndWait();
      
      if (result.get() == submit)
      {
         toCreate.setComments(comments.getText());
         toCreate.setUsername(Main.user);
         return toCreate;
      }
      toCreate.setReady(false);
      return toCreate;
   }
   
   /**
    * Allows the user to input a new piece of equipment or edit an old one
    * @param oldValues
    * @return [user choice, id, name, manufacturer, calibration interval, comments]
    */
   protected static Equipment displayNewEquipmentPane(Equipment toEdit)
   {
      Equipment toCreate = new Equipment();
      if (!toEdit.getName().equals(""))
      {
         toCreate.setCurrentuser(toEdit.getCurrentuser());
         toCreate.setDbrefnum(toEdit.getDbrefnum());
         toCreate.setCheckedout(toEdit.getCheckedout());
         toCreate.setEstimatedreturn(toEdit.getEstimatedreturn());
      }
      toCreate.setId(toEdit.getId());
      
      Alert alert = new Alert(AlertType.NONE);
      alert.setTitle("Create New Equipment");
      alert.setHeaderText("Please fill in the following fields to create a new piece of equipment...");
      
      GridPane grid = new GridPane();
      grid.setHgap(10);
      grid.setVgap(10);
      grid.setPadding(new Insets(20, 150, 10, 10));

      TextField itemname = new TextField(toEdit.getName());
      itemname.setPromptText("Equipment Name");
      TextField manuf = new TextField(toEdit.getManufacturer());
      manuf.setPromptText("Equipment Manufacturer");
      TextArea comments = new TextArea(toEdit.getComments());
      
      grid.add(new Label("Equipment Name:"), 0, 0);
      grid.add(itemname, 1, 0);
      grid.add(new Label("Manufacturer:"), 0, 1);
      grid.add(manuf, 1, 1);
      grid.add(new Label("Comments"), 0, 2);
      grid.add(comments, 1, 2);
      
      alert.getDialogPane().setContent(grid);
      
      Platform.runLater(() -> itemname.requestFocus());
      
      ButtonType submit = new ButtonType("Submit");
      ButtonType cancel = new ButtonType("Cancel",ButtonData.CANCEL_CLOSE);
      
      alert.getButtonTypes().setAll(submit,cancel);
      Optional<ButtonType> result = alert.showAndWait();
      
      if (result.get() == submit)
      {
         toCreate.setName(itemname.getText());
         toCreate.setManufacturer(manuf.getText());
         toCreate.setComments(comments.getText());
         
         if (!toCreate.checkFields())
         {
            toCreate.setReady(false);
            return toCreate;
         }
         
      }
      else
      {
         toCreate.setReady(false);
         return toCreate;
      }
      
      Alert alert2 = new Alert(AlertType.NONE);
      alert2.setTitle("Create New Equipment");
      alert2.setHeaderText("How often does the item need to be calibrated?\n(if it does not "
            + "need to be calibrated leave the number as 0)");
      
      GridPane grid2 = new GridPane();
      grid2.setHgap(10);
      grid2.setVgap(10);
      grid2.setPadding(new Insets(20, 150, 10, 10));
      
      ComboBox<String> numbers = new ComboBox<>();
      for (Integer i = 0; i < 32; i++)
         numbers.getItems().add(i.toString());
      ComboBox<String> units = new ComboBox<>();
      units.getItems().add("day(s)");
      units.getItems().add("week(s)");
      units.getItems().add("month(s)");
      units.getItems().add("year(s)");
      
      numbers.getSelectionModel().select(0);
      units.getSelectionModel().select(0);
      
      grid2.add(numbers, 0, 0);
      grid2.add(units, 1, 0);
      
      
      if (toEdit.getCalibrationinterval() != 0)
      {
         Long time = toEdit.getCalibrationinterval();
         time /= 86400000L;   // number of days
         if (time >= 365)
         {
            time /= 365;
            numbers.getSelectionModel().select(time.toString());
            units.getSelectionModel().select("year(s)");
         }
         else if (time >= 30)
         {
            time /= 30;
            numbers.getSelectionModel().select(time.toString());
            units.getSelectionModel().select("month(s)");
         }
         else if (time >= 7)
         {
            time /= 7;
            numbers.getSelectionModel().select(time.toString());
            units.getSelectionModel().select("week(s)");
         }
         else
         {
            numbers.getSelectionModel().select(time.toString());
            units.getSelectionModel().select("day(s)");
         }
      }
      
      
      alert2.getDialogPane().setContent(grid2);
      submit = new ButtonType("Submit");
      cancel = new ButtonType("Cancel",ButtonData.CANCEL_CLOSE);
      
      alert2.getButtonTypes().setAll(submit,cancel);
      result = alert2.showAndWait();
      
      if (result.get() == submit)
      {
         Long seconds = Long.parseLong(numbers.getSelectionModel().getSelectedItem());
         switch(units.getSelectionModel().getSelectedItem())
         {
            case "day(s)" : seconds *= 86400L;
               break;
            case "week(s)" : seconds *= 86400L * 7L;
               break;
            case "month(s)" : seconds *= 86400L * 30L;
               break;
            case "year(s)" : seconds *= 86400L * 365L;
               break;
            default: toCreate.setReady(false);
                     return toCreate;
         }
         // number of seconds to add to current time
         seconds *= 1000;
         toCreate.setCalibrationinterval(seconds);
         toCreate.setReady(true);
         return toCreate;
      }
      else
      {
         toCreate.setReady(false);
         return toCreate;
      }
   }
   
   /**
    * allows for the changing of jobs for a piece of equipment, this however limits the
    * jobs to the first 10 preset jobs for equipment that is broken, out for calibration, etc
    * not including checked in.
    * @return the corresponding dbrefnum
    */
   protected static int displayEquipmentStatus()
   {
      Alert alert = new Alert(AlertType.NONE);
      alert.setTitle("Create New Job");
      alert.setHeaderText("Please add any comments you have for the job...");
      
      GridPane grid = new GridPane();
      grid.setHgap(10);
      grid.setVgap(10);
      grid.setPadding(new Insets(20, 150, 10, 10));
      
      ComboBox<String> jobs = new ComboBox<>();
      jobs.getItems().add("Checked In (not broken or being calibrated)");
      jobs.getItems().add("Out of Service (broken)");
      jobs.getItems().add("Out for Calibration");
      jobs.getItems().add("Personal Item");
      jobs.getItems().add("None of the Above");
      
      grid.add(jobs, 0, 0);
      jobs.getSelectionModel().select(4);
      
      alert.getDialogPane().setContent(grid);
      ButtonType submit = new ButtonType("Submit");
      ButtonType cancel = new ButtonType("Cancel",ButtonData.CANCEL_CLOSE);
      
      alert.getButtonTypes().setAll(submit,cancel);
      Optional<ButtonType> result = alert.showAndWait();
      
      if (result.get() == submit)
      {
         String job = jobs.getSelectionModel().getSelectedItem();
         switch(job)
         {
            case "Checked In (not broken or being calibrated)" : return 0;
            case "Out of Service (broken)" : return 1;
            case "Out for Calibration" : return 2;
            case "Personal Item" : return 4;
            case "None of the Above" : return -1;
            default: return -1;
         }
      }
      return -1;
   }
   
   /**
    * 
    * @return
    * @throws SQLException
    */
   protected static int displayJobChooser() throws SQLException
   {
      Alert alert = new Alert(AlertType.NONE);
      alert.setTitle("Choose an Existing Job");
      alert.setHeaderText("Please type in your job's number");
      
      GridPane grid = new GridPane();
      grid.setHgap(10);
      grid.setVgap(10);
      grid.setPadding(new Insets(20, 150, 10, 10));
      
      TextField jobChooser = new TextField();
      
      grid.add(jobChooser, 0, 0);
      
      alert.getDialogPane().setContent(grid);
      
      Platform.runLater(() -> jobChooser.requestFocus());
      
      alert.getButtonTypes().setAll(ButtonType.OK,ButtonType.CANCEL);
      Optional<ButtonType> result = alert.showAndWait();
      
      if (result.get() == ButtonType.OK)
      {
         String projNum = jobChooser.getText();
         if (Main.database.checkJobExists(projNum))
            return Main.database.getJobDBrefnum(projNum);
         else
            return -1;
      }
      return -1;
   }
   
   
   protected static String displayJobNumberInput()
   {
      Alert alert = new Alert(AlertType.NONE);
      alert.setTitle("Create New Job");
      alert.setHeaderText("Please add any comments you have for the job...");
      
      GridPane grid = new GridPane();
      grid.setHgap(10);
      grid.setVgap(10);
      grid.setPadding(new Insets(20, 150, 10, 10));
      
      TextField projnum = new TextField();
      
      grid.add(projnum, 0, 0);
      
      alert.getDialogPane().setContent(grid);
      
      Platform.runLater(() -> projnum.requestFocus());
      
      alert.getButtonTypes().setAll(ButtonType.OK,ButtonType.CANCEL);
      Optional<ButtonType> result = alert.showAndWait();
      
      // MAKE BUTTONS
      
      if (result.get() == ButtonType.OK)
      {
         return projnum.getText();
         
      }
      return "";
   }
   
   /**
    * generic message box, use when asking questions with 1-2 answers not including cancel
    * @return 1 for first button, 2 for second, 0 for cancel
    */
   protected static int displayConfirmDialog(String message, int buttons, String[] buttonNames)
   {
      Alert alert = new Alert(AlertType.CONFIRMATION);
      alert.setTitle("Notice");
      alert.setHeaderText("Before you continue...");
      alert.setContentText(message);
      
      ButtonType buttonTypeOne, buttonTypeTwo, buttonTypeThree;
      
      buttonTypeOne = new ButtonType(buttonNames[0]);
      ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
      
      if (buttons == 2)
      {
         buttonTypeTwo = new ButtonType(buttonNames[1]);
         alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeCancel);
         Optional<ButtonType> result = alert.showAndWait();
         if (result.get() == buttonTypeOne)
         {
             return 1;
         }
         else if (result.get() == buttonTypeTwo)
         {
             return 2;
         }
         else
         {
             return 0;
         }
      }
      else if (buttons == 3)
      {
         buttonTypeTwo = new ButtonType(buttonNames[1]);
         buttonTypeThree = new ButtonType(buttonNames[2]);
         alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeThree
               , buttonTypeCancel);
         Optional<ButtonType> result = alert.showAndWait();
         if (result.get() == buttonTypeOne)
         {
             return 1;
         }
         else if (result.get() == buttonTypeTwo)
         {
             return 2;
         }
         else if (result.get() == buttonTypeThree)
         {
             return 3;
         }
         else
         {
             return 0;
         }
      }
      else
      {
         alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeCancel);
         Optional<ButtonType> result = alert.showAndWait();
         if (result.get() == buttonTypeOne)
         {
             return 1;
         }
         else
         {
             return 0;
         }
      }
   }
   
   /**
    * generic alert popup, returns itself so that it can be closed in another class
    * @param title
    * @param header
    * @param message
    * @return
    */
   protected static Alert displayAlert(String title, String header, String message)
   {
      Alert alert = new Alert(AlertType.INFORMATION);
      alert.setTitle(title);
      alert.setHeaderText(header);
      alert.setContentText(message);
      alert.show();
      return alert;
   }
   
   /**
    * displays when item id does not match selected
    */
   protected static void displayMatchFailure()
   {
      Alert alert = new Alert(AlertType.INFORMATION);
      alert.setTitle("Error");
      alert.setHeaderText("Failure");
      String s = "The seleced item's id does not match the id scanned";
      alert.setContentText(s);
      alert.show();
   }
   
   
   /**
    * method called when an item is scanned that isn't in the database
    * @param id barcode id scanned in
    */
   protected static void itemNotRecognized(String id)
   {
      String[] a = {"Continue"};
      WindowHandler.displayConfirmDialog("The item scanned is not recognized by the database. If this is a new "
            + "item, please fill out the following form, if not, please cancel this transaction and "
            + "rescan.  If this issue persists, please contace the database administrator.",1,a);
   }
}