Windows Azure Notification Hubs plugin for Apache Cordova
==================================
Exposes Windows Azure [Notification Hubs](http://www.windowsazure.com/en-us/services/notification-hubs/) functionality as Apache Cordova Plugin. Support of Windows8, Windows Phone8, iOS and Android.

### Sample usage ###

    var connectionString = "Endpoint=sb://[service bus name space].servicebus.windows.net/;SharedAccessKeyName=DefaultFullSharedAccessSignature;SharedAccessKey=[notification hub full key]",
        notificationHubPath = "[notification hub name]";

    var hub = new WindowsAzure.Messaging.NotificationHub(notificationHubPath, connectionString);

    hub.registerApplicationAsync().then(function (result) {
        console.log("Registration successful: " + result.registrationId);
    });

    hub.onPushNotificationReceived = function (msg) {
        console.log("Push Notification received: " + msg);
    };;

### Copyrights ###
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

#### Credits ####

Some pieces of iOS code were taken from the [PushPlugin](https://github.com/phonegap-build/PushPlugin) notifications plugin.

#### Warning ####

This plugin is a fork of [cordova-plugin-azure-notificationhub](https://github.com/sgrebnov/cordova-plugin-azure-notificationhub), with some heavy changes to the registration routines meant to make it work with [MicTorino](http://www.mictorino.it/web/) managed push notification registration service.