package fr.univ_evry.ibisc.atl.abstraction;


import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;

import fr.univ_evry.ibisc.atl.abstraction.beans.AgentAction;
import fr.univ_evry.ibisc.atl.abstraction.beans.Agent;
import fr.univ_evry.ibisc.atl.abstraction.beans.AtlModel;
import fr.univ_evry.ibisc.atl.abstraction.beans.State;
import fr.univ_evry.ibisc.atl.abstraction.beans.StateCluster;
import fr.univ_evry.ibisc.atl.abstraction.beans.Transition;

public class AbstractionUtils {
	
    private static final String MODEL_JSON_FILE_NAME = "";
	private final static Log logger = LogFactory.getLog(AbstractionUtils.class);

	public static List<StateCluster> getStateClusters(AtlModel atlModel) {
		List<StateCluster> stateClusters = new ArrayList<>();
		for (State state : atlModel.getStates()) {
			stateClusters.add(state.toStateCluster());
		}

		for (Agent agent : atlModel.getAgents()) {
			for (List<String> indistinguishableStateNameList : agent.getIndistinguishableStates()) {
				for (StateCluster stateCluster : stateClusters) {
					List<State> indistinguishableStateList = 
											indistinguishableStateNameList.parallelStream()
													.map(stateName->atlModel.getState(stateName)).collect(Collectors.toList());
					if (stateCluster.containsAnyChildState(indistinguishableStateList)) {
						for (State state : indistinguishableStateList) {
							if (!stateCluster.containsChildState(state)) {
								stateCluster.addChildState(state);
							}
						}
					}
				}
			}
		}
		
		for (StateCluster stateCluster1 : stateClusters) {
			for (StateCluster stateCluster2 : stateClusters) {
				if (stateCluster1.containsAnyChildState(stateCluster2)) {
					stateCluster1.addChildStates(stateCluster2);
				}
			}
		}
		
		return stateClusters.stream().distinct().collect(Collectors.toList());
	}
	
	
	
	public static List<Transition> getMayTransitions(final AtlModel atlModel, final List<StateCluster> stateClusters) {
		List<Transition> transitions = new ArrayList<>();
		for (StateCluster fromStateCluster : stateClusters) {
			for (StateCluster toStateCluster : stateClusters) {
				List<List<AgentAction>> agentActions = fromStateCluster.hasMayTransition(toStateCluster, atlModel);
				if (!agentActions.isEmpty()) {
					removeDuplicates(agentActions);
					Transition transition = new Transition();
					transition.setFromState(fromStateCluster.getName());
					transition.setToState(toStateCluster.getName());
					transition.setAgentActions(agentActions);
					transitions.add(transition);
				}
			}
		}
		
		return transitions;
	}
	
	public static List<Transition> getMustTransitions(final AtlModel atlModel, final List<StateCluster> stateClusters) {
		List<Transition> transitions = new ArrayList<>();
		for (StateCluster fromStateCluster : stateClusters) {
			for (StateCluster toStateCluster : stateClusters) {
				List<List<AgentAction>> agentActions = fromStateCluster.hasMustTransition(toStateCluster, atlModel);
				if (!agentActions.isEmpty()) {
					removeDuplicates(agentActions);
					Transition transition = new Transition();
					transition.setFromState(fromStateCluster.getName());
					transition.setToState(toStateCluster.getName());
					transition.setAgentActions(agentActions);
					transitions.add(transition);
				}
			}
		}
		
		return transitions;
	}
	
	public static void removeDuplicates(List<List<AgentAction>> agentActions) {
		Map<String, List<AgentAction>> actionMap = new HashMap<>();
		for (List<AgentAction> actionList : agentActions) {
			actionMap.put(actionList.toString(), actionList);
		}
		agentActions.clear();
		agentActions.addAll(actionMap.values());
	}
	
