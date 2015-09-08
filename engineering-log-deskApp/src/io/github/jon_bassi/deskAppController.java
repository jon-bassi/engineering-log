package io.github.jon_bassi;

import io.github.jon_bassi.db.objects.Equipment;
import io.github.jon_bassi.db.objects.Job;
import io.github.jon_bassi.view.ExceptionHandler;
import io.github.jon_bassi.view.ScanningHandler;
import io.github.jon_bassi.view.WindowHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.TreeSet;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * TODO : -prepare for job type selection (items are a part of a type, types need a certain
 *         amount of items
 *        -make sure items which are broken are attributed to admin???
 *        -look at Check Out Item method for correct algorithm
 *        -selecting to add to a new job and then typing in an existing job number
 *         shows error even though it worked
 *        -add last calibration date to calibration form
 *        -replace all throws with try catch to Exception handler
 * @author jon-bassi
 *
 */
public class deskAppController implements Initializable
{
   private long lastUpdate;
   private final long TIME_OUT = 900000;
   private Runnable runnable;
   
   @ FXML
   private ListView<String> checkedOutList;
   @ FXML
   private ListView<String> calibrationList;
   @ FXML
   private ListView<String> checkedOutAll;
   @ FXML
   private ListView<String> pastDueAll;
   @ FXML
   private ListView<String> searchResults;
   
   // Home tab
   @ FXML
   private Label currUser;
   @ FXML
   private Label currID;
   @ FXML
   private Label currManu;
   @ FXML
   private Label currName;
   @ FXML
   private Label currDate;
   @ FXML
   private Label currNum;
   @ FXML
   private Label currDept;
   @ FXML
   private Label currAct;
   @ FXML
   private TextArea currComments;
   
   // Checked Out tab
   @ FXML
   private Label checkedOutCurrUser;
   @ FXML
   private Label checkedOutCurrID;
   @ FXML
   private Label checkedOutCurrManu;
   @ FXML
   private Label checkedOutCurrName;
   @ FXML
   private Label checkedOutCurrDate;
   @ FXML
   private Label checkedOutCurrNum;
   @ FXML
   private Label checkedOutCurrDept;
   @ FXML
   private Label checkedOutCurrAct;
   @ FXML
   private TextArea checkedOutCurrComments;
   
   // Past Due tab
   @ FXML
   private Label pastDueCurrUser;
   @ FXML
   private Label pastDueCurrID;
   @ FXML
   private Label pastDueCurrManu;
   @ FXML
   private Label pastDueCurrName;
   @ FXML
   private Label pastDueCurrDate;
   @ FXML
   private Label pastDueCurrNum;
   @ FXML
   private Label pastDueCurrDept;
   @ FXML
   private Label pastDueCurrAct;
   @ FXML
   private TextArea pastDueCurrComments;
   // Search tab
   @ FXML
   private TextField searchField;
   @ FXML
   private Label searchUser;
   @ FXML
   private Label searchID;
   @ FXML
   private Label searchManu;
   @ FXML
   private Label searchName;
   @ FXML
   private Label searchDate;
   @ FXML
   private Label searchNum;
   @ FXML
   private Label searchDept;
   @ FXML
   private Label searchAct;
   @ FXML
   private TextArea searchComments;
   
   
   @ Override
   /**
    * sets up GUI home page when opened, first method called, ignore args
    * @param arg0
    * @param arg1
    */
   public void initialize(URL arg0, ResourceBundle arg1)
   {
      updateCheckedOut();
      updateCalibrations();
      
      // new thread for loading things not on the front page - won't stall the program on load
      // also include checks and updates here, updates can be tested once a week with current/next
      // update time stored in another database table
      runnable = new Runnable(){
         @Override
         public void run()
         {
            // update all the checked out items on page 2 and 3
            
            updateCheckedOutAll();
            updatePastDue();
            
            lastUpdate = System.currentTimeMillis();
            
            // this loop here keeps track of the time of the last performed action
            // disconnecting the database connection after the number of seconds
            // given by TIME_OUT
            while(true)
            {
               long delta = System.currentTimeMillis() - lastUpdate;
               if (delta >= TIME_OUT)
               {
                  try
                  {
                     Main.database.disconnect();
                  } catch (Exception e)
                  {
                     e.printStackTrace();
                  }
               }
               
               try
               {
                  Thread.sleep(5000);
               } catch (InterruptedException e)
               {
                  e.printStackTrace();
               }
            }
         }
      };
      Thread thread = new Thread(runnable);
      thread.start();
   }
   
   @ FXML
   /**
    * changes the information in the top-right info panel based on
    * selection in either list
    */
   public void viewCurrInfo() throws SQLException, FileNotFoundException, ClassNotFoundException, IOException
   {
      // get the information from db
      Main.database.reconnect();
      lastUpdate = System.currentTimeMillis();
      int list = 1;
      
      ArrayList<ListView<String>> lists = new ArrayList<>();
      lists.add(checkedOutList);
      lists.add(calibrationList);
      
      // check which frame is selected
      if (checkedOutList.isFocused())
         list = 0;
      
      if (lists.get(list).getSelectionModel().getSelectedItem() == null)
         return;
      String name = lists.get(list).getSelectionModel().getSelectedItem();
      int idx = name.indexOf(' ');
      String id = name.substring(0, idx);
      name = name.substring(idx+1,name.length());
      idx = name.indexOf(' ');
      name = name.substring(idx+1,name.length());
      
      Equipment currItem = new Equipment(Main.database.getItemInfo(id));
      
      currUser.setText(Main.database.getFullname(currItem.getCurrentuser()));
      currID.setText(currItem.getId());
      currManu.setText(currItem.getManufacturer());
      currName.setText(currItem.getName());
      currDate.setText(currItem.getCheckedout().toString());
      Job currJob = new Job(Main.database.getJobInfo(currItem.getDbrefnum()));
      if (currJob.getDbrefnum() <= 10)
      {
         currNum.setText("none");
         currAct.setText("none");
         currDept.setText("none");
      }
      else
      {
         currNum.setText(currJob.getProjectnumber());
         currAct.setText(currJob.getActivity());
         currDept.setText(currJob.getDept_client());
      }
      currComments.setText(currItem.getComments());
   }
   
   /**
    * Search algorithm for the equipment search tab, pretty basic all terms within the
    * text field use OR functionality while searching the database, not AND
    * @throws SQLException 
    * @throws IOException 
    * @throws ClassNotFoundException 
    * @throws FileNotFoundException 
    */
   public void equipmentSearch() throws SQLException, FileNotFoundException, ClassNotFoundException, IOException
   {
      Main.database.reconnect();
      lastUpdate = System.currentTimeMillis();
      
      String searchString = searchField.getText();
      
      if (searchString.contains("-"))
      {
         searchString = searchString.substring(searchString.lastIndexOf('-'), searchString.length());
      }
      searchString = searchString.toLowerCase();
      String[] searchStrings = searchString.split(" ");
      ArrayList<String> dbStrings = Main.database.getAllStrings();
      TreeSet<String> results = new TreeSet<>();
      
      for (String s : dbStrings)
      {
         if (s.equalsIgnoreCase(searchString))
            results.add(s);
         else if (s.contains(searchString))
            results.add(s);
         else if (s.toLowerCase().contains(searchString))
            results.add(s);
         else
         {
            String[] splitS = s.split(" ");
            for (String split : splitS)
            {
               split = split.substring(1, split.length());
               
               for (String ssSplit : searchStrings)
               {
                  String tempSS = ssSplit.substring(1, ssSplit.length());
                  if (split.equals(tempSS) || split.contains(tempSS))
                     results.add(s);
               }
            }
         }
      }
      
      ArrayList<String> itemResults = Main.database.getAllSearched(results);
      
      searchResults.getItems().clear();
      for (String s : itemResults)
         searchResults.getItems().add(s);
      searchResults.getSelectionModel().select(1);
      setSearchedInfo();
   }
   
   /**
    * sets the info for the selected items on the checked out (all) page
    * @throws SQLException 
    * @throws IOException 
    * @throws ClassNotFoundException 
    * @throws FileNotFoundException 
    */
   public void setCheckedOutInfo() throws SQLException, FileNotFoundException, ClassNotFoundException, IOException
   {
      Main.database.reconnect();
      lastUpdate = System.currentTimeMillis();
      
      String selected = checkedOutAll.getSelectionModel().getSelectedItem();
      if (selected == null || selected.equals(""))
      {
         checkedOutCurrUser.setText("");
         checkedOutCurrID.setText("");
         checkedOutCurrManu.setText("");
         checkedOutCurrName.setText("");
         checkedOutCurrDate.setText("");
         checkedOutCurrNum.setText("");
         checkedOutCurrAct.setText("");
         checkedOutCurrDept.setText("");
         checkedOutCurrComments.setText("");
         return;
      }
      
      int idx = selected.indexOf(' ');
      String id = selected.substring(0, idx);
      selected = selected.substring(idx+1,selected.length());
      idx = selected.indexOf(' ');
      selected = selected.substring(idx+1,selected.length());
      
      Equipment currItem = new Equipment(Main.database.getItemInfo(id));
      
      checkedOutCurrUser.setText(Main.database.getFullname(currItem.getCurrentuser()));
      checkedOutCurrID.setText(currItem.getId());
      checkedOutCurrManu.setText(currItem.getManufacturer());
      checkedOutCurrName.setText(currItem.getName());
      checkedOutCurrDate.setText(currItem.getCheckedout().toString());
      Job currJob = new Job(Main.database.getJobInfo(currItem.getDbrefnum()));
      if (currJob.getDbrefnum() <= 10)
      {
         checkedOutCurrNum.setText("none");
         checkedOutCurrAct.setText("none");
         checkedOutCurrDept.setText("none");
      }
      else
      {
         checkedOutCurrNum.setText(currJob.getProjectnumber());
         checkedOutCurrAct.setText(currJob.getActivity());
         checkedOutCurrDept.setText(currJob.getDept_client());
      }
      checkedOutCurrComments.setText(currItem.getComments());
   }
   
   /**
    * sets the info for the selected items on the past due page
    * @throws IOException 
    * @throws SQLException 
    * @throws ClassNotFoundException 
    * @throws FileNotFoundException 
    */
   public void setPastDueInfo() throws FileNotFoundException, ClassNotFoundException, SQLException, IOException
   {
      Main.database.reconnect();
      lastUpdate = System.currentTimeMillis();
      
      String selected = pastDueAll.getSelectionModel().getSelectedItem();
      if (selected == null || selected.equals(""))
      {
         pastDueCurrUser.setText("");
         pastDueCurrID.setText("");
         pastDueCurrManu.setText("");
         pastDueCurrName.setText("");
         pastDueCurrDate.setText("");
         pastDueCurrNum.setText("");
         pastDueCurrAct.setText("");
         pastDueCurrDept.setText("");
         pastDueCurrComments.setText("");
         return;
      }
      
      int idx = selected.indexOf(' ');
      String id = selected.substring(0, idx);
      selected = selected.substring(idx+1,selected.length());
      idx = selected.indexOf(' ');
      selected = selected.substring(idx+1,selected.length());
      
      Equipment currItem = new Equipment(Main.database.getItemInfo(id));
      
      pastDueCurrUser.setText(Main.database.getFullname(currItem.getCurrentuser()));
      pastDueCurrID.setText(currItem.getId());
      pastDueCurrManu.setText(currItem.getManufacturer());
      pastDueCurrName.setText(currItem.getName());
      pastDueCurrDate.setText(currItem.getCheckedout().toString());
      Job currJob = new Job(Main.database.getJobInfo(currItem.getDbrefnum()));
      if (currJob.getDbrefnum() <= 10)
      {
         pastDueCurrNum.setText("none");
         pastDueCurrAct.setText("none");
         pastDueCurrDept.setText("none");
      }
      else
      {
         pastDueCurrNum.setText(currJob.getProjectnumber());
         pastDueCurrAct.setText(currJob.getActivity());
         pastDueCurrDept.setText(currJob.getDept_client());
      }
      pastDueCurrComments.setText(currItem.getComments());
   }
   
   /**
    * sets the info for the selected item on the search page
    * @throws SQLException
    * @throws FileNotFoundException
    * @throws ClassNotFoundException
    * @throws IOException
    */
   public void setSearchedInfo() throws SQLException, FileNotFoundException, ClassNotFoundException, IOException
   {
      Main.database.reconnect();
      lastUpdate = System.currentTimeMillis();
      
      String selected = searchResults.getSelectionModel().getSelectedItem();
      if (selected == null || selected.equals("") || selected.equals("Checked In") 
            || selected.equals("Checked Out"))
      {
         searchUser.setText("");
         searchID.setText("");
         searchManu.setText("");
         searchName.setText("");
         searchDate.setText("");
         searchNum.setText("");
         searchAct.setText("");
         searchDept.setText("");
         searchComments.setText("");
         return;
      }
      
      int idx = selected.indexOf(' ');
      String id = selected.substring(0, idx);
      selected = selected.substring(idx+1,selected.length());
      idx = selected.indexOf(' ');
      selected = selected.substring(idx+1,selected.length());
      
      Equipment currItem = new Equipment(Main.database.getItemInfo(id));
      
      searchUser.setText(Main.database.getFullname(currItem.getCurrentuser()));
      searchID.setText(currItem.getId());
      searchManu.setText(currItem.getManufacturer());
      searchName.setText(currItem.getName());
      searchDate.setText(currItem.getCheckedout().toString());
      Job currJob = new Job(Main.database.getJobInfo(currItem.getDbrefnum()));
      if (currJob.getDbrefnum() <= 10)
      {
         searchNum.setText("none");
         searchAct.setText("none");
         searchDept.setText("none");
      }
      else
      {
         searchNum.setText(currJob.getProjectnumber());
         searchAct.setText(currJob.getActivity());
         searchDept.setText(currJob.getDept_client());
      }
      searchComments.setText(currItem.getComments());
   }
   
   /**
    * returns to the login screen - this is really hack-y, sorry
    * @throws IOException 
    * @throws SQLException 
    */
   @ FXML
   public void logout() 
   {
      try
      {
         Main.database.disconnect();
         Scanner file = new Scanner(new File("paths.dat"));
         String command = file.nextLine();
         file.close();
         @SuppressWarnings("unused")
         Process p = Runtime.getRuntime().exec(command);
         System.exit(0);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
         System.exit(0);
      }
      
   }
   
   /**
    * close the program
    */
   @ FXML
   public void menuClose()
   {
      System.exit(0);
   }
   
   @ FXML
   /**
    * refreshes every list (or so I hope)
    */
   public void refresh() throws FileNotFoundException, ClassNotFoundException, SQLException, IOException
   {
      Main.database.reconnect();
      lastUpdate = System.currentTimeMillis();
      updateCheckedOut();
      updateCalibrations();
      updateCheckedOutAll();
      updatePastDue();
   }
   
   @ FXML
   /**
    * Allows for the editing of information for a specific item
    */
   public void editItem() throws FileNotFoundException, ClassNotFoundException, SQLException, IOException
   {
      Main.database.reconnect();
      lastUpdate = System.currentTimeMillis();
      String id = scanItem();
      if (id.equals("") || !Main.database.checkEquipmentExists(id))
      {
         WindowHandler.displayMatchFailure();
         return;
      }
      
      Equipment toEdit = new Equipment(Main.database.getItemInfo(id));
      
      String[] options = {"Edit Status"};
      int result = WindowHandler.displayConfirmDialog("If you would like to change the status of this"
            + " piece of equipment (Checked In/Broken/Calibration/Personal) please select the Edit"
            + " Status button. If not, press cancel and you will be greeted with the dialog"
            + " for editing other attributes of the current item. DO NOT USE THE FOLLOWING"
            + " OPTIONS TO ADD A SAMPLE TO A JOB (scan single or multiple for that)."
            , 1, options);
      if (result == 1)
      {
         int dbrefnum = WindowHandler.displayEquipmentStatus();
         if (dbrefnum == 4)
         {
            String user = WindowHandler.displayUserChooser();
            toEdit.setEquipmentStatus(dbrefnum, user);
            
            if (toEdit.getCurrentuser().equals(""))
               toEdit.setEquipmentStatus(0);
         }
         else
            toEdit.setEquipmentStatus(dbrefnum);
      }
      
      toEdit = WindowHandler.displayNewEquipmentPane(toEdit);
      
      if (!toEdit.isReady())
      {
         WindowHandler.displayAlert("Failure", "Item information was not changed"
               , "Due to some error in the data (possibly a field left blank) the equipment"
               + " scanned was not edited. Please try again.");
         return;
      }
      
      Main.database.updateItemInfo(toEdit);
      refresh();
   }
   
   @ FXML
   /**
    * Allows for the editing of information for a specific job
    */
   public void editJob() throws FileNotFoundException, ClassNotFoundException, SQLException, IOException
   {
      Main.database.reconnect();
      lastUpdate = System.currentTimeMillis();
      
      int dbrefnum = WindowHandler.displayJobChooser();
      if (dbrefnum == -1 || (dbrefnum >= 0 && dbrefnum < 9))
      {
         WindowHandler.displayAlert("Error", "Job does not exist!", "The job number you"
               + " have entered does not correspond to any job currently in the database,"
               + " please try again.");
         return;
      }
      Job toEdit = new Job();
      try{
      toEdit = new Job(Main.database.getJobInfo(dbrefnum));
      toEdit.setEquipment(Main.database.getItemsForJob(toEdit.getDbrefnum()));
      toEdit = WindowHandler.displayNewJobPane(toEdit);
      } catch (Exception e)
      {
         e.printStackTrace();
      }
      if (!toEdit.isReady())
      {
         WindowHandler.displayAlert("Failure", "Job information was not changed"
               , "Due to some error in the data (possibly a field left blank) the job"
               + " selected was not edited. Please try again.");
         return;
      }
      
      Main.database.updateJobInfo(toEdit);
      refresh();
   }
   
   /**
    * displays the app info
    */
   @ FXML
   public void displayAbout()
   {
      WindowHandler.displayAlert("About","About",
            "ENGDB Desk App build " + Main.build + "\nView source at https://jon-bassi.github.io");
   }
   
   @ FXML
   /**
    * checks in or out the selected item depending on the equipment's job and status
    */
   public void checkInOutSelected() throws SQLException, FileNotFoundException, ClassNotFoundException, IOException
   {
      Main.database.reconnect();
      lastUpdate = System.currentTimeMillis();
      
      String id = currID.getText();
      
      ArrayList<String> itemInfo = Main.database.getItemInfo(id);
      
      // not recognized
      if (itemInfo.size() == 0)
      {
         WindowHandler.itemNotRecognized(id);
         return;
      }
      
      switch (itemInfo.get(1))
      {
         /**
          * Since this list is items that are checked out by the current user, and items
          * are attributed to the admin when checked out this should only display for admin
          * 
          * AUTO CHECK IN SHOULD ONLY HAPPEN IF THE ENTIRE PROCESS IS COMPLETED!!!!
          */
         case "0" : System.out.println("this should only be seen by admin");
            WindowHandler.displayAlert("Notification", "Uh Oh", "Something went wrong...\nplease continue");
            break;
            
         /**
          * Out of Service
          */
         case "1" : String[] a1 = {"Reinstate", "Both"};
            int choice1 = WindowHandler.displayConfirmDialog("This item is currently out of service, would you like to"
               + " reinstate this item, check out and reinstate, or niether?",2,a1);
            // broken - reinstate
            if (choice1 == 1)
            {
               // have user scan item again if the id is the same
               String idCheck = scanItem();
               
               if (idCheck.equals(id))
               {
                  autoCheckIn(id);
                  refresh();
               }
               else
                  WindowHandler.displayMatchFailure();
            }
            // broken - both
            if (choice1 == 2)
            {
            // have user scan item again if the id is the same:
               String idCheck = scanItem();
               if (idCheck.equals(id))
               {
                  String[] options = {"Create New Job","Add to Existing"};
                  int result = WindowHandler.displayConfirmDialog("Would you like to create"
                        + " a new job or add this sample to a preexisting job?",2,options);
                  Job toEdit = null;
                  if (result != 1 && result != 2)
                  {
                     WindowHandler.displayAlert("Failure", "Addition not successful"
                           , "The item was NOT successfully added to a job, please try again");
                     break;
                  }
                  if (result == 2)
                  {
                     toEdit = chooseExistingJob();
                     if (toEdit.isReady())
                     {
                        Equipment toAdd = new Equipment(Main.database.getItemInfo(id));
                        toAdd.addToJob(toEdit);
                        Main.database.updateItemInfo(toAdd);
                        Main.database.updateExistingJob(toEdit);
                        refresh();
                     }
                     else
                     {
                        WindowHandler.displayAlert("Failure", "Addition not successful"
                              , "The item was NOT successfully added to a job, please try again");
                     }
                  }
                  else
                  {
                     toEdit = chooseNewJob();
                     
                     if (!toEdit.isReady())
                     {
                        WindowHandler.displayAlert("Failure", "Addition not successful"
                              , "The item was NOT successfully added to a job, please try again");
                     }
                     
                     // update existing
                     else if (Main.database.checkJobExists(toEdit.getProjectnumber()))
                     {
                        Equipment toAdd = new Equipment(Main.database.getItemInfo(id));
                        toAdd.addToJob(toEdit);
                        Main.database.updateItemInfo(toAdd);
                        Main.database.updateExistingJob(toEdit);
                        refresh();
                     }
                     // create new
                     else
                     {
                        // add job to db
                        int dbrefnum = Main.database.insertNewJob(toEdit);
                        toEdit.setDbrefnum(dbrefnum);
                        // get equipment info
                        Equipment toAdd = new Equipment(Main.database.getItemInfo(id));
                        toAdd.addToJob(toEdit);
                        // update db fields
                        Main.database.updateItemInfo(toAdd);
                        Main.database.updateExistingJob(toEdit);
                        refresh();
                     }
                  }
               }
               else
               {
                  WindowHandler.displayMatchFailure();
               }
            }
            
            break;
            
         /**
          * Out for Calibration
          */
         case "2" : String[] a2 = {"Continue"};
            int choice2 = WindowHandler.displayConfirmDialog("This item is currently checked out for calibration, if "
               + "the calibration is complete please continue and it will be checked in, if not, "
               + "please cancel this transaction.",1,a2);
            // check back in
            if (choice2 == 1)
            {
               String idCheck = scanItem();
               if (id.equals(idCheck))
               {
                  Main.database.updateItemCalibrationDate(id);
                  Main.database.updateItemCheckIn(id);
                  Main.database.insertAudit(id, 2, Main.user, "admin");
               }
            }
            
            break;
         /**
          * Lab Items, unused atm
          */
         case "3" : 
               break;
         /**
          * Personal Items, only allows checking the item in 
          */
         case "4" : 
               String[] a3 = {"Check In"};
               int choice3 = WindowHandler.displayConfirmDialog("You currently have this item checked out"
                     + " as a personal item, from here you can check the item back into the database"
                     + " or cancel, if you wish to change ownership of the item use the Edit Item"
                     + " option in the Edit menu.",1,a3);
               if (choice3 == 1)
               {
                  autoCheckIn(id);
               }
               else
               {
                  return;
               }
               break;
         /**
          * Default - this option is available to everyone
          * TODO : should I change time for all items in this job, yes - idk
          */
         default : String[] a4 = {"Check In","Edit Info"};
            int choice4 = WindowHandler.displayConfirmDialog("You currently have this item checked out, would you like "
               + "to check it back in or edit your check out information?",2,a4);
            // checked out - check in
            if (choice4 == 1)
            {
               String idCheck = scanItem();
               
               if (idCheck.equals(id))
               {
                  autoCheckIn(id);
                  refresh();
               }
               else
                  WindowHandler.displayMatchFailure();
            }
            // checked out - edit
            else if (choice4 == 2)
            {
//               String idCheck = scanItem();
//               if (idCheck.equals(id))
//               {
//                  
//               }
               // scanning made optional right now, may add back in later
               Equipment toAdd = new Equipment(Main.database.getItemInfo(id));
               Job toEdit = new Job(Main.database.getJobInfo(toAdd.getDbrefnum()));
               
               toEdit = WindowHandler.displayNewJobPane(toEdit);
               toEdit.setEquipment(Main.database.getItemsForJob(toEdit.getDbrefnum()));
               Main.database.updateExistingJob(toEdit);
            }
            
            break;
      }
   }
   
   @ FXML
   /**
    * Called when Single Item Button is pressed - checks out item or adds to DB
    */
   public void checkOutItem() throws SQLException, FileNotFoundException, ClassNotFoundException, IOException
   {
      Main.database.reconnect();
      lastUpdate = System.currentTimeMillis();
      
      Equipment toCreate = new Equipment();
      String id = ScanningHandler.scan();
      if (id.equals(""))
      {
         WindowHandler.displayAlert("Failure", "Barcode not recognized"
               , "The item was NOT successfully scanned, please try again.");
         return;
      }
      toCreate.setId(id);
      
      // checking if the item exists so we can add it to the database
      if (!Main.database.checkEquipmentExists(toCreate.getId()))
      {
         toCreate = createNewItem(toCreate);
      }
      else
      {
         toCreate = new Equipment(Main.database.getItemInfo(toCreate.getId()));
      }
      // checking if the item needs calibration or is broken
      if (Main.database.checkEquipmentCalibration(id))
      {
         String[] options = {"Continue","Send for Calibration"};
         int result = WindowHandler.displayConfirmDialog("WARNING: this equipment is currently in need"
               + "of calibration, if you can and will attend to the calibration before use"
               + "on this job, please press continue. If this equipment needs to be sent out for"
               + "calibration, press Send for Calibration and prepare the equipment to be sent."
               + " If you wish to do niether of these options at the moment, please cancel."
               , 2, options);
         if (result == 0)
            return;
         else if (result == 2)
         {
            WindowHandler.displayAlert("Calibration", "Sending for Calibration"
                  , "You have chose to send this equipment out for calibration, please take"
                  + " the required measures to assure this happens smoothly and swiftly."
                  + " During this time the item will be checked out to you under the calibration"
                  + " job, when the item returns please select from the list of your checked"
                  + " out items and check it into the database. Thank you.");
            Main.database.updateItemToCalibration(id);
            Main.database.insertAudit(id, 2, "admin", Main.user);
            refresh();
            return;
         }
      }
      if (Main.database.checkEquipmentBroken(id))
      {
         String[] options = {"Not Broken"};
         int result = WindowHandler.displayConfirmDialog("WARNING: this equipment is currently marked"
               + " as broken. If this is an error or the equipment has been fixed, press Not Broken and"
               + " continue checking out. If it is in fact broken, please cancel and make the"
               + " neccessary arrangements for the equipment to be fixed."
               , 1, options);
         if (result != 1)
         {
            return;
         }
         else
         {
            Main.database.updateItemNotBroken(id);
            Main.database.insertAudit(id, 1, Main.user, "admin");
         }
      }
      
      String[] options = {"New Job","Existing Job","Personal"};
      int result = WindowHandler.displayConfirmDialog("Would you like to create"
            + " a new job or add this item to a preexisting job? If you only needed"
            + " to scan the item into the database please press cancel and accept the following"
            + " error message.",3,options);
      Job toEdit = new Job();
      if (result < 1 || result > 3)
      {
         WindowHandler.displayAlert("Failure", "Addition not successful"
               , "The item was NOT successfully added to a job, please try again.");
         return;
      }
      if (result == 2)
      {
         toEdit = chooseExistingJob();
         if (toEdit.isReady())
         {
            toCreate.addToJob(toEdit);
            Main.database.updateItemInfo(toCreate);
            Main.database.updateExistingJob(toEdit);
            refresh();
         }
         else
         {
            WindowHandler.displayAlert("Failure", "Addition not successful"
                  , "The item was NOT successfully added to a job, please try again.");
         }
      }
      else if (result == 3)
      {
         String user = WindowHandler.displayUserChooser();
         toCreate.setEquipmentStatus(4, user);
         
         if (toCreate.getCurrentuser().equals(""))
         {
            toCreate.setEquipmentStatus(0);
            WindowHandler.displayAlert("Failure", "Addition not successful"
                  , "The item was NOT successfully added to a job, please try again");
         }
         Main.database.updateItemInfo(toCreate);
         refresh();
      }
      else
      {
         toEdit = chooseNewJob();
         
         if (!toEdit.isReady())
         {
            WindowHandler.displayAlert("Failure", "Addition not successful"
                  , "The item was NOT successfully added to a job, please try again");
         }
         
         // update existing
         else if (Main.database.checkJobExists(toEdit.getProjectnumber()))
         {
            toCreate.addToJob(toEdit);
            Main.database.updateItemInfo(toCreate);
            Main.database.updateExistingJob(toEdit);
            refresh();
         }
         // create new
         else
         {
            // we must create the job and retrieve the dbrefnum in the database before
            // continuing in adding the sample to a job
            int dbrefnum = Main.database.insertNewJob(toEdit);
            toEdit.setDbrefnum(dbrefnum);
            
            toCreate.addToJob(toEdit);
            Main.database.updateItemInfo(toCreate);
            Main.database.updateExistingJob(toEdit);
            refresh();
         }
      }
   }
   
   @ FXML
   /**
    * Called when Multiple Item Button is pressed - checks out items or adds all to DB
    */
   public void checkOutItems() throws SQLException, FileNotFoundException, ClassNotFoundException, IOException
   {
      Main.database.reconnect();
      lastUpdate = System.currentTimeMillis();
      
      ArrayList<String> scans = new ArrayList<>();
      ArrayList<String> invalid = new ArrayList<>();
      ArrayList<String> unavalible = new ArrayList<>();
      
      do
      {
         scans.add(ScanningHandler.scan());
      } while (!scans.get(scans.size()-1).equals(""));
      scans.remove(scans.size()-1);
      
      for (int i = 0; i < scans.size(); i++)
      {
         String id = scans.get(i);
         if (!Main.database.checkEquipmentExists(id))
         {
            invalid.add(id);
            scans.remove(id);
            i--;
         }
         if (!Main.database.checkEquipmentCalibration(id))
         {
            unavalible.add(id);
            scans.remove(id);
            i--;
         }
         if (!Main.database.checkEquipmentBroken(id))
         {
            unavalible.add(id);
            scans.remove(id);
            i--;
         }
      }
      if (invalid.size() > 0)
      {
         String s = "";
         for (String id : invalid)
            s += id +"\n";
         WindowHandler.displayAlert("Invalid Scans","The following items were not recognized"
               + " by the database, they will be discarded.",s);
         WindowHandler.displayAlert("Invalid Scans", "Some of your scans are invalid..."
               , "One or more of the items scanned was not recognized by the database."
               + " If these are new items please use the single scan button to place them"
               + " in the database.");
      }
      
      if (unavalible.size() > 0)
      {
         String s = "";
         for (String id : unavalible)
            s += id +"\n";
         WindowHandler.displayAlert("Error","The following items were not included"
               + " in your job, they will be discarded.",s);
         WindowHandler.displayAlert("Invalid Scans", "Some of your scans are invalid..."
               , "One or more of the items scanned is currently broken, or in need of"
               + " calibration. If this is not the case, please scan these items in"
               + " seperately and read the directions on the messages displayed.");
      }
      
      if (scans.size() < 1)
         return;
      
      String[] options = {"Create New Job","Add to Existing"};
      int result = WindowHandler.displayConfirmDialog("Would you like to create"
            + " a new job or add these items to a preexisting job?",2,options);
      Job toEdit = new Job();
      
      if (result != 1 && result != 2)
      {
         WindowHandler.displayAlert("Failure", "Addition not successful"
            , "The items were NOT successfully added to a job, please try again.");
         return;
      }
      
      if (result == 2)
      {
         toEdit = chooseExistingJob();
         if (toEdit.isReady())
         {
            for (String id : scans)
            {
               Equipment toAdd = new Equipment(Main.database.getItemInfo(id));
               toAdd.addToJob(toEdit);
               Main.database.updateItemInfo(toAdd);
               Main.database.updateExistingJob(toEdit);
               refresh();
            }
         }
         else
         {
            WindowHandler.displayAlert("Failure", "Addition not successful"
                  , "The items were NOT successfully added to a job, please try again.");
         }
      }
      
      else
      {
         toEdit = chooseNewJob();
         
         if (!toEdit.isReady())
         {
            WindowHandler.displayAlert("Failure", "Addition not successful"
                  , "The item was NOT successfully added to a job, please try again");
         }
         
         // update existing
         else if (Main.database.checkJobExists(toEdit.getProjectnumber()))
         {
            for (String id : scans)
            {
               Equipment toAdd = new Equipment(Main.database.getItemInfo(id));
               toAdd.addToJob(toEdit);
               Main.database.updateItemInfo(toAdd);
               Main.database.updateExistingJob(toEdit);
               refresh();
            }
         }
         // create new
         else
         {
            for (String id : scans)
            {
               Equipment toAdd = new Equipment(Main.database.getItemInfo(id));
               toAdd.addToJob(toEdit);
               Main.database.updateItemInfo(toAdd);
               Main.database.updateExistingJob(toEdit);
               refresh();
            }
         }
      }
   }
   
   /**
    * updates the list of checked out items and selects the first item if possible
    */
   private void updateCheckedOut()
   {
      checkedOutList.getItems().clear();
      ArrayList<String> checkedOut = new ArrayList<String>();
      
      try
      {
         checkedOut = Main.database.getCheckedOutItems();
      } catch (SQLException e)
      {
         e.printStackTrace();
      }
      
      // each item is a concatenation of the id (barcode) and name so that
      // i can use that info later (it's possible for there to be items of the same
      // name but not same code
      for (int i = 0; i <checkedOut.size(); i++)
      {
         String s = checkedOut.get(i++);
         s += " " + checkedOut.get(i+1);
         checkedOutList.getItems().add(s + " " + checkedOut.get(i++));
      }
      
      checkedOutList.getSelectionModel().select(0);
      
      try
      {
            setCurrInfo();
      } catch (SQLException e)
      {
         e.printStackTrace();
      }
   }
   
   /**
    * displays equipment in need of calibration
    */
   private void updateCalibrations()
   {
      ArrayList<String> calibrations = new ArrayList<>();
      // find items with calibration date soon or passed
      try
      {
      calibrations = Main.database.getItemsToCalibrate();
      } catch (SQLException e)
      {
         e.printStackTrace();
      }
      // add these items to job 2 (need calibration job)
      //Main.database.updateCalibrationJob(calibrations);
      
      // display these items on the GUI
      calibrationList.getItems().clear();
      for (String s : calibrations)
         calibrationList.getItems().add(s);
   }
   
   /**
    * 
    */
   private void updatePastDue()
   {
      ArrayList<String> allPastDue = new ArrayList<>();
      try
      {
         allPastDue = Main.database.getAllCheckedOutPastDue();
      } catch (SQLException e)
      {
         e.printStackTrace();
      }
      
      pastDueAll.getItems().clear();
      for (String s : allPastDue)
         pastDueAll.getItems().add(s);
   }
   
   /**
    * updates the list of all checked out items, personal items 
    */
   private void updateCheckedOutAll()
   {
      ArrayList<String> allCheckedOut = new ArrayList<>();
      try
      {
         allCheckedOut = Main.database.getAllCheckedOut();
      } catch (SQLException e)
      {
         e.printStackTrace();
      }
      
      checkedOutAll.getItems().clear();
      for (String s : allCheckedOut)
         checkedOutAll.getItems().add(s);
   }
   
   /**
    * sets info on startup (workaround because it stays as placeholder text for some reason)
    * @throws SQLException 
    */
   private void setCurrInfo() throws SQLException
   {
      String name = checkedOutList.getSelectionModel().getSelectedItem();
      if (name == null || name.equals("") || name.equals(null))
      {
         currUser.setText("");
         currID.setText("");
         currManu.setText("");
         currName.setText("");
         currDate.setText("");
         currNum.setText("");
         currAct.setText("");
         currDept.setText("");
         currComments.setText("");
         return;
      }
      int idx = name.indexOf(' ');
      String id = name.substring(0, idx);
      name = name.substring(idx+1,name.length());
      idx = name.indexOf(' ');
      name = name.substring(idx+1,name.length());
      
      Equipment currItem = new Equipment(Main.database.getItemInfo(id));
      currUser.setText(Main.database.getFullname(currItem.getCurrentuser()));
      currID.setText(currItem.getId());
      currManu.setText(currItem.getManufacturer());
      currName.setText(currItem.getName());
      currDate.setText(currItem.getCheckedout().toString());
      Job currJob = new Job(Main.database.getJobInfo(currItem.getDbrefnum()));
      if (currJob.getDbrefnum() <= 10)
      {
         currNum.setText("none");
         currAct.setText("none");
         currDept.setText("none");
      }
      else
      {
         currNum.setText(currJob.getProjectnumber());
         currAct.setText(currJob.getActivity());
         currDept.setText(currJob.getDept_client());
      }
      currComments.setText(currItem.getComments());
   }
   
   
   /**
    * handles the scanning of an item throughout the application, returns the id
    */
   private String scanItem()
   {
      return ScanningHandler.scan();
   }
   
   /**
    * Calls the GUI for the creation of new equipment
    * @param id
    * @return
    * @throws SQLException 
    */
   private Equipment createNewItem(Equipment toCreate) throws SQLException
   {
      toCreate = WindowHandler.displayNewEquipmentPane(toCreate);
      if (!toCreate.isReady())
      {
         WindowHandler.displayAlert("Error", "Cannot Create Item", "Some information was missing"
               + "from this item and the item will not be created, please try again.");
         return toCreate;
      }
      Main.database.insertNewItem(toCreate);
      
      return toCreate;
   }
   
   /**
    * Automatically checks in an item to the admin and creates an audit trail entry
    * @param id barcode id of the item
    * @throws SQLException
    */
   private void autoCheckIn(String id) throws SQLException
   {
      Main.database.updateItemCheckIn(id);
      
      WindowHandler.displayAlert("Confirmation", "Success"
            , "The seleced item has been checked back in.");
   }
   
   /**
    * allows the user to create a new job for their items
    * @throws SQLException 
    */
   private Job chooseNewJob() throws SQLException
   {
      Job toCreate = WindowHandler.displayNewJobPane(new Job());
      
      String[] buttonNames = {"Edit Info","Change Number"};
      while (Main.database.checkJobExists(toCreate.getProjectnumber()))
      {
         int choice = WindowHandler.displayConfirmDialog("This job already exists in the"
               + "database, would you like to edit the preexisting job or "
               + "create a new job using the same information and a different"
               + "job number?", 2, buttonNames);
         if (choice == 1)
         {
            toCreate = WindowHandler.displayNewJobPane(toCreate);
         }
         if (choice == 2)
         {
            toCreate = WindowHandler.displayNewJobPane(toCreate);
         }
         else
         {
            toCreate.setReady(false);
            return toCreate;
         }
      }
      return toCreate;
   }
   
   private Job chooseExistingJob() throws SQLException
   {
      int dbrefnum = WindowHandler.displayJobChooser();
      Job toEdit = new Job();
      if (dbrefnum == -1)
      {
         WindowHandler.displayAlert("Failure", "Job does not exist",
               "The job you enetered does not exist in the database, please try again.");
         toEdit.setReady(false);
         return toEdit;
      }
      toEdit = new Job(Main.database.getJobInfo(dbrefnum));
      // confirm information
      toEdit = WindowHandler.displayNewJobPane(toEdit);
      return toEdit;
   }
   
}



