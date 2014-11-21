package msopentech.azure;

import java.util.Set;

import android.support.v4.app.NotificationCompat;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.app.PendingIntent;


import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.microsoft.windowsazure.messaging.*;
//import com.microsoft.windowsazure.notifications.NotificationsManager;

/**
 * Apache Cordova plugin for Windows Azure Notification Hub
 */
public class NotificationHub extends CordovaPlugin {

public static final String LOG_TAG = "luca_log";
    /**
     * The callback context from which we were invoked.
     */
    protected static CallbackContext _callbackContext = null;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        _callbackContext = callbackContext;
        try {

            if (action.equals("registerApplication")) {
                    String hubName = args.getString(0);
                    String connectionString = args.getString(1);
                    String senderId = args.getString(4);
                    registerApplication(hubName, connectionString, senderId);
                    return true;
            }

            if (action.equals("unregisterApplication")) {
                String hubName = args.getString(0);
                String connectionString = args.getString(1);
                unregisterApplication(hubName, connectionString);
                return true;
            }

            return false; // invalid action
        } catch (Exception e) {
            _callbackContext.error(e.getMessage());
        }
        return true;
    }

    /**
     * Asynchronously registers the device for native notifications.
     */
    @SuppressWarnings("unchecked")
    private void registerApplication(final String hubName, final String connectionString, final String senderId) {

        try {
                //NotificationsManager.handleNotifications(this, senderId, MyNotificationHandler.class);
            final GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(cordova.getActivity());
            final com.microsoft.windowsazure.messaging.NotificationHub hub =
                    new com.microsoft.windowsazure.messaging.NotificationHub(hubName, connectionString, cordova.getActivity());

            new AsyncTask() {
                @Override
                protected Object doInBackground(Object... params) {
                   try {
                      String gcmId = gcm.register(senderId);
                      NativeRegistration registrationInfo = hub.register(gcmId);

                      JSONObject registrationResult = new JSONObject();
                      registrationResult.put("registrationId", registrationInfo.getRegistrationId());
                      registrationResult.put("channelUri", registrationInfo.getGCMRegistrationId());
                      registrationResult.put("notificationHubPath", registrationInfo.getNotificationHubPath());
                      registrationResult.put("event", "registerApplication");
                      
                      PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, registrationResult);
                      // keepKallback is used to continue using the same callback to notify about push notifications received
                      pluginResult.setKeepCallback(true);

                      NotificationHub.getCallbackContext().sendPluginResult(pluginResult);

                   } catch (Exception e) {
                       NotificationHub.getCallbackContext().error(e.getMessage());
                   }
                   return null;
               }
             }.execute(null, null, null);
        } catch (Exception e) {
            NotificationHub.getCallbackContext().error(e.getMessage());
        }
    }

    /**
     * Unregisters the device for native notifications.
     */
    private void unregisterApplication(final String hubName, final String connectionString) {
        try {
            final com.microsoft.windowsazure.messaging.NotificationHub hub =
                    new com.microsoft.windowsazure.messaging.NotificationHub(hubName, connectionString, cordova.getActivity());
            hub.unregister();
            NotificationHub.getCallbackContext().success();
        } catch (Exception e) {
            NotificationHub.getCallbackContext().error(e.getMessage());
        }
    }

    /**
     * Handles push notifications received.
     */
    public static class PushNotificationReceiver extends android.content.BroadcastReceiver {
        
        int NOTIFICATION_ID = 1;
        private  NotificationManager mNotificationManager;
        
        private void sendNotification(Context context, String msg){

                 mNotificationManager = (NotificationManager)
                                 context.getSystemService(Context.NOTIFICATION_SERVICE);

		 PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                                  new Intent(context, it.ismb.FloodisApp.FloodisApp.class), 0);

                 Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                 
                 String uri = "@drawable/icon";
                 int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());
                 String appName =context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
                 
                 NotificationCompat.Builder mBuilder =
                  new NotificationCompat.Builder(context)
                  .setSmallIcon(imageResource) // todo get app icon
                  .setContentTitle(appName)
//                .setStyle(new NotificationCompat.BigTextStyle()
//                           .bigText())
                  .setContentText(msg)
                  .setSound(alarmSound);

		  mBuilder.setAutoCancel(true);
                  mBuilder.setContentIntent(contentIntent);

             mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
                
        }
        
        @Override
        public void onReceive(Context context, Intent intent) {
            
            
            JSONObject json = new JSONObject();
            String msg = new String(); 
            try {

                Set<String> keys = intent.getExtras().keySet();
                for (String key : keys) {
                    json.put(key, intent.getExtras().get(key));
                }
                msg = json.getString("message");

            } catch (JSONException e) {
                e.printStackTrace();
            }       

                


            if (NotificationHub.getCallbackContext() == null){
                sendNotification(context, msg);
                return;
            }else{
                PluginResult result = new PluginResult(PluginResult.Status.OK, json);
                result.setKeepCallback(true);
                NotificationHub.getCallbackContext().sendPluginResult(result);
            
            }
        }

    }

    /**
     * Returns plugin callback.
     */
    protected static CallbackContext getCallbackContext() {
        return _callbackContext;
    }
}