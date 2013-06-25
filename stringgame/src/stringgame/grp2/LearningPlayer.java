package stringgame.grp2;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import stringgame.Player;
import stringgame.grp2.pair;

//public class LearningPlayer extends Player {
//	final double alpha = 0.7f;
//	final double gamma = 0.7f;
//	Map<pair, Double> utils = new HashMap<pair, Double>();
//	ArrayList<pair> history;
//	Random rand;
//	double epsilon;
//
//	public LearningPlayer() {
//		rand = new Random();
//		history = new ArrayList<pair>();
//		epsilon = 0.8;
//	}
//
//	@Override
//	public String sense(String state, double reward) {
//		calculateUtilities(reward);
//		ArrayList<Integer> actions = new ArrayList<Integer>();
//		for (int i = 0; i < 9; i++) {
//			if (state.charAt(i) == '_') {
//				actions.add(i);
//			}
//		}
//		int action = getAction(state, actions);
//		// calculation of row and col needs to be checked
//		int col = action % 3 + 1;
//		int row = (action - action % 3 + 1) / 3 + 1;
//		// System.out.println("action " + action + " row " + row + " col " +
//		// col);
//		pair temp = new pair(state, action);
//		history.add(temp);
//		String back = "";
//		back += row + "," + col;
//		// System.out.println("My move: " + back);
//		
//		epsilon -= epsilon / 10.0;
//		
//		return back;
//	}
//
//	@Override
//	public void terminate(String state, double reward, boolean writeLearningData) {
//		// if(reward == 1){
//		// System.out.println("won!");
//		// }
//		calculateUtilities(reward);
//		epsilon = 0.8;
//		history = new ArrayList<pair>();
//	}
//
//	private void calculateUtilities(double reward) {
//		if (history.size() > 0) {
//			//int t = 0;
//			// for (int i = history.size() - 1; i < 0; i--) {
//			int i = history.size() - 1;
//			//t++;
//			String state = history.get(i).str;
//			Integer action = history.get(i).val;
//			pair temp = new pair(state, action);
//
//			// get max util of following actions
//			// what means s+1
//			String Nextstate = state;
//			Nextstate = replaceCharAt(Nextstate, action, 'x');
//			int index = 0;
//			double util = 0;
//			for (int a = 0; a < 9; a++) {
//				if (util < Get(Nextstate, a)) {
//					util = Get(Nextstate, a);
//					index = a;
//				}
//			}
//
//			// update
//			utils.put(temp, ((1.0 - alpha) * Get(state, action) + alpha
//					* (reward + gamma * util)));
//			// }
//		}
//	}
//
//	private static String replaceCharAt(String s, int pos, char c) {
//		StringBuffer buf = new StringBuffer(s);
//		buf.setCharAt(pos, c);
//		return buf.toString();
//	}
//
//	private int getAction(String state, ArrayList<Integer> actions) {
//		int action = 0;
//		double util = 0;
//		for (int i = 0; i < actions.size(); i++) {
//			int a = actions.get(i);
//			// only allowed actions
//			if (util < Get(state, a)) {
//				util = Get(state, a);
//				action = a;
//			}
//		}
//		int randomVal = rand.nextInt(actions.size());
//		double eps = rand.nextDouble();
//		if (eps < epsilon || util == 0) {
//			return actions.get(randomVal);
//		}
//		return action;
//	}
//
//	private Double Get(String state, Integer action) {
//		pair temp = new pair(state, action);
//		if (!utils.containsKey(temp)) {
//			return 0.0;
//		}
//		return utils.get(temp);
//	}
//}

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
		calculateUtilities(state, reward);
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
		calculateUtilities(state, reward);
		epsilon -= epsilon / 100000;
		alpha -= alpha / 10000;
		//lastAction = -1;
		if(rand.nextFloat() > 0.999993){
			String logFileName = "";
			logFileName += rand.nextFloat() + ".txt";
	        FileWriter fstream;
	        try {
	            fstream = new FileWriter(logFileName);
	        } catch (IOException e) {
	            e.printStackTrace();
	            return;
	        }
	        BufferedWriter out = new BufferedWriter(fstream);
	        
	        Object[] keys = utils.keySet().toArray();
	        for(int i = 0; i < keys.length; i++){
	        	for(int j = 0; j < 9; j++){
	        		try {
						out.write(keys[i] + ": " + j + " = " + utils.get(keys[i]).actionQ[j] + "\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	}
	        }
	        try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void calculateUtilities(String currentState, double reward) {
		if (lastAction != -1) {
			String state = lastState;
			Integer action = lastAction;

			// get max util of following actions
			// what means s+1
			String Nextstate = state;
			Nextstate = replaceCharAt(Nextstate, action, 'x');   // CORR: problem specific!
			// CORR: You need to compute the best action's utility from
			// the current state here, i.e. s_t, a_t = lastState/Action
			// s_t+1, r_t+1 = currentState, currentReward
			Nextstate = currentState;
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

	private static String replaceCharAt(String s, int pos, char c) {
		StringBuffer buf = new StringBuffer(s);
		buf.setCharAt(pos, c);
		return buf.toString();
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
