package com.portfolio.majeed.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Majeed on 08-04-2016.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MyViewHolder> {

    Context context;
    private List<Movie> mList;
    public MovieAdapter(Context context, List<Movie> mList) {
        this.context = context;
        this.mList = mList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView iv;
        TextView tvMovieName, tvRatingBar;
        AppCompatRatingBar rb;
        CardView cv;

        public MyViewHolder(View view) {
            super(view);

            cv = (CardView) view.findViewById(R.id.cv_cell);
            iv = (ImageView) view.findViewById(R.id.iv_movie_poster);
            tvMovieName = (TextView) view.findViewById(R.id.tv_movie_name);
            tvRatingBar = (TextView) view.findViewById(R.id.tv_rating);
            rb = (AppCompatRatingBar) view.findViewById(R.id.rating_bar);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gridiew_cell,null, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder,final int position) {

        Movie m = mList.get(position);
        Picasso.with(context).load(m.posterUrl).into(holder.iv);
        holder.tvMovieName.setText(m.movieName);
        holder.tvRatingBar.setText(String.valueOf(m.voteAvg));
        holder.rb.setRating(((float) m.voteAvg)/10 * holder.rb.getNumStars());

        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putParcelable(context.getString(R.string.movie_key), mList.get(position));

                    Intent intent = new Intent(context, DetailActivity.class);
                    intent.putExtras(bundle);
                    context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void addAll(List<Movie> mList){
        this.mList = mList;
    }
}
