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


## Running Segrada in Docker

You can also launch Segrada in Docker. Pull and rund with:

    docker pull ronix/segrada
    docker run --name segrada -p 8080:8080 ronix/segrada

If you want to test Segrada in a distributed environment, you can run OrientDb and Solr on Docker, too. Do the following:

    # get empty database and prepare Orient directory to use below
    mkdir mysegrada_test
    cd mysegrada_test
    wget http://segrada.org/fileadmin/downloads/SegradaEmptyDB.tar.gz
    tar xzf SegradaEmptyDB.tar.gz
    rm SegradaEmptyDB.tar.gz
    mkdir orientdbs
    mv segrada_data/db/ orientdbs/Segrada
    # this is for testing only - should be changed in production:
    chmod 777 segrada_data
    
    # pull and run OrientDB
    docker pull orientdb/orientdb
    docker run -d -e "ORIENTDB_ROOT_PASSWORD=12345" -v "$(pwd)/databases:/orientdb/databases" -p 2424:2424 \
        -p 2480:2480 orientdb/orientdb
    
    # pull and run Solr
    docker pull solr
    docker run --name my_solr -d -p 8983:8983 -t solr
    # create segrada core (Segrada will use default schema of Solr)
    sudo docker exec -it --user=solr my_solr bin/solr create_core -c segrada
    
    # pull and run
    docker pull ronix/segrada
    docker run -d -e "SEGRADA_ORIENTDB_LOGIN=admin" -e "SEGRADA_ORIENTDB_URL=remote:localhost/Segrada" \
        -v "$(pwd)/segrada_data:/usr/local/segrada/segrada_data" \
        -e "SEGRADA_ORIENTDB_PASSWORD=admin" \
        -e "SEGRADA_SOLR_SERVER=http://localhost:8983/solr/segrada" \
        --net="host" -p 8080:8080 ronix/segrada

Now you only have to cope with the issue of distributing uploading files. You can use a network and/or distributed file
system for this, like NFS, Gluster, or Ceph. Another option would be to synchronize files between nodes. BitTorrentSync
or syncthing would be choices here.

You can also user `docker-compose up` in the folder `deploy/compose` to create and start a Segrada cluster.


## Deploying Segrada as Servlet

You can deploy Segrada as WAR in a servlet container. This has not been tested thoroughly, yet, so help and feedback
is welcome.

Compile Segrada as WAR by changing the `packaging` Tag in pom.xml to WAR.

[First Steps >>](tutorial01.md)