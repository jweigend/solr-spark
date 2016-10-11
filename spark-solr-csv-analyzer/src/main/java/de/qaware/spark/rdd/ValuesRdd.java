package de.qaware.spark.rdd;

import com.lucidworks.spark.rdd.SolrJavaRDD;
import com.lucidworks.spark.rdd.SolrRDD;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.spark.api.java.JavaDoubleRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.DoubleFlatMapFunction;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.InflaterInputStream;

/**
 * Unpacks values.
 * Created by weigend on 09.10.16.
 */
public class ValuesRdd extends SolrJavaRDD {

    /**
     * The values rdd.
     *
     * @param solrRDD a solr rdd.
     */
    public ValuesRdd(SolrRDD solrRDD) {
        super(solrRDD);
    }

    /**
     * Queryies and extracts the values field.
     *
     * @param query the query.
     * @return a double rdd.
     */
    public JavaDoubleRDD queryValues(SolrQuery query) {

        JavaRDD<SolrDocument> queryResult = super.query(query.getQuery());

        JavaRDD<String> values = queryResult.map(
                document -> document.getFieldValue("values").toString()
        );
        return values.flatMapToDouble(
                (DoubleFlatMapFunction<String>) valuesString -> {
                    String json = extractEncodedJson(valuesString);
                    return parseValues(json);
                });
    }

    /**
     * Helper to parse all values from the JSON string.
     *
     * @param json the JSON.
     * @return a list of doubles.
     */
    private List<Double> parseValues(String json) {
        List<Double> result = new ArrayList<>();
        NumberFormat format = new DecimalFormat();
        try {
            for (int i = 0; i < json.length(); i++) {
                int idx = json.indexOf("v:\"", i);
                if (idx == -1) {
                    break;
                }
                int numberIdx = idx + "v:\"".length();
                int endIdx = json.indexOf("\"", numberIdx);
                result.add(format.parse(json.substring(numberIdx, endIdx )).doubleValue());
            }
        } catch (ParseException e) {
            Logger.getLogger(ValuesRdd.class.getName()).severe(e.getMessage());
        }
        return result;
    }

    /**
     * Extracts the compressed JSON String.
     *
     * @param values - a values string.
     * @return the decompressed Json String.
     */
    private String extractEncodedJson(String values) {
        byte[] rawData = Base64.getDecoder().decode(values);
        return decompress(rawData);
    }

    /**
     * Helper to decompress.
     *
     * @param bytes the bytes.
     * @return a
     */
    public static String decompress(byte[] bytes) {
        InputStream in = new InflaterInputStream(new ByteArrayInputStream(bytes));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) > 0)
                baos.write(buffer, 0, len);
            return new String(baos.toByteArray(), "UTF-8");
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
