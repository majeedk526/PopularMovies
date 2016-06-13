package com.portfolio.majeed.popularmovies.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import com.portfolio.majeed.popularmovies.database.MovieContract.MoviefAV;
/**
 * Created by Majeed on 12-06-2016.
 */
public class MovieDbHelper extends SQLiteOpenHelper {

    public static String DATABASE_NAME = "favmovies.db";
    private static int DATABSE_VERSION = 1;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABSE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_FAVMOVIES = "CREATE TABLE " + MoviefAV.TABLE_NAME + "( " +
                MoviefAV._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MoviefAV.COLUMN_MOVIE_ID + " TEXT NOT NULL UNIQUE, " +
                MoviefAV.COLUMN_MOVIE_TITLE + " TEXT NOT NULL," +
                MoviefAV.COLUMN_MOVIE_POSTER_PATH + " TEXT NOT NULL," +
                MoviefAV.COLUMN_MOVIE_VOTE_AVG + " DOUBLE NOT NULL," +
                MoviefAV.COLUMN_MOVIE_RELEASE_DATE + " TEXT NOT NULL," +
                MoviefAV.COLUMN_MOVIE_OVERVIEW + " TEXT NOT NULL" +
                " );";

        db.execSQL(SQL_CREATE_FAVMOVIES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MoviefAV.TABLE_NAME);
        onCreate(db);
    }

    public long insert(ContentValues values){
        SQLiteDatabase db = getWritableDatabase();

        long row_id = db.insert(MoviefAV.TABLE_NAME, null,values);
        return row_id;
    }

    public int delete(String movieId){

        SQLiteDatabase db = getWritableDatabase();
        int numRows = db.delete(MoviefAV.TABLE_NAME,MoviefAV.COLUMN_MOVIE_ID + " = ?", new String[]{movieId});
        return numRows;
    }

    public Cursor readAll(String[] projections){
        SQLiteDatabase db = getReadableDatabase();
        return db.query(MoviefAV.TABLE_NAME, projections,null,null,null,null,null);
    }

    public boolean isFavourite(String movieId){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(MoviefAV.TABLE_NAME, new String[]{MoviefAV.COLUMN_MOVIE_ID},
                MoviefAV.COLUMN_MOVIE_ID + " = ?",new String[]{movieId},null,null,null);

        if(c.moveToFirst()){return true;}
        else {return false;}
    }
}
