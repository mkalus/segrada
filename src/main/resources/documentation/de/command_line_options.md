# Segrada Kommandozeilenoptionen

## Kommandozeilenoptionen für die jar-Datei (nach dem JAR)

Beispiel:

    java -jar segrada-1.0-SNAPSHOT.jar headless

* `headless` Grafikloser Modus, starte den Server sofort ohne Launcher.


## Kommandozeilenoptionen über Java Properties (vor dem JAR mit Parameter -D)

Beispiel:

    java -jar -DsavePath="/home/myhome/devel/Segrada/segrada_data" -DorientDB.url="plocal:/home/myhome/Segrada/Java/segrada_data/db" segrada-1.0-SNAPSHOT.jar

* `server.port` Server-Port des eingebetteten Jetty-Servers (Voreinstellung: 8080)
* `server.context` Server-Context (Voreinstellung: "/")
* `savePath` Speicherpfad für Segrada-Daten (Voreinstellung: ./segrada_data)
* `orientDB.url` URL für zugrundeliegende OrientDB (Voreinstellung: plocal:./segrada_data/db)
* `orientDB.login` OrientDB-Datenbank Login (Voreinstellung: admin)
* `orientDB.password` OrientDB-Datenbank Passwort (Voreinstellung: admin)
* `lucene.analyzer` Verwendeter Lucene-Analyser (Voreinstellung: org.apache.lucene.analysis.standard.StandardAnalyzer)
