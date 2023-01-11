package fr.univ_evry.ibisc.atl.abstraction;

import com.google.common.base.Stopwatch;
import fr.univ_evry.ibisc.atl.abstraction.beans.AtlModel;
import fr.univ_evry.ibisc.atl.abstraction.beans.JsonObject;
import fr.univ_evry.ibisc.atl.abstraction.beans.State;
import fr.univ_evry.ibisc.atl.parser.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class MainInformationSharing {
    public static void main(String[] args) throws Exception {
        File sampleFile = new ClassPathResource(args[0]).getFile();
        AtlModel atlModel = JsonObject.load(new String(FileUtils.readFileToByteArray(sampleFile)), AtlModel.class);
        AbstractionUtils.validateAtlModel(atlModel, false);
//        CharStream codePointCharStream = CharStreams.fromString(args[1]);
//        ATLLexer lexer = new ATLLexer(codePointCharStream);
//        ATLParser parser = new ATLParser(new CommonTokenStream(lexer));
//        ParseTree tree = parser.atlExpr();
//        ATLVisitorImpl visitor = new ATLVisitorImpl();
//        ATL formula1 = visitor.visit(tree).normalForm();
//
//        codePointCharStream = CharStreams.fromString(args[2]);
//        lexer = new ATLLexer(codePointCharStream);
//        parser = new ATLParser(new CommonTokenStream(lexer));
//        tree = parser.atlExpr();
//        visitor = new ATLVisitorImpl();
//        ATL formula2 = visitor.visit(tree).normalForm();
        List<String> aux = Files.readAllLines(Path.of(args[1]));
        List<List<String>> trace = new ArrayList<>();
        List<Pair<Set<String>, Set<String>>> objectives = new ArrayList<>();
        for (String line : aux) {
            if (line.startsWith("---")) {
                trace.add(null);
                Set<String> obj1 = new HashSet<>(List.of(line.substring(line.indexOf("{") + 1, line.indexOf("}")).replace(" ", "").split(",")));
                line = line.substring(line.indexOf("|") + 1);
                Set<String> obj2 = new HashSet<>(List.of(line.substring(line.indexOf("{") + 1, line.indexOf("}")).replace(" ", "").split(",")));
                objectives.add(new MutablePair<>(obj1, obj2));
            } else {
                trace.add(List.of(line.replace(" ", "").split(",")));
            }
        }

        int bound = Integer.MAX_VALUE;
        if (args.length == 5) {
            bound = Integer.parseInt(args[4]);
        }
        FileWriter fw1 = new FileWriter("trace1.txt");
        fw1.write("");
        fw1.close();
        FileWriter fw2 = new FileWriter("trace2.txt");
        fw2.write("");
        fw2.close();
        int i = 1, oi = 0;
        Set<String> previouslyAchievedObjectives = new HashSet<>();
        boolean found;
        do {
            found = false;
            List<String> terms1 = new ArrayList<>(objectives.get(oi).getLeft().stream().map(t -> (t + "0")).toList());
            Collections.shuffle(terms1);
            Set<String> selectedTerms1 = new HashSet<>(terms1.subList(0, Math.min(terms1.size(), bound)));

            List<String> terms2 = new ArrayList<>(objectives.get(oi).getRight().stream().map(t -> (t + "1")).toList());
            Collections.shuffle(terms2);
            Set<String> selectedTerms2 = new HashSet<>(terms2.subList(0, Math.min(terms2.size(), bound)));

            atlModel.setFormula("<g1>F(" + String.join(" and ", selectedTerms1) + " and " + String.join(" and ", selectedTerms2) + ")");
            atlModel.setATL(null);
            atlModel.getATL();

            System.out.println("Monitor 1 shares { " + String.join(", ", selectedTerms1) + " } with Monitor 2 in exchange of { " + String.join(", ", selectedTerms2) + " }" );

            List<State> possibleInitialStates = new ArrayList<>();
            Set<String> termsByDesign1 = new HashSet<>();
            for (State state : atlModel.getStates()) {
                termsByDesign1.addAll(state.getLabels().stream().filter(t -> t.endsWith("1")).toList());
            }
            Set<String> termsByDesign2 = new HashSet<>();
            for (State state : atlModel.getStates()) {
                termsByDesign2.addAll(state.getLabels().stream().filter(t -> t.endsWith("0")).toList());
            }

            if (!previouslyAchievedObjectives.isEmpty()) {
                for (State s : atlModel.getStates()) {
                    if (new HashSet<>(s.getLabels()).containsAll(previouslyAchievedObjectives)) {
                        possibleInitialStates.add(s);
                    }
                }
            } else {
                possibleInitialStates.add(atlModel.getStates().stream().filter(State::isInitial).findAny().get());
            }
            for (State initialState : possibleInitialStates) {
                for (State aux1 : atlModel.getStates()) {
                    aux1.setInitial(aux1.getName().equals(initialState.getName()));
                }
                if (AtlModel.modelCheck(atlModel) == Automaton.Outcome.True) {
                    found = true;
                    previouslyAchievedObjectives = new HashSet<>();
                    previouslyAchievedObjectives.addAll(selectedTerms1);
                    previouslyAchievedObjectives.addAll(selectedTerms2);
                    List<List<String>> newTrace1 = new ArrayList<>();
                    for (int j = i; j < trace.size(); j++) {
                        List<String> event = trace.get(j);
                        if (event == null) {
                            break;
                        } else {
                            Set<String> event1 = new HashSet<>();
                            Set<String> terms = new HashSet<>();
                            terms.addAll(selectedTerms1);
                            terms.addAll(termsByDesign1);
                            for (String atom : terms) {
                                if (event.contains(atom.substring(0, atom.length() - 1))) {
                                    event1.add(atom.substring(0, atom.length() - 1));
                                }
                            }
                            newTrace1.add(new ArrayList<>(event1));
                        }

                    }
                    List<List<String>> newTrace2 = new ArrayList<>();
                    for (int j = i; j < trace.size(); j++) {
                        List<String> event = trace.get(j);
                        if (event == null) {
                            i = j + 1;
                            break;
                        } else {
                            Set<String> event2 = new HashSet<>();
                            Set<String> terms = new HashSet<>();
                            terms.addAll(selectedTerms2);
                            terms.addAll(termsByDesign2);
                            for (String atom : terms) {
                                if (event.contains(atom.substring(0, atom.length() - 1))) {
                                    event2.add(atom.substring(0, atom.length() - 1));
                                }
                            }
                            newTrace2.add(new ArrayList<>(event2));
                        }
                    }
                    oi++;
                    fw1 = new FileWriter("trace1.txt", true);
                    for (List<String> event : newTrace1) {
                        if (event == null) break;
                        fw1.write(String.join(", ", event) + "\n");
                    }
                    fw1.close();
                    fw2 = new FileWriter("trace2.txt", true);
                    for (List<String> event : newTrace2) {
                        if (event == null) break;
                        fw2.write(String.join(", ", event) + "\n");
                    }
                    fw2.close();
                    break;
                }
            }
        } while(found && oi < objectives.size());
        if (!found) System.out.println("Not found!");

//        int[] selector = new int[trace.size()];
//        selector[0] = -1;
//        while (true) {
//            int i;
//            for (i = 0; i < selector.length; i++) {
//                if (selector[i] < atlModel.getStates().size()-1) {
//                    selector[i]++;
//                    break;
//                } else {
//                    selector[i] = 0;
//                }
//            }
//            if (i == selector.length) {
//                System.out.println("Not found!");
//                return;
//            }
//            i = selector.length - 1;
//            StringBuilder path = new StringBuilder("<g1>X(" + atlModel.getStates().get(selector[i--]).getName() + ")");
//            for (; i >= 0; i--) {
//                path = new StringBuilder("<g1>X(" + atlModel.getStates().get(selector[i]).getName() + " and " + path + ")");
//            }
//            atlModel.setFormula("<g1>F(<g1>G(" + String.join(" and ", formula1.getTerms().stream().map(t -> (t+"0")).toList()) + " and " + String.join(" and ", formula2.getTerms().stream().map(t -> (t+"1")).toList()) + "))" + " and " + path);
//            atlModel.setATL(null);
//            atlModel.getATL();
//            if (AtlModel.modelCheck(atlModel) == Automaton.Outcome.True) {
//                System.out.println(path);
//                List<List<String>> newTrace1 = new ArrayList<>();
//                i = 0;
//                for (List<String> event : trace) {
//                    Set<String> event1 = new HashSet<>();
//                    for (String atom : atlModel.getStates().get(selector[i]).getLabels()) {
//                        if (atom.endsWith("0") && event.contains(atom.substring(0, atom.length() - 1))) {
//                            event1.add(atom.substring(0, atom.length() - 1));
//                        }
//                    }
//                    newTrace1.add(new ArrayList<>(event1));
//                    i++;
//                }
//                List<List<String>> newTrace2 = new ArrayList<>();
//                i = 0;
//                for (List<String> event : trace) {
//                    Set<String> event2 = new HashSet<>();
//                    for (String atom : atlModel.getStates().get(selector[i]).getLabels()) {
//                        if (atom.endsWith("1") && event.contains(atom.substring(0, atom.length() - 1))) {
//                            event2.add(atom.substring(0, atom.length() - 1));
//                        }
//                    }
//                    newTrace2.add(new ArrayList<>(event2));
//                    i++;
//                }
//                FileWriter fw1 = new FileWriter("trace1.txt");
//                for (List<String> event : newTrace1) {
//                    fw1.write(String.join(", ", event) + "\n");
//                }
//                fw1.close();
//                FileWriter fw2 = new FileWriter("trace2.txt");
//                for (List<String> event : newTrace2) {
//                    fw2.write(String.join(", ", event) + "\n");
//                }
//                fw2.close();
//                return;
//            }
//        }
    }
}
