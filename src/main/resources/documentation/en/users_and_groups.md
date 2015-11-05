# Users and Groups

Currently in progress. Planned features:
* Users being members of one user group
* Groups with access rights


## Starting Segrada in Multi User Mode

Normally, Segrada will log in an access as administrator by default. To enable logins, you must start Segrada with
the following command line option:

    java -jar -DrequireLogin="true" segrada-1.0-SNAPSHOT.jar

or in server mode:

    java -jar -DrequireLogin="true" segrada-1.0-SNAPSHOT.jar headless

After starting the application, you have to log in. To enhance security, please change the default user "Administrator"
to something else.
