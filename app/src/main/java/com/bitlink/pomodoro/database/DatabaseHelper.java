package com.bitlink.pomodoro.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.bitlink.pomodoro.database.model.Item;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "item";
    //private static final String DATABASE_PATH = ApplicationContextSingleton.getApplicationContext().getFilesDir().getAbsolutePath() + "/";
    private static final int DATABASE_VERSION = 1;
    static final String destDir = "/data/data/com.bitlink.countdown/databases/";
    static final String destPath = destDir + DATABASE_NAME;

    private static final String TABLE_ITEMS = "item";
    private static final String ITEM_ID = "ItemId";
    private static final String ITEM_TITLE = "ItemTitle";
    private static final String ITEM_WORK_DURATION = "ItemWorkDuration";
    private static final String ITEM_BREAK_DURATION = "ItemBreakDuration";
    private static final String ITEM_LONG_BREAK_DURATION = "ItemLongBreakDuration";
    private static final String ITEM_WORK_COLOR = "ItemWorkColor";
    private static final String ITEM_BREAK_COLOR = "ItemBreakColor";
    private static final String ITEM_LONG_BREAK_COLOR = "ItemLongBreakColor";
    private static final String ITEM_TOTAL_SESSION = "ItemTotalSession";
    //    private static final String ADDED_DATE = "AddedDate";
    private static final String[] ITEM_COLUMNS = new String[]{
            ITEM_ID, ITEM_TITLE, ITEM_WORK_DURATION, ITEM_BREAK_DURATION, ITEM_LONG_BREAK_DURATION,
            ITEM_WORK_COLOR, ITEM_BREAK_COLOR, ITEM_LONG_BREAK_COLOR, ITEM_TOTAL_SESSION};

    final Context context;
    SQLiteDatabase db;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_ITEM_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_ITEMS + "("
                + ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ITEM_TITLE + " TEXT,"
                + ITEM_WORK_DURATION + " INTEGER,"
                + ITEM_BREAK_DURATION + " INTEGER,"
                + ITEM_LONG_BREAK_DURATION + " INTEGER,"
                + ITEM_WORK_COLOR + " INTEGER,"
                + ITEM_BREAK_COLOR + " INTEGER,"
                + ITEM_LONG_BREAK_COLOR + " INTEGER,"
                + ITEM_TOTAL_SESSION + " INTEGER"
                + ");";

        db.execSQL(CREATE_ITEM_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
        String DROP_ITEM_TABLE = "DROP TABLE IF EXISTS " + TABLE_ITEMS;
        db.execSQL(DROP_ITEM_TABLE);
        onCreate(db);
    }

    public boolean insertItem(Item i) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(ITEM_TITLE, i.getTaskName());
        contentValues.put(ITEM_WORK_DURATION, i.getWorkSessionDuration());
        contentValues.put(ITEM_BREAK_DURATION, i.getBreakDuration());
        contentValues.put(ITEM_LONG_BREAK_DURATION, i.getLongBreakDuration());
        contentValues.put(ITEM_WORK_COLOR, i.getWorkSessionColor());
        contentValues.put(ITEM_BREAK_COLOR, i.getBreakColor());
        contentValues.put(ITEM_LONG_BREAK_COLOR, i.getLongBreakColor());
        contentValues.put(ITEM_TOTAL_SESSION, i.getTotalWorkSession());

        long result = db.insert(TABLE_ITEMS, null, contentValues);

        if (result != -1)
            return true;
        return false;
    }

    public Item getItemById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Item item = null;

        //  Cursor mCursor = db.rawQuery("SELECT * FROM " + TABLE_ITEMS + " WHERE " + ITEM_ID + "=" + id + "", null);
        Cursor mCursor = db.query(TABLE_ITEMS, ITEM_COLUMNS,
                ITEM_ID + " = ? ",
                new String[]{String.valueOf(id)}, null, null, null);

        if (mCursor != null && mCursor.getCount() > 0) {
            if (mCursor.moveToFirst()) {
                do {
                    item = new Item();
                    item.setId(mCursor.getInt(0));
                    item.setTaskName(mCursor.getString(1));
                    item.setWorkSessionDuration(mCursor.getShort(2));
                    item.setBreakDuration(mCursor.getShort(3));
                    item.setLongBreakDuration(mCursor.getShort(4));
                    item.setWorkSessionColor(mCursor.getInt(5));
                    item.setBreakColor(mCursor.getInt(6));
                    item.setLongBreakColor(mCursor.getInt(7));
                    item.setTotalWorkSession(mCursor.getShort(8));
                } while (mCursor.moveToNext());
            }
        }

        mCursor.close();

        return item;
    }

    public int rowsCountOfItemTable() {
        SQLiteDatabase db = this.getReadableDatabase();

        return (int) DatabaseUtils.queryNumEntries(db, TABLE_ITEMS);
    }

    public boolean updateItem(Item i) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(ITEM_TITLE, i.getTaskName());
        contentValues.put(ITEM_WORK_DURATION, i.getWorkSessionDuration());
        contentValues.put(ITEM_BREAK_DURATION, i.getBreakDuration());
        contentValues.put(ITEM_LONG_BREAK_DURATION, i.getLongBreakDuration());
        contentValues.put(ITEM_WORK_COLOR, i.getWorkSessionColor());
        contentValues.put(ITEM_BREAK_COLOR, i.getBreakColor());
        contentValues.put(ITEM_LONG_BREAK_COLOR, i.getLongBreakColor());
        contentValues.put(ITEM_TOTAL_SESSION, i.getTotalWorkSession());

        long result = db.update(TABLE_ITEMS,
                contentValues, ITEM_ID + " = ? ",
                new String[]{Integer.toString(i.getId())});

        if (result == 1)
            return true;
        return false;
    }

    public Integer deleteItem(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_ITEMS,
                ITEM_ID + " = ? ",
                new String[]{Integer.toString(id)});
    }

    public List<Item> getItemList() {
        List<Item> itemList = new ArrayList<Item>();
        SQLiteDatabase db = this.getReadableDatabase();

        String sqlQuery = "SELECT * FROM " + TABLE_ITEMS + " ORDER BY " + ITEM_ID + " DESC";
        Cursor mCursor = db.rawQuery(sqlQuery, null);

/*        Cursor mCursor = db.query(TABLE_ITEMS, ITEM_COLUMNS
                       null, null, null, ITEM_ID + " DESC", null);*/
        mCursor.moveToFirst();
        while (!mCursor.isAfterLast()) {
            Item item = new Item();
            item.setId(mCursor.getInt(0));
            item.setTaskName(mCursor.getString(1));
            item.setWorkSessionDuration(mCursor.getShort(2));
            item.setBreakDuration(mCursor.getShort(3));
            item.setLongBreakDuration(mCursor.getShort(4));
            item.setWorkSessionColor(mCursor.getInt(5));
            item.setBreakColor(mCursor.getInt(6));
            item.setLongBreakColor(mCursor.getInt(7));
            item.setTotalWorkSession(mCursor.getShort(8));

            itemList.add(item);

            mCursor.moveToNext();
        }

        mCursor.close();

        return itemList;
    }

    public synchronized void close() {
        if (db != null) {
            db.close();
        }
        super.close();
    }
}