#!/bin/bash

. functions.sh
makeClasspath ../bin/ $RSL_SIM_PATH/lib $RSL_SIM_PATH/jars


dsa="java -Xmx1024m -cp $CP RSLBench.Launcher -c $DIR/config/dsa.cfg"
efpo="java -Xmx1024m -cp $CP RSLBench.Launcher -c $DIR/config/efpo.cfg"

killall java;
killall xterm;
 
echo "Compiling the code ....";
cd  ../;
ant clean;
ant;
cd -;
echo "... done.";


echo "Running DSA ....";
repeat=10;
costfac=100.0
for ((i=0; i<repeat; i+=1))
do
        for range in "30000" "90000"
        do
                for start in "20" "45"
                do
                params="-ra $range -st $start -cf $costfac"
                echo "Starting episode $i with $params";

                cd ${RSL_SIM_PATH}/boot;
                xterm -T Simulator -e "./benchmark.sh" & 
                sleep 10;
                cd -;
                $dsa $params;
                killall java;
                killall xterm;
                rdir="results/loop_dsa_epi"$i"_range"$range"_start"$start;
                echo "Moving results to $rdir";
                mkdir $rdir;
                mv logs/*.dat $rdir/
                done
        done
done
