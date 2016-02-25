# Release Notes

## 3.7.1
* [NEW] Crash reporting got a little better. We now attach more meta data to crash reports, i.e. the name of the thread.
* [NEW] The SDK includes it's own `HockeyLog`-class that allows for fine-grained control of the amount of info HockeySDK-Android writes to LogCat
* [NEW] Added Spanish localization. Thanks a lot to [Rodrigo](https://github.com/Papipo) for to our SDK.
* [IMPROVEMENT] We've added a section to our readme that explains the advanced configuration of Android permissions. [(#124)](https://github.com/bitstadium/HockeySDK-Android/issues/124)
* [IMPROVEMENT] We've added a section to our readme that explains how to provide your own strings & localization. [(#116)](https://github.com/bitstadium/HockeySDK-Android/issues/116)
* [IMPROVEMENT] We play nicer with Proguard: we've prefixed our resources and adjusted the proguard settings for HockeySDK. [#121](https://github.com/bitstadium/HockeySDK-Android/issues/121)
* [BUGFIX] Fix crash in `LoginTask` caused by an IOException when authorizing the user by email only.
* [BUGFIX] Use seperate strings for the autorization UI [#133](https://github.com/bitstadium/HockeySDK-Android/pull/133)
* [BUGFIX] Fix for [#128](https://github.com/bitstadium/HockeySDK-Android/issues/128) that caused problems when creating the storage directory structure for the HockeySDK