	public static String generateDotGraph(AtlModel atlModel) {
		StringBuilder stringBuilder = new StringBuilder("digraph G {").append(System.lineSeparator());
		List<Transition> transitions = atlModel.getTransitions();
		for (Transition transition : transitions) {
			if (CollectionUtils.isEmpty(atlModel.getState(transition.getFromState()).getLabels())) {
				stringBuilder.append(transition.getFromState());
			} else {
				stringBuilder
						.append("\"").append(transition.getFromState()).append("(").append(String.join(", ", atlModel.getState(transition.getFromState()).getLabels())).append(")\"");
			}
			stringBuilder.append("->");
			if (CollectionUtils.isEmpty(atlModel.getState(transition.getToState()).getLabels())) {
				stringBuilder.append(transition.getToState());
			} else {
				stringBuilder
						.append("\"").append(transition.getToState()).append("(").append(String.join(", ", atlModel.getState(transition.getToState()).getLabels())).append(")\"");
			}
			List<String> list1 = new ArrayList<>();
			for(List<AgentAction> agentActionList: transition.getAgentActions()) {
				List<String> list2 = new ArrayList<>();
				for (AgentAction agentAction : agentActionList) {
					list2.add(agentAction.getAgent()+ "." +agentAction.getAction());
				}
				list1.add(MessageFormat.format("({0})", String.join(",", list2)));
			}
			stringBuilder.append("[ label = \"" + String.join("\\n", list1) + "\" ];").append(System.lineSeparator());
		}
		stringBuilder.append("}").append(System.lineSeparator());
		return stringBuilder.toString();
	}
	
	public static void validateAtlModel(AtlModel atlModel) throws Exception {
		validateTransitions(atlModel);
		validateGroup(atlModel);
	}
		
	private static void validateTransitions(AtlModel atlModel) throws Exception {
		for(Transition transition : atlModel.getTransitions()) {
			if (!atlModel.getStateMap().containsKey(transition.getFromState())) {
				throw new Exception(MessageFormat.format("invalid state {0} in transition : {1} {2}", 
						transition.getFromState(), System.lineSeparator(), transition));
			}
			if (!atlModel.getStateMap().containsKey(transition.getToState())) {
				throw new Exception(MessageFormat.format("invalid state {0} in transition : {1} {2}", 
						transition.getToState(), System.lineSeparator(), transition));
			}
			if (transition.isDefaultTransition() && CollectionUtils.isNotEmpty(transition.getAgentActions())) {
				throw new Exception(MessageFormat.format("The transition cannot be a default one and have explicit agent actions : {0} {1}", 
								System.lineSeparator(), transition));
			}			
			for(List<AgentAction> agentActionList : transition.getAgentActions()) {
				validateAgentActionList(atlModel, transition, agentActionList);
			}
		}
	}
	
	private static void validateAgentActionList(AtlModel atlModel, Transition transition, List<AgentAction> agentActionList) throws Exception {
		for (AgentAction agentAction : agentActionList) {
			validateAgentAction(atlModel, transition, agentAction);
		}
		List<String> agents = agentActionList.parallelStream().map(AgentAction::getAgent).collect(Collectors.toList());
		Collection<String> agentNotDefinedList = CollectionUtils.subtract(agents, atlModel.getAgentMap().keySet());
		if (CollectionUtils.isNotEmpty(agentNotDefinedList)) {
			throw new Exception (MessageFormat.format("Some agents have not been defined : {0} for the transition : {1} {2}", 
					agentNotDefinedList, System.lineSeparator(), transition));
		}
		Collection<String> missingAgentActionsList = CollectionUtils.subtract(atlModel.getAgentMap().keySet(), agents);
		if (CollectionUtils.isNotEmpty(missingAgentActionsList)) {
			throw new Exception (MessageFormat.format("Some agent actions have not been defined : {0} for the transition : {1} {2}", 
					missingAgentActionsList, System.lineSeparator(), transition));
		}
	}
	
	private static void validateAgentAction(AtlModel atlModel, Transition transition, AgentAction agentAction) throws Exception {
		if (!atlModel.getAgentMap().containsKey(agentAction.getAgent())) {
			throw new Exception (MessageFormat.format("Invalid agent {0} in agentAction : {1} for the transition : {2} {3}", 
					agentAction.getAgent(), agentAction, System.lineSeparator(), transition));
		}
		Agent agent = atlModel.getAgentMap().get(agentAction.getAgent());
		if (!agent.getActions().contains(agentAction.getAction())) {
			throw new Exception (MessageFormat.format("Invalid action {0} in agentAction : {1} for the transition : {2} {3}", 
					agentAction.getAction(), agentAction, System.lineSeparator(), transition));
		}
	}
	
