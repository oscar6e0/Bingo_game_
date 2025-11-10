package ict.mgame.bingogame;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DBHelper class manages the SQLite database for the app.
 * It creates the "gameinfo" table, handles upgrades, and provides methods for inserting and querying game data.
 */
public class DBHelper extends SQLiteOpenHelper {

    // Database name
    private static final String DATABASE_NAME = "bingo.db";
    // Database version (increment for schema changes)
    private static final int DATABASE_VERSION = 1;

    // Table name
    public static final String TABLE_GAMEINFO = "gameinfo";
    // Column names
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_ROUND = "round_number";
    public static final String COLUMN_WINNING_NUMBERS = "winning_numbers";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    /**
     * Constructor for DBHelper.
     * @param context Application context
     */
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the gameinfo table with specified columns
        String createTable = "CREATE TABLE " + TABLE_GAMEINFO + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT, " +
                COLUMN_ROUND + " INTEGER, " +
                COLUMN_WINNING_NUMBERS + " TEXT, " +
                COLUMN_TIMESTAMP + " INTEGER)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop table if exists and recreate on version upgrade
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GAMEINFO);
        onCreate(db);
    }

    /**
     * Inserts a GameInfo object into the database.
     * @param gameInfo The GameInfo to insert
     */
    public void insertGameInfo(GameInfo gameInfo) {
        // Get writable database
        SQLiteDatabase db = this.getWritableDatabase();
        // Map GameInfo fields to ContentValues
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, gameInfo.getUsername());
        values.put(COLUMN_ROUND, gameInfo.getRound());
        values.put(COLUMN_WINNING_NUMBERS, gameInfo.getWinningNumbers());
        values.put(COLUMN_TIMESTAMP, gameInfo.getTimestamp());
        // Insert values into table
        db.insert(TABLE_GAMEINFO, null, values);
        db.close();
    }

    /**
     * Retrieves all game info records, sorted by timestamp descending.
     * @return Cursor with query results
     */
    public Cursor getAllGameInfo() {
        // Get readable database
        SQLiteDatabase db = this.getReadableDatabase();
        // Query all columns, sorted by timestamp DESC
        return db.query(TABLE_GAMEINFO, null, null, null, null, null, COLUMN_TIMESTAMP + " DESC");  // Sorted by newest first
    }
}