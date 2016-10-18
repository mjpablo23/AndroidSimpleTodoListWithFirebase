package com.insequence.simpletodo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.ButterKnife;

// import com.google.android.gms.auth.api.Auth;
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.bumptech.glide.Glide;
//import com.firebase.ui.database.FirebaseRecyclerAdapter;
//import com.google.android.gms.auth.api.Auth;
//import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
//import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

// bug:  if all items are removed, it crashes

// make into todo list for groups

// need to make edit and remove work again. need to turn on blockDescendants in xml
// http://stackoverflow.com/questions/5551042/onitemclicklistener-not-working-in-listview

//public class MainActivity extends AppCompatActivity  {
public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener  {

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
//    private String mUsername;
//    private String mPhotoUrl;

    // Firebase instance variables
    private DatabaseReference mFirebaseDatabaseReference;

    private static final String TAG = "MainActivity";
    public static final String ITEMS_CHILD = "items";
    private static final int REQUEST_INVITE = 1;
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 10;
    public static final String ANONYMOUS = "anonymous";
    private static final String MESSAGE_SENT_EVENT = "message_sent";
    private String mUsername;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;
    private GoogleApiClient mGoogleApiClient;

    private Button mSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;

    ArrayList<String> items;
    ArrayList<TodoItem> todoItems;
    TodoItemAdapter itemsAdapter;
    ListView lvItems;
    // @BindView(R.id.lvItems) ListView lvItems;

    int inPortraitMode = 1;

    ArrayList<HashMap<String, String>> contactList;

    private ProgressDialog pDialog;
    // URL to get contacts JSON
    // json parsing tutorial:  http://www.androidhive.info/2012/01/android-json-parsing-tutorial/
    // private static String url = "http://api.androidhive.info/contacts/";
    private static String url_singleMovie = "https://api.themoviedb.org/3/movie/550?api_key=b6bddb4059caa8d601981525229a9d46";

    // popular movies get request
    private static String url_popular = "https://api.themoviedb.org/3/movie/popular?api_key=b6bddb4059caa8d601981525229a9d46&language=en-US";

    // now playing movies get request
    private static String url = "https://api.themoviedb.org/3/movie/now_playing?api_key=b6bddb4059caa8d601981525229a9d46";
    // slide 24
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        contactList = new ArrayList<>();
        // startFB();

        // replace with butterknife
        lvItems = (ListView) findViewById(R.id.lvItems);

        readTodoItems();

        System.out.println("setting items in itemsAdapter");
        itemsAdapter = new TodoItemAdapter(this, todoItems);
        lvItems.setAdapter(itemsAdapter);
        setupListViewListener();

        ButterKnife.bind(this);
        ButterKnife.setDebug(true);

        new GetContacts().execute();
    }

    private void startFB() {
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth    .getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            // finish();
            // return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

    }

    // http://stackoverflow.com/questions/10051104/android-menu-not-showing-up
    // sometimes need to clean and build app for new stuff to show up
    @Override
    public boolean onCreateOptionsMenu(Menu my_menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, my_menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mUsername = ANONYMOUS;
                startActivity(new Intent(this, SignInActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    private void setupListViewListener() {
        lvItems.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapter, View item, int pos, long id) {
                // items.remove(pos);

                TodoItem orig = todoItems.get(pos);
                // https://firebase.google.com/docs/database/android/save-data#delete_data
                mFirebaseDatabaseReference.child(ITEMS_CHILD).child(orig.getKey()).removeValue();

                // have it be removed in the firebase listener method
//                todoItems.remove(pos);
//                itemsAdapter.notifyDataSetChanged();item2


                // writeItems();
                return true;
            }
        });

        // setContentView(lvItems);
        // http://stackoverflow.com/questions/9097723/adding-an-onclicklistener-to-listview-android
        // http://stackoverflow.com/questions/36917725/error-setonclicklistener-from-an-android-app
        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View item, int pos, long id) {
//                Object o = lvItems.getItemAtPosition(pos);
//                String str = (String) o;
                // String str = items.get(pos);

                TodoItem todoItem = todoItems.get(pos);
                String str = todoItem.getKey();
                System.out.println("str: " + str);
                launchEditItem(str, pos, todoItem);
            }
        });
    }

    private final int REQUEST_CODE  = 20;
    // http://guides.codepath.com/android/Using-Intents-to-Create-Flows
    public void launchEditItem(String str, int pos, TodoItem todoItem) {
        Intent i = new Intent(MainActivity.this, EditItemActivity.class);
        i.putExtra("str", str);
        i.putExtra("pos", pos);
        i.putExtra("title", todoItem.getName());
        i.putExtra("description", todoItem.getText());
        i.putExtra("rating", todoItem.rating + "");
        i.putExtra("releaseDate", todoItem.releaseDate);
        i.putExtra("backdrop", todoItem.getBackdropUrl());
        i.putExtra("poster", todoItem.getPosterUrl());
        startActivityForResult(i, REQUEST_CODE);
    }

    // Once the sub-activity finishes, the onActivityResult() method in the calling activity is be invoked:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            String str = data.getExtras().getString("str");
            int pos = data.getExtras().getInt("pos");
            System.out.println("str: " + str + ", Pos: " + pos);
            //lvItems.getItemAtPosition()
            items.set(pos, str);

            TodoItem orig = todoItems.get(pos);
            orig.setText(str);
            todoItems.set(pos, orig);

            // https://firebase.google.com/docs/database/android/save-data
            mFirebaseDatabaseReference.child(ITEMS_CHILD).child(todoItems.get(pos).getKey()).child("text").setValue(str);

            itemsAdapter.notifyDataSetChanged();
            // writeItems();
        }
    }

    // slide 23
