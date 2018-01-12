# TrajectoryMining

This is the fork of the SPARE framework which discovers General Co-movement Patterns from Large-scale Trajectories.

The original repository serves the experimentary codes for the paper that was published in VLDB'17. 

This repository improves upon the original code base and adds the experimental harness and some documentation.

## Clustering
```
.bin/spark-submit --class input.MainApp \
~/TrajectoryMining/TrajectoryMining-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
e=15 p=10 h=87 s=174 r=1 \
master=local[4] \
input=/tmp/input.txt \
output=/tmp/output
```
