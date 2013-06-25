#!/bin/bash

TASKNAME=$1
OUTTYPE=(burning burned destroyedArea summedUtility utilityPerRound)
DATA_COLUMNS=(3 4 6 7 8)

GROPS="grp1 grp2 grp3 grp4 solutions"

for ((j=0;j<5;j++)); do
	OUTPUT="${OUTTYPE[${j}]}_grps.gnuplot"
	COLUMN=${DATA_COLUMNS[${j}]}

	PREFIX="${OUTTYPE[${j}]}.prefix"

	cat $PREFIX > $OUTPUT
	echo "plot \\" >> $OUTPUT

	echo Creating ${OUTTYPE[${j}]} gnuplot for $TASKNAME
	for i in $GROPS; do
	   echo Handling $i
	   FILENAME="${i}_${TASKNAME}.dat"
	   if [ $i == "solutions" ]; then
		  echo "\"../$FILENAME\" using 1:${COLUMN} with lines lw 4 ti \"$i\";" >> $OUTPUT
	   else
		  echo "\"../$FILENAME\" using 1:${COLUMN} with lines lw 4 ti \"$i\", \\" >> $OUTPUT
	   fi
	done
done
