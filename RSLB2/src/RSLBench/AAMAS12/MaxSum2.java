/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.AAMAS12;

import RSLBench.Assignment.Assignment;
import RSLBench.Assignment.DecentralAssignment;
import RSLBench.Comm.AbstractMessage;
import RSLBench.Comm.ComSimulator;

import RSLBench.Helpers.UtilityMatrix;
import java.util.Collection;
import java.util.HashSet;
import rescuecore2.worldmodel.EntityID;
import java.util.LinkedList;
import maxsum.Agent;

import messages.MessageQ;
import messages.MessageR;

import exception.PostServiceNotSetException;
import exception.VariableNotSetException;
import messages.MailMan;
import messages.MessageFactory;
import messages.MessageFactoryArrayDouble;
import factorgraph.NodeVariable;
import factorgraph.NodeFunction;
import factorgraph.NodeArgument;
import function.TabularFunction;
import java.util.*;
import operation.*;
import java.util.PriorityQueue;

/**
 *
 * @authors Fabio Maffioletti, Riccardo Reffato
 */
public class MaxSum2 implements DecentralAssignment {

    private static UtilityMatrix _utilityM = null;
    private EntityID _agentID;
    private EntityID _targetID = Assignment.UNKNOWN_TARGET_ID;
    private static MailMan _com = new MailMan();
    private Agent _maxSumAgent;
    private static MessageFactory _mfactory = new MessageFactoryArrayDouble();
    private static OTimes _otimes = new OTimes_MaxSum(_mfactory);
    private static OPlus _oplus = new OPlus_MaxSum(_mfactory);
    private static double _latestValue_start = Double.NEGATIVE_INFINITY;
    private static Operator _op = new MSumOperator(_otimes, _oplus);
    private static HashSet<NodeVariable> _variables = new HashSet<NodeVariable>();
    private static HashSet<NodeFunction> _functions = new HashSet<NodeFunction>();
    private static final int _targetPerAgent = 20;//numero di funzioni per agente
    private static int _dependencies = 5; //numero di variabili per funzione massimo
    private static int _maxFunPerVar = 4; //dominio massimo
    public static boolean toReset = false;
    private static HashMap<EntityID, ArrayList<EntityID>> _varToFunction;
    private static int _initializedAgents = 0;
    
    private int _sizeMex;
    
