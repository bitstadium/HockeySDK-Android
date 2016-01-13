[![Build Status](https://travis-ci.org/bitstadium/HockeySDK-Android.svg?branch=develop)](https://travis-ci.org/bitstadium/HockeySDK-Android)

## Version 3.7.0

## Introduction

HockeySDK-Android implements support for using HockeyApp in your Android application.

The following features are currently supported:

1. **Collect crash reports:** If your app crashes, a crash log is written to the device's storage. If the user starts the app again, they will be asked asked to submit the crash report to HockeyApp. This works for both beta and live apps, i.e. those submitted to Google Play or other app stores! Crash logs contain viable information for you to help resolve the issue. Furthermore you as a developer can add additional information to the report as well.

2. **Update Alpha/Beta apps:** The app will check with HockeyApp if a new version for your alpha/beta build is available. If yes, it will show a dialog to the user and let him see the release notes, the version history and start the installation process right away. You can even force the installation of certain updates.

3. **Feedback:** Besides crash reports, collecting feedback from your users from within your app is a great option to help with improving your app. You act and answer feedback directly from the HockeyApp backend.

4. **Authenticate:** To help you stay in control of closed tester groups you can identify and authenticate users against your registered testers with the HockeyApp backend. The authentication feature supports several ways of authentication.

This document contains the following sections:

1. [Requirements](#requirements)
2. [Setup](#setup)
  1. [Obtain an App Identifier](#app-identifier)
  2. [Get the SDK](#get-sdk)
  3. [Integrate HockeySDK](#integrate-sdk)
  4. [Add Crash Reporting](#crashreporting)
  5. [Add Update Distribution](#updatedistribution)
  6. [Add In-App Feedback](#feedback)
  7. [Add Authentication](#authentication)
3. [Changelog](#changelog)
4. [Advanced Setup](#advancedsetup) 
  1. [Manual Library Dependency](#manualdependency)
  2. [Crash Reporting](#crashreporting-advanced)
  3. [Update Distribution](#updatedistribution-advanced)
  4. [In-App Feedback](#feedback-advanced)
5. [Documentation](#documentation)
6. [Troubleshooting](#troubleshooting)
7. [Contributing](#contributing)
8. [Contributor License](#contributorlicense)
9. [Contact](#contact)

<a id="requirements"></a> 
## 1. Requirements

1. We assume that you already have an Android project in Android Studio or another Android IDE.
2. The SDK runs on devices with Android 2.3 or later, but you need to build your app with Android SDK 3.0 (Level 11) or later for the integration with HockeySDK.

<a id="setup"></a>
## 2. Setup

We recommend integration of our compiled library into your project using Android Studio and Gradle.
For other ways to setup the SDK, see [Advanced Setup](#advancedsetup).
A sample integration can be found in [this GitHub repository](https://github.com/bitstadium/HockeySDK-AndroidDemo).

<a id="app-identifier"></a>
### 2.1 Obtain an App Identifier

Please see the "[How to create a new app](http://support.hockeyapp.net/kb/about-general-faq/how-to-create-a-new-app)" tutorial. This will provide you with an HockeyApp-specific App Identifier to be used to initialize the SDK.

<a id="get-sdk"></a>
### 2.2 Get the SDK

Add the SDK to your app module's dependencies in Android Studio by adding the following line to your `dependencies { ... }` configuration:

```groovy
compile 'net.hockeyapp.android:HockeySDK:3.7.0'
```
also make sure your repository configuration contains

```java
repositories {
  mavenCentral()
}
```

or

```java
repositories {
  jcenter()
}
```

<a id="integrate-sdk"></a>
### 2.3 Integrate HockeySDK

1. Open your module's build.gradle file.
2. Add the following manifest placeholder to your configuration (typically the `defaultConfig`):
  
  ```groovy
  manifestPlaceholders = [HOCKEYAPP_APP_ID: "$APP_ID"]
  ```

3. The param $APP_ID must be replaced by your HockeyApp App Identifier. The app identifier can be found on the app's page in the "Overview" section of the HockeyApp backend.
4. Save your build.gradle file and make sure to trigger a Gradle build sync.
5. Open your AndroidManifest.xml file and add a `meta-data`-tag for the HockeySDK.
	
  ```xml
  <application> 
  	//your activity declarations and other stuff
  	<meta-data android:name="net.hockeyapp.android.appIdentifier" android:value="${HOCKEYAPP_APP_ID}" />
  </application>  
  ```

6. Save your AndroidManifest.xml file.

Now that you've integrated the SDK with your project it's time to make use of its features.

<a id="crashreporting"></a>
### 2.4 Add Crash Reporting
This will add crash reporting capabilities to your app. Advanced ways to configure crash reporting are covered in [Advanced Setup](#advancedsetup).

1. Open your main activity.
2. Add the following lines:

```java
import net.hockeyapp.android.CrashManager;

public class YourActivity extends Activity {
  @Override
  public void onResume() {
    super.onResume();
    // ... your own onResume implementation
    checkForCrashes();
  }
    
  private void checkForCrashes() {
    CrashManager.register(this);
  }

}
```

When the activity is resumed, the crash manager is triggered and checks if a new crash was created before. If yes, it presents a dialog to ask the user whether they want to send the crash log to HockeyApp. On app launch the crash manager registers a new exception handler to recognize app crashes.

<a id="updatedistribution"></a>
### 2.5 Add Update Distribution
This will add the in-app update mechanism to your app. For more configuration options of the update manager module see [Advanced Setup](#advancedsetup).

1. Open the activity where you want to inform the user about eventual updates. We'll assume you want to do this on startup of your main activity.
2. Add the following lines and make sure to always balance `register(...)` calls to SDK managers with `unregister()` calls in the corresponding lifecycle callbacks:

```java
import net.hockeyapp.android.UpdateManager;

public class YourActivity extends Activity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Your own code to create the view
    // ...
    
    checkForUpdates();
  }

  private void checkForUpdates() {
    // Remove this for store builds!
    UpdateManager.register(this);
  }
  
  private void unregisterManagers() {
    UpdateManager.unregister();
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

}
```

When the activity is created, the update manager checks for new updates in the background. If it finds a new update, an alert dialog is shown and if the user presses Show, they will be taken to the update activity. The reason to only do this once upon creation is that the update check causes network traffic and therefore potential costs for your users.

<a id="feedback"></a>
### 2.6 Add In-App Feedback
This will add the ability for your users to provide feedback from right inside your app. Detailed configuration options are in [Advanced Setup](#advancedsetup).

1. You'll typically only want to show the feedback interface upon user interaction, for this example we assume you have a button `feedback_button` in your view for this.
2. Add the following lines to your respective activity, handling the touch events and showing the feedback interface:

```java
import net.hockeyapp.android.FeedbackManager;

public class YourActivity extends Activitiy {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Your own code to create the view
    // ...

    FeedbackManager.register(this);

    Button feedbackButton = (Button) findViewById(R.id.feedback_button);
    feedbackButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FeedbackManager.showFeedbackActivity(MainActivity.this);
        }
    });
  }
  
}
```

When the user taps on the feedback button it will launch the feedback interface of the HockeySDK, where the user can create a new feedback discussion, add screenshots or other files for reference, and act on their previous feedback conversations.

<a id="authentication"></a>
### 2.7 Add Authentication
You can force authentication of your users through the `LoginManager` class. This will show a login screen to users if they are not fully authenticated to protect your app.

1. Retrieve your app secret from the HockeyApp backend. You can find this on the app details page in the backend right next to the "App ID" value. Click "Show" to access it. 
2. Open the activity you want to protect, if you want to protect all of your app this will be your main activity.
3. Add the following lines to this activity:

```java
import net.hockeyapp.android.LoginManager;

public class YourActivity extends Activity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Your own code to create the view
    // ...

    LoginManager.register(this, APP_SECRET, LoginManager.LOGIN_MODE_EMAIL_PASSWORD);
    LoginManager.verifyLogin(this, getIntent());
  }
}
```

Make sure to replace `APP_SECRET` with the value retrieved in step 1. This will launch the login activity every time a user launches your app.

<a id="changelog"></a>
## 3. Changelog
You can access the full changelog in our [releases-section](https://github.com/bitstadium/HockeySDK-Android/releases). The following paragraphs contain information what you might need to change when upgrading to the different new versions.

## 3.1 Upgrading from 3.6.x to 3.7.0

1. We didn't introduce any breaking changes, except that we have raised the minimum API level to 9.
2. Also consider switching to our new register-calls and adding your App ID to your configuration as described above.  
3. If you integrate the SDK using Gradle, you can remove the previously required activities from your manifest file.

```xml
 <!-- HockeySDK Activities – no longer required as of 3.7.0! -->
 <activity android:name="net.hockeyapp.android.UpdateActivity" />
 <activity android:name="net.hockeyapp.android.FeedbackActivity" />
 <activity android:name="net.hockeyapp.android.PaintActivity" />
```

<a id="advancedsetup"></a> 
## 4. Advanced Setup

<a id="manualdependency"></a> 
### 4.1 Manual Library Dependency
If you don't want to use Gradle or Maven dependency management you can also download and add the library manually. The easiest way to do this is using Android Studio.

1. Download the latest release from [here](http://hockeyapp.net/releases/#android).
2. Unzip the release distribution.
3. Copy the file libs/HockeySDK-$version.aar to the `libs` folder of your Android project. (`$version` is the version of the downloaded SDK, if the version is < 3.7.0 it will be a .jar file instead)
4. Configure your development tools to use the .aar/.jar file.
5. In Android Studio, create a new module via `File > New > New Module`
6. Select **Import .JAR/.AAR Package** and click **Next**.
7. In the next menu select the .aar/.jar file you just copied to the libs folder. You can rename the module to whatever you want, but we in general recommend leaving it as is. If you don't rename the module, it will match the name of the .aar/.jar file, in this case **HockeySDK-3.7.0**. This way you'll quickly know which version of the SDK you are using in the future.
8. Make sure Android Studio added the necessary code to integrate the HockeySDK:

Head over to your app's `build.gradle` to verify the dependency was added correctly. It should look like this:

```groovy
dependencies {
	//your other dependencies
	//...
	
    compile project(':HockeySDK-3.7.0')
}
```
Next, make sure your `settings.gradle` contains the new module:

```groovy
include ':app', ':HockeySDK-3.7.0'
```

Finally, check the `build.gradle` of the newly added module:
```groovy
configurations.maybeCreate("default")
artifacts.add("default", file('HockeySDK-3.7.0.aar'))
```

Once you have verified that everything necessary has been added, proceed with [SDK integration](#integrate-sdk).

<a id="crashreporting-advanced"></a>
### 4.2 Crash Reporting
The following options show only some of the many possibilities to interact and fine-tune the crash reporting feature. For more please check the full documentation of the classes `net.hockeyapp.android.CrashManager` and `net.hockeyapp.android.CrashManagerListener` in our [documentation](#documentation).

To configure a custom `CrashManagerListener` use the following `register()` method when configuring the manager:

```java
  CrashManager.register(context, APP_ID, new MyCustomCrashManagerListener());
```

#### 4.2.1 Autosend crash reports
Crashes are usually sent the next time the app starts. If your custom crash manager listener returns `true` for `shouldAutoUploadCrashes()`, crashes will be sent without any user interaction, otherwise a dialog will appear allowing the user to decide whether they want to send the report or not.

```java
public class MyCustomCrashManagerListener extends CrashManagerListener {
  @Override
  public boolean shouldAutoUploadCrashes() {
    return true;
  }
}
```

#### 4.2.2 Attach additional meta data
Starting with HockeyApp 3.6.0, you can add additional meta data (e.g. user-provided information) to a crash report. 
To achieve this call `CrashManager.handleUserInput()` and provide an instance of `net.hockeyapp.android.objects.CrashMetaData`.


<a id="updatedistribution-advanced"></a>
### 4.3 Update Distribution
You can customize the behavior of the in-app update process in several ways. The main class to look at is `net.hockeyapp.android.UpdateManagerListener` in our [documentation](#documentation).

To configure a custom `UpdateManagerListener` use the following `register()` method when configuring the manager:

```java
  UpdateManager.register(context, APP_ID, new MyCustomUpdateManagerListener());
```

### 4.3.1 Providing your own user interface for the update process
The `UpdateManager` will select a suitable activity or fragment depending on the availability of the feature. You can also supply your own by implementing the respective methods:

```
public class MyCustomUpdateManagerListener extends UpdateManagerListener {
  @Override
  public Class<? extends UpdateActivity> getUpdateActivityClass() {
    return MyCustomUpdateActivity.class;
  }

  @Override
  public Class<? extends UpdateFragment> getUpdateFragmentClass() {
    return MyCustomUpdateFragment.class;
  }
}
```

<a id="feedback-advanced"></a> 
### 4.4 In-App Feedback
As stated in the setup guide you'll typically want to show the feedback interface from an `onClick`, `onMenuItemSelected`, or `onOptionsItemSelected` listener method.

### 4.4.1 Capturing a screenshot for feedback
You can configure a notification to show to the user. When they select the notification the SDK will create a screenshot from the app in its current state and create a new feedback draft from it.

1. Open the activity from which you want to enable the screenshot.
2. Add this to your `onCreate()` method or an `onClick()` listener:

```java
  FeedbackManager.setActivityForScreenshot(YourActivity.this);
```

<a id="documentation"></a>
## 5. Documentation

Our documentation can be found on [HockeyApp](http://hockeyapp.net/help/sdk/android/3.7.0/index.html).

<a id="troubleshooting"></a>
## 6.Troubleshooting

1. Check if the APP_ID matches the App ID in HockeyApp.

2. Check if the `applicationId` in your `build.gradle` file matches the Bundle Identifier of the app in HockeyApp. HockeyApp accepts crashes only if both the App ID and the bundle identifier match their corresponding values in your app. Please note that the package value in your `AndroidManifest.xml` file might differ from the bundle identifier.

3. If your app crashes and you start it again, does the dialog show up which asks the user to send the crash report? If not, please crash your app again, then connect the debugger and set a break point in CrashManager.java, method [register](https://github.com/bitstadium/HockeySDK-Android/blob/master/src/main/java/net/hockeyapp/android/CrashManager.java#L100) to see why the dialog is not shown.

4. If it still does not work, please [contact us](http://support.hockeyapp.net/discussion/new).

<a id="contributing"></a>
## 7. Contributing

We're looking forward to your contributions via pull requests.

**Coding style**

* Please follow our [coding styleguide](https://github.com/bitstadium/android-guidelines)
* Every check in should build and lint without errors

**Development environment**

* Mac/Linux/Windows machine running the latest version of [Android Studio and the Android SDK](https://developer.android.com/sdk/index.html)

<a id="contributorlicense"></a>
## 8. Contributor License

You must sign a [Contributor License Agreement](https://cla.microsoft.com/) before submitting your pull request. To complete the Contributor License Agreement (CLA), you will need to submit a request via the [form](https://cla.microsoft.com/) and then electronically sign the CLA when you receive the email containing the link to the document. You need to sign the CLA only once to cover submission to any Microsoft OSS project. 

<a id="contact"></a>
## 9. Contact

If you have further questions or are running into trouble that cannot be resolved by any of the steps here, feel free to open a GitHub issue here or contact us at [support@hockeyapp.net](mailto:support@hockeyapp.net)
