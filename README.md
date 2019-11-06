# miaYeelight
Java desktop application to command a Yeelight Color bulb

As a precondition, the lamp has to be enabled for developer mode first so as it'll be possible to reach it via wifi.

The app can command one only bulb at once. It is set up for a preferred IP address of 192.168.1.100 (you can set it up from your modem), but if no lamp is there, is cycles from .2 and so on (cycling is a bit slow though). While the program is scanning for a lamp, you can specify an IP address where to go directly. You can disconnect to a lamp in any moment clicking the Yeelight icon in the left of the title bar. Then you can specify another IP address to connect to.

While connected the application periodically asks the lamp for its status in order to check if external applications have chanded it.

The lamp can be commanded in both CT (ColorTemperature) and HSB (HueSaturationBrightness) modes. Note that the Yeelight does not accept every possible RGB value (colors too dark are not accepted).

While you can configure the lamp to bright a single color, you can also set up a free flow of colors, by creating a new animation. An animation is defined by a list of tuples of: duration (the number of milliseconds that the step takes), hue, saturation, brightness (a the beginning of the step). You can add and remove freely the steps, but keep in mind that the lamp has some limitations in flow mode (which derive from the lamp itself, not from this app):
- in flow mode, HSB mode is not supported (the user's asked for HSB values for user friendliness, but then the values are translated in RGB mode). As a result more combinations of colors cannot be reproduced, especially colors too dark and not enough saturated
- the minimum time of a step is 50 milliseconds (which is quite fast)
- there's a limit in the number of steps you can make the lamp follow
In the end, you can also save (and of course load) the animation to a binary file you like. The space required is 16 Bytes per step. Default animations' files (inspired from the official app's) are provided.

You can set an auto-off timer to the lamp, which must be expressed in number of minutes.

The lamp can also be configured to set once, or keep track of the SystemColors.activeCaption color. If you're running Windows 10, you can gorgeously make the lamp follow your accent color (being updated by your desktop presentation if set up for doing so). In order to get this state of art, you gotta install a free app from the Microsoft Store called "Accent Applicator". I raccomend that app, even if you don't like this feature. Keep in mind that without that app, the lamp will always find the typical Windows light blue shade.
