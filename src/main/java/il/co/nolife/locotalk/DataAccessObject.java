package il.co.nolife.locotalk;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.appspot.enhanced_cable_88320.aroundmeapi.model.GeoPt;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.Message;
import com.google.api.client.util.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import il.co.nolife.locotalk.DataTypes.LocoEvent;
import il.co.nolife.locotalk.DataTypes.LocoForum;
import il.co.nolife.locotalk.DataTypes.LocoUser;

/**
 * Created by Victor Belski on 9/8/2015.
 * Designed to accommodate all data base related operation
 */
public class DataAccessObject extends SQLiteOpenHelper {

    public static final String TAG = "DataAccessObject";

    public static final int DATABASE_VERSION = 13;
    public static final String DATABASE_NAME = "areaChat";

    public static final String CONVERSATION_TABLE = "conversations";
    public static final String C_KEY = "conversationId";

    public static final String LOC_LAT = "latitude";
    public static final String LOC_LON = "longitude";
    public static final String OWNER = "owner";
    public static final String NAME = "name";
    public static final String RADIUS = "radius";
    public static final String DELETED = "deleted";

    public static final String MESSAGE_CONVERSATION = "messageConversation";
    public static final String M_FROM = "sender";
    public static final String M_CONTENT = "content";
    public static final String M_TIME = "time";

    public static final String DIRECT_CONVERSATIONS_TABLE = "directConversations";

    public static final String FORUMS_TABLE = "forums";
    public static final String F_KEY = "forumId";

    public static final String FORUM_USERS_TABLE = "forumUsers";

    public static final String EVENTS_TABLE = "events";
    public static final String E_KEY = "eventId";

    public static final String USERS_TABLE = "users";
    public static final String U_MAIL = "mail";
    public static final String U_SAFE = "safe";
    public static final String U_FRIEND = "friend";
    public static final String U_IMAGE = "icon";

    public static final String EVENT_COLLISIONS_TABLE = "eventCollisions";
    public static final String FORUM_COLLISIONS_TABLE = "forumCollisions";
    public static final String COL_FROM = "colFrom";
    public static final String COL_TO = "colTo";

    Context context;

    public DataAccessObject(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createConversations = "CREATE TABLE " + CONVERSATION_TABLE + " ("
                + C_KEY + " INTEGER PRIMARY KEY)";

        db.execSQL(createConversations);

        String msgConvConnector = "CREATE TABLE " + MESSAGE_CONVERSATION + " ("
                + C_KEY + " INTEGER, "
                + M_FROM + " TEXT, "
                + M_TIME + " INTEGER, "
                + M_CONTENT + " TEXT)";

        db.execSQL(msgConvConnector);

        String createPrivate = "CREATE TABLE " + DIRECT_CONVERSATIONS_TABLE + " ("
                + M_FROM + " TEXT, "
                + C_KEY + " INTEGER)";

        db.execSQL(createPrivate);

        String createForums = "CREATE TABLE " + FORUMS_TABLE + " ("
                + F_KEY + " INTEGER PRIMARY KEY, "
                + NAME + " TEXT, "
                + OWNER + " TEXT, "
                + C_KEY + " INTEGER, "
                + LOC_LAT + " REAL, "
                + LOC_LON + " REAL, "
                + DELETED + " INTEGER)";

        db.execSQL(createForums);

        String forumUsersConnector = "CREATE TABLE " + FORUM_USERS_TABLE + " ("
                + F_KEY + " INTEGER, "
                + U_MAIL + " TEXT, "
                + NAME + " TEXT)";

        db.execSQL(forumUsersConnector);

        String createEvents = "CREATE TABLE " + EVENTS_TABLE + " ("
                + E_KEY + " INTEGER PRIMARY KEY, "
                + NAME + " TEXT, "
                + OWNER + " TEXT, "
                + C_KEY + " INTEGER, "
                + RADIUS + " INTEGER, "
                + LOC_LAT + " REAL, "
                + LOC_LON + " REAL, "
                + DELETED + " INTEGER)";

        db.execSQL(createEvents);

        String createFriends = "CREATE TABLE " + USERS_TABLE + " ("
                + U_MAIL + " TEXT, "
                + NAME + " TEXT, "
                + U_IMAGE + " TEXT, "
                + LOC_LAT + " REAL, "
                + LOC_LON + " REAL, "
                + U_SAFE + " INTEGER, "
                + U_FRIEND + " INTEGER)";

        db.execSQL(createFriends);

        String createForumCollisions = "CREATE TABLE " + FORUM_COLLISIONS_TABLE + " ("
                + COL_FROM + " INTEGER, "
                + OWNER + " TEXT, "
                + COL_TO + " INTEGER)";

        db.execSQL(createForumCollisions);

        String createEventCollisions = "CREATE TABLE " + EVENT_COLLISIONS_TABLE + " ("
                + COL_FROM + " INTEGER, "
                + OWNER + " TEXT, "
                + COL_TO + " INTEGER)";

        db.execSQL(createEventCollisions);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + CONVERSATION_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + MESSAGE_CONVERSATION);
        db.execSQL("DROP TABLE IF EXISTS " + DIRECT_CONVERSATIONS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + FORUMS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + FORUM_USERS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + EVENTS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + FORUM_COLLISIONS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + EVENT_COLLISIONS_TABLE);

