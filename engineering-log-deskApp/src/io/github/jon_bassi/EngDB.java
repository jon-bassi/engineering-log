package io.github.jon_bassi;

import io.github.jon_bassi.db.objects.Equipment;
import io.github.jon_bassi.db.objects.Job;
import io.github.jon_bassi.view.ExceptionHandler;
import io.github.jon_bassi.view.WindowHandler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * 
 * @author jon-bassi
 *
 */
public class EngDB
{
   
   public Connection con = null;
   
   /**
    * Constructor - connects to DB on office server
    */
   public EngDB()
   {
      try {
         // load in user and pass
         BufferedReader input = new BufferedReader(new FileReader("data.dat"));
         String ip = input.readLine();
         String username = input.readLine();
         String pass = input.readLine();
         input.close();
         // location of jdbc.Driver in mysqlconnector.jar
         Class.forName("com.mysql.jdbc.Driver");
         
         // location of DB (local or otherwise)
         String url = ip;
         
         con = DriverManager.getConnection(url, username, pass);
         System.out.println("conn built");
         
      } catch (SQLException e)
      {
         WindowHandler.displayAlert("Error", "Could not connect", "Check your internet connection"
               + " and try again.");
      } catch (ClassNotFoundException e)
      {
         WindowHandler.displayAlert("Error", "Could not connect", "Check your internet connection"
               + " and try again.");
      } catch (IOException e)
      {
         WindowHandler.displayAlert("Error", "Could not connect", "Check your internet connection"
               + " and try again.");
      }
   }
   
