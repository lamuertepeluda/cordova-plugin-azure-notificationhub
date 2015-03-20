/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
module.exports = {
 registerApplication: function (success, fail) {//, params) {
  //Params seem not needed on windows...
  try {
   var pushNotificationCallback = window.NotificationHub_onNotificationReceivedGlobal;
   var pushNotificationHandler = function (e) {
    var notificationTypeName = "";
    var notificationPayload;
    var msg = {};

    try {
     var notificationType = Windows.Networking.PushNotifications.PushNotificationType;
     switch (e.notificationType) {
      case notificationType.toast:
       notificationPayload = e.toastNotification.content.getXml();
       notificationTypeName = "Toast";
       break;
      case notificationType.tile:
       notificationPayload = e.tileNotification.content.getXml();
       notificationTypeName = "Tile";
       break;
      case notificationType.badge:
       notification = e.badgeNotification.content.getXml();
       notificationTypeName = "Badge";
       break;
      case notificationType.raw:
       notificationPayload = e.rawNotification.content;
       notificationTypeName = "Raw";
       break;
     }
     msg.notificationTypeName = notificationTypeName;
     msg.notificationPayload = notificationPayload;

     pushNotificationCallback(msg);
    } catch (ex) {
     console.error("pushNotificationHandler::Exception caught", ex);
    }
    e.cancel = true;
   };

   var notificationChannel = null;

   Windows.Networking.PushNotifications.PushNotificationChannelManager.createPushNotificationChannelForApplicationAsync().then(function (channel) {
    notificationChannel = channel;
    notificationChannel.onpushnotificationreceived = pushNotificationHandler;
    var msg = {
     event: 'registerApplication',
     registrationId: notificationChannel.uri
    };
    success(msg);
   }, fail);

  } catch (ex) {
   fail(ex);
  }

 },
 unregisterApplication: function (success, fail, args) {
  try {
   //The following looks unsupported
   //          (new NotificationHubRuntimeProxy.HubApi()).unregisterNativeAsync(notificationHubPath, connectionString);
   success();

  } catch (ex) {
   fail(ex);
  }
 }

};

require("cordova/exec/proxy").add("NotificationHub", module.exports);
