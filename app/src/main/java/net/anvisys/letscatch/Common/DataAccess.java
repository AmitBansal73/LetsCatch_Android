package net.anvisys.letscatch.Common;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import net.anvisys.letscatch.Object.APP_CONST;
import net.anvisys.letscatch.Object.APP_SETTINGS;
import net.anvisys.letscatch.Object.ActiveMeeting;
import net.anvisys.letscatch.Object.ActiveMeetingGroup;
import net.anvisys.letscatch.Object.ChatMessage;
import net.anvisys.letscatch.Object.Contact;
import net.anvisys.letscatch.Object.Log;
import net.anvisys.letscatch.Object.Message;
import net.anvisys.letscatch.Object.Schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Amit Bansal on 09-01-2017.
 */
public class DataAccess {
    Context mContext;
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase myDatabase;

    private static final String DATABASE_NAME = "letscatch.db";
    private static final String CONTACT_TABLE_NAME = "contact_master";
    private static final String TEMP_CONTACT_TABLE_NAME = "temp_contact_master";
    private static final String PHONE_CONTACT_TABLE_NAME = "phone_contact_master";
    private static final String LOG_TABLE_NAME = "log_master";
    private static final String ACTIVE_MEETING_TABLE_NAME = "active_meeting";
    private static final String MESSAGE_TABLE_NAME = "message_master";

    private static final int DATABASE_VERSION = 1;



