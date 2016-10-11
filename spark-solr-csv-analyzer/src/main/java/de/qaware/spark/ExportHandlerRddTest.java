/*
 _____________________________________________________________________________

            Project:    BigData 2016
  _____________________________________________________________________________

         Created by:    Johannes Weigend, QAware GmbH
      Creation date:    September 2016
  _____________________________________________________________________________

          License:      Apache License 2.0
  _____________________________________________________________________________
 */
package de.qaware.spark;

import com.lucidworks.spark.rdd.SolrRDD;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Some;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Tests a SolrRDD in combination with the /export Handler.
 * Created by weigend on 04.10.16.
 */
public class ExportHandlerRddTest {
    public static void main(String[] args) throws IOException {

        final String solrZkHost = "192.168.1.100:2181";
        //final String sparkMasterUrl = "local[*]";
        final String sparkMasterUrl = "spark://192.168.1.100:7077";

        SparkConf conf = new SparkConf().setMaster(sparkMasterUrl).setAppName("SparkSolrCsvAnayzer");

        try (JavaSparkContext context = new JavaSparkContext(conf)) {
            context.addJar("./build/libs/spark-solr-csv-analyzer-1.0-SNAPSHOT-all.jar");

            // make sure executors are up an running
            context.parallelize(new ArrayList<String>(), 20).map(n -> n);

            Date start = new Date();

            // SolrQuery contains the query and all required parameters for using the /export handler.
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.set("q", "metric:*");

            // required parameters for the /export request handler are fl and sort
            solrQuery.set("fl", "date,metric,host,process,value");
            solrQuery.set("sort", "date asc");
            solrQuery.set("qt", "/export");

            // this does a parallel standard cursor based deep query
            SolrRDD simpleRdd = new SolrRDD(
                    solrZkHost,
                    "ekgdata2",
                    context.sc(),
                    new Some<>("/export"),
                    Some.empty(),
                    Some.empty(),
                    Some.empty(),
                    Some.empty(),
                    Some.empty(),
                    new Some<>(solrQuery));

            long exportedDocs = simpleRdd.count();

            Date end = new Date();
            long duration = end.getTime() - start.getTime();

            System.out.println("Result: " + exportedDocs + " SolrDocuments read in Spark in " + (duration) + " ms.");

            simpleRdd.query(solrQuery);
            System.out.println(Arrays.deepToString((Object[]) simpleRdd.take(5)));

        } finally {
            //noinspection ThrowFromFinallyBlock,ResultOfMethodCallIgnored
            System.in.read();

        }
    }
}
