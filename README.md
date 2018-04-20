# Segrada - Semantic Graph Database

Segrada is a semantic graph database for research and documentation.

Features:

* Create entities and describe them. Add tags, colors and icons to your entities.
* Connect entities sematically. Define relation types, tag relations and types.
* Create a tag ontology by creating a tag hierarchy.
* Add source entries and add references to entities and relations.
* Upload files and connect them with everything.
* Full text search of database and files
* Graph view of connections
* PDF-Preview of uploaded pdf files
* Nice full screen image viewer enabling you to zoom and rotate images
* Powerful filtering and search features
* Multiple users possible
* Scalable: Use on desktop, run as single or distributed server.
* Internationalized: Currently English and German

**Note:** Application is still beta and subject to enhancements and changes. The application is capable to update older
databases to newer versions, if needed. Still, there is a certain risk and the author does not take any responsibility
for lost and/or mangled data.


## Documentation

[Documentation on GitHub](https://github.com/mkalus/segrada/blob/master/src/main/resources/documentation/index.md)


## Translation

[Translation project on Transifex](https://www.transifex.com/auxnet/segrada/dashboard/)

We are looking for translators. If you want to support Segrada, feel free to contact us.


## Running Segrada

Download from: [www.segrada.org](http://www.segrada.org/ "Segrada Homepage")

In order to try out Segrada, download the program and unpack it to a folder of your choice.  
On Windows, start the application with Segrada.exe. On Linux, Unix, and Apple Mac OS, run start.sh (you might have to make this file executable).  
A window should appear which can start the actual application. Press "Start" and wait for the button "Open Application" to become active.  
The application runs in your browser. You can stop the application by clicking "Stop" in the control window or by closing it.

You can also run Segrada using Docker:

```bash
docker run --name segrada -p 8080:8080 ronix/segrada
```

See https://hub.docker.com/r/ronix/segrada/ for more information.

See deploy folder for more examples, e.g. running Segrada using Docker compose and on Kubernetes.