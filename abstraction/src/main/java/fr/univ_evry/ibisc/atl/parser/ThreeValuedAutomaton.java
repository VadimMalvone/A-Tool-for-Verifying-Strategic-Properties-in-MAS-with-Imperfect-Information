package fr.univ_evry.ibisc.atl.parser;

import java.util.*;
import java.util.stream.Collectors;


public class ThreeValuedAutomaton {
    private Set<String> states = new HashSet<>();
    private Map<String, Set<ATL>> stateLTLMap = new HashMap<>();
    private Set<String> initialStates = new HashSet<>();
    private Map<String, Map<Set<ATL>, Set<String>>> transitions = new HashMap<>();
    private Set<Set<String>> finalStates = new HashSet<>();

    private ThreeValuedAutomaton() { }

    public ThreeValuedAutomaton(Set<String> states, Set<String> initialStates, Map<String, Map<Set<ATL>, Set<String>>> transitions, Set<Set<String>> finalStates) {
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

    public ThreeValuedAutomaton(ATL property, Set<ATL> closure) {
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
            if(!stateLTLMap.get(state).contains(property) && !stateLTLMap.get(state).contains(new ATL.Not(property))) {
                initialStates.add(state);
            }
        }

        Set<ATL.Next> nexts = closure.stream().filter(ltl -> ltl instanceof ATL.Next).map(ltl -> (ATL.Next) ltl).collect(Collectors.toSet());
        Set<ATL.Not> notNexts = closure.stream().filter(ltl -> ltl instanceof ATL.Not && ((ATL.Not) ltl).getSubFormula() instanceof ATL.Next).map(ltl -> (ATL.Not) ltl).collect(Collectors.toSet());
        Set<ATL.Until> untils = closure.stream().filter(ltl -> ltl instanceof ATL.Until).map(ltl -> (ATL.Until) ltl).collect(Collectors.toSet());
        Set<ATL.Not> notUntils = closure.stream().filter(ltl -> ltl instanceof ATL.Not && ((ATL.Not) ltl).getSubFormula() instanceof ATL.Until).map(ltl -> (ATL.Not) ltl).collect(Collectors.toSet());
        for(String fromState : states) {
            Set<ATL> A = stateLTLMap.get(fromState).stream().filter(ltl -> ltl instanceof ATL.Atom || (ltl instanceof ATL.Not && ((ATL.Not) ltl).getSubFormula() instanceof ATL.Atom)).collect(Collectors.toSet());
            for(String toState : states) {
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
                if(transitions.containsKey(fromState)) {
                    if(transitions.get(fromState).containsKey(A)) { //.stream().map(String::valueOf).collect(Collectors.joining()))) {
                        transitions.get(fromState).get(A).add(toState); //.stream().map(String::valueOf).collect(Collectors.joining())).add(toState);
                    } else {
                        Set<String> aux = new HashSet<>();
                        aux.add(toState);
                        transitions.get(fromState).put(A, aux); //.stream().map(String::valueOf).collect(Collectors.joining()), aux);
                    }
                } else {
                    Map<Set<ATL>, Set<String>> aux1 = new HashMap<>();
                    Set<String> aux2 = new HashSet<>();
                    aux2.add(toState);
                    aux1.put(A, aux2); //.stream().map(String::valueOf).collect(Collectors.joining()), aux2);
                    transitions.put(fromState, aux1);
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

}
