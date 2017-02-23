package com.viaplay.ericlindeberg.viaplaydemo;

import android.app.ProgressDialog;
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
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    TextView title;
    TextView desc;

    private HashMap<String, String> test = new HashMap<>();
    private List<String> href = new ArrayList<>();

    ViaplayFetcher viaplayFetcher;

    NavigationView navigationView;
    DrawerLayout drawer;
    Toolbar toolbar;

    private class FetchItemsTask extends AsyncTask<Void, Void, HashMap<String, String>>{

        @Override
        protected HashMap<String, String> doInBackground(Void... params){

            return viaplayFetcher.fetchData("https://content.viaplay.se/androidv2-se");

        }

        @Override
        protected void onPostExecute(HashMap<String, String> returnedTitles) {
            test = returnedTitles;
            UpdateMenuItems();
        }
    }

    private class FetchHrefTask extends AsyncTask<String, Void, List<String>>{

        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Laddar");
            progressDialog.show();
        }

        @Override
        protected List<String> doInBackground(String... strings) {
            return viaplayFetcher.fetchFromHref(strings[0]);
        }

        @Override
        protected void onPostExecute(List<String> strings) {
            href = strings;
            title.setText(href.get(0));
            desc.setText(href.get(1));
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viaplayFetcher = new ViaplayFetcher();
        new FetchItemsTask().execute();

        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        title = (TextView) findViewById(R.id.textTitle);
        desc = (TextView) findViewById(R.id.textDesc);

    }

    private void UpdateMenuItems(){

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        Menu menu = navigationView.getMenu();

        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        for (String item :
                test.keySet()) {
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

        // När ett objekt i Navigation listan blir valt så hämtas dess respektive href data
        new FetchHrefTask().execute(test.get(item.toString()));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
