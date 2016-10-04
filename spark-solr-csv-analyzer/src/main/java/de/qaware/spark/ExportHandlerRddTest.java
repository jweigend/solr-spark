package de.qaware.spark;

import com.lucidworks.spark.rdd.SolrJavaRDD;
import com.lucidworks.spark.rdd.SolrRDD;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.io.Tuple;
import org.apache.solr.common.SolrDocument;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Some;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by weigend on 04.10.16.
 */
public class ExportHandlerRddTest {
    public static void main(String[] args) throws IOException {

        final String solrZkHost = "192.168.1.100:2181";
        //final String sparkMasterUrl = "local[*]";
        final String sparkMasterUrl = "spark://192.168.1.100:7077";

        JavaSparkContext context = null;
        try {
            SparkConf conf = new SparkConf().setMaster(sparkMasterUrl).setAppName("SparkSolrCsvAnayzer");

            context = new JavaSparkContext(conf);
            context.addJar("./build/libs/spark-solr-csv-analyzer-1.0-SNAPSHOT-all.jar");

            // make sure executors are up an running
            context.parallelize(Collections.EMPTY_LIST, 20).map(n -> n);

            Date start = new Date();

            // SolrQuery contains the query and all required parameters for using the /export handler.
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.set("q", "metric:*");

            // required parameters for the /export request handler are fl and sort
            solrQuery.set("fl", "date,metric,host,process,value");
            solrQuery.set("sort", "data asc");
            solrQuery.set("qt", "/export");

            // this does a parallel standard cursor based deep query
            SolrRDD simpleRdd = new SolrRDD(
                    solrZkHost,
                    "ekgdata2",
                    context.sc(),
                    new Some("/export"),
                    Some.empty(),
                    Some.empty(),
                    Some.empty(),
                    Some.empty(),
                    Some.empty(),
                    new Some(solrQuery));

            long exportedDocs = simpleRdd.count();

            Date end = new Date();
            long duration = end.getTime() - start.getTime();

            System.out.println("Result: " + exportedDocs + " SolrDocuments read in Spark in " + (duration) + " ms.");

            simpleRdd.query(solrQuery);
            System.out.println(Arrays.deepToString((Object[])simpleRdd.take(5)));

        } finally {
            System.in.read();
            context.close();
        }
    }
}