	private static void validateGroup(AtlModel atlModel) throws Exception {
		List<String> groupAgents = atlModel.getGroup().getAgents();
		Collection<String> agentNotDefinedList = CollectionUtils.subtract(groupAgents, atlModel.getAgentMap().keySet());
		if (CollectionUtils.isNotEmpty(agentNotDefinedList)) {
			throw new Exception (MessageFormat.format("Some agents in the group have not been defined : {0}", 
					agentNotDefinedList, System.lineSeparator(), atlModel));
		}
	}
	
	
	public static String readSampleFile() {
		try {
			File sampleFile = new ClassPathResource(MODEL_JSON_FILE_NAME).getFile();
			return new String(FileUtils.readFileToByteArray(sampleFile));
		} catch (IOException ioe) {
			logger.error("Error while trying to read the sample file.", ioe);
		}
		
		return null;
	}
	
	public static void processDefaultTransitions(AtlModel atlModel) throws Exception {
		for(Entry<String, List<Transition>> entry : atlModel.getTransitionMap().entrySet()) {
			List<Transition> transitions = entry.getValue();
			List<Transition> defaultTransitions = transitions.parallelStream().filter(transition -> transition.isDefaultTransition()).collect(Collectors.toList());
			if (CollectionUtils.isEmpty(defaultTransitions)) {
				continue;
			}
			if (defaultTransitions.size() > 1) {
				throw new Exception (MessageFormat.format("The state {0} has {1} default transition, only one is allowed. Transitions : {2} {3}", 
						entry.getKey(), defaultTransitions.size(), System.lineSeparator(), defaultTransitions));
			}
			Collection<Transition> explicitTransitions = CollectionUtils.subtract(transitions, defaultTransitions);
			List<List<List<AgentAction>>> existingActionLists = explicitTransitions.parallelStream().map(Transition::getAgentActions).collect(Collectors.toList());
			Set<List<AgentAction>> actions = defaultTransitions.get(0).getMultipleAgentActions()
												.parallelStream()
												.map(
													multipleAgentAction->multipleAgentAction.getActions()
																	.stream()
																	.map(action -> new AgentAction(multipleAgentAction.getAgent(), action))
												.collect(Collectors.toList()))
												.collect(Collectors.toSet()); 
			List<List<AgentAction>> possibleActions = Lists.cartesianProduct(actions.toArray(new ArrayList<?>[actions.size()])).parallelStream().map(list->list.stream().map(action->(AgentAction) action).collect(Collectors.toList())).collect(Collectors.toList());
			for (List<List<AgentAction>> agentActionsList : existingActionLists) {
				for (List<AgentAction> agentActionList : agentActionsList) {
					Iterator<List<AgentAction>> iterator = possibleActions.iterator();
					while (iterator.hasNext()) {
						if (CollectionUtils.isEqualCollection(iterator.next(), agentActionList)) {
							iterator.remove();
						}
					}
				}
			}
			atlModel.getTransitions().add(new Transition(entry.getKey(), defaultTransitions.get(0).getToState(), possibleActions));
			atlModel.setTransitions(Lists.newLinkedList(CollectionUtils.removeAll(atlModel.getTransitions(), defaultTransitions)));
		}
	}


