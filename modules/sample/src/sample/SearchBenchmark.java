package sample;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import rescuecore2.log.Logger;
import rescuecore2.worldmodel.EntityID;

public class SearchBenchmark implements SearchAlgorithm
{
    private SearchAlgorithm primarySearch;
    private SearchAlgorithm secondarySearch;
    
    public SearchBenchmark(SearchAlgorithm primary, SearchAlgorithm secondary)
    {
        primarySearch = primary;
        secondarySearch = secondary;
    }
    
    @Override
    public List<EntityID> search(EntityID start, EntityID goal, Graph graph, DistanceInterface distanceMatrix)
    {
        return null;
    }

    @Override
    public List<EntityID> search(EntityID start, Collection<EntityID> goals, Graph graph, DistanceInterface distanceMatrix)
    {
        graph.resetAccessCount();
        Logger.debugColor("primarySearchAlgorithm: "+ primarySearch.getClass().getName(), Logger.BG_RED);
        Logger.debugColor("secondarySearchAlgorithm: "+ secondarySearch.getClass().getName(), Logger.BG_YELLOW);
        List<EntityID> path = primarySearch.search(start, goals, graph, distanceMatrix);
        int primaryNumberOfExpansions = graph.getAccessCount();
        int primaryPathLength = computePathLength(path, distanceMatrix);
        graph.resetAccessCount();
        List<EntityID> secondaryPath = secondarySearch.search(start, goals, graph, distanceMatrix);
        int secondaryNumberOfExpansions = graph.getAccessCount();
        int secondaryPathLength = computePathLength(secondaryPath, distanceMatrix);
        if (primaryPathLength != secondaryPathLength)
        {
            Logger.debugColor("PATHS DIFFERENT LENGTH: " + primaryPathLength + " / " + secondaryPathLength, Logger.BG_BLUE);
        }
        SearchStatistics.addSearch(primaryPathLength, secondaryPathLength, primaryNumberOfExpansions, secondaryNumberOfExpansions);
        return path;
    }

    private int computePathLength(List<EntityID> path, DistanceInterface distanceMatrix)
    {
        if (path != null)
        {
            if (path.size() >= 2)
            {
                EntityID previous;
                EntityID current;
                int pathLength = 0;
                Iterator<EntityID> it = path.iterator();
                current = it.next();
                while (it.hasNext())
                {
                    previous = current;
                    current = it.next();
                    pathLength += distanceMatrix.getDistance(previous, current);
                }
                return pathLength;
            }
        }
        return 0;
    }
}
