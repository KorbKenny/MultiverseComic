package com.korbkenny.multiversecomic;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.korbkenny.multiversecomic.home.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private EditText mEditUsername, mEditEmail, mEditPassword, mEditLoginEmailUsername, mEditLoginPassword;
    private Button mSignUpButton, mLogInButton;
    private List<String> mUsernameList;
    private String mCreatingUsername;
    private FirebaseDatabase db;
    private DatabaseReference dUserRef;
    private RelativeLayout mSignUpLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        createAuthListener();
        getListOfUsernames();
        simpleSetup();

        //============================
        //       Sign Up Button
        //============================
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLogInButton.setEnabled(false);
                mSignUpButton.setEnabled(false);
                if (mUsernameList.contains(mEditUsername.getText().toString().toLowerCase())) {
                    mEditUsername.setError("Name already taken");
                    mSignUpButton.setEnabled(true);
                } else {
                    mEditUsername.setError(null);
                    mCreatingUsername = mEditUsername.getText().toString();
                    signUp(mEditEmail.getText().toString(), mEditPassword.getText().toString());
                }
            }
        });

        //============================
        //        Log In Button
        //============================
        mLogInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLogInButton.setEnabled(false);
                mSignUpButton.setEnabled(false);
                logIn(mEditLoginEmailUsername.getText().toString(),mEditLoginPassword.getText().toString());
            }
        });

        mSignUpLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSignUpLayout.setVisibility(View.GONE);
                mLogInButton.setVisibility(View.GONE);
                mEditLoginEmailUsername.setVisibility(View.GONE);
                mEditLoginPassword.setVisibility(View.GONE);

                mSignUpButton.setVisibility(View.VISIBLE);
                mEditUsername.setVisibility(View.VISIBLE);
                mEditUsername.requestFocus();
                InputMethodManager inputMethodManager =
                        (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInputFromWindow(
                        mEditUsername.getApplicationWindowToken(),
                        InputMethodManager.SHOW_IMPLICIT, 0);
                mEditPassword.setVisibility(View.VISIBLE);
                mEditEmail.setVisibility(View.VISIBLE);

            }
        });
    }

    //===============================
    //  Get all the usernames
    //===============================
    private void getListOfUsernames() {
        mUsernameList = new ArrayList<>();
        db = FirebaseDatabase.getInstance();
        dUserRef = db.getReference("usernames");
        dUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                new AsyncTask<Void,Void,Void>(){
                    @Override
                    protected Void doInBackground(Void... voids) {
                        if(dataSnapshot.getValue()!=null){
                            for(DataSnapshot ds:dataSnapshot.getChildren()){
                                mUsernameList.add(ds.getValue(String.class).toLowerCase());
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        mSignUpButton.setEnabled(true);
                    }
                }.execute();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //============================
    //       Sign Up
    //============================
    private void signUp(String email, String password) {
        if(!validateSignInForm()){
            return;
        }
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()){
                    mLogInButton.setEnabled(true);
                    mSignUpButton.setEnabled(true);
                    mCreatingUsername = null;
                    Toast.makeText(LoginActivity.this, "Failed to sign up", Toast.LENGTH_SHORT).show();
                } else {
                    final String userId = task.getResult().getUser().getUid();
                    final String userEmail = task.getResult().getUser().getEmail();
                    FirebaseDatabase db = FirebaseDatabase.getInstance();
                    final DatabaseReference userRef = db.getReference(Constants.USERS).child(userId);
                    new AsyncTask<Void,Void,Void>(){
                        @Override
                        protected Void doInBackground(Void... voids) {
                            dUserRef.child(userId).setValue(mCreatingUsername);
                            userRef.child(Constants.USER_NAME).setValue(mCreatingUsername);
                            userRef.child(Constants.USER_ID).setValue(userId);
                            userRef.child(Constants.USER_EMAIL).setValue(userEmail);
                            userRef.child(Constants.PAGE_UPDATE).setValue(Constants.DB_NULL);
                            return null;
                        }
                    }.execute();
                }
            }
        });
    }

    //============================
    //       Log In
    //============================
    private void logIn(String email, String password) {
        if(!validateLogInForm()){
            mLogInButton.setEnabled(true);
            mSignUpButton.setEnabled(true);
            return;
        }
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()){
                    mLogInButton.setEnabled(true);
                    mSignUpButton.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Failed to log in", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //============================
    //      Set up views
    //============================
    private void simpleSetup() {
        mEditLoginEmailUsername = (EditText)findViewById(R.id.username_or_email);
        mEditLoginPassword = (EditText)findViewById(R.id.log_in_password);

        mEditUsername = (EditText)findViewById(R.id.username_edit);
        mEditEmail = (EditText)findViewById(R.id.sign_up_email);
        mEditPassword = (EditText)findViewById(R.id.sign_up_password);

        mLogInButton = (Button)findViewById(R.id.log_in_button);
        mSignUpButton = (Button)findViewById(R.id.sign_up_button);

        mSignUpLayout = (RelativeLayout)findViewById(R.id.text_to_sign_up);
    }

    //============================
    //      Auth Listener
    //============================
    private void createAuthListener() {
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user!=null){
                      // This is the new home page, with the viewpager and fragments and stuff
                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);

                    //  Old home page.
//                    Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
                    intent.putExtra(Constants.MY_USER_ID,user.getUid());
                    startActivity(intent);
                    finish();
                }
            }
        };
        mAuth.addAuthStateListener(mAuthListener);
    }


    //==============================================
    //  Extra methods for validating email/password
    //==============================================
    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private boolean validateSignInForm() {
        boolean valid = true;

        String email = mEditEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEditEmail.setError("Required.");
            valid = false;
        } else if (!isEmailValid(email)){
            mEditEmail.setError("Not a Valid Email.");
            valid = false;
        } else {
            mEditEmail.setError(null);
        }

        String password = mEditPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mEditPassword.setError("Required.");
            valid = false;
        } else if (!isPasswordValid(password)) {
            mEditPassword.setError("Too Short.");
            valid = false;
        } else {
            mEditPassword.setError(null);
        }

        String username = mEditUsername.getText().toString();
        if(TextUtils.isEmpty(username)){
            mEditUsername.setError("Required.");
            valid = false;
        } else if (username.length() < 4){
            mEditUsername.setError("Too Short.");
            valid = false;
        } else {
            mEditUsername.setError(null);
        }
        mSignUpButton.setEnabled(true);
        return valid;
    }


    private boolean validateLogInForm() {
        boolean valid = true;

        String email = mEditLoginEmailUsername.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEditLoginEmailUsername.setError("Required.");
            valid = false;
        } else if (!isEmailValid(email)){
            mEditLoginEmailUsername.setError("Not a Valid Email.");
            valid = false;
        } else {
            mEditLoginEmailUsername.setError(null);
        }

        String password = mEditLoginPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mEditLoginPassword.setError("Required.");
            valid = false;
        } else if (!isPasswordValid(password)) {
            mEditLoginPassword.setError("Too Short.");
            valid = false;
        } else {
            mEditLoginPassword.setError(null);
        }

        return valid;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAuth.removeAuthStateListener(mAuthListener);
    }
}
