#! /bin/bash

. functions.sh

#processArgs $*

makeClasspath ../bin/ $RSL_SIM_PATH/lib $RSL_SIM_PATH/jars

#echo $CP

-ra 10 -st 10 -cf 100.0

java -Xmx1024m -cp $CP RSLBench.Launcher -c $DIR/config/dsa.cfg $*
