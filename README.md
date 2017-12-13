# WEEGI
This project allows the user to interface with the Cyton from their Android device over wifi.

**Note** This project requires the wifi shield.

# Setup
To get started using the app you first need to flash the Cyton firmware. Please refer to the Open BCI [documentation](http://docs.openbci.com/Hardware/05-Cyton_Board_Programming_Tutorial) for how to flash firmware to the Cyton. The file that will be used to flash the firmware is [here](https://github.com/DavidMCarek/WEEGI/tree/master/Firmware).

The firmware changes adds a status command that is used by sending 'n'. This returns if the Cyton is streaming or recording. There is also an addition that allows recording to the microSD card to be triggered by pressing the program button on the Cyton (Its a little tricky to press with the wifi shield on).

Next, setup the wifi shield as per the Open BCI [documentation](http://docs.openbci.com/Tutorials/03-Wifi_Getting_Started_Guide) again. Once the Android device and wifi shield are setup on the same network, install the Android app. The app will scan for the board using SSDP and enter it into the dropdown once it is found. Once discovered the app will make requests every 3 seconds to the Cyton to check its status (if its recording or streaming). Now, you should be able to stop and stop recording/streaming from your phone.

# Contributions
We would love for this app to be improved. If you would like to make changes, make a pull request and we will look it over in a timely manner (hopefully).

# License
MIT
