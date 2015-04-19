#Overview

The goal of this portion of the exercise is to make sure you have an installation of MySQL that can support the following sections.

You will perform the following activities in this exercise

  1. Validate and/or install MySQL. Ensure that you can reach the MySQL command line interface.
  2. Prepare the Schema and the table to be used later.
  3. Create a user and grant that user the rights needed to support the exercise.

#Requirements

The ability to access MySQl Command Line and an active install of MySQL or the ability to install MySQl on you local system. The examples in this exercise are all based on a local install of MySQL v5.6.x on a Mac OS X system.

##1. Validate and/or install MySQL. Ensure that you can reach the MySQL command line interface.

If you need to install MySQL please visit this site: [http://dev.mysql.com/downloads/mysql/](http://dev.mysql.com/downloads/mysql/)

MySQL should be up and running prior to executing these steps.

On a Mac OS X/Linux system you should be able to start the MySQL Command Line with the command below. This assumes that you have minimal security and that root user is enabled. Use the second command if you have a password enabled for the root user.

          mysql --user=root

          mysql --user=root --password=password

If MySQL is not on the local host or you are connecting to a remote MySQL server use the command below substituting your username, password and ip address.

        mysql --user=root --password=password --host=<ip address>

##2. Prepare the Schema and the table to be used later.

Once at the `mysql>` prompt you can issue the following commands to prepare the database and tables for our example.

          CREATE SCHEMA spark_cass_test;  # Create the schema we will use

          SHOW DATABASES;                 # Show a list of schemas and verify we create ours

          USE spark_cass_test;            # Set our new schema as the default schema

          CREATE TABLE simple (
               id MEDIUMINT NOT NULL AUTO_INCREMENT,
               value CHAR(30) NOT NULL,
               PRIMARY KEY (id)
          );                              # Create the table we will use

          SHOW TABLES IN spark_cass_test;            # Validate that the table was created

The following command will insert the data that we will use in our examples. The command will create 150 records which will be important to our partitioned RDD.

          INSERT INTO simple (value) VALUES ('A000'), ('A001'), ('A002'), ('A003'), ('A004'), ('A005'), ('A006'), ('A007'), ('A008'), ('A009');
          INSERT INTO simple (value) VALUES ('A010'), ('A011'), ('A012'), ('A013'), ('A014'), ('A015'), ('A016'), ('A017'), ('A018'), ('A019');
          INSERT INTO simple (value) VALUES ('A020'), ('A021'), ('A022'), ('A023'), ('A024'), ('A025'), ('A026'), ('A027'), ('A028'), ('A029');
          INSERT INTO simple (value) VALUES ('A030'), ('A031'), ('A032'), ('A033'), ('A034'), ('A035'), ('A036'), ('A037'), ('A038'), ('A039');
          INSERT INTO simple (value) VALUES ('A040'), ('A041'), ('A042'), ('A043'), ('A044'), ('A045'), ('A046'), ('A047'), ('A048'), ('A049');

          INSERT INTO simple (value) VALUES ('B000'), ('B001'), ('B002'), ('B003'), ('B004'), ('B005'), ('B006'), ('B007'), ('B008'), ('B009');
          INSERT INTO simple (value) VALUES ('B010'), ('B011'), ('B012'), ('B013'), ('B014'), ('B015'), ('B016'), ('B017'), ('B018'), ('B019');
          INSERT INTO simple (value) VALUES ('B020'), ('B021'), ('B022'), ('B023'), ('B024'), ('B025'), ('B026'), ('B027'), ('B028'), ('B029');
          INSERT INTO simple (value) VALUES ('B030'), ('B031'), ('B032'), ('B033'), ('B034'), ('B035'), ('B036'), ('B037'), ('B038'), ('B039');
          INSERT INTO simple (value) VALUES ('B040'), ('B041'), ('B042'), ('B043'), ('B044'), ('B045'), ('B046'), ('B047'), ('B048'), ('B049');

          INSERT INTO simple (value) VALUES ('C000'), ('C001'), ('C002'), ('C003'), ('C004'), ('C005'), ('C006'), ('C007'), ('C008'), ('C009');
          INSERT INTO simple (value) VALUES ('C010'), ('C011'), ('C012'), ('C013'), ('C014'), ('C015'), ('C016'), ('C017'), ('C018'), ('C019');
          INSERT INTO simple (value) VALUES ('C020'), ('C021'), ('C022'), ('C023'), ('C024'), ('C025'), ('C026'), ('C027'), ('C028'), ('C029');
          INSERT INTO simple (value) VALUES ('C030'), ('C031'), ('C032'), ('C033'), ('C034'), ('C035'), ('C036'), ('C037'), ('C038'), ('C039');
          INSERT INTO simple (value) VALUES ('C040'), ('C041'), ('C042'), ('C043'), ('C044'), ('C045'), ('C046'), ('C047'), ('C048'), ('C049');

Now, query the table we created to make sure we have gotten this far without issue. The first 5 rows should look somethin glike the table below the command.

          SELECT id, value FROM simple;

          +-----+-------+
          | id  | value |
          +-----+-------+
          |   1 | A000  |
          |   2 | A001  |
          |   3 | A002  |
          |   4 | A003  |
          |   5 | A004  |


##3. Create a user and grant that user the rights needed to support the exercise.

Next we need to create a user and grant that user the necessary access to work with the data. WARNING: This step exposes your system to outside access. If you are worried about this you need to investigate MySQL security and approach this from a more secure perspective.

          CREATE USER 'dseuser'@'%' IDENTIFIED BY 'datastax';

          GRANT ALL PRIVILEGES ON *.* TO 'dseuser'@'%' IDENTIFIED BY 'datastax';

Finally, quit the MySQL command line `mysql>QUIT` and login again as the user we just created.

          mysql --user=dseuser --password=datastax

Make sure you can access the Schema we created and query the data.

          USE spark_cass_test;

          SELECT * FROM simple;

If you get the same output as seen earlier you are ready to proceed to the next step.
