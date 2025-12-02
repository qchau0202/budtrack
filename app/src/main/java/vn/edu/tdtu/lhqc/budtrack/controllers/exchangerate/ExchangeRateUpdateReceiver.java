package vn.edu.tdtu.lhqc.budtrack.controllers.exchangerate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import vn.edu.tdtu.lhqc.budtrack.controllers.settings.SettingsHandler;

/**
 * BroadcastReceiver for automatically updating exchange rates weekly.
 */
public class ExchangeRateUpdateReceiver extends BroadcastReceiver {

    private static final String TAG = "ExchangeRateUpdateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Exchange rate update triggered");
        
        // Fetch and update exchange rate from API
        boolean success = ExchangeRateService.updateExchangeRateFromAPI(context);
        
        if (success) {
            Log.d(TAG, "Exchange rate updated successfully");
        } else {
            Log.e(TAG, "Failed to update exchange rate");
        }
        
        // Reschedule for next week
        SettingsHandler.scheduleWeeklyExchangeRateUpdate(context);
    }
}

