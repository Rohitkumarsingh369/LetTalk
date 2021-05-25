package com.example.upproject;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions;
import com.google.mlkit.nl.languageid.LanguageIdentifier;

import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;

public class LanguageRecognitionFragment extends Fragment {
    String FileName="myfile";
    TextView greetingmessage,displaynametext,displaylangtitle;
        EditText texttodetectformat;
        Button detectbtn,refreshbtn;
        LinearLayout layout;
        String langcode="";
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
        texttodetectformat=getActivity().findViewById(R.id.texttodetect);
        layout=getActivity().findViewById(R.id.layoutview);

        displaylangtitle=getActivity().findViewById(R.id.displaytitle);

        refreshbtn= getActivity().findViewById(R.id.refresh);
        refreshbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displaylangtitle.setVisibility(View.GONE);
                layout.removeAllViews();
                texttodetectformat.setText("");
            }
        });

        detectbtn= getActivity().findViewById(R.id.detect);
        detectbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text=texttodetectformat.getText().toString();
                text=text.trim();
                if(text.length()!=0)
                {
                    displaylangtitle.setVisibility(View.VISIBLE);
                    addlayout();
                }
                else{
                    displayalert();
                }
            }
        });

    }
    public void displayalert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage("Enter your Text")
                .setCancelable(false)

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

   private void addlayout() {
        layout.removeAllViews();
        String text=texttodetectformat.getText().toString();
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
                View langtodisplay=LayoutInflater.from(getContext()).inflate(R.layout.detectlanguagecard, null);
                TextView worddisplay=langtodisplay.findViewById(R.id.wordtodisplay);
                TextView langname=langtodisplay.findViewById(R.id.langdetectdisplay);
                LanguageIdentifier languageIdentifier = LanguageIdentification.getClient(
                        new LanguageIdentificationOptions.Builder()
                                .setConfidenceThreshold(0.34f)
                                .build());

                String finalWord = word;
                String undefined="undefined";
                languageIdentifier.identifyLanguage(finalWord).addOnSuccessListener(
                                new OnSuccessListener<String>() {
                                    @Override
                                    public void onSuccess(@Nullable String languageCode) {
                                        if (languageCode.equals("und")) {
                                            worddisplay.setText(finalWord);
                                            langname.setText(undefined);

                                        } else {
                                            worddisplay.setText(finalWord);
                                            if(MapActivity.language.containsKey(languageCode))
                                                langname.setText(MapActivity.language.get(languageCode));
                                            else
                                                langname.setText(undefined);
                                            //Toast.makeText(getActivity(),"language code "+languageCode,Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Model couldn’t be loaded or other internal error.
                                        // ...
                                    }
                                });

                layout.addView(langtodisplay);
                word="";

            }
        }
    }
    private void detectlang(String text) {

        LanguageIdentifier languageIdentifier = LanguageIdentification.getClient(
                new LanguageIdentificationOptions.Builder()
                        .setConfidenceThreshold(0.34f)
                        .build());

        languageIdentifier.identifyLanguage(text)
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@Nullable String languageCode) {
                                if (languageCode.equals("und")) {
                                    langcode=languageCode;
                                    // Toast.makeText(getActivity(),"YUndefined",Toast.LENGTH_SHORT).show();
                                } else {
                                    langcode=languageCode;
                                    //Toast.makeText(getActivity(),"language code "+languageCode,Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Model couldn’t be loaded or other internal error.
                                // ...
                            }
                        });
    }
    private ActionBar getActionBar() {
        return ((MainActivity) getActivity()).getSupportActionBar();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState) {
        // ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        View view=inflater.inflate(R.layout.language_recognition_fragment, container, false);

        return view;
    }
}
