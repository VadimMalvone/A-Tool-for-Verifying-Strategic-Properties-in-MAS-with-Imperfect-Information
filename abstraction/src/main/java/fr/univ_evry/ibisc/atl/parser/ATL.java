package fr.univ_evry.ibisc.atl.parser;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

public abstract class ATL implements Cloneable {

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof ATL)) { return false; }
        return o.toString().equals(this.toString());
    }

    public abstract boolean isLTL();
    public abstract List<String> getTerms();
    public abstract ATL transl(boolean v);
    @Override
    public abstract ATL clone();

    public static class Atom extends ATL {
        private String atom;

        public Atom(String atom) {
            this.atom = atom;
        }

        @Override
        public String toString() {
            return atom;
        }

        public String getAtom() {
            return atom;
        }

        public void setAtom(String atom) {
            this.atom = atom;
        }

        @Override
        public boolean isLTL() {
            return true;
        }

        @Override
        public List<String> getTerms() {
            List<String> list = new ArrayList<>();
            list.add(atom);
            return list;
        }

        @Override
        public Atom transl(boolean v) {
            if(atom.equals("true")) {
                return new Atom("false");
            } else if (atom.equals("false")) {
                return new Atom("true");
            } else {
                return new Atom(atom + (v ? "_tt" : "_ff"));
            }
        }

        @Override
        public Atom clone() {
            return new Atom(atom);
        }
    }

    public static class Next extends ATL {
        private ATL subFormula;

        public Next(ATL subFormula) {
            this.subFormula = subFormula;
        }

        @Override
        public String toString() {
            return "X(" + subFormula.toString() + ")";
        }

        public ATL getSubFormula() {
            return subFormula;
        }

        public void setSubFormula(ATL subFormula) {
            this.subFormula = subFormula;
        }

        @Override
        public boolean isLTL() {
            return subFormula.isLTL();
        }

        @Override
        public List<String> getTerms() {
            return subFormula.getTerms();
        }

        @Override
        public Next transl(boolean v) {
            return new Next(subFormula.transl(v));
        }

        @Override
        public Next clone() {
            return new Next(this.subFormula.clone());
        }
    }

    public static class And extends ATL {
        private ATL left;
        private ATL right;

        public And(ATL left, ATL right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public String toString() {
            return "(" + left.toString() + " and " + right.toString() + ")";
        }

        public ATL getLeft() {
            return left;
        }

        public void setLeft(ATL left) {
            this.left = left;
        }

        public ATL getRight() {
            return right;
        }

        public void setRight(ATL right) {
            this.right = right;
        }

        @Override
        public boolean isLTL() {
            return left.isLTL() && right.isLTL();
        }

        @Override
        public List<String> getTerms() {
            List<String> list = new ArrayList<>();
            list.addAll(left.getTerms());
            list.addAll(right.getTerms());
            return list;
        }

        @Override
        public And transl(boolean v) {
            return new And(left.transl(v), right.transl(v));
        }

        @Override
        public And clone() {
            return new And(left.clone(), right.clone());
        }
    }

    public static class Or extends ATL {
        private ATL left;
        private ATL right;

        public Or(ATL left, ATL right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public String toString() {
            return "(" + left.toString() + " or " + right.toString() + ")";
        }

        public ATL getLeft() {
            return left;
        }

        public void setLeft(ATL left) {
            this.left = left;
        }

        public ATL getRight() {
            return right;
        }

        public void setRight(ATL right) {
            this.right = right;
        }

        @Override
        public boolean isLTL() {
            return left.isLTL() && right.isLTL();
        }

        @Override
        public List<String> getTerms() {
            List<String> list = new ArrayList<>();
            list.addAll(left.getTerms());
            list.addAll(right.getTerms());
            return list;
        }

        @Override
        public Or transl(boolean v) {
            return new Or(left.transl(v), right.transl(v));
        }

        @Override
        public Or clone() {
            return new Or(left.clone(), right.clone());
        }
    }

    public static class Implies extends ATL {
        private ATL left;
        private ATL right;

        public Implies(ATL left, ATL right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public String toString() {
            return "(" + left.toString() + " -> " + right.toString() + ")";
        }

        public ATL getLeft() {
            return left;
        }

        public void setLeft(ATL left) {
            this.left = left;
        }

        public ATL getRight() {
            return right;
        }

        public void setRight(ATL right) {
            this.right = right;
        }

        @Override
        public boolean isLTL() {
            return left.isLTL() && right.isLTL();
        }

        @Override
        public List<String> getTerms() {
            List<String> list = new ArrayList<>();
            list.addAll(left.getTerms());
            list.addAll(right.getTerms());
            return list;
        }

        @Override
        public Implies transl(boolean v) {
            return new Implies(left.transl(v), right.transl(v));
        }

        @Override
        public Implies clone() {
            return new Implies(left.clone(), right.clone());
        }
    }

    public static class Eventually extends ATL {
        private ATL subFormula;

        public Eventually(ATL subFormula) {
            this.subFormula = subFormula;
        }

        @Override
        public String toString() {
            return "F(" + subFormula.toString() + ")";
        }

        public ATL getSubFormula() {
            return subFormula;
        }

        public void setSubFormula(ATL subFormula) {
            this.subFormula = subFormula;
        }

        @Override
        public boolean isLTL() {
            return subFormula.isLTL();
        }

        @Override
        public List<String> getTerms() {
            return subFormula.getTerms();
        }

        @Override
        public ATL transl(boolean v) {
            if(v) {
                return new Eventually(subFormula.transl(true));
            } else {
                return new Globally(subFormula.transl(false));
            }

        }

        @Override
        public Eventually clone() {
            return new Eventually(subFormula.clone());
        }
    }

    public static class Globally extends ATL {
        private ATL subFormula;

        public Globally(ATL subFormula) {
            this.subFormula = subFormula;
        }

        @Override
        public String toString() {
            return "G(" + subFormula.toString() + ")";
        }

        public ATL getSubFormula() {
            return subFormula;
        }

        public void setSubFormula(ATL subFormula) {
            this.subFormula = subFormula;
        }

        @Override
        public boolean isLTL() {
            return subFormula.isLTL();
        }

        @Override
        public List<String> getTerms() {
            return subFormula.getTerms();
        }

        @Override
        public ATL transl(boolean v) {
            if(v) {
                return new Globally(subFormula.transl(true));
            } else {
                return new Eventually(subFormula.transl(false));
            }
        }

        @Override
        public Globally clone() {
            return new Globally(subFormula.clone());
        }
    }

    public static class Until extends ATL {
        private ATL left;
        private ATL right;

        public Until(ATL left, ATL right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public String toString() {
            return "(" + left.toString() + " U " + right.toString() + ")";
        }

        public ATL getLeft() {
            return left;
        }

        public void setLeft(ATL left) {
            this.left = left;
        }

        public ATL getRight() {
            return right;
        }

        public void setRight(ATL right) {
            this.right = right;
        }

        @Override
        public boolean isLTL() {
            return left.isLTL() && right.isLTL();
        }

        @Override
        public List<String> getTerms() {
            List<String> list = new ArrayList<>();
            list.addAll(left.getTerms());
            list.addAll(right.getTerms());
            return list;
        }

        @Override
        public ATL transl(boolean v) {
//            return v ? new Until(left.transl(true), right.transl(true)) : new Release(left.transl(false), right.transl(false));
            return v ? new Until(left.transl(true), right.transl(true)) : new Or(new Until(right.transl(false), new ATL.And(left.transl(false), right.transl(false))), new Globally(right.transl(false)));
        }

        @Override
        public Until clone() {
            return new Until(left.clone(), right.clone());
        }
    }

    // to be removed
    public static class Release extends ATL {
        private ATL left;
        private ATL right;

        public Release(ATL left, ATL right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public String toString() {
            return "(" + left.toString() + " R " + right.toString() + ")";
        }

        public ATL getLeft() {
            return left;
        }

        public void setLeft(ATL left) {
            this.left = left;
        }

        public ATL getRight() {
            return right;
        }

        public void setRight(ATL right) {
            this.right = right;
        }

        @Override
        public boolean isLTL() {
            return left.isLTL() && right.isLTL();
        }

        @Override
        public List<String> getTerms() {
            List<String> list = new ArrayList<>();
            list.addAll(left.getTerms());
            list.addAll(right.getTerms());
            return list;
        }

        @Override
        public ATL transl(boolean v) {
            return v ? new Release(left.transl(true), right.transl(true)) : new Until(left.transl(false), right.transl(false));
        }

        @Override
        public Release clone() {
            return new Release(left.clone(), right.clone());
        }
    }

    public static class Not extends ATL {
        private ATL subFormula;

        public Not(ATL subFormula) {
            this.subFormula = subFormula;
        }

        @Override
        public String toString() {
            return "not(" + subFormula.toString() + ")";
        }

        public ATL getSubFormula() {
            return subFormula;
        }

        public void setSubFormula(ATL subFormula) {
            this.subFormula = subFormula;
        }

        @Override
        public boolean isLTL() {
            return subFormula.isLTL();
        }

        @Override
        public List<String> getTerms() {
            return subFormula.getTerms();
        }

        @Override
        public ATL transl(boolean v) {
            return subFormula.transl(!v);
        }

        @Override
        public Not clone() {
            return new Not(subFormula.clone());
        }
    }

    public static class Strategic extends ATL {
        private String group;
        private ATL subFormula;

        public Strategic(String group, ATL subFormula) {
            this.group = group;
            this.subFormula = subFormula;
        }

        @Override
        public String toString() { return "<" + group + ">" + subFormula.toString(); }

        public ATL getSubFormula() {
            return subFormula;
        }

        public void setSubFormula(ATL subFormula) {
            this.subFormula = subFormula;
        }

        public String getGroup() { return group; }

        @Override
        public boolean isLTL() {
            return false;
        }

        @Override
        public List<String> getTerms() {
            return subFormula.getTerms();
        }

        @Override
        public Strategic transl(boolean v) {
            return new Strategic(group, subFormula.transl(v));
        }

        @Override
        public Strategic clone() {
            return new Strategic(group, subFormula.clone());
        }
    }

}
