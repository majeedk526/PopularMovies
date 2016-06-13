package com.portfolio.majeed.popularmovies.database;

import android.provider.BaseColumns;

/**
 * Created by Majeed on 12-06-2016.
 */
public class MovieContract {


    public static class MoviefAV implements BaseColumns{

        public static String TABLE_NAME = "favmovie";

        public static String COLUMN_MOVIE_ID = "movieid";
        public static String COLUMN_MOVIE_TITLE = "title";
        public static String COLUMN_MOVIE_POSTER_PATH = "posterpath";
        public static String COLUMN_MOVIE_VOTE_AVG = "voteavg";
        public static String COLUMN_MOVIE_RELEASE_DATE = "releasedate";
        public static String COLUMN_MOVIE_OVERVIEW = "overview";

    }

}
