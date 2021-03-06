package cluster;

import java.io.Serializable;
import model.SnapshotClusters;
import org.apache.spark.api.java.JavaRDD;

public interface ClusteringMethod extends Serializable{
    public JavaRDD<SnapshotClusters> doClustering(JavaRDD<String> input);
}
