package fr.univ_evry.ibisc.atl.abstraction;

import fr.univ_evry.ibisc.atl.parser.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class TestParser {

    public static void main(String[] args) {
        String expression = "a U b";
        CharStream codePointCharStream = CharStreams.fromString(expression);
        LTLLexer lexer = new LTLLexer(codePointCharStream);
        LTLParser parser = new LTLParser(new CommonTokenStream(lexer));
        ParseTree tree = parser.ltlExpr();
        ClosureLTLVisitor visitor = new ClosureLTLVisitor();
        LTL property = visitor.visit(tree);
        System.out.println("Result: " + visitor.getClosure());
        ThreeValuedAutomaton automaton = new ThreeValuedAutomaton(property, visitor.getClosure());
    }

}
