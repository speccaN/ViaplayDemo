package com.viaplay.ericlindeberg.viaplaydemo;

import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import java.util.HashMap;

public class TaskFragment extends Fragment{

    ViaplayFetcher viaplayFetcher;
    private boolean isConnected;
    ProgressDialog progressDialog;

    interface TaskCallbacks{
        void onPreExecute();
        void onCancelled();
        void onPostExecute(HashMap<String, String> s);
    }

    private TaskCallbacks mCallbacks;
    private FetchItemsTask mTask;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (TaskCallbacks) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viaplayFetcher = new ViaplayFetcher();

        CheckInternetConnection();

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        // Create and execute the background task.
        mTask = new FetchItemsTask();
        mTask.execute();
    }

    /**
     * Set the callback to null so we don't accidentally leak the
     * Activity instance.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;

    }

    @Override
    public void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("Laddar");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, HashMap<String, String>>{

        @Override
        protected void onPreExecute() {
            if (mCallbacks != null) {
                mCallbacks.onPreExecute();
            }

            showProgressDialog();
        }

        @Override
        protected void onCancelled(HashMap<String, String> hashMap) {
            if (mCallbacks != null) {
                mCallbacks.onCancelled();
            }
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
        }

        @Override
        protected HashMap<String, String> doInBackground(Void... params){
            return viaplayFetcher.fetchData("https://content.viaplay.se/androidv2-se", isConnected,
                    getContext());
        }

        @Override
        protected void onPostExecute(HashMap<String, String> returnedTitles) {
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();

            if (mCallbacks != null) {
                mCallbacks.onPostExecute(returnedTitles);
            }
        }
    }

    private void CheckInternetConnection(){
        ConnectivityManager cm =
                (ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
