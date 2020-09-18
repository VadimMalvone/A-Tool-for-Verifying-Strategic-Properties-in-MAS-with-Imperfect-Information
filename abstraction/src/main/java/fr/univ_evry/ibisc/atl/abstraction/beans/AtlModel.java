package fr.univ_evry.ibisc.atl.abstraction.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiKeyMap;

public class AtlModel extends JsonObject {

	@SerializedName("states")
	@Expose
	private List<? extends State> states = null;
	@SerializedName("agents")
	@Expose
	private List<Agent> agents = new ArrayList<>();
	@SerializedName("transitions")
	@Expose
	private List<Transition> transitions = new ArrayList<>();
	@SerializedName("group")
	@Expose
	private Group group;
	@SerializedName("formula")
	@Expose
	private Formula formula;
	
	private transient Map<String, State> stateMap;
	
	private transient Map<String, Agent> agentMap;
	
	private transient Map<String, List<Transition>> transitionMap;

	private transient MultiKeyMap<String, List<List<AgentAction>>> agentActionsByStates;

	public List<? extends State> getStates() {
		return states;
	}

	public void setStates(List<? extends State> states) {
		this.states = states;
	}

	public List<Agent> getAgents() {
		return agents;
	}

	public void setAgents(List<Agent> agents) {
		this.agents = agents;
	}

	public List<Transition> getTransitions() {
		return transitions;
	}

	public void setTransitions(List<Transition> transitions) {
		this.transitions = transitions;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	public Formula getFormula() {
		return formula;
	}

	public void setFormula(Formula formula) {
		this.formula = formula;
	}
	
	public Map<String, State> getStateMap() {
		if (stateMap == null) {
			stateMap = new HashMap<>();
			for (State state : getStates()) {
				stateMap.put(state.getName(), state);
			}
		}
		return stateMap;
	}
	
	public State getState(String stateName) {
		return getStateMap().get(stateName);
	}
	
	public Map<String, Agent> getAgentMap() {
		if (agentMap == null) {
			agentMap = new HashMap<>();
			for (Agent agent : getAgents()) {
				agentMap.put(agent.getName(), agent);
			}
		}
		return agentMap;
	}
	
	public MultiKeyMap<String, List<List<AgentAction>>> getAgentActionsByStates() {
		if (agentActionsByStates == null) {
			agentActionsByStates = new MultiKeyMap<>();
			for (Transition transition : getTransitions()) {
				if (!agentActionsByStates.containsKey(transition.getFromState(), transition.getToState())) {
					agentActionsByStates.put(transition.getFromState(), transition.getToState(), new ArrayList<>());
				}
				agentActionsByStates.get(transition.getFromState(), transition.getToState()).addAll(transition.getAgentActions());
			}
		}
		
		return agentActionsByStates;
	}
	
	public Map<String, List<Transition>> getTransitionMap() {
		if (MapUtils.isEmpty(transitionMap)) {
			transitionMap = new HashMap<>();
			for (Transition transition : getTransitions()) {
				if (!transitionMap.containsKey(transition.getFromState())) {
					transitionMap.put(transition.getFromState(), new ArrayList<>());
				}
				transitionMap.get(transition.getFromState()).add(transition);
			}
		}
		
		return transitionMap;
	}
	

}
