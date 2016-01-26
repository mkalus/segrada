## Datenback mit OrientDb direkt verändern

Sie können Ihre Daten direkt mit OrientDb verändern. Dies können Sie machen, falls etwas an den Daten kaputt sein
sollte oder wenn die reine Neugier Sie dazu treibt. Führen Sie dazu folgende Schritte aus:

* Öffnen Sie die Seite [http://orientdb.com/download/](http://orientdb.com/download/) und laden Sie die
  Community-Version für Ihre Plattform herunter.
* Extrahieren Sie das ZIP-Archiv in einen Ordner. Sie sollten einen Unterordner namens "databases" sehen (dieser
  enthält eine Datenbank mit der Bezeichnung "GratefulDeadConcerts").
* In Ihrer Segrada-Datenbank (`segrada_data`) gibt es einen Ordner "db". Kopieren Sie diesen Ordner in den gerade
  genannten "databases"-Ordner.
* Führen Sie den OrientDb-Server (mit `server.sh` oder `server.bat`) und öffnen Sie die Seite
  [http://localhost:2480/](http://localhost:2480/).
* Loggen Sie sich in die Datenbank "db" ein. Login/Passwort sind admin/admin. 
* Sie können nun direkt auf die Daten zugreifen - OrientDb verwendet eine SQL-ähnliche Syntax. 

Nach Veränderungen müssen Sie den "db"-Ordner wieder in Ihren `segrada_data`-Ordner zurück kopieren.