   /**
    * Returns results from successful query
    * @param sql String of SQL statement
    * @return (see description)
    */
   private ResultSet runSql(String sql) 
   {
      
      Statement sta;
      try
      {
         sta = con.createStatement();
         return sta.executeQuery(sql);
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
      return null;
   }
   
   /**
    * Returns true if successful - use if no data is needed
    * @param sql String of SQL statement
    * @return (see description)
    */
   private boolean runSql2 (String sql) 
   {
      try
      {
         PreparedStatement stmt = con.prepareStatement(sql);
         return stmt.execute(sql);
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
      return false;
   }
   
   /**
    * @param projnum
    * @return boolean value whether a job with the same number is present in the db
    */
   public boolean checkJobExists(String projnum) 
   {
      try
      {
         String sql = "SELECT * FROM jobs WHERE projectnumber ='" + projnum + "'";
         ResultSet rs = runSql(sql);
         if (rs.next())
            return true;
         
         return false;
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
      return false;
   }
   
   /**
    * @param id
    * @return boolean value whether a piece of equipment with the same barcode is present in
    * the db
    */
   public boolean checkEquipmentExists(String id) 
   {
      try
      {
         String sql = "SELECT * FROM equipment WHERE id ='" + id + "'";
         ResultSet rs = runSql(sql);
         if (rs.next())
         {
            return true;
         }
         return false;
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
      return false;
   }
   
   /**
    * @param id barcode id of the equipment
    * @return whether or not the equipment needs to be calibrated
    */
   public boolean checkEquipmentCalibration(String id) 
   {
      try
      {
         String sql = "SELECT * FROM equipment WHERE id = '" + id
               + "' AND nextcalibrationdate <= '" + new Date(System.currentTimeMillis()) + "' "
               + "AND nextcalibrationdate > '2000-01-01 00:00:00'";
         ResultSet rs = runSql(sql);
         if (rs.next())
            return true;
            
         return false;
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
      return false;
   }
   
   /**
    * checks if a specific piece of equipment is broken
    * @param id
    * @return
    */
   public boolean checkEquipmentBroken(String id) 
   {
      try
      {
         String sql = "SELECT * FROM equipment WHERE id = '" + id
               + "' AND dbrefnum = '1'";
         ResultSet rs = runSql(sql);
         if (rs.next())
            return true;
            
         return false;
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
      return false;
   }
   
   /**
    * returns a list of all users - may be a 2D list so that all info from user table will be returned
    * @return ArrayList of all users
    */
   public ArrayList<String> getAllUsers() 
   {
      try
      {
         String sql = "SELECT * FROM users WHERE 1";
         ArrayList<String> users = new ArrayList<>();
         ResultSet rs = runSql(sql);
         
         while (rs.next())
         {
            String user = rs.getString(1);
            users.add(user);
         }
         
         return users;
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
      return null;
   }
   
   /**
    * returns a set of all searchable strings in the database
    * @return
    */
   public ArrayList<String> getAllStrings() 
   {
      try
      {
         String sql = "SELECT id,name,manufacturer FROM equipment WHERE 1";
         ResultSet rs = runSql(sql);
         
         ArrayList<String> strings = new ArrayList<>();
         while (rs.next())
         {
            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++)
            {
               strings.add(rs.getString(i+1));
            }
         }
         
         return strings;
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
      return null;
   }
   
   /**
    * returns the full name of a user based on their username
    * @param user
    * @return
    */
   public String getFullname(String user) 
   {
      try
      {
         String sql = "SELECT fullname FROM users WHERE username = '" + user + "'";
         
         ResultSet rs = runSql(sql);
         String name = "";
         if (rs.next())
         {
            name = rs.getString(1);
         }
         return name;
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
      return null;
   }
   
   /**
    * returns a list of all items checked out by current user
    * @return ArrayList of all users
    */
   public ArrayList<String> getCheckedOutItems() 
   {
      try
      {
         String sql = "SELECT id,name,manufacturer FROM equipment WHERE currentuser = '" + Main.user + "'"
               + " AND dbrefnum <> '4'";
         ArrayList<String> items = new ArrayList<>();
         
         items.add("Checked");
         items.add("Jobs");
         items.add("Out on");
         
         ResultSet rs = runSql(sql);
         
         while (rs.next())
         {
            int cols = rs.getMetaData().getColumnCount();
            for (int i = 0; i < cols; i++)
            {
               items.add(rs.getString(i+1));
            }
         }
         items.add("Personal");
         items.add("Items");
         items.add("");
         sql = "SELECT id,name,manufacturer FROM equipment WHERE currentuser = '" + Main.user + "'"
               + " AND dbrefnum = '4'";
         rs = runSql(sql);
         
         while (rs.next())
         {
            int cols = rs.getMetaData().getColumnCount();
            for (int i = 0; i < cols; i++)
            {
               items.add(rs.getString(i+1));
            }
         }
         
         return items;
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
      return null;
   }
   
   /**
    * returns a list of all items checked in to the database
    * @return
    */
   public ArrayList<String> getAllCheckedIn()
   {
      try
      {
         String sql = "SELECT id,name,manufacturer FROM equipment WHERE currentuser"
               + " = 'admin' AND dbrefnum = '0'";
         ArrayList<String> items = new ArrayList<>();
         ResultSet rs = runSql(sql);
         
         while (rs.next())
         {
            items.add(rs.getString(1) + " " + rs.getString(3) + " " + rs.getString(2));
         }
         
         return items;
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
      return null;
   }
   
   
   public ArrayList<String> getFilteredCheckedIn(String filterText)
   {
      try
      {
         String sql = "SELECT id,name,manufacturer FROM equipment WHERE currentuser"
               + " = 'admin' AND dbrefnum = '0' AND (name LIKE '%" + filterText + "%' OR "
               + " manufacturer LIKE '%" + filterText + "%' OR id LIKE '%" + filterText + "%')";
         ArrayList<String> items = new ArrayList<>();
         ResultSet rs = runSql(sql);
         
         while (rs.next())
         {
            items.add(rs.getString(1) + " " + rs.getString(3) + " " + rs.getString(2));
         }
         
         return items;
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
      return null;
   }
   
   /**
    * returns a list of all items checked out minus personal items
    * @return ArrayList of all users
    */
   public ArrayList<String> getAllCheckedOut() 
   {
      try
      {
         String sql = "SELECT id,name,manufacturer FROM equipment WHERE currentuser"
               + " <> 'admin' AND dbrefnum <> '4'";
         ArrayList<String> items = new ArrayList<>();
         ResultSet rs = runSql(sql);
         
         while (rs.next())
         {
            items.add(rs.getString(1) + " " + rs.getString(3) + " " + rs.getString(2));
         }
         
         return items;
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
      return null;
   }
   
   /**
    * returns a list of all items checked out longer than they were supposed to be
    * @return
    */
   public ArrayList<String> getAllCheckedOutPastDue() 
   {
      try
      {
         String sql = "SELECT id,name,manufacturer FROM equipment WHERE estimatedreturn"
               + " <= '" + new Date(System.currentTimeMillis()) + "' AND estimatedreturn"
               + " > '" + new Date(0) + "'";
         ArrayList<String> items = new ArrayList<>();
         ResultSet rs = runSql(sql);
         
         while (rs.next())
         {
            items.add(rs.getString(1) + " " + rs.getString(3) + " " + rs.getString(2));
         }
         
         return items;
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
      return null;
   }
   
   /**
    * 
    * @return
    */
   public ArrayList<String> getAllActiveJobs()
   {
      try
      {
         String sql = "SELECT dbrefnum FROM equipment WHERE checkedout <> '" + new Timestamp(0) + "'";
         ResultSet rs = runSql(sql);
         
         HashSet<Integer> dbrefnums = new HashSet<>();
         while (rs.next())
         {
            dbrefnums.add(rs.getInt(1));
         }
         
         dbrefnums.remove(0);
         dbrefnums.remove(1);
         dbrefnums.remove(2);
         dbrefnums.remove(3);
         dbrefnums.remove(4);
         
         
         Iterator<Integer> itr = dbrefnums.iterator();
         ArrayList<String> jobs = new ArrayList<>();
         while (itr.hasNext())
         {
            Integer dbrefnum = itr.next();
            sql = "SELECT projname,projectnumber FROM jobs WHERE dbrefnum = '" + dbrefnum +"'";
            rs = runSql(sql);
            if (rs.next())
            {
               jobs.add(rs.getString(2) + " " + rs.getString(1));
            }
         }
         
         return jobs;
         
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      }
      return null;
   }
   
   /**
    * returns information for a specific item
    * @return [id,dbrefnum,name,manufacturer,currentuser,checkedout,estimatedreturn,comments
    * calibrationinterval,nextcalibraitondate]
    */
   public ArrayList<String> getItemInfo(String id) 
   {
      try
      {
         String sql = "SELECT * FROM equipment WHERE id = '" + id + "'";
         ArrayList<String> item = new ArrayList<>();
         ResultSet rs = runSql(sql);
         
         while (rs.next())
         {
            int cols = rs.getMetaData().getColumnCount();
            for (int i = 0; i < cols; i++)
            {
               item.add(rs.getString(i+1));
            }
         }
         
         return item;
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
      return null;
   }
   
   /**
    * returns information for a specific item
    * @return ArrayList of all users
    */
   public ArrayList<String> getItemsToCalibrate() 
   {
      try
      {
         // add 2 weeks to current date
         Timestamp date = new Timestamp(System.currentTimeMillis() + 1209600000L);
         
         String sql = "SELECT id,manufacturer,name FROM equipment WHERE nextcalibrationdate <= '" + date + "' "
               + "AND nextcalibrationdate > '2000-01-01 00:00:00' AND dbrefnum <> '2'";
         ArrayList<String> items = new ArrayList<>();
         ResultSet rs = runSql(sql);
         
         while (rs.next())
         {
            int cols = rs.getMetaData().getColumnCount();
            String item = "";
            for (int i = 0; i < cols; i++)
            {
               item += rs.getString(i+1) + " ";
            }
            item = item.substring(0, item.length()-1);
            items.add(item);
         }
         return items;
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
      return null;
   }
   
   /**
    * returns all equipment corresponding to a certain job
    * @param dbrefnum
    * @return
    */
   public ArrayList<Equipment> getItemsForJob(int dbrefnum) 
   {
      try
      {
         ArrayList<Equipment> equipment = new ArrayList<>();
         
         String sql = "SELECT * FROM equipment WHERE dbrefnum = '" + dbrefnum + "'";
         
         ResultSet rs = runSql(sql);
         
         while (rs.next())
         {
            int cols = rs.getMetaData().getColumnCount();
            ArrayList<String> item = new ArrayList<>();
            for (int i = 0; i < cols; i++)
            {
               item.add(rs.getString(i+1));
            }
            equipment.add(new Equipment(item));
         }
         return equipment;
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
      return null;
   }
   
   /**
    * 
    * @return
    */
   public ArrayList<String> getAllJobs() 
   {
      try
      {
         String sql = "SELECT projectnumber FROM jobs WHERE 1";
         
         ArrayList<String> jobs = new ArrayList<>();
         ResultSet rs = runSql(sql);
         
         while (rs.next())
         {
            String job = rs.getString(1);
            //if (job.length() == 1)
            //   continue;
            jobs.add(job);
         }
         
         return jobs;
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
      return null;
   }
   
   /**
    * retrieves all info on a job that would be input through the new job creation
    * this method is to retrieve information on past jobs so that they can be edited
    * @param dbrefnum
    * @return [dbrefnum,username,projectnumber,projname,activity,dept_client,datetime,
    * location,comments]
    */
   public ArrayList<String> getJobInfo(int dbrefnum) 
   {
      try
      {
         ArrayList<String> jobInfo = new ArrayList<>();
         String sql = "SELECT * FROM jobs WHERE dbrefnum = '" + dbrefnum + "'";
         
         ResultSet rs = runSql(sql);
         
         while (rs.next())
         {
            int cols = rs.getMetaData().getColumnCount();
            for (int i = 0; i < cols; i++)
            {
               jobInfo.add(rs.getString(i+1));
            }
         }
         
         return jobInfo;
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
      return null;
   }
   
   /**
    * returns the dbrefnum of a job based on the project number parameter
    * @param projectnumber
    * @return
    */
   public int getJobDBrefnum(String projectnumber) 
   {
      try
      {
         ResultSet rs = con.prepareStatement("SELECT dbrefnum FROM jobs WHERE projectnumber"
               + " = '" + projectnumber + "'").executeQuery();
         if (rs.next())
            return rs.getInt(1);
         else
            return -1;
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
      return -1;
   }
   
   /**
    * retrieves the list of audits from our audit trail
    */
   public ArrayList<String> getAudits()
   {
      try
      {
         String sql = "SELECT * FROM englog ORDER BY datetime DESC";
         
         ResultSet rs = runSql(sql);
         
         ArrayList<String> results = new ArrayList<>();
         while (rs.next())
         {
            Job j = new Job(getJobInfo(rs.getInt(2)));
            results.add(rs.getString(8) + ": " + rs.getString(3) + " " + rs.getString(5)
                  + " from " + rs.getString(6) + " to " + rs.getString(7) + " for "
                  + j.getProjname());
         }
         return results;
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
      
      return null;
   }
   
   
   public ArrayList<String> getFilteredAudits(String filter)
   {
      try
      {
         String sql = "SELECT * FROM englog WHERE equipmentid LIKE"
               + " '%" + filter + "%' OR projname LIKE '%" + filter + "%' OR equipmentname"
               + " LIKE '%" + filter + "%' OR userfrom LIKE '%" + filter + "%' OR"
               + " userto LIKE '%" + filter + "%' ORDER BY datetime DESC";
         
         ResultSet rs = runSql(sql);
         
         ArrayList<String> results = new ArrayList<>();
         while (rs.next())
         {
            Job j = new Job(getJobInfo(rs.getInt(2)));
            results.add(rs.getString(8) + ": " + rs.getString(3) + " " + rs.getString(5)
                  + " from " + rs.getString(6) + " to " + rs.getString(7) + " for "
                  + j.getProjname());
         }
         return results;
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
      
      return null;
   }
   
   /**
    * sets the item's job to 0 and user to admin
    * @param id
    */
   public void updateItemCheckIn(String id) 
   {
      try
      {
         insertAudit(id,0,Main.user, "admin");
         String sql = "UPDATE equipment SET dbrefnum = '0', currentuser = 'admin', checkedout"
               + " = '" + new Timestamp(0L) + "', estimatedreturn = '" + new Date(0L)
               + "' WHERE id = '" + id + "'";
         PreparedStatement stmt = con.prepareStatement(sql);
         stmt.execute();
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
   }
   
   /**
    * sets the calibration date to the next calibration date based on current time
    * @param id the equipment barcode id
    */
   public void updateItemCalibrationDate(Equipment toEdit) 
   {
      try
      {
         String sql = "Update equipment SET nextcalibrationdate = '" 
               + new Timestamp(toEdit.getCalibrationinterval()+ System.currentTimeMillis())
               + "' WHERE id = '" + toEdit.getId() + "'";
         PreparedStatement stmt = con.prepareStatement(sql);
         stmt.execute();
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
   }
   
   /**
    * sets a specific piece of equipment to the calibration job
    * @param items
    */
   public void updateItemToCalibration(String id)
   {
      try
      {
         PreparedStatement stmt = con.prepareStatement("UPDATE equipment SET dbrefnum = '2'"
               + ", currentuser = 'admin' WHERE id = (?)");
         stmt.setString(1, id);
         stmt.execute();
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
   }
   
   /**
    * Sets a piece of equipment in the database as broken
    * @param id
    */
   public void updateItemBroken(String id)
   {
      try
      {
         String sql = "UPDATE equipment SET dbrefnum = 1 WHERE id = '" + id + "'";
         runSql2(sql);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
   }
   
   /**
    * sets the equipment as not broken in the database
    * @param id barcode id of equipment
    */
   public void updateItemNotBroken(String id)
   {
      String sql = "UPDATE equipment SET dbrefnum = 0 WHERE id = '" + id + "'";
      try
      {
         runSql2(sql);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
   }
   
   /**
    * Edits the information for a given piece of equipment
    * @param toEdit
    */
   public void updateItemInfo(Equipment toEdit)
   {
      try {
         String sql = "UPDATE equipment SET dbrefnum = '"  + toEdit.getDbrefnum() + "'"
               + ", name = (?), manufacturer = (?), currentuser = (?), checkedout = (?)"
               + ", estimatedreturn = (?), comments = (?), calibrationinterval = (?)"
               + ", nextcalibrationdate = (?), dimensions = (?), weight = (?)"
               + ", value = (?) WHERE id = '" + toEdit.getId() + "'";
         
         PreparedStatement stmt = con.prepareStatement(sql);
         
         stmt.setString(1, toEdit.getName());
         stmt.setString(2, toEdit.getManufacturer());
         stmt.setString(3, toEdit.getCurrentuser());
         stmt.setTimestamp(4, toEdit.getCheckedout());
         stmt.setDate(5, toEdit.getEstimatedreturn());
         stmt.setString(6, toEdit.getComments());
         stmt.setLong(7, toEdit.getCalibrationinterval());
         if (toEdit.getCalibrationinterval() == 0)
            stmt.setDate(8, new Date(0L));
         else
            stmt.setDate(8, toEdit.getNextcalibrationdate());
         stmt.setString(9, toEdit.getDimensions());
         stmt.setString(10,toEdit.getWeight());
         stmt.setFloat(11, toEdit.getValue());
         
         stmt.executeUpdate();
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
   }
   
   /**
    * Edits the information for the given piece of equipment during a checkout
    * @param id
    * @param dbrefnum
    * @param time
    */
   private void updateItemInfo(Equipment eq, int dbrefnum, Date returndate)
   {
      try
      {
         insertAudit(eq.getId(),dbrefnum,"admin",Main.user);
         if (eq.getCheckedout().getTime() == 0)
            eq.setCheckedout(new Timestamp(System.currentTimeMillis()));
         String sql = "UPDATE equipment SET dbrefnum = '" + dbrefnum + "',"
               + "currentuser = '" + Main.user + "', checkedout"
               + " = '" + eq.getCheckedout() + "', estimatedreturn ="
               + " '" + returndate + "' WHERE id = '" + eq.getId() + "'";
         PreparedStatement stmt = con.prepareStatement(sql);
         stmt.execute();
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
   }
   
   /**
    * updates the information for a job that already exists and updates information on a 
    * piece of equipment
    * @param jobInfo
    * @return dbrefnum of the job
    */
   public int updateExistingJob(Job toEdit)
   {
      try
      {
         for (Equipment e : toEdit.getEquipment())
         {
            updateItemCheckIn(e.getId());
         }
         
         String sql = "UPDATE jobs SET username = '" + Main.user + "',"
               + " projname = '" + toEdit.getProjname() + "', projectnumber = '" + toEdit.getProjectnumber() + "',"
               + "activity = '" + toEdit.getActivity() + "', dept_client = '" + toEdit.getDept_client() + "',"
               + " location = '" + toEdit.getLocation() + "', comments = '" + toEdit.getComments() + "' WHERE dbrefnum = '"
               + toEdit.getDbrefnum() +"'";
         
         PreparedStatement stmt = con.prepareStatement(sql);
         stmt.execute();
         
         for (Equipment e : toEdit.getEquipment())
         {
            updateItemInfo(e,toEdit.getDbrefnum(),toEdit.getReturnDate());
         }
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
      return toEdit.getDbrefnum();
   }
   
   /**
    * Updates the information of a job in the database
    * @param toEdit
    */
   public void updateJobInfo(Job toEdit)
   {
      try
      {
         String sql = "UPDATE jobs SET username = (?), projectnumber = (?), projname"
               + " = (?), activity = (?), dept_client = (?), datetime = (?), location"
               + " = (?), comments = (?) WHERE dbrefnum = '" + toEdit.getDbrefnum() + "'";
         
         PreparedStatement stmt = con.prepareStatement(sql);
         
         stmt.setString(1, Main.user);
         stmt.setString(2, toEdit.getProjectnumber());
         stmt.setString(3, toEdit.getProjname());
         stmt.setString(4, toEdit.getActivity());
         stmt.setString(5, toEdit.getDept_client());
         stmt.setTimestamp(6, toEdit.getDatetime());
         stmt.setString(7, toEdit.getLocation());
         stmt.setString(8, toEdit.getComments());
         
         stmt.executeUpdate();
         
         for (Equipment e : toEdit.getEquipment())
         {
            updateItemInfo(e);
         }
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
   }
   

   
   /**
    * checks out an item when creating a new job
    * @param id
    * @return the dbrefnum of the new job
    */
   public int insertNewJob(Job toCreate)
   {
      int dbrefnum = -1;
      try
      {
         if (checkJobExists(toCreate.getProjectnumber()))
         {
            return -1;
         }
         
         String sql = "INSERT INTO jobs (username,projectnumber,projname,activity,dept_client,"
               + "location,comments) VALUES (?,?,?,?,?,?,?)";
         PreparedStatement stmt = con.prepareStatement(sql);
         stmt.setString(1, Main.user);
         stmt.setString(2, toCreate.getProjectnumber());
         stmt.setString(3, toCreate.getProjname());
         stmt.setString(4, toCreate.getActivity());
         stmt.setString(5, toCreate.getDept_client());
         stmt.setString(6, toCreate.getLocation());
         stmt.setString(7, toCreate.getComments());
         stmt.execute();
         
         dbrefnum = getJobDBrefnum(toCreate.getProjectnumber());
         
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
      
      return dbrefnum;
   }
   
   /**
    * inserts a new user into the user table in the database
    * @param username the person's username (hopefully HDR name, will shorten to 8 char)
    * @param fullname the person's full name (first last, so we know who to blame)
    */
   public void insertNewUser(String username, String fullname, String email)
   {
      try
      {
         PreparedStatement stmt = con.prepareStatement("INSERT INTO users (username,fullname,email) VALUES (?,?,?)");
         
         if (username.length() > 8)
            username = username.substring(0, 8);
         
         stmt.setString(1, username);
         stmt.setString(2,fullname);
         stmt.setString(3,email);
         stmt.executeUpdate();
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
   }
   
   /**
    * Adds to the audit trail
    * @param id
    * @param dbrefnum
    * @param user
    */
   public void insertAudit(String id, int dbrefnum, String userfrom, String userto)
   {
      try {
         PreparedStatement stmt = con.prepareStatement("INSERT INTO englog (jobdbrefnum,equipmentid,projname,equipmentname,userfrom,userto)"
               + "VALUES (?,?,?,?,?,?)");
         stmt.setInt(1, dbrefnum);
         stmt.setString(2,id);
         
         // get projname from database, using job dbrefnum
         ResultSet rs = runSql("SELECT projname FROM jobs WHERE dbrefnum = '" + dbrefnum + "'");
         String projname = "";
         if (rs.next())
            projname = rs.getString(1);
         stmt.setString(3, projname);
         
         // get equipment name from db using equipmentid
         rs = runSql("SELECT name FROM equipment WHERE id = '" + id + "'");
         String equipname = "";
         if (rs.next())
            equipname = rs.getString(1);
         stmt.setString(4,equipname);
         
         stmt.setString(5,userfrom);
         stmt.setString(6, userto);
         stmt.executeUpdate();
         
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
   }
   
   /**
    * adds a new item into the database
    * @param itemInfo
    */
   public void insertNewItem(Equipment toCreate)
   {
      try
      {
         String sql = "INSERT INTO equipment (id,dbrefnum,name,manufacturer,currentuser,"
               + "checkedout,estimatedreturn,comments,calibrationinterval,nextcalibrationdate)"
               + " VALUES (?,?,?,?,?,?,?,?,?,?)";
         
         PreparedStatement stmt = con.prepareStatement(sql);
         
         stmt.setString(1, toCreate.getId());
         stmt.setString(2, "0");
         stmt.setString(3, toCreate.getName());
         stmt.setString(4, toCreate.getManufacturer());
         stmt.setString(5, "admin");
         stmt.setTimestamp(6, new Timestamp(0L));
         stmt.setTimestamp(7, new Timestamp(0L));
         stmt.setString(8, toCreate.getComments());
         stmt.setLong(9, toCreate.getCalibrationinterval());
         stmt.setDate(10, toCreate.getNextcalibrationdate());
         
         stmt.executeUpdate();
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
   }
   
   /**
    * 
    * @param search
    * @return
    */
   public ArrayList<String> search(String search)
   {
      try
      {
         String sql = "SELECT id FROM equipment WHERE name LIKE '%" + search + "%' OR "
               + " manufacturer LIKE '%" + search + "%' OR id LIKE '%" + search + "%'";
         
         ResultSet rs = runSql(sql);
         TreeSet<String> results = new TreeSet<>();
         
         while (rs.next())
         {
            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++)
            {
               results.add(rs.getString(i+1));
            }
         }
         return searchIDs(results);
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
      return null;
   }
   
   /**
    * returns the id manufacturer name of each item contained in the results set from
    * our search algorithm
    * @param results
    * @return
    */
   private ArrayList<String> searchIDs(TreeSet<String> results) 
   {
      try
      {
         ArrayList<String> items = new ArrayList<String>();
         TreeSet<String> checkedInItems = new TreeSet<String>();
         TreeSet<String> checkedOutItems = new TreeSet<String>();
         
         for (String s : results)
         {
            String sql = "SELECT id,name,manufacturer,currentuser FROM equipment WHERE id = '" + s + "'";
            ResultSet rs = runSql(sql);
            
            while (rs.next())
            {
               String rsString = rs.getString(1) + " " + rs.getString(3) + " " + rs.getString(2);
               if (rs.getString(4).equals("admin"))
                  checkedInItems.add(rsString);
               else if (!checkedInItems.contains(rsString))
               {
                  checkedOutItems.add(rsString);
               }
            }
         }
         items.add("Checked In");
         items.addAll(checkedInItems);
         items.add("Checked Out");
         items.addAll(checkedOutItems);
         
         return items;
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
      return null;
   }
   
   /**
    * reconnects to the database if the connection has been closed or is not established
    */
   protected void reconnect()
   {
      try
      {
         if (con.isClosed())
         {
            // load in user and pass
            BufferedReader input = new BufferedReader(new FileReader("data.dat"));
            String ip = input.readLine();
            String username = input.readLine();
            String pass = input.readLine();
            input.close();
            
            Class.forName("com.mysql.jdbc.Driver");
            
            // location of DB (local or otherwise)
            String url = ip;
            
            con = DriverManager.getConnection(url, username, pass);
            System.out.println("con reestablished");
         }
      } catch (ClassNotFoundException | SQLException | IOException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
   }
   
   /**
    * closes connection
    */
   public void disconnect()
   {
      try
      {
         if (!con.isClosed())
         {
            con.close();
            System.out.println("con closed");
         }
      } catch (SQLException e)
      {
         ExceptionHandler.displayException(e);
      } catch (Exception e)
      {
         ExceptionHandler.displayException(e);
      }
   }
   
   /**
    * close connection - overrides "deconstructor"
    */
   @ Override
   protected void finalize() throws Throwable
   {
      if (con != null || !con.isClosed())
      {
         con.close();
      }
   }
   
   
}
