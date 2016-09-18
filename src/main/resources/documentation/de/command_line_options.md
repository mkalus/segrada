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
* `orientDB.remote_root` Superuser von OrientDB Server (Voreinstellung: null)
* `orientDB.remote_pw` Superuser-Passwort von OrientDB Server (Voreinstellung: null)
* `solr.server` Falls angegeben, verwende Solr-Server statt lokalem Lucene (Voreinstellung: leer)
* `solr.field_*` Definitionen, welche Daten auf welche Felder in Solr abgebildet werden (Voreinstellungen: sollten mit der Standardeinstellung funktionieren)
* `lucene.analyzer` Verwendeter Lucene-Analyser (Voreinstellung: org.segrada.search.lucene.LuceneSegradaAnalyzer)
* `requireLogin` Login notwendig? (Voreinstellung: false = Autologin als Administrator)
* `allowAnonymous` Anonymen Login erlauben, falls requireLogin wahr ist (Voreinstellung: false)
* `uploads.storage` Art des Zwischenspeichers für hochgeladene Dateien, MEMORY oder FILE (Voreinstellung: MEMORY)
* `uploads.maximum_upload_size` Maximum upload size (Voreinstellung: 52428800 aka 50 MB)
* `map.engine` Verwendeter Kartenserver (Voreinstellung: ol für OpenLayers, keine weiteren Optionen im Moment)
* `binaryDataService` Verwendeter Dateidienst, für Hadoop org.segrada.service.binarydata.BinaryDataServiceHadoop setzen (Voreinstellung: org.segrada.service.binarydata.BinaryDataServiceFile)
* `binaryDataService.hadoop.configurationFiles` Hadoop Optionale Konfigurationsdateien (Komma-getrennt, Vorsteinstellung: leer)
* `binaryDataService.hadoop.fs.defaultFS` Hadoop Server-URI (Voreinstellung: hdfs://localhost:9000/)
* `binaryDataService.hadoop.path` Pfad, unter welchem die Daten von Segrada gespeichert werden (Vorsteinstellung: /segrada/)
* `binaryDataService.hadoop.userName` Benutzername zum Anmelden, wie HADOOP_USER_NAME (Voreinstellung: leer)

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
* `SEGRADA_ORIENTDB_REMOTE_ROOT` -> `orientDB.remote_root`
* `SEGRADA_ORIENTDB_REMOTE_PASSWORD` -> `orientDB.remote_pw`
* `SEGRADA_SOLR_SERVER` -> `solr.server`
* `SEGRADA_LUCENE_ANALYZER` -> `lucene.analyzer`
* `SEGRADA_REQUIRE_LOGIN` -> `requireLogin`
* `SEGRADA_ALLOW_ANONYMOUS` -> `allowAnonymous`
* `SEGRADA_UPLOADS_STORAGE` -> `uploads.storage`
* `SEGRADA_UPLOADS_MAX_SIZE` -> `uploads.maximum_upload_size`
* `SEGRADA_SOLR_FIELD_ID` -> `solr.field_id`
* `SEGRADA_SOLR_FIELD_CLASS_NAME` -> `solr.field_className`
* `SEGRADA_SOLR_FIELD_TITLE` -> `solr.field_title`
* `SEGRADA_SOLR_FIELD_CONTENT` -> `solr.field_content`
* `SEGRADA_SOLR_FIELD_TAG` -> `solr.field_tag`
* `SEGRADA_SOLR_FIELD_COLOR` -> `solr.field_color`
* `SEGRADA_SOLR_FIELD_ICON` -> `solr.field_icon`
* `SEGRADA_MAP_ENGINE` -> `map.engine`
* `SEGRADA_BINARY_DATA_SERVICE` -> `binaryDataService`
* `SEGRADA_HADOOP_CONFIGURATION_FILES` -> `binaryDataService.hadoop.configurationFiles`
* `SEGRADA_HADOOP_FS_DEFAULT_FS` -> `binaryDataService.hadoop.fs.defaultFS`
* `SEGRADA_HADOOP_PATH` -> `binaryDataService.hadoop.path`
* `HADOOP_USER_NAME` -> `binaryDataService.hadoop.userName`