package il.co.nolife.locotalk;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.appspot.enhanced_cable_88320.aroundmeapi.model.GeoPt;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.Message;
import com.google.api.client.util.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Victor Belski on 9/8/2015.
 * Designed to accommodate all data base related operation
 */
public class DataAccessObject extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "areaChat";

    public static final String DIRECT_MESSAGES_TABLE = "directMessages";
    public static final String M_KEY = "messageId";
    public static final String M_FROM = "sender";
    public static final String M_CONTENT = "content";
    public static final String M_TIME = "time";

    public static final String LOCATION_MESSAGES_TABLE = "tocationMessages";
    public static final String M_LOC_LAT = "messageLat";
    public static final String M_LOC_LON = "messageLon";

    public static final String FORUMS_TABLE = "forums";
    public static final String F_KEY = "forumId";
    public static final String F_NAME = "name";
    public static final String F_LOC_LAT = "locationLat";
    public static final String F_LOC_LON = "locationLon";

    public static final String FORUM_MESSAGES_TABLE = "forumMessages";
    public static final String FORUM_USERS_TABLE = "forumUsers";

    public static final String FRIENDS_TABLE = "friendsTable";
    public static final String U_MAIL = "mail";
    public static final String U_NAME = "name";
    public static final String U_LOC_LAT = "locationLat";
    public static final String U_LOC_LON = "locationLon";
    public static final String U_SAFE = "safe";

    public DataAccessObject(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createPrivate = "CREATE TABLE " + DIRECT_MESSAGES_TABLE + "("
                + M_KEY + " INTEGER PRIMARY KEY, "
                + M_FROM + " TEXT, "
                + M_CONTENT + " TEXT, "
                + M_TIME + " INTEGER"
                + ")";

        db.execSQL(createPrivate);

        String createPrivateLoc = "CREATE TABLE " + LOCATION_MESSAGES_TABLE + "("
                + M_KEY + " INTEGER PRIMARY KEY, "
                + M_FROM + " TEXT, "
                + M_CONTENT + " TEXT, "
                + M_TIME + " INTEGER, "
                + M_LOC_LAT + " REAL, "
                + M_LOC_LON + " REAL"
                + ")";

        db.execSQL(createPrivateLoc);

        String createForums = "CREATE TABLE " + FORUMS_TABLE + "("
                + F_KEY + " INTEGER PRIMARY KEY, "
                + F_NAME + " TEXT, "
                + F_LOC_LAT + " REAL, "
                + F_LOC_LON + " REAL"
                + ")";

        db.execSQL(createForums);

        String createForumMessages = "CREATE TABLE " + FORUM_MESSAGES_TABLE + "("
                + F_KEY + " INTEGER, "
                + M_KEY + " INTEGER PRIMARY KEY, "
                + M_FROM + " TEXT, "
                + M_CONTENT + " TEXT, "
                + M_TIME + " INTEGER"
                + ")";

        db.execSQL(createForumMessages);

        String forumForumUsers = "CREATE TABLE " + FORUM_USERS_TABLE + "("
                + F_KEY + " INTEGER, "
                + U_MAIL + " TEXT, "
                + U_NAME + " TEXT"
                + ")";

        db.execSQL(forumForumUsers);

        String createFriends = "CREATE TABLE " + FRIENDS_TABLE + "("
                + U_MAIL + " TEXT, "
                + U_NAME + " TEXT, "
                + U_LOC_LAT + " REAL, "
                + U_LOC_LON + " REAL, "
                + U_SAFE + " INTEGER"
                + ")";

        db.execSQL(createFriends);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + DIRECT_MESSAGES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + LOCATION_MESSAGES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + FORUMS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + FORUM_MESSAGES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + FORUM_USERS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + FRIENDS_TABLE);

        onCreate(db);

    }

    public void WriteMessageFromUser(Message message) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(M_FROM, message.getFrom());
        values.put(M_TIME, message.getTimestamp().getValue());
        values.put(M_CONTENT, message.getContnet());

        if(message.getLocation() != null) {

            values.put(M_LOC_LAT, message.getLocation().getLatitude());
            values.put(M_LOC_LON, message.getLocation().getLongitude());
            db.insert(LOCATION_MESSAGES_TABLE, null, values);

        } else {
            db.insert(DIRECT_MESSAGES_TABLE, null, values);
        }

        db.close();

    }

    public Boolean CreateForum(List<LocoUser> uList, GeoPt pt, String name, long forumId) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(F_LOC_LAT, pt.getLatitude());
        values.put(F_LOC_LON, pt.getLongitude());
        values.put(F_KEY, forumId);
        values.put(F_NAME, name);

        long res = db.insert(FORUMS_TABLE, null, values);

        if(res != -1) {

            for (LocoUser u : uList) {

                ContentValues userVal = new ContentValues();
                userVal.put(F_KEY, forumId);
                userVal.put(U_MAIL, u.getMail());
                userVal.put(U_NAME, u.getName());
                db.insert(FORUM_USERS_TABLE, null, userVal);

            }
            return true;

        } else {
            return false;
        }

    }

    public void RemoveUserFromForum(String mail, long forumId) {

        SQLiteDatabase db = this.getWritableDatabase();

        db.rawQuery("DELETE FROM " + FORUM_USERS_TABLE + " WHERE " + U_MAIL + "='" + mail + "' AND " + F_KEY + "=" + forumId, null);

    }

    public void WriteMessageToForum(long forumId, Message message) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(F_KEY, forumId);
        values.put(M_FROM, message.getFrom());
        values.put(M_TIME, message.getTimestamp().getValue());
        values.put(M_CONTENT, message.getContnet());

        db.insert(FORUM_MESSAGES_TABLE, null, values);
        db.close();

    }

    public void AddFriend(LocoUser user) {

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + FRIENDS_TABLE + " WHERE " + U_MAIL + "='" + user.getMail() + "'", null);

        if(cursor.getCount() > 0) {
            Log.e(getClass().toString(), "Trying to insert a user with the same mail");
        } else {

            ContentValues values = new ContentValues();
            values.put(U_MAIL, user.getMail());
            values.put(U_NAME, user.getName());
            values.put(U_LOC_LAT, user.getLocation().getLatitude());
            values.put(U_LOC_LON, user.getLocation().getLongitude());
            values.put(U_SAFE, 0);

            db.insert(FRIENDS_TABLE, null, values);

        }

        cursor.close();

    }

    public void ValidateFriend(String mail) {

        SQLiteDatabase db = this.getWritableDatabase();

        db.rawQuery("UPDATE " + FRIENDS_TABLE + " SET " + U_SAFE + "=1 WHERE " + U_MAIL + "='" + mail + "'", null);

    }

    public List<Message> GetMessagesFromUser(String mail) {

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + DIRECT_MESSAGES_TABLE + " WHERE " + M_FROM + "='" + mail + "'", null);

        List<Message> retVal = new ArrayList<>();

        if(cursor.moveToFirst()) {

            do {

                Message m = new Message();
                m.setTimestamp(new DateTime(cursor.getLong(cursor.getColumnIndex(M_TIME))));
                m.setContnet(cursor.getString(cursor.getColumnIndex(M_CONTENT)));
                retVal.add(m);

            } while(cursor.moveToNext());

        }

        cursor.close();

        return retVal;

    }

    public List<Message> GetLocationMessagesFromUser(String mail) {

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + LOCATION_MESSAGES_TABLE + " WHERE " + M_FROM + "='" + mail + "'", null);

        List<Message> retVal = new ArrayList<>();

        if(cursor.moveToFirst()) {

            do {

                Message m = new Message();
                m.setTimestamp(new DateTime(cursor.getLong(cursor.getColumnIndex(M_TIME))));
                m.setContnet(cursor.getString(cursor.getColumnIndex(M_CONTENT)));
                GeoPt l = new GeoPt();
                l.setLatitude(cursor.getFloat(cursor.getColumnIndex(M_LOC_LAT)));
                l.setLongitude(cursor.getFloat(cursor.getColumnIndex(M_LOC_LON)));
                m.setLocation(l);
                retVal.add(m);

            } while(cursor.moveToNext());

        }

        cursor.close();

        return retVal;

    }

    public List<Message> GetAllMessagesFromForum(long forumId) {

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + FORUM_MESSAGES_TABLE + " WHERE " + F_KEY + "=" + forumId, null);

        List<Message> retVal = new ArrayList<>();

        if(cursor.moveToFirst()) {

            do {

                Message m = new Message();
                m.setTimestamp(new DateTime(cursor.getLong(cursor.getColumnIndex(M_TIME))));
                m.setContnet(cursor.getString(cursor.getColumnIndex(M_CONTENT)));
                m.setFrom(cursor.getString(cursor.getColumnIndex(M_FROM)));
                retVal.add(m);

            } while(cursor.moveToNext());

        }

        cursor.close();

        return retVal;

    }

    public List<LocoUser> GetAllFriends() {

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + FRIENDS_TABLE, null);

        List<LocoUser> retVal = new ArrayList<>();

        if(cursor.moveToFirst()) {

            do {

                LocoUser u = new LocoUser();
                GeoPt loc = new GeoPt();
                loc.setLatitude(cursor.getFloat(cursor.getColumnIndex(U_LOC_LAT)));
                loc.setLongitude(cursor.getFloat(cursor.getColumnIndex(U_LOC_LON)));
                u.setLocation(loc);
                u.setMail(cursor.getString(cursor.getColumnIndex(U_MAIL)));
                u.setName(cursor.getString(cursor.getColumnIndex(U_NAME)));

                retVal.add(u);

            } while(cursor.moveToNext());

        }

        cursor.close();

        return retVal;

    }

    public List<Forum> GetAllForums() {

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + FORUMS_TABLE, null);
        List<Forum> retVal = new ArrayList<>();

        if(cursor.moveToFirst()) {

            do {

                Forum f = new Forum();
                f.setForumId(cursor.getLong(cursor.getColumnIndex(F_KEY)));
                GeoPt l = new GeoPt();
                l.setLatitude(cursor.getFloat(cursor.getColumnIndex(F_LOC_LAT)));
                l.setLongitude(cursor.getFloat(cursor.getColumnIndex(F_LOC_LON)));
                f.setLoc(l);
                f.setName(cursor.getString(cursor.getColumnIndex(F_NAME)));

                Cursor users = db.rawQuery("SELECT * FROM " + FORUM_USERS_TABLE + " WHERE " + F_KEY + "=" + f.getForumId(), null);
                List<LocoUser> uList = new ArrayList<>();

                if(users.moveToFirst()) {

                    do {

                        LocoUser u = new LocoUser();
                        u.setMail(users.getString(users.getColumnIndex(U_MAIL)));
                        u.setName(users.getString(users.getColumnIndex(U_NAME)));
                        uList.add(u);


                    } while(users.moveToNext());

                }

                users.close();
                f.setUsers(uList);
                retVal.add(f);

            } while(cursor.moveToNext());

        }

        cursor.close();

        return retVal;

    }

}
