#!/bin/bash

cat > /tmp/gnuplot_tmp.gnuplot <<EOF
set terminal postscript eps enhanced color solid defaultplex "Times-Roman" 20
#set terminal postscript enh color eps
set output "burned.eps"
set ylabel "# Buildings Burned"
#set xtics ("0" 0, "25" 25000 ,"50" 50000, "75" 75000, "100" 100000)
set xlabel "Time"
set key top left
#set key box
#set yrange [-0.5:0.5]
#set grid
set pointsize 3

plot \\
EOF

first=1
for i in $*; do
   name=$(basename $i .dat | tr _ " ")
   if [ $first -eq 1 ]; then
      echo -ne "\"$i\" using 1:4 with lines lw 4 ti \"$name\"" >> /tmp/gnuplot_tmp.gnuplot
      first=0
   else
      echo ", \\" >> /tmp/gnuplot_tmp.gnuplot
      echo -ne "\"$i\" using 1:4 with lines lw 4 ti \"$name\"" >> /tmp/gnuplot_tmp.gnuplot
   fi
done

echo ";" >> /tmp/gnuplot_tmp.gnuplot 

gnuplot /tmp/gnuplot_tmp.gnuplot
