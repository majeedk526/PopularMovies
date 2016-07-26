package com.portfolio.majeed.popularmovies;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.portfolio.majeed.popularmovies.database.MovieContract.MoviefAV;
import com.portfolio.majeed.popularmovies.database.MovieDbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {


    public boolean isSw600 = false;

    RecyclerView rv;
    MovieAdapter adapter;
    ImageView ivToolbar;
    ArrayList<Movie> mList = null;
    private final String LOG_TAG = getClass().getSimpleName();
    String movieBaseUri = "http://api.themoviedb.org/3/movie/popular?";
    private Callback callback;
    //projections for reading data from database of favourite movie

    private final String[] projection = {
            MoviefAV.COLUMN_MOVIE_ID,
            MoviefAV.COLUMN_MOVIE_TITLE,
            MoviefAV.COLUMN_MOVIE_POSTER_PATH,
            MoviefAV.COLUMN_MOVIE_VOTE_AVG,
            MoviefAV.COLUMN_MOVIE_RELEASE_DATE,
            MoviefAV.COLUMN_MOVIE_OVERVIEW
    };

    private final static int COLUMN_MOVIE_ID = 0;
    private final static int COLUMN_MOVIE_TITLE = 1;
    private final static int COLUMN_MOVIE_POSTER = 2;
    private final static int COLUMN_MOVIE_VOTE = 3;
    private final static int COLUMN_MOVIE_RELEASE_DATE = 4;
    private final static int COLUMN_MOVIE_OVERVIEW = 5;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        callback = (Callback) getActivity();

    }

    @Override
    public void onDetach() {
        callback =null;
        super.onDetach();

    }

    public interface Callback{
        void setImage(String posterUrl);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("mList", mList);
        String s = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getString(getString(R.string.pref_order_key),
                        getString(R.string.rated));
        outState.putString("sort", s);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        adapter = new MovieAdapter(getContext(),new ArrayList<Movie>());
        rv = (RecyclerView) rootView.findViewById(R.id.rv_movies);


                //.findViewById(R.id.iv_toolbar);

        int disp_orient = getResources().getConfiguration().orientation;

        GridLayoutManager glm = new GridLayoutManager(getContext(),
                (disp_orient == Configuration.ORIENTATION_PORTRAIT) ? 2:3
                );

        RecyclerView.LayoutManager mLayoutManager = glm;
        rv.setLayoutManager(mLayoutManager);
        rv.setItemAnimator(new DefaultItemAnimator());

        String s = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(
                getString(R.string.pref_order_key),
                getString(R.string.rated));

        if (savedInstanceState == null || !savedInstanceState.containsKey("mList")) {

            if(s.equals(getString(R.string.favourite))){
                setFavourite();
            } else if (isConnected(getContext())) {
                FetchImage fi = new FetchImage();
                fi.execute(movieBaseUri);
            }

        } else {
            mList = savedInstanceState.getParcelableArrayList("mList");
            String prevSort = savedInstanceState.getString("sort");



            if (prevSort.equals(s)) {
                if (mList != null) {
                    adapter.addAll(mList);
                    adapter.notifyDataSetChanged();
                }

            } else if (s.equals(getString(R.string.favourite))){
                    setFavourite();
            } else {

                if (isConnected(getContext())) {
                    FetchImage fi = new FetchImage();
                    fi.execute(movieBaseUri);
                }
            }
        }

        rv.setAdapter(adapter);
        return rootView;
    }


    public void fetchImage(){
        FetchImage fi = new FetchImage();
        fi.execute(movieBaseUri);
    }

    private void setFavourite(){

        MovieDbHelper mDbHelper = new MovieDbHelper(getContext());
        mList = new ArrayList<>();

        Cursor c = mDbHelper.readAll(projection);

        while (c.moveToNext()){
            mList.add(new Movie(getContext(),c.getString(COLUMN_MOVIE_ID),
                    c.getString(COLUMN_MOVIE_TITLE),
                    c.getString(COLUMN_MOVIE_POSTER),
                    c.getDouble(COLUMN_MOVIE_VOTE),
                    c.getString(COLUMN_MOVIE_RELEASE_DATE),
                    c.getString(COLUMN_MOVIE_OVERVIEW)
                    ));
        }

        adapter.addAll(mList);
        adapter.notifyDataSetChanged();
    }

    public  static boolean isConnected(Context mContext) {
        // Check internet connectivity
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(mContext, "Check your internet connection.", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    class FetchImage extends FetchData {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            ArrayList<Movie> mlist = null;
            try {
                mlist = getDataFromJson(s);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (mlist == null || mlist.size()==0) {
                Toast.makeText(getContext(),"Could not download movie list.",Toast.LENGTH_LONG).show();
                return;
            }
            mList = mlist;
            adapter.addAll(mList);
            adapter.notifyDataSetChanged();

            if(mList.size() > 0){

                callback.setImage(mList.get(0).posterUrl);
            }

        }

        @Override
        protected ArrayList<Movie> getDataFromJson(String jsonString) throws JSONException {

            final String baseImageUrl = "http://image.tmdb.org/t/p/w342";
            JSONObject jo = null;
            JSONArray ja = null;

            if(jsonString==null){return new ArrayList<Movie>();}

            try {
                jo = new JSONObject(jsonString);
                ja = jo.getJSONArray("results");


            } catch (JSONException e) {
                e.printStackTrace();
            }

            ArrayList<Movie> mList = new ArrayList<>();

            if (ja != null) {
                for (int i = 0; i < ja.length(); i++) {
                    try {
                        jo = ja.getJSONObject(i);
                        mList.add(new Movie(getContext(),
                                jo.getString("id"),
                                jo.getString("original_title"),
                                baseImageUrl + jo.getString("poster_path"),
                                jo.getDouble("vote_average"),
                                //jo.getDouble("popularity"),
                                jo.getString("release_date"),
                                jo.getString("overview")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
            return mList;
        }

        @Override
        protected Uri getUri(String baseUri, String api) {

            String sortKey = null;

            String defaultSortOrder = getContext().getString(R.string.rated);
            String sortOrder = PreferenceManager.getDefaultSharedPreferences(getContext())
                    .getString(getContext().getString(R.string.pref_order_key),
                            defaultSortOrder);

            if (!sortOrder.equals(defaultSortOrder)) {
                sortKey = "popularity.desc";
            } else {
                sortKey = "vote_average.desc";
            }

            Uri uri = Uri.parse(baseUri).buildUpon()
                    .appendQueryParameter("sort_by", sortKey)
                    .appendQueryParameter("api_key", api).build();

            return uri;
        }
    }
}
