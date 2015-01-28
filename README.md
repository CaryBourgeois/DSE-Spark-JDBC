#Introduction

In this set of exercises we will walk through the process of loading data into Cassandra from a JDBC data source using Spark. The process will be broken down into several parts that build upon one another. Please review these in order. If something does not work in one section it may be due to pieces that were built in a previous session.

#Prerequisites
These exercises were all built using DSE 4.6. At a minimum you should be using that version of DSE. For this series of exercises we will be running all of the examples against a single node cluster.

An instance of MySQL running on your local machine is also a requirement. The general approach will work for any JDBC data source but the specifics are unique to MySQL

Scala is used heavily in the exercises that follow. Some familiarity with Scala will be very beneficial but is not an absolute requirement. All the exercises could be completed using the DSE Python Spark integration. That effort is left as an exercise to the reader.

##1. Prepare MySQL

The goal of this portion of the exercise is to make sure you have an installation of MySQL that can support the following sections.

You will perform the following activities in this exercise

  * Validate and/or install MySQL. Ensure that you can reach the MySQL command line interface.
  * Prepare the Schema and the table to be used later.
  * Create a user and grant that user the rights needed to support the exercise.

Please proceed to the file [PrepareMySQL.md](./PrepareMySQL.md)

##2. Build and run a Scala program that reads local files and loads them into native Cassandra tables.

The goal of this exercise is to build and run a Spark program using Scala that will read several local files and load them into native cassandra tables on a Spark enabled Cassandra cluster.

In this exercise you will perform the following steps:

  * Clone a GitHub repository to your local machine
  * Ensure that you have sbt installed and accessible on your machine
  * Find and edit the Scala code example to ensure it is configured for your environment
  * Use sbt to build and run the example on your Cassandra/Spark cluster
  * Use SparkSQL from the DSE Spark REPL to validate the data loaded into your cluster

Please proceed to the file [LoadFromJDBC.md](./LoadFromJDBC.md)