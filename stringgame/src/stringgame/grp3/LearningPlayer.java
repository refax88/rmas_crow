package stringgame.grp3;

import java.util.Random;

import stringgame.Player;

public class LearningPlayer extends Player
{
	double Q[][];
	static final double gamma = 0.9; //discount factor
	double epsilon = 0.7; // probability of random actions (decreasing) 
	double alpha = 1.0; // learning rate (decreasing)
	Random rand;
	String lastState;
	int lastAction = -1;
	int numOfGames = 0;

	public LearningPlayer() {
		Q = new double[27*27*27][9];
		rand = new Random();
	}
	
	@Override
    public String sense(String state, double reward) {
        // TODO Auto-generated method stub
		int action;
		if(rand.nextDouble() < epsilon){ //do random action => exploration
			do {
				action = rand.nextInt(9);
			} while(!isValidAction(state, action));
		} else { // greedy action => exploitation
			action = getBestValidAction(state);
		}		
		//Q-Update
		if(lastAction >= 0)
			Q[getStateIndex(lastState)][lastAction] = 
				(1.0-alpha) * Q[getStateIndex(lastState)][lastAction]
				 +   alpha  * (reward+gamma*getBestQValue(state));
		lastState = state;
		lastAction = action;
        return getActionString(action);
    }

    @Override
    public void terminate(String state, double reward, boolean writeLearningData) {
        // TODO Auto-generated method stub
    	Q[getStateIndex(lastState)][lastAction] = reward;
    	// CORR: This should be an update with the learning rate.
    	// For probabilistic actions this is not deterministic.
    	
    	//alpha *= 0.999994;
    	//epsilon *= 0.99999;
    	numOfGames++;
    	if(numOfGames % 1000 == 0){
        	alpha *= 0.993;
        	epsilon *= 0.99;
    	}
    	lastAction = -1;
    }
    
    static private int getStateIndex(String state) {
    	int index = 0;
    	for(int i=0; i<9; i++){
    		index *= 3;
    		switch(state.charAt(i)){
    			case 'x': index += 1; break;
    			case 'o': index += 2; break;    		
    		}
    	}
    	return index;
    }
    static private String getActionString(int a){
    	return ""+((a/3)+1)+","+((a%3)+1);
    }
    
    private double getBestQValue(String state){
    	int index = getStateIndex(state);
    	double maxValue = -1e20;
    	for(int a=0; a<9; a++){
    		if(isValidAction(state,a) && Q[index][a] > maxValue){
    			maxValue = Q[index][a];
    		}
    	}
    	return maxValue;    	
    }
    
    private int getBestValidAction(String state){
    	int index = getStateIndex(state);
    	double maxValue = -1e20;
    	int bestAction = 0;
    	for(int a=0; a<9; a++){
    		if(isValidAction(state,a) && Q[index][a] > maxValue){
    			maxValue = Q[index][a];
    			bestAction = a;
    		}
    	}
    	return bestAction;    	
    }
    
    static private boolean isValidAction(String state, int action){
    	return state.charAt(action) == '_';
    }

}
