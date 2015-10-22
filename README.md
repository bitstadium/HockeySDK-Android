## Version 3.6.2

## Introduction

HockeySDK-Android implements support for using HockeyApp in your Android application.

The following features are currently supported:

1. **Collect crash reports:** If your app crashes, a crash log is written to the device's storage. If the user starts the app again, he is asked to submit the crash report to HockeyApp. This works for both beta and live apps, i.e. those submitted to Google Play or other app stores!
I
2. **Update Alpha/Beta apps:** The app will check with HockeyApp if a new version for your alpha/beta build is available. If yes, it will show a dialog to the user and let him see the release notes, the version history and start the installation process right away. 

3. **Feedback:** Collect feedback from your users from within your app and communicate directly with them using the HockeyApp backend.

4. **Authenticate:** Identify and authenticate users against your registered testers with the HockeyApp backend

This document contains the following sections:

1. [Requirements](#requirements)
2. [Setup](#setup)
3. [Advanced Setup](#advancedsetup) 
   1. [Manual Library Dependency](#manualdependency)
   2. [In-App Feedback](#feedback)
   3. [Crash Reporting](#crashreporting)
4. [Documentation](#documentation)
5. [Troubleshooting](#troubleshooting)
6. [Contributing](#contributing)
7. [Contributor License](#contributorlicense)
8. [Contact](#contact)

<a id="requirements"></a> 
## 1. Requirements

1. We assume that you already have an Android project in Android Studio or another Android IDE.
2. The SDK runs on devices with Android 2.1 or later, but you need to build your app with Android SDK 3.0 (Level 11) or later for the integration with HockeySDK.

<a id="setup"></a>
## 2. Setup

We recommend integration of our compiled library into your project using Android Studio.
For other ways to setup the SDK, see [Advanced Setup](#advancedsetup).
A sample integration can be found in [this GitHub repository](https://github.com/akoscz/HockeyAppSample).

### 2.1 Obtain an App Identifier

Please see the "[How to create a new app](http://support.hockeyapp.net/kb/about-general-faq/how-to-create-a-new-app)" tutorial. This will provide you with an HockeyApp specific App Identifier to be used to initialize the SDK.

### 2.2 Download the SDK

Add the SDK to your app module's dependencies in Android Studio by adding the following line to your `dependencies { ... }` configuration:

```groovy
  compile 'net.hockeyapp.android:HockeySDK:3.6.2'
```
also make sure your repository configuration contains

```java
  mavenCentral()
```

<a id="setup-modifycode"></a>
### 2.3 Modify Code

1. Open your AndroidManifest.xml file.
2. Add the `INTERNET` permission to your manifest:

  ```xml
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.INTERNET" />
  ```
2. If you also want in-app updates:
    * Add the following line as a child element of `<application>`:

    ```xml
      <activity android:name="net.hockeyapp.android.UpdateActivity" />
    ```
    * Add the following permission to your manifest:

    ```xml
      <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    ```
4. Save your AndroidManifest.xml file.
5. Open your main activity or the activity in which you want to integrate the update process and crash reporting.
6. Add the following lines:

```java
import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

public class YourActivity extends Activity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Your own code to create the view
    // ...
    
    checkForUpdates();
  }

  @Override
  public void onResume() {
    super.onResume();
    checkForCrashes();
  }
  
  @Override
  public void onPause() {
    super.onPause();
    unregisterManagers();
  }
  
  @Override
  public void onDestroy() {
    super.onDestroy();
    unregisterManagers();
  }
  
  private void checkForCrashes() {
    CrashManager.register(this, APP_ID);
  }
  
  private void checkForUpdates() {
    // Remove this for store builds!
    UpdateManager.register(this, APP_ID);
  }
  
  private void unregisterManagers() {
    UpdateManager.unregister();
    // unregister other managers if necessary...
  }
  
  // Probably more methods
}
```

7. If you only want crash reporting but no in-app updates leave out all the `UpdateManager`-related lines.
8. The param APP_ID has to be replaced by your HockeyApp App Identifier. The app identifier can be found on the app's page in the "Overview" section of the HockeyApp backend.
9. Always make sure to balance `register(...)` calls to SDK managers with `unregister()` calls in the corresponding lifecycle callbacks.

The above code does two things: 

1. When the activity is created, the update manager checks for new updates in the background. If it finds a new update, an alert dialog is shown and if the user presses Show, they will be taken to the update activity.
2. When the activity is resumed, the crash manager is triggered and checks if a new crash was created before. If yes, it presents a dialog to ask the user whether they want to send the crash log to HockeyApp. On app launch the crash manager registers a new exception handler to recognize app crashes.

The reason for the two different entry points is that the update check causes network traffic and therefore potential costs for your users. In contrast, the crash manager only searches for new files in the file system, i.e. the call is pretty fast. 

**Congratulation, now you're all set to use HockeySDK!**

<a id="advancedsetup"></a> 
## 3. Advanced Setup

<a id="manualdependency"></a> 
### 3.2 Manual Library Dependency

If you don't want to use Android Studio, Gradle, or Maven you can also manually download and add the library manually.

1. Download the latest release from [here](http://hockeyapp.net/releases/#android).
2. Unzip the file.
3. Copy the file libs/HockeySDK.jar to the libs folder of your Android project.
4. If you use Eclipse with ADT 16 or older, then add the .jar file to your classpath. With ADT 17 or newer, this is done automatically.

Then proceed with [Modifying your Code](#setup-modifycode).

<a id="feedback"></a> 
### 3.2 In-App Feedback

Starting with HockeySDK 3.0, you can integrate a feedback view in your app, so your users can send textual feedback or even create screenshots and upload them to HockeyApp:

1. Open your AndroidManifest.xml.
2. Add the following lines as child elements of your `<application>`: 

  ```xml
    <activity android:name="net.hockeyapp.android.FeedbackActivity" />
    <activity android:name="net.hockeyapp.android.PaintActivity" />
  ```

3. If not already present, add the `INTERNET`-permission:

  ```xml
    <uses-permission android:name="android.permission.INTERNET" />
  ```

4. Save the AndroidManifest.xml.
5. Open the activity from which you want to show the feedback view.
6. Add the following method:

  ```java
    public void showFeedbackActivity() {
      FeedbackManager.register(this, APP_ID, null);
      FeedbackManager.showFeedbackActivity(this);
    }
  ```

7. Replace APP_ID with your app's identifier.
8. Call the method `showFeedbackActivity()` in an `onClick`, `onMenuItemSelected`, or `onOptionsItemSelected` listener method.

<a id="crashreporting"></a> 
### 3.3 Crash Reporting

The following options show only some of the many possibilities to interact and fine-tune the crash reporting feature. For more please check the full documentation of the classes `net.hockeyapp.android.CrashManager` and `net.hockeyapp.android.CrashManagerListener` in our [documentation](#documentation).

To configure a custom `CrashManagerListener` use the following `register()` method when configuring the manager:

```java
  CrashManager.register(context, APP_ID, new MyCustomCrashManagerListener());
```


#### 3.3.1 Autosend crash reports

Crashes are sent the next time the app starts. If your custom crash manager listener returns `true` for `shouldAutoUploadCrashes()`, crashes will be sent without any user interaction, otherwise a dialog will appear allowing the user to decide whether they want to send the report or not.

```java

public class MyCustomCrashManagerListener extends CrashManagerListener {
  @Override
  public boolean shouldAutoUploadCrashes() {
    return true;
  }
}

```

#### 3.3.2 Attach additional meta data

Starting with HockeyApp 3.6.0 you can add additional meta data (e.g. user-provided information) to a crash report. 
To achieve this call `CrashManager.handleUserInput()` and provide an instance of `net.hockeyapp.android.objects.CrashMetaData`.

<a id="documentation"></a>
## 4. Documentation

Our documentation can be found on [HockeyApp](http://hockeyapp.net/help/sdk/android/3.6/index.html).

<a id="troubleshooting"></a>
## 5.Troubleshooting

1. Check if the APP_ID matches the App ID in HockeyApp.

2. Check if the `applicationId` in your `build.gradle` file matches the Bundle Identifier of the app in HockeyApp. HockeyApp accepts crashes only if both the App ID and the bundle identifier equal their corresponding values in your app. Please note that the package value in your `AndroidManifest.xml` file might differ from the bundle identifier.

3. If your app crashes and you start it again, is the dialog shown which asks the user to send the crash report? If not, please crash your app again, then connect the debugger and set a break point in CrashManager.java, method [register](https://github.com/bitstadium/HockeySDK-Android/blob/master/src/main/java/net/hockeyapp/android/CrashManager.java#L100) to see why the dialog is not shown.

4. If it still does not work, please [contact us](http://support.hockeyapp.net/discussion/new).

<a id="contributing"></a>
## 6. Contributing

We're looking forward to your contributions via pull requests.

**Development environment**

* Mac/Linux/Windows machine running the latest version of [Android Studio and the Android SDK](https://developer.android.com/sdk/index.html)

<a id="contributorlicense"></a>
## 7. Contributor License

You must sign a [Contributor License Agreement](https://cla.microsoft.com/) before submitting your pull request. To complete the Contributor License Agreement (CLA), you will need to submit a request via the [form](https://cla.microsoft.com/) and then electronically sign the CLA when you receive the email containing the link to the document. You need to sign the CLA only once to cover submission to any Microsoft OSS project. 

<a id="contact"></a>
## 8. Contact

If you have further questions or are running into trouble that cannot be resolved by any of the steps here, feel free to open a GitHub issue here or contact us at [support@hockeyapp.net](mailto:support@hockeyapp.net)
