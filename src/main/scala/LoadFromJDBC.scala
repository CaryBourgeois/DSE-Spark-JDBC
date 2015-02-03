/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Below are the libraries required for this project.
 * In this example all of the dependencies are included with the DSE 4.6 distribution.
 * We need to account for that fact in the build.sbt file in order to make sure we don't introduce
 * library collisions upon deployment to the runtime.
 */

import java.sql.{ResultSet, DriverManager}

import com.datastax.spark.connector._
import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.log4j.{Level, Logger}
import org.apache.spark.rdd.JdbcRDD
import org.apache.spark.sql.cassandra.CassandraSQLContext
import org.apache.spark.{SparkContext, SparkConf}

object LoadFromJDBC {
  /*
   * This is the entry point for the application
   */
  def main(args: Array[String]): Unit =  {

    Logger.getRootLogger.setLevel(Level.WARN)

    /*
     * The first step in this process is to set up the context for configuration for the Spark instance being used.
     * For this example the configuration reflects running DSE/Spark on the local system. In a production system you
     * would want to modify the host and Master to reflect your installation.
     */
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

    // create a new SparkContext
    val sc = new SparkContext(sparkConf)

    // create a new SparkSQLContext
    val csc = new CassandraSQLContext(sc)

    /*
     * call the initCassandra function to create the Keyspace and Table where we will write the data.
     */
    initCassandra(sparkConf)

    /*
     * call queryJDBCandSaveToCassandra to read the data from our JDBC source and the save to the
     * Cassandra table created in intiCassandra
     */
    queryJDBCandSaveToCassandra(sc)

    /*
     * call validateResults to make sure we returned the correct number of rows.
     */
    validateResults(csc)
  }

  def initCassandra (sparkConf: SparkConf): Unit = {
    /*
     *   In this section we create a native session to Cassandra.
     *   This is done so that native CQL statements can be executed against the cluster.
     */
    CassandraConnector(sparkConf).withSessionDo { session =>
      /*
       * Make sure that the keyspace we want to use exists and if not create it.
       *
       * Change the topology an replication factor to suit your cluster.
       */
      session.execute(s"CREATE KEYSPACE IF NOT EXISTS spark_cass WITH REPLICATION = { 'class':'SimpleStrategy', 'replication_factor':1}")

      /*
          Below the data table is DROPped and re-CREATEd to ensure that we are dealing with new data.
       */

      session.execute(s"DROP TABLE IF EXISTS spark_cass.simple")
      session.execute(s"CREATE TABLE IF NOT EXISTS spark_cass.simple (pk_id int, value text, PRIMARY KEY(pk_id))")

      //Close the native Cassandra session when done with it. Otherwise, we get some nasty messages in the log.
      session.close()
    }
  }

  def queryJDBCandSaveToCassandra(sc: SparkContext): Unit = {
    /*
     * In this section we create an RDD of type JdbcRDD. This entire process is handled in the
     * simple act of creating the RDD object. It is VERY important to understand each of the parameters
     * in the object creation call.
     *
     *  1. sc -> is the Spark Context created at the beginning of the program and passed to this function
     *  2. () -> JDBC Driver Manager specification. The details of this call will be specific to the JDBC
     *           driver being used. This on corresponds to the version of MySQL we setup earlier.'
     *  3. SQL -> This is the SELECT command that will be passed to the database. NOTICE the WHERE clause
     *            and specifically the primary key range that is specified. This plays a key roll in how
     *            Spark will partition the results. Each partition will query the database individually
     *            passing in the SQL that represents the range of Primary keys for that partition.
     *  4. 1 -> The minimum value of the partition key used in the SQL command.
     *  5. 150 -> The maximum value of the partition key used inthe SQL command.
     *  6. 3 -> The number of Spark partitions to divide the keys across.
     *  7. (r: ResultSet) -> Mapping function that translates the returned rows to an standard RDD.
     */
    val rdd = new JdbcRDD(
      sc,
      () => { DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/spark_cass_test", "dseuser", "datastax") },
      "SELECT id, value FROM simple WHERE ? <= id AND id <= ?",
      1, 150, 3,
      (r: ResultSet) => { (r.getInt(1), r.getString(2)) } )

    /*
     * Save the RDD to Cassandra Keyspace and Table previously prepared.
     */
   rdd.saveToCassandra("spark_cass", "simple", SomeColumns("pk_id", "value"))
  }

  def validateResults (csc: CassandraSQLContext): Unit = {
    /*
     * In this section we execute a simple SparkSQL query to make sure we returned
     * the correct number of rows.
     */
    println("Simple Table Record Count: ", csc.sql(s"SELECT COUNT(*) FROM spark_cass.simple").collect.foreach(println))
  }

}
