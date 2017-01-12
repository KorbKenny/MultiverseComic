package com.korbkenny.multiversecomic.Drawing;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.korbkenny.multiversecomic.R;

public class DrawingActivity extends AppCompatActivity {
    private DrawView mDrawView;
    private Button mUndoButton;
    private FirebaseDatabase db;
    private DatabaseReference globalRef;
    private String iPageId;
    private TextView mBrushSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        iPageId = getIntent().getStringExtra("PageId");
        mDrawView = (DrawView) findViewById(R.id.draw_view);
        mUndoButton = (Button) findViewById(R.id.undo_button);
        mBrushSize = (TextView) findViewById(R.id.size_of_brush);

        SeekBar sizePicker = (SeekBar)findViewById(R.id.seekbar_size_picker);
        sizePicker.setMax(34);
        sizePicker.setProgress(12);
        sizePicker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                String sizeText = String.valueOf(i+2) + "px";
                mBrushSize.setText(sizeText);
                mDrawView.setBrushSize(i+2);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        db = FirebaseDatabase.getInstance();
        globalRef = db.getReference("Global").child(iPageId);

        mUndoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawView.undo();
            }
        });

        mUndoButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                finish();
                return false;
            }
        });
    }

    @Override
    protected void onPause() {
        globalRef.child("beingworkedon").setValue("no");
        super.onPause();
    }

    @Override
    protected void onResume() {
        globalRef.child("beingworkedon").setValue("yes");
        super.onResume();
    }

    @Override
    protected void onStop() {
        globalRef.child("beingworkedon").setValue("no");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        globalRef.child("beingworkedon").setValue("no");
        super.onDestroy();
    }
}
