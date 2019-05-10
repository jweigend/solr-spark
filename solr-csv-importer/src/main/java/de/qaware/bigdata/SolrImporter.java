/*
 _____________________________________________________________________________
 
            Project:    BigData
  _____________________________________________________________________________
  
         Created by:    Johannes Weigend, QAware GmbH
      Creation date:    April 2016
  _____________________________________________________________________________
  
          License:      Apache License 2.0
  _____________________________________________________________________________ 
 */
package de.qaware.bigdata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.common.SolrInputDocument;

public class SolrImporter {

    /**
     * Import files.
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws org.apache.solr.client.solrj.SolrServerException
     */
    public static void main(String[] args) throws IOException, SolrServerException {

        final String[] ZK_CONNECTION_STRING = {"192.168.1.100:2181","192.168.1.101:2181","192.168.1.102:2181","192.168.1.103:2181","192.168.1.104:2181"};
        List<String> strings = Arrays.asList(ZK_CONNECTION_STRING);


        CloudSolrClient.Builder builder = new CloudSolrClient.Builder(strings, Optional.empty());


        try (CloudSolrClient client =  builder.build()) {

            client.setDefaultCollection("bigdata");

            client.connect();

                // delete all
            client.deleteByQuery("*:*");
            client.commit();
 
            List<SolrInputDocument> docs = readCSV();

            // import ~ 100.000.000 Rows=
            for (int i = 0; i < 10000; i++) {
                client.add(docs);
                adjustIds(docs, i);
                if (i % 100 == 0) {
                    System.out.print(".");
                }
            }
            client.commit();

        }
    }

    /**
     * Simple reader which reads the marketing data from csv.
     *
     * @return a list of SolrInputDocuments. One Documentent for each row.
     * @throws IOException if a communication error occours.
     */
    private static List<SolrInputDocument> readCSV() throws IOException {
        InputStream csv = SolrImporter.class.getResourceAsStream("/WA_Fn-UseC_-Marketing-Customer-Value-Analysis.csv");
        BufferedReader reader = new BufferedReader(new InputStreamReader(csv));
        String header = reader.readLine();
        String[] fields = header.split(",");
        List<SolrInputDocument> docs = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            SolrInputDocument doc = new SolrInputDocument();
            String[] values = line.split(",");
            if (values.length != fields.length) {
                System.err.println("csv content is inconsistent to header");
                continue;
            }
            int i = 0;
            for (String value : values) {
                if (fields[i].equals("Total_Claim_Amount")) {
                    continue; // fix this
                }
                doc.addField(fields[i++], value);
            }
            docs.add(doc);
        }
        return docs;
    }

    /**
     * Helper to make unique ids.
     * @param docs the list of docs.
     * @param id postfix id.
     */
    private static void adjustIds(List<SolrInputDocument> docs, int id) {
        for (int i = 0; i < docs.size(); i++) {
            docs.get(i).setField("id", id * docs.size() + i);
        }
    }
}
