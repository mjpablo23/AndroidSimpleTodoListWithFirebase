package com.insequence.simpletodo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

// comment

public class MainActivity extends AppCompatActivity {

    ArrayList<String> items;
    ArrayAdapter<String> itemsAdapter;
    ListView lvItems;

    // slide 24
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lvItems = (ListView) findViewById(R.id.lvItems);
        // items = new ArrayList<>();
        readItems();
        itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        lvItems.setAdapter(itemsAdapter);
        //items.add("First Item");
        //items.add("Second Item");
        setupListViewListener();
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
