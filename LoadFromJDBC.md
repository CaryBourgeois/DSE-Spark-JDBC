#Overview

The goal of this exercise is to build and run a Spark program using Scala that will read data from a JDBC connection and then load it into native cassandra tables on a Spark enabled Cassandra cluster.

In this exercise you will perform the following steps:

1. Clone a GitHub repository to your local machine
2. Ensure that you have sbt installed and accessible on your machine
3. Find and edit the Scala code example to ensure it is configured for your environment
4. Use sbt to build and run the example on your Cassandra/Spark cluster
5. Use SparkSQL from the DSE Spark REPL to validate the data loaded into your cluster

#Requirements

You have completed the first step in this exercise and have completed the preperation of MySQL. If not please see: [PrepareMySQL.md](./PrepareMySQL.md)

Local copy of DSE 4.6 installed (This example is based on a tarball install on Mac OS X). Make sure you can get to and interact with the Spark REPL included with DSE.

You need to have a GitHub id and git installed on your local machine. Further, you must be able to clone GitHub repositories to your local machine.

The ability to install sbt on your local machine is also required.

##1. Clone a GitHub repository to your local machine

Navigate to a directory that you would like to use for this project. From the command line in that directory issue the following command

                git https://github.com/CaryBourgeois/DSE-Spark-JDBC.git

Review the directory to ensure that you have downloaded all of the files from the repository.

##2. Ensure that you have sbt installed and accessible on your machine

This program uses the Scala Build Tool (sbt) to build and run the code in this exercise. For this to work sbt must be installed and on the executable path of your system.

To validate sbt is installed on your system you should be able to go to the command line and execute the following commands and get these or similar results.

        $>sbt sbt-version
        [info] Set current project to bin (in build file:/Users/carybourgeois/bin/)
        [info] 0.13.5
If this is not the case then please visit the [sbt site](http://www.scala-sbt.org/) for instructions on how to download and install sbt.

##3. Find and edit the Scala code example to ensure that it is configured for your environment

This project was created with the Community Edition of IntelliJ IDEA. The simplest way to review and modify the scala code is to open the project with this IDE. Alternatively, you can use any text editor to view/edit the file as the build and execute process will work from the command line via sbt.

From the directory where you cloned the github project, navigate to the `/src/main/scala` directory. Locate and open the file `LoadFromJDBC.scala` file.

This is a simple Scala/Spark example. It contain one object `LoadFromJDBC`. Within that object, there are several methods. The methods are described below.

  * `main` This is the entry point into the program.
    * Create the `SparkConf` variable.
    * Specify the Spark configuration parameters (These will have to be modified to fit your environment)
    * Create the Spark Context `sc` based on the Spark Configuration
    * Create the `CassandraSQLContext` based on the `sc` just created. This will be used in places where we use SparkSQL.
    * Call the subsequent methods in the program
  * `initCassandra(sparkConf)` Prepare the Cassandra keyspace and tables for the new data
    * Obtain a native connection to Cassandra based on passed `sparkConf`.
    * Verify/create the keyspace on the cluster
    * Drop tables if they already exist and create new tables to receive the data
    * Close the session variable as it will not be needed again.
  * `queryJDBCandSaveToCassandra(sc)` as the method suggests, Connect to JDBC read data and then save to Cassandra.
    * Create a new JdbcRDD. This is a single call to create a new RDD object. Pay attention to the details, all the work is don ein the constructor including connection to and querying from the JDBC source as well as the treatment of the results by Saprk.
    * Save the RDD containing the parsed lines into Cassandra using the `saveToCassandra` method
  * `validateResults(csc)` Use SparkSQL to validate the load inserted the correct number of records

Once you have reviewed the code you will need to make changes to reflect your specific system.

  * Locate the SparkConf settings and modify the ip to reflect your system. If you are running a local copy of DSE no changes will be required. If not, you will need to substitute your server's ip address in place of `127.0.0.1`.

        val sparkConf = new SparkConf().set("spark.cassandra.connection.host", "127.0.0.1")
              /*
               * The next two lines that are commented can be used to trace the execution of the
               * job using the sparkUI. On a local system where this code would work the URL for the
               * spark UI would be http://127.0.0.1:7080.
               * Before un-commenting these lines, make sure the spark.eventLog.dir exist and is
               * accessible by the process running spark.
              */
              //.set("spark.eventLog.enabled", "true")
              //.set("spark.eventLog.dir", "/Users/carybourgeois/Datastax/log/spark-events")
              .setJars(Array("target/scala-2.10/DSE-Spark-JDBC-assembly-1.0.jar"))
              .setMaster("spark://127.0.0.1:7077")
              .setAppName("DSE Spark JDBC")

  * Locate the section of the method `queryJDBCandSaveToCassandra` where the connection to MySQL is made. Substitute the settings for your MySQL instance.

        val rdd = new JdbcRDD(
              sc,
              () => { DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/spark_cass_test", "dseuser", "datastax") },
              "SELECT id, value FROM simple WHERE ? <= id AND id <= ?",
              1, 150, 3,
              (r: ResultSet) => { (r.getInt(1), r.getString(2)) } )

  * Save the changes to the file.

##4. Use sbt to build and run the example on your Cassandra/Spark cluster

You will now use sbt to build and run the file you have modified.

  * Navigate back to the root of the project if you changed your path in the previous exercise.

  * Run sbt to build the project using the command below form the command line. This command will compile for file we created and build the "fat" jar that will be copied to the Spark master for execution. This could be a lengthy process as sbt probably have to download a number of files. At the end of the process you should have a response of `[success]`

        sbt assembly

  * Run the project using the sbt run command. This will copy the "fat" jar to the Spark system and execute the program. The output will contain a bunch of [INFO] entries and conclude with [success] is all has gone well.

        sbt run

  * You can look at the status of your job using Spark Web UI. You get to this using the URL http://\<your ip address\>:7080. If you are using a local DSE/Spark environment the link would be [http://127.0.0.1:7080](http://127.0.0.1:7080)

NOTE: One of the more challenging parts of running a program on a spark cluster is building the "fat" jar that contains the libraries you need while not duplicating those on the Spark system and causing conflicts. This is managed by the `build.sbt` file. In this example, pay close attention to the `mysql` line of this file. Notice that the `provided` flag is not set. This means that the `sbt assembly` process will include this dependency in the fat jar that gets pushed to the Spark nodes.

If you plan on doing much of this work you should read and understand how this process works.

##5. Use SparkSQL from the DSE Spark REPL to validate the data loaded into your cluster

The next step is to go back to the DSE Spark REPL and validate that the data was loaded correctly.

  * To validate the number of records that was loaded execute the command below.

        csc.sql(s"SELECT COUNT(*) FROM spark_cass.simple").first.foreach(println)

  * To get a listing of the records in the table you just created.

        csc.sql(s"SELECT * FROM spark_cass.simple").take(10).foreach(println)

