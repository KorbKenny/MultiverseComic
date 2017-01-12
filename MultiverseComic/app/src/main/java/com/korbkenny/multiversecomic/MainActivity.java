package com.korbkenny.multiversecomic;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    public static String FIRST_PAGE_ID = "-KaAQza-n3z7al9Egl0N";
    FirebaseDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        db = FirebaseDatabase.getInstance();
//        DatabaseReference global = db.getReference("Global");



//                PAGE_ID = global.push().getKey();
//
//        page.child("text").setValue(PageActivity.DB_NULL);
//        page.child("left").setValue(PageActivity.DB_NULL);
//        page.child("right").setValue(PageActivity.DB_NULL);
//        page.child("image").setValue(PageActivity.DB_NULL);
//        page.child("from").setValue(PageActivity.DB_NULL);
//        page.child("user").setValue(PageActivity.DB_NULL);
//        page.child("beingworkedon").setValue("no");


        Button startFromBeginning = (Button)findViewById(R.id.bt_start_from_beginning);

        startFromBeginning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,PageActivity.class);
                intent.putExtra("nextpage",FIRST_PAGE_ID);
                startActivity(intent);
            }
        });


    }
}
