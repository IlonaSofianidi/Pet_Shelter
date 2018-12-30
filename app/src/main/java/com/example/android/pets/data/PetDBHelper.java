package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PetDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "shelter.db";
    private static final int DB_VERSION = 10;
    private final String CREATE_TABLE = "CREATE TABLE pets (" + PetContract.PetEntry._id + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
            + PetContract.PetEntry.COLUMN_NAME + " TEXT,"
            + PetContract.PetEntry.COLUMN_BREED + " TEXT,"
            + PetContract.PetEntry.COLUMN_GENDER + " INTEGER, "
            + PetContract.PetEntry.COLUMN_WEIGHT + " INTEGER);";

    public PetDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS pets;");
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }
}