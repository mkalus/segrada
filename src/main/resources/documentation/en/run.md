# Running Segrada

[First Steps >>](tutorial01.md)

## Using the Launcher

Segrada Launcher is a convenient way to start Segrada on your local machine.

![Segrada launcher](SegradaLauncher.png "Segrada launcher")

### Windows

* Download Segrada as ZIP file from [www.segrada.org](http://segrada.org/fileadmin/downloads/Segrada.zip).
* Unpack ZIP to a folder of your choice.
* Open Windows Explorer in that folder and click on Segrada.exe to run the launcher. If the launcher does **not** start,
  check your Java version and/or download it from [Oracle](https://www.java.com/).
* You should see the launcher window similar to the one above.
* _Optionally:_ Select a path with an existing database (or select an empty folder to initialize a database there).
  Default is the folder `segrada_data` in the folder where Segrada.exe and the other program files are located.
* In the launcher window, click on "Start" to create a database. This will take a while on the first time.
* Windows Firewall might complain. Click "Ok" to accept the changes.
* Open your browser and to to [http://localhost:8080/](http://localhost:8080/).

### Mac

* Download Segrada as TGZ file from [www.segrada.org](http://segrada.org/fileadmin/downloads/Segrada.tgz).
* Unpack TGZ to a folder of your choice.
* Run the `start_mac.command` in the folder you just extracted. If the launcher does **not** start,
  you have to install Java. For some braindead reason, Apple requires you to install the JDK if you want to run Java
  programs. Visit the [Java Download Site on Oracle](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
  and choose one of the JDKs (e.g. Java SE Development Kit 8u66 for Mac 64bit) for your platform.
* You should see the launcher window similar to the one above.
* _Optionally:_ Select a path with an existing database. Default is the folder `segrada_data` in the folder where
  Segrada.exe and the other program files are located. 
* In the launcher window, click on "Start" to create a database. This will take a while on the first time.
* Open your browser and to to [http://localhost:8080/](http://localhost:8080/).

### Linux, Unix

* Download Segrada as TGZ file from [www.segrada.org](http://segrada.org/fileadmin/downloads/Segrada.tgz).
* Unpack TGZ to a folder of your choice.
* Open the command line and run `./start.sh` in the folder you just extracted. If the launcher does **not** start,
  check your Java version. You can install OpenJDK 8, look on the
  [OpenJDK website](http://openjdk.java.net/install/index.html) for help.
* You should see the launcher window similar to the one above.
* _Optionally:_ Select a path with an existing database. Default is the folder `segrada_data` in the folder where
  Segrada.exe and the other program files are located. 
* In the launcher window, click on "Start" to create a database. This will take a while on the first time.
* Open your browser and to to [http://localhost:8080/](http://localhost:8080/).



## Starting Segrada as Standalone Server

In order to run Segrada as a server, start the server with the headless option:

    java -jar segrada-1.0-SNAPSHOT.jar headless

See [Command Line Options](command_line_options.md) for more options. You can also
[start Segrada automatically](autostart.md).


## Deploying Segrada as Servlet

You can deploy Segrada as WAR in a servlet container. This has not been tested thoroughly, yet, so help and feedback
is welcome.

Compile Segrada as WAR by changing the `packaging` Tag in pom.xml to WAR.

[First Steps >>](tutorial01.md)