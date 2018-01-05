# TrajectoryMining
This project is about discovering General Co-movement Pattern from Large-scale Trajectories.

To facililate the large-scale needs, we use Apache-Spark as our solution platform. 
The current version of the project serves as the experimentary codes for our paper that were be published in VLDB'17. 

## Clustering
```
.bin/spark-submit --class input.MainApp \
~/TrajectoryMining/TrajectoryMining-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
e=15 p=10 h=87 s=174 r=1 \
master=local[4] \
input=/tmp/input.txt \
output=/tmp/output
```
