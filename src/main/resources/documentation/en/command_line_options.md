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
* `orientDB.remote_root` OrientDB remote database superuser (default: null)
* `orientDB.remote_pw` OrientDB remote database superuser password (default: null)
* `solr.server` If specified use remote Solr server to index search instead of local Lucene (default: empty)
* `lucene.analyzer` Lucene analyser to use (default: org.apache.lucene.analysis.standard.StandardAnalyzer)
* `requireLogin` Require login? (default: false, meaning autologin as admin)
* `allowAnonymous` Allow anonymous login, if requireLogin is true (default: false)
* `uploads.storage` Type of storage used when uploading files, MEMORY or FILE (default: MEMORY)
* `uploads.maximum_upload_size` Maximum upload size (default: 52428800 aka 50 MB)

## Environmental variables

You can also set options via environmental variables. On Windows this can be done using `set` command, on Linux and
Mac OS you can do this using `export` or by adding the variables before the command. Please note that command line
options above override environmental variables.

Windows example:

    set SEGRADA_SAVE_PATH=C:\path\to\segrada
    set SEGRADA_ORIENTDB_URL=plocal:C:\path\to\segrada\db
    java -jar segrada-1.0-SNAPSHOT.jar

Linux/OS X example:

    SEGRADA_SAVE_PATH=/home/myhome/devel/Segrada/segrada_data \
    SEGRADA_ORIENTDB_URL=plocal:/home/myhome/Segrada/Java/segrada_data/db \
    java -jar segrada-1.0-SNAPSHOT.jar

Mappings to command line parameters above:

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