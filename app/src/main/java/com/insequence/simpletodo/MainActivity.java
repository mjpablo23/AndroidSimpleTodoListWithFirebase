package com.insequence.simpletodo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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

// comment

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener  {

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
//    private String mUsername;
//    private String mPhotoUrl;

    private static final String TAG = "MainActivity";
    public static final String MESSAGES_CHILD = "messages";
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
    ArrayAdapter<String> itemsAdapter;
    ListView lvItems;

    // slide 24
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startFB();

        lvItems = (ListView) findViewById(R.id.lvItems);
        // items = new ArrayList<>();
        readItems();
        itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        lvItems.setAdapter(itemsAdapter);
        //items.add("First Item");
        //items.add("Second Item");
        setupListViewListener();
    }

    private void startFB() {
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth    .getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

//        // Initialize ProgressBar and RecyclerView.
//        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
//        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
//        mLinearLayoutManager = new LinearLayoutManager(this);
//        mLinearLayoutManager.setStackFromEnd(true);
//        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
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
                items.remove(pos);
                itemsAdapter.notifyDataSetChanged();
                writeItems();
                return true;
            }
        });

        // setContentView(lvItems);
        // http://stackoverflow.com/questions/9097723/adding-an-onclicklistener-to-listview-android
        // http://stackoverflow.com/questions/36917725/error-setonclicklistener-from-an-android-app
        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View item, int pos, long id) {
                Object o = lvItems.getItemAtPosition(pos);
                String str = (String) o;
                System.out.println("str: " + o);
                launchEditItem(str, pos);
            }
        });



    }

    private final int REQUEST_CODE  = 20;
    // http://guides.codepath.com/android/Using-Intents-to-Create-Flows
    public void launchEditItem(String str, int pos) {
        Intent i = new Intent(MainActivity.this, EditItemActivity.class);
        i.putExtra("str", str);
        i.putExtra("pos", pos);
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
            itemsAdapter.notifyDataSetChanged();
            writeItems();
        }
    }

    // slide 23
    public void onAddItem(View v) {

        EditText etNewItem = (EditText) findViewById(R.id.etNewItem);
        String itemText = etNewItem.getText().toString();
        System.out.println("onAddItem: " + itemText);
        itemsAdapter.add(itemText);
        etNewItem.setText("");
        writeItems();
    }

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

    private void writeItems() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, "todo.txt");
        try {
            FileUtils.writeLines(todoFile, items);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
