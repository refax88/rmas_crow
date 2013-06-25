package sample;

import rescuecore2.worldmodel.EntityID;

public class NoHealthPredictor extends CivilianHealthPredictor
{

    @Override
    public void init()
    {
    }

    @Override
    public double getPrediction(EntityID civ, int time)
    {
        return 0;
    }

    @Override
    public void update(EntityID civ, int time, double hitpoints, double damage)
    {
    }

}
