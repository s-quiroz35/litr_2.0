package com.trashboys.litr;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.*;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements
        View.OnClickListener {

    private static final String TAG = "EmailPassword";

    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mPasswordConfirmField;
    private EditText mUsernameField;
    private Button mSignInButton;
    private TextView mForgotPassword;
    private LinearLayout toggle;
    private boolean signUp = false;
    private boolean recover = false;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        // Views
        mEmailField = findViewById(R.id.fieldEmail);
        mPasswordField = findViewById(R.id.fieldPassword);
        mPasswordConfirmField = findViewById(R.id.fieldPasswordConfirm);
        mUsernameField = findViewById(R.id.fieldUsername);
        mForgotPassword = findViewById(R.id.forgotPassword);
        toggle = findViewById(R.id.createToggleContainer);

        // Buttons
        mSignInButton = findViewById(R.id.emailSignInButton);
        mSignInButton.setOnClickListener(this);
        findViewById(R.id.emailCreateAccountButton).setOnClickListener(this);
        findViewById(R.id.forgotPassword).setOnClickListener(this);
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }
    // [END on_start_check_user]

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }
        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);

                            ///////////////ADD TO DATABASE/////////////////
                            // Create a new user with a first and last name
                            Map<String, Object> newUser = new HashMap<>();
                            newUser.put("username", mUsernameField.getText().toString());
                            newUser.put("email", mEmailField.getText().toString());
                            newUser.put("UID", mAuth.getCurrentUser().getUid());
                            newUser.put("points", 5);
                            newUser.put("profilepicture", "https://i.imgur.com/C8ENv8y.jpg");

                            // Add a new document with a generated ID
                            db.collection("users")
                                    .add(newUser)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error adding document", e);
                                        }
                                    });


                            //END ADD TO DATABASE//
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                    }
                });





    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }


        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                    }
                });
    }



    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }
        String password = mPasswordField.getText().toString();

        if (!recover) {
            if (TextUtils.isEmpty(password)) {
                mPasswordField.setError("Required.");
                valid = false;
            } else {
                mPasswordField.setError(null);
            }
        }

        if (signUp) {
            String passwordConfirm = mPasswordConfirmField.getText().toString();
            if (TextUtils.isEmpty(passwordConfirm)) {
                mPasswordConfirmField.setError("Required.");
                valid = false;
            } else if (!password.equals(passwordConfirm)) {
                mPasswordConfirmField.setError("Passwords must match.");
                valid = false;
            } else {
                mPasswordConfirmField.setError(null);
            }

            String username = mUsernameField.getText().toString();
            if (TextUtils.isEmpty(username)) {
                mUsernameField.setError("Required.");
                valid = false;
            } else {
                mUsernameField.setError(null);
            }
        }

        return valid;
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
            myIntent.putExtra("user", user);
            LoginActivity.this.startActivity(myIntent);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.emailCreateAccountButton) {
            if (!signUp) {
                mSignInButton.setText("Sign Up");
                ViewGroup.LayoutParams params = mPasswordConfirmField.getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                mPasswordConfirmField.setLayoutParams(params);
                mUsernameField.setLayoutParams(params);
                mForgotPassword.setVisibility(View.INVISIBLE);
                toggle.setVisibility(View.INVISIBLE);
                signUp = true;
            }
        } else if (i == R.id.emailSignInButton) {
            if (recover) {
                if (!validateForm()) {
                    return;
                }

                mAuth.sendPasswordResetEmail(mEmailField.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "Success",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Sending password email failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                return;
            }
            if (!signUp) {
                signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
            } else {
                createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
            }
        } else if (i == R.id.forgotPassword) {
            mPasswordField.setVisibility(View.INVISIBLE);
            mForgotPassword.setVisibility(View.INVISIBLE);
            toggle.setVisibility(View.INVISIBLE);
            mSignInButton.setText("Recover Password");
            recover = true;
        }
    }


}
