package sample;

import java.util.Random;

import rescuecore2.worldmodel.EntityID;

public class CivilianUpdate
{
    private static Random rng = null;
    
    private EntityID id;
    private double hitPoints;
    private double damage;
    private double realHitPoints;
    private double realDamage;
    private int time;
    
    public CivilianUpdate(int id, int hitPoints, int damage, int time)
    {
        if(rng == null)
        {
            rng = new Random();
        }
        this.id = new EntityID(id);
        this.realHitPoints = hitPoints;
        this.hitPoints = addGaussianNoise(hitPoints, 0.05);
        this.realDamage = damage;
        this.damage = addGaussianNoise(damage, 0.2);
    }
    
    public EntityID getID()
    {
        return id;
    }
    public double getHitPoints()
    {
        return hitPoints;
    }
    public double getDamage()
    {
        return damage;
    }
    
    public int getTime()
    {
        return time;
    }
    
    public double getRealHitPoints()
    {
        return realHitPoints;
    }

    public double getRealDamage()
    {
        return realDamage;
    }

    private double addGaussianNoise(int value, double noiseFactor)
    {
        return value + rng.nextGaussian() * value * noiseFactor;
    }
}
