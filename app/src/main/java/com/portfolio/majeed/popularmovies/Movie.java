package com.portfolio.majeed.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;

/**
 * Created by Majeed on 08-04-2016.
 */
public class Movie implements Comparable<Movie>, Parcelable{

    String movieName, posterUrl;
    int votes;
    double popularity;
    Context context;

    public Movie(Context c, String name, String posterUrl, int votes, double popularity){

        this.context = c;
        this.movieName = name;
        this.posterUrl = posterUrl;
        this.votes = votes;
        this.popularity = popularity;
    }

    protected Movie(Parcel in) {
        movieName = in.readString();
        posterUrl = in.readString();
        votes = in.readInt();
        popularity = in.readDouble();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {

        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    @Override
    public int compareTo(Movie m) {
        String defaultSortOrder = context.getString(R.string.rated);
        String sortOrder = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pref_order_key),
                        defaultSortOrder);

        if(sortOrder.equals(defaultSortOrder)){
            if(this.votes < m.votes){return -1;}
            else if(this.votes == m.votes){return 0;}
            else {return 1;}
        } else {
            if(this.popularity < m.popularity){return -1;}
            else if(this.popularity == m.popularity){return 0;}
            else {return 1;}
        }

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(movieName);
        dest.writeString(posterUrl);
        dest.writeInt(votes);
        dest.writeDouble(popularity);
    }



}
