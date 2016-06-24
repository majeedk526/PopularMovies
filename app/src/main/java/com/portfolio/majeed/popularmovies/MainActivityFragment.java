package com.portfolio.majeed.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
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
public class MainActivityFragment extends Fragment implements AdapterView.OnItemClickListener {


    public boolean isSw600 = false;

    GridView gv;
    ArrayAdapter<Movie> adapter;
    ArrayList<Movie> mList = null;
    private final String LOG_TAG = getClass().getSimpleName();
    String movieBaseUri = "http://api.themoviedb.org/3/movie/popular?";

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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("mList", mList);
        String s = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getString(getString(R.string.pref_order_key),
                        getString(R.string.rated));
        outState.putString("sort", s);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        if (!(getActivity().findViewById(R.id.ll_sw600) == null)) {
            isSw600 = true;
        }
    }

    private void loadDetailFragment(Bundle b){

        DetailActivityFragment detailFragment = new DetailActivityFragment();
        detailFragment.setArguments(b);

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_detail,detailFragment)
                .commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        adapter = new MovieAdapter(getContext(), new ArrayList<Movie>());
        gv = (GridView) rootView.findViewById(R.id.gv_movies);
        gv.setOnItemClickListener(this);

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

        gv.setAdapter(adapter);
        return rootView;
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Bundle bundle = new Bundle();
        bundle.putParcelable(getString(R.string.movie_key), adapter.getItem(position));

    if(!isSw600){
        Intent intent = new Intent(getContext(), DetailActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    } else {
        loadDetailFragment(bundle);
    }

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
