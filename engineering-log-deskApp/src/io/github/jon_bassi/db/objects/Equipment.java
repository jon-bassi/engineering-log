package io.github.jon_bassi.db.objects;

import io.github.jon_bassi.Main;

import java.util.ArrayList;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * Class to hold equipment object
 * @author jon-bassi
 */
public class Equipment
{
   private String id;
   private int dbrefnum;
   private String name;
   private String manufacturer;
   private String currentuser;
   private Timestamp checkedout;
   private Date estimatedreturn;
   private String comments;
   private long calibrationinterval;
   private Date nextcalibrationdate;
   private String dimensions;
   private String weight;
   private float value;
   
   private boolean isCheckedIn;
   private boolean isBroken;
   private boolean isBeingCalibrated;
   private boolean isReady;
   
   public Equipment()
   {
      id = "";
      dbrefnum = 0;
      name = "";
      manufacturer = "";
      currentuser = "admin";
      checkedout = new Timestamp(0);
      estimatedreturn = new Date(0);
      comments = "";
      calibrationinterval = 0;
      nextcalibrationdate = new Date (0);
      dimensions = "";
      weight = "";
      value = 0.0f;
   }
   
   public Equipment(ArrayList<String> resultSet)
   {
      id = resultSet.get(0);
      dbrefnum = Integer.parseInt(resultSet.get(1));
      switch (dbrefnum)
      {
         case 1 : isBroken = true;
                  isBeingCalibrated = false;
                  break;
         case 2 : isBroken = false;
                  isBeingCalibrated = true;
                  break;
         default : isBroken = false;
                   isBeingCalibrated = false;
      }
      name = resultSet.get(2);
      if (name.equals("admin"))
      {
         isCheckedIn = true;
      }
      else
      {
         isCheckedIn = false;
      }
      manufacturer = resultSet.get(3);
      currentuser = resultSet.get(4);
      checkedout = Timestamp.valueOf(resultSet.get(5));
      estimatedreturn = Date.valueOf(resultSet.get(6));
      comments = resultSet.get(7);
      calibrationinterval = Long.parseLong(resultSet.get(8));
      nextcalibrationdate = Date.valueOf(resultSet.get(9));
      dimensions = resultSet.get(10);
      weight = resultSet.get(11);
      value = Float.parseFloat(resultSet.get(12));
   }
   
   /**
    * @return the id
    */
   public String getId()
   {
      return id;
   }

   /**
    * @return the dbrefnum
    */
   public int getDbrefnum()
   {
      return dbrefnum;
   }

   /**
    * @return the name
    */
   public String getName()
   {
      return name;
   }

   /**
    * @return the manufacturer
    */
   public String getManufacturer()
   {
      return manufacturer;
   }

   /**
    * @return the currentuser
    */
   public String getCurrentuser()
   {
      return currentuser;
   }

   /**
    * @return the checkedout
    */
   public Timestamp getCheckedout()
   {
      return checkedout;
   }

   /**
    * @return the estimatedreturn
    */
   public Date getEstimatedreturn()
   {
      return estimatedreturn;
   }

   /**
    * @return the comments
    */
   public String getComments()
   {
      return comments;
   }

   /**
    * @return the calibrationinterval
    */
   public long getCalibrationinterval()
   {
      return calibrationinterval;
   }

   /**
    * @return the nextcalibrationdate
    */
   public Date getNextcalibrationdate()
   {
      return nextcalibrationdate;
   }
   
   /**
    * @return the dimensions
    */
   public String getDimensions()
   {
      return dimensions;
   }
   
   /**
    * @return the weight
    */
   public String getWeight()
   {
      return weight;
   }
   
   /**
    * @return the value ($$$)
    */
   public float getValue()
   {
      return value;
   }
   
   /**
    * @param id the id to set
    */
   public void setId(String id)
   {
      this.id = id;
   }

   /**
    * @param dbrefnum the dbrefnum to set
    */
   public void setDbrefnum(int dbrefnum)
   {
      this.dbrefnum = dbrefnum;
   }

   /**
    * @param name the name to set
    */
   public void setName(String name)
   {
      this.name = name;
   }

   /**
    * @param manufacturer the manufacturer to set
    */
   public void setManufacturer(String manufacturer)
   {
      this.manufacturer = manufacturer;
   }

   /**
    * @param currentuser the currentuser to set
    */
   public void setCurrentuser(String currentuser)
   {
      this.currentuser = currentuser;
   }

