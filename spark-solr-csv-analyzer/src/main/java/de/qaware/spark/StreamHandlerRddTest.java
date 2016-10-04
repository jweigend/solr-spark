package de.qaware.spark;

import com.lucidworks.spark.rdd.SolrJavaRDD;
import com.lucidworks.spark.rdd.SolrRDD;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.io.Tuple;
import org.apache.solr.common.SolrDocument;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Option;
import scala.Some;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by weigend on 04.10.16.
 */
public class StreamHandlerRddTest {
    public static void main(String[] args) throws IOException {

        final String solrZkHost = "192.168.1.100:2181";
        //final String sparkMasterUrl = "local[*]";
        final String sparkMasterUrl = "spark://192.168.1.100:7077";

        Date start = new Date();
        JavaSparkContext context = null;
        try {
            SparkConf conf = new SparkConf().setMaster(sparkMasterUrl).setAppName("SparkSolrCsvAnayzer");

            context = new JavaSparkContext(conf);
            context.addJar("./build/libs/spark-solr-csv-analyzer-1.0-SNAPSHOT-all.jar");

            String expr =
                    "search(ekgdata2, " +
                            " q=\"metric:*used\"," +
                            " fl=\"date,metric,value,host,process\"," +
                            " sort=\"date asc\"," +
                            " qt=\"/export\")";

            SolrQuery solrQuery = new SolrQuery();
            solrQuery.set("expr", expr);

            SolrRDD streamExprRDD = new SolrRDD(
                    solrZkHost,
                    "ekgdata2",
                    context.sc(),
                    new Some<>("/stream"),
                    Some.empty(),
                    Some.empty(),
                    Some.empty(),
                    Some.empty(),
                    Some.empty(),
                    new Some(solrQuery));

            SolrJavaRDD rdd = new SolrJavaRDD(streamExprRDD);

            System.out.println("Result: " + rdd.count() + " Documents streamed.");

            JavaRDD<SolrDocument> docs = rdd.query("");

            List<SolrDocument> d = docs.take(10);
            System.out.println(Arrays.deepToString(d.toArray()));

        } finally {
            System.in.read();
            context.close();
        }
    }
}
