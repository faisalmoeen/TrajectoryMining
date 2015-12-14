package baseline;

import java.util.ArrayList;
import java.util.Map;

import model.Cluster;
import model.GroupClusters;
import model.Pattern;
import model.SnapShot;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import scala.Tuple2;
import conf.AppProperties;

public class BaselineLayout {
    protected final JavaSparkContext jsc;
    private final int K, L, M, G, group_partition, eps, minPts;
    private final int hdfs_read_partitions, snapshot_partitions;
    private final String hdfs_input;

    public BaselineLayout(JavaSparkContext context) {
	jsc = context;
	K = Integer.parseInt(AppProperties.getProperty("K"));
	L = Integer.parseInt(AppProperties.getProperty("L"));
	M = Integer.parseInt(AppProperties.getProperty("M"));
	G = Integer.parseInt(AppProperties.getProperty("G"));
	group_partition = Integer.parseInt(AppProperties
		.getProperty("group_partition"));
	eps = Integer.parseInt(AppProperties.getProperty("eps"));
	minPts = Integer.parseInt(AppProperties.getProperty("minPts"));
	snapshot_partitions = Integer.parseInt(AppProperties
		.getProperty("snapshot_partitions"));
	hdfs_read_partitions = Integer.parseInt(AppProperties
		.getProperty("hdfs_read_partitions"));
	hdfs_input = AppProperties.getProperty("hdfs_input");
    }

    private JavaRDD<String> readInput() {
	return jsc.textFile(hdfs_input, hdfs_read_partitions);
    }

    private JavaPairRDD<Integer, SnapShot> genSnapshots(JavaRDD<String> inputs) {
	return inputs.filter(new TupleFilter())
		.mapToPair(new SnapshotGenerator())
		.reduceByKey(new SnapshotCombinator(), snapshot_partitions);
    }

    public void runLogic() {
	JavaRDD<String> rawFiles = readInput();
	JavaPairRDD<Integer, SnapShot> snapshots = genSnapshots(rawFiles);
	JavaRDD<ArrayList<Cluster>> clusters = snapshots.map(new DBSCANWrapper(
		eps, minPts));
	// then we group proximate clusters together
	// first get the max and min ts
	Tuple2<Integer, SnapShot> start = snapshots
		.max(SnapshotTSComparator.maxComp);
	Tuple2<Integer, SnapShot> end = snapshots
		.max(SnapshotTSComparator.minComp);
	JavaPairRDD<Integer, GroupClusters> groupedCluster = clusters
		.flatMapToPair(
			new OverlapPartitioner(start._1, end._1, group_partition, L)).groupByKey()
		.mapValues(new ClusterGrouper(8));

	JavaPairRDD<Integer, ArrayList<Pattern>> local_patterns = groupedCluster
		.mapValues(new LocalCMCMiner(M, L, G, K));
//	local_patterns.collect();
	// we also need to compute the object->temporal list
	JavaPairRDD<Integer, ArrayList<Tuple2<Integer, String>>> object_temporal_list = 
		clusters.flatMapToPair(new ObjectTemporalMap()).groupByKey().mapValues(new TupleToList());
	
	//then for each local patterns, we check whether it is global pattern
	local_patterns.map(new PatternCheck(object_temporal_list, M, L, G, K));
	Map<Integer, ArrayList<Pattern>> global_patterns = local_patterns.collectAsMap();
	for(ArrayList<Pattern> gp : global_patterns.values()) {
	    System.out.println(gp);
	}
    }
}
