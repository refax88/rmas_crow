package sample;

import rescuecore2.worldmodel.EntityID;
import Jama.Matrix;

/**
 * Predict hitpoints and damage from previous observations.
 */
public abstract class CivilianHealthPredictor
{
    /**
     * Init data structures such as transition matrix, etc. 
     */
    public abstract void init();
    
    /**
     * Predict the hitpoints of civ at time given the current estimates.
     * Does NOT change the internal state!
     */
    public abstract double getPrediction(EntityID civ, int time);
    
    /**
     * Update the internal filter by this measurement.
     * 
     * Civilian civ was seen at time with these hitpoints and damage.
     * Only newer measurements should be integrated.
     * 
     * Performs Kalman filter predict step from last seen time to time and then
     * performs update step for measurement (hitpoints, damage).
     */
    public abstract void update(EntityID civ, int time, double hitpoints, double damage);

    /**
     * Compute the prediction noise matrix.
     * @param deltaTime - the time difference between the last integration and the time to predict to
     * @param hitpoints - the hitpoints before prediction
     * @param damage - the damage before prediction
     */
    protected final Matrix computePredictNoise(int deltaTime, double hitpoints, double damage) {
        double scaledHp = hitpoints * deltaTime;
        double scaledDam = damage * deltaTime;
        Matrix ret = new Matrix(new double[][]
            {{scaledHp * SIGMA2_PREDICT_HP, 0.0},
             {0.0, scaledDam * SIGMA2_PREDICT_DAMAGE}});
        return ret;
    }

    /**
     * Compute the update noise matrix.
     * @param hitpoints - the hitpoints before update
     * @param damage - the damage before update
     */
    protected final Matrix computeUpdateNoise(double hitpoints, double damage) {
        Matrix ret = new Matrix(new double[][]
            {{(1.0 + hitpoints) * SIGMA2_UPDATE_HP, 0.0},
             {0.0, (1.0 + damage) * SIGMA2_UPDATE_DAMAGE}});
        return ret;
    }
    
    /**
     * Noise values as percentage from measured value, e.g.
     * hitpoints = 10 -> sigma_hp^2 = 10*0.06
     */                                                                           
    static final double SIGMA2_UPDATE_HP = 0.06;
    static final double SIGMA2_UPDATE_DAMAGE = 0.25;

    /**
     * Noise values as percentage per round, e.g.
     * damage = 20, round 10 ->
     * round 20: sigma_dam^2 = 20 * 0.08 * 10.0
     * hp = 10000, round 10 ->
     * round 20: hp
     */
    static final double SIGMA2_PREDICT_HP = 0.1;
    static final double SIGMA2_PREDICT_DAMAGE = 0.08;
}
