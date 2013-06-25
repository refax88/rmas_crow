#!/bin/bash

OUTTYPE=(burning burned destroyedArea utilityPerRound summedUtility)
DATA_COLUMNS=(3 4 6 7 8)

FILES="central_assignment_efpo_all_targets dsa0 dsa100 dsa10000"


for ((j=0;j<5;j++)); do
	OUTPUT="${OUTTYPE[${j}]}.gnuplot"
	COLUMN=${DATA_COLUMNS[${j}]}

	PREFIX="${OUTTYPE[${j}]}.prefix"

	cat $PREFIX > $OUTPUT
	echo "plot \\" >> $OUTPUT

	echo Creating ${OUTTYPE[${j}]} gnuplot to compare assignment algorithms.
	for i in $FILES; do
	   echo Handling $i
	   FILENAME="${i}.dat"
	   if [ $i == "dsa10000" ]; then
		  echo "\"../$FILENAME\" using 1:${COLUMN} with lines lw 4 ti \"$i\";" >> $OUTPUT
	   else
		  echo "\"../$FILENAME\" using 1:${COLUMN} with lines lw 4 ti \"$i\", \\" >> $OUTPUT
	   fi
	done
done
