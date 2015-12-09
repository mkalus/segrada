## Starting Segrada Automatically

### Windows

There are a number of possibilities on how to automatically start Segrada when you start the computer or log in. The
application will be started in the backround and is accessible on [http://localhost:8080/](http://localhost:8080/).

#### Using the Task Scheduler

1. Start the [Task Scheduler](https://en.wikipedia.org/wiki/Windows_Task_Scheduler). Easiest way is to click on start
   and use the text field. Enter "task scheduler" and press enter.
1. Create a New Task ("Action" menu -> "Create Task..."). You will see a window with several tabs.
1. In the **General** tab, add the name of your task ("Segrada" for example). If you want to run your application as
   a team server, check "Run whether user is logged on or not".
1. In the **Triggers** tab, click on "New..." and set "Begin the task" to "At Startup". You might want to add a small
   delay in seconds to wait for the application to start. Also make sure, "Enabled" is checked. Click on "Ok".
1. In the **Actions** tab, also click on "New...". Set "Action" as "Start a program". In "Program/Script", select the
   "start_server.bat" file found in your Segrada folder. Also click on "Ok".
1. In the **Settings** tab, check the option "Allow task to be run on demand". Disable "Stop task if it runs longer
   than" if it is set. Also keep "Do not start a new instance" for "If the task is already running, then the following
   rule applies". Click "Ok".
1. Click "Ok" button and enter the password of the user, if needed.

#### Start on Login

1. Use Windows Explorer to go to your Segrada folder.
1. Find the file called "start_server.bat" and right-click on it. Press "Create Shortcut". Right-click on your newly
   created shortcut and select "Cut".
1. Click on your "Start" button and select "All Programs", find the folder called "Startup". Right-click on it and
   select "Open".
1. Paste the shortcut (right-click in the folder and choose "Paste", or press CTRL+V).

On the next login, Segrada will be automatically started.

### Mac

If anyone can provide information automatic launches here, we are happy to include them.

### Linux, UNIX

#### Gnome, Ubuntu Desktops

1. Launch the program "Startup Applications".
1. Click "Add". Fill out the name "Segrada". As command enter `/path/to/segrada/start.sh headless`

#### Using Supervisord

If you use supervisord, add the following to your configuration (either /etc/supervisord.conf or an extra file in
/etc/supervisord.d/). Edit it to fit your needs:

    [program:segrada]
    command = /path/to/segrada/start.sh headless
    directory = /path/to/segrada/
    autorestart = True
    user = some_user

#### Using Upstart

Add a file called `segrada.conf` to /etc/init/. Edit the following template to get started:

    description "Segrada"
    
    start on (local-filesystems and net-device-up IFACE!=lo)
    stop on runlevel [!2345]
    
    env STNORESTART=yes
    env HOME=/path/to/segrada/
    setuid "some_user"
    setgid "some_user"
    
    exec /path/to/segrada/start.sh headless
    
    respawn

#### Using systemd

To set up Segrada as a system service, add and edit the file `segrada.service` to /etc/systemd/system/:

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
