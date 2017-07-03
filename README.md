# NewsApp (Refactored)
Read news from different newspapers : TuoiTre, ThanhNien, TienPhong, DanTri

I'm using **[Model View Presenter](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93presenter)** architectural pattern which improve the **separation of concerns in presentation logic** and make **unit testing more easy** (ex: if we want to unit test our presenter we just need to **mock ( [Mockito](http://site.mockito.org/), [Roboelectric](http://robolectric.org/), ...)** our model and view, presenter for unit test model and the same thing for view). I'm looking for MVP because i honestly want to write **[clean](https://www.amazon.com/Clean-Code-Handbook-Software-Craftsmanship/dp/0132350882), maintainable and testable code**. I currently have not unit tested it yet!!!


**[Dagger 2](https://github.com/google/dagger)** as a **dependency framework** that help us provides dependencies to our class. A well-known about Dagger 2 is it falcilitate **reuse** the class and to be able to **test** them independent of other classes.

**[Loader API](https://developer.android.com/guide/components/loaders.html)** helps **loading data asynchronously**, as android developer site show that "If you fetch the data from another thread, perhaps with **[AsyncTask](https://developer.android.com/reference/android/os/AsyncTask.html)**, then you're responsible for **managing both the thread and the UI thread** through various **activity or fragment lifecycle events**, such as onDestroy() and **configurations changes**" and easily falling down with memory leaks.

**[Retrofit](http://square.github.io/retrofit/)** make it easier to communicate with **RESTful** web services. 

**[Firebase Job Dispatcher](https://github.com/firebase/firebase-jobdispatcher-android#user-content-firebase-jobdispatcher-)** scheduling API that I use to optimize **network request frequencies** instead of **polling** the server for new information continuously. Another way that i can apply for the same thing but more **flexible and efficient** is using **[Firebase Cloud Messaging](https://developers.google.com/cloud-messaging/)** (FCM new version of **Google Cloud Messaging** GCM). Server can **push notification** to client in this case is my **NewsApp** to inform that there are some new data on the server, therefore in this way i ensure that i could send request to server to request for new data. Now, I don't need my service send requests to server to ask for new data at specific frequencies.

**[Picasso](http://square.github.io/picasso/)** library for loading and caching image.

**[Overdraw](https://developer.android.com/studio/profile/dev-options-overdraw.html)** avoid overdraw could help your app perf.

**[LeakCanary](https://github.com/square/leakcanary)** a handy tool detecting memory leaks.

**[DiffUtil](https://developer.android.com/reference/android/support/v7/util/DiffUtil.html)** improves updating content of recyclerview instead of updating whole content, by calculating the diffrence between old contents and new contents then the library dispatches changes to recyclerview.

Improving user experience by postponing loading of content when swipping between pages or switching tabs.

Because my friend doesn't update RESTful API currently, so i have to use current RESTful API design to make the app.

- **Next goals**

|               |   Activity    |    
| ------------- |:-------------:|
|1| Place Holder UI |
|2| Unit and Integration testing |
|3| Apply more about material design |
|4| Implement bookmark feature |
|5| Think about caching data in json format in file system approach instead of caching in database |




Screens
-----

- **Small Screen**

<img src="https://github.com/PeaceOfHeaven/NewsApp/blob/master/screenshots/Screenshot_1498816338.png" width="318" height="560" ><img src="https://github.com/PeaceOfHeaven/NewsApp/blob/master/screenshots/Screenshot_1498816611.png" width="318" height="560" >


- **Tablet**
<img src="https://github.com/PeaceOfHeaven/NewsApp/blob/master/screenshots/demo.gif" width="318" height="560" >

