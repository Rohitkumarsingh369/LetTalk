package com.example.upproject;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class ObjectDetectionFragment extends Fragment {
    EditText mResultEt;
    ImageView mPreviewIv;
    String FileName="myfile";
    TextView greetingmessage,displaynametext;
    ImageButton gallerybtn,camerabtn;
    TextView textdisplayshow;
    ImageView previewimage;
    Button detectbtn,refreshbtn,display_title;
    LinearLayout layout;

    CardView display_detail;
        // camera detail
    private static final int CAMERA_REQUEST_CODE=200;
    private static final int STORAGE_REQUEST_CODE=400;
    private static final int IMAGE_PICK_GALLERY_CODE=1000;
    private static final int IMAGE_PICK_CAMERA_CODE=1001;
    HashMap<String, Float> objectdetail=new HashMap<String, Float>();

    String cameraPermission[];
    String storagePermission[];
    Uri image_uri;
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActionBar().setTitle("");
        //display the greeting message to the user
        greetingmessage= getActivity().findViewById(R.id.greetingmessage1);
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        String message="";
        if(timeOfDay >= 0 && timeOfDay < 12){
            message="Good morning";
        }else if(timeOfDay >= 12 && timeOfDay < 16){
            message="Good afternoon";
        }else if(timeOfDay >= 16 && timeOfDay < 24){
            message="Good evening";
        }
        message="Hello , "+message;
        greetingmessage.setText(message);

        //display the name from d=sharedprefernces
        displaynametext=getActivity().findViewById(R.id.displayname);
        SharedPreferences sharedPreferences=getContext().getSharedPreferences(FileName,MODE_PRIVATE);
        String defaultvalue="";
        String name=sharedPreferences.getString("name",defaultvalue);
        name=name.trim()+" ";
        int length=name.length();
        String nametodisplay="";
        char ch;
        for(int i=0;i<length;i++){
            ch=name.charAt(i);
            if(ch!=' '){
                if(i==0)
                    ch=Character.toUpperCase(ch);
                nametodisplay+=ch;
            }
            else{
                break;
            }
        }

        displaynametext.setText(nametodisplay);

        // display text
        textdisplayshow=getActivity().findViewById(R.id.imagetotexttodetect);
        //display image
        previewimage=getActivity().findViewById(R.id.imagepreview);

        //button
        detectbtn=getActivity().findViewById(R.id.detect);
        refreshbtn=getActivity().findViewById(R.id.refresh);
        display_title=getActivity().findViewById(R.id.displaytitle);
        //camera permission
        cameraPermission=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //storage pemission
        storagePermission=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //pick image
        gallerybtn=getActivity().findViewById(R.id.camera);
        camerabtn=getActivity().findViewById(R.id.speak);

        //live

        //layout to display the sign
        layout=getActivity().findViewById(R.id.displayobjectdetail);
        //open camera
        camerabtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //camera option clicked
                if(!checkCameraPermission()){
                    //camera permission not allowed, request it
                    requestCameraPermission();
                }
                else{
                    //permission allowed, take picture
                    display_title.setVisibility(View.GONE);
                    display_detail.setVisibility(View.GONE);
                    pickCamera();
                }
            }
        });

        //open gallery
        gallerybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //gallery option clicked
                if(!checkStoragePermission()){
                    //camera permission not allowed, request it
                    requestStoragePermission();
                }
                else{
                    //permission allowed, take picture
                    display_title.setVisibility(View.GONE);
                    display_detail.setVisibility(View.GONE);
                    pickGallery();
                }
            }
        });

        refreshbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previewimage.setImageResource(R.drawable.imagepreview);
                layout.removeAllViews();
                display_title.setVisibility(View.GONE);
                display_detail.setVisibility(View.GONE);
                //textdisplayshow.setText("");
            }
        });
        display_detail=getActivity().findViewById(R.id.displaydetail);
        detectbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //addimage();
                if(objectdetail.size()!=0){
                    display_detail.setVisibility(View.VISIBLE);
                    display_title.setVisibility(View.VISIBLE);
                    layout.removeAllViews();
                    detect();
                }
                else
                    displayalert();


            }
        });


    }
    public void displayalert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage("Select image to detect object")
                .setCancelable(false)

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void detect() {
        layout.removeAllViews();
        if(objectdetail.size()!=0){
            for(Map.Entry<String,Float> entry:objectdetail.entrySet()){
                View objectdetialtoshow = LayoutInflater.from(getContext()).inflate(R.layout.objectdetectiondetaildisplay, null);
                TextView objectname=objectdetialtoshow.findViewById(R.id.objectname);
                TextView confidence=objectdetialtoshow.findViewById(R.id.confidence);
                View border=objectdetialtoshow.findViewById(R.id.border);
                objectname.setText(entry.getKey());
                Log.d("myTag", "This is my message");
                Float confidence_of_object=objectdetail.get(entry.getKey());
                confidence.setText(confidence_of_object.toString());
                //objectdetail.remove(entry.getKey());
              //  confidence.setText(entry.getValue());
                layout.addView(objectdetialtoshow);
            }
                objectdetail.clear();
        }
        else{
            Toast.makeText(getActivity(),"You Reselected ",Toast.LENGTH_SHORT).show();
        }
    }

    //camera detail
    private boolean checkCameraPermission() {
        boolean result= ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1=ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(getActivity(),cameraPermission,CAMERA_REQUEST_CODE);
    }
    private void pickCamera() {
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"NewPic"); //title of the new image
        values.put(MediaStore.Images.Media.DESCRIPTION,"Images To text"); //description
        image_uri=getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

       // Toast.makeText(this, "image", Toast.LENGTH_SHORT).show();
        Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);

    }

    //gallery detail
    private boolean checkStoragePermission() {
        boolean result=ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);

        return result;
    }
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(getActivity(),storagePermission,STORAGE_REQUEST_CODE);
    }
    private void pickGallery() {

        //INTENT to pick image from the gallery
        Intent intent=new Intent(Intent.ACTION_PICK);
        //set intent type to image
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }
    //handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:
                if(grantResults.length>0){
                    boolean  cameraAccepted=grantResults[0]==
                            PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted=grantResults[0]==
                            PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && writeStorageAccepted){
                        pickCamera();
                    }
                    else
                    {
                        Toast.makeText(getActivity(),"permission denied",Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case STORAGE_REQUEST_CODE :
                if(grantResults.length>0){

                    boolean writeStorageAccepted=grantResults[0]==
                            PackageManager.PERMISSION_GRANTED;
                    if(writeStorageAccepted){
                        pickGallery();
                    }
                    else
                    {
                        Toast.makeText(getActivity(),"permission denied",Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

   // @SuppressLint("MissingSuperCall")
   @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMultiTouchEnabled(true)
                        .start(getContext(),this);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                CropImage.activity(image_uri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMultiTouchEnabled(true)
                        .start(getContext(),this);

            }
        }
        //get crop images
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK){
                Uri resultUri=result.getUri();
                previewimage.setImageURI(resultUri);

                FirebaseVisionImage image;
                try {
                    image = FirebaseVisionImage.fromFilePath(getContext(), result.getUri());
                    FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance()
                            .getOnDeviceImageLabeler();
                    labeler.processImage(image)
                            .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                                @Override
                                public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                                    // Task completed successfully
                                    // ...
                                    //textdisplayshow.setText("");
                                    for (FirebaseVisionImageLabel label: labels) {
                                        String text = label.getText();
                                        String entityId = label.getEntityId();
                                        float confidence = label.getConfidence();
                                       // textdisplayshow.append(text+"\t"+confidence+"\n");
                                        objectdetail.put(text,confidence);
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Task failed with an exception
                                    // ...
                                }
                            });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(resultCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception error=result.getError();
                Toast.makeText(getActivity(), "nonono", Toast.LENGTH_SHORT).show();
            }
        }

    }


    //action
    private ActionBar getActionBar() {
        return ((MainActivity) getActivity()).getSupportActionBar();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.object_detection_fragment, container, false);
    }
}
