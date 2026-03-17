package vn.edu.tdtu.lhqc.budtrack.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import vn.edu.tdtu.lhqc.budtrack.controllers.settings.SettingsHandler;

/**
 * IntentService for fetching and updating exchange rates from external API.
 * This service runs in a background thread and automatically stops when work is complete.
 */
public class ExchangeRateUpdateService extends IntentService {

    private static final String TAG = "ExchangeRateUpdateService";
    private static final String ACTION_UPDATE_EXCHANGE_RATE = "vn.edu.tdtu.lhqc.budtrack.action.UPDATE_EXCHANGE_RATE";
    
    // Using exchangerate-api.com free tier (no API key needed for basic usage)
    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest/USD";

    public ExchangeRateUpdateService() {
        super("ExchangeRateUpdateService");
    }

    /**
     * Create an Intent to start this service for updating exchange rate
     */
    public static Intent createUpdateIntent(android.content.Context context) {
        Intent intent = new Intent(context, ExchangeRateUpdateService.class);
        intent.setAction(ACTION_UPDATE_EXCHANGE_RATE);
        return intent;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_EXCHANGE_RATE.equals(action)) {
                handleUpdateExchangeRate();
            }
        }
    }

    /**
     * Handle the exchange rate update action
     */
    private void handleUpdateExchangeRate() {
        Log.d(TAG, "Starting exchange rate update service");
        
        try {
            float rate = fetchExchangeRate();
            if (rate > 0) {
                SettingsHandler.setExchangeRate(this, rate);
                Log.d(TAG, "Exchange rate updated successfully: " + rate);
                
                // Broadcast success result
                Intent resultIntent = new Intent("vn.edu.tdtu.lhqc.budtrack.EXCHANGE_RATE_UPDATED");
                resultIntent.putExtra("success", true);
                resultIntent.putExtra("rate", rate);
                sendBroadcast(resultIntent);
            } else {
                Log.e(TAG, "Failed to fetch exchange rate");
                
                // Broadcast failure result
                Intent resultIntent = new Intent("vn.edu.tdtu.lhqc.budtrack.EXCHANGE_RATE_UPDATED");
                resultIntent.putExtra("success", false);
                sendBroadcast(resultIntent);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in exchange rate update service", e);
            
            // Broadcast error result
            Intent resultIntent = new Intent("vn.edu.tdtu.lhqc.budtrack.EXCHANGE_RATE_UPDATED");
            resultIntent.putExtra("success", false);
            sendBroadcast(resultIntent);
        }
    }

    /**
     * Fetch USD to VND exchange rate from external API
     * @return Exchange rate (USD to VND) or -1 if failed
     */
    private float fetchExchangeRate() {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000); // 10 seconds
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                );
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse JSON response
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONObject rates = jsonResponse.getJSONObject("rates");
                
                // Get VND rate (1 USD = X VND)
                if (rates.has("VND")) {
                    float rate = (float) rates.getDouble("VND");
                    Log.d(TAG, "Fetched exchange rate: 1 USD = " + rate + " VND");
                    return rate;
                } else {
                    Log.e(TAG, "VND rate not found in API response");
                    return -1;
                }
            } else {
                Log.e(TAG, "API request failed with code: " + responseCode);
                return -1;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching exchange rate", e);
            return -1;
        }
    }
}

