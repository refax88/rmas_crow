package sample;

import java.util.Random;

import rescuecore2.components.GlobalMemory;

public class DummyGlobalMemory implements GlobalMemory
{
    int randomId;
 
    public DummyGlobalMemory() {
        randomId = (new Random()).nextInt();
    }
    void printSomething(int id) {
        System.out.println("Test Dummy Random: " + randomId + " in agent: " + id);
    }
}
