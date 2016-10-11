package de.qaware.spark.importer.spark.util;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;

import java.io.Serializable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by weigend on 10.10.16.
 */
public final class PooledSolrClient {
    private static PooledSolrClient instance;

    public static int POOLSIZE = 4;

    private final BlockingQueue<SolrClient> queue = new ArrayBlockingQueue<SolrClient>(POOLSIZE);

    private PooledSolrClient(String zkHost, String collection) {
        for (int i = 0; i < POOLSIZE; i++) {
            final CloudSolrClient solrCloudClient = new CloudSolrClient.Builder().withZkHost(zkHost).build();
            solrCloudClient.setDefaultCollection(collection);
            queue.add(solrCloudClient);
        }
    }

    /**
     * Gets the instance.
     * @param zkHost the host.
     * @param collection the collection.
     * @return a instance.
     */
    public static synchronized PooledSolrClient getInstance(String zkHost, String collection) {
        if (instance == null) {
            instance = new PooledSolrClient(zkHost, collection);
        }
        return instance;
    }

    /**
     * Gests a client to Solr.
     * @return
     */
    public SolrClient getClient()  {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    public void takeBack(SolrClient solrCloudClient) {
        try {
            queue.put(solrCloudClient);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }
}
