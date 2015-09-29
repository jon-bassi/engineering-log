package io.github.jon_bassi;

import io.github.jon_bassi.db.objects.Equipment;
import io.github.jon_bassi.db.objects.Job;
import io.github.jon_bassi.view.ExceptionHandler;
import io.github.jon_bassi.view.ScanningHandler;
import io.github.jon_bassi.view.WindowHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
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
import javafx.stage.FileChooser;

/**
 * TODO : -prepare for job type selection (items are a part of a type, types need a certain
 *         amount of items
 *        -look at Check Out Item method for correct algorithm
 *        -add check in job (checks in all items in a job??)
 *        -strings file??
 *        -add list to check out that displays items
 *        -add edit selected back to lite version?
 *        -Ubavalible Items tab - all checked out, all past due, al broken
 *        -Fix audit trail dupes 2 mos storage, backup 1ce a month
 *        -make checked in and checked out more visible
 *        -calibration list shows both needed calibrations and all items that have dates
 *         in order of when they are to be calibrated
 *        -lookinto coloring text
 * @author jon-bassi
 *
 */
public class deskAppController implements Initializable
{
   private long lastUpdate;
   private final long TIME_OUT = 900000;
   
   @ FXML
   private ListView<String> checkedOutList;
   @ FXML
   private ListView<String> checkedInList;
   @ FXML
   private ListView<String> calibrationList;
   @ FXML
   private ListView<String> calibrationOutList;
   @ FXML
   private ListView<String> checkedOutAll;
   @ FXML
   private ListView<String> pastDueAll;
   @ FXML
   private ListView<String> searchResults;
   @ FXML
   private ListView<String> jobListJobs;
   @ FXML
   private ListView<String> jobListItems;
   @ FXML
   private ListView<String> auditTrail;
   
   // Home tab
   @ FXML
   private TextField filterField;
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
   private Label currRetDate;
   @ FXML
   private Label currNum;
   @ FXML
   private Label currDept;
   @ FXML
   private Label currAct;
   @ FXML
   private Label currNextCal;
   @ FXML
   private TextArea currComments;
   
   // Calibration tab
   @ FXML
   private Label calCurrUser;
   @ FXML
   private Label calCurrID;
   @ FXML
   private Label calCurrManu;
   @ FXML
   private Label calCurrName;
   @ FXML
   private Label calCurrDate;
   @ FXML
   private Label calRetDate;
   @ FXML
   private Label calCurrNum;
   @ FXML
   private Label calCurrDept;
   @ FXML
   private Label calCurrAct;
   @ FXML
   private Label calNextCal;
   @ FXML
   private TextArea calComments;
   
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
   private Label pastDueRetDate;
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
   
   // JobList tab
   @ FXML
   private Label jobListName;
   @ FXML
   private Label jobListNum;
   @ FXML
   private Label jobListDept;
   @ FXML
   private Label jobListAct;
   @ FXML
   private Label jobListStartDate;
   @ FXML
   private Label jobListLocale;
   @ FXML
   private TextArea jobListComments;
   @ FXML
   private Label jobListID;
   @ FXML
   private Label jobListMan;
   @ FXML
   private Label jobListItemName;
   @ FXML
   private Label jobListCheckedOut;
   @ FXML
   private Label jobListRetDate;
   @ FXML
   private Label jobListUser;
   @ FXML
   private TextArea jobListItemComments;
   
   // Audit Trail tab
   @ FXML
   private TextField auditTrailFilterText;
   