    @Override
    public void initialize(EntityID agentID, UtilityMatrix utilityM) {
        //System.out.println("Stampa 1");
        _initializedAgents++;
        _agentID = agentID;
        _utilityM = utilityM;

        _maxSumAgent = Agent.getAgent(_agentID.getValue());
        _maxSumAgent.setPostservice(_com);
        _maxSumAgent.setOp(_op);
        //variable
        //each agent controls only one variable, so we can associate it with the agentid
        NodeVariable nodevariable = NodeVariable.getNodeVariable(_agentID.getValue());

        _variables.add(nodevariable);
        _maxSumAgent.addNodeVariable(nodevariable);

        // Assegnamento delle funzioni agli agenti
        ArrayList<EntityID> me = new ArrayList<EntityID>();
        me.add(_agentID);
        ArrayList<EntityID> targets = (ArrayList<EntityID>) _utilityM.getNBestTargets(70, me);
        Iterator targetIterator = targets.iterator();
        for (int i = 0; i < _targetPerAgent; i++) {
            if (targetIterator.hasNext()) {
                EntityID nextTargetID = (EntityID)targetIterator.next();
                NodeFunction target = NodeFunction.putNodeFunction(nextTargetID.getValue(), new TabularFunction());
                if (!_functions.contains(target)) {
                    _functions.add(target);
                    _maxSumAgent.addNodeFunction(target);
                } else {
                    i--;
                }
            } else {
                break;
            }
        }
        
        //costruisco il factorGraph: assegno le funzioni alle variabili e viceversa
        Iterator factorGraphIterator = _maxSumAgent.getFunctions().iterator();
        while (factorGraphIterator.hasNext()) {
            NodeFunction nodeTarget = (NodeFunction) factorGraphIterator.next();
            int count = 0;
            ArrayList<EntityID> myTarget = new ArrayList<EntityID>();
            int tarID = nodeTarget.getId();
            EntityID target = _utilityM.getTargetID(tarID);
            myTarget.add(target);
            ArrayList<EntityID> bestAgents = (ArrayList<EntityID>)_utilityM.getNBestAgents(12, myTarget);
            
            Iterator agentIterator = bestAgents.iterator();
            while (agentIterator.hasNext() && count < _dependencies) {
                EntityID agent = (EntityID)agentIterator.next();
                NodeVariable tempVar = NodeVariable.getNodeVariable(agent.getValue());
                if (tempVar.getNeighbour().size() < _maxFunPerVar) {
                    count++;
                    nodeTarget.addNeighbour(NodeVariable.getNodeVariable(agent.getValue()));
                    NodeVariable.getNodeVariable(agent.getValue()).addNeighbour(nodeTarget);
                    NodeVariable.getNodeVariable(agent.getValue()).addValue(NodeArgument.getNodeArgument(nodeTarget.getId()));
 
                }
                
                else {
                    HashSet<NodeFunction> assignedToMe = tempVar.getNeighbour();
                    EntityID worstTarget = _utilityM.getTargetID(tarID);
                    double targetUtility = _utilityM.getUtility(agent, worstTarget);
                    double worstUtility = targetUtility;
                    for (NodeFunction assigned: assignedToMe) {
                        double oldUtility = _utilityM.getUtility(agent, _utilityM.getTargetID(assigned.getId()));
                        if (oldUtility < worstUtility) {
                            worstUtility = oldUtility;
                            worstTarget = _utilityM.getTargetID(assigned.getId());
                        }
                    }
                    
                    if (worstUtility != targetUtility) {
                        count++;
                        nodeTarget.addNeighbour(tempVar);
                        try {
                        NodeFunction.getNodeFunction(worstTarget.getValue()).removeNeighbourBeforeTuples(tempVar);
                        tempVar.changeNeighbour(NodeFunction.getNodeFunction(worstTarget.getValue()), nodeTarget);
                        } catch (exception.FunctionNotPresentException e) {System.out.println("sono nella merda");}
                        tempVar.changeValue(NodeArgument.getNodeArgument(worstTarget.getValue()), NodeArgument.getNodeArgument(nodeTarget.getId()));
                        
                    }
                }
            }
            }
        if (_initializedAgents == 12) {
            tupleBuilder();
        }
        /*Iterator tupleIterator = _maxSumAgent.getFunctions().iterator();
        int k = 0;
        while (tupleIterator.hasNext()) {
            double cost = 0;
            int countAgent = 0;
            NodeFunction function = (NodeFunction) tupleIterator.next();
            int targetID = function.getId();
            int[] possibleValues = {0, targetID};
            int[][] combinations = createCombinations(possibleValues);
            for (int[] arguments : combinations) {
                NodeArgument[] arg = new NodeArgument[function.size()];
               
                for (int i = 0; i < function.size(); i++) {
                    arg[i] = NodeArgument.getNodeArgument(arguments[i]);
                    if (((Integer) arg[i].getValue()).intValue() == targetID) {
                        countAgent++;
                        cost = cost + _utilityM.getUtility(varToFunction[k][i], _utilityM.getTargetID(targetID));
                    }
                }
                
                
                    
                
                
                function.getFunction().addParametersCost(arg, cost);
               
            }
            k++;
        }

        */
        System.out.println("L'agente "+_agentID.getValue()+" ha "+_maxSumAgent.getFunctions().size()+" vicini.");
        System.out.println("La variabile "+nodevariable.getId()+" ha "+nodevariable.getNeighbour().size()+" vicini.");
    }
    
    
    private void tupleBuilder() {
        for(NodeFunction function: _functions) {
            double cost = 0;
            int countAgent = 0;
            int targetID = function.getId();
            int[] possibleValues = {0, targetID};
            int[][] combinations = createCombinations(possibleValues);
            for (int[] arguments : combinations) {
                NodeArgument[] arg = new NodeArgument[function.size()];     
                for (int i = 0; i < function.size(); i++) {
                    arg[i] = NodeArgument.getNodeArgument(arguments[i]);
                    Iterator prova = function.getNeighbour().iterator();
                    if (((Integer) arg[i].getValue()).intValue() == targetID) {
                        countAgent++;
                        NodeVariable var = (NodeVariable)prova.next();
                        cost = cost + _utilityM.getUtility((EntityID)_utilityM.getAgentIDFromNumericID(var.getId()), _utilityM.getTargetID(targetID));
                    }
                }
       

                function.getFunction().addParametersCost(arg, cost);
               
            }
        }

        
    }
    public boolean improveAssignment() {

            HashSet<NodeVariable> vars = new HashSet<NodeVariable>();
            vars = (HashSet<NodeVariable>) _maxSumAgent.getVariables();
            Iterator it = vars.iterator();
            NodeVariable var = (NodeVariable) it.next();
            HashSet<NodeFunction> func = new HashSet<NodeFunction>();
            func = var.getNeighbour();
            if (!func.isEmpty()) {
                //System.out.println("Messaggi 4");
try {
                _maxSumAgent.sendZMessages();
} catch (PostServiceNotSetException e) {}
                //System.out.println("Messaggi 5");
                _maxSumAgent.updateVariableValue();

                //System.out.println("Messaggi 6");
            }

        //System.out.println("Variabileeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee "+_maxSumAgent.variableValueToString());
        toReset = true;
        for (NodeVariable variable : _maxSumAgent.getVariables()) {
            try {
                String target = variable.getStateArgument().getValue().toString();
                /*
                 * Random n = new Random(); if (n.nextDouble() > 0.8 ||
                 * _targetID.getValue() == Assignment.UNKNOWN_TARGET_ID.getValue())
                 */
                _targetID = new EntityID(Integer.parseInt(target));
                //System.out.println("Target: "+variable.getStateArgument().getValue().toString());
            } catch (Exception e) {
            };
            
            /*HashSet<NodeVariable> vars = new HashSet<NodeVariable>();
            vars = (HashSet<NodeVariable>) _maxSumAgent.getVariables();
            Iterator it = vars.iterator();
            NodeVariable var = (NodeVariable) it.next();
            HashSet<NodeFunction> func = new HashSet<NodeFunction>();
            func = var.getNeighbour();
            if (func.isEmpty()) {
                ArrayList<EntityID> me = new ArrayList<EntityID>();
                me.add(_agentID);
                ArrayList<EntityID> target = (ArrayList<EntityID>)_utilityM.getNBestTargets(1, me);
                _targetID = target.get(0); 
            }*/
        }
        //System.out.println("L'agente "+_agentID.getValue()+" ha target "+_targetID.getValue());
        return true;
    }

