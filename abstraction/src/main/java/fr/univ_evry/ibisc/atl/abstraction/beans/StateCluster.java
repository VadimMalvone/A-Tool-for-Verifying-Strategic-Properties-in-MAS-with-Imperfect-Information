package fr.univ_evry.ibisc.atl.abstraction.beans;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ArrayUtils;

public class StateCluster extends State {
	
	private transient List<State> childStates = new ArrayList<>();
	
	public StateCluster(State... states) {
		if (ArrayUtils.isNotEmpty(states)){
			childStates.addAll(Arrays.asList(states));
			Collections.sort(childStates);
			setLabels();
			setName();
			for (State state:states) {
				if (state.isInitial()) {
					setInitial(true);
					break;
				}
			}
		}
	}
	
	public void addChildStates(StateCluster stateCluster) {
		for (State state : stateCluster.childStates) {
			if (!childStates.contains(state)) {
				childStates.add(state);
			}
		}
		Collections.sort(childStates);
		setName();
		setLabels();
	}
	
	public void addChildState(State childState) {
		childStates.add(childState);
		Collections.sort(childStates);
		setName();
		setLabels();
	}
	
	public boolean containsChildState(State childState) {
		return childStates.contains(childState);
	}

	public void setName() {
		setName(childStates.stream().map(State::getName).collect(Collectors.joining("_")));
	}
	
	private void setLabels() {
		List<String> labels = new ArrayList<>();
		List<String> allLabels = new ArrayList<>();
		if (!childStates.isEmpty()) {
			labels.addAll(childStates.get(0).getLabels());
			allLabels.addAll(childStates.get(0).getLabels());
			for (int i = 1; i < childStates.size() && !labels.isEmpty(); i++) {
				labels = ListUtils.intersection(labels, childStates.get(i).getLabels());
				allLabels = ListUtils.union(allLabels, childStates.get(i).getLabels());
			}
		}
		setLabels(labels.stream().distinct().collect(Collectors.toList()));
		setFalseLabels(ListUtils.subtract(allLabels, labels).stream().distinct().collect(Collectors.toList()));
	}
	
	public State toState() {
		State state = new State(getName(), isInitial(), getLabels().toArray(new String[getLabels().size()]));
		state.setFalseLabels(getFalseLabels());
		return state;
	}
	
	public boolean containsAnyChildState(List<State> states) {
		return CollectionUtils.containsAny(childStates, states);
	}
	
	public boolean containsAnyChildState(StateCluster stateCluster) {
		return CollectionUtils.containsAny(childStates, stateCluster.childStates);
	}

//	public List<List<AgentAction>> hasMustTransition(StateCluster toStateCluster, AtlModel atlModel) {
//		List<List<AgentAction>> agentActions = new ArrayList<>();
//		for (State fromChildState : childStates) {
//			boolean toStateFound = false;
//			for (State toChildState : toStateCluster.childStates) {
//				if (atlModel.getAgentActionsByStates().containsKey(fromChildState.getName(), toChildState.getName())) {
//					agentActions.addAll(atlModel.getAgentActionsByStates().get(fromChildState.getName(), toChildState.getName()));
//					toStateFound = true;
//				}
//			}
//
//			if (!toStateFound) {
//				agentActions.clear();
//				break;
//			}
//		}
//
//		return agentActions;
//	}

	public List<List<AgentAction>> hasMustTransition(StateCluster toStateCluster, AtlModel atlModel) {
		List<String> coalition = atlModel.getGroup().getAgents();
		Map<String, List<String>> mustActions = new HashMap<>();
		for(String agent : coalition) {
			for(String action : atlModel.getAgentMap().get(agent).getActions()) {
				boolean toStateFound = false;
				for(State fromChildState : childStates) {
					toStateFound = false;
					for(State toChildState : toStateCluster.childStates) {
						if(atlModel.getAgentActionsByStates().get(fromChildState.getName(), toChildState.getName()) == null) {
							continue;
						}
						for(List<AgentAction> agentActionList : atlModel.getAgentActionsByStates().get(fromChildState.getName(), toChildState.getName())) {
							for(AgentAction agentAction : agentActionList) {
								if(agentAction.getAction().equals(action)) {
									toStateFound = true;
									break;
								}
							}
							if(toStateFound) {
								break;
							}
						}
						if(toStateFound) {
							break;
						}
					}
					if(!toStateFound) {
						break;
					}
				}
				if(toStateFound) {
					if(mustActions.containsKey(agent)) {
						mustActions.get(agent).add(action);
					} else {
						List<String> aux = new ArrayList<>();
						aux.add(action);
						mustActions.put(agent, aux);
					}
				}
			}
		}

		for(String agent : mustActions.keySet()) {
			if(mustActions.get(agent).isEmpty()) {
				return new ArrayList<>();
			}
		}

		List<List<AgentAction>> agentActions = new ArrayList<>();
		for (State fromChildState : childStates) {
			for (State toChildState : toStateCluster.childStates) {
				if (atlModel.getAgentActionsByStates().containsKey(fromChildState.getName(), toChildState.getName())) {
					List<List<AgentAction>> aux = new ArrayList<>();
					for(List<AgentAction> agentActionList : atlModel.getAgentActionsByStates().get(fromChildState.getName(), toChildState.getName())) {
						boolean valid = true;
						for(String agent : mustActions.keySet()) {
							Optional<AgentAction> optAct = agentActionList.stream().filter(a -> a.getAgent().equals(agent) && mustActions.get(agent).contains(a.getAction())).findAny();
							if(!optAct.isPresent()) {
								valid = false;
								break;
							}
						}
						if(valid) {
							agentActions.add(agentActionList);
						}
					}
					agentActions.addAll(atlModel.getAgentActionsByStates().get(fromChildState.getName(), toChildState.getName()));
				}
			}
		}

		return agentActions;
	}

	public List<List<AgentAction>> hasMayTransition(StateCluster toStateCluster, AtlModel atlModel) {
		List<List<AgentAction>> agentActions = new ArrayList<>();
		for (State fromChildState : childStates) {
			for (State toChildState : toStateCluster.childStates) {
				if (atlModel.getAgentActionsByStates().containsKey(fromChildState.getName(), toChildState.getName())) {
					agentActions.addAll(atlModel.getAgentActionsByStates().get(fromChildState.getName(), toChildState.getName()));
				}
			}
		}
		
		return agentActions;
	}

	public List<State> getChildStates() {
		return childStates;
	}
	
}
