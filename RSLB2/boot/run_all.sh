#!/bin/bash

repeat=1;
tifffile=${PURSUIT_EVASION}/maps/haiti.tiff

algos=run_da_efpo.sh,run_dsa.sh


echo "Compiling the code ....";
cd  ../;
ant;
cd -;
echo "... done.";

for ((i=0; i<repeat; i+=1))
do
    for algo in ./run_dsa.sh ./run_da_efpo.sh ./run_ca_efpo.sh
        do
           echo "Starting $algo run $i ";
           cd ${RSL_SIM_PATH}/boot;
            xterm -T Simulator -e "./benchmark.sh" & 
#            xterm -T Simulator -e "./benchmark_noview.sh" & 
           sleep 10;
           cd -;
           $algo;
           killall java;
           killall xterm;
        done
        DTSTAMP=$(date "+%Y_%b_%d_%a_%H:%M::%S")
        rdir=results/$DTSTAMP;
        echo "Moving results to $rdir";
        mkdir $rdir;
        mv logs/*.dat $rdir/
        echo "Copying config files to $rdir";
        cp -r config $rdir/
        echo "Performing evaluation";
done
