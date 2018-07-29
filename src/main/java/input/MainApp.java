package input;

import model.SnapshotClusters;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import cluster.BasicClustering;
import cluster.ClusteringMethod;

import conf.AppProperties;

/**
 * reads input from HDFS and 
 * write clusters in each snapshots to HDFS
 * @author a0048267
 *
 */
public class MainApp {

	public static void main(String[] args) {
		int hdfs_partitions = 87;
		double eps = 15;
		int minpt = 10 ;
		int snapshot_partitions = 87;
		int M = minpt;
		int earth = 0;
		String master = null, input = null, output = null;
		if(args.length > 0) {
			for(String arg : args) {
				System.out.println(arg);
				if(arg.startsWith("h=")|| arg.startsWith("H=")) {
					hdfs_partitions = Integer.parseInt(arg.split("=")[1]);
				} else if(arg.startsWith("e=")|| arg.startsWith("E=")) {
					eps = Double.parseDouble(arg.split("=")[1]);
				} else if(arg.startsWith("m=")|| arg.startsWith("M=")) {
					minpt = Integer.parseInt(arg.split("=")[1]);
					M = minpt;
				} else if(arg.startsWith("s=")|| arg.startsWith("S=")) {
					snapshot_partitions = Integer.parseInt(arg.split("=")[1]);
				} else if(arg.startsWith("r=") || arg.startsWith("R=")) {
					earth = Integer.parseInt(arg.split("=")[1]);
				} else if(arg.startsWith("master=") || arg.startsWith("Master=")) {
					master = arg.split("=")[1];
				} else if(arg.startsWith("input=") || arg.startsWith("Input=")) {
					input = arg.split("=")[1];
				} else if(arg.startsWith("output=") || arg.startsWith("Output=")) {
					output = arg.split("=")[1];
				}
			}
		} else {
			System.out.println("No commandline arguments found. Using default values instead");
			System.out.println("Usage: .bin/spark-submit --class input.MainApp " +
					"~/TrajectoryMining/TrajectoryMining-0.0.1-SNAPSHOT-jar-with-dependencies.jar " +
					" e=15 p=10 h=87 s=174 r=1 master=local[4] input=/tmp/input.txt output=/tmp/output");
			System.out.println("Missing values are replaced by defaults!");
			System.exit(-1);
		}
		String hdfs_input = input;
		String hdfs_output = output;
		String name = String.format("DBSCAN-E=%f-PM=%d", eps, minpt);
		Logger.getLogger("org").setLevel(Level.OFF);
		Logger.getLogger("aka").setLevel(Level.OFF);


		SparkConf conf = new SparkConf().setAppName(name).setMaster(master);
		JavaSparkContext context = new JavaSparkContext(conf);
		JavaRDD<String> inputRDD = context.textFile(hdfs_input, hdfs_partitions);
		ClusteringMethod cm = new BasicClustering(eps, minpt, M, snapshot_partitions, earth);
		JavaRDD<SnapshotClusters> CLUSTERS = cm.doClustering(inputRDD);
		String hdfs_out = String.format(hdfs_output + "/clusters-e%f-m%d",
				eps, minpt);
		CLUSTERS.saveAsObjectFile(hdfs_out);
		context.close();
		System.out.println("===============FINISHED SUCCESSFULLY=========================================================");
	}

}
