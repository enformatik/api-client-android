# api-client-android
A sample Android app that calls the Google Genomics API.

## Getting started
* If you have not done so already:
  * [Set up Android Studio](http://developer.android.com/sdk/index.html) so that you can build and install Android apps.
  * Register your Android OAuth client for Google Sign-In: http://android-developers.blogspot.com/2016/03/registering-oauth-clients-for-google.html
* Click [here](https://console.developer.google.com/flows/enableapi?apiid=genomics) to enable the Genomics API for your Android OAuth client.
* [Clone this repository](https://help.github.com/articles/working-with-repositories/)
  to your local machine
* Use Android Studio's **Import Project** option to create a project from
  the gradle files within your cloned directory.
* Run the app from whithin Android Studio and your connected device should see a basic
  drawer-based layout which lists datasets and jobs. 

Note: this is just a sample app, so it's not very feature rich - [contributions welcome](CONTRIBUTING.rst)!

Also note: to simplify the code, this app requires [Android Lollipop](https://developer.android.com/about/versions/android-5.0.html). If your device does not support Lollipop you'll need to use the emulator. Real Android projects that use the Google Genomics API would not need this restriction.