	public static String generateMCMASProgram(AtlModel atlModel, boolean isMayModel) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(System.lineSeparator()).append("Agent Environment").append(System.lineSeparator());
		stringBuilder.append("\t").append("Vars :").append(System.lineSeparator());
		for (State state: atlModel.getStates()) {
			stringBuilder.append("\t").append("\t").append(state.getName()).append(" : boolean;").append(System.lineSeparator());
			for (String label: state.getLabels()) {
				stringBuilder.append("\t").append("\t").append(label).append(" : boolean;").append(System.lineSeparator());
			}
			for (String label: state.getFalseLabels()) {
				stringBuilder.append("\t").append("\t").append(label).append(" : boolean;").append(System.lineSeparator());
			}
		}
		stringBuilder.append("\t").append("end Vars").append(System.lineSeparator());
		stringBuilder.append("\t").append("Actions = {};").append(System.lineSeparator());
		stringBuilder.append("\t").append("Protocol :").append(System.lineSeparator());
		stringBuilder.append("\t").append("end Protocol").append(System.lineSeparator());
		stringBuilder.append("\t").append("Evolution :").append(System.lineSeparator());
		for (Transition transition: atlModel.getTransitions()) {
			State toState = atlModel.getState(transition.getToState());
			State fromState = atlModel.getState(transition.getFromState());
			stringBuilder.append("\t").append("\t");
			if (!toState.equals(fromState)) {
				stringBuilder.append(fromState.getName()).append(" = false ");
				if (CollectionUtils.isNotEmpty(fromState.getLabels())) {
					for (String label: fromState.getLabels()) {
						stringBuilder.append("and ").append(label).append(" = false ");
					}
				}
				stringBuilder.append("and ");
			}

			stringBuilder.append(toState.getName()).append(" = true ");
			if (CollectionUtils.isNotEmpty(toState.getLabels())) {
				for (String label : toState.getLabels()) {
					stringBuilder.append("and ").append(label).append(" = true ");
				}
			}
			stringBuilder.append(" if ");

			stringBuilder.append(fromState.getName()).append(" = true ");
			if (CollectionUtils.isNotEmpty(fromState.getLabels())) {
				for (String label: fromState.getLabels()) {
					stringBuilder.append("and ").append(label).append(" = true ");
				}
			}

			if (!toState.equals(fromState)) {
				stringBuilder.append("and ").append(toState.getName()).append(" = false ");
				if (CollectionUtils.isNotEmpty(toState.getLabels())) {
					for (String label : toState.getLabels()) {
						stringBuilder.append("and ").append(label).append(" = false ");
					}
				}
			}

			stringBuilder.append("and ");
			if (transition.getAgentActions().size()>1)
				stringBuilder.append("(");
			for (int i = 0; i < transition.getAgentActions().size(); i++) {
				List<AgentAction> agentActionList = transition.getAgentActions().get(i);
				stringBuilder.append("(");
				for (int j = 0; j < agentActionList.size(); j++) {
					AgentAction agentAction = agentActionList.get(j);
					stringBuilder.append(agentAction.getAgent()).append(".Action").append(" = ").append(agentAction.getAction());
					if (j<agentActionList.size()-1)
						stringBuilder.append(" and ");
				}
				stringBuilder.append(")");
				if (i<transition.getAgentActions().size()-1)
					stringBuilder.append(" or ").append(System.lineSeparator()).append("\t\t\t\t\t");
			}
			if (transition.getAgentActions().size()>1)
				stringBuilder.append(")");

			stringBuilder.append(";").append(System.lineSeparator());
		}
		stringBuilder.append("\t").append("end Evolution").append(System.lineSeparator());
		stringBuilder.append("end Agent").append(System.lineSeparator());

		for (Agent agent:atlModel.getAgents()) {
			stringBuilder.append("Agent ").append(agent.getName()).append(System.lineSeparator());
			List<String> lobsvars = new ArrayList<>();
			for (State state : atlModel.getStates()) {
				lobsvars.add(state.getName());
				for (String label : state.getLabels())
					lobsvars.add(label);
			}
			stringBuilder.append("\t").append("Lobsvars = {").append(String.join(", ", lobsvars)).append("};").append(System.lineSeparator());
			stringBuilder.append("\t").append("Vars : ").append(System.lineSeparator());
			stringBuilder.append("\t").append("\t").append("play : boolean;").append(System.lineSeparator());
			stringBuilder.append("\t").append("end Vars").append(System.lineSeparator());
			stringBuilder.append("\t").append("Actions = {").append(String.join(",", agent.getActions())).append("};");
			Map<String, List<String>> availableActionMap = getAvailableActions(atlModel, agent);
			stringBuilder.append(System.lineSeparator()).append("\t").append("Protocol : ").append(System.lineSeparator());
			for (Entry<String, List<String>> availableActionsEntry: availableActionMap.entrySet()) {
				stringBuilder.append("\t").append("\t").append("Environment.")
								.append(availableActionsEntry.getKey()).append(" = true");
				State state = atlModel.getState(availableActionsEntry.getKey());
				if (CollectionUtils.isNotEmpty(state.getLabels())) {
					for (String label : state.getLabels())
						stringBuilder.append(" and ").append("Environment.").append(label).append(" = true");
				}
				stringBuilder.append(" : {")
								.append(String.join(",", availableActionsEntry.getValue())).append("};").append(System.lineSeparator());
			}
			stringBuilder.append("\t").append("end Protocol").append(System.lineSeparator());
			stringBuilder.append("\t").append("Evolution : ").append(System.lineSeparator());
			stringBuilder.append("\t").append("\t").append("play = true if play = true;").append(System.lineSeparator());
			stringBuilder.append("\t").append("end Evolution").append(System.lineSeparator());
			stringBuilder.append("end Agent").append(System.lineSeparator());
		}