    public EntityID getAgentID() {
        return _agentID;
    }

    public EntityID getTargetID() {
        return _targetID;
    }

    public Collection<AbstractMessage> sendMessages(){
        _sizeMex = 0; //dimensione mex inviati
        /*
         * MailMan mailman = ((MailManAdapter) com).getMailMan(); if
         * (_maxSumAgent.getPostservice() == null) {
         * _maxSumAgent.setPostservice(mailman); } try {
         * _maxSumAgent.sendQMessages(); _maxSumAgent.sendRMessages(); } catch
         * (PostServiceNotSetException p) { p.printStackTrace();
         * //System.exit(0);
        }
         */
        try {
            //System.out.println("Stampa messaggi 1");
            _maxSumAgent.sendQMessages();
  
            //System.out.println("Stampa messaggi 2");
            _maxSumAgent.sendRMessages();
            //System.out.println("Stampa messaggi 3");
            
        } catch (PostServiceNotSetException p) {
            p.printStackTrace();
        }
        return new LinkedList<AbstractMessage>();
    }

    /*private boolean ismyFunction(NodeFunction f){
        Set<NodeFunction> functions = _maxSumAgent.getFunctions();
        NodeFunction function = null;
        Iterator<NodeFunction> iteratorf = functions.iterator();
   
        while (iteratorf.hasNext()){
            function = iteratorf.next();
            if(f.equals(function)){ //se possiedo f ritorno true altrimenti false
                return true;
            }
        }
        return false;
    }
    
    private boolean ismyVariable(NodeVariable v){
        Set<NodeVariable> variables = _maxSumAgent.getVariables(); //mie variabili
        NodeVariable variable = null;
        Iterator<NodeVariable> iteratorv = variables.iterator();
   
        while (iteratorv.hasNext()){
            variable = iteratorv.next();
            if(v.equals(variable)){ //se la variabile è la mia
                return true;
            }
        }
        return false;
    }*/
    
