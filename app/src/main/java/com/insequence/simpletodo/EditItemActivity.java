package com.insequence.simpletodo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class EditItemActivity extends AppCompatActivity {

    private int pos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        String str = getIntent().getStringExtra("str");
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String backdrop= getIntent().getStringExtra("backdrop");
        String poster = getIntent().getStringExtra("poster");
        String rating = getIntent().getStringExtra("rating");
        String releaseDate = getIntent().getStringExtra("releaseDate");
        pos = getIntent().getIntExtra("pos", 0);

        Log.d("i", "pos: " + pos);
        TextView titleView = (TextView) findViewById(R.id.infoTitleView);
        TextView descriptionView = (TextView) findViewById(R.id.infoDescriptionView);
        TextView ratingView = (TextView) findViewById(R.id.infoRatingView);
        TextView releaseDateView = (TextView) findViewById(R.id.infoReleaseDateView);

        // don't know why they are reversed
        descriptionView.setText(title);
        titleView.setText(description);

        ratingView.setText("Average Rating: " + rating);
        releaseDateView.setText("Release Date: " + releaseDate);

//        EditText editText = (EditText) findViewById(R.id.editItemText);
//        editText.setText(str);

        // http://stackoverflow.com/questions/14327412/set-focus-on-edittext
//        editText.requestFocus();
    }

    public void save(View v) {
        System.out.println("save button");
        onSubmit(v);
    }

    // when the subactivity is complete then it can return the result to the parent
    public void onSubmit(View v) {
        System.out.println("onSubmit");
//        EditText etNewItem = (EditText) findViewById(R.id.editItemText);
//        String itemText = etNewItem.getText().toString();
//        System.out.println("save: " + itemText);

        Intent data = new Intent();
        data.putExtra("str", "itemText");
        data.putExtra("pos", pos);
        setResult(RESULT_OK, data);
        finish();
    }
}
