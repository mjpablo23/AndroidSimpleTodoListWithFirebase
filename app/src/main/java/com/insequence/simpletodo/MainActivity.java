package com.insequence.simpletodo;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

// bug:  if all items are removed, it crashes

// make into todo list for groups

// need to make edit and remove work again. need to turn on blockDescendants in xml
// http://stackoverflow.com/questions/5551042/onitemclicklistener-not-working-in-listview

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
    //ArrayAdapter<String> itemsAdapter;
    // ArrayAdapter<TodoItem> itemsAdapter;
    TodoItemAdapter itemsAdapter;
    ListView lvItems;

    // slide 24
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startFB();

        lvItems = (ListView) findViewById(R.id.lvItems);
        // items = new ArrayList<>();
        // readItems();
        readItemsFromFirebase();

        System.out.println("setting items in itemsAdapter");
        // itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        // itemsAdapter = new ArrayAdapter<TodoItem>(this, android.R.layout.simple_list_item_1, todoItems);

        // TodoItemAdapter(Context context, ArrayList<TodoItem> users)
        itemsAdapter = new TodoItemAdapter(this, todoItems);
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
                String str = items.get(pos);
                System.out.println("str: " + str);
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
    public void onAddItem(View v) {

        EditText etNewItem = (EditText) findViewById(R.id.etNewItem);
        String itemText = etNewItem.getText().toString();

        System.out.println("onAddItem: " + itemText);
        // adding in firebase instead
//        itemsAdapter.add(itemText);

        // write to firebase
        TodoItem todoItem = new
                TodoItem(itemText,
                mUsername,
                mPhotoUrl, "", false);

        mFirebaseDatabaseReference.child(ITEMS_CHILD)
                .push().setValue(todoItem);

        etNewItem.setText("");
        // writeItems();
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

    // https://firebase.google.com/docs/database/android/retrieve-data
    private void readItemsFromFirebase() {
        items = new ArrayList<String>();
        todoItems = new ArrayList<TodoItem>();

        System.out.println("readItemsFromFirebase using child event listener");

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                System.out.println("onChildAdded:" + dataSnapshot);

                // A new comment has been added, add it to the displayed list
                // Comment comment = dataSnapshot.getValue(Comment.class);
                TodoItem todoItem = dataSnapshot.getValue(TodoItem.class);
                todoItem.setChecked((boolean) dataSnapshot.child("checked").getValue());
                todoItem.setKey(dataSnapshot.getKey());
                System.out.println("todoItem key: " + todoItem.getKey() + ", " + todoItem.getName() + ", " + todoItem.getText());
                items.add(todoItem.getText());
                todoItems.add(todoItem);
                itemsAdapter.notifyDataSetChanged();
                // ...
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                System.out.println("onChildChanged:" + dataSnapshot.getKey());

                TodoItem changedTodoItem = dataSnapshot.getValue(TodoItem.class);
                changedTodoItem.setChecked((boolean) dataSnapshot.child("checked").getValue());
                changedTodoItem.setKey(dataSnapshot.getKey());

                //did override of equals method for todoItems
                int ind = todoItems.indexOf(changedTodoItem);

                todoItems.set(ind, changedTodoItem);
                items.set(ind, changedTodoItem.getText());
                itemsAdapter.notifyDataSetChanged();

                // ...
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                System.out.println("onChildRemoved:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so remove it.
//                String commentKey = dataSnapshot.getKey();

                TodoItem removedTodoItem = dataSnapshot.getValue(TodoItem.class);
                removedTodoItem.setKey(dataSnapshot.getKey());

                //did override of equals method for todoItems
                int ind = todoItems.indexOf(removedTodoItem);

                todoItems.remove(ind);
                items.remove(ind);
                itemsAdapter.notifyDataSetChanged();

                // ...
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                // A comment has changed position, use the key to determine if we are
                // displaying this comment and if so move it.
//                Comment movedComment = dataSnapshot.getValue(Comment.class);
//                String commentKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                // Toast.makeText(mContext, "Failed to load comments.",
                   //     Toast.LENGTH_SHORT).show();
            }

        };
        mFirebaseDatabaseReference.child(ITEMS_CHILD).addChildEventListener(childEventListener);
    }


    // ----- no longer used -------
    private void readItemsFromFirebase_usingValueEventListener() {
        items = new ArrayList<String>();
        System.out.println("readItemsFromFirebase");
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                System.out.println("dataSnapshot: " + dataSnapshot);
                TodoItem todoItem = dataSnapshot.getValue(TodoItem.class);

                items.add(todoItem.getText());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mFirebaseDatabaseReference.child(ITEMS_CHILD).addValueEventListener(postListener);
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
