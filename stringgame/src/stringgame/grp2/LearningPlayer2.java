package stringgame.grp2;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import stringgame.Player;

public class LearningPlayer extends Player {
	class Q {
		double[] actionQ = new double[9];

		public Q() {
			for (int i = 0; i < actionQ.length; i++) {
				actionQ[i] = 1;
			}
		}

		public void Set(int action, double Q) {
			actionQ[action] = Q;
		}
	}

	double alpha = 0.5f;
	final double gamma = 0.9f;
	Map<String, Q> utils = new HashMap<String, Q>();
	String lastState;
	int lastAction;
	Random rand;
	double epsilon;

	public LearningPlayer() {
		rand = new Random();
		epsilon = 1.0;
		lastAction = -1;
	}

	@Override
	public String sense(String state, double reward) {
		//System.out.println("My reward: " + reward);
		calculateUtilities(reward,state);
		ArrayList<Integer> actions = new ArrayList<Integer>();
		for (int i = 0; i < 9; i++) {
			if (state.charAt(i) == '_') {
				actions.add(i);
			}else{
				if(utils.containsKey(state)){
					Q q = utils.get(state);
					q.actionQ[i] = Double.NEGATIVE_INFINITY;
					utils.put(state, q);
				}else{
					Q q = new Q();
					q.actionQ[i] = Double.NEGATIVE_INFINITY;
					utils.put(state, q);
				}
			}
		}
		int action = getAction(state, actions);

		lastState = state;
		lastAction = action;
		
		// calculation of row and col needs to be checked
		int col = action % 3 + 1;
		int row = (action - action % 3 + 1) / 3 + 1;

		String back = "";
		back += row + "," + col;
		// System.out.println("My move: " + back);

		return back;
	}

	@Override
	public void terminate(String state, double reward, boolean writeLearningData) {
		calculateUtilities(reward, state);
		epsilon -= epsilon / 100000;
		alpha -= alpha / 10000;
		lastAction = -1;
	}

	private void calculateUtilities(double reward, String currentstate) {
		if (lastAction != -1) {
			String state = lastState;
			Integer action = lastAction;

			// get max util of following actions
			String Nextstate = currentstate;
			double util = Double.NEGATIVE_INFINITY;
			for (int a = 0; a < 9; a++) {
				if (util < Get(Nextstate, a)) {
					util = Get(Nextstate, a);
				}
			}

			// update
			Q update;
			if (utils.containsKey(state)) {
				update = utils.get(state);
			} else {
				update = new Q();
			}
			update.Set(action, ((1.0 - alpha) * Get(state, action) + alpha
					* (reward + gamma * util)));

			utils.put(state, update);
		}
	}

	private int getAction(String state, ArrayList<Integer> actions) {
		int action = -1;
		double util = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < actions.size(); i++) {
			int a = actions.get(i);
			// only allowed actions
			if (util < Get(state, a)) {
				util = Get(state, a);
				action = a;
			}
		}
		int randomVal = rand.nextInt(actions.size());
		double eps = rand.nextDouble();
		if (eps < epsilon || action == -1) {
			return actions.get(randomVal);
		}
		return action;
	}

	private Double Get(String state, Integer action) {
		// pair temp = new pair(state, action);
		if (!utils.containsKey(state)) {
			return 1.0;
		}
		return utils.get(state).actionQ[action];
	}
}
