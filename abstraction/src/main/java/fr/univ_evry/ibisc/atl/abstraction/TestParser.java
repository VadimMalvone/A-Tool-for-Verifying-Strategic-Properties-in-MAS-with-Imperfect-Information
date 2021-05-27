package fr.univ_evry.ibisc.atl.abstraction;

import fr.univ_evry.ibisc.atl.abstraction.beans.*;
import fr.univ_evry.ibisc.atl.parser.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestParser {

    public static void main(String[] args) throws Exception {
        String expression = "<g1> (b U r1)";
        CharStream codePointCharStream = CharStreams.fromString(expression);
        ATLLexer lexer = new ATLLexer(codePointCharStream);
        ATLParser parser = new ATLParser(new CommonTokenStream(lexer));
        ParseTree tree = parser.atlExpr();
        ClosureLTLVisitor visitor = new ClosureLTLVisitor();
        ATL property = visitor.visit(tree);
        ATL pt = property.transl(true);
        ATL pf = property.transl(false);
        System.out.println("Result: " + visitor.getClosure());
        ClosureLTLVisitor ptVisitor = new ClosureLTLVisitor();
        ptVisitor.visit(new ATLParser(new CommonTokenStream(new ATLLexer(CharStreams.fromString(pt.toString())))).atlExpr());

        AtlModel atlModel = JsonObject.load(AbstractionUtils.readSampleFile(), AtlModel.class);

        Set<String> alphabet = new HashSet<>();
        for(List<String> labels : atlModel.getStates().stream().map(State::getLabels).collect(Collectors.toList())) {
            alphabet.addAll(labels);
        }
        Automaton automaton = new Automaton(pt, ptVisitor.getClosure(), Automaton.Outcome.Unknown, alphabet, true);


        AbstractionUtils.validateAtlModel(atlModel);
        AbstractionUtils.processDefaultTransitions(atlModel);
        List<StateCluster> mustStateClusters = AbstractionUtils.getStateClusters(atlModel);
//        List<StateCluster> mayStateClusters = AbstractionUtils.getStateClusters(atlModel);
        List<Transition> mustTransitions = AbstractionUtils.getMustTransitions(atlModel, mustStateClusters);
//        List<Transition> mayTransitions  = AbstractionUtils.getMayTransitions(atlModel, mayStateClusters);

        //List<State> states = stateClusters.stream().map(StateCluster::toState).collect(Collectors.toList());

        AtlModel mustAtlModel = JsonObject.load(AbstractionUtils.readSampleFile(), AtlModel.class);
        mustAtlModel.setStates(mustStateClusters);
        mustAtlModel.setTransitions(mustTransitions);

        Automaton automaton1 = atlModel.toAutomaton();
        Automaton product = automaton.product(automaton1);


        Automaton path = product.getPath();


//        AtlModel product = AtlModel.product(mustAtlModel, automaton);

        int pippo = 0;
    }

}
