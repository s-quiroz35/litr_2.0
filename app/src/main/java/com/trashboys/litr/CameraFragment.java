package com.trashboys.litr;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.*;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.objects.FirebaseVisionObject;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Frame;
import com.otaliastudios.cameraview.FrameProcessor;
import com.otaliastudios.cameraview.Size;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.ContentValues.TAG;


public class CameraFragment extends Fragment {

    private Button takePictureButton;
    private CameraView cameraView;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    FirebaseVisionObjectDetectorOptions options;
    FirebaseVisionObjectDetector objectDetector;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_camera, null);

        options =
                new FirebaseVisionObjectDetectorOptions.Builder()
                        .setDetectorMode(FirebaseVisionObjectDetectorOptions.STREAM_MODE)
                        .enableClassification()  // Optional
                        .build();

        objectDetector =
                FirebaseVision.getInstance().getOnDeviceObjectDetector(options);


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

                                              Toast.makeText(getContext(), getCategory(detectedObjects.get(0).getClassificationCategory()),
                                                      Toast.LENGTH_SHORT).show();
                                              return;
                                          }
                                          return;
                                     }
                                 })
                         .addOnFailureListener(
                                 new OnFailureListener() {
                                     @Override
                                     public void onFailure(@NonNull Exception e) {
                                         // Task failed with an exception
                                         // ...
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
                //takePicture();
            }
        });
        return view;
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

}