   @ Override
   /**
    * sets up GUI home page when opened, first method called, ignore args
    * @param arg0
    * @param arg1
    */
   public void initialize(URL arg0, ResourceBundle arg1)
   {
      updateCheckedOut();
      updateCheckedIn();
      
      refreshJobList();
      
      //refreshAuditTrail();
      
      // new thread for loading things not on the front page - won't stall the program on load
      // also include checks and updates here, updates can be tested once a week with current/next
      // update time stored in another database table
      
      Thread t = new Thread(new Runnable(){
         @Override
         public void run()
         {
            // I know this throws an illegal state exception but Platform.runLater won't
            // allow the rest of the application to load, therefore a new thread is the only
            // way, even if it throws an IllegalStateException
            try {
               // update other pages
               updateCalibrations();
               //updateCheckedOutAll();
               updatePastDue();
               refreshAuditTrail();
            } catch (Exception e) {
               System.out.println("NOT ON FX THREAD - ignore");
            }
            // update other pages
            updateCalibrations();
            //updateCheckedOutAll();
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
      });
      t.start();
   }
   @ FXML
   /**
    * changes the information in the top-right info panel based on
    * selection in either list
    */
   public void viewCurrInfo()
   {
      // get the information from db
      Main.database.reconnect();
      lastUpdate = System.currentTimeMillis();
      int list = 1;
      
      ArrayList<ListView<String>> lists = new ArrayList<>();
      lists.add(checkedOutList);
      lists.add(checkedInList);
      
      // check which frame is selected
      if (checkedOutList.isFocused())
         list = 0;
      
      String name = lists.get(list).getSelectionModel().getSelectedItem();
      if (name == null || name.equals("########## Checked Out on Jobs ##########") || name.equals("########## Personal Items ##########"))
      {
         currUser.setText("");
         currID.setText("");
         currManu.setText("");
         currName.setText("");
         currDate.setText("");
         currRetDate.setText("");
         currNum.setText("");
         currAct.setText("");
         currDept.setText("");
         currNextCal.setText("");
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
      if (currItem.getCheckedout().getTime() == 0)
         currDate.setText("Checked In");
      else
         currDate.setText(currItem.getCheckedout().toString());
      Job currJob = new Job(Main.database.getJobInfo(currItem.getDbrefnum()));
      if (currJob.getDbrefnum() <= 10 && currJob.getDbrefnum() != 4)
      {
         currNum.setText("none");
         currAct.setText("none");
         currDept.setText("none");
      }
      else if (currJob.getDbrefnum() == 4)
      {
         currNum.setText("Personal Item");
         currAct.setText("none");
         currDept.setText("none");
      }
      else
      {
         currNum.setText(currJob.getProjectnumber());
         currAct.setText(currJob.getActivity());
         currDept.setText(currJob.getDept_client());
      }
      if (currItem.getEstimatedreturn().getTime() <= 0)
         currRetDate.setText("none");
      else
         currRetDate.setText(currItem.getEstimatedreturn().toString());
      if (currItem.getCalibrationinterval() == 0)
         currNextCal.setText("none");
      else
         currNextCal.setText(currItem.getNextcalibrationdate().toString());
      currComments.setText(currItem.getComments());
   }
   
   /**
    * Search algorithm for the equipment search tab, pretty basic all terms within the
    * text field use OR functionality while searching the database, not AND
    */
   public void equipmentSearch()
   {
      Main.database.reconnect();
      lastUpdate = System.currentTimeMillis();
      
      String searchString = searchField.getText();
      if (searchString.contains("-"))
      {
         searchString = searchString.substring(searchString.lastIndexOf('-') + 1, searchString.length());
      }
      searchString = searchString.toLowerCase();
      
      ArrayList<String> itemResults = Main.database.search(searchString);
      
      searchResults.getItems().clear();
      
      for (String s : itemResults)
         searchResults.getItems().add(s);
      searchResults.getSelectionModel().select(1);
      setSearchedInfo();
   }
   
   @ FXML
   /**
    * sets the info for the selected items on the checked out (all) page
    */
   public void setCalibrationInfo()
   {
      Main.database.reconnect();
      lastUpdate = System.currentTimeMillis();
      
      int list = 1;
      
      ArrayList<ListView<String>> lists = new ArrayList<>();
      lists.add(calibrationList);
      lists.add(calibrationOutList);
      
      // check which frame is selected
      if (calibrationList.isFocused())
         list = 0;
      
      String selected = lists.get(list).getSelectionModel().getSelectedItem();
      if (selected == null || selected.equals(""))
      {
         calCurrUser.setText("");
         calCurrID.setText("");
         calCurrManu.setText("");
         calCurrName.setText("");
         calCurrDate.setText("");
         calRetDate.setText("");
         calCurrNum.setText("");
         calCurrAct.setText("");
         calCurrDept.setText("");
         calNextCal.setText("");
         calComments.setText("");
         return;
      }
      
      int idx = selected.indexOf(' ');
      String id = selected.substring(0, idx);
      selected = selected.substring(idx+1,selected.length());
      idx = selected.indexOf(' ');
      selected = selected.substring(idx+1,selected.length());
      
      Equipment currItem = new Equipment(Main.database.getItemInfo(id));
      
      calCurrUser.setText(Main.database.getFullname(currItem.getCurrentuser()));
      calCurrID.setText(currItem.getId());
      calCurrManu.setText(currItem.getManufacturer());
      calCurrName.setText(currItem.getName());
      if (currItem.getCheckedout().getTime() <= 0)
         calCurrDate.setText("Checked In");
      else
         calCurrDate.setText(currItem.getCheckedout().toString());
      if (currItem.getEstimatedreturn().getTime() <= 0)
         calRetDate.setText("none");
      else
         calRetDate.setText(currItem.getEstimatedreturn().toString());
      Job currJob = new Job(Main.database.getJobInfo(currItem.getDbrefnum()));
      if (currJob.getDbrefnum() <= 10 && currJob.getDbrefnum() != 4)
      {
         calCurrNum.setText("none");
         calCurrAct.setText("none");
         calCurrDept.setText("none");
      }
      else if (currJob.getDbrefnum() == 4)
      {
         calCurrNum.setText("Personal Item");
         calCurrAct.setText("none");
         calCurrDept.setText("none");
      }
      else
      {
         calCurrNum.setText(currJob.getProjectnumber());
         calCurrAct.setText(currJob.getActivity());
         calCurrDept.setText(currJob.getDept_client());
      }
      calNextCal.setText(currItem.getNextcalibrationdate().toString());
      calComments.setText(currItem.getComments());
   }
   
   /**
    * sets the info for the selected items on the past due page
    */
   public void setPastDueInfo()
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
         pastDueRetDate.setText("");
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
      if (currItem.getEstimatedreturn().getTime() <= 0)
         pastDueRetDate.setText("none");
      else
         pastDueRetDate.setText(currItem.getEstimatedreturn().toString());
      Job currJob = new Job(Main.database.getJobInfo(currItem.getDbrefnum()));
      if (currJob.getDbrefnum() <= 10 && currJob.getDbrefnum() != 4)
      {
         pastDueCurrNum.setText("none");
         pastDueCurrAct.setText("none");
         pastDueCurrDept.setText("none");
      }
      else if (currJob.getDbrefnum() == 4)
      {
         pastDueCurrNum.setText("Personal Item");
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
    */
   public void setSearchedInfo()
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
      if (currJob.getDbrefnum() <= 10 && currJob.getDbrefnum() != 4)
      {
         searchNum.setText("none");
         searchAct.setText("none");
         searchDept.setText("none");
      }
      else if (currJob.getDbrefnum() == 4)
      {
         searchNum.setText("Personal Item");
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
    */
   @ FXML
   public void logout() 
   {
      try
      {
         Main.database.disconnect();
         Scanner file = new Scanner(new File("paths.dat"));
         String cmd = file.nextLine();
         file.close();
         @SuppressWarnings("unused")
         Process p = Runtime.getRuntime().exec(cmd);
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
   public void refresh()
   {
      Main.database.reconnect();
      lastUpdate = System.currentTimeMillis();
      updateCheckedOut();
      updateCheckedIn();
      updateCalibrations();
      //updateCheckedOutAll();
      updatePastDue();
      refreshJobList();
   }
   
   @ FXML
   /**
    * Allows for the editing of information for a specific item
    */
   public void editItem()
   {
      Main.database.reconnect();
      lastUpdate = System.currentTimeMillis();
      String id = scanItem(null);
      if (id.equals("") || !Main.database.checkEquipmentExists(id))
      {
         WindowHandler.displayMatchFailure();
         return;
      }
      
      editItem(id);
   }
   
   
   private void editItem(String id)
   {
      Main.database.reconnect();
      lastUpdate = System.currentTimeMillis();
      
      if (id.equals("") || !Main.database.checkEquipmentExists(id))
      {
         WindowHandler.displayMatchFailure();
         return;
      }
      
      Equipment toEdit = new Equipment(Main.database.getItemInfo(id));
      
      String[] options = {"Change Status","Edit Info"};
      int result = WindowHandler.displayConfirmDialog("If you wish to set this item "
            + "as broken, out for calibration, a personal item, please press Change Status,"
            + " otherwise Edit Info."
            , 2, options);
      if (result != 1 && result != 2)
      {
         WindowHandler.displayAlert("Failure", "Item information was not changed"
               , "^");
         return;
      }
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
      WindowHandler.displayAlert("Confirmation", "Success"
            , "The seleced item's information was updated.");
   }
   
   
   @ FXML
   /**
    * Allows for the editing of information for a specific job
    */
   public void editJob()
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
      WindowHandler.displayAlert("Confirmation", "Success"
            , "The seleced job's information was updated.");
   }
   
   @ FXML
   /**
    * opens barcode creation page in ie
    */
   public void createBarcode()
   {
      String[] options = {"Ok"};
      int result = WindowHandler.displayConfirmDialog("Please read these instructions"
            + " before you continue.\n1. The barcode should be made up of a combination of an acronym"
            + " of the manufacturer name and equiment name, followed by the S/N of the equipment.\n"
            + "2. The barcode shall not be greater than 11 characters.\n3. The barcode shall"
            + " only contain alpha-numeric characters (0-9,aA-zZ).\n4. If the equipment does"
            + " not have a S/N use an acronym for the manufacturer+name followed by 0001, incrementing"
            + " the number for each item of that type.\n5. The barcode shall be of the"
            + " format: \"Code 128\" X-dimension: \"2\" and contain human readable text.\n\n"
            + "Place the barcode into the excel spreadsheet labeled barcodes and format so that"
            + " they fit onto the barcode Avery Template 5160 labels.", 1, options);
      if (result == 0)
      {
         return;
      }
      try
      {
         Scanner file = new Scanner(new File("paths.dat"));
         file.nextLine();
         String cmd = file.nextLine();
         file.close();
         
         if (!System.getProperty("os.name").contains("Windows"))
         {
            return;
         }
         @SuppressWarnings("unused")
         Process p = Runtime.getRuntime().exec(cmd);
      } catch (FileNotFoundException e)
      {
         ExceptionHandler.displayException(e);
      } catch (IOException e)
      {
         ExceptionHandler.displayException(e);
      }
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
    * filters the checked in list on the front page when the filter button is pressed in
    * enter is pressed in the text field
    */
   public void filterCheckedIn()
   {
      Main.database.reconnect();
      lastUpdate = System.currentTimeMillis();
      
      String filterText = filterField.getText();
      
      ArrayList<String> filteredItems = Main.database.getFilteredCheckedIn(filterText);
      
      checkedInList.getItems().clear();
      checkedInList.getItems().addAll(filteredItems);
      
   }
   
   
   
   
   @ FXML
   /**
    * checks in the selected item depending on the equipment's job and status
    */
   public void checkInSelected()
   {
      Main.database.reconnect();
      lastUpdate = System.currentTimeMillis();
      
      String id = currID.getText();
      
      // not recognized
      if (!Main.database.checkEquipmentExists(id))
      {
         WindowHandler.itemNotRecognized(id);
         return;
      }
      
      Equipment toCheckIn = new Equipment(Main.database.getItemInfo(id));
      
      
      
      switch (toCheckIn.getDbrefnum())
      {
         /**
          * Since this list is items that are checked out by the current user, and items
          * are attributed to the admin when checked out this should only display for admin
          * 
          * AUTO CHECK IN SHOULD ONLY HAPPEN IF THE ENTIRE PROCESS IS COMPLETED!!!!
          */
         case 0 : System.out.println("this should only be seen by admin");
            WindowHandler.displayAlert("Notification", toCheckIn.toString(),
                  "Test error, no one should ever see this");
            break;
            
         /**
          * Out of Service
          */
         case 1 : String[] a1 = {"Check In"};
            int choice1 = WindowHandler.displayConfirmDialog("This item is currently marked as "
                  + "out of service, if it is fixed please press Check In to reinstate"
               + " reinstate this item, check out and reinstate, or niether?",1,a1);
            
            if (choice1 == 1)
            {
               autoCheckIn(id);
               refresh();
            }
            else
            {
               WindowHandler.displayAlert("Confirmation", "Failure"
                     , "The seleced item has NOT been checked in.");
            }
            break;
            
         /**
          * Out for Calibration - shouldn't be called
          */
         case 2 : 
               break;
         /**
          * Lab Items, unused atm
          */
         case 3 : 
               break;
         /**
          * Personal Items, only allows checking the item in 
          */
         case 4 : 
               String[] a3 = {"Check In"};
               int choice3 = WindowHandler.displayConfirmDialog("This is a personal item,"
                     + " do you really want to check it in?",1,a3);
               if (choice3 == 1)
               {
                  autoCheckIn(id);
                  refresh();
               }
               else
               {
                  WindowHandler.displayAlert("Confirmation", "Failure"
                        , "The seleced item has NOT been checked in.");
               }
               break;
         /**
          * Default - this option is available to everyone
          */
         default : 
            // checked out - check in
            String[] a4 = {"Check In"};
            int choice4 = WindowHandler.displayConfirmDialog("Do you wish to check in "
                  + currID.getText() + " " + currManu.getText() + " " + currName.getText()
                  ,1,a4);
            
            if (choice4 == 1)
            {
               autoCheckIn(id);
               refresh();
            }
            else
            {
               WindowHandler.displayAlert("Confirmation", "Failure"
                     , "The seleced item has NOT been checked in.");
            }
            break;
      }
   }
   
   @ FXML
   /**
    * checks in the item on the calibration page back into the database
    */
   public void checkInCalSelected()
   {
      String id = calCurrID.getText();
      Equipment toCheckIn = new Equipment(Main.database.getItemInfo(id));
      
      if (toCheckIn.getDbrefnum() != 2)
      {
         WindowHandler.displayAlert("Error", "Cannot check in", "This item is currently not"
               + " checked out for calibration or currently on a job, please use this button"
               + " only for items which were calibrated off-site.");
         return;
      }
      
      String[] options = {"Check In"};
      int choice = WindowHandler.displayConfirmDialog("Would you like to check in "
            + calCurrID.getText() + " " + calCurrManu.getText() + " " + calCurrName.getText()
            + " back into the database?",1,options);
      if (choice == 1)
      {
         Main.database.updateItemCalibrationDate(toCheckIn);
         Main.database.updateItemCheckIn(id);
         Main.database.insertAudit(id, 2, "admin", Main.user);
         WindowHandler.displayAlert("Confirmation", "Success", "Item was successfully"
               + " added back to the database.");
         refresh();
      }
      else
      {
         WindowHandler.displayAlert("Confirmation", "Failure"
               , "The seleced item has NOT been checked in.");
      }
   }
   
   
   @ FXML
   /**
    * checks out the selected item on the front page
    */
   public void checkOutSelected()
   {
      Main.database.reconnect();
      lastUpdate = System.currentTimeMillis();
      
      String id = currID.getText();
      
      id = scanItem(id);
      
      if (!Main.database.checkEquipmentExists(id))
      {
         WindowHandler.displayAlert("Failure", "Barcode not recognized"
               , "The item was NOT successfully scanned, please try again.");
         return;
      }
      
      Equipment toCheckOut = new Equipment(Main.database.getItemInfo(id));
      if (toCheckOut.getDbrefnum() > 10 || toCheckOut.getDbrefnum() == 4)
      {
         WindowHandler.displayAlert("Error", "You already have this item checked out..."
               , "This item is already checked out, cannot complete request.");
         return;
      }
      else if (toCheckOut.getDbrefnum() == 4)
      {
         String[] options = {"Continue"};
         int choice = WindowHandler.displayConfirmDialog("You have selected one of your personal items"
               + " for check out, if this was in error, please cancel, otherwise press continue."
               , 1, options);
         if (choice == 1)
         {
            checkOutItem(id);
         }
         return;
      }
      else
      {
         checkOutItem(id);
      }
   }
   
   @ FXML
   /**
    * brings up edit item window for item from list on front page
    */
   public void editSelected()
   {
      Main.database.reconnect();
      lastUpdate = System.currentTimeMillis();
      
      // get the data from the selected item - this shouldn't cause any errors and is
      // faster than depending on which list is focused
      String id = currID.getText();
      
      id = scanItem(id);
      
      editItem(id);
   }
   
   @ FXML
   /**
    * Called when Single Item Button is pressed - checks out item or adds to DB
    */
   public void checkOutItem()
   {
      Main.database.reconnect();
      lastUpdate = System.currentTimeMillis();
      
      Equipment toCreate = new Equipment();
      String id = ScanningHandler.scan(null);
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
      checkOutItem(id);
   }
   
   /**
    * Checks out a single item
    * @param id
    */
   private void checkOutItem(String id)
   {
      Main.database.reconnect();
      lastUpdate = System.currentTimeMillis();
      
      Equipment toCreate = new Equipment(Main.database.getItemInfo(id));
      
      // checking if the item needs calibration or is broken
      if (Main.database.checkEquipmentCalibration(id))
      {
         String[] options = {"Continue"};
         int result = WindowHandler.displayConfirmDialog("NOTE: This item needs to be calibrated before"
               + " being used in the field. Please continue to set the calibration up", 1, options);
         if (result == 0)
            return;
         else
         {
            String[] cOptions = {"Calibrate Now","Send for Calibration"};
            result = WindowHandler.displayConfirmDialog("If this equipment can be easily calibrated inhouse"
                  + " press Calibrate Now, if it needs to be sent out, press Send for Calibration."
                  , 2,cOptions);
            if (result == 0)
               return;
            else if (result == 1)
            {
               Main.database.updateItemCalibrationDate(toCreate);
               Main.database.insertAudit(id, 2, "admin", Main.user);
               WindowHandler.displayAlert("Confirmation", "Success", "Item was successfully"
                     + " calibrated.");
               refresh();
            }
            else
            {
               Main.database.updateItemToCalibration(id);
               Main.database.insertAudit(id, 2, Main.user, "admin");
               WindowHandler.displayAlert("Confirmation", "Success", "Item was successfully"
                     + " marked as out for calibration, please check in from the calibration"
                     + " tab when it returns.");
               return;
            }
            
            
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
            + " a new job, add this item to a preexisting job, or add as a personal item?"
            ,3,options);
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
            WindowHandler.displayAlert("Confirmation", "Success"
                  , "The seleced item has been checked out.");
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
            WindowHandler.displayAlert("Confirmation", "Success"
                  , "The seleced item has been checked out.");
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
            WindowHandler.displayAlert("Confirmation", "Success"
                  , "The seleced item has been checked out.");
         }
      }
   }
   
   @ FXML
   /**
    * Called when Multiple Item Button is pressed - checks out items or adds all to DB
    */
   public void checkOutItems()
   {
      Main.database.reconnect();
      lastUpdate = System.currentTimeMillis();
      
      ArrayList<String> scans = new ArrayList<>();
      ArrayList<String> invalid = new ArrayList<>();
      ArrayList<String> unavalible = new ArrayList<>();
      
      do
      {
         scans.add(ScanningHandler.scan(null));
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
         if (Main.database.checkEquipmentCalibration(id))
         {
            unavalible.add(id);
            scans.remove(id);
            i--;
         }
         if (Main.database.checkEquipmentBroken(id))
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
               + " calibration. Please scan these seperately.");
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
            WindowHandler.displayAlert("Confirmation", "Success"
                  , "The seleced items have been checked out.");
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
            WindowHandler.displayAlert("Confirmation", "Success"
                  , "The seleced items have been checked out.");
         }
         // create new
         else
         {
            int dbrefnum = Main.database.insertNewJob(toEdit);
            toEdit.setDbrefnum(dbrefnum);
            for (String id : scans)
            {
               Equipment toAdd = new Equipment(Main.database.getItemInfo(id));
               toAdd.addToJob(toEdit);
               Main.database.updateItemInfo(toAdd);
               Main.database.updateExistingJob(toEdit);
               refresh();
            }
            WindowHandler.displayAlert("Confirmation", "Success"
                  , "The seleced items have been checked out.");
         }
      }
   }
   
   @ FXML
   /**
    * adds an item to the database
    */
   public void addItem()
   {
      Main.database.reconnect();
      lastUpdate = System.currentTimeMillis();
      
      String id = scanItem(null);
      
      if (id.equals(""))
      {
         WindowHandler.displayAlert("Failure", "Barcode not recognized"
               , "The item was NOT successfully scanned, please try again.");
         return;
      }
      
      Equipment toCreate = new Equipment();
      toCreate.setId(id);
      
      if (Main.database.checkEquipmentExists(id))
      {
         WindowHandler.displayAlert("Failure", "Duplicate Item"
               , "A piece of equipment with this barcode already exists in the database"
               + ", please change the id and try again.");
         return;
      }
      
      toCreate = createNewItem(toCreate);
      refresh();
      WindowHandler.displayAlert("Confirmation", "Success"
            , "The item was added to the database.");
   }
   
   @ FXML
   /**
    * Refreshes the jobList page in the application, separated because this page may take
    * more time to refresh than other pages
    */
   public void refreshJobList()
   {
      Main.database.reconnect();
      lastUpdate = System.currentTimeMillis();
      // get all jobs which have items... search items for dbrefnum and put in hash set
      // for all dbrefnum, load jobs into list
      ArrayList<String> activeJobs = Main.database.getAllActiveJobs();
      jobListJobs.getItems().clear();
      jobListJobs.getItems().addAll(activeJobs);
      jobListJobs.getSelectionModel().select(0);
      selectJobJobList();
      
      jobListItems.getSelectionModel().select(0);
      selectItemJobList();
   }
   
   @ FXML
   /**
    * exports job info to CSV along with equipment in job
    */
   public void exportJob()
   {
      FileChooser chooser = new FileChooser();
      FileChooser.ExtensionFilter ext = new FileChooser.ExtensionFilter("CSV", "*.csv");
      chooser.getExtensionFilters().add(ext);
      File file = chooser.showSaveDialog(null);
      if (file == null)
      {
         WindowHandler.displayAlert("Failure", "File was not saved", "The data was not exported"
               + " please try again");
         return;
      }
      if (!file.getName().endsWith(".csv"))
      {
         file = new File(file.getAbsolutePath() + ".csv");
      }
      
      try
      {
         
         
         BufferedWriter writer = new BufferedWriter(new FileWriter(file));
         
         writer.write(jobListNum.getText() + " " + jobListName.getText() +"\nDepartment:," + jobListDept.getText()
               + "\nActivity:," + jobListAct.getText() + "\nDestination:," + jobListLocale.getText()
               + "\nEquipment,Manufacturer,Name,Dimensions,Weight,Value\n");
         ArrayList<Equipment> equipment = Main.database.getItemsForJob(Main.database.getJobDBrefnum(jobListNum.getText()));
         for (Equipment e : equipment)
         {
            writer.write(e.getId() + "," + e.getManufacturer() + "," + e.getName() + ","
                  + e.getDimensions() + "," + e.getWeight() + "lb,$" + String.format("%.2f", e.getValue()) + "\n");
         }
         
         writer.close();
         
      } catch (IOException e)
      {
         ExceptionHandler.displayException(e);
      }
      
      WindowHandler.displayAlert("Confirmation", "Success"
            , "The job information was exported successfully.");
   }
   
   @ FXML
   /**
    * triggers when selecting a job in the jobList tab
    */
   public void selectJobJobList()
   {
      Main.database.reconnect();
      lastUpdate = System.currentTimeMillis();
      
      if (jobListJobs.getSelectionModel().getSelectedItem() == null)
         return;
      
      // the code below deals with getting all the equipment on this job and displaying it
      String jobNum = jobListJobs.getSelectionModel().getSelectedItem();
      jobNum = jobNum.substring(0, jobNum.indexOf(' '));
      int dbrefnum = Main.database.getJobDBrefnum(jobNum);
      
      ArrayList<Equipment> equipment = Main.database.getItemsForJob(dbrefnum);
      
      TreeSet<String> itemNames = new TreeSet<>();
      for (Equipment e : equipment)
      {
         itemNames.add(e.getId() + " " + e.getManufacturer() + " " + e.getName());
      }
      jobListItems.getItems().clear();
      jobListItems.getItems().addAll(itemNames);
      
      Job job = new Job(Main.database.getJobInfo(dbrefnum));
      
      jobListName.setText(job.getProjname());
      jobListNum.setText(job.getProjectnumber());
      jobListDept.setText(job.getDept_client());
      jobListAct.setText(job.getActivity());
      jobListStartDate.setText(job.getDatetime().toString());
      jobListLocale.setText(job.getLocation());
      jobListComments.setText(job.getComments());
      
      jobListItems.getSelectionModel().select(0);
      selectItemJobList();
   }
   
   @FXML
   /**
    * triggers when selecting a piece of equiment in the jobList tab
    */
   public void selectItemJobList()
   {
      Main.database.reconnect();
      lastUpdate = System.currentTimeMillis();
      
      if (jobListItems.getSelectionModel().getSelectedItem() == null)
         return;
      
      String id = jobListItems.getSelectionModel().getSelectedItem();
      
      id = id.substring(0,id.indexOf(' '));
      
      Equipment item = new Equipment(Main.database.getItemInfo(id));
      
      jobListID.setText(item.getId());
      jobListMan.setText(item.getManufacturer());
      jobListItemName.setText(item.getName());
      jobListCheckedOut.setText(item.getCheckedout().toString());
      jobListRetDate.setText(item.getEstimatedreturn().toString());
      jobListUser.setText(Main.database.getFullname(item.getCurrentuser()));
      jobListItemComments.setText(item.getComments());
      
   }
   
   @ FXML
   public void filterAuditTrail()
   {
      auditTrail.getItems().clear();
      auditTrail.getItems().add("Loading...");
      
      ArrayList<String> audits = Main.database.getFilteredAudits(auditTrailFilterText.getText());
      
      auditTrail.getItems().clear();
      auditTrail.getItems().addAll(audits);
   }
   
   
   @ FXML
   /**
    * refreshes and displays information on recent events within the database
    */
   public void refreshAuditTrail()
   {
      auditTrail.getItems().clear();
      auditTrail.getItems().add("Loading...");
      
      ArrayList<String> audits = Main.database.getAudits();
      
      auditTrail.getItems().clear();
      try {
      for (String s : audits)
         auditTrail.getItems().add(s);
      } catch (Exception e)
      {
         System.out.println("NOT ON FX THREAD - ignore");
      }
   }
   
   /**
    * updates the list of checked out items and selects the first item if possible
    */
   private void updateCheckedOut()
   {
      checkedOutList.getItems().clear();
      ArrayList<String> checkedOut = new ArrayList<String>();
      checkedOut = Main.database.getCheckedOutItems();
      
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
      
      setCurrInfo();
   }
   
   /**
    * displays equipment in need of calibration
    */
   private void updateCalibrations()
   {
      ArrayList<String> calibrations = new ArrayList<>()
            ;
      // find items with calibration date soon or passed
      calibrations = Main.database.getItemsToCalibrate();
      
      // display these items on the GUI
      calibrationList.getItems().clear();
      for (String s : calibrations)
         calibrationList.getItems().add(s);
      
      
      ArrayList<Equipment> onCalibration = Main.database.getItemsForJob(2);
      
      calibrationOutList.getItems().clear();
      for (Equipment e : onCalibration)
         calibrationOutList.getItems().add(e.getId() + " " + e.getManufacturer() + " " + e.getName());
   }
   
   /**
    * updates the list of all past due equipment
    */
   private void updatePastDue()
   {
      ArrayList<String> allPastDue = new ArrayList<>();
      
      allPastDue = Main.database.getAllCheckedOutPastDue();
      
      pastDueAll.getItems().clear();
      for (String s : allPastDue)
         pastDueAll.getItems().add(s);
   }
   
   /**
    * updates the list of all checked in items on the front page
    */
   private void updateCheckedIn()
   {
      ArrayList<String> checkedIn = new ArrayList<>();
      
      checkedIn = Main.database.getAllCheckedIn();
      
      checkedInList.getItems().clear();
      for (String s : checkedIn)
         checkedInList.getItems().add(s);
   }
   
   /**
    * sets info on startup (workaround because it stays as placeholder text for some reason)
    */
   private void setCurrInfo()
   {
      String name = checkedOutList.getSelectionModel().getSelectedItem();
      if (name == null || name.equals("") || name.equals("########## Checked Out on Jobs ##########")
            || name.equals("########## Personal Items ##########"))
      {
         currUser.setText("");
         currID.setText("");
         currManu.setText("");
         currName.setText("");
         currDate.setText("");
         currRetDate.setText("");
         currNum.setText("");
         currAct.setText("");
         currDept.setText("");
         currNextCal.setText("");
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
      if (currJob.getDbrefnum() <= 10 && currJob.getDbrefnum() != 4)
      {
         currNum.setText("none");
         currAct.setText("none");
         currDept.setText("none");
      }
      else if (currJob.getDbrefnum() == 4)
      {
         currNum.setText("Personal Item");
         currAct.setText("none");
         currDept.setText("none");
      }
      else
      {
         currNum.setText(currJob.getProjectnumber());
         currAct.setText(currJob.getActivity());
         currDept.setText(currJob.getDept_client());
      }
      if (currItem.getEstimatedreturn().getTime() <= 0)
         currRetDate.setText("none");
      else
         currRetDate.setText(currItem.getEstimatedreturn().toString());
      if (currItem.getCalibrationinterval() == 0)
         currNextCal.setText("none");
      else
         currNextCal.setText(currItem.getNextcalibrationdate().toString());
      currComments.setText(currItem.getComments());
   }
   
   
   /**
    * handles the scanning of an item throughout the application, returns the id
    */
   private String scanItem(String id)
   {
      return ScanningHandler.scan(id);
   }
   
   /**
    * Calls the GUI for the creation of new equipment
    * @param id
    * @return
    */
   private Equipment createNewItem(Equipment toCreate)
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
    */
   private void autoCheckIn(String id)
   {
      Main.database.updateItemCheckIn(id);
      refresh();
      WindowHandler.displayAlert("Confirmation", "Success"
            , "The seleced item has been checked back in.");
   }
   
   /**
    * allows the user to create a new job for their items
    */
   private Job chooseNewJob()
   {
      Job toCreate = WindowHandler.displayNewJobPane(new Job());
      String[] buttonNames = {"Edit Info","Change Number"};
      while (Main.database.checkJobExists(toCreate.getProjectnumber()))
      {
         int choice = WindowHandler.displayConfirmDialog("This job already exists in the"
               + " database, would you like to edit the preexisting job or"
               + " create a new job using the same information and a different"
               + " job number?", 2, buttonNames);
         if (choice == 1)
         {
            toCreate.setDbrefnum(Main.database.getJobDBrefnum(toCreate.getProjectnumber()));
            toCreate.setEquipment(Main.database.getItemsForJob(toCreate.getDbrefnum()));
            toCreate = WindowHandler.displayNewJobPane(toCreate);
            return toCreate;
         }
         else if (choice == 2)
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
   
   private Job chooseExistingJob()
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



