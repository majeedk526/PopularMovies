package com.portfolio.majeed.popularmovies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;

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
import java.util.LinkedList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {


    GridView gv;
    ArrayAdapter<Movie> adapter;
    ArrayList<Movie> mList = null;
    private final String LOG_TAG = getClass().getSimpleName();

    public MainActivityFragment() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("mList",mList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        adapter = new MovieAdapter(getContext(),new ArrayList<Movie>());
        gv = (GridView) rootView.findViewById(R.id.gv_movies);

        if(savedInstanceState==null || !savedInstanceState.containsKey("mList")){
            FetchImage fi = new FetchImage();
            fi.execute();
            Log.v(LOG_TAG,"fetched list from internet");
        } else {
            mList = savedInstanceState.getParcelableArrayList("mList");
            adapter.addAll(mList);
            adapter.notifyDataSetChanged();
            gv.setAdapter(adapter);
            Log.v(LOG_TAG, "retrive list");
        }


        return rootView;
    }


    class FetchImage extends AsyncTask<Void, Void, ArrayList<Movie>> {

        private final String LOG_TAG = getClass().getSimpleName();
        private final String baseUrl = "http://image.tmdb.org/t/p/w342";

        @Override
        protected void onPostExecute(ArrayList<Movie> mlist) {
            super.onPostExecute(mList);


//            Log.d(LOG_TAG, s[0]);
            if(mlist==null){return;}
            mList = mlist;
            adapter.addAll(mList);
            adapter.notifyDataSetChanged();
            gv.setAdapter(adapter);

            //Picasso.with(getContext()).load(baseUrl + s[0]).into(imView);

        }

        @Override
        protected ArrayList<Movie> doInBackground(Void... params) {

            // Check internet connectivity
            ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo == null || !networkInfo.isConnected()) {
                return null;
            }

            String movieBaseUri = "http://api.themoviedb.org/3/movie/popular?";
            String api = "ee7381179e87d11721d8cd920fd28081";

            Uri uri = Uri.parse(movieBaseUri).buildUpon()
                    .appendQueryParameter("api_key", api).build();

            URL url = null;

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

        private ArrayList<Movie> getDataFromJson(String js){

            JSONObject jo=null;
            JSONArray ja=null;

            try {
                jo = new JSONObject(js);
                ja = jo.getJSONArray("results");



            } catch (JSONException e) {
                e.printStackTrace();
            }

            ArrayList<Movie> mList = new ArrayList<>();

            if(ja!=null){
                for(int i=0; i<10; i++){
                    try {
                        jo = ja.getJSONObject(i);
                        mList.add(new Movie(getContext(),
                                jo.getString("original_title"),
                                baseUrl + jo.getString("poster_path"),
                                jo.getInt("vote_count"),
                                jo.getDouble("popularity")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
            return mList;
        }
    }
}
