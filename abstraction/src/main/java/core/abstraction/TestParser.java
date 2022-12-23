package core.abstraction;

import com.google.common.base.Stopwatch;
import core.abstraction.model.*;
import core.parser.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.*;
import java.util.stream.Collectors;

public class TestParser {

    public static void main(String[] args) throws Exception {
        int nProcesses = 3;
        long startTime, endTime, duration;
        CGSModel model = GenerateScheduler.generate(nProcesses);
        AbstractionUtils.validateCGS(model);

        startTime = System.nanoTime();
        System.out.println("Model: " + CGSModel.modelCheck(model));
        endTime = System.nanoTime();
        duration = (endTime - startTime) / 1000000;
        System.out.println("#### Execution time: " + duration + " [ms]");

        CGSModel mustModel = model.createAbstraction(CGSModel.Abstraction.Must, (s ->
        {
            for(int i = 1; i <= nProcesses; i++) {
                if(s.getLabels().contains("wt"+i+"_tt")) {
                    return true;
                }
            }
            return false;
        }));
        CGSModel mayModel = model.createAbstraction(CGSModel.Abstraction.May, (s ->
        {
            for(int i = 1; i <= nProcesses; i++) {
                if(s.getLabels().contains("wt"+i+"_tt")) {
                    return true;
                }
            }
            return false;
        }));

        startTime = System.nanoTime();
        System.out.println("Abstract model: " + CGSModel.modelCheck3(mustModel, mayModel));
        endTime = System.nanoTime();
        duration = (endTime - startTime) / 1000000;
        System.out.println("#### Execution time: " + duration + " [ms]");
//
//
//        startTime = System.nanoTime();
//        System.out.println("May model: " + CGSModel.modelCheck(mayModel));
//        endTime = System.nanoTime();
//        duration = (endTime - startTime) / 1000000;
//        System.out.println("#### Execution time: " + duration + " [ms]");

//        List<State> statesToAbstract = new ArrayList<>();
//        statesToAbstract.add(model.getStates().get(new Random().nextInt(model.getStates().size())));
//        statesToAbstract.add(model.getStates().get(new Random().nextInt(model.getStates().size())));
//        statesToAbstract.stream().map(s -> s.getName() + " - ").forEach(System.out::print);
//        System.out.println();
//        CGSModel mustModel = model.createAbstraction(CGSModel.Abstraction.Must, statesToAbstract);
//        System.out.println(CGSModel.modelCheck(mustModel));
//        CGSModel mayModel = model.createAbstraction(CGSModel.Abstraction.May, statesToAbstract);
//        System.out.println(CGSModel.modelCheck(mayModel));

//        String expression = "Exists x. (X a and Forall y. (ag1, x) F p)";
//        CharStream codePointCharStream = CharStreams.fromString(expression);
//        StrategyLogicLexer lexer = new StrategyLogicLexer(codePointCharStream);
//        StrategyLogicParser parser = new StrategyLogicParser(new CommonTokenStream(lexer));
//        ParseTree tree = parser.slExpr();
//        StrategyLogicVisitorImpl visitor = new StrategyLogicVisitorImpl();
//        StrategyLogic property = visitor.visit(tree).normalForm();

//        ATLVisitorImpl ptVisitor = new ATLVisitorImpl();
//        ptVisitor.visit(new ATLParser(new CommonTokenStream(new ATLLexer(CharStreams.fromString(pt.toString())))).atlExpr());
//
//        AtlModel atlModel = JsonObject.load(AbstractionUtils.readSampleFile(), AtlModel.class);
//        AbstractionUtils.validateAtlModel(atlModel, false);
//
//        FileWriter file = new FileWriter("test.ispl");
//        file.write(AbstractionUtils.generateMCMASProgram(atlModel, false));
//        file.close();

//        Set<String> alphabet = new HashSet<>();
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

//        Stopwatch stopwatch = Stopwatch.createStarted();
////        System.out.println("Output: " + AbstractionUtils.modelCheckingProcedure(atlModel));
//        System.out.println("Output: " + AtlModel.modelCheck(atlModel));
//        stopwatch.stop();
//        System.out.println(stopwatch.elapsed().toMillis() + " [ms]");
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
