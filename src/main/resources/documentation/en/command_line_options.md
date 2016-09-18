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
* `solr.field_*` Field settings to map certain types of search fields to specific Solr fields (defaults: should work out of the box)
* `lucene.analyzer` Lucene analyser to use (default: org.segrada.search.lucene.LuceneSegradaAnalyzer)
* `requireLogin` Require login? (default: false, meaning autologin as admin)
* `allowAnonymous` Allow anonymous login, if requireLogin is true (default: false)
* `uploads.storage` Type of storage used when uploading files, MEMORY or FILE (default: MEMORY)
* `uploads.maximum_upload_size` Maximum upload size (default: 52428800 aka 50 MB)
* `map.engine` Map engine to use (default: ol for OpenLayers, no other options yet)
* `binaryDataService` File service use, for Hadoop, set org.segrada.service.binarydata.BinaryDataServiceHadoop (default: org.segrada.service.binarydata.BinaryDataServiceFile)
* `binaryDataService.hadoop.configurationFiles` Hadoop optional configuration file (comma separated, default: empty)
* `binaryDataService.hadoop.fs.defaultFS` Hadoop server URI (default: hdfs://localhost:9000/)
* `binaryDataService.hadoop.path` Path under which Segrada files will be saved in Hadoop (default: /segrada/)
* `binaryDataService.hadoop.userName` Hadoop user name to set, same as environmental variable HADOOP_USER_NAME (default: empty)


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