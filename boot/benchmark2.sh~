#! /bin/bash

DIR=`pwd`
BASEDIR="`cd .. && pwd`"
LOGDIR="logs"
MAP="$BASEDIR/maps/scn1"
TEAM=""
TIMESTAMP_LOGS=""
CONFIGDIR="$DIR/config"

# Delete old logs
rm -f $LOGDIR/*.log

. functions.sh

if [ ! -z $1 ]; then
   MAP="$BASEDIR/maps/$1"
   echo Using map $MAP
fi

# Start the kernel
##################
KERNEL_OPTIONS="-c $CONFIGDIR/kernel.cfg --gis.map.dir=$MAP --kernel.logname=$LOGDIR/rescue.log $* --nomenu --autorun --nogui"
makeClasspath $BASEDIR/jars $BASEDIR/lib
java -Xmx1024m -cp $CP kernel.StartKernel $KERNEL_OPTIONS 2>&1 | tee $LOGDIR/kernel-out.log &
PIDS="$PIDS $!"

# Wait for the kernel to start
waitFor $LOGDIR/kernel-out.log "Listening for connections"


# Start Viewer
##############
TEAM_NAME_ARG="Benchmark Team"
if [ ! -z "$TEAM" ]; then
        TEAM_NAME_ARG="\"--viewer.team-name=$TEAM\"";
fi
java -Xmx256m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/sample.jar rescuecore2.LaunchComponents sample.SampleViewer -c $CONFIGDIR/viewer.cfg $TEAM_NAME_ARG $* 2>&1 | tee $LOGDIR/viewer-out.log &
PIDS="$PIDS $!"

# Wait for the viewer to start
waitFor $LOGDIR/viewer-out.log "connected"


# Start Simulators
##################
makeClasspath $BASEDIR/lib
java -Xmx256m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/misc.jar rescuecore2.LaunchComponents misc.MiscSimulator -c $CONFIGDIR/misc.cfg --nogui $* 2>&1 | tee $LOGDIR/misc-out.log &
PIDS="$PIDS $!"

java -Xmx256m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/traffic3.jar rescuecore2.LaunchComponents traffic3.simulator.TrafficSimulator --nogui -c $CONFIGDIR/traffic3.cfg $* 2>&1 | tee $LOGDIR/traffic-out.log &
PIDS="$PIDS $!"
   
#java -Xmx256m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/resq-fire.jar:$BASEDIR/oldsims/firesimulator/lib/commons-logging-1.1.1.jar rescuecore2.LaunchComponents firesimulator.FireSimulatorWrapper -c $CONFIGDIR/resq-fire.cfg $* 2>&1 | tee $LOGDIR/fire-out.log &
#PIDS="$PIDS $!"


#java -Xmx256m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/ignition.jar rescuecore2.LaunchComponents ignition.IgnitionSimulator -c $CONFIGDIR/ignition.cfg $* 2>&1 | tee $LOGDIR/ignition-out.log &
#PIDS="$PIDS $!"
    
java -Xmx256m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/collapse.jar rescuecore2.LaunchComponents collapse.CollapseSimulator -c $CONFIGDIR/collapse.cfg $* 2>&1 | tee $LOGDIR/collapse-out.log &
PIDS="$PIDS $!"
    
#java -Xmx256m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/clear.jar rescuecore2.LaunchComponents clear.ClearSimulator -c $CONFIGDIR/clear.cfg $* 2>&1 | tee $LOGDIR/clear-out.log &
#PIDS="$PIDS $!"

echo "Start your agents"
# Start Agents
##############

#java -Xmx256m -cp $CP rescuecore2.LaunchComponents sample.SampleFireBrigade*n sample.SampleAmbulanceTeam*n sample.SamplePoliceForce*n sample.SampleCentre*n -c $DIR/config/sample-agents.cfg 2>&1 | tee $LOGDIR/sample-out.log


waitFor $LOGDIR/kernel.log "Kernel has shut down" 30
kill $PIDS
