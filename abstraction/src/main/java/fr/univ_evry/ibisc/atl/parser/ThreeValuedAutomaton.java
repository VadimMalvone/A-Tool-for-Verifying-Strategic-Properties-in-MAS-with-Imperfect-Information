package fr.univ_evry.ibisc.atl.parser;

import fr.univ_evry.ibisc.atl.abstraction.beans.State;

import java.util.*;
import java.util.stream.Collectors;


public class ThreeValuedAutomaton {
    private Set<String> states = new HashSet<>();
    private Set<String> initialStates = new HashSet<>();
    private Map<String, Map<String, Set<String>>> transitions = new HashMap<>();
    private Set<Set<String>> finalStates = new HashSet<>();

    private ThreeValuedAutomaton() { }

    public ThreeValuedAutomaton(Set<String> states, Set<String> initialStates, Map<String, Map<String, Set<String>>> transitions, Set<Set<String>> finalStates) {
        this.states = states;
        this.initialStates = initialStates;
        this.transitions = transitions;
        this.finalStates = finalStates;
    }

    public ThreeValuedAutomaton(LTL property, Set<LTL> closure) {
        LTL[] closureArr = closure.toArray(new LTL[0]);
        int n = closure.size();
        Map<String, Set<LTL>> elementarySets = new HashMap<>();
        int id = 0;
        for(int i = 0; i < (1 << n); i++) {
            Set<LTL> elementarySet = new HashSet<>();
            for(int j = 0; j < n; j++) {
                if ((i & (1 << j)) > 0) {
                    elementarySet.add(closureArr[j]);
                }
            }
            if(isElementary(
                    elementarySet,
                    closure.stream().filter(ltl -> ltl instanceof LTL.And).map(ltl -> (LTL.And) ltl).collect(Collectors.toSet()),
                    closure.stream().filter(ltl -> ltl instanceof LTL.Not && ((LTL.Not) ltl).getSubFormula() instanceof LTL.Not).map(ltl -> (LTL.Not) ltl).collect(Collectors.toSet()),
                    closure.stream().filter(ltl -> ltl instanceof LTL.Until).map(ltl -> (LTL.Until) ltl).collect(Collectors.toSet()),
                    closure.contains(new LTL.Atom("true")))) {
                elementarySets.put("s_" + id, elementarySet);
                states.add("s_" + id++);
            }
        }
//        for(Set<LTL> set : elementarySets) {
//            System.out.println(set);
//        }
        for(String state : states) {
            if(!elementarySets.get(state).contains(property) && !elementarySets.get(state).contains(new LTL.Not(property))) {
                initialStates.add(state);
            }
        }

        Set<LTL.Next> nexts = closure.stream().filter(ltl -> ltl instanceof LTL.Next).map(ltl -> (LTL.Next) ltl).collect(Collectors.toSet());
        Set<LTL.Not> notNexts = closure.stream().filter(ltl -> ltl instanceof LTL.Not && ((LTL.Not) ltl).getSubFormula() instanceof LTL.Next).map(ltl -> (LTL.Not) ltl).collect(Collectors.toSet());
        Set<LTL.Until> untils = closure.stream().filter(ltl -> ltl instanceof LTL.Until).map(ltl -> (LTL.Until) ltl).collect(Collectors.toSet());
        Set<LTL.Not> notUntils = closure.stream().filter(ltl -> ltl instanceof LTL.Not && ((LTL.Not) ltl).getSubFormula() instanceof LTL.Until).map(ltl -> (LTL.Not) ltl).collect(Collectors.toSet());
        for(String fromState : states) {
            Set<LTL> A = elementarySets.get(fromState).stream().filter(ltl -> ltl instanceof LTL.Atom || (ltl instanceof LTL.Not && ((LTL.Not) ltl).getSubFormula() instanceof LTL.Atom)).collect(Collectors.toSet());
            for(String toState : states) {
                boolean exclude = false;
                for(LTL.Next ltl : nexts) {
                    if (elementarySets.get(fromState).contains(ltl) && !elementarySets.get(toState).contains(ltl.getSubFormula())) {
                        exclude = true;
                        break;
                    }
                    if (elementarySets.get(toState).contains(ltl.getSubFormula()) && !elementarySets.get(fromState).contains(ltl)) {
                        exclude = true;
                        break;
                    }
                }
                if(exclude) {
                    continue;
                }
                for(LTL.Not ltl : notNexts) {
                    if (elementarySets.get(fromState).contains(ltl) && !elementarySets.get(toState).contains(new LTL.Not(((LTL.Next) ltl.getSubFormula()).getSubFormula()))) {
                        exclude = true;
                        break;
                    }
                    if (!elementarySets.get(fromState).contains(ltl) && elementarySets.get(toState).contains(new LTL.Not(((LTL.Next) ltl.getSubFormula()).getSubFormula()))) {
                        exclude = true;
                        break;
                    }
                }
                if(exclude) {
                    continue;
                }
                for(LTL.Until ltl : untils) {
                    if (elementarySets.get(fromState).contains(ltl) && !elementarySets.get(fromState).contains(ltl.getRight()) &&
                            (!elementarySets.get(fromState).contains(ltl.getLeft()) || !elementarySets.get(toState).contains(ltl))) {
                        exclude = true;
                        break;
                    }
                    if (!elementarySets.get(fromState).contains(ltl) && (elementarySets.get(fromState).contains(ltl.getRight()) ||
                            (elementarySets.get(fromState).contains(ltl.getLeft()) && elementarySets.get(toState).contains(ltl)))) {
                        exclude = true;
                        break;
                    }
                }
                if(exclude) {
                    continue;
                }
                for(LTL.Not ltl : notUntils) {
                    if (elementarySets.get(fromState).contains(ltl) &&
                            (!elementarySets.get(fromState).contains(new LTL.Not(((LTL.Until) ltl.getSubFormula()).getRight())) ||
                            (!elementarySets.get(fromState).contains((new LTL.Not(((LTL.Until) ltl.getSubFormula()).getLeft()))) &&
                                !elementarySets.get(toState).contains(ltl)))) {
                        exclude = true;
                        break;
                    }
                    if (!elementarySets.get(fromState).contains(ltl) &&
                            (elementarySets.get(fromState).contains(new LTL.Not(((LTL.Until) ltl.getSubFormula()).getRight())) &&
                                    (elementarySets.get(fromState).contains((new LTL.Not(((LTL.Until) ltl.getSubFormula()).getLeft()))) ||
                                            elementarySets.get(toState).contains(ltl)))) {
                        exclude = true;
                        break;
                    }
                }
                if(exclude) {
                    continue;
                }
                if(transitions.containsKey(fromState)) {
                    if(transitions.get(fromState).containsKey(A.stream().map(String::valueOf).collect(Collectors.joining()))) {
                        transitions.get(fromState).get(A.stream().map(String::valueOf).collect(Collectors.joining())).add(toState);
                    } else {
                        Set<String> aux = new HashSet<>();
                        aux.add(toState);
                        transitions.get(fromState).put(A.stream().map(String::valueOf).collect(Collectors.joining()), aux);
                    }
                } else {
                    Map<String, Set<String>> aux1 = new HashMap<>();
                    Set<String> aux2 = new HashSet<>();
                    aux2.add(toState);
                    aux1.put(A.stream().map(String::valueOf).collect(Collectors.joining()), aux2);
                    transitions.put(fromState, aux1);
                }
            }
        }
        for(LTL.Until ltl : untils) {
            Set<String> finalState = new HashSet<>();
            for(String state : states) {
                if(!elementarySets.get(state).contains(ltl) || elementarySets.get(state).contains(ltl.getRight())) {
                    finalState.add(state);
                }
            }
            finalStates.add(finalState);
        }
        for(LTL.Not ltl : notUntils) {
            Set<String> finalState = new HashSet<>();
            for(String state : states) {
                if(!elementarySets.get(state).contains(ltl) ||
                    (elementarySets.get(state).contains(new LTL.Not(((LTL.Until) ltl.getSubFormula()).getLeft())) &&
                        elementarySets.get(state).contains(new LTL.Not(((LTL.Until) ltl.getSubFormula()).getRight())))) {
                    finalState.add(state);
                }
            }
            finalStates.add(finalState);
        }

        int pippo = 0;
    }

