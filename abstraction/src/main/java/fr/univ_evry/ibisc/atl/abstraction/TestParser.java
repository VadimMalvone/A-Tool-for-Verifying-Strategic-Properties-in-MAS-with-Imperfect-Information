package fr.univ_evry.ibisc.atl.abstraction;

import fr.univ_evry.ibisc.atl.abstraction.beans.*;
import fr.univ_evry.ibisc.atl.parser.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TestParser {

    public static void main(String[] args) throws Exception {
        String expression = "a U b";
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
        alphabet.add("a_tt"); alphabet.add("a_ff");
        alphabet.add("b_tt"); alphabet.add("b_ff");
        Automaton automaton = new Automaton(pt, pt.getClosure(), Automaton.Outcome.Unknown, alphabet, true);
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

        System.out.println("Output: " + AbstractionUtils.modelCheckingProcedure(atlModel));

        int pippo = 0;
    }

}
