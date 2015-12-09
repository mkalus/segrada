# Segrada automatisch starten

### Windows

Es gibt mehrere Möglichkeiten, Segrada automatisch zu starten. Der Start erfolgt im Hintergrund und Sie können die
Anwendung unter der Adresse [http://localhost:8080/](http://localhost:8080/) erreichen.

#### Starten über die Aufgabenplanung

1. Führen Sie die Windows Aufgabenplanung aus. Am einfachsten klicken Sie dazu auf "Start" und geben "aufgabenplanung"
   in der Textzeile ein.
1. Erstellen Sie eine neue Aufgabe (Menü "Aktion" -> "Aufgabe erstellen..."). Es erscheint ein Fenster mit mehreren
   Reitern/Tabs.
1. Im Reiter **Allgmein**: Geben Sie ihrer Aufgabe einen Namen (z.B. "Segrada"). Falls Sie die Anwendung als Server für
   viele Benutzer laufen lassen wollen, wählen Sie "Unabhängig von der Benutzeranmeldung ausführen".
1. Im Reiter **Trigger**: Klicken Sie auf "Neu..." und wählen "Beim Start" aus. Wenn Sie wollen, verzögern Sie den Start
   um ein paar Sekunden. Vergewissern Sie sich, dass "Aktiviert" angehakt ist. Klicken Sie "Ok".
1. Im Reiter **Aktionen**: Ebenfalls "Neu..." wählen und Aktion "Programm starten" wählen. Im Feld "Programm/Skript"
   wählen Sie die Datei "start_server.bat", die sich in Ihrem Segrada-Ordner befindet. Klicken Sie "Ok".
1. Im Reiter **Einstellungen**: Wählen Sie "Ausführung der Aufgabe bei Bedarf zulassen". Löschen Sie den Haken bei
   "Aufgabe beenden, falls sie länger ausgeführt wird als:". Behalten Sie "Keine neue Instanz starten" im Feld
   "Folgende Regel anwenden, falls die Aufgabe bereits ausgeführt wird:". Klicken Sie "Ok".
1. Klicken Sie auf den "Ok"-Knopf und geben Sie das Passwort des Benutzers ein falls nötig.

#### Start on Login

1. Verwenden Sie den Windows Explorer, um zum Segrada-Ordner zu navigieren.
1. Rechts-klicken Sie auf die Datei "start_server.bat". Wählen Sie "Verknüpfung erstellen". Rechts-klicken Sie danach
   auf den neu erstellten Link und wählen Sie "Ausschneiden".
1. Klicken Sie auf "Start" und wählen alle "Programme". Rechts-klicken Sie auf den Ordner "Autostart" und wählen Sie
   die Option "Öffnen"
1. Fügen Sie die Verknüpfung hier ein (Rechts-Klick im Ordner und "Einfügen" wählen oder STRG+V).

Beim nächsten Login wird Segrada dann automatisch ausgeführt.

### Mac

Wir bitten um Unterstützung/Hilfe bezüglich automatischer Mac-Starts.

### Linux, UNIX

#### Gnome, Ubuntu Desktops

1. Starten Sie das Programm "Startprogramme".
1. Auf "Hinzufügen" klicken. Wählen Sie als Programmname "Segrada". Im Feld Befehl schreiben Sie etwas in der Art
   `/path/to/segrada/start.sh headless`

#### Supervisord verwenden

Falls Sie supervisord verwenden, fügen Sie folgende Zeilen zu Ihrer Konfiguration hinzu (entweder /etc/supervisord.conf
oder eine eigene Datei unter /etc/supervisord.d/). Passen Sie die Daten an Ihre Bedürfnisse an.

    [program:segrada]
    command = /path/to/segrada/start.sh headless
    directory = /path/to/segrada/
    autorestart = True
    user = some_user

#### Upstart verwenden

Fügen Sie eine Datei `segrada.conf` zum Verzeichnis /etc/init/ hinzu. Diese enthält Daten wie folgt:

    description "Segrada"
    
    start on (local-filesystems and net-device-up IFACE!=lo)
    stop on runlevel [!2345]
    
    env STNORESTART=yes
    env HOME=/path/to/segrada/
    setuid "some_user"
    setgid "some_user"
    
    exec /path/to/segrada/start.sh headless
    
    respawn

#### systemd verwenden

Sie können Segrada als System-Dienst starten, indem Sie eine Datei `segrada.service` im Verzeichnis
/etc/systemd/system/ erstellen:

    [Unit]
    Description=Segrada - Semantic Graph Database
    Documentation=https://github.com/mkalus/segrada/blob/master/src/main/resources/documentation/index.md
    After=network.target
    
    [Service]
    User=some_user
    Environment=STNORESTART=yes
    ExecStart=/path/to/segrada/start.sh headless
    Restart=on-failure
    SuccessExitStatus=0
    
    [Install]
    WantedBy=multi-user.target