        onCreate(db);

    }

    void WriteMessageToConversation(Message message, long convId) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(M_FROM, message.getFrom());
        values.put(M_TIME, message.getTimestamp().getValue());
        values.put(M_CONTENT, message.getContnet());
        values.put(C_KEY, convId);

        if(db.insert(MESSAGE_CONVERSATION, null, values) == -1) {
            Log.e(TAG, "Failed to insert message into conversation table");
        } else {

        }

    }

    public long GetUserConversation(String mail) {

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + DIRECT_CONVERSATIONS_TABLE + " WHERE " + M_FROM + "='" + mail + "'", null);

        long retVal = -1;
        if(cursor.moveToFirst()) {
            retVal = cursor.getLong(cursor.getColumnIndex(C_KEY));
        }

        cursor.close();

        return retVal;

    }

    public List<Message> GetMessagesFromConversation(long convId) {

        SQLiteDatabase db = this.getWritableDatabase();

        List<Message> retVal = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT * FROM " + MESSAGE_CONVERSATION + " WHERE " + C_KEY + "=" + convId, null);

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

    public void WriteMessageToUserConversation(Message message, boolean myMessage) {

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor conversation = db.rawQuery("SELECT * FROM " + DIRECT_CONVERSATIONS_TABLE + " WHERE " + M_FROM + "='" + ((myMessage) ? (message.getTo()) : (message.getFrom())) + "'", null);

        if(conversation.moveToFirst()) {

            conversation.moveToFirst();
            WriteMessageToConversation(message, conversation.getLong(conversation.getColumnIndex(C_KEY)));

        } else {

            long convId = db.insert(CONVERSATION_TABLE, C_KEY, null);
            WriteMessageToConversation(message, convId);

            ContentValues newUserConv = new ContentValues();
            newUserConv.put(M_FROM, ((myMessage)?(message.getTo()):(message.getFrom())));
            newUserConv.put(C_KEY, convId);
            db.insert(DIRECT_CONVERSATIONS_TABLE, null, newUserConv);

        }

        conversation.close();

        AppController.NewPrivateMessage(message);

    }

    public boolean CreateOwnedForum(List<LocoUser> uList, GeoPt pt, String name, String owner, long forumId) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(LOC_LAT, pt.getLatitude());
        values.put(LOC_LON, pt.getLongitude());
        values.put(F_KEY, forumId);
        values.put(NAME, name);
        values.put(OWNER, owner);
        values.put(DELETED, 0);

        long convId = db.insert(CONVERSATION_TABLE, C_KEY, null);
        values.put(C_KEY, convId);

        long res = db.insert(FORUMS_TABLE, null, values);

        if(res != -1) {

            for (LocoUser u : uList) {

                ContentValues userVal = new ContentValues();
                userVal.put(F_KEY, forumId);
                userVal.put(U_MAIL, u.getMail());
                userVal.put(NAME, u.getName());
                db.insert(FORUM_USERS_TABLE, null, userVal);

            }

            return true;

        } else {
            return false;
        }

    }

    public Long CreateUnownedForum(List<LocoUser> uList, GeoPt pt, String name, String owner, long forumId) {

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor created = db.rawQuery("SELECT * FROM " + FORUM_COLLISIONS_TABLE + " WHERE " + COL_FROM + "=" + forumId, null);

        if(created.moveToFirst()) {

            do {
                if(created.getString(created.getColumnIndex(OWNER)).compareTo(owner) == 0) {
                    // This forum already exists
                    return null;
                }
            } while(created.moveToNext());

        }

        created.close();
        created = db.rawQuery("SELECT * FROM " + FORUMS_TABLE + " WHERE " + F_KEY + "=" + forumId, null);
        if(created.moveToFirst()) {
            if(created.getString(created.getColumnIndex(OWNER)).compareTo(owner) == 0) {
                // This forum already exists
                return null;
            }
        }
        created.close();

        ContentValues values = new ContentValues();
        values.put(LOC_LAT, pt.getLatitude());
        values.put(LOC_LON, pt.getLongitude());
        values.put(F_KEY, forumId);
        values.put(NAME, name);
        values.put(OWNER, owner);
        values.put(DELETED, 0);

        long convId = db.insert(CONVERSATION_TABLE, C_KEY, null);
        values.put(C_KEY, convId);

        long res = db.insert(FORUMS_TABLE, null, values);

        if(res != -1) {

            for (LocoUser u : uList) {

                ContentValues userVal = new ContentValues();
                userVal.put(F_KEY, forumId);
                userVal.put(U_MAIL, u.getMail());
                userVal.put(NAME, u.getName());
                db.insert(FORUM_USERS_TABLE, null, userVal);

            }

        } else {

            values.remove(F_KEY);

            long newId = db.insert(FORUMS_TABLE, null, values);

            for (LocoUser u : uList) {

                ContentValues userVal = new ContentValues();
                userVal.put(F_KEY, newId);
                userVal.put(U_MAIL, u.getMail());
                userVal.put(NAME, u.getName());
                db.insert(FORUM_USERS_TABLE, null, userVal);

            }

            ContentValues collision = new ContentValues();
            collision.put(COL_FROM, forumId);
            collision.put(OWNER, owner);
            collision.put(COL_TO, newId);

            db.insert(FORUM_COLLISIONS_TABLE, null, collision);
            res = newId;

        }

        return res;

    }

    public void RemoveUserFromForum(LocoForum forum, String user) {

        SQLiteDatabase db =this.getWritableDatabase();

        Cursor collision = db.rawQuery("SELECT * FROM " + FORUM_COLLISIONS_TABLE + " WHERE " + COL_FROM + "=" + forum.getId(), null);

        if(collision.moveToFirst()) {

            do {

                if (collision.getString(collision.getColumnIndex(OWNER)).compareTo(forum.getOwner()) == 0) {

                    long resolvedForumId = collision.getLong(collision.getColumnIndex(COL_TO));
                    db.delete(FORUM_USERS_TABLE, F_KEY + "=" + resolvedForumId + " AND " + U_MAIL + "='" + user + "'", null);
                    break;

                }

            } while(collision.moveToNext());

        } else {

            db.delete(FORUM_USERS_TABLE, F_KEY + "=" + forum.getId() + " AND " + U_MAIL + "='" + user + "'", null);

        }

        collision.close();

    }

    public void WriteMessageToForum(long forumId, String owner, Message message) {

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor collision = db.rawQuery("SELECT * FROM " + FORUM_COLLISIONS_TABLE + " WHERE " + COL_FROM + "=" + forumId, null);

        if(collision.moveToFirst()) {

            do {

                if (collision.getString(collision.getColumnIndex(OWNER)).compareTo(owner) == 0) {

                    long resolvedForumId = collision.getLong(collision.getColumnIndex(COL_TO));
                    Cursor forum = db.rawQuery("SELECT * FROM " + FORUMS_TABLE + " WHERE " + F_KEY + "=" + resolvedForumId, null);
                    if(forum.moveToFirst()) {
                        WriteMessageToConversation(message, forum.getLong(forum.getColumnIndex(C_KEY)));
                        AppController.NewForumMessage(resolvedForumId);
                        break;
                    }

                    forum.close();

                }

            } while(collision.moveToNext());

        } else {

            Cursor forum = db.rawQuery("SELECT * FROM " + FORUMS_TABLE + " WHERE " + F_KEY + "=" + forumId, null);

            if(forum.moveToFirst()) {

                WriteMessageToConversation(message, forum.getLong(forum.getColumnIndex(C_KEY)));
                AppController.NewForumMessage(forumId);

            }

            forum.close();

        }

        collision.close();

    }

    public void WriteMessageToForum(LocoForum forum, Message message) {
        WriteMessageToForum(forum.getId(), forum.getOwner(), message);
    }

    public boolean CreateOwnedEvent(GeoPt loc, String name, String owner, int radius, long eventId) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(LOC_LAT, loc.getLatitude());
        values.put(LOC_LON, loc.getLongitude());
        values.put(E_KEY, eventId);
        values.put(NAME, name);
        values.put(OWNER, owner);
        values.put(RADIUS, radius);
        values.put(DELETED, 0);

        long convId = db.insert(CONVERSATION_TABLE, C_KEY, null);
        values.put(C_KEY, convId);

        long res = db.insert(EVENTS_TABLE, null, values);

        if(res != -1) {
            return true;
        } else {
            return false;
        }

    }

    public Long CreateUnownedEvent(GeoPt loc, String name, String owner, int radius, long eventId) {

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor created = db.rawQuery("SELECT * FROM " + EVENT_COLLISIONS_TABLE + " WHERE " + COL_FROM + "=" + eventId, null);

        if(created.moveToFirst()) {

            do {
                if(created.getString(created.getColumnIndex(OWNER)).compareTo(owner) == 0) {
                    // This event already exists
                    return null;
                }
            } while(created.moveToNext());

        }

        created.close();
        created = db.rawQuery("SELECT * FROM " + EVENTS_TABLE + " WHERE " + E_KEY + "=" + eventId, null);
        if(created.moveToFirst()) {
            if(created.getString(created.getColumnIndex(OWNER)).compareTo(owner) == 0) {
                // This event already exists
                return null;
            }
        }

        created.close();

        ContentValues values = new ContentValues();
        values.put(LOC_LAT, loc.getLatitude());
        values.put(LOC_LON, loc.getLongitude());
        values.put(E_KEY, eventId);
        values.put(NAME, name);
        values.put(OWNER, owner);
        values.put(RADIUS, radius);
        values.put(DELETED, 0);

        long convId = db.insert(CONVERSATION_TABLE, C_KEY, null);
        values.put(C_KEY, convId);

        long res = db.insert(EVENTS_TABLE, null, values);

        if(res == -1) {

            values.remove(E_KEY);

            long newId = db.insert(EVENTS_TABLE, null, values);

            ContentValues collision = new ContentValues();
            collision.put(COL_FROM, eventId);
            collision.put(OWNER, owner);
            collision.put(COL_TO, newId);

            db.insert(EVENT_COLLISIONS_TABLE, null, collision);
            res = newId;

        }

        return res;

    }

    public void WriteMessageToEvent(long eventId, String name, String owner, GeoPt loc, int radius, Message message) {

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor collision = db.rawQuery("SELECT * FROM " + EVENT_COLLISIONS_TABLE + " WHERE " + COL_FROM + "=" + eventId, null);

        if(collision.moveToFirst()) {

            do {

                long collidingForumId = collision.getLong(collision.getColumnIndex(COL_FROM));
                if(collidingForumId == eventId) {

                    if (collision.getString(collision.getColumnIndex(OWNER)).compareTo(owner) == 0) {

                        long resolvedEventId = collision.getLong(collision.getColumnIndex(COL_TO));
                        Cursor event = db.rawQuery("SELECT * FROM " + EVENTS_TABLE + " WHERE " + E_KEY + "=" + resolvedEventId, null);
                        if(event.moveToFirst()) {
                            WriteMessageToConversation(message, event.getLong(event.getColumnIndex(C_KEY)));
                            AppController.NewEventMessage(resolvedEventId);

                            ContentValues undelete = new ContentValues();
                            undelete.put(DELETED, 0);

                            db.update(EVENTS_TABLE, undelete, E_KEY + "=" + event.getLong(event.getColumnIndex(E_KEY)), null);

                            break;
                        }

                        event.close();

                    }

                }

            } while(collision.moveToNext());

        } else {

            Cursor event = db.rawQuery("SELECT * FROM " + EVENTS_TABLE + " WHERE " + E_KEY + "=" + eventId, null);

            if(event.moveToFirst()) {

                WriteMessageToConversation(message, event.getLong(event.getColumnIndex(C_KEY)));

                ContentValues undelete = new ContentValues();
                undelete.put(DELETED, 0);

                db.update(EVENTS_TABLE, undelete, E_KEY + "=" + event.getLong(event.getColumnIndex(E_KEY)), null);

                AppController.NewEventMessage(eventId);

            } else {

                Long newEvent = CreateUnownedEvent(loc, name, owner, radius, eventId);
                if(newEvent != null) {
                    WriteMessageToEvent(eventId, name, owner, loc, radius, message);
                } else {
                    Log.e(TAG,"An unknown error occurred trying to create event during event message writing");
                }

            }

            event.close();

        }

        collision.close();

    }

    public void WriteMessageToEvent(LocoEvent event, Message message) {
        WriteMessageToEvent(event.getId(), event.getName(), event.getOwner(), event.getLocation(), event.getRadius(), message);
    }

    public void AddUser(LocoUser user) {

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + USERS_TABLE + " WHERE " + U_MAIL + "='" + user.getMail() + "'", null);

        if(cursor.getCount() > 0) {
            Log.e(getClass().toString(), "Trying to insert a user with the same mail");
        } else {

            ContentValues values = new ContentValues();
            values.put(U_MAIL, user.getMail());
            values.put(NAME, user.getName());
            values.put(LOC_LAT, user.getLocation().getLatitude());
            values.put(LOC_LON, user.getLocation().getLongitude());
            values.put(U_SAFE, user.getSafe());
            values.put(U_FRIEND, user.getFriend());
            values.put(U_IMAGE, user.getIcon());

            db.insert(USERS_TABLE, null, values);

        }

        cursor.close();

    }

    public void AddUsers(Collection<LocoUser> users) {

        SQLiteDatabase db = this.getWritableDatabase();

        for (LocoUser user : users) {

            ContentValues values = new ContentValues();
            values.put(U_MAIL, user.getMail());
            values.put(NAME, user.getName());
            values.put(LOC_LAT, user.getLocation().getLatitude());
            values.put(LOC_LON, user.getLocation().getLongitude());
            values.put(U_SAFE, user.getSafe());
            values.put(U_FRIEND, user.getFriend());
            values.put(U_IMAGE, user.getIcon());

            db.insert(USERS_TABLE, null, values);

        }

    }

    public void ValidateUser(String mail) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(U_SAFE, 1);

        db.update(USERS_TABLE, values, U_MAIL + "='" + mail + "'", null);
        Log.i(getClass().toString(), "Validating user:" + mail);

    }

    public void AddUserToFriends(String mail) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(U_FRIEND, 1);

        db.update(USERS_TABLE, values, U_MAIL + "='" + mail + "'", null);

    }

    public void RemoveUserFromFriends(String mail) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(U_FRIEND, 0);

        db.update(USERS_TABLE, values, U_MAIL + "='" + mail + "'", null);

    }

    public List<LocoUser> GetAllUsers() {

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + USERS_TABLE, null);

        List<LocoUser> retVal = new ArrayList<>();

        if(cursor.moveToFirst()) {

            do {

                LocoUser user = new LocoUser();
                GeoPt loc = new GeoPt();
                loc.setLatitude(cursor.getFloat(cursor.getColumnIndex(LOC_LAT)));
                loc.setLongitude(cursor.getFloat(cursor.getColumnIndex(LOC_LON)));
                user.setLocation(loc);
                user.setMail(cursor.getString(cursor.getColumnIndex(U_MAIL)));
                user.setName(cursor.getString(cursor.getColumnIndex(NAME)));
                user.setIcon(cursor.getString(cursor.getColumnIndex(U_IMAGE)));
                int friend = cursor.getInt(cursor.getColumnIndex(U_FRIEND));
                if(friend == 1) {
                    user.setFriend(true);
                }
                int safe = cursor.getInt(cursor.getColumnIndex(U_SAFE));
                if(safe == 1) {
                    user.setSafe(true);
                }

                retVal.add(user);

            } while(cursor.moveToNext());

        }

        cursor.close();

        return retVal;

    }

    public List<Message> GetAllMessagesFromForum(long forumId) {

        SQLiteDatabase db = this.getWritableDatabase();

        List<Message> retVal = new ArrayList<>();
        Cursor forum = db.rawQuery("SELECT * FROM " + FORUMS_TABLE + " WHERE " + F_KEY + "=" + forumId, null);

        if(forum.moveToFirst()) {
            retVal = GetMessagesFromConversation(forum.getLong(forum.getColumnIndex(C_KEY)));
        }

        forum.close();

        return retVal;

    }

    public List<Message> GetAllMessagesFromEvent(long eventId) {

        SQLiteDatabase db = this.getWritableDatabase();

        List<Message> retVal = new ArrayList<>();
        Cursor forum = db.rawQuery("SELECT * FROM " + EVENTS_TABLE + " WHERE " + E_KEY + "=" + eventId, null);

        if(forum.moveToFirst()) {
            retVal = GetMessagesFromConversation(forum.getLong(forum.getColumnIndex(C_KEY)));
        }

        forum.close();

        return retVal;

    }

    public List<LocoUser> GetAllSafeUsers() {

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + USERS_TABLE + " WHERE " + U_SAFE + "=1", null);

        List<LocoUser> retVal = new ArrayList<>();

        if(cursor.moveToFirst()) {

            do {

                LocoUser u = new LocoUser();
                GeoPt loc = new GeoPt();
                loc.setLatitude(cursor.getFloat(cursor.getColumnIndex(LOC_LAT)));
                loc.setLongitude(cursor.getFloat(cursor.getColumnIndex(LOC_LON)));
                u.setLocation(loc);
                u.setMail(cursor.getString(cursor.getColumnIndex(U_MAIL)));
                u.setName(cursor.getString(cursor.getColumnIndex(NAME)));
                u.setIcon(cursor.getString(cursor.getColumnIndex(U_IMAGE)));
                int friend = cursor.getInt(cursor.getColumnIndex(U_FRIEND));
                if(friend == 1) {
                    u.setFriend(true);
                }
                u.setSafe(true);

                retVal.add(u);

            } while(cursor.moveToNext());

        }

        cursor.close();

        return retVal;

    }

    public List<LocoUser> GetAllFriends() {

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + USERS_TABLE + " WHERE " + U_FRIEND + "=1", null);

        List<LocoUser> retVal = new ArrayList<>();

        if(cursor.moveToFirst()) {

            do {

                LocoUser u = new LocoUser();
                GeoPt loc = new GeoPt();
                loc.setLatitude(cursor.getFloat(cursor.getColumnIndex(LOC_LAT)));
                loc.setLongitude(cursor.getFloat(cursor.getColumnIndex(LOC_LON)));
                u.setLocation(loc);
                u.setMail(cursor.getString(cursor.getColumnIndex(U_MAIL)));
                u.setName(cursor.getString(cursor.getColumnIndex(NAME)));
                u.setIcon(cursor.getString(cursor.getColumnIndex(U_IMAGE)));
                int safe = cursor.getInt(cursor.getColumnIndex(U_SAFE));
                if(safe != 0) {
                    u.setSafe(true);
                }
                u.setFriend(true);

                retVal.add(u);

            } while(cursor.moveToNext());

        }

        cursor.close();

        return retVal;

    }

    public List<LocoForum> GetAllForums() {

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + FORUMS_TABLE + " WHERE " + DELETED + "=0", null);
        List<LocoForum> retVal = new ArrayList<>();

        if(cursor.moveToFirst()) {

            do {

                LocoForum forum = new LocoForum();
                forum.setId(cursor.getLong(cursor.getColumnIndex(F_KEY)));
                GeoPt location = new GeoPt();
                location.setLatitude(cursor.getFloat(cursor.getColumnIndex(LOC_LAT)));
                location.setLongitude(cursor.getFloat(cursor.getColumnIndex(LOC_LON)));
                forum.setLocation(location);
                forum.setName(cursor.getString(cursor.getColumnIndex(NAME)));
                forum.setOwner(cursor.getString(cursor.getColumnIndex(OWNER)));
                forum.setConversation(cursor.getLong(cursor.getColumnIndex(C_KEY)));

                Cursor users = db.rawQuery("SELECT * FROM " + FORUM_USERS_TABLE + " WHERE " + F_KEY + "=" + forum.getId(), null);
                List<LocoUser> uList = new ArrayList<>();

                if(users.moveToFirst()) {

                    do {

                        LocoUser u = new LocoUser();
                        u.setMail(users.getString(users.getColumnIndex(U_MAIL)));
                        u.setName(users.getString(users.getColumnIndex(NAME)));
                        uList.add(u);


                    } while(users.moveToNext());

                }

                users.close();
                forum.setUsers(uList);
                retVal.add(forum);

            } while(cursor.moveToNext());

        }

        cursor.close();
        return retVal;

    }

    public List<LocoEvent> GetAllEvents() {

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + EVENTS_TABLE + " WHERE " + DELETED + "=0", null);
        List<LocoEvent> retVal = new ArrayList<>();

        if(cursor.moveToFirst()) {

            do {

                DatabaseUtils.dumpCurrentRow(cursor);

                LocoEvent event = new LocoEvent();
                event.setId(cursor.getLong(cursor.getColumnIndex(E_KEY)));
                GeoPt location = new GeoPt();
                location.setLatitude(cursor.getFloat(cursor.getColumnIndex(LOC_LAT)));
                location.setLongitude(cursor.getFloat(cursor.getColumnIndex(LOC_LON)));
                event.setLocation(location);
                event.setName(cursor.getString(cursor.getColumnIndex(NAME)));
                event.setOwner(cursor.getString(cursor.getColumnIndex(OWNER)));
                event.setConversation(cursor.getLong(cursor.getColumnIndex(C_KEY)));
                event.setRadius(cursor.getInt(cursor.getColumnIndex(RADIUS)));

                retVal.add(event);

            } while(cursor.moveToNext());

        }

        cursor.close();
        return retVal;

    }

    public List<Message> GetMessagesFromDirectConversation(String mail) {

        SQLiteDatabase db = this.getWritableDatabase();

        List<Message> retVal = new ArrayList<>();
        Cursor directCov = db.rawQuery("SELECT * FROM " + DIRECT_CONVERSATIONS_TABLE + " WHERE " + M_FROM + "='" + mail + "'", null);

        if(directCov.moveToFirst()) {
            retVal = GetMessagesFromConversation(directCov.getLong(directCov.getColumnIndex(C_KEY)));
        }

        directCov.close();
        return retVal;

    }

    public LocoForum GetForum(long forumId) {

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + FORUMS_TABLE + " WHERE " + F_KEY + "=" + forumId, null);

        LocoForum newForum = null;

        if(cursor.moveToFirst()) {

            newForum = new LocoForum();
            newForum.setId(cursor.getLong(cursor.getColumnIndex(F_KEY)));
            GeoPt location = new GeoPt();
            location.setLatitude(cursor.getFloat(cursor.getColumnIndex(LOC_LAT)));
            location.setLongitude(cursor.getFloat(cursor.getColumnIndex(LOC_LON)));
            newForum.setLocation(location);
            newForum.setName(cursor.getString(cursor.getColumnIndex(NAME)));
            newForum.setOwner(cursor.getString(cursor.getColumnIndex(OWNER)));
            newForum.setConversation(cursor.getLong(cursor.getColumnIndex(C_KEY)));

            Cursor users = db.rawQuery("SELECT * FROM " + FORUM_USERS_TABLE + " WHERE " + F_KEY + "=" + newForum.getId(), null);
            List<LocoUser> uList = new ArrayList<>();

            if(users.moveToFirst()) {

                do {

                    LocoUser u = new LocoUser();
                    u.setMail(users.getString(users.getColumnIndex(U_MAIL)));
                    u.setName(users.getString(users.getColumnIndex(NAME)));
                    uList.add(u);


                } while(users.moveToNext());

            }

            users.close();
            newForum.setUsers(uList);

        }

        cursor.close();

        return newForum;

    }

    public void RemoveForum(LocoForum forum) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DELETED, 1);

        db.update(FORUMS_TABLE, values, F_KEY + "=" + forum.getId(), null);

    }

    public LocoEvent GetEvent(long eventId) {

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + EVENTS_TABLE + " WHERE " + E_KEY + "=" + eventId, null);

        LocoEvent newEvent = null;

        if(cursor.moveToFirst()) {

            newEvent = new LocoEvent();
            newEvent.setId(cursor.getLong(cursor.getColumnIndex(E_KEY)));
            GeoPt location = new GeoPt();
            location.setLatitude(cursor.getFloat(cursor.getColumnIndex(LOC_LAT)));
            location.setLongitude(cursor.getFloat(cursor.getColumnIndex(LOC_LON)));
            newEvent.setLocation(location);
            newEvent.setName(cursor.getString(cursor.getColumnIndex(NAME)));
            newEvent.setOwner(cursor.getString(cursor.getColumnIndex(OWNER)));
            newEvent.setConversation(cursor.getLong(cursor.getColumnIndex(C_KEY)));
            newEvent.setRadius(cursor.getInt(cursor.getColumnIndex(RADIUS)));

        }

        cursor.close();

        return newEvent;

    }

    public void RemoveEvent(LocoEvent event) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DELETED, 1);

        db.update(EVENTS_TABLE, values, E_KEY + "=" + event.getId(), null);

    }

    public void UpdateUsersLocation(Collection<LocoUser> users) {

        SQLiteDatabase db = this.getWritableDatabase();

        for (LocoUser user : users) {

            ContentValues values = new ContentValues();

            values.put(LOC_LAT, user.getLocation().getLatitude());
            values.put(LOC_LON, user.getLocation().getLongitude());

            db.update(USERS_TABLE, values, U_MAIL + "='" + user.getMail() + "'", null);

        }

    }

}
