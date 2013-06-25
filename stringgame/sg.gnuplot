#!/bin/sh 
gnuplot -persist << EOF

set yrange [-5 : 1005]

#plot "stats.dat" using 0:1 with lines ti "xWin", "stats.dat" using 0:2 with lines ti "oWin", "stats.dat" using 0:3 with lines ti "draw"
#plot "stats_solutions.dat" using 0:1 with lines ti "xWin", "stats_solutions.dat" using 0:2 with lines ti "oWin", "stats_solutions.dat" using 0:3 with lines ti "draw"
plot "stats_$1.dat" using 0:1 with lines ti "xWin", "stats_$1.dat" using 0:2 with lines ti "oWin", "stats_$1.dat" using 0:3 with lines ti "draw"

EOF
