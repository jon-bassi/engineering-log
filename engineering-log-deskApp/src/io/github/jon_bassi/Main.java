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
   
   public static final String build = "1.2.1 beta";
   
   private final Boolean DEBUG = false;
   
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
      if (!DEBUG)
      {
         showLogin();
      }
      else
         user = "admin";
      
      // check user against user table
      // pull up list of users - note, later grab the full name as well so it can be placed into GUI
      ArrayList<String> users = database.getAllUsers();
      // if not present ask if they would like to create a new username or re-input their name
      // else continue to GUI
      
      if (!users.contains(user))
      {
         String[] options = {"Create New User","Re-input Username"};
         switch(WindowHandler.displayConfirmDialog(user + " is not a recognized username. Please choose and option:",2,options))
         {
         // create new user - with some check system - include email here as well - check for duplicates?- will just throw error
         case 1 :
            createNewUser();
            break;
            
         // re-input
         case 2 :
            do
            {
               showLogin();
            } while(!users.contains(user));
            break;
            
         // default case
         default : System.exit(0);
         }
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
   
   private void showLogin() throws SQLException
   {
      String[] result = WindowHandler.displayLoginPane();
      int res = Integer.parseInt(result[0]);
      user = result[1].toLowerCase();
      
      if (res == 1)
      {
         user = createNewUser();
      }
      else if (user == null || user.equals("") || res == 2)
         System.exit(0);
      
      
      return;
   }
   
   /**
    * calls panels to input user information and submits it to the database
    * @throws SQLException
    */
   private String createNewUser() throws SQLException
   {
      // creation of user
      String[] result;
      int res = 0;
      
      do
      {
         result = WindowHandler.displayNewUserPane();
         res = Integer.parseInt(result[0]);
         
         if (res != 1 || result[1] == null || result[1].equals(""))
            System.exit(0);
         
         res = WindowHandler.displayConfirmUserPane(result);
         
         if ((res != 1 && res != 2) || result[1] == null || result[1].equals(""))
            System.exit(0);
      } while (res != 1);
      
      System.out.println("new user created");
      database.insertNewUser(result[1].toLowerCase(), result[2], result[3]);
      return result[1].toLowerCase();
   }
   
}
