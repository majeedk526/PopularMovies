package com.portfolio.majeed.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;
import java.util.zip.Inflater;

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

        //TextView tv = (TextView) convertView.findViewById(R.id.tv_movie_name);
        //tv.setText(m.voteAvg + " " + m.popularity);

        return convertView;
    }
}
