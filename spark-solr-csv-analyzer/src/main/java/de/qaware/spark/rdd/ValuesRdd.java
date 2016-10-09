package de.qaware.spark.rdd;

import com.lucidworks.spark.rdd.SolrJavaRDD;
import com.lucidworks.spark.rdd.SolrRDD;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.spark.api.java.JavaDoubleRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.DoubleFlatMapFunction;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.execution.columnar.DOUBLE;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by weigend on 09.10.16.
 */
public class ValuesRdd extends SolrJavaRDD {

    /**
     * The values rdd.
     * @param solrRDD a solr rdd.
     */
    public ValuesRdd(SolrRDD solrRDD) {
        super(solrRDD);
    }

    /**
     * Queryies and extracts the values field.
     * @param query
     * @return
     */
    public JavaDoubleRDD queryValues(SolrQuery query) {

        JavaRDD<SolrDocument> result = super.query(query.getQuery());

        JavaRDD<String> values = result.map(
                document -> document.getFieldValue("values").toString()
        );
        JavaDoubleRDD valuesAsDoubles = values.flatMapToDouble(
                (DoubleFlatMapFunction<String>) s -> {
                    String json = extractEncodedJson(s);
                    List<Double> result1 = parseValues(json);
                    return result1;
                });
        return valuesAsDoubles;
    }

    private List<Double> parseValues(String json) {
        List<Double> result = new ArrayList<>();
        return result;
    }

    private String extractEncodedJson(String s) {






        return null;
    }
}
