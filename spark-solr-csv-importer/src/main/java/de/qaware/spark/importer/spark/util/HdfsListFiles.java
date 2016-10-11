package de.qaware.spark.importer.spark.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by weigend on 10.10.16.
 */
public final class HdfsListFiles {

    /**
     * List files in HDFS directory.
     * @param uri a uri.
     * @return a list of hdfs file uris.
     */
    public static List<String> listFiles(URI uri) {
        List<String> result = new ArrayList<>();
        FileSystem fs = null;
        try {
            fs = FileSystem.get(uri, new Configuration(true), "cloud");
            RemoteIterator<LocatedFileStatus> files = fs.listFiles(new Path(uri.getPath()), false);
            while (files.hasNext()) {
                LocatedFileStatus fileStatus = files.next();
                if (fileStatus.isFile()) {
                    String path = fileStatus.getPath().toUri().toString();
                    result.add(path);
                }

            }
        } catch (IOException|InterruptedException e) {
            throw new IllegalStateException(e);
        }
        return result;
    }
}
