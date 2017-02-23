package com.viaplay.ericlindeberg.viaplaydemo;

import android.support.v4.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TaskFragment.TaskCallbacks {

    TextView title;
    TextView desc;

    private static final String TAG_TASK_FRAGMENT = "task_fragment";

    private  TaskFragment mTaskFragment;

    private HashMap<String, String> hmap = new HashMap<>();
    private List<String> href = new ArrayList<>();
    private boolean isConnected;

    ViaplayFetcher viaplayFetcher;

    NavigationView navigationView;
    DrawerLayout drawer;
    Toolbar toolbar;

    @Override
    public void onPreExecute() {

    }

    @Override
    public void onCancelled() {

    }

    @Override
    public void onPostExecute(HashMap<String,String> s) {
        hmap = s;
        UpdateMenuItems();
    }

    //region AsyncTask Fetchers

    // Körs vid start av applikationen. Finns det internet-uppkoppling så hämtas
    // alla JSON filer. Har dom redan hämtats så läses dom in från den lokala lagringen.
    private class FetchItemsTask extends AsyncTask<Void, Void, HashMap<String, String>>{

        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Laddar");
            progressDialog.show();
        }

        @Override
        protected HashMap<String, String> doInBackground(Void... params){
            return viaplayFetcher.fetchData("https://content.viaplay.se/androidv2-se", isConnected,
                    getApplicationContext());
        }

        @Override
        protected void onPostExecute(HashMap<String, String> returnedTitles) {
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();

            hmap = returnedTitles;
            UpdateMenuItems();
        }
    }

    // Om det finns internet-uppkoppling så hämta data från den href som blivit vald
    // i Navigation Menu. Istället för att hämta alla JSON filer varje gång
    // så läser den bara in den man valt i menyn och sparar den till
    // den lokala lagringen för att ha den senast uppdaterade versionen.
    private class FetchHrefTask extends AsyncTask<String, Void, List<String>>{

        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Laddar");
            //progressDialog.show();
        }

        @Override
        protected List<String> doInBackground(String... strings) {
            return viaplayFetcher.fetchFromHref(strings[0], strings[1],
                    isConnected, getApplicationContext());
        }

        @Override
        protected void onPostExecute(List<String> strings) {
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
            href = strings;
            // Skriver ut titel för att visa användaren vart den befinner sig just nu
            title.setText(href.get(0));
            // Skriver ut beskrivning för användaren
            desc.setText(href.get(1));
        }
    }
    //endregion

    private void CheckInternetConnection(){
        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viaplayFetcher = new ViaplayFetcher();

        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        title = (TextView) findViewById(R.id.textTitle);
        desc = (TextView) findViewById(R.id.textDesc);

        FragmentManager fm = getSupportFragmentManager();
        mTaskFragment = (TaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

        if (mTaskFragment == null){
            mTaskFragment = new TaskFragment();
            fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
        } else {
            fm.findFragmentByTag(TAG_TASK_FRAGMENT);
            fm.beginTransaction().remove(mTaskFragment).commit();
            mTaskFragment = new TaskFragment();
            fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
        }

        CheckInternetConnection();

        //viaplayFetcher = new ViaplayFetcher();
        //new FetchItemsTask().execute();

        if (hmap.size() == 0) {
            desc.setText("Var god starta om applikationen när du har internet-uppkopling");
        }
    }

    // Metod för att dynamiskt uppdatera Navigation Menu med dess undermenyer
    private void UpdateMenuItems(){

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        Menu menu = navigationView.getMenu();

        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // För att dynamiskt lägga till de undermenyer som ligger i Navigation Menu
        for (String item :
                hmap.keySet()) {
            menu.add(1, Menu.NONE, Menu.NONE, item);
        }

        menu.setGroupCheckable(1, true, true);
        menu.performIdentifierAction(0, 0);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        CheckInternetConnection();

        // När ett objekt i Navigation listan blir valt så hämtas dess respektive href data
        new FetchHrefTask().execute(hmap.get(item.toString()), item.toString());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
