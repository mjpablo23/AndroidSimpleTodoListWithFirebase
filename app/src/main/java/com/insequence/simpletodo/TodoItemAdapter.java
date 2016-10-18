package com.insequence.simpletodo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.InputStream;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by paulyang on 9/20/16.
 */

// http://guides.codepath.com/android/Using-an-ArrayAdapter-with-ListView

public class TodoItemAdapter extends ArrayAdapter<TodoItem> {
    // View lookup cache
    public static final String ITEMS_CHILD = "items";
    private DatabaseReference mFirebaseDatabaseReference;



    // private static class ViewHolder {
    static class ViewHolder {
//         TextView itemPersonName;
//         TextView itemText;
//         ImageView imageView;

        @BindView(R.id.itemPersonName) TextView itemPersonName;
        @BindView(R.id.itemText) TextView itemText;
        @BindView(R.id.imageView3) ImageView imageView;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public TodoItemAdapter(Context context, ArrayList<TodoItem> users) {
        super(context, R.layout.todo_item, users);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final TodoItem todoItem = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        final ViewHolder viewHolder; // view lookup cache stored in tag
        // ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            // If there's no view to re-use, inflate a brand new view for row
            // viewHolder = new ViewHolder();  // replace with butter

            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.todo_item, parent, false);
            viewHolder = new ViewHolder(convertView); // butter

            // replace with butter
//             viewHolder.itemPersonName = (TextView) convertView.findViewById(R.id.itemPersonName);
//             viewHolder.itemText = (TextView) convertView.findViewById(R.id.itemText);
//             viewHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView3);

            // Cache the viewHolder object inside the fresh view
            convertView.setTag(viewHolder);
        } else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolder) convertView.getTag();
        }



        // Populate the data into the template view using the data object
        viewHolder.itemPersonName.setText(todoItem.getName());
        viewHolder.itemText.setText(todoItem.getText());

        // get the profile picture from url

        // use 2nd solution:
        //http://stackoverflow.com/questions/5776851/load-image-from-url

        new DownloadImageTask(viewHolder.imageView)
                .execute(todoItem.getPhotoUrl());
//      Return the completed view to render on screen
        return convertView;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }



}