   /**
    * @param checkedout the checkedout to set
    */
   public void setCheckedout(Timestamp checkedout)
   {
      this.checkedout = checkedout;
   }

   /**
    * @param estimatedreturn the estimatedreturn to set
    */
   public void setEstimatedreturn(Date estimatedreturn)
   {
      this.estimatedreturn = estimatedreturn;
   }

   /**
    * @param comments the comments to set
    */
   public void setComments(String comments)
   {
      this.comments = comments;
   }

   /**
    * @param calibrationinterval the calibrationinterval to set
    */
   public void setCalibrationinterval(long calibrationinterval)
   {
      this.calibrationinterval = calibrationinterval;
   }

   /**
    * @param nextcalibrationdate the nextcalibrationdate to set
    */
   public void setNextcalibrationdate(Date nextcalibrationdate)
   {
      this.nextcalibrationdate = nextcalibrationdate;
   }

   /**
    * @param dimensions the dimensions to set
    */
   public void setDimensions(String dimensions)
   {
      this.dimensions = dimensions;
   }
   
   /**
    * @param weight the weight to set
    */
   public void setWeight(String weight)
   {
      this.weight = weight;
   }
   
   /**
    * @param value the value to set ($$$)
    */
   public void setValue(Float value)
   {
      this.value = value;
   }
   
   /**
    * Sets the status of a piece of equipment, also changes dbrefnum and current user if
    * applicable
    * @param dbrefnum
    */
   public void setEquipmentStatus(int dbrefnum)
   {
      switch (dbrefnum)
      {
         case 0 : isCheckedIn = true;
                  isBroken = false;
                  isBeingCalibrated = false;
                  this.dbrefnum = 0;
                  this.currentuser = "admin";
                  checkedout = new Timestamp(0);
                  break;
         case 1 : isBroken = true;
                  isBeingCalibrated = false;
                  this.dbrefnum = 1;
                  this.currentuser = "admin";
                  break;
         case 2 : isBroken = false;
                  isBeingCalibrated = true;
                  this.dbrefnum = 2;
                  this.currentuser = Main.user;
                  break;
         case 4 : isCheckedIn = false;
                  isBroken = false;
                  isBeingCalibrated = false;
                  this.dbrefnum = 4;
                  this.currentuser = Main.user;
         default : // do nothing presently, don't want to change values if the user cancels
                   // selection
      }
   }
   
   /**
    * Sets the status for a personal item
    * @param dbrefnum
    * @param user
    */
   public void setEquipmentStatus(int dbrefnum, String user)
   {
      switch (dbrefnum)
      {
         case 4 : isCheckedIn = false;
                  isBroken = false;
                  isBeingCalibrated = false;
                  this.dbrefnum = 4;
                  this.currentuser = user;
         
      }
   }
   /**
    * @return if the equipment is checked in or not
    */
   public boolean isCheckedIn()
   {
      return isCheckedIn;
   }
   
   /**
    * @return if the equipment is broken or not
    */
   public boolean isBroken()
   {
      return isBroken;
   }
   
   /**
    * @return if the equipment is being calibrated or not
    */
   public boolean isBeingCalibrated()
   {
      return isBeingCalibrated;
   }
   
   /**
    * sets whether or not the equipmet is ready to be inserted into the database
    */
   public void setReady(boolean isReady)
   {
      this.isReady = isReady;
   }
   
   /**
    * @return if the equipment is ready to be inserted into the database,
    * also works as a check to see if a user cancelled creation of an item as the isReady
    * boolean will be set to false if this is the case
    */
   public boolean isReady()
   {
      return isReady;
   }
   
   /**
    * checks if the necessary fields are filled
    * @return true if the name, manufacturer and id are not empty strings, false if otherwise
    */
   public boolean checkFields()
   {
      if (id.equals("") || name.equals("") || manufacturer.equals(""))
         return false;
      return true;
   }
   
   /**
    * sets notable fields in this piece of equipment to that of the job it will be added
    * to in db call
    * @param toAdd
    */
   public void addToJob(Job toAdd)
   {
      this.dbrefnum = toAdd.getDbrefnum();
      this.estimatedreturn = toAdd.getReturnDate();
      this.currentuser = toAdd.getUsername();
      toAdd.getEquipment().add(this);
      this.setCheckedout(new Timestamp(System.currentTimeMillis()));
   }
   
   /**
    * Overrided toString method
    * @return ID Manufacturer Name
    */
   @ Override
   public String toString()
   {
      return id + " " + manufacturer + " " + name;
   }
}
