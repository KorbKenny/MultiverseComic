package com.korbkenny.multiversecomic;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.korbkenny.multiversecomic.home.MainActivity;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private EditText mEditUsername, mEditEmail, mEditPassword;
    private Button mSignUpButton, mLogInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        createAuthListener();
        simpleSetup();

        //============================
        //       Sign Up Button
        //============================
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLogInButton.setEnabled(false);
                mSignUpButton.setEnabled(false);
                signUp(mEditEmail.getText().toString(),mEditPassword.getText().toString());
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
                logIn(mEditEmail.getText().toString(),mEditPassword.getText().toString());
            }
        });
    }

    //============================
    //       Sign Up
    //============================
    private void signUp(String email, String password) {
        if(!validateLoginForm()){
            return;
        }
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()){
                    mLogInButton.setEnabled(true);
                    mSignUpButton.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Failed to sign up", Toast.LENGTH_SHORT).show();
                } else {
                    final String userId = task.getResult().getUser().getUid();
                    final String userEmail = task.getResult().getUser().getEmail();
                    FirebaseDatabase db = FirebaseDatabase.getInstance();
                    final DatabaseReference userRef = db.getReference("Users").child(userId);
                    new AsyncTask<Void,Void,Void>(){
                        @Override
                        protected Void doInBackground(Void... voids) {
                            userRef.child("userid").setValue(userId);
                            userRef.child("useremail").setValue(userEmail);
                            userRef.child("pageUpdate").setValue(GlobalPageActivity.DB_NULL);
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
        if(!validateLoginForm()){
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
//        mEditUsername = (EditText)findViewById(R.id.username_edit);
        mEditEmail = (EditText)findViewById(R.id.sign_up_email);
        mEditPassword = (EditText)findViewById(R.id.sign_up_password);
        mLogInButton = (Button)findViewById(R.id.log_in_button);
        mSignUpButton = (Button)findViewById(R.id.sign_up_button);
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
                    intent.putExtra("MyUserId",user.getUid());
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

    private boolean validateLoginForm() {
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

//        String username = mEditUsername.getText().toString();
//        if(TextUtils.isEmpty(username)){
//            mEditUsername.setError("Required.");
//            valid = false;
//        } else if (username.length() < 4){
//            mEditUsername.setError("Too Short.");
//            valid = false;
//        } else {
//            mEditUsername.setError(null);
//        }
        return valid;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAuth.removeAuthStateListener(mAuthListener);
    }
}
