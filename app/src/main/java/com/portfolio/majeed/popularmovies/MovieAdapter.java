package com.portfolio.majeed.popularmovies;

import android.content.Context;
import android.support.v7.widget.AppCompatRatingBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Majeed on 08-04-2016.
 */
public class MovieAdapter extends ArrayAdapter<Movie> {

    Context context;
    List<Movie> mList;
    public MovieAdapter(Context context, List<Movie> mList) {
        super(context, 0, mList);
        this.context = context;
        this.mList = mList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Movie m = mList.get(position);

        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.gridiew_cell, null);
        }

        ImageView iv = (ImageView) convertView.findViewById(R.id.iv_movie_poster);
        Picasso.with(getContext()).load(m.posterUrl).into(iv);
        ((TextView) convertView.findViewById(R.id.tv_movie_name)).setText(m.movieName);
        AppCompatRatingBar rb = (AppCompatRatingBar) convertView.findViewById(R.id.rating_bar);
        rb.setRating(((float) m.voteAvg)/10 * rb.getNumStars());
        ((TextView) convertView.findViewById(R.id.tv_rating)).setText(String.valueOf(m.voteAvg));

        return convertView;
    }
}
