package com.trashboys.litr;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.*;
import android.widget.Button;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.objects.FirebaseVisionObject;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraUtils;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Frame;
import com.otaliastudios.cameraview.FrameProcessor;
import com.otaliastudios.cameraview.Size;



import java.io.*;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.*;
import static android.content.ContentValues.TAG;


public class CameraFragment extends Fragment {



    private Button takePictureButton;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean mLocationPermissionGranted;
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    // Create a storage reference from our app
    StorageReference storageRef = storage.getReference();

    public static int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION =1;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    Map<String, Object> litter = new HashMap<>();


    private CameraView cameraView;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();


    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }


    private FirebaseVisionObjectDetectorOptions options;
    private FirebaseVisionObjectDetector objectDetector;
    private SurfaceView transparentView;
    private SurfaceHolder holderTransparent;
    private Canvas canvas;
    private Paint paint;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_camera, null);

        options =
                new FirebaseVisionObjectDetectorOptions.Builder()
                        .setDetectorMode(FirebaseVisionObjectDetectorOptions.STREAM_MODE)
                        //.enableClassification()  // Optional
                        .build();

        objectDetector =
                FirebaseVision.getInstance().getOnDeviceObjectDetector(options);

        transparentView = view.findViewById(R.id.TransparentView);
        transparentView.setZOrderOnTop(true);
        holderTransparent = transparentView.getHolder();
        holderTransparent.setFormat(PixelFormat.TRANSPARENT);

        cameraView = view.findViewById(R.id.cameraView);
        cameraView.setLifecycleOwner(this);
        cameraView.addFrameProcessor(new FrameProcessor() {
             @Override
             @WorkerThread
             public void process(Frame frame) {
                 byte[] data = frame.getData();
                 int rotation = frame.getRotation();
                 long time = frame.getTime();
                 Size size = frame.getSize();
                 int format = frame.getFormat();
                 if (data == null) {
                     return;
                 }
                 ByteBuffer buffer = ByteBuffer.wrap(data);
                 rotation = degreesToFirebaseRotation(closestDegree(rotation));
                 FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                         .setWidth(size.getWidth())   // 480x360 is typically sufficient for
                         .setHeight(size.getHeight())  // image recognition
                         .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                         .setRotation(rotation)
                         .build();
                 FirebaseVisionImage image = FirebaseVisionImage.fromByteBuffer(buffer, metadata);

                 objectDetector.processImage(image)
                         .addOnSuccessListener(
                                 new OnSuccessListener<List<FirebaseVisionObject>>() {
                                     @Override
                                     public void onSuccess(List<FirebaseVisionObject> detectedObjects) {
                                          if (!detectedObjects.isEmpty()) {
                                                for (FirebaseVisionObject obj: detectedObjects)
                                                    DrawFocusRect(obj.getBoundingBox().left,obj.getBoundingBox().top, obj.getBoundingBox().right, obj.getBoundingBox().bottom, 0xFE0000FE);
                                              return;
                                          }
                                          clearCanvas();
                                          return;
                                     }
                                 })
                         .addOnFailureListener(
                                 new OnFailureListener() {
                                     @Override
                                     public void onFailure(@NonNull Exception e) {
                                         clearCanvas();
                                         return;
                                     }
                                 });

             }
         });

        takePictureButton = view.findViewById(R.id.btn_takepicture);
        assert takePictureButton != null;
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
        return view;
    }

    private void takePicture(){
        ///////////////ADD TO DATABASE/////////////////
        // Create a new user with a first and last name


        litter.put("picker", "null");
        litter.put("recorder", mAuth.getCurrentUser().getUid());

        getLocationPermission();

        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        //TODO: UI updates.
                    }
                }
            }
        };


        LocationServices.getFusedLocationProviderClient(getActivity()).requestLocationUpdates(mLocationRequest, mLocationCallback, null);



        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            litter.put("geolocation",   new GeoPoint(location.getLatitude(), location.getLongitude()));
                        } else{
                            litter.put("geolocation",   new GeoPoint(0f, 0f));
                        }
                    }
                });


        cameraView.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(final byte[] picture) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final String path = GetFileName();
                        final StorageReference photoRef = storageRef.child(path);

                        UploadTask uploadTask = photoRef.putBytes(picture);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                                // ...
                                litter.put("litterPicture", path);
                            }
                        });
                    }
                }).start();

            }
        });

        cameraView.capturePicture();



        // Add a new document with a generated ID
        db.collection("litter")
                .add(litter)
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
    }


    private String GetFileName(){
        String fileName = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
        fileName = "litter/" + fileName + ".jpg";
        return fileName;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private int closestDegree(int deg) {
        int min = Integer.MAX_VALUE;
        int mindeg = 0;
        for (int i = 0; i < 4; i++) {
            if (Math.abs(deg - (i*90)) < min) {
                min = Math.abs(deg - (i*90));
                mindeg = i;
            }
        }

        return mindeg * 90;
    }

    private int degreesToFirebaseRotation(int degrees) {
        switch (degrees) {
            case 0:
                return FirebaseVisionImageMetadata.ROTATION_0;
            case 90:
                return FirebaseVisionImageMetadata.ROTATION_90;
            case 180:
                return FirebaseVisionImageMetadata.ROTATION_180;
            case 270:
                return FirebaseVisionImageMetadata.ROTATION_270;
            default:
                throw new IllegalArgumentException(
                        "Rotation must be 0, 90, 180, or 270.");
        }
    }

    private void clearCanvas() {
        if (holderTransparent != null && transparentView != null) {
            canvas = holderTransparent.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                holderTransparent.unlockCanvasAndPost(canvas);
            }

        }

    }

    private void DrawFocusRect(final float RectLeft, final float RectTop, final float RectRight, final float RectBottom, final int color)
    {
        if (holderTransparent != null && transparentView != null) {
            canvas = holderTransparent.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                //border's properties
                paint = new Paint();
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(color);
                paint.setStrokeWidth(3);
                canvas.drawRect(RectLeft, RectTop, RectRight, RectBottom, paint);

                holderTransparent.unlockCanvasAndPost(canvas);
            }

        }

    }

    private String getCategory(int i) {
        switch (i) {
            case 0: return "Unknown";
            case 1: return "Home Good";
            case 2: return "Fashion Good";
            case 3: return "Place";
            case 4: return "Plant";
            case 5: return "food";
            default: return "Unknown";
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraView.destroy();
    }

}
