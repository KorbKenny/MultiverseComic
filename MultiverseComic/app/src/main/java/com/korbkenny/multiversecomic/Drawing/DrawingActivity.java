package com.korbkenny.multiversecomic.drawing;

import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.korbkenny.multiversecomic.Constants;
import com.korbkenny.multiversecomic.R;

import java.io.File;
import java.io.FileOutputStream;

public class DrawingActivity extends AppCompatActivity {
    public static final String FILE_NAME = "mypage.png";
    private DrawView mDrawView;
    private Button mUndoButton, mSaveButton;
    private FirebaseDatabase db;
    private DatabaseReference dPageRef;
    private StorageReference dStorageRef;
    private String iPageId, iUserId, iGroupId, mYellowText, mFromUser, iFromPageId;
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
                        if(iGroupId==null) {
                            dStorageRef = FirebaseStorage.getInstance().getReference(Constants.PAGES).child(iPageId).child(FILE_NAME);
                        } else {
                            dStorageRef = FirebaseStorage.getInstance().getReference(Constants.GROUPS).child(iGroupId).child(iPageId).child(FILE_NAME);
                        }
                        dStorageRef.putFile(image).addOnSuccessListener(DrawingActivity.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                                new AsyncTask<Void, Void, Void>() {
                                    @Override
                                    protected void onPreExecute() {
                                    }

                                    @Override
                                    protected Void doInBackground(Void... voids) {
                                        if (taskSnapshot.getDownloadUrl() != null) {
                                            dPageRef.child(Constants.TEXT).setValue(mYellowText);
                                            dPageRef.child(Constants.IMAGE).setValue(taskSnapshot.getDownloadUrl().toString());
                                            if(iGroupId==null) {
                                                dPageRef.child(Constants.USER).setValue(iUserId);
                                                DatabaseReference updatedPageRef = db.getReference(Constants.USERS).child(mFromUser).child("pageUpdate");
                                                updatedPageRef.setValue(iPageId);
                                            } else {
                                                dPageRef.child(Constants.IMAGE_USER).setValue(iUserId);
                                            }
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
        sizePicker.setProgress(6);
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
        iPageId = getIntent().getStringExtra(Constants.PAGE_ID);
        iUserId = getIntent().getStringExtra(Constants.MY_USER_ID);
        iGroupId = getIntent().getStringExtra(Constants.GROUP_ID);
        iFromPageId = getIntent().getStringExtra(Constants.FROM_PAGE_ID);

        mFromUser = getIntent().getStringExtra(Constants.FROM_USER);

        db = FirebaseDatabase.getInstance();
        if(iGroupId==null) {
            dPageRef = db.getReference(Constants.GLOBAL).child(iPageId);
        } else {
            dPageRef = db.getReference(Constants.GROUPS).child(iGroupId).child(iPageId);
        }

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
    protected void onResume() {
        dPageRef.child(Constants.BEING_WORKED_ON).setValue(Constants.BEING_WORKED_ON_YES);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        dPageRef.child(Constants.BEING_WORKED_ON).setValue(Constants.BEING_WORKED_ON_NO);
        super.onDestroy();
    }
}
