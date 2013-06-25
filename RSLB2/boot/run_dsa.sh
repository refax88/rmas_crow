#! /bin/bash
RSL_SIM_PATH=../..
. functions.sh

processArgs $*

killall java
killall xterm

# Compile the code
echo "Compiling the code ....";
cd  ../;
ant clean;
ant;
cd -;
echo "... done.";

# Run simulator
cd ${RSL_SIM_PATH}/boot;
xterm -T Simulator -e "./benchmark.sh" &
sleep 10;
cd -;


makeClasspath ../bin/ $RSL_SIM_PATH/lib $RSL_SIM_PATH/jars

#echo $CP

java -Xmx1024m -cp $CP RSLBench.Launcher -c $DIR/config/dsa.cfg 