		stringBuilder.append("Evaluation").append(System.lineSeparator());
		for (String term: atlModel.getFormula().getTerms()) {
			stringBuilder.append("\t").append(term).append(" if (Environment.").append(term).append(" = true);").append(System.lineSeparator());
		}
		stringBuilder.append("\t").append("end Evaluation").append(System.lineSeparator());

		stringBuilder.append("\t").append("InitStates").append(System.lineSeparator());
		for (int i = 0; i < atlModel.getStates().size(); i++) {
			State state = atlModel.getStates().get(i);
			stringBuilder.append("\t").append("\t").append("Environment.").append(state.getName()).append(" = ").append(state.isInitial());
			if (CollectionUtils.isNotEmpty(state.getLabels())) {
				stringBuilder.append(" and ").append(System.lineSeparator());
				for (int j = 0; j < state.getLabels().size(); j++) {
					String label = state.getLabels().get(j);
					stringBuilder.append("\t").append("\t").append("Environment.").append(label).append(" = ").append(state.isInitial());
					if (j<state.getLabels().size()-1)
						stringBuilder.append(" and ").append(System.lineSeparator());
				}
			}

			if (CollectionUtils.isNotEmpty(state.getFalseLabels())) {
				stringBuilder.append(" and ").append(System.lineSeparator());
				for (int j = 0; j < state.getFalseLabels().size(); j++) {
					String label = state.getFalseLabels().get(j);
					stringBuilder.append("\t").append("\t").append("Environment.").append(label).append(" = false");
					if (j<state.getLabels().size()-1)
						stringBuilder.append(" and ").append(System.lineSeparator());
				}
			}

			if (i<atlModel.getStates().size()-1) {
				stringBuilder.append(" and ").append(System.lineSeparator());
			}
		}

		if (CollectionUtils.isNotEmpty(atlModel.getAgents())) {
			stringBuilder.append(" and ").append(System.lineSeparator());
			for (int i = 0; i < atlModel.getAgents().size(); i++) {
				Agent agent = atlModel.getAgents().get(i);
				stringBuilder.append("\t").append("\t").append(agent.getName()).append(".play = true");
				if (i<atlModel.getAgents().size()-1)
					stringBuilder.append(" and ").append(System.lineSeparator());
			}
		}

		stringBuilder.append(";").append(System.lineSeparator()).append("\t").append("end InitStates").append(System.lineSeparator());

		stringBuilder.append("Groups").append(System.lineSeparator());
		stringBuilder.append("\t").append(atlModel.getGroup().getName()).append("=").append("{").append(String.join(",", atlModel.getGroup().getAgents())).append("};").append(System.lineSeparator());
		stringBuilder.append("end Groups").append(System.lineSeparator());

		stringBuilder.append("Formulae").append(System.lineSeparator());
		stringBuilder.append("\t");
		if (isMayModel)
			stringBuilder.append("!(");
		stringBuilder.append("<").append(atlModel.getGroup().getName()).append(">").append(atlModel.getFormula().getSubformula());
		if (isMayModel)
			stringBuilder.append(")");
		stringBuilder.append(";").append(System.lineSeparator());
		stringBuilder.append("end Formulae").append(System.lineSeparator());



		return stringBuilder.toString();
	}


	private static Map<String, List<String>> getAvailableActions(AtlModel atlModel, Agent agent) {
		Map<String, List<String>> availableActionMap = new HashMap<>();
		for (Transition transition : atlModel.getTransitions()) {
			if (!availableActionMap.containsKey(transition.getFromState())) {
				availableActionMap.put(transition.getFromState(), new ArrayList<>());
			}

			for (List<AgentAction> agentActionList : transition.getAgentActions()) {
				for (AgentAction agentAction : agentActionList) {
					if (agentAction.getAgent().equals(agent.getName())
							&&
								!availableActionMap.get(transition.getFromState()).contains(agentAction.getAction())) {
						availableActionMap.get(transition.getFromState()).add(agentAction.getAction());
					}

				}
			}
		}

		return availableActionMap;
	}

	public static String modelCheck(String mcmasFilePath) throws IOException {
		try(Scanner scanner = new Scanner(Runtime.getRuntime().exec("/home/ec2-user/mcmas-linux64-1.3.0 " + mcmasFilePath).getInputStream()).useDelimiter("\\A")) {
			return scanner.hasNext() ? scanner.next() : "";
		}
	}

	public static boolean getMcmasResult(String mcmasOutput) {
		return  (mcmasOutput.contains("is TRUE in the model"));
	}
}
