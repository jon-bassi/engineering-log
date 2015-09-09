package io.github.jon_bassi.db.objects;

import io.github.jon_bassi.Main;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * class to hold a job object
 * @author jon-bassi
 */
public class Job
{
   private int dbrefnum;
   private String username;
   private String projectnumber;
   private String projname;
   private String activity;
   private String dept_client;
   private Timestamp datetime;
   private String location;
   private String comments;
   
   private boolean isReady;
   
   private Date returnDate;
   
   private ArrayList<Equipment> equipment = new ArrayList<>();
   
   /**
    * 
    */
   public Job()
   {
      dbrefnum = -1;
      username = "";
      projectnumber = "";
      projname = "";
      activity = "";
      dept_client = "";
      datetime = new Timestamp(System.currentTimeMillis());
      location = "";
      comments = "";
      returnDate = new Date(0);
   }
   
   /**
    * 
    * @param resultSet
    */
   public Job(ArrayList<String> resultSet)
   {
      dbrefnum = Integer.parseInt(resultSet.get(0));
      username = resultSet.get(1);
      projectnumber = resultSet.get(2);
      projname = resultSet.get(3);
      activity = resultSet.get(4);
      dept_client = resultSet.get(5);
      if (resultSet.size() >0)
         datetime = Timestamp.valueOf(resultSet.get(6));
      else
         datetime = new Timestamp(System.currentTimeMillis());
      location = resultSet.get(7);
      comments = resultSet.get(8);
      
      setEquipment(Main.database.getItemsForJob(dbrefnum));
   }

   /**
    * @return the dbrefnum
    */
   public int getDbrefnum()
   {
      return dbrefnum;
   }

   /**
    * @return the username
    */
   public String getUsername()
   {
      return username;
   }

   /**
    * @return the projectnumber
    */
   public String getProjectnumber()
   {
      return projectnumber;
   }

   /**
    * @return the projname
    */
   public String getProjname()
   {
      return projname;
   }

   /**
    * @return the activity
    */
   public String getActivity()
   {
      return activity;
   }

   /**
    * @return the dept_client
    */
   public String getDept_client()
   {
      return dept_client;
   }

   /**
    * @return the datetime
    */
   public Timestamp getDatetime()
   {
      return datetime;
   }

   /**
    * @return the location
    */
   public String getLocation()
   {
      return location;
   }

   /**
    * @return the comments
    */
   public String getComments()
   {
      return comments;
   }

   /**
    * @param dbrefnum the dbrefnum to set
    */
   public void setDbrefnum(int dbrefnum)
   {
      this.dbrefnum = dbrefnum;
   }

   /**
    * @param username the username to set
    */
   public void setUsername(String username)
   {
      this.username = username;
   }

   /**
    * @param projectnumber the projectnumber to set
    */
   public void setProjectnumber(String projectnumber)
   {
      this.projectnumber = projectnumber;
   }

   /**
    * @param projname the projname to set
    */
   public void setProjname(String projname)
   {
      this.projname = projname;
   }

   /**
    * @param activity the activity to set
    */
   public void setActivity(String activity)
   {
      this.activity = activity;
   }

   /**
    * @param dept_client the dept_client to set
    */
   public void setDept_client(String dept_client)
   {
      this.dept_client = dept_client;
   }

   /**
    * @param datetime the datetime to set
    */
   public void setDatetime(Timestamp datetime)
   {
      this.datetime = datetime;
   }

   /**
    * @param location the location to set
    */
   public void setLocation(String location)
   {
      this.location = location;
   }

   /**
    * @param comments the comments to set
    */
   public void setComments(String comments)
   {
      this.comments = comments;
   }
   

   /**
    * @return the isReady
    */
   public boolean isReady()
   {
      return isReady;
   }

   /**
    * @param isReady the isReady to set
    */
   public void setReady(boolean isReady)
   {
      this.isReady = isReady;
   }
   
   /**
    * 
    * @return
    */
   public boolean checkFields()
   {
      if (projectnumber.equals("") || projname.equals("") || location.equals(""))
         return false;
      return true;
   }
   
   
   /**
    * 
    * @param equipment
    */
   public void setEquipment(ArrayList<Equipment> equipment)
   {
      this.equipment = equipment;
      returnDate = getItemsReturnDate();
   }
   
   /**
    * 
    * @return
    */
   public ArrayList<Equipment> getEquipment()
   {
      return equipment;
   }
   
   public Date getReturnDate()
   {
      return returnDate;
   }
   
   public void setReturnDate(Date returnDate)
   {
      this.returnDate = returnDate;
      for (Equipment e : equipment)
         e.setEstimatedreturn(returnDate);
   }
   
   private Date getItemsReturnDate()
   {
      if (equipment.size() == 0)
         return new Date(0);
      else
         return equipment.get(0).getEstimatedreturn();
   }
}