//    public void onAddItem(View v) {
//
//        EditText etNewItem = (EditText) findViewById(R.id.etNewItem);
//        String itemText = etNewItem.getText().toString();
//
//        System.out.println("onAddItem: " + itemText);
//
//        // write to firebase
//        TodoItem todoItem = new
//                TodoItem(itemText,
//                mUsername,
//                mPhotoUrl, "", "", "");
//
//        mFirebaseDatabaseReference.child(ITEMS_CHILD)
//                .push().setValue(todoItem);
//
//        etNewItem.setText("");
//    }

    // slide 25
    private void readItems() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, "todo.txt");
        try {
            items = new ArrayList<String>(FileUtils.readLines(todoFile));
        }
        catch (IOException e) {
            items = new ArrayList<String>();
        }
    }

    private void readTodoItems() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, "todo.txt");
        try {
            items = new ArrayList<String>(FileUtils.readLines(todoFile));
        }
        catch (IOException e) {
            items = new ArrayList<String>();
        }

        todoItems = new ArrayList<TodoItem>();

//        for (int i=0; i<items.size(); i++) {
//            TodoItem t = new TodoItem("text: " + items.get(i), "name: " + items.get(i), "", "" + i, true);
//            todoItems.add(t);
//        }
    }


    private void writeItems() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, "todo.txt");
        try {
            FileUtils.writeLines(todoFile, items);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d("i", "changed to landscape");
            inPortraitMode = 0;
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();

            for (int i=0; i<todoItems.size(); i++) {
                String backdrop = todoItems.get(i).getBackdropUrl();
                todoItems.get(i).setPhotoUrl(backdrop);
            }

            // setContentView(R.layout.todo_item);
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Log.d("i", "changed to portrait");
            inPortraitMode = 1;
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();

            for (int i=0; i<todoItems.size(); i++) {
                String poster = todoItems.get(i).getPosterUrl();
                todoItems.get(i).setPhotoUrl(poster);
            }

            // setContentView(R.layout.todo_item);
        }

        itemsAdapter.notifyDataSetChanged();
    }

    /**
     * Async task class to get json by making HTTP call
     */
    private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    //JSONArray contacts = jsonObj.getJSONArray("contacts");
                    JSONArray contacts = jsonObj.getJSONArray("results");

                    // looping through All Contacts
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);

                        String id = c.getString("id");
                        String title = c.getString("title");
                        String overview = c.getString("overview");
                        String poster_path = c.getString("poster_path");
                        String backdrop_path = c.getString("backdrop_path");
                        String release_date = c.getString("release_date");
                        String vote_average = c.getString("vote_average");

                        // tmp hash map for single contact
                        HashMap<String, String> contact = new HashMap<>();

                        // adding each child node to HashMap key => value
                        contact.put("id", id);
                        contact.put("title", title);
                        contact.put("overview", overview);
                        contact.put("poster_path", poster_path);
                        contact.put("backdrop_path", backdrop_path);
                        contact.put("vote_average", vote_average);
                        contact.put("release_date", release_date);

//                        contact.put("email", email);
//                        contact.put("mobile", mobile);

                        // adding contact to contact list
                        contactList.add(contact);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */


            // populate the to do items with stuff from contact list

            // todoItems = new ArrayList<TodoItem>();

            for (int i=0; i<contactList.size(); i++) {
                // tmp hash map for single contact
                HashMap<String, String> contact = contactList.get(i);
                String title = contact.get("title");
                String overview = contact.get("overview");
                String id = contact.get("id");
                String backdrop = contact.get("backdrop_path");
                String poster = contact.get("poster_path");
                String rating = contact.get("vote_average") + "";
                String releaseDate = contact.get("release_date");

                String backdrop_url = "https://image.tmdb.org/t/p/w500" + backdrop;
                String poster_url = "https://image.tmdb.org/t/p/w185" + poster;
                String url_for_display = "";

                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
//                    image = "image_portrait.png";
                    Log.d("i", "display poster image");
                    url_for_display = poster_url;
                    // ...
                } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                    image = "image_landscape.png";
                    Log.d("i", "display backdrop image");
                    url_for_display = backdrop_url;
                    // ...
                }

                TodoItem t = new TodoItem(title, overview, url_for_display, id, poster_url, backdrop_url);
                t.rating = rating;
                t.releaseDate = releaseDate;
                todoItems.add(t);
            }
            TodoItemAdapter adapter = new TodoItemAdapter(MainActivity.this, todoItems);
            lvItems.setAdapter(adapter);

        }

    }

}
