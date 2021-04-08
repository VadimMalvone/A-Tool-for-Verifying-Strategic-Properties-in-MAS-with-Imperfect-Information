package fr.univ_evry.ibisc.atl.parser;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.HashSet;
import java.util.Set;

public class ClosureLTLVisitor extends LTLBaseVisitor<LTL> {
    private Set<LTL> closure = new HashSet<>();

    public ClosureLTLVisitor() {
        //closure.add(new LTL.Atom("true"));
    }

    public Set<LTL> getClosure() {
        return closure;
    }

    @Override
    public LTL visitImplies(LTLParser.ImpliesContext ctx) {
        LTL.Implies implies = new LTL.Implies(visit(ctx.left), visit(ctx.right));
        LTL.Not not = new LTL.Not(implies);
//        LTL.Not notNot = new LTL.Not(not);
        closure.add(implies);
        closure.add(not);
//        closure.add(notNot);
        return implies;
    }

    @Override
    public LTL visitNegation(LTLParser.NegationContext ctx) {
        LTL.Not not = new LTL.Not(visit(ctx.getChild(1)));
        LTL.Not notNot = new LTL.Not(not);
        closure.add(not);
        closure.add(notNot);
        return not;
    }

    @Override
    public LTL visitNext(LTLParser.NextContext ctx) {
        LTL.Next next = new LTL.Next(visit(ctx.getChild(1)));
        LTL.Not not = new LTL.Not(next);
//        LTL.Not notNot = new LTL.Not(not);
        closure.add(next);
        closure.add(not);
//        closure.add(notNot);
        return next;
    }

    @Override
    public LTL visitEventually(LTLParser.EventuallyContext ctx) {
        LTL.Globally eventually = new LTL.Globally(visit(ctx.getChild(1)));
        LTL.Not not = new LTL.Not(eventually);
//        LTL.Not notNot = new LTL.Not(not);
        closure.add(eventually);
        closure.add(not);
//        closure.add(notNot);
        return eventually;
    }

    @Override
    public LTL visitConjunction(LTLParser.ConjunctionContext ctx) {
        LTL.And and = new LTL.And(visit(ctx.left), visit(ctx.right));
        LTL.Not not = new LTL.Not(and);
//        LTL.Not notNot = new LTL.Not(not);
        closure.add(and);
        closure.add(not);
//        closure.add(notNot);
        return and;
    }

    @Override
    public LTL visitAlways(LTLParser.AlwaysContext ctx) {
        LTL.Globally globally = new LTL.Globally(visit(ctx.getChild(1)));
        LTL.Not not = new LTL.Not(globally);
//        LTL.Not notNot = new LTL.Not(not);
        closure.add(globally);
        closure.add(not);
//        closure.add(notNot);
        return globally;
    }

    @Override
    public LTL visitUntil(LTLParser.UntilContext ctx) {
        LTL.Until until = new LTL.Until(visit(ctx.left), visit(ctx.right));
        LTL.Not not = new LTL.Not(until);
//        LTL.Not notNot = new LTL.Not(not);
        closure.add(until);
        closure.add(not);
//        closure.add(notNot);
        return until;
    }

    @Override
    public LTL visitAtomExpr(LTLParser.AtomExprContext ctx) {
        LTL.Atom atom = new LTL.Atom(ctx.getText());
        LTL.Not notAtom = new LTL.Not(atom);
//        LTL.Not notNotAtom = new LTL.Not(notAtom);
        closure.add(atom);
        closure.add(notAtom);
//        closure.add(notNotAtom);
        return atom;
    }


}
