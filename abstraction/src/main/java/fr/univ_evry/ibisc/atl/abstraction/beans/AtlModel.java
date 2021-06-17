package fr.univ_evry.ibisc.atl.abstraction.beans;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import fr.univ_evry.ibisc.atl.abstraction.AbstractionUtils;
import fr.univ_evry.ibisc.atl.parser.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.collections4.MapUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.collections4.map.MultiKeyMap;

public class AtlModel extends JsonObject implements Cloneable {

	@SerializedName("states")
	@Expose
	private List<? extends State> states = null;
	@SerializedName("agents")
	@Expose
	private List<Agent> agents = new ArrayList<>();
	@SerializedName("transitions")
	@Expose
	private List<Transition> transitions = new ArrayList<>();
	@SerializedName("groups")
	@Expose
	private List<Group> groups = new ArrayList<>();
	@SerializedName("formula")
	@Expose
	private String formula;
	private ATL atl;
	
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

	public List<Group> getGroups() {
		return groups;
	}

	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}

	public String getFormula() {return formula; }
	public void setFormula(String formula) { this.formula = formula; }

	public ATL getATL() {
		if(atl == null) {
			CharStream codePointCharStream = CharStreams.fromString(formula);
			ATLLexer lexer = new ATLLexer(codePointCharStream);
			ATLParser parser = new ATLParser(new CommonTokenStream(lexer));
			ParseTree tree = parser.atlExpr();
			ClosureLTLVisitor visitor = new ClosureLTLVisitor();
			atl = visitor.visit(tree);
		}
		return atl;
	}

	public void setATL(ATL formula) {
		this.atl = formula;
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

	public void setStateMap(Map<String, State> map) {
		this.stateMap = map;
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

	public static AtlModel product(AtlModel model, Automaton automaton) {
		AtlModel result = model.clone();

		result.setAgents(model.getAgents());

		List<State> states = new ArrayList<>();

		for(String s : model.getStates().stream().map(State::getName).collect(Collectors.toList())) {
			for(String q : automaton.getStates()) {
				State state = new State();
				state.setName(s + "_x_" + q);
				state.setLabels(new ArrayList<>());
				state.setFalseLabels(new ArrayList<>());
				for(ATL ATL : automaton.getStateLTLMap().get(q)) {
					if(ATL instanceof ATL.Atom) {
						state.getLabels().add(ATL.toString());
					} else if(ATL instanceof ATL.Not) {
						state.getFalseLabels().add(((ATL.Not) ATL).getSubFormula().toString());
					}
				}
				states.add(state);
			}
		}
		result.setStates(states);

		for(State initialState : model.getStates().stream().filter(State::isInitial).collect(Collectors.toList())) {
			Set<ATL> event = new HashSet<>();
			initialState.getLabels().forEach(l -> event.add(new ATL.Atom(l)));
			initialState.getFalseLabels().forEach(l -> event.add(new ATL.Not(new ATL.Atom(l))));
			for (String q0 : automaton.getInitialStates()) {
				for (String next : automaton.next(q0, event)) {
					result.getState(initialState.getName() + "_x_" + next).setInitial(true);
//					State state = new State();
//					state.setName(initialState.getName() + "_x_" + next);
//					state.setLabels(new ArrayList<>());
//					state.setFalseLabels(new ArrayList<>());
//					for(LTL ltl : automaton.getStateLTLMap().get(next)) {
//						if(ltl instanceof LTL.Atom) {
//							state.getLabels().add(ltl.toString());
//						} else if(ltl instanceof LTL.Not) {
//							state.getFalseLabels().add(((LTL.Not) ltl).getSubFormula().toString());
//						}
//					}
//					states.add(state);
				}
			}
		}
		result.setTransitions(new ArrayList<>());
		for (Transition tr : model.getTransitions()) {
			Set<ATL> e = new HashSet<>();
			State toState = model.getState(tr.getToState());
			toState.getLabels().forEach(l -> e.add(new ATL.Atom(l)));
			toState.getFalseLabels().forEach(l -> e.add(new ATL.Not(new ATL.Atom(l))));
			for (String q : automaton.getStates()) {
				Set<String> qs = automaton.next(q, e);
				for (String qs1 : qs) {
					Transition newTr = new Transition();
					newTr.setFromState(tr.getFromState() + "_x_" + q);
					newTr.setToState(tr.getToState() + "_x_" + qs1);
					newTr.setAgentActions(tr.getAgentActions());
					newTr.setDefaultTransition(tr.isDefaultTransition());
					newTr.setMultipleAgentActions(tr.getMultipleAgentActions());
					result.getTransitions().add(newTr);
				}
			}
		}
		return result;
	}

	@Override
	public AtlModel clone() {
		AtlModel clone;
		try {
			clone = (AtlModel) super.clone();
		}
		catch (CloneNotSupportedException ex) {
			throw new RuntimeException("Superclass messed up", ex);
		}

		List<State> statesAuxList = new ArrayList<>();
		for (State state : states) {
			State newState = new State(state.getName(), state.isInitial());
			newState.setLabels(new ArrayList<>(state.getLabels()));
			newState.setFalseLabels(new ArrayList<>(state.getFalseLabels()));
			statesAuxList.add(newState);
		}
		clone.states = statesAuxList;
		List<Agent> agentsAuxList = new ArrayList<>();
		for(Agent agent : agents) {
			Agent newAgent = new Agent();
			newAgent.setName(agent.getName());
			newAgent.setActions(new ArrayList<>(agent.getActions()));
			newAgent.setIndistinguishableStates(new ArrayList<>());
			for(List<String> indS : agent.getIndistinguishableStates()) {
				newAgent.getIndistinguishableStates().add(new ArrayList<>(indS));
			}
			agentsAuxList.add(newAgent);
		}
		clone.agents = agentsAuxList;
		List<Transition> transitionsAuxList = new ArrayList<>();
		for(Transition tr : transitions) {
			Transition newTransition = new Transition();
			newTransition.setFromState(tr.getFromState());
			newTransition.setToState(tr.getToState());
			newTransition.setAgentActions(new ArrayList<>());
			for(List<AgentAction> aal : tr.getAgentActions()) {
				List<AgentAction> aalAux = new ArrayList<>();
				for(AgentAction aa : aal) {
					AgentAction newAa = new AgentAction();
					newAa.setAgent(aa.getAgent());
					newAa.setAction(aa.getAction());
					aalAux.add(newAa);
				}
				newTransition.getAgentActions().add(aalAux);
			}
			List<MultipleAgentAction> maalAux = new ArrayList<>();
			for(MultipleAgentAction maa : tr.getMultipleAgentActions()) {
				MultipleAgentAction newMaa = new MultipleAgentAction();
				newMaa.setAgent(maa.getAgent());
				newMaa.setActions(new ArrayList<>(maa.getActions()));
				maalAux.add(newMaa);
			}
			newTransition.setMultipleAgentActions(maalAux);
			newTransition.setDefaultTransition(tr.isDefaultTransition());
			transitionsAuxList.add(newTransition);
		}
		clone.transitions = transitionsAuxList;
		clone.groups = new ArrayList<>();
		for(Group g : groups) {
			Group ng = new Group();
			ng.setName(g.getName());
			ng.setAgents(new ArrayList<>(g.getAgents()));
			clone.groups.add(ng);
		}
		clone.formula = formula;
		clone.atl = getATL().clone();
		clone.agentMap = null;
		clone.stateMap = null;
		return clone;
	}

	public Automaton toAutomaton() {
		Set<String> states = this.states.stream().map(State::getName).collect(Collectors.toSet());
		Set<String> initialStates = new HashSet<>(); // this.states.stream().filter(State::isInitial).map(State::getName).collect(Collectors.toSet());
		String init = "init";
		while(states.contains(init)) {
			init = init + "_init";
		}
		states.add(init);
		initialStates.add(init);
		Map<String, Map<Set<ATL>, Set<String>>> transitions = new HashMap<>();
		for(State s : this.states) {
			Set<ATL> event = new HashSet<>();
			for(String l : s.getLabels()) {
				event.add(new ATL.Atom(l));
			}
			for(String l : s.getFalseLabels()) {
				event.add(new ATL.Not(new ATL.Atom(l)));
			}
			for(String from : this.transitions.stream().filter(t -> t.getToState().equals(s.getName())).map(Transition::getFromState).collect(Collectors.toSet())) {
				if(!transitions.containsKey(from)) {
					transitions.put(from, new HashMap<>());
				}
				if(!transitions.get(from).containsKey(event)) {
					transitions.get(from).put(event, new HashSet<>());
				}
				transitions.get(from).get(event).add(s.getName());
			}
			if(s.isInitial()) {
				if(!transitions.containsKey(init)) {
					transitions.put(init, new HashMap<>());
				}
				if(!transitions.get(init).containsKey(event)) {
					transitions.get(init).put(event, new HashSet<>());
				}
				transitions.get(init).get(event).add(s.getName());
			}
		}
		Set<Set<String>> finalStates = new HashSet<>();
		finalStates.add(this.states.stream().map(State::getName).collect(Collectors.toSet()));
		return new Automaton(states, initialStates, transitions, finalStates);
	}

	public enum Abstraction { May, Must }

	public AtlModel createAbstraction(Abstraction kind) {
		if(kind == Abstraction.Must) {
			List<StateCluster> mustStateClusters = AbstractionUtils.getStateClusters(this);
			List<Transition> mustTransitions = AbstractionUtils.getMustTransitions(this, mustStateClusters);
			AtlModel mustAtlModel = this.clone();
			mustAtlModel.setStates(mustStateClusters);
			mustAtlModel.setTransitions(mustTransitions);
			mustAtlModel.setATL(this.getATL().transl(true));
			return mustAtlModel;
		} else {
			List<StateCluster> mayStateClusters = AbstractionUtils.getStateClusters(this);
			List<Transition> mayTransitions = AbstractionUtils.getMayTransitions(this, mayStateClusters);
			AtlModel mayAtlModel = this.clone();
			mayAtlModel.setStates(mayStateClusters);
			mayAtlModel.setTransitions(mayTransitions);
			mayAtlModel.setATL(this.getATL().transl(false));
			return mayAtlModel;
		}
	}

	public static Automaton.Outcome modelCheck(AtlModel model) throws IOException {
		String mcmasProgram = AbstractionUtils.generateMCMASProgram(model, false);
		String fileName = "/tmp/st" + System.currentTimeMillis() + ".ispl";
		while (Files.exists(Paths.get(fileName))) {
			fileName = "/tmp/st" + System.currentTimeMillis() + ".ispl";
		}
		Files.write(Paths.get(fileName), mcmasProgram.getBytes());
		String mcmasOutputMustAtlModel = AbstractionUtils.modelCheck(fileName);
		if (AbstractionUtils.getMcmasResult(mcmasOutputMustAtlModel)) {
			return Automaton.Outcome.True;
		} else {
			return Automaton.Outcome.False;
		}
	}

	public static Automaton.Outcome modelCheck(AtlModel mustAtlModel, AtlModel mayAtlModel) throws IOException {
		String mustMcmasProgram = AbstractionUtils.generateMCMASProgram(mustAtlModel, false);
		String fileName = "/tmp/must" + System.currentTimeMillis() + ".ispl";
		while (Files.exists(Paths.get(fileName))) {
			fileName = "/tmp/must" + System.currentTimeMillis() + ".ispl";
		}
		Files.write(Paths.get(fileName), mustMcmasProgram.getBytes());
		String mcmasOutputMustAtlModel = AbstractionUtils.modelCheck(fileName);
		if (AbstractionUtils.getMcmasResult(mcmasOutputMustAtlModel)) {
			return Automaton.Outcome.True;
		} else {
			String mayMcmasProgram = AbstractionUtils.generateMCMASProgram(mayAtlModel, true);
			fileName = "/tmp/may" + System.currentTimeMillis() + ".ispl";
			while (Files.exists(Paths.get(fileName))) {
				fileName = "/tmp/may" + System.currentTimeMillis() + ".ispl";
			}
			Files.write(Paths.get(fileName), mayMcmasProgram.getBytes());
			String mcmasOutputMayAtlModel = AbstractionUtils.modelCheck(fileName);
			if (!AbstractionUtils.getMcmasResult(mcmasOutputMayAtlModel)) {
				return Automaton.Outcome.False;
			}
		}
		return Automaton.Outcome.Unknown;
	}

	public void updateModel(String atom_tt, String atom_ff, List<? extends State> states) {
		for(State s : this.states) {
			if(states.stream().anyMatch(s1 -> s1.getName().equals(s.getName()))) {
				s.getLabels().add(atom_tt);
				s.getFalseLabels().add(atom_ff);
			} else {
				s.getLabels().add(atom_ff);
				s.getFalseLabels().add(atom_tt);
			}
		}
	}

}
