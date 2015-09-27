# Running Segrada

## Using the Launcher

Segrada Launcher is a convenient way to start Segrada on your local machine.

![Segrada launcher](SegradaLauncher.png "Segrada launcher")

### Windows

* Download Segrada as ZIP file from [www.segrada.org](http://segrada.org/fileadmin/downloads/Segrada.zip).
* Unpack ZIP to a folder of your choice.
* Open Windows Explorer in that folder and click on Segrada.exe to run the launcher. If the launcher does **not** start,
  check your Java version and/or download it from [Oracle](https://www.java.com/).
* You should see the launcher window similar to the one above.
* _Optionally:_ Select a path with an existing database. Default is the folder `segrada_data` in the folder where
  Segrada.exe and the other program files are located. 
* In the launcher window, click on "Start" to create a database. This will take a while on the first time.
* Windows Firewall might complain. Click "Ok" to accept the changes.
* Open your browser and to to [http://localhost:8080/](http://localhost:8080/).

### Mac, Linux, Unix

* Download Segrada as TGZ file from [www.segrada.org](http://segrada.org/fileadmin/downloads/Segrada.tgz).
* Unpack TGZ to a folder of your choice.
* Open the command line and run `./start.sh` in the folder you just extracted. If the launcher does **not** start,
  check your Java version and/or download it from [Oracle](https://www.java.com/).
* You should see the launcher window similar to the one above.
* _Optionally:_ Select a path with an existing database. Default is the folder `segrada_data` in the folder where
  Segrada.exe and the other program files are located. 
* In the launcher window, click on "Start" to create a database. This will take a while on the first time.
* Open your browser and to to [http://localhost:8080/](http://localhost:8080/).

Planned: Simpler Mac launcher.