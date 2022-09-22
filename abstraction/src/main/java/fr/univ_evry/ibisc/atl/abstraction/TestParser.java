package fr.univ_evry.ibisc.atl.abstraction;

import com.google.common.base.Stopwatch;
import fr.univ_evry.ibisc.atl.abstraction.beans.*;
import fr.univ_evry.ibisc.atl.parser.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class TestParser {

    private static void runExperiments(int minStates, int maxStates, int minAgents, int maxAgents, int depth, int nRuns) throws Exception {
//        File file = new File("./tmp/results" + minStates + "_" + maxStates + "_" + minAgents + "_" + maxAgents + "_" + percImperfect1 + ".csv");
//        FileWriter fw = new FileWriter(file);
        Random rnd = new Random();
        for(double percImperfect = 0.1; percImperfect <= 1; percImperfect+=0.1) {
            int nSuccesses = 0;
            int nSuccessesBR = 0;
            //int nRights = 0;
            int nSuccessesSt = 0;
            long avgMCMASTime = 0;
            long avgOursTime = 0;
            long avgBRTime = 0;
            for (int i = 0; i < nRuns; i++) {
                //System.out.println("Run n. " + i + " of " + nRuns);
                int nStates = rnd.nextInt(maxStates - minStates + 1) + minStates;
                List<State> states = new ArrayList<>();
                int initial = rnd.nextInt(nStates);
                for (int j = 0; j < nStates; j++) {
                    State s = new State();
                    s.setName("s" + j);
                    if (initial == j) {
                        s.setInitial(true);
                    }
                    states.add(s);
                }
                int nAgents = rnd.nextInt(maxAgents - minAgents + 1) + minAgents;
                List<Agent> agents = new ArrayList<>();
                for (int j = 0; j < nAgents; j++) {
                    Agent a = new Agent();
                    a.setName("a" + j);
                    int nActions = rnd.nextInt(maxStates - minStates + 1) + minStates;
                    List<String> actions = new ArrayList<>();
                    for (int k = 0; k < nActions; k++) {
                        actions.add("act" + k);
                    }
                    int nImpStates = (int) (percImperfect * nStates / 2);
                    List<List<String>> indStates = new ArrayList<>();
                    for (int k = 0; k < nImpStates; k++) {
                        List<String> ind = new ArrayList<>();
                        int si = rnd.nextInt(nStates);
                        int sj;
                        do {
                            sj = rnd.nextInt(nStates);
                        } while (si == sj);
                        ind.add("s" + si);
                        ind.add("s" + sj);
                        indStates.add(ind);
                    }
                    a.setActions(actions);
                    a.setIndistinguishableStates(indStates);
                    agents.add(a);
                }
                int nTransitions = rnd.nextInt(nStates * (nStates - 1)) + 1;
                List<Transition> transitions = new ArrayList<>();
                for (int j = 0; j < nStates; j++) {
                    int si = j;
                    int sj = rnd.nextInt(nStates);
                    createTransition(agents, transitions, si, sj);
                }
                for (int j = 0; j < nTransitions; j++) {
                    int si = rnd.nextInt(nStates);
                    int sj = rnd.nextInt(nStates);
                    Transition transition = new Transition();
                    transition.setFromState("s" + si);
                    transition.setToState("s" + sj);
                    List<List<AgentAction>> agentActs = new ArrayList<>();
                    List<AgentAction> acts = new ArrayList<>();
                    for (Agent agent : agents) {
                        AgentAction act = new AgentAction();
                        act.setAgent(agent.getName());
                        Collections.shuffle(agent.getActions());
                        act.setAction(agent.getActions().stream().findAny().get());
                        acts.add(act);
                    }
                    agentActs.add(acts);
                    transition.setAgentActions(agentActs);
                    if (!transitions.contains(transition)) {
                        transitions.add(transition);
                    }
                }
                int groupSize = rnd.nextInt(nAgents) + 1;
                Group group = new Group();
                group.setName("g");
                group.setAgents(agents.stream().limit(groupSize).map(Agent::getName).collect(Collectors.toList()));

                int si = rnd.nextInt(nStates);
                ATL formula = rnd.nextBoolean() ? new ATL.Eventually(new ATL.Atom("s" + si)) : rnd.nextBoolean() ? new ATL.Globally(new ATL.Atom("s" + si)) : new ATL.Next(new ATL.Atom("s" + si));
                formula = new ATL.Existential("g", formula);
                ATL fAux = formula.clone();
                for (int j = 0; j < depth; j++) {
                    int sj = rnd.nextInt(nStates);
                    ATL subFormula = rnd.nextBoolean() ? new ATL.Eventually(new ATL.Atom("s" + si)) : rnd.nextBoolean() ? new ATL.Globally(new ATL.Atom("s" + sj)) : new ATL.Next(new ATL.Atom("s" + si));
                    subFormula = new ATL.Existential("g", subFormula);
                    fAux = new ATL.And(fAux, subFormula);
                }
                AtlModel m = new AtlModel();
                m.setStates(states);
                m.setAgents(agents);
                m.setTransitions(transitions);
                List<Group> groups = new ArrayList<>();
                groups.add(group);
                m.setGroups(groups);
                m.setATL(formula);
                m.makeTransitionsUnique();
                try {
                    Stopwatch stopwatch = Stopwatch.createStarted();
                    Automaton.Outcome res = AbstractionUtils.modelCheckingProcedure(m);
                    stopwatch.stop();
                    avgOursTime += stopwatch.elapsed().toMillis();
                    if (res == Automaton.Outcome.True) {
                        nSuccesses++;
                    } else if (res == Automaton.Outcome.False) {
                        nSuccesses++;
                    }
                    stopwatch = Stopwatch.createStarted();
                    res = AbstractionUtils.modelCheckingProcedureBR(m, 20);
                    stopwatch.stop();
                    avgBRTime += stopwatch.elapsed().toMillis();
                    if (res == Automaton.Outcome.True) {
                        nSuccessesBR++;
                    } else if (res == Automaton.Outcome.False) {
                        nSuccessesBR++;
                    }
                } catch (Exception e){ i--; }
            }
            System.out.println("Perc: " + percImperfect);
            System.out.println("Number of times our procedure returns a final verdict: " + nSuccesses + " (" + (((double) nSuccesses) / nRuns * 100) + "%)" + "[" + (avgOursTime/nRuns) + " ms]");
            System.out.println("Number of times bounded recall procedure returns a final verdict: " + nSuccessesBR + " (" + (((double) nSuccessesBR) / nRuns * 100) + "%)" + "[" + (avgBRTime/nRuns) + " ms]");
        }
    }

    private static void createTransition(List<Agent> agents, List<Transition> transitions, int si, int sj) {
        Transition transition = new Transition();
        transition.setFromState("s" + si);
        transition.setToState("s" + sj);
        List<List<AgentAction>> agentActs = new ArrayList<>();
        List<AgentAction> acts = new ArrayList<>();
        for (Agent agent : agents) {
            AgentAction act = new AgentAction();
            act.setAgent(agent.getName());
            Collections.shuffle(agent.getActions());
            act.setAction(agent.getActions().stream().findAny().get());
            acts.add(act);
        }
        agentActs.add(acts);
        transition.setAgentActions(agentActs);
        transitions.add(transition);
    }

    public static AtlModel modify(AtlModel m, int nStatesToAdd, int propertyDepthToAdd) {
        AtlModel res = m.clone();
        Random rnd = new Random();
        for(int i = 0; i < nStatesToAdd; i++) {
            State state = new State();
            state.setName("newAddedState" + i);
            State aux = m.getStates().get(rnd.nextInt(m.getStates().size()));
            state.getLabels().addAll(aux.getLabels());
            state.getFalseLabels().addAll(aux.getFalseLabels());
            ((List<State>) (res.getStates())).add(state);
            int nTr = rnd.nextInt(3); //m.getStates().size()/2);
            for(int j = 0; j < nTr; j++) {
                int index = rnd.nextInt(m.getStates().size());
                Transition transition = new Transition();
                transition.setFromState(m.getStates().get(index).getName());
                transition.setToState("newAddedState" + i);
                transition.setAgentActions(m.getTransitions().get(rnd.nextInt(m.getTransitions().size())).getAgentActions());
                res.getTransitions().add(transition);
            }
        }
        for(int i = 0; i < propertyDepthToAdd; i++) {
            if(i % 3 == 0) {
                res.setATL(new ATL.Existential("g2", new ATL.Eventually(res.getATL())));
            } else if(i % 3 == 1){
                res.setATL(new ATL.Existential("g1", new ATL.Eventually(res.getATL())));
            } else {
                res.setATL(new ATL.Existential("g2", new ATL.Next(res.getATL())));
            }
        }
        return res;
    }

    public static boolean good;

    public static void main(String[] args) throws Exception {
        String expression = "(X a || X b) && (G F c)";
        CharStream codePointCharStream = CharStreams.fromString(expression);
        ATLLexer lexer = new ATLLexer(codePointCharStream);
        ATLParser parser = new ATLParser(new CommonTokenStream(lexer));
        ParseTree tree = parser.atlExpr();
        ATLVisitorImpl visitor = new ATLVisitorImpl();
        ATL property = visitor.visit(tree).normalForm();
        ATL pt = property.transl(true);
        ATL pf = property.transl(false);
        ATLVisitorImpl ptVisitor = new ATLVisitorImpl();
        ptVisitor.visit(new ATLParser(new CommonTokenStream(new ATLLexer(CharStreams.fromString(pt.toString())))).atlExpr());

        AtlModel atlModel = JsonObject.load(AbstractionUtils.readSampleFile(), AtlModel.class);
        AbstractionUtils.validateAtlModel(atlModel);

        Set<String> alphabet = new HashSet<>();
//        for(List<String> labels : atlModel.getStates().stream().map(State::getLabels).collect(Collectors.toList())) {
//            alphabet.addAll(labels);
//        }

//        alphabet.add("a_tt"); alphabet.add("a_ff");
//        alphabet.add("b_tt"); alphabet.add("b_ff");
//        alphabet.add("c_tt"); alphabet.add("c_ff");
//        Automaton automaton = new Automaton(pt, pt.getClosure(), Automaton.Outcome.Unknown, alphabet, true);
//        FileWriter fw = new FileWriter("automaton.dot");
//        fw.write(automaton.toDot());
//        fw.close();
//        fw = new FileWriter("automaton.hoa");
//        fw.write(automaton.toHOA());
//        fw.close();
//
//
//
//        AbstractionUtils.processDefaultTransitions(atlModel);
//        List<StateCluster> mustStateClusters = AbstractionUtils.getStateClusters(atlModel);
//        List<Transition> mustTransitions = AbstractionUtils.getMustTransitions(atlModel, mustStateClusters);
//
//        AtlModel mustAtlModel = JsonObject.load(AbstractionUtils.readSampleFile(), AtlModel.class);
//        mustAtlModel.setStates(mustStateClusters);
//        mustAtlModel.setTransitions(mustTransitions);
//
//        Automaton automaton1 = atlModel.toAutomaton();
//        Automaton product = automaton.product(automaton1);
//
//
//        Automaton path = product.getPath();

        Stopwatch stopwatch = Stopwatch.createStarted();
        System.out.println("Output: " + AbstractionUtils.modelCheckingProcedure(atlModel));
        stopwatch.stop();
        System.out.println(stopwatch.elapsed().toMillis() + " [ms]");
        // ours: True, 1160 [ms]
        // BR: ?, 215 [ms]

//        runExperiments(5, 5, 2, 2, 10, 1000);

//        int nRuns = 5;
//        int nStates = 100;
//        int dFormula = 10;
//        fw = new FileWriter("results.txt");
//        fw.write("");
//        fw.close();
//        for(int n = 1; n < nStates; n++) {
//            for (int nf = 0; nf < dFormula; nf++) {
//                int nSuccesses = 0;
//                double avgOursTime = 0.0, maxOursTime = 0.0, minOursTime = Double.MAX_VALUE;
//                for (int i = 0; i < nRuns; ) {
////                    try{
//                        AtlModel m = modify(atlModel, n, nf);
//                        good = false;
//                        Stopwatch stopwatch = Stopwatch.createStarted();
//                        Automaton.Outcome res = AbstractionUtils.modelCheckingProcedure(m);
//                        stopwatch.stop();
//                        if(!good) {
//                            avgOursTime += stopwatch.elapsed().toMillis();
//                            if (stopwatch.elapsed().toMillis() > maxOursTime) {
//                                maxOursTime = stopwatch.elapsed().toMillis();
//                            }
//                            if (stopwatch.elapsed().toMillis() < minOursTime) {
//                                minOursTime = stopwatch.elapsed().toMillis();
//                            }
//                            if (res == Automaton.Outcome.True) {
//                                nSuccesses++;
//                            } else if (res == Automaton.Outcome.False) {
//                                nSuccesses++;
//                            }
//                            i++;
//                        }
////                    } catch (Exception e) {
////                        i--; System.out.println("error");
////                    }
//                }
//                System.out.println("states " + (n+atlModel.getStates().size()) + ", depth formula " + (nf+3) + ": (" + (((double) nSuccesses) / nRuns * 100) + "%)" + "[" + minOursTime + "ms , " + (avgOursTime / nRuns) + "ms , " + maxOursTime + " ms]");
//                fw = new FileWriter("results.txt", true);
//                fw.write("" + (n+atlModel.getStates().size()) + ", " + (nf+3) + ", " + (((double) nSuccesses) / nRuns * 100) + ", " + minOursTime + ", " + (avgOursTime / nRuns) + ", " + maxOursTime + "\n");
//                fw.close();
//            }
//        }
    }

}
