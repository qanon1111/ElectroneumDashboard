package com.example.joes.electroneumwallet;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;


import org.json.JSONException;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;


/**
 * Created by Joes on 31/1/2018.
 */

class NetworkUtils {
    // URL Needed for Data Gathering
    private static String STATS_MINING_ADDRESS;
    private static String ELECTRONEUM_PRICE_TICKER;

    private static String TEMPORARY_URL = "";
    private static final String DONE_URL = "DONE";
    private static final String ERROR_URL = "{\"error\":\"not found\"}";
    private static boolean ERROR_TICKER;
    private static int NetworkCode;


    static void GetNetworkData() throws MalformedURLException {

        Log.i("Current Info", "Address: " + STATS_MINING_ADDRESS);
        URL STATS_MINING_ADDRESS_URL = new URL(STATS_MINING_ADDRESS);
        URL ETN_PRICE_TICKER_URL = new URL(ELECTRONEUM_PRICE_TICKER);

        new MiningAsyncTask().execute(STATS_MINING_ADDRESS_URL);
        new MiningAsyncTask().execute(ETN_PRICE_TICKER_URL);
    }

    static void URLCreator(String ELECTRONEUM_WALLET_ADDRESS) {
        String BASE_ELECTRONEUM  = "https://api.etn.spacepools.org/v1";
        ERROR_TICKER = false;
        NetworkCode = 0;
        STATS_MINING_ADDRESS = BASE_ELECTRONEUM + "/stats/address/" + ELECTRONEUM_WALLET_ADDRESS;
        ELECTRONEUM_PRICE_TICKER = "https://api.coinmarketcap.com/v1/ticker/electroneum/";
    }




     static class MiningAsyncTask extends AsyncTask<URL, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            TEMPORARY_URL = "";

            MainActivity.CleanUIData();
            Log.i("WebResult", "ON PRE");
            MainActivity.NoInternetConnectionUI();
            MainActivity.Network_Relative_Layout.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(URL... urls) {
            URL searchUrl = urls[0];
            String WebResult = "";
            Log.i("WebResult", "DO IN");
            try {

                WebResult =  getResponseFromHttpUrl(searchUrl);

                Log.i("WebResult", WebResult);



                if (WebResult.equals(ERROR_URL)) {
                    TEMPORARY_URL = ERROR_URL;
                }
                else {
                    TEMPORARY_URL = searchUrl.toString();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return WebResult;
        }

        @Override
        protected void onPostExecute(String s) {
            if ( s.equals("<html>")) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                MainActivity.No_Internet_Relative_Layout.setVisibility(View.GONE);
                try {
                    NetworkUtils.GetNetworkData();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
            Log.i("HTTP", String.valueOf(NetworkCode));
            Log.i("Errror Ticker", TEMPORARY_URL);
            if (TEMPORARY_URL.equals(ERROR_URL)) {
                MainActivity.ProvideError();
                ERROR_TICKER = true;
            }

            else if (TEMPORARY_URL.equals(STATS_MINING_ADDRESS)) {
                JSONUtil.GetMiningData(s);

            }
            else if (TEMPORARY_URL.equals(ELECTRONEUM_PRICE_TICKER)) {
                try {
                    JSONUtil.GetCurrencyData(s);
                    TEMPORARY_URL = DONE_URL;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Log.i("Errror Ticker", String.valueOf(ERROR_TICKER));
            if (TEMPORARY_URL.equals(DONE_URL) && !ERROR_TICKER) {
                MainActivity.Network_Relative_Layout.setVisibility(View.GONE);
                MainActivity.UpdateUIWithData();

            }

        }
    }
    public static String getResponseFromHttpUrl(URL url) throws IOException {

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");



            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } catch (FileNotFoundException e) {
            NetworkCode = HttpURLConnection.HTTP_UNAVAILABLE;
            InputStream er = urlConnection.getErrorStream();
            Scanner erscan = new Scanner(er);
            return  erscan.next();
        }
        finally {
            urlConnection.disconnect();
        }

    }

}
