package de.qaware.spark.importer.spark;

import de.qaware.spark.importer.spark.util.HdfsListFiles;
import org.junit.Test;
import sun.jvm.hotspot.utilities.Assert;

import java.net.URI;
import java.util.List;

/**
 * Created by weigend on 10.10.16.
 */
public class HdfsListFilesTest {
    @Test
    public void listFiles() throws Exception {
        List<String> files = HdfsListFiles.listFiles(new URI("hdfs://192.168.1.100:/csv-all"));
        Assert.that(!files.isEmpty(), "HDFS directory should not be empty.");
    }
}