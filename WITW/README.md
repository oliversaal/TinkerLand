Where in the world
=====================

Demonstrates use of the Google Play services Location API and Shared Preferences to retrieve user
data from local storage.

Introduction
============

This app shows a simple way of getting a device's last known location, which
is usually equivalent to the device's current location.
The accuracy of the location returned is based on the location
permissions you've requested and the location sensors that are currently active
for the device. It also demonstrates the use of Android's SharedPreferences for data storage.
Android provides several options for you to save persistent application data. SharedPreferences
store private primitive data in key-value pairs.

To run this app, **location must be enabled**.

This app uses
[Google Play services (GoogleApiClient)](ihttps://developer.android.com/reference/com/google/android/gms/common/api/GoogleApiClient.html)
[Android (SharedPreferences)](ihttps://developer.android.com/reference/android/content/SharedPreferences.html)

Prerequisites
--------------

- Android API Level >v11
- Android Build Tools >v25
- Google Support Repository

Getting Started
---------------

This app uses the Gradle build system. To build this project, use the
"gradlew build" command or use "Import Project" in Android Studio.

Support
-------

- Stack Overflow: http://stackoverflow.com/questions/tagged/google-play-services
- Android: http://developer.android.com/guide/topics/data/data-storage.html#pref
- Digital Art Thingy: http://digitalartthingy.com

If you've found an error in this app, please file an issue:
https://github.com/oliversaal/TinkerLand

Patches are encouraged, and may be submitted via github.

License
-------

Copyright (C) 2017 Digital Art Thingy Inc.

