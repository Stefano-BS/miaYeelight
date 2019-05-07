# miaYeelight
Java desktop application to command a single Yeelight Color bulb

Obviously, it needs the lamp to be enabled for developer mode first so as it'll be possible to reach it via wifi.

The app can command one only bulb at once (and it's not possible to choose one, it selects automatically the first found). It is set up for a preferred IP address of 192.168.1.100 (you can set it up from your modem), but if no lamp is there, is cycles from .2 and so on. Cycling is a bit slow though.

The lamp can be commanded in both CT (ColorTemperature) and HSB (HueSaturationBrightness) modes. Note that the Yeelight does not accepts every possible RGB value. Brightness level is supported, flows are not instead.

The lamp can also be configured to set once, or keep track of the SystemColors.activeCaption color. If you're running Windows 10, you can gorgeousy make the lamp follow your accent color (being updated by your desktop presentation if set up for doing so). In order to get this state of art, you gotta install a free app from the Microsoft Store called "Accent Applicator". I raccomend that app, even if you don't like this feature. Keep in mind that without that app, the lamp will always find the typical Windows light blue shade.
