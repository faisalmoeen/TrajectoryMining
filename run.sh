#!/usr/bin/env bash


DATASETS=(trucks273s.txt brink.txt tDrive.txt)
CLUSTERING_EXP_RESULT=clustering_exps.txt
SPARE_EXP_RESULT=spare_exps.txt
CORES=(1 2 3 4 5 6 7 8)
CLUSTERING_IN_DIR=/Users/faisal/phd/data
CLUSTERING_OUT_DIR=/Users/faisal/phd/output


#===============================================Clustering Phase============================================

E=(0.000600) #eps for DBSCAN
M=(5) #min num of objs in a cluster
H=(87) #hdfs partitions
S=(87) #snapshot partitions
r=0 #use earth distance calc = false


rm ${CLUSTERING_EXP_RESULT}
touch ${CLUSTERING_EXP_RESULT}
echo "dataset,e,m,cores,time(ms)" >> ${CLUSTERING_EXP_RESULT}

for dataset in ${DATASETS[0]}
do
for core in ${CORES[@]}
do
for m in ${M[@]}
do
for e in ${E[@]}
do
    start=$(gdate +%s%3N)
    echo $dataset,${e},${m}
    rm -r ${CLUSTERING_OUT_DIR}/${dataset}/clusters-e${e}-m${m}
    java -Xmx10g -cp target/TrajectoryMining-0.0.1-SNAPSHOT.jar input.MainApp e=${e} m=${m} master=local[${core}] \
        input=${CLUSTERING_IN_DIR}/${dataset} output=${CLUSTERING_OUT_DIR}/${dataset}
    end=$(gdate +%s%3N)
    echo $dataset,${e},${m},${core},$(($end-$start))
    echo "$dataset,${e},${m},${core},$(($end-$start))" >> ${CLUSTERING_EXP_RESULT}
done
done
done
done

# ================================Trajectory Mining==============================


rm ${SPARE_EXP_RESULT}
touch ${SPARE_EXP_RESULT}
echo "dataset,k,l,m,g,c,cores,time(ms)" >> $SPARE_EXP_RESULT

CORES=(1 2 3 4 5 6 7 8)
#K=(90 180 270 360)
K=(180)
L=(180) #length of local consecutive pattern
M=(5)
G=(0) # gap
C=(115) # Num of clique miner partitions


for dataset in ${DATASETS[0]}
do
for c in ${C[@]}
do
for k in ${K[@]}
do
for l in ${L[@]}
do
for m in ${M[@]}
do
for e in ${E[@]}
do
for g in ${G[@]}
do
for core in ${CORES[@]}
do
    start=$(gdate +%s%3N)
    echo ${dataset},${k},${l},${m},${g},${c}
    java -Xmx10g -cp target/TrajectoryMining-0.0.1-SNAPSHOT.jar apriori.MainSP k=${k} l=${l} m=${m} g=${g} c=${c}\
        input=${CLUSTERING_OUT_DIR}/${dataset}/clusters-e${e}-m${m} master=local[$core]
    end=$(gdate +%s%3N)
    echo ${dataset},${k},${l},${m},${g},${c},$(($end-$start))
    echo "${dataset},${k},${l},${m},${g},${c},${core},$(($end-$start))" >> $SPARE_EXP_RESULT
done
done
done
done
done
done
done
done