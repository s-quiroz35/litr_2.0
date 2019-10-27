package com.trashboys.litr;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

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
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.squareup.picasso.*;
//import com.squareup.okhttp.internal.*;

import static android.support.constraint.Constraints.TAG;

public class UserInfoFragment extends Fragment {

    private ArrayList<QueryDocumentSnapshot> userslist = new ArrayList<>();
    private ArrayList<QueryDocumentSnapshot> litterlist = new ArrayList<>();
    //private ArrayList<Double> litterlat = new ArrayList<>();
    //private ArrayList<Double> litterlong = new ArrayList<>();
    private ArrayList<String> litter = new ArrayList<>();
    private ArrayList<String> litterpics = new ArrayList<>();
    private Activity the_context = this.getActivity();
    private TextView points;
    private long pointval = 0;
    private ImageView pic;
    private String pictureURL;
    private ListView list;
    private int i = 0;
    private String str;
    TextView userNameText;

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
        userNameText = (TextView) view.findViewById(R.id.usernameText);
        pic = (ImageView) view.findViewById(R.id.profile_pic);

        points = (TextView) view.findViewById(R.id.points_earned);
        list=(ListView) view.findViewById(R.id.list);

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
                                userslist.add(document);
                                if (userslist.get(0) != null && userslist.get(0).get("profilepicture") != null) {
                                    pointval += (Long) userslist.get(0).get("points");
                                    //pic = getBitmapFromURL((String) list.get(0).get("profilepicture"));
                                    pictureURL = (String) userslist.get(0).get("profilepicture");
                                    userNameText.setText("Username: " + userslist.get(0).get("username"));
                                    //Toast toast = Toast.makeText(getActivity(), pictureURL, Toast.LENGTH_LONG);
                                    //toast.show();
                                    Picasso.get().load(pictureURL).resize(50,50).into(pic);
                                }
                                Long pointvalue = new Long(pointval);
                                points.setText(pointvalue.toString());
                                //Integer pointvalue = list.size();
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        //litter.add("yeyey");
        //litterpics.add("https://i.imgur.com/C8ENv8y.jpg");
        db.collection("litter")
                .whereEqualTo("recorder", mAuth.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                litterlist.add(document);
                                Toast toast = Toast.makeText(getActivity(), (String) document.get("recorder"), Toast.LENGTH_LONG);
                                toast.show();
                                if (litterlist.get(i) != null && litterlist.get(i).get("geolocation") != null) {
                                    double lat = ((GeoPoint) litterlist.get(i).get("geolocation")).getLatitude();
                                    double longi = ((GeoPoint) litterlist.get(i).get("geolocation")).getLongitude();

                                    litter.add("Latitude: " + lat + ", Longitude: " + longi);
                                    //litterpics.add((String) litterlist.get(i).get("litterPicture"));
                                    str = (String) litterlist.get(i).get("litterPicture");

                                    str = str.substring(str.indexOf('/') + 1, str.indexOf('.'));
                                    new GetContacts().execute();
                                    //litterpics.add("https://i.imgur.com/C8ENv8y.jpg");

                                }
                                //litter.add("yeyey");
                                //litterpics.add("https://i.imgur.com/C8ENv8y.jpg");
                                //listAppend();
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                i++;
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
        /*ArrayList<String> testlist1 = new ArrayList<>();
        testlist1.add("test");
        testlist1.add("test");
        testlist1.add("test");
        ArrayList<String> testlist2 = new ArrayList<>();
        testlist2.add("https://i.imgur.com/C8ENv8y.jpg");
        testlist2.add("https://i.imgur.com/C8ENv8y.jpg");
        testlist2.add("https://i.imgur.com/C8ENv8y.jpg");*/

//        Integer theprint = list.size();

        //userNameText.setText(Integer.toString())

        //Picasso.get().load("https://i.imgur.com/C8ENv8y.jpg").into(picture);

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
    private void listAppend() {
        LitterAdapter adapter = new LitterAdapter(this.getActivity(), litter, litterpics);
        list.setAdapter(adapter);
    }


    private class GetContacts extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            str = "https://firebasestorage.googleapis.com/v0/b/litr-v2.appspot.com/o/litter%2F201910270254" + ".txt.jpg";
            String jsonStr = sh.makeServiceCall(str);

            Log.e(TAG, "Response from url: " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    str = str + "?alt=media&token=" + jsonObj.getString("downloadTokens");
                    //Toast.makeText(getActivity(),
                     //       "Couldn't get json from server. Check LogCat for possible errors!",
                       //     Toast.LENGTH_LONG).show();

                    // Getting JSON Array node
                    //JSONArray contacts = jsonObj.getJSONArray("contacts");

                    // looping through All Contacts

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());

                }

            } else {
                Log.e(TAG, "Couldn't get json from server.");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            litterpics.add(str);
            listAppend();
        }
    }
}
