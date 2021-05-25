package com.example.upproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class VoicetosignFragment extends Fragment {
    String FileName="myfile";
    TextView greetingmessage,displaynametext,displayword,displayletter;
    EditText textdisplayshow;
    Button refreshbtn,detectbtn;
    ImageButton speakbtn,handbtn;
    LinearLayout layout;
    CardView displaycard;
    ImageView displayimage;
    ArrayList<String> wordlist=new ArrayList<>();
    private static final int REQUEST_CODE_SPEECH_INPUT = 1;
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

        textdisplayshow=getActivity().findViewById(R.id.voicetotexttodetect);
        textdisplayshow.setFocusable(false);
        layout=getActivity().findViewById(R.id.layoutview);

        speakbtn=getActivity().findViewById(R.id.speak);
        speakbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                textdisplayshow.setFocusableInTouchMode(true);
                Intent intent
                        = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                        Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice to Sign");

                try {
                    startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
                }
                catch (Exception e) {

                }
            }
        });


        handbtn=getActivity().findViewById(R.id.hand2);
        handbtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                final FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container, new FingerprintFragment(), "NewFragmentTag");
                ft.addToBackStack(null);
                ft.commit();
            }
        });
        refreshbtn=getActivity().findViewById(R.id.refresh);
       refreshbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                displaycard.setVisibility(View.GONE);
                displayword.setText("");
                layout.removeAllViews();
                textdisplayshow.setText("");
                textdisplayshow.setHint("Tap mic to speak");
            }
        });

        detectbtn= getActivity().findViewById(R.id.detect);
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
    private void addimage1() {
        layout.removeAllViews();
        String text=textdisplayshow.getText().toString();
        text=text.trim();
        // text=text.toLowerCase();
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

        builder.setMessage("Tap mic to speak")
                .setCancelable(false)

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);
                textdisplayshow.setText(
                        Objects.requireNonNull(result).get(0));
            }
        }
    }
    private void addimage() {
        layout.removeAllViews();
        String text=textdisplayshow.getText().toString();
        text=text.trim();
        text=text.toLowerCase();
        text=text+" ";

        String word="";

        for(int i=0;i<text.length();i++){
            if(i!=text.length()-1&&text.charAt(i+1)==' '&&text.charAt(i)==' ')
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
                    if(c>='a'&& c<='z')
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
    private ActionBar getActionBar() {
        return ((MainActivity) getActivity()).getSupportActionBar();
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState) {
        // ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        View view=inflater.inflate(R.layout.voice_to_sign_fragment, container, false);

        return view;
    }
}
