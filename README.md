![](https://raw.githubusercontent.com/MeGysssTaa/keiko-plugin-inspector/master/img/Keiko%20Logo%20Big%20V2.png)


## About

Are you scared to use many public plugins just because they can harm your server? Want to monitor and/or restrict Internet activity, file access and other stuff for certain plugins? **That's all exactly what Keiko comes for.**

### [→ Keiko on SpigotMC](https://www.spigotmc.org/resources/66278/)


**Keiko is capable of:**

* inspecting bytecode of plugins at startup and check them for potentially unsafe operations or malware, including "Force-OP";

* monitoring and restricting nearly every single action plugins do, including network connectivity, file access, natives linkage, and more, with an easy to use and understand rules syntax;

* checking the integrity of installed plugins, ensuring they are not artificially modified or infected;

* detecting suspicious behavior of plugins at run-time, and sometimes even remediating the damage they've done.


## Installation

**Keiko is not a plugin!** Please visit the [Installation Instructions](https://github.com/MeGysssTaa/keiko-plugin-inspector/wiki/Installation-Instructions) page for a complete guide on getting started with Keiko.


## Configuration, help and troubleshooting

* **[Check Keiko Wiki](https://github.com/MeGysssTaa/keiko-plugin-inspector/wiki)** for a general guide through Keiko, its configuration, and more.

* **[Use the Issue Tracker](https://github.com/MeGysssTaa/keiko-plugin-inspector/issues)** to report bugs, suggest features or get other sort of help.

* **[Join Keiko's Discord](https://discord.gg/QWHzCXX)** for other kind of assistance or just for live chat and discussion.


## Building (Gradle)

To build Keiko yourself from source:

1. Clone or download this repository, `cd` into it.
2. Run `./gradlew shadowJar`.
3. Wait a little bit.
4. Grab the built JAR file in `build/libs/`.
5. Profit!

Note: Keiko is built against **Java 8**, but, as it works perfrectly with **Java 16 *runtime***, in theory, it should be possible to build Keiko against newer Java versions as well.


```bash
echo "Cloning Keiko..."
git clone https://github.com/MeGysssTaa/keiko-plugin-inspector
cd keiko-plugin-inspector
echo "Building Keiko..."
./gradlew shadowJar

echo "Done! You can find the output JAR in build/libs/"
echo "Use './gradlew shadowJar' in Keiko's root folder (you are browsing it at the moment) whenever you want to rebuild Keiko from source."
```


## License

**[GNU GPLv3](https://github.com/MeGysssTaa/keiko-plugin-inspector/blob/master/LICENSE)**
