# Segrada starten

[Erste Schritte >>](tutorial01.md)

## Den Starter benutzer

Der Starter is eine einfache Möglichkeit, Segrada auf dem lokalen Rechner laufen zu lassen.

![Segrada-Starter](SegradaLauncher.png "Segrada-Starter")

### Windows

* Laden Sie Segrada als ZIP-Datei von der Seite [www.segrada.org](http://segrada.org/fileadmin/downloads/Segrada.zip)
  herunter.
* Entpacken Sie die ZIP-Datei in einen beliebigen Ordner Ihrer Wahl.
* Öffnen Sie den Windows Explorer in diesem Ordner und starten Sie Segrada.exe, um den Starter zu öffnen. Falls der
  Starter **nicht** starten sollte, prüfen Sie bitte Ihre Java-Version und/oder laden sie Java von
  [Oracle](https://www.java.com/) herunter.
* Das Starterfenster sollte nun in ähnlicher Weise wie oben zu sehen sein.
* _Optional:_ Wählen Sie einen Pfad, in dem eine existierende Datenbank bereits gespeichert ist. Der Standardordner
  ist `segrada_data` im selben Verzeichnis wie die Programmdateien von Segrada.
* Klicken Sie auf "Start" im Starter-Fenster, um eine Datenbank zu erstellen. Dies wird beim ersten Mal eine Weile
  dauern.
* Es kann sein, dass der Windowsfirewall eine Warnung ausgibt. Diese können Sie mit "Ok" bestätigen.
* Öffnen Sie ihren Browser unter [http://localhost:8080/](http://localhost:8080/).

### Mac

* Laden Sie Segrada als TGZ-Datei von der Seite [www.segrada.org](http://segrada.org/fileadmin/downloads/Segrada.tgz)
  herunter.
* Entpacken Sie die TGZ-Datei in einen beliebigen Ordner Ihrer Wahl.
* Starten Sie das Kommando `start_mac.command` in dem gerade entpackten Ordner aus. Falls der
  Starter **nicht** starten sollte, prüfen Sie bitte Ihre Java-Version und/oder laden sie Java von
  [Oracle](https://www.java.com/) herunter.
* Das Starterfenster sollte nun in ähnlicher Weise wie oben zu sehen sein.
* _Optional:_ Wählen Sie einen Pfad, in dem eine existierende Datenbank bereits gespeichert ist (oder verwenden Sie
  einen leeren Ordner, um dort eine neue Datenbank zu erstellen). Der Standardordner ist `segrada_data` im selben
  Verzeichnis wie die Programmdateien von Segrada.
* Klicken Sie auf "Start" im Starter-Fenster, um eine Datenbank zu erstellen. Dies wird beim ersten Mal eine Weile
  dauern.
* Öffnen Sie ihren Browser unter [http://localhost:8080/](http://localhost:8080/).

### Linux, Unix

* Laden Sie Segrada als TGZ-Datei von der Seite [www.segrada.org](http://segrada.org/fileadmin/downloads/Segrada.tgz)
  herunter.
* Entpacken Sie die TGZ-Datei in einen beliebigen Ordner Ihrer Wahl.
* Öffnen Sie die Kommandozeile und führen Sie den Befehl `./start.sh` in dem gerade entpackten Ordner aus. Falls der
  Starter **nicht** starten sollte, prüfen Sie bitte Ihre Java-Version. Aus irgendeinem obskuren Grund muss auf
  Apple-Rechner das JDK installiert werden, um Java auszuführen. Besuchen Sie dazu die
  [Java-Seite von Oracle](http://www.oracle.com/technetwork/java/javase/downloads/index.html) und wählen Sie ein
  Installationspaket für Ihre Platform (z.B. Java SE Development Kit 8u66 für Mac 64-Bit).
* Das Starterfenster sollte nun in ähnlicher Weise wie oben zu sehen sein.
* _Optional:_ Wählen Sie einen Pfad, in dem eine existierende Datenbank bereits gespeichert ist (oder verwenden Sie
  einen leeren Ordner, um dort eine neue Datenbank zu erstellen). Der Standardordner ist `segrada_data` im selben
  Verzeichnis wie die Programmdateien von Segrada.
* Klicken Sie auf "Start" im Starter-Fenster, um eine Datenbank zu erstellen. Dies wird beim ersten Mal eine Weile
  dauern.
* Öffnen Sie ihren Browser unter [http://localhost:8080/](http://localhost:8080/).



## Segrada als Server starten

Um Segrada als eigenständigen Server zu starten, kann man die headless-Option verwenden:

    java -jar segrada-1.0-SNAPSHOT.jar headless

Mehr Optionen unter [Kommandozeilenoptionen](command_line_options.md). Zudem können Sie
[Segrada automatisch starten lassen](autostart.md).


## Segrada in Docker starten

Sie können Segrada auch als Docker-Container laufen lassen. Holen Sie das Image und starten Sie es mit:

    docker pull ronix/segrada
    docker run --name segrada -p 8080:8080 ronix/segrada

Falls Sie Segrada in einer verteilten Umgebung testen wollen, können Sie auch OrientDb und Solr als Docker-Container
starten. Hier ein Beispielskript:

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
        -e "SEGRADA_ORIENTDB_PASSWORD=admin" --net="host" -p 8080:8080 ronix/segrada

Nun müssen Sie nur noch die Verteilung von hochgeladenen Dateien zwischen den einzelnen Knoten angehen. Sie können
dafür ein (verteiltes) Netzwerk-Dateisystem verwenden z.B. NFS, Gluster oder Ceph. Eine andere Option wäre die
Synchronisation der Dateien zwischen den Knoten. BitTorrentSync oder syncthing können hier beispielsweise verwendet
werden.


## Segrada im Servlet-Kontext

Sie können Segrada als WAR in einem Servlet-Container starten. Diese Funktion ist bislang nicht besonders intensiv
getestet. Erfahrungen und Feedback sind daher erwünscht. 

Kompilieren Sie Segrada mit folgender Änderung: Ändern Sie das `packaging` Tag in der Datei pom.xml nach WAR.

[Erste Schritte >>](tutorial01.md)
