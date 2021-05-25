package com.example.upproject;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.SparseArray;
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
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.Calendar;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class ImagetosignFragment extends Fragment {

    String FileName="myfile";
    TextView greetingmessage,displaynametext,displayword,displayletter;
    ImageButton gallerybtn,camerabtn,handbtn;
    EditText textdisplayshow;
    ImageView previewimage,displayimage;
    Button detectbtn,refreshbtn;
    LinearLayout layout;
    CardView displaycard;
    ArrayList<String> wordlist=new ArrayList<>();
        // camera detail
    private static final int CAMERA_REQUEST_CODE=200;
    private static final int STORAGE_REQUEST_CODE=400;
    private static final int IMAGE_PICK_GALLERY_CODE=1000;
    private static final int IMAGE_PICK_CAMERA_CODE=1001;

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

        //slider info start
        displaycard=getActivity().findViewById(R.id.display_card);
        displayword=getActivity().findViewById(R.id.display_word);
        displayimage=getActivity().findViewById(R.id.display_image);
        displayletter=getActivity().findViewById(R.id.display_letter);
        //slider info end
        // display text
        textdisplayshow=getActivity().findViewById(R.id.imagetotexttodetect);
        textdisplayshow.setFocusable(false);
        //display image
        previewimage=getActivity().findViewById(R.id.imagepreview);

        //button
        detectbtn=getActivity().findViewById(R.id.detect);
        refreshbtn=getActivity().findViewById(R.id.refresh);

        //camera permission
        cameraPermission=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //storage pemission
        storagePermission=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //pick image
        gallerybtn=getActivity().findViewById(R.id.gallery);
        camerabtn=getActivity().findViewById(R.id.camera);
        handbtn=getActivity().findViewById(R.id.speak);

        //layout to display the sign
        layout=getActivity().findViewById(R.id.layoutview);
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
                    displaycard.setVisibility(View.GONE);
                    layout.removeAllViews();
                    textdisplayshow.setFocusableInTouchMode(true);
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
                    displaycard.setVisibility(View.GONE);
                    layout.removeAllViews();
                    textdisplayshow.setFocusableInTouchMode(true);
                    pickGallery();
                }
            }
        });

        refreshbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displaycard.setVisibility(View.GONE);
                displayword.setText("");
                previewimage.setImageResource(R.drawable.imagepreview);
                layout.removeAllViews();
                textdisplayshow.setText("");
                textdisplayshow.setFocusable(false);
            }
        });
        handbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container, new FingerprintFragment(), "NewFragmentTag");
                ft.addToBackStack(null);
                ft.commit();
            }
        });
        detectbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text=textdisplayshow.getText().toString();
                if(text.length()!=0){

                    showImageImportDialog();
                }
                else{
                    displayalert();
                }

            }
        });
    }
    private void showImageImportDialog() {
        String[] items={"Word wise Slider","All in One"};
        AlertDialog.Builder dialog=new AlertDialog.Builder(getActivity());
        dialog.setTitle("Select your options");
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0){
                    displaycard.setVisibility(View.GONE );
                    displayword.setText("");
                    displayimage.setImageResource(0);
                    displayletter.setText("");
                    layout.removeAllViews();
                    addimage();
                }
                if(which==1){
                    layout.removeAllViews();
                    displaycard.setVisibility(View.VISIBLE);
                    displayword.setText("");
                    displayimage.setImageResource(0);
                    displayletter.setText("");
                    addimage1();
                    addimage2();
                }
            }
        });
        dialog.create().show();//show dialog
    }
    private void addimage() {
        layout.removeAllViews();
        String text=textdisplayshow.getText().toString();
        text=text.trim().replaceAll("\n", " ");
        text=text.replaceAll("\\p{Punct}","");

        text=text+" ";

        String word="";

        for(int i=0;i<text.length();i++){
            if(i!=text.length()-1&&text.charAt(i+1)==' '&&text.charAt(i)==' ')
                continue;

            if(i!=text.length()-1&&text.charAt(i)<'a'&&text.charAt(i)>'z')
                continue;

            if(text.charAt(i)!=' ')
                word=word+text.charAt(i);
            else{
                int length=word.length();
                View texttosign = LayoutInflater.from(getContext()).inflate(R.layout.displaysign, null);
                TextView texttodisplay=texttosign.findViewById(R.id.addtextview);
                texttodisplay.setText(word);
                LinearLayout layoutimage=texttosign.findViewById(R.id.addimageview);

                for(int k=0;k<length;k++){
                    final int finali = k;
                    char c=word.charAt(finali);
                    View imagetodisplay=LayoutInflater.from(getContext()).inflate(R.layout.imagewithletter, null);
                    //image
                    ImageView imageView=imagetodisplay.findViewById(R.id.imgtodisplay);
                    if(c>='A'&& c<='Z')
                    {
                        imageView.setImageResource(MapActivity.alphabets[c-'A']);
                    }
                    else if(c>='a'&& c<='z')
                    {
                        imageView.setImageResource(MapActivity.alphabets[c-'a']);
                    }
                    else if(c>='0' && c<='9'){
                        imageView.setImageResource(MapActivity.digit[c-'0']);
                    }
                    LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(200,350);
                    layoutParams.setMargins(10,10,0,10);
                    imageView.setLayoutParams(layoutParams);
                    //text
                    TextView letterdislay=imagetodisplay.findViewById(R.id.lettodisplay);
                    String letter=String.valueOf(c);
                    letterdislay.setText(letter);
                    LinearLayout.LayoutParams layoutParams1=new LinearLayout.LayoutParams(200,100);
                    layoutParams1.setMargins(10,10,0,10);
                    letterdislay.setLayoutParams(layoutParams1);



                    layoutimage.addView(imagetodisplay);

                }
                layout.addView(texttosign);
                word="";
            }
        }
    }
    private void addimage1() {
        layout.removeAllViews();
        String text=textdisplayshow.getText().toString();
        text=text.trim().replaceAll("\n", " ");
        text=text.replaceAll("\\p{Punct}","");

        text=text+" ";

        String word="";

        for(int i=0;i<text.length();i++){
            if(i!=text.length()-1&&text.charAt(i+1)==' '&&text.charAt(i)==' ')
                continue;
            if(text.charAt(i)!=' ')
                word=word+text.charAt(i);
            else{
                int length=word.length();
                wordlist.add(word);
                word="";
            }
        }
    }
    private void addimage2() {
        displayword.setText("");
        String word="";
        String word2="";
        for (int d = 0; d < wordlist.size(); d++) {
            word += wordlist.get(d);
            word2 += wordlist.get(d)+" ";
        }
        word2=word2.trim();

        // displayword.setText(word2);
        final  String text=word;
        final String text2=word2.trim();
        Handler handler1 = new Handler();
        Handler handler2=new Handler();

        displayword.setText(text2);
        for (int i = 0; i < text.length(); i++) {
            final int finalI = i;
            handler1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    char c = text.charAt(finalI);

                    if (c >= 'A' && c <= 'Z') {
                        displayimage.setImageResource(MapActivity.alphabets[c - 'A']);
                    } else if (c >= 'a' && c <= 'z') {
                        displayimage.setImageResource(MapActivity.alphabets[c - 'a']);
                    } else if (c >= '0' && c <= '9') {
                        displayimage.setImageResource(MapActivity.digit[c - '0']);
                    }
                    displayletter.setText(String.valueOf(c));
                }

            }, 1000*finalI);
        }

        wordlist.clear();
    }
    public void displayalert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage("Select image to display text")
                .setCancelable(false)

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
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

                //get darwable bitmap

                BitmapDrawable bitmapDrawable=(BitmapDrawable)previewimage.getDrawable();
                Bitmap bitmap=bitmapDrawable.getBitmap();

                TextRecognizer recognizer=new TextRecognizer.Builder(getActivity().getApplicationContext()).build();

                if(!recognizer.isOperational()){
                    Toast.makeText(getActivity(),"Error",Toast.LENGTH_SHORT).show();
                }
                else{
                    Frame frame=new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items=recognizer.detect(frame);
                    StringBuilder sb=new StringBuilder();

                    //get text from sb until there is no text

                    for(int i=0;i<items.size();i++){
                        TextBlock myItem=items.valueAt(i);
                        sb.append(myItem.getValue());
                        sb.append("\n");
                    }
                    textdisplayshow.setText(sb.toString());
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
        return inflater.inflate(R.layout.image_to_sign_fragment, container, false);
    }
}
