# AndroidApp-WebRemoteVolumeControl
Adjust sound volume of your Android device from any device that has a web browser, Android or not.  
Therefore, this also includes non-Android devices such as Apple or Microsoft devices, maybe televisions or even maybe watches if these things have web browsers.  

Both devices must be connected to the same local network (Wifi).  

This Android application is (and will remain) free, without ads and open source.  

You can find on Google Play Store here :  
[Web Remote Volume Control](https://play.google.com/store/apps/details?id=com.tanaka42.webremotevolumecontrol)


How to use it :
===============

1. First, start this application on the Android device of which you want to control sound volume remotely,  
   it will display the internet address (URL, example : http://192.168.1.35:9000/) you will have to connect to in order to control sound volume remotely.
2. Then, on any other device connected on the same local network (Wifi) as your Android device, open a web browser (Chrome, Safari, Firefox, whatever, any web browser should work), and navigate to above address.
3. Finally, on the page that appears, press buttons to remotely adjust your Android device's sound volume.

![Captures d'écran de l'application](https://raw.githubusercontent.com/tanaka42/androidapp-webremotevolumecontrol/master/google-play-store-images-20.29.1/image_1024_500_en.png "Captures d'écran de l'application")

How it works :
==============

On your Android device, this app will start a lightweight minimalistic and app-specific web server, as a foreground service.  
This web server will listen on port 9000 and serve a static html page (single page application).  
That page will display only two buttons, Raise Volume and Lower Volume, which when clicked will asynchronously tell the web server / Android device to adjust main sound volume.

The web server isn't really one : it does not list directories or serve any requested file from filesystem.  
It only responds to a few commands (URLs below) (see the switch case in HttpServer.java, subclass ClientThread, method Run) :  
* / : serves the web page
* /volume-up.png : serves the volume-up.png image included in the web page
* /volume-down.png : serves the volume-up.png image included in the web page
* /volume-up : raises volume
* /volume-down : lowers volume
* any other URL will respond with a 404.

Also it will listen only on local IP addresses : when it determines the IP address to listen on, it aborts if obtained IP address is not a Class C IP Adress (first member between 192 and 223 inclusive).

Various information :
=====================

The base HTTP server code comes from Sonu Auti which I hereby thank : https://github.com/sonuauti/Android-Web-Server/  
From his project, I have kept only the small part I needed : if you are here to see an implentation of an http server, then be sure to look at his work instead of mine.

I did not include a Mute button or a Slider input at the moment because to do so Android API seems to require me to point at a specific audio stream : I supposed this would not work in some situations, or make things complicated, so I gave up for now.

This is my very first GitHub experience, and my very first Android experience too, any advice or remark is welcome.

I did build that app because I often use an Android tablet, connected to an audio sound system (HiFi, an old one that does not have a remote controller), to play music, and I wanted to be able to adjust sound remotely from an iPhone, in a simple way (I mean not by using a Teamviewer/Vysor like thing).
