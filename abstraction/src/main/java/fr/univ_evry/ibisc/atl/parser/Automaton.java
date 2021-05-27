package fr.univ_evry.ibisc.atl.parser;

import org.apache.commons.lang3.mutable.MutableInt;
import org.checkerframework.checker.units.qual.A;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Automaton {
    private Set<String> states = new HashSet<>();
    private Map<String, Set<ATL>> stateLTLMap = new HashMap<>();
    private Set<String> initialStates = new HashSet<>();
    private Map<String, Map<Set<ATL>, Set<String>>> transitions = new HashMap<>();
    private Set<Set<String>> finalStates = new HashSet<>();

    public enum Outcome { True, False, Unknown };

    private Automaton() { }

    public Automaton(Set<String> states, Set<String> initialStates, Map<String, Map<Set<ATL>, Set<String>>> transitions, Set<Set<String>> finalStates) {
        this.states = states;
        this.initialStates = initialStates;
        this.transitions = transitions;
        this.finalStates = finalStates;
    }

    public Set<String> getStates() {
        return states;
    }

    public Map<String, Map<Set<ATL>, Set<String>>> getTransitions() {
        return transitions;
    }

    public Set<Set<String>> getFinalStates() {
        return finalStates;
    }

    public Map<String, Set<ATL>> getStateLTLMap() {
        return stateLTLMap;
    }

    public Automaton(ATL property, Set<ATL> closure, Outcome outcome, Set<String> alphabet, boolean explicit) {
        ATL[] closureArr = closure.toArray(new ATL[0]);
        int n = closure.size();
        stateLTLMap = new HashMap<>();
        int id = 0;
        for(int i = 0; i < (1 << n); i++) {
            Set<ATL> elementarySet = new HashSet<>();
            for(int j = 0; j < n; j++) {
                if ((i & (1 << j)) > 0) {
                    elementarySet.add(closureArr[j]);
                }
            }
            if(isElementary(
                    elementarySet,
                    closure.stream().filter(ltl -> ltl instanceof ATL.And).map(ltl -> (ATL.And) ltl).collect(Collectors.toSet()),
                    closure.stream().filter(ltl -> ltl instanceof ATL.Not && ((ATL.Not) ltl).getSubFormula() instanceof ATL.Not).map(ltl -> (ATL.Not) ltl).collect(Collectors.toSet()),
                    closure.stream().filter(ltl -> ltl instanceof ATL.Until).map(ltl -> (ATL.Until) ltl).collect(Collectors.toSet()),
                    closure.contains(new ATL.Atom("true")))) {
                stateLTLMap.put("s_" + id, elementarySet);
                states.add("s_" + id++);
            }
        }
//        for(Set<LTL> set : elementarySets) {
//            System.out.println(set);
//        }
        for(String state : states) {
            if(outcome == Outcome.Unknown) {
                if (!stateLTLMap.get(state).contains(property) && !stateLTLMap.get(state).contains(new ATL.Not(property))) {
                    initialStates.add(state);
                }
            } else if(outcome == Outcome.True) {
                if (stateLTLMap.get(state).contains(property) && !stateLTLMap.get(state).contains(new ATL.Not(property))) {
                    initialStates.add(state);
                }
            } else {
                if (!stateLTLMap.get(state).contains(property) && stateLTLMap.get(state).contains(new ATL.Not(property))) {
                    initialStates.add(state);
                }
            }
        }

        Set<ATL.Next> nexts = closure.stream().filter(ltl -> ltl instanceof ATL.Next).map(ltl -> (ATL.Next) ltl).collect(Collectors.toSet());
        Set<ATL.Not> notNexts = closure.stream().filter(ltl -> ltl instanceof ATL.Not && ((ATL.Not) ltl).getSubFormula() instanceof ATL.Next).map(ltl -> (ATL.Not) ltl).collect(Collectors.toSet());
        Set<ATL.Until> untils = closure.stream().filter(ltl -> ltl instanceof ATL.Until).map(ltl -> (ATL.Until) ltl).collect(Collectors.toSet());
        Set<ATL.Not> notUntils = closure.stream().filter(ltl -> ltl instanceof ATL.Not && ((ATL.Not) ltl).getSubFormula() instanceof ATL.Until).map(ltl -> (ATL.Not) ltl).collect(Collectors.toSet());

        for(String toState : states) {
            Set<ATL> A = stateLTLMap.get(toState).stream().filter(ltl -> ltl instanceof ATL.Atom || (ltl instanceof ATL.Not && ((ATL.Not) ltl).getSubFormula() instanceof ATL.Atom)).collect(Collectors.toSet());
            if(explicit){
                Set<ATL> Aaux = new HashSet<>();
                for(ATL atl : A) {
                    if(atl instanceof ATL.Atom) {
                        if(((ATL.Atom) atl).getAtom().endsWith("_tt")) {
                            Aaux.add(new ATL.Not(new ATL.Atom(((ATL.Atom) atl).getAtom().replace("_tt", "_ff"))));
                        } else {
                            Aaux.add(new ATL.Not(new ATL.Atom(((ATL.Atom) atl).getAtom().replace("_ff", "_tt"))));
                        }
                    }
                }
                A.addAll(Aaux);
            }
            List<Set<ATL>> As =  new ArrayList<>();
            List<String> updatedAlphabet = new ArrayList<>();
            for(String a : alphabet) {
                if(explicit && (A.contains(new ATL.Atom(a + "_tt")) || A.contains(new ATL.Atom(a + "_ff")) || A.contains(new ATL.Not(new ATL.Atom(a + "_tt"))) || A.contains(new ATL.Not(new ATL.Atom(a + "_ff"))))) {
                    continue;
                } else if(!explicit && (A.contains(new ATL.Atom(a)) || A.contains(new ATL.Not(new ATL.Atom(a))))) {
                    continue;
                }
                updatedAlphabet.add(a);
            }
            boolean[] permutation = new boolean[updatedAlphabet.size()];
            if(explicit) {
                for (int i = 0; i < Math.pow(2, updatedAlphabet.size()); i++) {
                    Set<ATL> A1 = new HashSet<>(A);
                    for (int j = 0; j < permutation.length; j++) {
                        if (permutation[j]) {
                            A1.add(new ATL.Atom(updatedAlphabet.get(j) + "_tt"));
                            A1.add(new ATL.Not(new ATL.Atom(updatedAlphabet.get(j) + "_ff")));
                        } else {
                            A1.add(new ATL.Atom(updatedAlphabet.get(j) + "_ff"));
                            A1.add(new ATL.Not(new ATL.Atom(updatedAlphabet.get(j) + "_tt")));
                        }
//                        else {
//                            A1.add(new ATL.Not(new ATL.Atom(updatedAlphabet.get(j) + "_tt")));
//                            A1.add(new ATL.Not(new ATL.Atom(updatedAlphabet.get(j) + "_ff")));
//                        }
                    }
                    As.add(A1);
                    for (int j = 0; j < permutation.length; j++) {
                        if (!permutation[j]) {
                            permutation[j] = true;
                            break;
                        } else {
                            permutation[j] = false;
                        }
                    }
                }
            } else {
                for (int i = 0; i < Math.pow(2, updatedAlphabet.size()); i++) {
                    Set<ATL> A1 = new HashSet<>(A);
                    for (int j = 0; j < permutation.length; j++) {
                        if (permutation[j]) {
                            A1.add(new ATL.Atom(updatedAlphabet.get(j)));
                        } else {
                            A1.add(new ATL.Not(new ATL.Atom(updatedAlphabet.get(j))));
                        }
                    }
                    As.add(A1);
                    for (int j = 0; j < permutation.length; j++) {
                        if (!permutation[j]) {
                            permutation[j] = true;
                            break;
                        } else {
                            permutation[j] = false;
                        }
                    }
                }
            }
            for(String fromState : states) {
                boolean exclude = false;
                for(ATL.Next ltl : nexts) {
                    if (stateLTLMap.get(fromState).contains(ltl) && !stateLTLMap.get(toState).contains(ltl.getSubFormula())) {
                        exclude = true;
                        break;
                    }
                    if (stateLTLMap.get(toState).contains(ltl.getSubFormula()) && !stateLTLMap.get(fromState).contains(ltl)) {
                        exclude = true;
                        break;
                    }
                }
                if(exclude) {
                    continue;
                }
                for(ATL.Not ltl : notNexts) {
                    if (stateLTLMap.get(fromState).contains(ltl) && !stateLTLMap.get(toState).contains(new ATL.Not(((ATL.Next) ltl.getSubFormula()).getSubFormula()))) {
                        exclude = true;
                        break;
                    }
                    if (!stateLTLMap.get(fromState).contains(ltl) && stateLTLMap.get(toState).contains(new ATL.Not(((ATL.Next) ltl.getSubFormula()).getSubFormula()))) {
                        exclude = true;
                        break;
                    }
                }
                if(exclude) {
                    continue;
                }
                for(ATL.Until ltl : untils) {
                    if (stateLTLMap.get(fromState).contains(ltl) && !stateLTLMap.get(fromState).contains(ltl.getRight()) &&
                            (!stateLTLMap.get(fromState).contains(ltl.getLeft()) || !stateLTLMap.get(toState).contains(ltl))) {
                        exclude = true;
                        break;
                    }
                    if (!stateLTLMap.get(fromState).contains(ltl) && (stateLTLMap.get(fromState).contains(ltl.getRight()) ||
                            (stateLTLMap.get(fromState).contains(ltl.getLeft()) && stateLTLMap.get(toState).contains(ltl)))) {
                        exclude = true;
                        break;
                    }
                }
                if(exclude) {
                    continue;
                }
                for(ATL.Not ltl : notUntils) {
                    if (stateLTLMap.get(fromState).contains(ltl) &&
                            (!stateLTLMap.get(fromState).contains(new ATL.Not(((ATL.Until) ltl.getSubFormula()).getRight())) ||
                            (!stateLTLMap.get(fromState).contains((new ATL.Not(((ATL.Until) ltl.getSubFormula()).getLeft()))) &&
                                !stateLTLMap.get(toState).contains(ltl)))) {
                        exclude = true;
                        break;
                    }
                    if (!stateLTLMap.get(fromState).contains(ltl) &&
                            (stateLTLMap.get(fromState).contains(new ATL.Not(((ATL.Until) ltl.getSubFormula()).getRight())) &&
                                    (stateLTLMap.get(fromState).contains((new ATL.Not(((ATL.Until) ltl.getSubFormula()).getLeft()))) ||
                                            stateLTLMap.get(toState).contains(ltl)))) {
                        exclude = true;
                        break;
                    }
                }
                if(exclude) {
                    continue;
                }

                for(Set<ATL> A1 : As) {
                    if(transitions.containsKey(fromState)) {
                        if(transitions.get(fromState).containsKey(A1)) { //.stream().map(String::valueOf).collect(Collectors.joining()))) {
                            transitions.get(fromState).get(A1).add(toState); //.stream().map(String::valueOf).collect(Collectors.joining())).add(toState);
                        } else {
                            Set<String> aux = new HashSet<>();
                            aux.add(toState);
                            transitions.get(fromState).put(A1, aux); //.stream().map(String::valueOf).collect(Collectors.joining()), aux);
                        }
                    } else {
                        Map<Set<ATL>, Set<String>> aux1 = new HashMap<>();
                        Set<String> aux2 = new HashSet<>();
                        aux2.add(toState);
                        aux1.put(A1, aux2); //.stream().map(String::valueOf).collect(Collectors.joining()), aux2);
                        transitions.put(fromState, aux1);
                    }
                }
            }
        }
        for(ATL.Until ltl : untils) {
            Set<String> finalState = new HashSet<>();
            for(String state : states) {
                if(!stateLTLMap.get(state).contains(ltl) || stateLTLMap.get(state).contains(ltl.getRight())) {
                    finalState.add(state);
                }
            }
            finalStates.add(finalState);
        }
        for(ATL.Not ltl : notUntils) {
            Set<String> finalState = new HashSet<>();
            for(String state : states) {
                if(!stateLTLMap.get(state).contains(ltl) ||
                    (stateLTLMap.get(state).contains(new ATL.Not(((ATL.Until) ltl.getSubFormula()).getLeft())) &&
                        stateLTLMap.get(state).contains(new ATL.Not(((ATL.Until) ltl.getSubFormula()).getRight())))) {
                    finalState.add(state);
                }
            }
            finalStates.add(finalState);
        }
    }

    private boolean isElementary(Set<ATL> set, Set<ATL.And> ands, Set<ATL.Not> notnots, Set<ATL.Until> untils, boolean trueBelongsToClosure) {
//        if(set.isEmpty()) {
//            return false;
//        }
        if(trueBelongsToClosure && !set.contains(new ATL.Atom("true"))) {
            return false;
        }
        for(ATL.And ltl : ands) {
            if(set.contains(ltl) && (!set.contains(ltl.getLeft()) || !set.contains(ltl.getRight()))) {
                return false;
            }
            if(!set.contains(ltl) && set.contains(ltl.getLeft()) && set.contains(ltl.getRight())) {
                return false;
            }
        }
        for(ATL.Not ltl : notnots) {
            if(set.contains(((ATL.Not) ltl.getSubFormula()).getSubFormula()) && !set.contains(ltl)) {
                return false;
            }
            if(!set.contains(((ATL.Not) ltl.getSubFormula()).getSubFormula()) && set.contains(ltl)) {
                return false;
            }
        }
        for(ATL ATL : set) {
            if(set.contains(new ATL.Not(ATL))) {
                return false;
            }
        }
        for(ATL.Until until : untils) {
            if(set.contains(until.getRight()) && !set.contains(until)) {
                return false;
            }
            if(set.contains(new ATL.Not(until)) && !set.contains(new ATL.Not(until.getRight()))) {
                return false;
            }
            if(set.contains(until) && !set.contains(until.getRight()) && !set.contains(until.getLeft())) {
                return false;
            }
            if(set.contains(new ATL.Not(until.getLeft())) && !set.contains(new ATL.Not(until)) && set.contains(new ATL.Not(until.getRight()))) {
                return false;
            }
        }
        return true;
    }

    public void addStates(String... states) {
        this.states.addAll(Arrays.asList(states.clone()));
    }

    public void removeStates(String... state) {
        this.states.removeAll(Arrays.asList(states.toArray()));
    }

    public boolean hasState(String state) {
        return states.contains(state);
    }

    public void setInitialStates(String... initialStates) {
        this.initialStates.addAll(Arrays.asList(initialStates.clone()));
    }

    public Set<String> getInitialStates() {
        return initialStates;
    }

    public void addTransition(String fromState, Set<ATL> event, String... toStates) {
        Set<String> reachedStates = new HashSet<>(Arrays.asList(toStates.clone()));
        if(transitions.containsKey(fromState)) {
            if(transitions.get(fromState).containsKey(event)) {
                transitions.get(fromState).get(event).addAll(reachedStates);
            } else {
                transitions.get(fromState).put(event, reachedStates);
            }
        } else {
            Map<Set<ATL>, Set<String>> aux = new HashMap<>();
            aux.put(event, reachedStates);
            transitions.put(fromState, aux);
        }
    }

    public void removeTransition(String fromState, Set<ATL> event, String... toStates) {
        if(transitions.containsKey(fromState) && transitions.get(fromState).containsKey(event)) {
            transitions.get(fromState).get(event).removeAll(Arrays.asList(toStates.clone()));
            if(transitions.get(fromState).get(event).isEmpty()) {
                transitions.get(fromState).remove(event);
            }
            if(transitions.get(fromState).isEmpty()) {
                transitions.remove(fromState);
            }
        }
    }

    @SafeVarargs
    public final void addFinalStates(Set<String>... finalStates) {
        this.finalStates.addAll(Arrays.asList(finalStates.clone()));
    }

    @SafeVarargs
    public final void removeFinalStates(Set<String>... finalStates) {
        this.finalStates.removeAll(Arrays.asList(finalStates.clone()));
    }

    public Set<String> next(String state, Set<ATL> event) {
        if(!states.contains(state)) return new HashSet<>();
        Set<String> result = new HashSet<>();
        for(Map.Entry<Set<ATL>, Set<String>> trans : transitions.get(state).entrySet()) {
            if(trans.getKey().equals(event)) {
                result.addAll(trans.getValue());
            }
        }
        return result;
    }

    public Automaton degeneralise() {
        if(finalStates.size() == 1) {
            return new Automaton(this.states, this.initialStates, this.transitions, this.finalStates);
        } else {
            Automaton result = null;
            for(Set<String> set : finalStates) {
                Set<Set<String>> singleFinalStates = new HashSet<>();
                singleFinalStates.add(set);
                if(result == null) {
                    result = new Automaton(this.states, this.initialStates, this.transitions, singleFinalStates);
                } else {
                    Automaton aux = new Automaton(this.states, this.initialStates, this.transitions, singleFinalStates);
                    result = result.product(aux);
                }
            }
            return result;
        }

    }

    public Automaton product(Automaton automaton) {
         Set<String> states = new HashSet<>();
         Set<String> initialStates = new HashSet<>();
         Map<String, Map<Set<ATL>, Set<String>>> transitions = new HashMap<>();
         Set<Set<String>> finalStates = new HashSet<>();

         for(String s1 : this.states) {
             for(String s2 : automaton.states) {
                 states.add(s1 + "_" + s2 + "_a");
                 states.add(s1 + "_" + s2 + "_b");
             }
         }
         for(String i1 : this.initialStates) {
             for(String i2 : automaton.initialStates) {
                 initialStates.add(i1 + "_" + i2 + "_a");
             }
         }
         Set<String> finalStatesAux = new HashSet<>();
         for(Set<String> fs1 : this.finalStates) {
             for(Set<String> fs2 : automaton.finalStates) {
                 for(String f1 : fs1) {
                     for(String f2 : fs2) {
                         finalStatesAux.add(f1 + "_" + f2 + "_b");
                     }
                 }
             }
         }
         finalStates.add(finalStatesAux);
         for(String s1 : this.transitions.keySet()) {
             for(String s2 : automaton.transitions.keySet()) {
                 for(Set<ATL> event1 : this.transitions.get(s1).keySet()) {
                     for(Set<ATL> event2 : automaton.transitions.get(s2).keySet()) {
                         if(event1.equals(event2)) {
                             if(!transitions.containsKey(s1 + "_" + s2 + "_a")) {
                                 transitions.put(s1 + "_" + s2 + "_a", new HashMap<>());
                             }
                             if(!transitions.get(s1 + "_" + s2 + "_a").containsKey(event1)) {
                                 transitions.get(s1 + "_" + s2 + "_a").put(event1, new HashSet<>());
                             }
                             for(String next1 : this.transitions.get(s1).get(event1)) {
                                 String label = this.finalStates.stream().noneMatch(f -> f.contains(next1)) ? "_a" : "_b";
                                 for(String next2 : automaton.transitions.get(s2).get(event2)) {
                                     transitions.get(s1 + "_" + s2 + "_a").get(event1).add(next1 + "_" + next2 + label);
                                 }
                             }

                             if(!transitions.containsKey(s1 + "_" + s2 + "_b")) {
                                 transitions.put(s1 + "_" + s2 + "_b", new HashMap<>());
                             }
                             if(!transitions.get(s1 + "_" + s2 + "_b").containsKey(event1)) {
                                 transitions.get(s1 + "_" + s2 + "_b").put(event1, new HashSet<>());
                             }
                             for(String next1 : this.transitions.get(s1).get(event1)) {
                                 for(String next2 : automaton.transitions.get(s2).get(event2)) {
                                     String label = automaton.finalStates.stream().noneMatch(f -> f.contains(next2)) ? "_b" : "_a";
                                     transitions.get(s1 + "_" + s2 + "_b").get(event1).add(next1 + "_" + next2 + label);
                                 }
                             }
                         }
                     }
                 }
             }
         }
         return new Automaton(states, initialStates, transitions, finalStates);
    }

    public Automaton getPath() {
        for(String initial : initialStates) {
            Automaton path = getPathAux(new MutableInt(0), initial, new HashMap<>());
            if(path != null) {
                return path;
            }
        }
        return null;
    }

    private Automaton getPathAux(MutableInt depth, String state, Map<String, Integer> visited) {
        if(visited.containsKey(state)) {
            System.out.println("Already visited: " + state + " with depth " + depth.intValue());
            for(String finalState : finalStates.stream().findFirst().get()) {
                if(visited.containsKey(finalState) && visited.get(finalState) >= visited.get(state)) {
                    Set<String> states = new HashSet<>();
                    Set<String> initialStates = new HashSet<>();
                    Map<String, Map<Set<ATL>, Set<String>>> transitions = new HashMap<>();
                    Set<Set<String>> finalStates = new HashSet<>();
                    states.add(state);
                    finalStates.add(Stream.of(finalState).collect(Collectors.toSet()));
                    return new Automaton(states, initialStates, transitions, finalStates);
                }
            }
        } else {
            int depthAux = depth.intValue();
            visited.put(state, depth.intValue());
            depth.increment();
            Map<String, Set<Set<ATL>>> events = new HashMap<>();
            for(Map.Entry<Set<ATL>, Set<String>> next : this.transitions.get(state).entrySet()) {
                for(String nextState : next.getValue()) {
                    if (!events.containsKey(nextState)) {
                        events.put(nextState, new HashSet<>());
                    }
                    events.get(nextState).add(next.getKey());
                }
            }
            for(Map.Entry<String, Set<Set<ATL>>> next : events.entrySet()) {
                String nextState = next.getKey();
                Automaton path = getPathAux(depth, nextState, visited);
                if(path != null) {
                    path.states.add(state);
                    Map<Set<ATL>, Set<String>> auxMap = new HashMap<>();
                    for(Set<ATL> event : next.getValue()) {
                        auxMap.put(event, Stream.of(nextState).collect(Collectors.toSet()));
                    }
                    if(depthAux == 0) {
                        path.initialStates.add(state);
                    }
                    path.transitions.put(state, auxMap);
                    return path;
                }
            }
        }
        visited.remove(state);
        return null;
    }

}
