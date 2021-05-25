package com.example.upproject;

import android.app.FragmentManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.google.android.play.core.review.ReviewManager;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import java.util.Arrays;
public class MainActivity extends AppCompatActivity implements DrawerAdapter.OnItemSelectedListener {

    //botton_navigation
    MeowBottomNavigation bottomNavigation;
    private long pressedTime;
    private TextView mVoiceInputTv;
    private static final int REQ_CODE_SPEECH_INPUT =200 ;
    private ReviewManager reviewManager;
    private static final int POS_DASHBOARD = 0;
    private static final int POS_TEXT_TO_SIGN = 1;
    private static final int POS_IMAGE_TO_SIGN = 2;
    private static final int POS_VOICE_TO_SIGN = 3;
    private static final int POS_GESTURE_TO_SIGN = 4;
    private static final int POS_OBJECT_DETECTION = 5;
    private static final int POS_LANGUAGE_RECOGNITION = 6;
    private static final int POS_SETTINGS = 9;
    private static final int POS_LOGOUT = 10;

    private String[] screenTitles;
    private Drawable[] screenIcons;

    private SlidingRootNav slidingRootNav;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);

        //bottom_navigation
        bottomNavigation=findViewById(R.id.bottom_navigation);
        bottomNavigation.add(new MeowBottomNavigation.Model(1,R.drawable.share_logo));
        bottomNavigation.add(new MeowBottomNavigation.Model(2,R.drawable.home_logo));
        bottomNavigation.add(new MeowBottomNavigation.Model(3,R.drawable.contact_logo));

        bottomNavigation.setOnShowListener(new MeowBottomNavigation.ShowListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onShowItem(MeowBottomNavigation.Model item) {
                //Initialize fragement
                Fragment fragment=null;
                switch(item.getId()){
                    case 1:
                        fragment=null;
                        try {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Let's Talk");
                            String shareMessage= "\nLet me recommend you this application\n\n";
                            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
                            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                            startActivity(Intent.createChooser(shareIntent, "Share via"));
                        } catch(Exception e) {
                            //e.toString();
                        }

                    case 2:
                        fragment=new DashboardFragment();
                        break;
                    case 3:
                        fragment=new DashboardFragment();
                            sendEmail();
                       break;

                    default:
                        throw new IllegalStateException("Unexpected value: " + item.getId());
                }
                FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container,fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        bottomNavigation.show(2,true);
        bottomNavigation.setOnClickMenuListener(new MeowBottomNavigation.ClickListener() {
            @Override
            public void onClickItem(MeowBottomNavigation.Model item) {
                //Toast.makeText(getApplicationContext(),"You Clicked"+item.getId(),Toast.LENGTH_SHORT).show();;
            }
        });

        bottomNavigation.setOnReselectListener(new MeowBottomNavigation.ReselectListener() {
            @Override
            public void onReselectItem(MeowBottomNavigation.Model item) {
               // Toast.makeText(getApplicationContext(),"You Reselected "+item.getId(),Toast.LENGTH_SHORT).show();;
            }
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        slidingRootNav = new SlidingRootNavBuilder(this)
                .withToolbarMenuToggle(toolbar)
                .withMenuOpened(false)
                .withContentClickableWhenMenuOpened(false)
                .withSavedState(savedInstanceState)
                .withMenuLayout(R.layout.menu_left_drawer)
                .inject();

        screenIcons = loadScreenIcons();
        screenTitles = loadScreenTitles();

        DrawerAdapter adapter = new DrawerAdapter(Arrays.asList(
                createItemFor(POS_DASHBOARD).setChecked(true),
                createItemFor(POS_TEXT_TO_SIGN),
                createItemFor(POS_IMAGE_TO_SIGN),
                createItemFor(POS_VOICE_TO_SIGN),
                createItemFor(POS_GESTURE_TO_SIGN),
                createItemFor(POS_OBJECT_DETECTION),
                createItemFor(POS_LANGUAGE_RECOGNITION),
                new SpaceItem(48),
                new SpaceItem(48),
                createItemFor(POS_SETTINGS),
                createItemFor(POS_LOGOUT)));
        adapter.setListener(this);

        RecyclerView list = findViewById(R.id.list);
        list.setNestedScrollingEnabled(false);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        adapter.setSelected(POS_DASHBOARD);
    }



    @SuppressWarnings("rawtypes")
    private DrawerItem createItemFor(int position) {
        return new SimpleItem(screenIcons[position], screenTitles[position])
                .withIconTint(color(R.color.black))
                .withTextTint(color(R.color.black))
                .withSelectedIconTint(color(R.color.main))
                .withSelectedTextTint(color(R.color.main));
    }

    private String[] loadScreenTitles() {
        return getResources().getStringArray(R.array.id_activityScreenTitles);
    }

    private Drawable[] loadScreenIcons() {
        TypedArray ta = getResources().obtainTypedArray(R.array.id_activityScreenIcons);
        Drawable[] icons = new Drawable[ta.length()];
        for (int i = 0; i < ta.length(); i++) {

            int id = ta.getResourceId(i, 0);
            if (id != 0) {
                icons[i] = ContextCompat.getDrawable(this, id);
            }
        }
        ta.recycle();
        return icons;
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm=getFragmentManager();
        if(fm.getBackStackEntryCount()>0){
            fm.popBackStack();
        }else{
            super.onBackPressed();
        }
      /*  if (pressedTime + 3000 > System.currentTimeMillis()) {
            super.onBackPressed();
            finish();
        } else {
            Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        pressedTime = System.currentTimeMillis();*/
    }

    @ColorInt
    private int color(@ColorRes int res) {
        return ContextCompat.getColor(this, res);
    }

    @Override
    public void onItemSelected(int position) {
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();

        if(position==POS_DASHBOARD){
            DashboardFragment dashboardFragment=new DashboardFragment();
            transaction.replace(R.id.container,dashboardFragment);
        }
        else if(position==POS_TEXT_TO_SIGN){
            bottomNavigation.show(2,false);
            TexttosignFragment texttosignFragment=new TexttosignFragment();
            transaction.replace(R.id.container,texttosignFragment);

        }
        else if(position==POS_IMAGE_TO_SIGN){

            ImagetosignFragment imagetosignFragment=new ImagetosignFragment();
            transaction.replace(R.id.container,imagetosignFragment);

        }
        else if(position==POS_VOICE_TO_SIGN){
            VoicetosignFragment voicetosignFragment=new VoicetosignFragment();
            transaction.replace(R.id.container,voicetosignFragment);
        }
        else if(position==POS_GESTURE_TO_SIGN){
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("mquinn.sign_language");
            if (launchIntent != null) {
                startActivity(launchIntent);
            } else {
                Toast.makeText(MainActivity.this, "There is no package available in android", Toast.LENGTH_LONG).show();
            }
        }
        else if(position==POS_OBJECT_DETECTION){
            ObjectDetectionFragment objectDetectionFragment=new ObjectDetectionFragment();
            transaction.replace(R.id.container,objectDetectionFragment);
        }
        else if(position==POS_LANGUAGE_RECOGNITION){
            LanguageRecognitionFragment languageRecognitionFragment=new LanguageRecognitionFragment();
            transaction.replace(R.id.container,languageRecognitionFragment);
        }
        else if(position==POS_SETTINGS){
           Editprofile editprofile=new Editprofile();
            transaction.replace(R.id.container,editprofile);
        }
        else if (position == POS_LOGOUT) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Let's Talk");
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setMessage("Are you sure you want to exit?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
        slidingRootNav.closeMenu();
        transaction.addToBackStack(null);
        transaction.commit();
    }
    private void sendEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO)
                .setData(new Uri.Builder().scheme("mailto").build())
                .putExtra(Intent.EXTRA_EMAIL, new String[]{ "Rohit Kumar Singh <rohitkumarsingh0206@gmail.com>" })
                .putExtra(Intent.EXTRA_SUBJECT, "Query about Let's Talk app")
                .putExtra(Intent.EXTRA_TEXT, "Hello,\n")
                ;

        ComponentName emailApp = intent.resolveActivity(getPackageManager());
        ComponentName unsupportedAction = ComponentName.unflattenFromString("com.android.fallback/.Fallback");
        if (emailApp != null && !emailApp.equals(unsupportedAction))
            try {
                // Needed to customise the chooser dialog title since it might default to "Share with"
                // Note that the chooser will still be skipped if only one app is matched
                Intent chooser = Intent.createChooser(intent, "Send email with");
                startActivity(chooser);
                return;
            }
            catch (ActivityNotFoundException ignored) {
            }
        Toast.makeText(this, "Couldn't find an email app and account", Toast.LENGTH_LONG)
                .show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        DialogFragment dialogFragment = null;
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.aboutus) {
            dialogFragment=new DialogFragment();
            // return true;
        }
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container,dialogFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        return super.onOptionsItemSelected(item);
    }
}
