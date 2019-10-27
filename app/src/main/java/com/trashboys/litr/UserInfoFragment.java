package com.trashboys.litr;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import static android.support.constraint.Constraints.TAG;

public class UserInfoFragment extends Fragment {

    private static ArrayList<QueryDocumentSnapshot> list = new ArrayList<>();
    private static TextView points;
    private static long pointval = 0;
    private static Bitmap pic;

    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //just change the fragment_dashboard
        //with the fragment you want to inflate
        //like if the class is HomeFragment it should have R.layout.home_fragment
        //if it is DashboardFragment it should have R.layout.fragment_dashboard
        mAuth = FirebaseAuth.getInstance();

        // Access a Cloud Firestore instance from your Activity
        //FirebaseFirestore db = FirebaseFirestore.getInstance();


        View view = inflater.inflate(R.layout.fragment_user_info, null);
        TextView userNameText = (TextView) view.findViewById(R.id.usernameText);
        userNameText.setText("Username: " + mAuth.getUid());
        points = (TextView) view.findViewById(R.id.points_earned);

        //View view = inflater.inflate(R.layout.fragment_user_info, null);
        //TextView userNameText = (TextView) view.findViewById(R.id.usernameText);

        //ArrayList<QueryDocumentSnapshot> list = new ArrayList<>();

        db.collection("users")
                .whereEqualTo("UID", mAuth.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                list.add(document);
                                if (list.get(0) != null && list.get(0).get("profilepicture") != null) {
                                    //pointval += (Long) list.get(0).get("points");
                                    pic = getBitmapFromURL((String) list.get(0).get("profilepicture"));
                                }
                                Long pointvalue = new Long(pointval);
                                //Integer pointvalue = list.size();
                                points.setText(pointvalue.toString());
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

//        Integer theprint = list.size();
//        points.setText(theprint.toString());
        //userNameText.setText(Integer.toString())

        Button signOutButton = view.findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener(mCorkyListener);

        return view;
    }

    private void signOut() {
        mAuth.signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
    }

    private View.OnClickListener mCorkyListener = new View.OnClickListener() {
        public void onClick(View v) {
            int i = v.getId();
            if (i == R.id.signOutButton) {
                signOut();
            }
        }
    };

    public Bitmap getBitmapFromURL(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
