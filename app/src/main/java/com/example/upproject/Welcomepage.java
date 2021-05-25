package com.example.upproject;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

public class Welcomepage extends AppCompatActivity {
    String FileName="myfile";
    Button getstartedbtn;
    TextInputEditText usernamesave;
    boolean flag=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcomepage);
            getstartedbtn=findViewById(R.id.getstarted);
            usernamesave=findViewById(R.id.username);
       /* SharedPreferences sharedPreferences=getSharedPreferences(FileName,MODE_PRIVATE);
        String defaultvalue="DefaultName";
        String name=sharedPreferences.getString("name",defaultvalue);
        name=name.trim();
        usernamesave.setText(name);*/
        SharedPreferences sp = getSharedPreferences("getstartedbtn",MODE_PRIVATE);

        if(sp.getBoolean("logged",false)){
            goToMainActivity();
        }
        getstartedbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homepage();
                if(flag==true)
                {
                    goToMainActivity();
                    sp.edit().putBoolean("logged",true).apply();
                }
            }
        });

    }
    public void homepage(){
        String name=usernamesave.getText().toString();

        if(name.length()==0){
            displayalert();
        }
        else {
            flag=true;
            SharedPreferences sharedPreferences = getSharedPreferences(FileName, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("name", name);
            editor.commit();
        }
       // Toast.makeText(this,"Data saved Successfully",Toast.LENGTH_SHORT).show();

    }
    public void goToMainActivity(){
            Intent i = new Intent(this,MainActivity.class);
            startActivity(i);
            finish();

    }
    public void displayalert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(Welcomepage.this);

        builder.setMessage("Please enter your name")
                .setCancelable(false)

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}