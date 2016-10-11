package de.qaware.spark.rdd;

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
 * Created by weigend on 09.10.16.
 */
public class ValuesRddTest {
    public static void main(String[] args) throws IOException {

        final String solrZkHost = "192.168.1.100:2181";
        //final String sparkMasterUrl = "local[*]";
        final String sparkMasterUrl = "spark://192.168.1.100:7077";

        SparkConf conf = new SparkConf().setMaster(sparkMasterUrl).setAppName("SparkSolrCsvAnayzer");

        try (JavaSparkContext context = new JavaSparkContext(conf)) {
            context.addJar("./build/libs/spark-solr-csv-analyzer-1.0-SNAPSHOT-all.jar");

            // make sure executors are up an running
            context.parallelize(new ArrayList<String>(), 20).map(n -> n);

            // SolrQuery contains the query and all required parameters for using the /export handler.
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.set("q", "metric:*Heap*used AND host:lpswl09");

            // required parameters for the /export request handler are fl and sort
            solrQuery.set("fl", "metric,host,process,values,mindate");
            solrQuery.set("sort", "mindate asc");
            solrQuery.set("qt", "/export");

            for (int i = 0; i < 10; i++) {

                Date start = new Date();

                // this does a parallel standard cursor based deep query
                SolrRDD simpleRdd = new SolrRDD(
                        solrZkHost,
                        "ekgdata3",
                        context.sc(),
                        new Some<>("/export"),
                        Some.empty(),
                        Some.empty(),
                        Some.empty(),
                        Some.empty(),
                        Some.empty(),
                        new Some<>(solrQuery));

                // Accessing Values

                ValuesRdd values = new ValuesRdd(simpleRdd);
                System.out.println(values.queryValues(solrQuery).stats());

                Date end = new Date();
                long duration = end.getTime() - start.getTime();

                System.out.println("Result takes " + (duration) + " ms.");
            }
        } finally {
            //noinspection ThrowFromFinallyBlock,ResultOfMethodCallIgnored
            System.in.read();
        }
    }
}
