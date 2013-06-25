#!/bin/bash

algos="../central_assignment_hungarian.dat ../central_assignment_efpo_all_targets.dat"

if [ $# -ge 1 ]; then
   algos=$*
fi

names="burning burned destroyedArea utilityPerRound summedUtility"

for i in $names; do
   echo doing $i
   script=$i.sh
   eps=$i.eps
   echo algos: $algos
   ./$script $algos
   epstopdf $eps
done

rm *.eps

