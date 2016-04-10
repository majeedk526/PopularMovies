package com.portfolio.majeed.popularmovies;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.TextViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_detail, container, false);

        Bundle b = getActivity().getIntent().getExtras();
        Movie  m = b.getParcelable(getString(R.string.movie_key));

        ImageView iv = (ImageView) rootView.findViewById(R.id.iv_movie_poster);
        TextView tv_releaseDate = (TextView) rootView.findViewById(R.id.tv_release_date);
        TextView tv_voteAvg = (TextView) rootView.findViewById(R.id.tv_rating);
        TextView tv_overView = (TextView) rootView.findViewById(R.id.tv_detail);

        Picasso.with(getContext()).load(m.posterUrl).into(iv);
        tv_releaseDate.setText(m.releaseDate);
        tv_voteAvg.setText(m.voteAvg + "/10.0");
        tv_overView.setText(m.overView);

        getActivity().setTitle(m.movieName);

        return rootView;
    }
}
