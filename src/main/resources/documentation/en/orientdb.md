## Accessing your Database through OrientDb

You can access your database through OrientDb directly. This might be needed if you want to fix your data or look at it
directly for some reason (nosiness?). Work the following steps:

* Go to [http://orientdb.com/download/](http://orientdb.com/download/) and download the community version of your
  platform.
* Extract the ZIP to some folder - you will see a sub folder called "databases" (which should contain a database called
  "GratefulDeadConcerts").
* In your Segrada database (`segrada_data`) there should be a sub folder called "db". Copy that folder to the
  "databases" folder mentioned above.
* Run the OrientDb server (using `server.sh` or `server.bat`) and point your browser to
  [http://localhost:2480/](http://localhost:2480/).
* Log into your database ("db") using admin/admin as login/password.
* You can access your data directly now - OrientDb uses an SQL like syntax.

After you changed stuff, copy back your "db" folder to `segrada_data`.