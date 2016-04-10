package com.portfolio.majeed.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements AdapterView.OnItemClickListener {


    GridView gv;
    ArrayAdapter<Movie> adapter;
    ArrayList<Movie> mList = null;
    private final String LOG_TAG = getClass().getSimpleName();

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


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        adapter = new MovieAdapter(getContext(), new ArrayList<Movie>());
        gv = (GridView) rootView.findViewById(R.id.gv_movies);
        gv.setOnItemClickListener(this);

        if (savedInstanceState == null || !savedInstanceState.containsKey("mList")) {

            if (isConnected()) {
                FetchImage fi = new FetchImage();
                fi.execute();
            }

        } else {
            mList = savedInstanceState.getParcelableArrayList("mList");
            String prevSort = savedInstanceState.getString("sort");
            String s = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(
                    getString(R.string.pref_order_key),
                    getString(R.string.rated));
            if (!prevSort.equals(s)) {
                if (isConnected()) {
                    FetchImage fi = new FetchImage();
                    fi.execute();
                }

            } else {
                if(mList!=null){
                    adapter.addAll(mList);
                    adapter.notifyDataSetChanged();
                }

            }
        }

        gv.setAdapter(adapter);
        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Bundle bundle = new Bundle();
        bundle.putParcelable(getString(R.string.movie_key), adapter.getItem(position));

        Intent intent = new Intent(getContext(), DetailActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private boolean isConnected() {
        // Check internet connectivity
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(getContext(),"Internet not connected.",Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }


    class FetchImage extends AsyncTask<Void, Void, ArrayList<Movie>> {

        private final String LOG_TAG = getClass().getSimpleName();
        private final String baseUrl = "http://image.tmdb.org/t/p/w342";


        @Override
        protected void onPostExecute(ArrayList<Movie> mlist) {
            super.onPostExecute(mList);

            if (mlist == null) {
               Toast.makeText(getContext(),"Could not download movie list.",Toast.LENGTH_LONG).show();
                return;
            }
            mList = mlist;
            adapter.addAll(mList);
            adapter.notifyDataSetChanged();


        }

        @Override
        protected ArrayList<Movie> doInBackground(Void... params) {


            String movieBaseUri = "http://api.themoviedb.org/3/discover/movie?";
            String api = "ee7381179e87d11721d8cd920fd28081";
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


            Uri uri = Uri.parse(movieBaseUri).buildUpon()
                    .appendQueryParameter("sort_by", sortKey)
                    .appendQueryParameter("api_key", api).build();

            URL url = null;

            Log.v(LOG_TAG, uri.toString());

            try {
                url = new URL(uri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            String movieId = "";
            BufferedReader reader = null;
            HttpURLConnection urlConnection = null;


            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }

                movieId = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
            } finally {

                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }

                return getDataFromJson(movieId);
            }
        }

        private ArrayList<Movie> getDataFromJson(String js) {

            JSONObject jo = null;
            JSONArray ja = null;

            try {
                jo = new JSONObject(js);
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
                                jo.getString("original_title"),
                                baseUrl + jo.getString("poster_path"),
                                jo.getDouble("vote_average"),
                                jo.getDouble("popularity"),
                                jo.getString("release_date"),
                                jo.getString("overview")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
            return mList;
        }
    }
}
