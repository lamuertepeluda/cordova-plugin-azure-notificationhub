package msopentech.azure;

import java.util.Set;
import java.util.List;
import java.util.Collections;

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
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Apache Cordova plugin for Windows Azure Notification Hub
 */
public class NotificationHub extends CordovaPlugin {

 public static final String LOG_TAG = "plugin_log";
 /**
  * The callback context from which we were invoked.
  */
 protected static CallbackContext _callbackContext = null;

 @Override
 public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
  _callbackContext = callbackContext;
  try {

   if (action.equals("registerApplication")) {
    String senderId = args.getString(2);
    registerApplication(
	senderId);
    return true;
   }

   if (action.equals("unregisterApplication")) {
    unregisterApplication();
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
 private void registerApplication(
 final String senderId) {

  try {
   final GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(cordova.getActivity());

   new AsyncTask() {
    @Override
    protected Object doInBackground(Object... params) {
     try {
      String gcmId = gcm.register(senderId);
      JSONObject registrationResult = new JSONObject();
      registrationResult.put("registrationId", gcmId);
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
 private void unregisterApplication(){
  try {
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
  private NotificationManager mNotificationManager;

  private void sendNotification(Context context, String msg) {

   mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
   String packageName = context.getApplicationContext().getPackageName();
   Intent intent = new Intent();

   final PackageManager pm = context.getPackageManager();

   Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
   mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

   List<ResolveInfo> appList = pm.queryIntentActivities(mainIntent, 0);
   Collections.sort(appList, new ResolveInfo.DisplayNameComparator(pm));

   for (ResolveInfo temp : appList) {

    if (temp.activityInfo.packageName.equals(packageName)) {
     intent.setClassName(context, temp.activityInfo.name);
//				System.out.println("### package and activity name = "+ temp.activityInfo.name);
     break;
    }
   }

   PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

   Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

   String uri = "@drawable/icon";
   int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());
   String appName = context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();

   NotificationCompat.Builder mBuilder
           = new NotificationCompat.Builder(context)
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

   if (NotificationHub.getCallbackContext() == null) {
    sendNotification(context, msg);
    return;
   } else {
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
