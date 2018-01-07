#!/usr/bin/env bash


#Clustering

# Trajectory Mining
rm results.csv
CORES=(1 2 3 4 5 6 7 8)
K=(90 180 270 360)
L=(180)
G=(0)
H=(195)
E=(0.006)
P=(10)
C=(115)
S=(115)
for c in ${CORES[@]}
do
    start=$(gdate +%s%3N)
    echo $c
    java -Xmx10g -cp target/TrajectoryMining-0.0.1-SNAPSHOT.jar apriori.MainSP k=${K[1]} l=${L[0]} g=${G[0]} h=${G[0]} e=${E[0]} p=${P[0]} c=${C[0]} s=${S[0]} input=/Users/faisal/phd/data/output/trucks/clusters-e15-p10 master=local[$c]
    end=$(gdate +%s%3N)
    echo trucks,${K[1]},${L[0]},${G[0]},${H[0]},${E[0]},${P[0]},${C[0]},${S[0]},$(($end-$start))
    echo "trucks,${K[1]},${L[0]},${G[0]},${H[0]},${E[0]},${P[0]},${C[0]},${S[0]},%c,$(($end-$start))\n" >> results.csv
done