# Segrada Kommandozeilenoptionen

## Kommandozeilenoptionen für die jar-Datei (nach dem JAR)

Beispiel:

    java -jar segrada-1.0-SNAPSHOT.jar headless

* `headless` Grafikloser Modus, starte den Server sofort ohne Launcher.


## Kommandozeilenoptionen über Java Properties (vor dem JAR mit Parameter -D)

Beispiel:

    java -jar -DsavePath="/home/myhome/devel/Segrada/segrada_data" \
      -DorientDB.url="plocal:/home/myhome/Segrada/Java/segrada_data/db" segrada-1.0-SNAPSHOT.jar

* `server.port` Server-Port des eingebetteten Jetty-Servers (Voreinstellung: 8080)
* `server.context` Server-Context (Voreinstellung: "/")
* `savePath` Speicherpfad für Segrada-Daten (Voreinstellung: ./segrada_data)
* `orientDB.url` URL für zugrundeliegende OrientDB (Voreinstellung: plocal:./segrada_data/db)
* `orientDB.login` OrientDB-Datenbank Login (Voreinstellung: admin)
* `orientDB.password` OrientDB-Datenbank Passwort (Voreinstellung: admin)
* `solr.server` Falls angegeben, verwende Solr-Server statt lokalem Lucene (Voreinstellung: leer)
* `lucene.analyzer` Verwendeter Lucene-Analyser (Voreinstellung: org.apache.lucene.analysis.standard.StandardAnalyzer)
* `requireLogin` Login notwendig? (Voreinstellung: false = Autologin als Administrator)
* `uploads.storage` Art des Zwischenspeichers für hochgeladene Dateien, MEMORY oder FILE (Voreinstellung: MEMORY)
* `uploads.maximum_upload_size` Maximum upload size (Voreinstellung: 52428800 aka 50 MB)

## Umgebungsvariablen

Sie können die Optionen auch über Umgebungsvariablen des Systems definieren. In Windows können diese über den Befehl
`set` definiert werden, in Linux und Mac OS kann man `export` verwenden oder die Variablen dem auszuführenden Befehl
voranstellen. Bitte beachten Sie, dass das Setzen von Kommandozeilenoptionen Umgebungsvariablen überschreiben kann.

Beispiel Windows:

    set SEGRADA_SAVE_PATH=C:\path\to\segrada
    set SEGRADA_ORIENTDB_URL=plocal:C:\path\to\segrada\db
    java -jar segrada-1.0-SNAPSHOT.jar

Beispiel Linux/OS X:

    SEGRADA_SAVE_PATH=/home/myhome/devel/Segrada/segrada_data \
    SEGRADA_ORIENTDB_URL=plocal:/home/myhome/Segrada/Java/segrada_data/db \
    java -jar segrada-1.0-SNAPSHOT.jar

Umgebungsvariablen und ihre Äquivalente von oben:

* `SEGRADA_SERVER_PORT` -> `server.port`
* `SEGRADA_SERVER_CONTEXT` -> `server.context`
* `SEGRADA_SAVE_PATH` -> `savePath`
* `SEGRADA_ORIENTDB_URL` -> `orientDB.url`
* `SEGRADA_ORIENTDB_LOGIN` -> `orientDB.login`
* `SEGRADA_ORIENTDB_PASSWORD` -> `orientDB.password`
* `SEGRADA_SOLR_SERVER` -> `solr.server`
* `SEGRADA_LUCENE_ANALYZER` -> `lucene.analyzer`
* `SEGRADA_REQUIRE_LOGIN` -> `requireLogin`
* `SEGRADA_UPLOADS_STORAGE` -> `uploads.storage`
* `SEGRADA_UPLOADS_MAX_SIZE` -> `uploads.maximum_upload_size`