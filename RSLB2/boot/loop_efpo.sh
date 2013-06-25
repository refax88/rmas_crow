#!/bin/bash

. functions.sh
makeClasspath ../bin/ $RSL_SIM_PATH/lib $RSL_SIM_PATH/jars

efpo="java -Xmx1024m -cp $CP RSLBench.Launcher -c $DIR/config/efpo.cfg"

killall java;
killall xterm;

echo "Compiling the code ....";
cd  ../;
ant clean;
ant;
cd -;
echo "... done.";


echo "Running EFPO ....";
repeat=5;
costfac=100.0
for range in "500" "50000" "500000"
do
    for start in "20" "30" "50" 
    do
        for ((i=0; i<repeat; i+=1))
        do
                params="-ra $range -st $start -cf $costfac"
                echo "Starting episode $i with $params";

                cd ${RSL_SIM_PATH}/boot;
                xterm -T Simulator -e "./benchmark.sh" & 
                sleep 10;
                cd -;
                $efpo $params;
                killall java;
                killall xterm;
                rdir="results/loop_efpo_epi"$i"_range"$range"_start"$start;
                echo "Moving results to $rdir";
                mkdir $rdir;
                mv logs/*.dat $rdir/
                echo "Copying config files to $rdir";
                cp -r config $rdir/
                done
        done
done