    public void receiveMessages(Collection<AbstractMessage> messages) {
        
        /*Set<NodeFunction> functions = _maxSumAgent.getFunctions();
        Set<NodeVariable> variables = _maxSumAgent.getVariables();
        // Read Q messages 
        Iterator<NodeVariable> iteratorv = variables.iterator();
        // Ne ha una in teoria 
         NodeVariable variable = null;
         NodeFunction function = null;
        while (iteratorv.hasNext()){
            variable = iteratorv.next();
            Iterator<NodeFunction> iteratorf = variable.getNeighbour().iterator();
            // Funzioni legate alla variabile 
            while (iteratorf.hasNext()){
                function = iteratorf.next();
                if(!ismyFunction(function)){ //se function non è mia allora calcolo i mex tra var e function
                    MessageQ mq = _com.readQMessage(variable, function);
                    int dim = mq.size()*8;
                    _sizeMex += dim;
                }
            }
        }
        // Read R messages
        variable = null;
        function = null;
        Iterator<NodeFunction> iteratorf = functions.iterator();
        while (iteratorf.hasNext()){    //Per ogni funzione che possiedo
             function = iteratorf.next();
             Iterator<NodeVariable> iteratorv_ = function.getNeighbour().iterator();
             while (iteratorv_.hasNext()){ //per ogni variabile controllo prima che non sia la mia
                 variable = iteratorv_.next();
                 if(!ismyVariable(variable)){
                     MessageR mr = _com.readRMessage(function, variable);
                     int dim = mr.size()*8;
                     _sizeMex += dim;
                 }
             }
        }
     */            
    }

    private int[][] createCombinations(int[] possibleValues) {
        int totalCombinations = (int) Math.pow(2, _dependencies);

        int[][] combinationsMatrix = new int[totalCombinations][_dependencies];
        int changeIndex = 1;

        for (int i = 0; i < _dependencies; i++) {
            int index = 0;
            int count = 1;

            changeIndex = changeIndex * possibleValues.length;
            for (int j = 0; j < totalCombinations; j++) {
                combinationsMatrix[j][i] = possibleValues[index];
                if (count == (totalCombinations / changeIndex)) {
                    count = 1;
                    index = (index + 1) % (possibleValues.length);
                } else {
                    count++;
                }

            }
        }
        return combinationsMatrix;
    }

    public void resetStructures() {
        if (toReset) {
            toReset = false;
            Agent.resetIds();
            NodeVariable.resetIds();
            NodeFunction.resetIds();
            NodeArgument.resetIds();
            _mfactory = new MessageFactoryArrayDouble();
            _otimes = new OTimes_MaxSum(_mfactory);
            _oplus = new OPlus_MaxSum(_mfactory);
            _op = new MSumOperator(_otimes, _oplus);
            _com = new MailMan();
            _variables = new HashSet<NodeVariable>();
            _functions = new HashSet<NodeFunction>();
        }

    }
    public int getCcc(){
return 0;
    }

    public int getMessageSize(){
        return 0;
    }
}
