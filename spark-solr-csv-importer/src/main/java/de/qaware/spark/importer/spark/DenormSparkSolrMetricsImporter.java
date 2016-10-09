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
package de.qaware.spark.importer.spark;

import de.qaware.spark.importer.MetricsImporter;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.input.PortableDataStream;
import scala.Tuple2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Parallel CSV Import Sample. Metric CSVs must have the following form:
 * <p>
 * CSV
 * ----------------------------------------------------------
 * Date; Metric-Name1; Metric2; Metric3 ...
 * dd.MM.yyyy HH:mm:ss.SSS; 100,100,100.000 (US Format)
 * ----------------------------------------------------------
 * <p>
 * Created by weigend on 20.09.16.
 */
public class
DenormSparkSolrMetricsImporter implements MetricsImporter, Serializable {

    /**
     * The delimiter of the imported files.
     */
    private static final String DELIMITER = ";";

    /**
     * The collection name of the Solr collection to import the data.
     */
    private static final String COLLECTION_NAME = "ekgdata3";

    /**
     * The Zookeeper URL (host/port). f.e. localhost:2181.
     */
    private String zkHost;

    /**
     * Our CSV Dateformat.
     */
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS", Locale.US);


    /**
     * A default constructor is required by Spark to serialize this class and their lambdas.
     */
    public DenormSparkSolrMetricsImporter() {
    }

    /**
     * Initializes this class with a Zookeeper Url for Solr Cloud and a Spark Master to run this code.
     */
    public DenormSparkSolrMetricsImporter(String zkHost) {
        this.zkHost = zkHost;
    }

    /**
     * Parallel Solr import using Spark.
     *
     * @param pathUrl the URL could be a local file or path or a hdfs path.
     * @param jsc     The context.
     * @see SparkContext#binaryFiles(String, int)  method.
     */
    public void importMetrics(String pathUrl, JavaSparkContext jsc) {

        // Get a collection of all files in the given path.
        JavaPairRDD<String, PortableDataStream> rdd = jsc.binaryFiles(pathUrl);

        // default: max tasks - full parallelism.
        jsc.parallelize(rdd.toArray()).foreach(new ImporterImpl()::importFile);
    }

    /**
     * Command class for import to avoid Lambdas.
     */
    private class ImporterImpl implements Serializable {
        void importFile(Tuple2<String, PortableDataStream> tuple) {
            String fileUrl = tuple._1;
            PortableDataStream fileStream = tuple._2;
            try {
                importIntoSolr(fileUrl, fileStream);
            } catch (ParseException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * Helper to import a single file.
     *
     * @param fileUrl    the filename.
     * @param fileStream the stream.
     */
    private void importIntoSolr(String fileUrl, PortableDataStream fileStream) throws ParseException {

        // Cloud Client
        final CloudSolrClient solrCloudClient = new CloudSolrClient.Builder().withZkHost(zkHost).build();
        solrCloudClient.setDefaultCollection(COLLECTION_NAME);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileStream.open()), 1000000)) {
            String line;

            // assuming first line is a csv header
            // Caution: This only works with files < 128 MB / One Hadoop Block
            String firstLine = reader.readLine();
            String[] fieldNames = firstLine.split(DELIMITER);
            if (!fieldNames[0].equals("Date")) {
                Logger.getLogger(SimpleSparkSolrMetricsImporter.class.getName()).warning("Unknown file format!");
                return;
            }

            // Build a list of value JSON strings. Each string contains the values of a single CSV row.
            List<StringBuilder> valuesList = new ArrayList<>();
            for (int i = 1; i < fieldNames.length; i++) {
                valuesList.add(new StringBuilder("values: ["));
            }

            // split host/process/type information out of the filename
            FileMetadata parts = new FileMetadata(fileUrl);

            Date minDate = null;
            Date maxDate = null;

            // loop over csv file, produce and add documents
            while ((line = reader.readLine()) != null) {

                String[] values = line.split(DELIMITER);
                if (minDate == null) {
                    minDate = DATE_FORMAT.parse(values[0]);
                } else {
                    maxDate = DATE_FORMAT.parse(values[0]);
                }

                // Produce a long String containing a JSON rep of all date:value Pairs
                for (int i = 1; i < fieldNames.length; i++) {
                    valuesList.get(i - 1)
                            .append("{ d:\"")
                            .append(values[0])
                            .append("\",v:\"")
                            .append(values[i])
                            .append("\"},");
                }
            }

            List<SolrInputDocument> documents = new ArrayList<>();
            int metricIdx = 1;
            for (StringBuilder values : valuesList) {
                values.append("]"); // close json array
                String metric = fieldNames[metricIdx++];

                byte[] compressedJson = StringCompressor.compress(values.toString());
                String compressedJsonBase64 = Base64.getEncoder().encodeToString(compressedJson);

                documents.add(createDocument(parts, metric, compressedJsonBase64, minDate, maxDate));
            }

            solrCloudClient.add(documents);
            solrCloudClient.commit();

        } catch (IOException | SolrServerException e) {
            Logger.getLogger(SimpleSparkSolrMetricsImporter.class.getName()).warning(e.getMessage());
        }
    }

    /**
     * Create a single Solr document.
     * @param meta the file metadata.
     * @param metric the metric name.
     * @param base64GZippedValues The denormalized JSON KV values array. GZipped and Base64 encoded.
     * @return a Solr document.
     */
    private SolrInputDocument createDocument(FileMetadata meta, String metric, String base64GZippedValues, Date minDate, Date maxDate) {
        SolrInputDocument document = new SolrInputDocument();
        // do not use "NEW" here
        document.addField("id", UUID.randomUUID().toString());
        document.addField("process", meta.getProcessName());
        document.addField("host", meta.getHostName());
        document.addField("type", meta.getType());
        document.addField("metric", metric);
        document.addField("values", base64GZippedValues);
        document.addField("mindate", minDate);
        document.addField("maxdate", maxDate);
        return document;
    }

    /**
     * Extract information out of a filename.
     */
    private static final class FileMetadata {
        private String processName;
        private String type;
        private String hostName;

        FileMetadata(String fileUrl) {
            String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
            String[] splits = fileName.split("_");
            this.hostName = splits[0];
            this.processName = splits[1];
            this.type = splits[2];
        }

        String getProcessName() {
            return processName;
        }

        String getType() {
            return type;
        }

        String getHostName() {
            return hostName;
        }
    }
}
