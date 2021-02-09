# Assignment 3
**Due by 11:59pm on Monday, 2/22/2021** <br />
**Demo due by 11:59pm on Monday, 3/8/2021**

In this assignment, we'll adapt our weather app to gracefully deal with transitions in the activity lifecycle by implementing the `ViewModel` architecture.  You'll also add some basic user preferences to the app.  There are a few different tasks associated with this assignment, described below.  This repository provides you with some starter code that implements the connected weather app from assignment 2.

**NOTE: make sure to add your own API key as described in [`MainActivity.java`](app/src/main/java/com/example/android/lifecycleweather/MainActivity.java#L35-L51) to make the app work.**

## 1. Implement the ViewModel architecture and use Retrofit

One thing you might notice is that when you do things like rotate your device when viewing the main activity, the activity is recreated, resulting in a new network call to fetch the same weather forecast data.  You can know this is happening because the loading indicator will be displayed, indicating that the network call to fetch forecast data from the OpenWeather API is being re-executed.  This happens because the network call is initiated in the `onCreate()` function in the main activity class.

> Note that this version of the app uses [Volley](https://developer.android.com/training/volley) to perform network communication.  It should hopefully be straightforward to understand how the Volley setup works here, but don't hesitate to ask if you have questions.

Your first task in this assignment is to fix this problem by moving the main activity's data management behind a `ViewModel` to make our activity better cope with lifecycle transitions.  Doing this will involve a few different sub-tasks:

  * Set up a Retrofit service interface that you'll use to represent communication with the OpenWeather API.  Your service interface will only need one method to call the OpenWeather 5 day/3 hour forecast API.  Make sure to set this method up with appropriate arguments to instantiate all the fields you'll need to put into the URL to make the API call (e.g. city name, units, API key).

  * Implement a Repository class to perform the data operations associated with communicating with the OpenWeather API.  This Repository class will contain an instantiation of your Retrofit service, which you'll use to make API calls.  The Repository class will also be the location where the forecast data is ultimately stored within the app (as `LiveData`).  Make sure to implement a reasonable caching strategy in the Repository, so that new data is loaded only when necessary (e.g. only when the user changes the city for which a forecast is being displayed and/or when the cached forecast data has become stale, based on a timestamp).  You should include log statements to demonstrate when your app is fetching new data and when it is relying on cached data.

  * Implement a `ViewModel` class to serve as the intermediary between the Repository class and the UI.  This class should contain methods for triggering a new data load and for returning the forecast data to the UI.

  * Set up the UI (i.e. the main activity class) to observe changes to the forecast data held behind the `ViewModel` and to update the state of the UI as appropriate based on changes to that data.  Importantly, this should be done in such a way that the progress bar and error message behave as currently implemented in the UI.

As a result of these changes, you should see your app fetch results from the OpenWeather API only one time through typical usage of the app, including through rotations of the phone and navigation around the app.

## 2. Add some basic user preferences to the app

When you run the version of the app provided in this repository, you'll probably notice a settings icon in the title bar of the main activity.  Your second task in this assignment is to create a new activity named `SettingsActivity` that implements a user preferences screen using a `PreferenceFragment`.  This activity should be launched when you click the settings icon in the main activity.

The preferences screen should allow the user to set the following preferences:

  * **Forecast units** - The user should be allowed to select between "Imperial", "Metric", and "Kelvin" temperature units.  The currently-selected value should be displayed as the summary for the preference.  See the OpenWeather API documentation here for more info on how this preference value will be used to formulate API requests: https://openweathermap.org/forecast5#data.

  * **Forecast location** - The user should be allowed to enter an arbitrary location for which to fetch a forecast.  The currently-set value should be set as the summary for the preference.  You can specify any default location you'd like.  See the OpenWeather API documentation here for more info on how this preference value will be used to formulate API requests: https://openweathermap.org/forecast5#name5.

The settings of these preferences should affect the URL used to query the OpenWeather API.  The app should be hooked up so that any change to the preferences results in the OpenWeather API being re-queried and the new results being displayed.  Importantly, there are a couple places in the UI where the "F" is hard-coded to indicate a Fahrenheit temperature.  Make sure to update these locations to display the appropriate unit for the current setting ("F" for Fahrenheit, "C" for Celsius, and "K" for Kelvin).

## Extra credit: Kotlin implementation

For extra credit, you may implement this assignment using Kotlin instead of Java.  To earn this extra credit, you must still implement the app using the `ViewModel`-based architecture described above.  Note that you may have to replace some of the Java starter code with Kotlin to be able to implement the app described above.

## Submission

As usual, we'll be using GitHub Classroom for this assignment, and you will submit your assignment via GitHub.  Make sure your completed files are committed and pushed by the assignment's deadline to the master branch of the GitHub repo that was created for you by GitHub Classroom.  A good way to check whether your files are safely submitted is to look at the master branch your assignment repo on the github.com website (i.e. https://github.com/osu-cs492-w21/assignment-3-YourGitHubUsername/). If your changes show up there, you can consider your files submitted.

## Grading criteria

This assignment is worth 100 points, broken down as follows:

  * 70 points: Implements `ViewModel` architecture
    * 10 points: implements Retrofit service interface to represent communication with the OpenWeather API
    * 20 points: implements Repository class to perform data fetching and store data
    * 10 points: implements `ViewModel` class to interface between UI and Repository
    * 15 points: observes `ViewModel` data in UI and updates the UI state appropriately (including progress bar and error message) as data changes
    * 10 points: data is cached in Repository, and cached data is used when appropriate instead of fetching new data
    * 5 points: adds logging statements to demonstrate when cached data is being used and when data is being fetched

  * 30 points: Implements user settings activity
    * 15 points: implements a preference fragment to allow the user to select temperature units and forecast location
    * 5 points: summaries of both preferences reflect the current values set for those preferences
    * 10 points: changing preference values results in new data being fetched/displayed and correct updates being made to the UI, as described above

You may also earn 10 points of extra credit for implementing your app using Kotlin.  Note that if you use Kotlin, your app will still be graded as normal based on the criteria described above.
