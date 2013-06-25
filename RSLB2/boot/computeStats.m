# run with octave3.2 or matlab

starts = ["10"; "20"; "25"; "30"; "35"; "40"; "45"; "50"];
ranges = ["0"; "30000"; "40000"; "50000"; "60000"; "70000"; "80000"; "90000" ];
episodes = ["1"; "2"; "3"; "4"; "5"; "6"; "7"; "8"; "9"; "10" ];

printf("range \tStart  \tAlgo \tMin\tMax\tMean\tStd\n");
for s=1:length(starts(:,1))
    for r=1:length(ranges(:,1))
        count = 0;
        VAL1 = [0 0 0 0 0 0 0 0 0 0 ];
        VAL2 = [0 0 0 0 0 0 0 0 0 0 ];
        for e=1:length(episodes(:,1))
            file1 = strcat("loop_efpo_epi", episodes(e,:), "_range", ranges(r,:), "_start", starts(s,:), "/RSLBench_AAMAS12_DA_EFPO.dat);
            file2 = strcat("loop_dsa_epi", episodes(e,:), "_range", ranges(r,:), "_start", starts(s,:), "/RSLBench_AAMAS12_DSA.dat);

            if (exist (file1, "file"))        
                if (exist (file2, "file"))
                    M1=dlmread(file1," ");
                    M2=dlmread(file2," ");
                    VAL1(count) = max(M1(:6));
                    VAL2(count) = max(M2(:6));
                    count = count +1;
            end
        end
        printf("%1.2lf \t %d \t DSA \t %1.2lf   %1.2lf  %1.2lf  %1.2lf  EFPO:   %1.2lf  %1.2lf  %1.2lf  %1.2lf", 
              r, s, mean(VAL1), std(VAL1), min(VAL1), max(VAL1), mean(VAL2), std(VAL2), min(VAL2), max(VAL2));
    end
end
