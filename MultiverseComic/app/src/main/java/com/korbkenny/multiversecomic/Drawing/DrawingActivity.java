package com.korbkenny.multiversecomic.Drawing;

import android.app.Dialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.korbkenny.multiversecomic.R;

import java.io.File;
import java.io.FileOutputStream;

public class DrawingActivity extends AppCompatActivity {
    public static final String FILE_NAME = "mypage.png";
    private DrawView mDrawView;
    private Button mUndoButton, mSaveButton;
    private FirebaseDatabase db;
    private DatabaseReference dPageRef;
    private String iPageId, iUserId, mYellowText;
    private TextView mBrushSize, mOpacity, mLoadingBg;
    private ProgressBar mLoadingCircle;
    private ImageView mCurrentColor;
    private EditText mEditText;
    private Bitmap mBitmapToSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        //=================================
        //  Setup Methods
        //=================================
        simpleSetup();
        changeSizeSeekbar();
        changeOpacitySeekbar();

        //=================================
        //  Undo Button
        //=================================
        mUndoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawView.undo();
            }
        });

        //=================================
        //  Save Button
        //=================================
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createSaveDialog();
            }
        });
    }

    //=================================
    //      Save Dialog
    //=================================
    private void createSaveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.submit_drawing_dialog,null));
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                submitDrawing();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create().show();
    }


    //=======================================
    //    Submit Drawing
    //=======================================
    private void submitDrawing() {
        mBitmapToSave = null;
        mDrawView.setDrawingCacheEnabled(true);
        mBitmapToSave = mDrawView.getDrawingCache();


        new AsyncTask<Void,Void,String>(){
            @Override
            protected void onPreExecute() {
                mLoadingBg.setVisibility(View.VISIBLE);
                mLoadingCircle.setVisibility(View.VISIBLE);
                mYellowText = mEditText.getText().toString();

            }

            @Override
            protected String doInBackground(Void... voids) {
                return saveImageToDisk(mBitmapToSave);
            }

            @Override
            protected void onPostExecute(final String path) {
                new AsyncTask<Void,Void,Void>(){
                    @Override
                    protected Void doInBackground(Void... voids) {
                        Uri image = Uri.fromFile(new File(path + "/" + FILE_NAME));
                        StorageReference storageRef = FirebaseStorage.getInstance().getReference("Pages").child(iPageId).child(FILE_NAME);
                        storageRef.putFile(image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                                new AsyncTask<Void, Void, Void>() {
                                    @Override
                                    protected void onPreExecute() {
                                    }

                                    @Override
                                    protected Void doInBackground(Void... voids) {
                                        if (taskSnapshot.getDownloadUrl() != null) {
                                            dPageRef.child("text").setValue(mYellowText);
                                            dPageRef.child("image").setValue(taskSnapshot.getDownloadUrl().toString());
                                            dPageRef.child("user").setValue(iUserId);
                                        }
                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Void voidsa) {
                                        finish();
                                    }
                                }.execute();
                            }
                        });
                        return null;
                    }
                }.execute();
            }
        }.execute();

    }

    //=================================
    //      Opacity Seekbar
    //=================================
    private void changeOpacitySeekbar() {
        SeekBar opacityPicker = (SeekBar)findViewById(R.id.seekbar_opacity_picker);
        opacityPicker.setMax(100);
        opacityPicker.setProgress(100);
        opacityPicker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                String opacityText = "Opacity: " + String.valueOf(i) + "%";
                mOpacity.setText(opacityText);
                mDrawView.setOpacity(i);
                mCurrentColor.setAlpha((float)i/100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    //=================================
    //      Size Seekbar
    //=================================
    private void changeSizeSeekbar() {
        SeekBar sizePicker = (SeekBar)findViewById(R.id.seekbar_size_picker);
        sizePicker.setMax(34);
        sizePicker.setProgress(12);
        sizePicker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                String sizeText = "Size: " + String.valueOf(i+2) + "px";
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
    }

    //=================================
    //      Simple Setup
    //=================================
    private void simpleSetup() {
        iPageId = getIntent().getStringExtra("PageId");
        iUserId = getIntent().getStringExtra("MyUserId");

        db = FirebaseDatabase.getInstance();
        dPageRef = db.getReference("Global").child(iPageId);

        mDrawView = (DrawView) findViewById(R.id.draw_view);
        mUndoButton = (Button) findViewById(R.id.undo_button);
        mSaveButton = (Button) findViewById(R.id.save_button);
        mBrushSize = (TextView) findViewById(R.id.size_textview);
        mOpacity = (TextView) findViewById(R.id.opacity_textview);
        mCurrentColor = (ImageView) findViewById(R.id.current_color);
        mEditText = (EditText) findViewById(R.id.yellow_edit_text);

        mLoadingBg = (TextView) findViewById(R.id.loading_drawing_bg);
        mLoadingCircle = (ProgressBar) findViewById(R.id.loading_drawing_circle);
    }

    //=======================================
    //    From xml, when a paint is clicked
    //=======================================
    public void paintClicked(View view){
        String color = view.getTag().toString();
        mDrawView.setColor(color);
        mCurrentColor.setBackgroundColor(Color.parseColor(color));
    }


    //=======================================
    //  Save Image to Disk before uploading
    //=======================================
    private String saveImageToDisk(Bitmap bitmap){
        Bitmap bitmapToSave = Bitmap.createScaledBitmap(bitmap,400,400,false);

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir",MODE_PRIVATE);
        File imagePath = new File(directory,FILE_NAME);

        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(imagePath);
            bitmapToSave.compress(Bitmap.CompressFormat.PNG,0,fos);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }


    //=================================
    //  Change if BeingWorkedOn
    //=================================
    @Override
    protected void onPause() {
        dPageRef.child("beingWorkedOn").setValue("no");
        super.onPause();
    }

    @Override
    protected void onResume() {
        dPageRef.child("beingWorkedOn").setValue("yes");
        super.onResume();
    }

    @Override
    protected void onStop() {
        dPageRef.child("beingWorkedOn").setValue("no");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        dPageRef.child("beingWorkedOn").setValue("no");
        super.onDestroy();
    }
}