    private boolean isElementary(Set<LTL> set, Set<LTL.And> ands, Set<LTL.Not> notnots, Set<LTL.Until> untils, boolean trueBelongsToClosure) {
//        if(set.isEmpty()) {
//            return false;
//        }
        if(trueBelongsToClosure && !set.contains(new LTL.Atom("true"))) {
            return false;
        }
        for(LTL.And ltl : ands) {
            if(set.contains(ltl) && (!set.contains(ltl.getLeft()) || !set.contains(ltl.getRight()))) {
                return false;
            }
            if(!set.contains(ltl) && set.contains(ltl.getLeft()) && set.contains(ltl.getRight())) {
                return false;
            }
        }
        for(LTL.Not ltl : notnots) {
            if(set.contains(((LTL.Not) ltl.getSubFormula()).getSubFormula()) && !set.contains(ltl)) {
                return false;
            }
            if(!set.contains(((LTL.Not) ltl.getSubFormula()).getSubFormula()) && set.contains(ltl)) {
                return false;
            }
        }
        for(LTL ltl : set) {
            if(set.contains(new LTL.Not(ltl))) {
                return false;
            }
        }
        for(LTL.Until until : untils) {
            if(set.contains(until.getRight()) && !set.contains(until)) {
                return false;
            }
            if(set.contains(new LTL.Not(until)) && !set.contains(new LTL.Not(until.getRight()))) {
                return false;
            }
            if(set.contains(until) && !set.contains(until.getRight()) && !set.contains(until.getLeft())) {
                return false;
            }
            if(set.contains(new LTL.Not(until.getLeft())) && !set.contains(new LTL.Not(until)) && set.contains(new LTL.Not(until.getRight()))) {
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

    public void addTransition(String fromState, String event, String... toStates) {
        Set<String> reachedStates = new HashSet<>(Arrays.asList(toStates.clone()));
        if(transitions.containsKey(fromState)) {
            if(transitions.get(fromState).containsKey(event)) {
                transitions.get(fromState).get(event).addAll(reachedStates);
            } else {
                transitions.get(fromState).put(event, reachedStates);
            }
        } else {
            Map<String, Set<String>> aux = new HashMap<>();
            aux.put(event, reachedStates);
            transitions.put(fromState, aux);
        }
    }

    public void removeTransition(String fromState, String event, String... toStates) {
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

}
