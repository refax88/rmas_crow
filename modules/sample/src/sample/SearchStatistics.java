package sample;

import java.util.ArrayList;
import java.util.List;

public class SearchStatistics
{
    private List<Double> shortestPathRelation;
    private List<Double> expansionCountRelation;
    
    private static SearchStatistics instance = null;
    
    private SearchStatistics()
    {
        shortestPathRelation = new ArrayList<Double>();
        expansionCountRelation = new ArrayList<Double>();
    }
    
    private static void instantiate()
    {
        if (instance == null)
        {
            instance = new SearchStatistics();
        }
    }
    
    public static void addSearch(int grpPathLength, int solutionsPathLength, int grpExpansionCount, int solutionsExpansionCount)
    {
        instantiate();
        instance.shortestPathRelation.add((double)grpPathLength / (double)solutionsPathLength);
        instance.expansionCountRelation.add((double)grpExpansionCount / (double)solutionsExpansionCount);
    }
    
    public static String print()
    {
        instantiate();
        return instance.toString();
    }

    public String toString()
    {
        if (expansionCountRelation.isEmpty())
        {
            StringBuffer buffer = new StringBuffer();
            buffer.append(" searchCount: ").append(shortestPathRelation.size());
            double meanShortestPathRelation = 0;
            for (double number : shortestPathRelation)
            {
                meanShortestPathRelation += number;
            }
            meanShortestPathRelation /= shortestPathRelation.size();
    
            double meanExpansionCountRelation = 0;
            for (double number : expansionCountRelation)
            {
                meanExpansionCountRelation += number;
            }
            meanExpansionCountRelation /= expansionCountRelation.size();
    
            buffer.append(" meanPathLengthRelation: ").append(meanShortestPathRelation);
            buffer.append(" meanExpansionRelation: ").append(meanExpansionCountRelation);
            return buffer.toString();
        }
        return "";
    }
}
