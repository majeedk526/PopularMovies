package com.portfolio.majeed.popularmovies;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Majeed on 08-04-2016.
 */
public class Movie implements Parcelable{

    String movieName, posterUrl, releaseDate, overView;
    double voteAvg;
    Context context;
    String movieId;

    public Movie(Context c, String id, String name, String posterUrl, double voteAvg, String releaseDate,
                 String overView){

        this.context = c;
        this.movieName = name;
        this.movieId = id;
        this.posterUrl = posterUrl;
        this.voteAvg = voteAvg;
        this.releaseDate = releaseDate;
        this.overView = overView;
    }

    protected Movie(Parcel in) {
        movieId = in.readString();
        movieName = in.readString();
        posterUrl = in.readString();
        voteAvg = in.readDouble();
        releaseDate = in.readString();
        overView = in.readString();
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
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(movieId);
        dest.writeString(movieName);
        dest.writeString(posterUrl);
        dest.writeDouble(voteAvg);
        dest.writeString(releaseDate);
        dest.writeString(overView);
    }



}
