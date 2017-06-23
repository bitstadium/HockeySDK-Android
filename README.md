[![Build Status](https://travis-ci.org/bitstadium/HockeySDK-Android.svg?branch=develop)](https://travis-ci.org/bitstadium/HockeySDK-Android)
[![Slack Status](https://slack.hockeyapp.net/badge.svg)](https://slack.hockeyapp.net)

## Version 4.1.5

HockeySDK-Android implements support for using HockeyApp in your Android applications.

The following features are currently supported:

1. **Crash Reporting:** If your app crashes, a crash log is written to the device's storage. If the user starts the app again, they will be asked to submit the crash report to HockeyApp. This works for both beta and live apps, i.e. those submitted to Google Play or other app stores. Crash logs contain viable information for you to help resolve the issue. Furthermore, you as a developer can add additional information to the report as well.

2. **User Metrics:** Understand user behavior to improve your app. Track usage through daily and monthly active users, monitor crash impacted users, as well as customer engagement through session count. User Metrics requires a minimum API level of 14 (Android 4.x Ice Cream Sandwich).

3. **Update alpha/beta apps:** The app will check with HockeyApp if a new version for your alpha/beta build is available. If yes, it will show a dialog to users and let them see the release notes, the version history and start the installation process right away. You can even force the installation of certain updates.

4. **Feedback:** Besides crash reports, collecting feedback from your users from within your app is a great option to help with improving your app. You act on and answer feedback directly from the HockeyApp backend.

5. **Authenticate:** To help you stay in control of closed tester groups you can identify and authenticate users against your registered testers with the HockeyApp backend. The authentication feature supports several ways of authentication.


## 2. Setup

It is super easy to use HockeyApp in your Android app. Have a look at our [documentation](https://support.hockeyapp.net/kb/client-integration-android/hockeyapp-for-android-sdk) and onboard your app within minutes.

## 3. Documentation

Please visit [our landing page](https://support.hockeyapp.net/kb/client-integration-android) as a starting point for all of our documentation.

Please check out our [getting started documentation](https://support.hockeyapp.net/kb/client-integration-android/hockeyapp-for-android-sdk), [changelog](https://github.com/bitstadium/HockeySDK-Android/releases/tag/4.1.5), [java doc](https://www.hockeyapp.net/help/sdk/android/4.1.5/index.html) as well as our [troubleshooting section](https://www.hockeyapp.net/help/sdk/android/4.1.5/docs/docs/Guide-Installation-Setup.html#troubleshooting).

## 4. Contributing

We're looking forward to your contributions via pull requests to our [repo on GitHub](https://github.com/bitstadium/HockeySDK-Android).

### 4.1 Development environment

* A Mac running the latest version of OS X.
* Get the latest Xcode from the Mac App Store.
* [AppleDoc](https://github.com/tomaz/appledoc) 
* [CocoaPods](https://cocoapods.org/)
* [Carthage](https://github.com/Carthage/Carthage)

### 4.2 Code of Conduct

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

### 4.3 Contributor License

You must sign a [Contributor License Agreement](https://cla.microsoft.com/) before submitting your pull request. To complete the Contributor License Agreement (CLA), you will need to submit a request via the [form](https://cla.microsoft.com/) and then electronically sign the CLA when you receive the email containing the link to the document. You need to sign the CLA only once to cover submission to any Microsoft OSS project. 

## 5. Contact

If you have further questions or are running into trouble that cannot be resolved by any of the steps [in our troubleshooting section](https://support.hockeyapp.net/kb/client-integration-android/hockeyapp-for-android-sdk#6-troubleshooting), feel free to open an issue here, contact us at [support@hockeyapp.net](mailto:support@hockeyapp.net) or join our [Slack](https://slack.hockeyapp.net).