iphone6-reserve-bot
===================
An Android app to automatically submit request to reserve iPhone 6 in Apple Store Hong Kong.
Detailed tutorial is available at [my blog](http://blog.30sparks.com/iphone-reserve-bot-tutorial/?utm_source=github&utm_medium=web&utm_campaign=ibot) (Chinese).

The app will:

* Retrieve the captcha for user to input
* Retrieve the SMS code and send out SMS
* Auto-fill in the reservation code after receiving the SMS reply
* Auto-submit the reservation order

To run, please download the following librarys to `libs/` folder:

* [Okhttp](http://square.github.io/okhttp)
* [Glide](https://github.com/bumptech/glide), together with okHttp integration library
* [Android Support Library v4](http://developer.android.com/tools/support-library/index.html) (required by `Glide`)