    private static final String TABLE_CREATE_CONTACTS = "CREATE TABLE IF NOT EXISTS "
            + CONTACT_TABLE_NAME
            + "(id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "USER_ID INTEGER, name VARCHAR(20), mobile_no VARCHAR(15), location VARCHAR(15),"
            + "imgBMP BLOB);";
    private static final String TABLE_CREATE_TEMP_CONTACTS = "CREATE TABLE IF NOT EXISTS "
            + TEMP_CONTACT_TABLE_NAME
            + "(id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "name VARCHAR(20), mobile_no VARCHAR(15));";

    private static final String TABLE_CREATE_PHONE_CONTACTS = "CREATE TABLE IF NOT EXISTS "
            + PHONE_CONTACT_TABLE_NAME
            + "(id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "name VARCHAR(20), mobile_no VARCHAR(15));";

    private static final String TABLE_CREATE_LOG = "CREATE TABLE IF NOT EXISTS "
            + LOG_TABLE_NAME
            + "(ID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "text VARCHAR(20), date_time datetime(20));";


    private static final String TABLE_CREATE_ACTIVE_MEETING = "CREATE TABLE IF NOT EXISTS "
            + ACTIVE_MEETING_TABLE_NAME
            + "(ID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "mobile_no VARCHAR(20), status VARCHAR(10), location VARCHAR(20),"
            + "name VARCHAR(20), time datetime(20));";

    private static final String TABLE_CREATE_MESSAGE = "CREATE TABLE IF NOT EXISTS "
            + MESSAGE_TABLE_NAME
            + "(id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "dest_mobile INTEGER, msg_mobile VARCHAR(20), msg_type VARCHAR(20), message VARCHAR(50), date_time datetime(20));";

    public DataAccess(Context context) {
        this.mContext = context;
    }

    public static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);

        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TABLE_CREATE_CONTACTS);
            db.execSQL(TABLE_CREATE_TEMP_CONTACTS);
            db.execSQL(TABLE_CREATE_LOG);
            db.execSQL(TABLE_CREATE_ACTIVE_MEETING);
            db.execSQL(TABLE_CREATE_PHONE_CONTACTS);
            db.execSQL(TABLE_CREATE_MESSAGE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + MESSAGE_TABLE_NAME);
            onCreate(db);
        }
    }


    public DataAccess open() throws SQLException {
        try {
            mDbHelper = new DatabaseHelper(mContext);
            myDatabase = mDbHelper.getWritableDatabase();
            return this;
        }
        catch (SQLException ex)
        {
            Toast.makeText(mContext, ex.getMessage().toString(), Toast.LENGTH_LONG).show();
            return null;
        }
        catch (Exception EEx)
        {
            Toast.makeText(mContext,EEx.getMessage().toString(),Toast.LENGTH_LONG).show();
            return null;
        }
    }

    public boolean ClearAll()
    {
        try {
            myDatabase.execSQL("DROP TABLE IF EXISTS " + CONTACT_TABLE_NAME);
            myDatabase.execSQL("DROP TABLE IF EXISTS " + TEMP_CONTACT_TABLE_NAME);
            myDatabase.execSQL("DROP TABLE IF EXISTS " + LOG_TABLE_NAME);
            myDatabase.execSQL("DROP TABLE IF EXISTS " + ACTIVE_MEETING_TABLE_NAME);
            myDatabase.execSQL("DROP TABLE IF EXISTS " + PHONE_CONTACT_TABLE_NAME);
            myDatabase.execSQL("DROP TABLE IF EXISTS " + MESSAGE_TABLE_NAME);

            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    public void close() {
        mDbHelper.close();
    }


    public List<Schedule> getMeetingForDate(String date)
      {
          List<Schedule> DayList = new ArrayList<>();
          Schedule tempSchedule = new Schedule("Test","87655678", "Deepak Srivastav", 1, 2,"23/01/2017","13:15");
          DayList.add(tempSchedule);
          return DayList;
      }

    public List<Schedule> getMeetingForMonth(int month,int Year)
    {
        List<Schedule> MonthList = new ArrayList<>();
        Schedule tempSchedule = new Schedule("Test","87655678", "Deepak Srivastav", 1, 2,"23/01/2017","13:15");
        MonthList.add(tempSchedule);
        return MonthList;
    }

   public void insertNewSchedule(Schedule newMeet)
    {}

    //region ContactFunction


    public boolean deleteAllContact() {
        try {
            myDatabase.execSQL("Delete from " + CONTACT_TABLE_NAME);
            myDatabase.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE name='" + CONTACT_TABLE_NAME + "';");

        }
        catch(Exception ex){}

        return false;
    }


    public long insertNewTempContact(String number, String name) {
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("name", name);
            initialValues.put("mobile_no", number);
            if (!checkTempMobileNoExist(number)) {
                myDatabase.execSQL(TABLE_CREATE_TEMP_CONTACTS);
                return myDatabase.insert(TEMP_CONTACT_TABLE_NAME, null, initialValues);
            } else
            {
                myDatabase.execSQL("DELETE FROM "+TEMP_CONTACT_TABLE_NAME+" WHERE mobile_no = "+ number);
                return myDatabase.insert(TEMP_CONTACT_TABLE_NAME, null, initialValues);
            }
        }
        catch (Exception ex)
        {
            throw new SQLException();
        }

    }

    public void deleteTempContact(String number)
    {
        try{
            String delete_query = "DELETE FROM " + TEMP_CONTACT_TABLE_NAME + " WHERE mobile_no = '" + number + "'";
            myDatabase.execSQL(delete_query);
        }
        catch (Exception ex)
        {

        }

    }

    public HashMap<String,String> getAllTempContact() {
        Cursor cur;
        HashMap<String,String> numberlist = new HashMap();
        try {
            myDatabase.execSQL(TABLE_CREATE_TEMP_CONTACTS);
            String selectQuery = "SELECT  * FROM " + TEMP_CONTACT_TABLE_NAME;
            cur = myDatabase.rawQuery(selectQuery, null);
            if(cur!=null && cur.getCount() > 0) {
                if (cur.moveToFirst()) {
                    do {
                        String Name = (cur.getString(cur.getColumnIndex("name")));
                        String MobileNumber = (cur.getString(cur.getColumnIndex("mobile_no")));
                        numberlist.put(MobileNumber, Name);
                    } while (cur.moveToNext());
                }
            }
        }
        catch(Exception ex){
            int a =5;
            a++;
            numberlist.clear();
        }
        return numberlist;
    }

    public long insertNewContact(Contact con) {
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("USER_ID", con.ID);
            initialValues.put("name", con.userName);
            initialValues.put("imgBMP", " ");
            initialValues.put("mobile_no", con.MobileNumber);
            initialValues.put("location", con.location);

            if (!checkMobileNoExist(con.MobileNumber)) {
                myDatabase.execSQL(TABLE_CREATE_CONTACTS);
                return myDatabase.insert(CONTACT_TABLE_NAME, null, initialValues);
            } else
            {
                myDatabase.execSQL("DELETE FROM contact_master WHERE USER_ID = "+ con.ID);
                return myDatabase.insert(CONTACT_TABLE_NAME, null, initialValues);
            }

        }
        catch (Exception ex)
        {
            int a =0;
            return a;
        }

    }

    public void  updateContact(Contact con)
    {
        try {
            if (!checkMobileNoExist(con.MobileNumber)) {
                String selectQuery = "UPDATE contact_master SET location = \"" + con.location + "\", name = \" "+con.userName + "\", imgBMP = \" "+ con.strImage +"\" WHERE USER_ID = " + con.ID;
                myDatabase.execSQL(selectQuery);
            }
        }
        catch (Exception ex)
        {
            int a=5;
            a++;

        }
        return ;
    }


    public void  insertContactImage(int ID, String imgString)
    {
        try {
            String selectQuery =  "UPDATE contact_master SET imgBMP = \"" + imgString + "\" WHERE USER_ID = "+ ID;
            myDatabase.execSQL(selectQuery);
        }
        catch (Exception ex)
        {
            int a=5;
            a++;
        }
        return ;
    }


    public Cursor getSearchResult(String query) {
        Cursor cur =null;
        try {
            myDatabase.execSQL(TABLE_CREATE_CONTACTS);
            // String selectQuery = "SELECT  * FROM " + CONTACT_TABLE_NAME;
            String selectQuery = "SELECT * FROM " + CONTACT_TABLE_NAME + " WHERE upper(name) like '%" + query.toUpperCase() + "%'";
            cur = myDatabase.rawQuery(selectQuery, null);

        }
        catch(Exception ex){
            String x = ex.getMessage().toString();
        }
        return cur;
    }

    public List<Contact> getAllContact() {
        Cursor cur;
        List<Contact> noticelist = new ArrayList<Contact>();
        try {
            myDatabase.execSQL(TABLE_CREATE_CONTACTS);
            String selectQuery = "SELECT  * FROM " + CONTACT_TABLE_NAME;
            cur = myDatabase.rawQuery(selectQuery, null);
            if(cur!=null && cur.getCount() > 0) {
                if (cur.moveToFirst()) {
                    do {
                        Contact nt = new Contact();
                        nt.ID = (cur.getInt(cur.getColumnIndex("USER_ID")));
                        nt.userName = (cur.getString(cur.getColumnIndex("name")));
                        nt.MobileNumber = (cur.getString(cur.getColumnIndex("mobile_no")));
                        nt.location = (cur.getString(cur.getColumnIndex("location")));
                        nt.strImage = (cur.getString(cur.getColumnIndex("imgBMP")));
                        // adding to todo list
                        noticelist.add(nt);
                    } while (cur.moveToNext());
                }
            }
        }
        catch(Exception ex){
            int a =5;
            a++;
            noticelist.clear();
        }
        return noticelist;
    }

    public Contact getContactByName(String contName) {
        Contact cont = new Contact();
        try {
            myDatabase.execSQL(TABLE_CREATE_CONTACTS);
            String selectQuery = "SELECT  * FROM " + CONTACT_TABLE_NAME + " WHERE name ='" + contName + "'";
            Cursor c = myDatabase.rawQuery(selectQuery, null);
            if (c.moveToFirst()) {
                do {
                    cont.ID = (c.getInt(c.getColumnIndex("USER_ID")));
                    cont.userName = (c.getString(c.getColumnIndex("name")));
                    cont.MobileNumber= (c.getString(c.getColumnIndex("mobile_no")));
                    cont.location= (c.getString(c.getColumnIndex("location")));
                    cont.strImage= (c.getString(c.getColumnIndex("imgBMP")));
                    // adding to todo list

                } while (c.moveToNext());
            }
        }
        catch(Exception ex){

        }
        return cont;
    }

    public Contact getContactByMobile(String MobileNumber) {
        Contact cont = new Contact();
        try {
            myDatabase.execSQL(TABLE_CREATE_CONTACTS);
            String selectQuery = "SELECT  * FROM " + CONTACT_TABLE_NAME + " WHERE mobile_no ='" + MobileNumber + "'";
            Cursor c = myDatabase.rawQuery(selectQuery, null);
            if (c.moveToFirst()) {
                do {
                    cont.ID = (c.getInt(c.getColumnIndex("USER_ID")));
                    cont.userName = (c.getString(c.getColumnIndex("name")));
                    cont.MobileNumber= (c.getString(c.getColumnIndex("mobile_no")));
                    cont.location= (c.getString(c.getColumnIndex("location")));
                    cont.strImage= (c.getString(c.getColumnIndex("imgBMP")));
                    // adding to todo list

                } while (c.moveToNext());
                return cont;
            }
            else
            {
               return null;
            }
        }
        catch(Exception ex){
            return null;
        }

    }

    public String getNameByMobile(String MobileNumber) {
        String name ="";
        try {
            myDatabase.execSQL(TABLE_CREATE_CONTACTS);
            String selectQuery = "SELECT  * FROM " + CONTACT_TABLE_NAME + " WHERE mobile_no ='" + MobileNumber + "'";
            Cursor c = myDatabase.rawQuery(selectQuery, null);
            if (c.moveToFirst()) {
                do {
                    name = (c.getString(c.getColumnIndex("name")));
                } while (c.moveToNext());
            }
            else
            {
                name = "";
            }
        }
        catch(Exception ex){

        }
        return name;
    }

    public boolean checkMobileNoExist(String _mobileNo)
    {
        try {
            Cursor mCursor = myDatabase.query(true, CONTACT_TABLE_NAME, new String[]{"id",
                            "name", "mobile_no", "imgBMP"}, "mobile_no" + "='" + _mobileNo + "'", null,
                    null, null, null, null);
            if(mCursor == null|| mCursor.getCount()<1)
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    public boolean checkTempMobileNoExist(String _mobileNo)
    {
        try {
            Cursor mCursor = myDatabase.query(true, CONTACT_TABLE_NAME, new String[]{"id",
                            "name", "mobile_no"}, "mobile_no" + "='" + _mobileNo + "'", null,
                    null, null, null, null);
            if(mCursor == null|| mCursor.getCount()<1)
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        catch (Exception ex)
        {
            return false;
        }
    }


    public long insertNewPhoneContact(String number, String name) {
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("name", name);
            initialValues.put("mobile_no", number);
            return myDatabase.insert(PHONE_CONTACT_TABLE_NAME, null, initialValues);
        }
        catch (Exception ex)
        {
           return 1;
        }

    }

    public boolean IsOldNumber(String _mobileNo)
    {
        try {
            myDatabase.execSQL(TABLE_CREATE_PHONE_CONTACTS);
            Cursor mCursor = myDatabase.query(true, PHONE_CONTACT_TABLE_NAME, new String[]{"id",
                            "name", "mobile_no"}, "mobile_no" + "='" + _mobileNo + "'", null,
                    null, null, null, null);
            if(mCursor == null|| mCursor.getCount()<1)
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    //endregion

    // region Log Function

    public long InsertLog(String message) {
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("text", message);
            initialValues.put("date_time", UTILITY.CurrentLocalDateTimeString());
            myDatabase.execSQL(TABLE_CREATE_LOG);
            long value = myDatabase.insert(LOG_TABLE_NAME, null, initialValues);

            return value;

        }
        catch (Exception ex)
        {
            return 0;
        }

    }

    public boolean deleteLog(int ID) {
        try {
            myDatabase.execSQL("Delete from " + LOG_TABLE_NAME + " Where ID = " + ID);
          //  myDatabase.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE name='" + LOG_TABLE_NAME + "';" );

        }
        catch(Exception ex){}

        return false;

    }

    public boolean deleteAllLog() {
        try {
            myDatabase.execSQL("Delete from " + LOG_TABLE_NAME);
            myDatabase.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE name='" + LOG_TABLE_NAME + "';" );

        }
        catch(Exception ex){}

        return false;

    }

    public List<Log> getAllLog()
    {
        Cursor cur;
        List<Log> logList = new ArrayList<Log>();
        try {
            myDatabase.execSQL(TABLE_CREATE_LOG);
            String selectQuery = "SELECT * FROM " + LOG_TABLE_NAME;
            cur = myDatabase.rawQuery(selectQuery, null);
            if (cur.moveToFirst()) {
                do {
                    Log tempLog = new Log();
                    tempLog.ID = (cur.getInt(cur.getColumnIndex("ID")));
                    tempLog.DateTime = (cur.getString(cur.getColumnIndex("date_time")));
                    tempLog.text = (cur.getString(cur.getColumnIndex("text")));
                    logList.add(tempLog);

                } while (cur.moveToNext());
            } else {
                Log emptyLog = new Log();
                emptyLog.ID = 999;
                emptyLog.text = "Log File is empty";
                emptyLog.DateTime = UTILITY.CurrentLocalDateTimeString();

                logList.add(emptyLog);
            }
        }
        catch (Exception ex)
        {

        }
        return logList;
    }

    public void LimitLogData()
    {
        try {
            //String selectQuery = " DELETE FROM Table_Forum WHERE id in (SELECT id FROM Table_Forum ORDER BY id Desc Limit -1 OFFSET  (select count(*)-10 from Table_name) )";
            String selectQuery = "delete from log_master where ID not in ( select ID from Table_Forum order by id desc limit 50 )";
            myDatabase.execSQL(selectQuery);
        }
        catch (Exception ex)
        {
            int a =5;
        }
    }

    //endregion

    // region Active Meeting

    public void UpdateAllActiveMeetings()
    {
        try {
            myDatabase.execSQL("Delete from " + ACTIVE_MEETING_TABLE_NAME);

            if (ActiveMeetingGroup.GetInstance(mContext).RunningMeetings.size() > 0) {
                for (ActiveMeeting meeting : ActiveMeetingGroup.GetInstance(mContext).RunningMeetings.values()
                        ) {
                    ContentValues initialValues = new ContentValues();
                    initialValues.put("mobile_no", meeting.DESTINATION_MOBILE_NO);
                    initialValues.put("name", meeting.DESTINATION_NAME);
                    initialValues.put("status", meeting.MEETING_STATUS);
                    initialValues.put("time", meeting.START_TIME);
                    initialValues.put("location", meeting.DESTINATION_LATLONG);
                    myDatabase.execSQL("DELETE FROM " + ACTIVE_MEETING_TABLE_NAME + " WHERE mobile_no = " + meeting.DESTINATION_MOBILE_NO);
                    myDatabase.insert(ACTIVE_MEETING_TABLE_NAME, null, initialValues);
                }
                ;
            }
        }
        catch (Exception ex)
        {
            int b =1;
        }

    }

    public HashMap<String,ActiveMeeting> GetAllActiveMeetings()
    {
        Cursor cur;
        HashMap<String,ActiveMeeting> meetingList = new HashMap<>();
        myDatabase.execSQL(TABLE_CREATE_ACTIVE_MEETING);
        String selectQuery = "SELECT * FROM " + ACTIVE_MEETING_TABLE_NAME;
        cur = myDatabase.rawQuery(selectQuery, null);
        if (cur.moveToFirst()) {
            do {
                String Mobile = (cur.getString(cur.getColumnIndex("mobile_no")));
                String Name = (cur.getString(cur.getColumnIndex("name")));
                String Status = (cur.getString(cur.getColumnIndex("status")));
                String StartTime = (cur.getString(cur.getColumnIndex("time")));
                String location = (cur.getString(cur.getColumnIndex("location")));

                if(UTILITY.MinutesDifference(UTILITY.CurrentLocalDateTimeString(), StartTime)> APP_SETTINGS.MEETING_LIFE_MINUTE)
                {
                    myDatabase.execSQL("DELETE FROM " + ACTIVE_MEETING_TABLE_NAME + " WHERE mobile_no = " + Mobile);
                    deleteMessage(Mobile);
                    InsertLog("Meeting with " + Name + " exceeded life span");
                }
                else
                {
                    ActiveMeeting tempMeeting = new ActiveMeeting(Mobile,Name,Status,location,StartTime);
                    meetingList.put(Mobile, tempMeeting);
                }

            } while (cur.moveToNext());
        }
        return meetingList;

    }

    public  void InsertMeeting(String Mobile, String Name, String Status, String TargetLocation)
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put("mobile_no", Mobile);
        initialValues.put("name", Name);
        initialValues.put("status", Status);
        initialValues.put("location", TargetLocation);
        initialValues.put("time", UTILITY.CurrentLocalDateTimeString());
        if(checkMeetingExist(Mobile)) {
            myDatabase.execSQL("DELETE FROM " + ACTIVE_MEETING_TABLE_NAME + " WHERE mobile_no = " + Mobile);
        }
        myDatabase.insert(ACTIVE_MEETING_TABLE_NAME, null, initialValues);
    }



    public void RemoveActiveMeeting(String Mobile)
    {
        try {
            if (checkMeetingExist(Mobile)) {
                String selectQuery = "DELETE FROM " + ACTIVE_MEETING_TABLE_NAME + " WHERE mobile_no = " + Mobile;
                myDatabase.execSQL(selectQuery);
            }
        }
        catch (Exception ex)
        {
            int a=5;
            a++;
            throw new SQLException();
        }
        return ;
    }

    public boolean UpdateLocation(String Mobile, String Location)
    {
        try {
            if (!checkMeetingExist(Mobile)) {
                String selectQuery = "UPDATE active_meeting SET location = \"" + Location +"\" WHERE mobile_no = " + Mobile;
                myDatabase.execSQL(selectQuery);
                return true;
            }
            else
            {
                return false;
            }
        }
        catch (Exception ex)
        {
            int a=5;

        }
        return false;
    }

    public boolean UpdateActiveMeeting(String Mobile, String Status)
    {
        try {
            if (!checkMeetingExist(Mobile)) {
                String selectQuery = "UPDATE active_meeting SET status = \"" + Status +"\" WHERE mobile_no = " + Mobile;
                myDatabase.execSQL(selectQuery);
                return true;
            }
            else
            {
                return false;
            }
        }
        catch (Exception ex)
        {
            int a=5;

        }
        return false;
    }

    public boolean checkMeetingExist(String _mobileNo)
    {
        try {
            Cursor mCursor = myDatabase.query(true, ACTIVE_MEETING_TABLE_NAME, new String[]{"ID",
                            "name", "mobile_no"}, "mobile_no" + "='" + _mobileNo + "'", null,
                    null, null, null, null);
            if(mCursor == null|| mCursor.getCount()<1)
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    // endregion

    // region Chat Message


    public long insertNewMessage(String DEST_MOBILE, String MSG_FROM,String TYPE, String MESSAGE) {
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("dest_mobile", DEST_MOBILE);
            initialValues.put("msg_mobile", MSG_FROM);
            initialValues.put("msg_type", TYPE);
            initialValues.put("message", MESSAGE);
            initialValues.put("date_time",UTILITY.CurrentLocalDateTimeString());
            myDatabase.execSQL(TABLE_CREATE_MESSAGE);
            LimitMessageData(DEST_MOBILE);
            return myDatabase.insert(MESSAGE_TABLE_NAME, null, initialValues);

        }
        catch (Exception ex)
        {
            throw new SQLException();
        }

    }

    public boolean deleteMessage(String mobile) {
        try {
            myDatabase.execSQL("Delete from " + MESSAGE_TABLE_NAME + " Where dest_mobile = '" + mobile + "'");
            //  myDatabase.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE name='" + LOG_TABLE_NAME + "';" );

        }
        catch(Exception ex){}

        return false;

    }

    public LinkedHashMap<Integer,ChatMessage> GetMessage(String  mobile) {
        LinkedHashMap<Integer,ChatMessage> chatList = new LinkedHashMap<>();
        int key =999;
        try {
            myDatabase.execSQL(TABLE_CREATE_MESSAGE);
            String selectQuery = "SELECT  * FROM " + MESSAGE_TABLE_NAME + " WHERE dest_mobile ='" + mobile + "' order by id asc";
            Cursor c = myDatabase.rawQuery(selectQuery, null);
            if (c.moveToFirst()) {
                do {
                    key = key-1;
                    ChatMessage chat = new ChatMessage();
                    chat.Dest_Mobile = (c.getString(c.getColumnIndex("dest_mobile")));
                    chat.Mobile= (c.getString(c.getColumnIndex("msg_mobile")));
                    chat.Type = (c.getString(c.getColumnIndex("msg_type")));
                       if(chat.Type.equalsIgnoreCase(Message.IMAGE_MESSAGE))
                       {
                           chat.ImageName= (c.getString(c.getColumnIndex("message")));
                       }
                       else if(chat.Type.equalsIgnoreCase(Message.TEXT_MESSAGE))
                       {
                           chat.Message= (c.getString(c.getColumnIndex("message")));
                       }
                    chat.dateTime= (c.getString(c.getColumnIndex("date_time")));
                    chat.DownloadStatus= 1;
                    chat.Delivery_Status= 1;
                    // adding to todo list
                    chatList.put(key, chat);

                } while (c.moveToNext());
            }
        }
        catch(Exception ex){

        }
        return chatList;

    }

    public ChatMessage GetLastMessage(String  mobile) {
        ChatMessage chat = new ChatMessage();

        try {
            myDatabase.execSQL(TABLE_CREATE_MESSAGE);
            String selectQuery = "SELECT  * FROM " + MESSAGE_TABLE_NAME + " WHERE dest_mobile ='" + mobile + " WHERE msg_type ='" + Message.TEXT_MESSAGE+ "' order by id desc limit 1";
            Cursor c = myDatabase.rawQuery(selectQuery, null);
            if (c.moveToFirst()) {
                do {
                    chat.Dest_Mobile = (c.getString(c.getColumnIndex("dest_mobile")));
                    chat.Mobile= (c.getString(c.getColumnIndex("msg_mobile")));
                    chat.Message= (c.getString(c.getColumnIndex("message")));
                    chat.dateTime= (c.getString(c.getColumnIndex("date_time")));
                    // adding to todo list
                } while (c.moveToNext());
            }
        }
        catch(Exception ex){

        }
        return chat;

    }

    public void LimitMessageData(String MobileNumber)
    {
        try {
            //String selectQuery = " DELETE FROM Table_Forum WHERE id in (SELECT id FROM Table_Forum ORDER BY id Desc Limit -1 OFFSET  (select count(*)-10 from Table_name) )";
            String selectQuery = "delete from message_master where ID not in ( select ID from message_master where dest_mobile = '"+ MobileNumber +"' order by id desc limit 10 )";
            myDatabase.execSQL(selectQuery);
        }
        catch (Exception ex)
        {
            int a =5;
        }
    }

    // endregion

    public ArrayList<Contact> GetDummyContactList()
    {
        ArrayList<Contact> dummyList = new ArrayList<>();

        Contact tempContact = new Contact();
        tempContact.ID =1;
        tempContact.userName = "Naveen Pandey";
        tempContact.MobileNumber = "1231234567";
        tempContact.location = "Noida";
        tempContact.strImage = "";
        tempContact.LatLong = "28.61548,77.39156";
        tempContact.Status = APP_CONST.MEETING_STATUS_NONE;
        dummyList.add(tempContact);

        tempContact = new Contact();
        tempContact.ID =2;
        tempContact.userName = "G Nagaraju";
        tempContact.MobileNumber = "2341234567";
        tempContact.location = "Hyderabad";
        tempContact.strImage = "";
        tempContact.LatLong = "28.635523,77.39256";
        tempContact.Status = APP_CONST.MEETING_STATUS_NONE;
        dummyList.add(tempContact);

        tempContact = new Contact();
        tempContact.ID =3;
        tempContact.userName = "Sridhar S";
        tempContact.MobileNumber = "9871234567";
        tempContact.location = "Delhi";
        tempContact.strImage = "";
        tempContact.LatLong = "28.655663,77.392512";
        tempContact.Status = APP_CONST.MEETING_STATUS_NONE;
        dummyList.add(tempContact);

        tempContact = new Contact();
        tempContact.ID =4;
        tempContact.userName = "Vinay Kumar";
        tempContact.MobileNumber = "7684534567";
        tempContact.location = "Noida";
        tempContact.strImage = "";
        tempContact.LatLong = "28.615163,77.394912";
        tempContact.Status = APP_CONST.MEETING_STATUS_NONE;
        dummyList.add(tempContact);

        tempContact = new Contact();
        tempContact.ID =5;
        tempContact.userName = "Manish Tyagi";
        tempContact.MobileNumber = "4587694567";
        tempContact.location = "Noida";
        tempContact.strImage = "";
        tempContact.LatLong = "28.625978,77.354412";
        tempContact.Status = APP_CONST.MEETING_STATUS_NONE;
        dummyList.add(tempContact);

        tempContact = new Contact();
        tempContact.ID =6;
        tempContact.userName = "Sudeep Agrawal";
        tempContact.MobileNumber = "9911234567";
        tempContact.location = "Lucknow";
        tempContact.strImage = "";
        tempContact.LatLong = "28.623978,77.334412";
        tempContact.Status = APP_CONST.MEETING_STATUS_NONE;
        dummyList.add(tempContact);

        tempContact = new Contact();
        tempContact.ID =7;
        tempContact.userName = "Devendra Gupta";
        tempContact.MobileNumber = "4567834567";
        tempContact.location = "Lucknow";
        tempContact.strImage = "";
        tempContact.LatLong = "28.623078,77.319412";
        tempContact.Status = APP_CONST.MEETING_STATUS_NONE;
        dummyList.add(tempContact);

        tempContact = new Contact();
        tempContact.ID =8;
        tempContact.userName = "Praveen Kumar";
        tempContact.MobileNumber = "3331234567";
        tempContact.location = "Noida";
        tempContact.strImage = "";
        tempContact.LatLong = "28.603078,77.339412";
        tempContact.Status = APP_CONST.MEETING_STATUS_NONE;
        dummyList.add(tempContact);

        tempContact = new Contact();
        tempContact.ID =9;
        tempContact.userName = "Azad";
        tempContact.MobileNumber = "4587174567";
        tempContact.location = "Pune";
        tempContact.strImage = "";
        tempContact.LatLong = "28.125978,77.554412";
        tempContact.Status = APP_CONST.MEETING_STATUS_NONE;
        dummyList.add(tempContact);

        tempContact = new Contact();
        tempContact.ID =10;
        tempContact.userName = "Sundeep Agrawal";
        tempContact.MobileNumber = "9913334567";
        tempContact.location = "Kota";
        tempContact.strImage = "";
        tempContact.LatLong = "28.373978,77.534412";
        tempContact.Status = APP_CONST.MEETING_STATUS_NONE;
        dummyList.add(tempContact);

        tempContact = new Contact();
        tempContact.ID =11;
        tempContact.userName = "Kishan Gupta";
        tempContact.MobileNumber = "4567834567";
        tempContact.location = "Agra";
        tempContact.strImage = "";
        tempContact.LatLong = "28.353078,77.389412";
        tempContact.Status = APP_CONST.MEETING_STATUS_NONE;
        dummyList.add(tempContact);

        tempContact = new Contact();
        tempContact.ID =12;
        tempContact.userName = "Vijay Bharadwaj";
        tempContact.MobileNumber = "9831234567";
        tempContact.location = "Noida";
        tempContact.strImage = "";
        tempContact.LatLong = "28.903078,77.359412";
        tempContact.Status = APP_CONST.MEETING_STATUS_NONE;
        dummyList.add(tempContact);

        return dummyList;
    }
}
