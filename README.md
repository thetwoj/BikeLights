# BikeLights
A Teensy 3.1 based in-wheel bike lights setup that allows images and text to be displayed while riding

### Demo videos

[Without Android app or SD card](https://www.youtube.com/watch?v=aE5S9sO_OZM)

[With Anroid app and on-board SD card](https://www.youtube.com/watch?v=Cu0mKToSdps)

### Structure

There are three components of this project, two of which are optional:

* Teensy 3.1 in-wheel circuitry and Arduino code (bikelights.ino) - **required**
* Companion Android app that allows selection of images from the SD card via bluetooth - **optional**
* Python script that takes an input image and outputs a file of RGB values to be loaded on SD card - **optional**

### Dependencies

#### bikelights.ino
* [FastLED](https://github.com/FastLED/FastLED)
* [Teensyduino](https://www.pjrc.com/teensy/teensyduino.html)

#### Pixel printer.py
* [Python](https://www.python.org/downloads/windows/)

### Circuit diagrams

No SD card or BT module
![No SD card or BT module](http://i.imgur.com/D2f2Zo0.png)

SD card and BT module
![SD card and BT module](http://i.imgur.com/fEVoohg.png)
