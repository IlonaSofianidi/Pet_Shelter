package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

public class PetProvider extends ContentProvider {
    public static final String LOG_TAG = PetProvider.class.getSimpleName();
    private PetDBHelper dbHelper;
    private static final int PETS = 100;
    private static final int PET_ID = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new PetDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
        SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        int match = sUriMatcher.match(uri);
        Log.v(LOG_TAG, "match is " + match);
        switch (match) {
            case PETS:
                cursor = sqLiteDatabase.query(PetContract.PetEntry.TABLE_NAME, strings, s, strings1, null, null, s1);
                break;
            case PET_ID:
                cursor = sqLiteDatabase.query(PetContract.PetEntry.TABLE_NAME, strings,
                        PetContract.PetEntry._id + " =?", new String[]{String.valueOf(ContentUris.parseId(uri))},
                        null, null, s1);
                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown uri" + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match= sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetContract.PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetContract.PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);

        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supportet for this uri" + uri);
        }

    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPet(Uri uri, ContentValues contentValues) {
        int gender = contentValues.getAsInteger(PetContract.PetEntry.COLUMN_GENDER);
        Log.e("GENDER", gender + "");
        if (TextUtils.isEmpty(contentValues.getAsString(PetContract.PetEntry.COLUMN_NAME))) {
            throw new IllegalArgumentException("Pet requires a name");
//        } else if (TextUtils.isEmpty(contentValues.getAsString(PetContract.PetEntry.COLUMN_BREED))) {
//            throw new IllegalArgumentException("Pet requires a breed name");

        } else if (contentValues.getAsInteger(PetContract.PetEntry.COLUMN_WEIGHT) < 0) {
            throw new IllegalArgumentException("Weight cannot be negative");
        }
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
        long id = sqLiteDatabase.insert(PetContract.PetEntry.TABLE_NAME, null, contentValues);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert raw for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int id = 0;
        switch (match){
            case PETS:

                id = deletePet(uri, s, strings);
                break;

            case PET_ID:
                String selection = PetContract.PetEntry._id +"=?";
                String[] selectionArgs= new String[]{String.valueOf(ContentUris.parseId(uri))};
                id = deletePet(uri, selection, selectionArgs);

                break;

                default:throw  new IllegalArgumentException("Cannot delete pet by current uri"+uri);
        }
        if (id != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return id;


    }
    private int deletePet(Uri uri,String selection , String[] selectionArgs){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return  db.delete(PetContract.PetEntry.TABLE_NAME,selection,selectionArgs);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PETS:
                return updatePet(uri, contentValues, s, strings);
            case PET_ID:
                String selection = PetContract.PetEntry._ID + "=?";
                String[] selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, contentValues, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Failed to update current uri" + uri);
        }

    }

    private int updatePet(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        if(contentValues.size()==0){
            return  0;
        }

        if (contentValues.containsKey(PetContract.PetEntry.COLUMN_NAME)) {
            String name = contentValues.getAsString(PetContract.PetEntry.COLUMN_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }

        }
//        if (contentValues.containsKey(PetContract.PetEntry.COLUMN_BREED)) {
//            String breed = contentValues.getAsString(PetContract.PetEntry.COLUMN_BREED);
//            if (breed == null) {
//                throw new IllegalArgumentException("Pet requires a breed name");
//            }
//        }
        if (contentValues.containsKey(PetContract.PetEntry.COLUMN_WEIGHT)) {
            int weight = contentValues.getAsInteger(PetContract.PetEntry.COLUMN_WEIGHT);
            if (weight < 0) {
                throw new IllegalArgumentException("Weight cannot be negative");
            }
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsUpdated = db.update(PetContract.PetEntry.TABLE_NAME, contentValues, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}