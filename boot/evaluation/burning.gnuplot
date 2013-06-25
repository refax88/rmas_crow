set terminal postscript eps enhanced color solid defaultplex "Times-Roman" 20
#set terminal postscript enh color eps
set output "burning.eps"
set ylabel "# Buildings Burning"
#set xtics ("0" 0, "25" 25000 ,"50" 50000, "75" 75000, "100" 100000)
set xlabel "Time"
set key top left
#set key box
#set yrange [-0.5:0.5]
#set grid
set pointsize 3

plot \
"../central_assignment_efpo_all_targets.dat" using 1:3 with lines lw 4 ti "central_assignment_efpo_all_targets", \
"../dsa0.dat" using 1:3 with lines lw 4 ti "dsa0", \
"../dsa100.dat" using 1:3 with lines lw 4 ti "dsa100", \
"../dsa10000.dat" using 1:3 with lines lw 4 ti "dsa10000" ,\
"../central_assignment_hungarian.dat" using 1:3 with lines lw 4 ti "central_assignment_hungarian"
