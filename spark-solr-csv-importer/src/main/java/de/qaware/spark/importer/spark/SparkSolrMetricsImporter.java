package de.qaware.spark.importer.spark;

import de.qaware.spark.importer.MetricsImporter;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Imports Metric CSVs
 * Created by weigend on 20.09.16.
 */
public class
SparkSolrMetricsImporter implements MetricsImporter, Serializable {

    /**
     * The batch size for imports.
     */
    private static final int BATCH_SIZE = 100000;

    /**
     * The delimiter of the imported files.
     */
    private static final String DELIMITER = ";";


    private static final String COLLECTION_NAME = "ekgdata2";

    /**
     * The ZK_HOST.
     */
    private String zkHost;


    // Serialization requires this.
    public SparkSolrMetricsImporter() {
    }

    /**
     * Initializes this class with a Zookeeper Url for Solr Cloud and a Spark Master to run this code.
     */
    public SparkSolrMetricsImporter(String zkHost) {
        this.zkHost = zkHost;
    }

    /**
     * Parallel Solr import using Spark.
     *
     * @param pathUrl the URL could be a local file or path or a hdfs path.
     *                @see SparkContext#binaryFiles(String, int)  method.
     * @param jsc     The context.
     */
    public void importMetrics(String pathUrl, JavaSparkContext jsc) {

        // Get a collection of all files in the given path.
        JavaPairRDD<String, PortableDataStream> rdd = jsc.binaryFiles(pathUrl);

        // default: max tasks - full parallelism.
        jsc.parallelize(rdd.toArray()).foreach(new ImporterImpl()::importFile);

        // One task per file/segment
        // long totalCount = rdd.count();
        // rdd.repartition((int) totalCount).foreach(new ImporterImpl()::importFile);
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

        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS", Locale.US);

        // Cloud Client
        final CloudSolrClient solrCloudClient = new CloudSolrClient.Builder().withZkHost(zkHost).build();
        solrCloudClient.setDefaultCollection(COLLECTION_NAME);

        //HttpSolrClient solrLocalClient = new HttpSolrClient.Builder("http://localhost:8983/solr/ekgdata2").build();
        //solr = solrLocalClient;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileStream.open()), 1000000)) {
            String line;

            // assuming first line is a csv header
            // Caution: This only works with files < 128 MB / One Hadoop Block
            String firstLine = reader.readLine();
            String[] fieldNames = firstLine.split(DELIMITER);

            // split host/process/type information out of the filename
            FileNameParts parts = new FileNameParts(fileUrl);

            // loop over csv file, produce and add documents
            List<SolrInputDocument> documents = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                List<SolrInputDocument> docs = createDocumentFromLine(line, fieldNames, parts, dateFormat);
                documents.addAll(docs);
                if (documents.size() > BATCH_SIZE) {
                    solrCloudClient.add(documents);
                    documents.clear();
                }
            }
            if (!documents.isEmpty()) {
                solrCloudClient.add(documents); // add the rest (last chunk)
                solrCloudClient.commit();
            }

        } catch (IOException | SolrServerException e) {
            //Logger.getLogger(SparkSolrMetricsImporter.class.getName()).warning(e.getMessage());
            System.err.println(e.getMessage());
        }
    }

    private long id = 0;

    /**
     * Helper to import a single csv line.
     *
     * @param line       the line.
     * @param fieldNames the field names.
     * @param parts      infos about the filename.
     * @param dateFormat the date format.
     * @return a solr input document.
     */
    private List<SolrInputDocument> createDocumentFromLine(String line, String[] fieldNames, FileNameParts parts, DateFormat dateFormat) throws ParseException {
        NumberFormat format = new DecimalFormat();

        String[] values = line.split(DELIMITER);
        List<SolrInputDocument> docs = new ArrayList<>();
        // 1st field ist the date field
        for (int i = 1; i < fieldNames.length; i++) {
            SolrInputDocument document = new SolrInputDocument();
            // do not use "NEW" here
            document.addField("id", UUID.randomUUID().toString());
            document.addField("date", dateFormat.parse(values[0]));
            document.addField("process", parts.getProcessName());
            document.addField("host", parts.getHostName());
            document.addField("type", parts.getType());
            document.addField("metric", fieldNames[i]);
            document.addField("value", format.parse(values[i]));
            docs.add(document);
        }
        return docs;
    }

    /**
     * Extract information out of a filename.
     */
    private static final class FileNameParts {
        private String processName;
        private String type;
        private String hostName;

        public FileNameParts(String fileUrl) {
            String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
            String[] splits = fileName.split("_");
            this.hostName = splits[0];
            this.processName = splits[1];
            this.type = splits[2];
        }

        public String getProcessName() {
            return processName;
        }

        public String getType() {
            return type;
        }

        public String getHostName() {
            return hostName;
        }
    }
}
