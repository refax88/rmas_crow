package rescuecore2.components;

/**
 * Agents that need global memory access should implement this interface.
 */
public interface GlobalMemoryAccess {
    void setGlobalMemory(GlobalMemory g); 
}
