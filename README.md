# miaYeelight
Java desktop application to command a Yeelight Color bulb, supports:

**English - Italiano - Español - Português - Français - Bresà**

(language is inferred automatically, if not provided as calling parameter lang:val, val between it, en, es, pt, fr)

As a precondition, the lamp has to be enabled for developer mode first so as it'll be possible to reach it via Wi-Fi.

The app can command one only bulb at once. The app implements Yeelight's protocol for **multicast discovery**: thanks to this it should find any lamp automatically. This is not 100% reliable, so the app is also able to start a normal polling cycle over 192.168.1.xxx addresses, or to go for one specified by you. Once connected, you can disconnect in any moment clicking the Yeelight icon in the left of the title bar. Then you can specify another IP address to connect to, or trigger again auto-discovery.

While connected the application periodically asks the lamp for its status in order to check if external applications have changed it.

The lamp can be commanded in both CT (ColorTemperature) and HSB (HueSaturationBrightness) modes. Note that the Yeelight does not accept every possible RGB value (colors too dark are not accepted).

While you can configure the lamp to bright a single color, you can also set up a free flow of colors, by creating a new **animation**. An animation is defined by a list of tuples of: duration (the number of milliseconds that the step takes), hue, saturation, brightness (at the beginning of the step). You can add and remove freely the steps, but keep in mind that the lamp has some limitations in flow mode (which derive from the lamp itself, not from this app):
- in flow mode, HSB mode is not supported (the user's asked for HSB values for user-friendliness, but then the values are converted in RGB). As a result more combinations of colors cannot be reproduced, especially colors too dark and not enough saturated;
- the minimum time of a step is 50 milliseconds (which is quite fast);
- there's a limit in the number of steps you can make the lamp follow.

In the end, you can also save (and of course load) the animation to a binary file you like. Default animations' files (inspired from the official app's) are provided.

You can set an **auto-off** timer to the lamp, which must be expressed in number of minutes.

You can even make the lamp **follow the most prominent color on your screen**: perfect for creating an involving atmosphere in your room while you're enjoying some content you like.

The lamp can also be configured to set once, or keep track of the SystemColors.activeCaption color. If you're running Windows 10, you can make the lamp follow your accent color (being updated by your desktop presentation if set up for doing so). In order to get this feature working, you have to install a free app from the Microsoft Store called "Accent Applicator". Keep in mind that without that app, the lamp will always find the typical Windows light blue shade.


NOTE: The app is pre-packaged in an executable JAR, compiled for Java 17.