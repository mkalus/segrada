# Segrada Command Line Options

## Command line options for the jar (added after the JAR)

Example:

    java -jar segrada-1.0-SNAPSHOT.jar headless

* `headless` Run in headless mode, starting the server right away.


## Command line parameters via Java properties (added before the JAR with -D parameter)

Example:

    java -jar -DsavePath="/home/myhome/devel/Segrada/segrada_data" \
      -DorientDB.url="plocal:/home/myhome/Segrada/Java/segrada_data/db" segrada-1.0-SNAPSHOT.jar

* `server.port` Port of embedded Jetty server (default: 8080)
* `server.context` Server context (default: "/")
* `savePath` Save path for the data of Segrada (default: ./segrada_data)
* `orientDB.url` OrientDB database URL (default: plocal:./segrada_data/db)
* `orientDB.login` OrientDB database login (default: admin)
* `orientDB.password` OrientDB database password (default: admin)
* `lucene.analyzer` Lucene analyser to use (default: org.apache.lucene.analysis.standard.StandardAnalyzer)
