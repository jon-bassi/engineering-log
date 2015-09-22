package io.github.jon_bassi;

import io.github.jon_bassi.view.ExceptionHandler;
import io.github.jon_bassi.view.WindowHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * 
 * @author jon-bassi
 *
 */
public class Main extends Application
{
   
   public static String user;
   
   public static EngDB database;
   
   public static final String build = "1.4.0-beta";
   
   private final boolean DEBUG = false;
   
   public static void main(String[] args)
   {
      
      try
      {
      Application.launch(args);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
      
   }
   
   
   @ Override
   public void start(Stage primaryStage) throws SQLException, IOException
   {
      
      database = new EngDB();
      
      // login logic & debug catch - use command line instead of code input
      // check user against user table
      // pull up list of users - note, later grab the full name as well so it can be placed into GUI
      ArrayList<String> users = database.getAllUsers();
      users.remove("admin");
      if (!DEBUG)
      {
         showLogin(users);
      }
      else
      {
         user = "admin";
      }
      
      // load main GUI
      try {
         Parent root = FXMLLoader.load(getClass().getResource("/io/github/jon_bassi/deskApp.fxml"));
         primaryStage.setTitle(user + " - Engineering Equipment Database");
         Scene scene = new Scene(root,1150,720);
         primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() 
         {
            public void handle(WindowEvent we){
               try {
                  System.exit(0);
                 } catch (Exception e) {
                    ExceptionHandler.displayException(e);
                    System.exit(0);
                 }
             }
          });
         primaryStage.setScene(scene);
         primaryStage.show();
      } catch (Exception e) {
         ExceptionHandler.displayException(e);
         System.exit(0);
      }
      
   }
   
   private void showLogin(ArrayList<String> users) throws SQLException
   {
      String[] result = WindowHandler.displayLoginPane(users);
      int res = Integer.parseInt(result[0]);
      user = result[1].toLowerCase();
      
      if (user.equals("") || res == 2)
      {
         System.exit(0);
      }
      
      return;
   }
}
