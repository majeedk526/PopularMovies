package com.portfolio.majeed.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.portfolio.majeed.popularmovies.database.MovieContract.MoviefAV;
import com.portfolio.majeed.popularmovies.database.MovieDbHelper;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    ArrayAdapter<Review> lvReviewAdapter;
    ReviewAdapter elvAdapter_review;
    SimpleExpandableListAdapter sela_review;
    List<Review> reviewList = null;
    List<VideoInfo> vlist = null;

    ArrayAdapter<VideoInfo> lvVideoAdapter;
    ListView lv, lvVideos;
    ExpandableListView elv_review;
    ScrollView sv;
    private static String movieId;

    RatingBar rbFav; // rating bar to set favourite movies

    public DetailActivityFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_detail, container, false);

        Bundle b = getActivity().getIntent().getExtras();
        final Movie  m = b.getParcelable(getString(R.string.movie_key));

        sv = (ScrollView) rootView.findViewById(R.id.sv);

        ImageView iv = (ImageView) rootView.findViewById(R.id.iv_movie_poster);
        TextView tv_releaseDate = (TextView) rootView.findViewById(R.id.tv_release_date);
        TextView tv_voteAvg = (TextView) rootView.findViewById(R.id.tv_rating);
        TextView tv_overView = (TextView) rootView.findViewById(R.id.tv_detail);
        RatingBar rb = (RatingBar) rootView.findViewById(R.id.rating_bar);

        Picasso.with(getContext()).load(m.posterUrl).into(iv);
        String year = (m.releaseDate.split("-"))[0];
        tv_releaseDate.setText(year);
        tv_voteAvg.setText(m.voteAvg + "/10.0");
        rb.setRating(((float) m.voteAvg)/10 * rb.getNumStars());
        tv_overView.setText(m.overView);

        final ImageView ivFav = (ImageView) rootView.findViewById(R.id.iv_fav);

        MovieDbHelper dbHelper = new MovieDbHelper(getContext());
        if(dbHelper.isFavourite(m.movieId)){ivFav.setSelected(true);}

        ivFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!ivFav.isSelected()){ ivFav.setSelected(true);
                    //write code to add in database
                    MovieDbHelper dbHelper = new MovieDbHelper(getContext());
                    ContentValues values = new ContentValues();
                    values.put(MoviefAV.COLUMN_MOVIE_ID,m.movieId);
                    values.put(MoviefAV.COLUMN_MOVIE_TITLE, m.movieName);
                    values.put(MoviefAV.COLUMN_MOVIE_POSTER_PATH, m.posterUrl);
                    values.put(MoviefAV.COLUMN_MOVIE_VOTE_AVG, m.voteAvg);
                    values.put(MoviefAV.COLUMN_MOVIE_RELEASE_DATE, m.releaseDate);
                    values.put(MoviefAV.COLUMN_MOVIE_OVERVIEW, m.overView);
                    long rowID = dbHelper.insert(values);

                    if(rowID>0){Toast.makeText(getContext(),"Marked favourite",Toast.LENGTH_LONG).show();}
                    else {Toast.makeText(getContext(),"Could not mark favourite",Toast.LENGTH_LONG).show();}
                }
                else {ivFav.setSelected(false);
                    //check for existence in database and remove entry if exists
                    MovieDbHelper dbHelper = new MovieDbHelper(getContext());
                    dbHelper.delete(m.movieId);
                }
            }
        });

        movieId = m.movieId;
        getActivity().setTitle(m.movieName);

        if(MainActivityFragment.isConnected(getContext())){
            FetchReview freview = new FetchReview();
            freview.execute("http://api.themoviedb.org/3/movie/");

            FetchVideo fvideo = new FetchVideo();
            fvideo.execute("http://api.themoviedb.org/3/movie/");
        }

        return rootView;
    }


    private class FetchReview extends FetchData{

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                 reviewList = getDataFromJson(s);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(reviewList.size()==0){
                ((TextView) getView().findViewById(R.id.tv_review_not_found)).setVisibility(View.VISIBLE);
                ((ProgressBar) getView().findViewById(R.id.pb_reviews)).setVisibility(View.GONE);
                return;
            }

           lvReviewAdapter = new ArrayAdapter<Review>(getActivity(),R.layout.review_cell, reviewList){

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {

                   // View rootView = super.getView(position,convertView,parent);

                    if(convertView==null){
                        convertView = getActivity().getLayoutInflater().inflate(R.layout.review_cell,null);
                    }

                    View rootView = convertView;

                    TextView txt1 = (TextView) rootView.findViewById(R.id.tv_author);
                    txt1.setText(getItem(position).author);
                    ((TextView) rootView.findViewById(R.id.tv_content)).setText(getItem(position).content);
                    return rootView;
                }
            };

            lv = (ListView) getActivity().findViewById(R.id.lv_reviews);
            lv.setAdapter(lvReviewAdapter);
            lv.setVisibility(View.VISIBLE);

        }

        @Override
        protected List<Review> getDataFromJson(String jsonString) throws JSONException {

            if(jsonString==null){return new ArrayList<Review>();} //return an empty list

            JSONObject jo = new JSONObject(jsonString);
            JSONArray ja = jo.getJSONArray("results");
            List<Review> rlist = new ArrayList<>();


            for(int i=0; i<ja.length(); i++){
                jo = ja.getJSONObject(i);
                String author = jo.getString("author");
                String content = jo.getString("content");
                rlist.add(new Review(author,content));
            }

            return rlist;
        }

        @Override
        protected Uri getUri(String baseUri, String api) {

            Uri uri = Uri.parse(baseUri).buildUpon()

                    .appendPath(movieId)
                    .appendEncodedPath("reviews")
                    .appendQueryParameter("api_key", api)
                    .build();

            Log.v(getClass().getSimpleName(),uri.toString());

            return uri;
        }
    }

    private class FetchVideo extends FetchData implements AdapterView.OnItemClickListener{

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                vlist  = getDataFromJson(s);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(vlist.size()==0){
                ((TextView) getView().findViewById(R.id.tv_trailer_not_found)).setVisibility(View.VISIBLE);
                ((ProgressBar) getView().findViewById(R.id.pb_videos)).setVisibility(View.GONE);
                return;
            }

            lvVideoAdapter = new ArrayAdapter<VideoInfo>(getContext(),R.layout.video_cell,vlist){
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {

                  //  View rootView = super.getView(position,convertView,parent);

                    if(convertView==null){
                        convertView = getActivity().getLayoutInflater().inflate(R.layout.video_cell,null);
                    }

                    View rootView = convertView;

                    TextView tvVideoName = (TextView) rootView.findViewById(R.id.tv_video_name);
                    tvVideoName.setText(getItem(position).name);

                    return rootView;
                }
            };

            lvVideos = (ListView) getActivity().findViewById(R.id.lv_videos);
            lvVideos.setAdapter(lvVideoAdapter);
            lvVideos.setVisibility(View.VISIBLE);
            lvVideos.setOnItemClickListener(this);
            ((ProgressBar) getView().findViewById(R.id.pb_videos)).setVisibility(View.GONE);

            if(lvVideoAdapter.getCount()==0){
                ((TextView) getView().findViewById(R.id.tv_trailer_videos)).setText("Trailer : not found");
            }

        }

        @Override
        protected List<VideoInfo> getDataFromJson(String jsonString) throws JSONException {

            if(jsonString==null){
                return new ArrayList<VideoInfo>();
            }

            JSONObject jo = new JSONObject(jsonString);
            JSONArray ja = jo.getJSONArray("results");
            List<VideoInfo> vlist = new ArrayList<>();


            for(int i=0; i<ja.length(); i++){
                jo = ja.getJSONObject(i);
                String videoName = jo.getString("name");
                String videoKey = jo.getString("key");
                vlist.add(new VideoInfo(videoName,videoKey));
            }

            return vlist;
        }

        @Override
        protected Uri getUri(String baseUri, String api) {

            Uri uri = Uri.parse(baseUri).buildUpon()

                    .appendPath(movieId)
                    .appendEncodedPath("videos")
                    .appendQueryParameter("api_key", api)
                    .build();

            Log.v(getClass().getSimpleName(),uri.toString());

            return uri;


        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.youtube.com/watch?v=" + lvVideoAdapter.getItem(position).key));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            if(intent.resolveActivity(getActivity().getPackageManager()) != null){
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "App not found which can play the video",Toast.LENGTH_LONG).show();
            }
        }
    }


    private class VideoInfo{
        String name, key;

        public VideoInfo(String name, String key){
            this.name = name;
            this.key = key;
        }
    }


    private class Review{
        String author, content;

        public Review(String author, String content){
            this.author = author;
            this.content = content;
        }
    }


    private class ReviewAdapter implements ExpandableListAdapter{

        List<Review> itemList;

        public ReviewAdapter(List<Review> itemList){
            this.itemList = itemList;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {


        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public int getGroupCount() {
            return 1;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return itemList.size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return 0;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {

            return itemList.get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent){

            if(convertView==null){
                convertView = getActivity().getLayoutInflater().inflate(R.layout.header,null);
            }

            ((TextView) convertView.findViewById(R.id.tv_header)).setText("Review");

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent){

            if(convertView == null){
                convertView = getActivity().getLayoutInflater().inflate(R.layout.review_cell,null);
            }
            TextView txt1 = (TextView) convertView.findViewById(R.id.tv_author);
            txt1.setText(((Review)getChild(0,childPosition)).author);
            ((TextView) convertView.findViewById(R.id.tv_content)).setText(((Review)getChild(0,childPosition)).content);

            return convertView;

        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public void onGroupExpanded(int groupPosition) {

        }

        @Override
        public void onGroupCollapsed(int groupPosition) {

        }

        @Override
        public long getCombinedChildId(long groupId, long childId) {
            return 0;
        }

        @Override
        public long getCombinedGroupId(long groupId) {
            return 0;
        }
    }

}
