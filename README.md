![](https://raw.githubusercontent.com/MeGysssTaa/keiko-plugin-inspector/master/img/Keiko%20Logo%20Big%20V2.png)


## About

Owning a big server? Can't use many public plugins just because you worry that they can harm your network? Want to monitor and/or restrict Internet activity, file access and other stuff for certain plugins? **That's all exactly what Keiko comes for.**

### [→ Keiko on SpigotMC](https://www.spigotmc.org/resources/keiko-server-security-and-plugins-inspections.66278/)


**Keiko is capable of:**

* inspecting bytecode of plugins at startup and check them for potentially unsafe operations or malware, including "Force-OP";

* monitoring and restricting nearly every single action plugins do, including network connectivity, file access, natives linkage, and more, with an easy to use and understand rules syntax;

* checking the integrity of installed plugins, ensuring they are not artificially modified or infected;

* ***[planned]*** detecting suspicious behavior of plugins at runtime.


## Installation

Go to **[releases page](https://github.com/MeGysssTaa/keiko-plugin-inspector/releases)** and download the latest Keiko release JAR, then put it in your server's `plugins/` folder and restart.

> **IMPORTANT:** if you're runnning a BungeeCord network, make sure to install Keiko **both** on the Bungee itself and on **all** its 'child' (Bukkit) servers. This will let Keiko protect not only all your endpoints, but also your Bungee (proxy)!


## Configuration, help and troubleshooting

* **[Check Keiko Wiki](https://github.com/MeGysssTaa/keiko-plugin-inspector/wiki)** for a general guide through Keiko, its configuration, and more.

* **[Use the Issue Tracker](https://github.com/MeGysssTaa/keiko-plugin-inspector/issues)** to report bugs, suggest features or get other sort of help.

* **[Join Keiko's Discord](https://discord.gg/QWHzCXX)** for other kind of assistance or just for live chat.


## Building (Gradle)

1. Install **[library kantanj](https://github.com/MeGysssTaa/kantanj)** in your local Maven repository.
2. Clone or download this repository, `cd` into it.
3. Run `./gradlew build`.
4. Wait a little bit.
5. Grab the built JAR file in `build/libs/`.
6. Profit!


```bash
echo "Cloning and installing kantanj..."
git clone https://github.com/MeGysssTaa/kantanj
cd kantanj
mvn clean install

cd ..

echo "Cloning and building keiko-plugin-inspector..."
git clone https://github.com/MeGysssTaa/keiko-plugin-inspector
cd keiko-plugin-inspector
./gradlew build

echo "Done! You can find the output JAR in build/libs/"
echo "Use './gradlew build' in its root folder (you are browsing it at the moment) whenever you want to rebuild Keiko."
```


## License

Keiko is completely free and open source. It is released under **[Apache License 2.0](https://github.com/MeGysssTaa/keiko-plugin-inspector/blob/master/LICENSE)**.
