# Benutzer und Gruppen

Die Optionen sind im Moment noch im Aufbau. Geplant sind:

* Benutzer mit einer Benutzergruppe
* Gruppen mit bestimmten Rechten


## Mehrbenutzerbetrieb starten

Standardmäßig loggt sich der zugreifende Benutzer automatisch als Administrator ein. Um Logins und die
Benutzerverwaltung zu starten, muss man Segrada mit folgender Kommandozeilenoption starten:

    java -jar -DrequireLogin="true" segrada-1.0-SNAPSHOT.jar

oder im Servermodus:

    java -jar -DrequireLogin="true" segrada-1.0-SNAPSHOT.jar headless

Nach dem Start muss man sich anmelden. Es ist sinnvoll den Benutzer "Administrator" entsprechend zu ändern.
