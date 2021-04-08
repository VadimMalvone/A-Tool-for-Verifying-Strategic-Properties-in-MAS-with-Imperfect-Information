package fr.univ_evry.ibisc.atl.parser;

public abstract class LTL {

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof LTL)) { return false; }
        return o.toString().equals(this.toString());
    }

    public static class Atom extends LTL {
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
    }

    public static class Next extends LTL {
        private LTL subFormula;

        public Next(LTL subFormula) {
            this.subFormula = subFormula;
        }

        @Override
        public String toString() {
            return "X(" + subFormula.toString() + ")";
        }

        public LTL getSubFormula() {
            return subFormula;
        }

        public void setSubFormula(LTL subFormula) {
            this.subFormula = subFormula;
        }
    }

    public static class And extends LTL {
        private LTL left;
        private LTL right;

        public And(LTL left, LTL right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public String toString() {
            return "(" + left.toString() + " and " + right.toString() + ")";
        }

        public LTL getLeft() {
            return left;
        }

        public void setLeft(LTL left) {
            this.left = left;
        }

        public LTL getRight() {
            return right;
        }

        public void setRight(LTL right) {
            this.right = right;
        }
    }

    public static class Or extends LTL {
        private LTL left;
        private LTL right;

        public Or(LTL left, LTL right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public String toString() {
            return "(" + left.toString() + " and " + right.toString() + ")";
        }

        public LTL getLeft() {
            return left;
        }

        public void setLeft(LTL left) {
            this.left = left;
        }

        public LTL getRight() {
            return right;
        }

        public void setRight(LTL right) {
            this.right = right;
        }
    }

    public static class Implies extends LTL {
        private LTL left;
        private LTL right;

        public Implies(LTL left, LTL right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public String toString() {
            return "(" + left.toString() + " U " + right.toString() + ")";
        }

        public LTL getLeft() {
            return left;
        }

        public void setLeft(LTL left) {
            this.left = left;
        }

        public LTL getRight() {
            return right;
        }

        public void setRight(LTL right) {
            this.right = right;
        }
    }

    public static class Eventually extends LTL {
        private LTL subFormula;

        public Eventually(LTL subFormula) {
            this.subFormula = subFormula;
        }

        @Override
        public String toString() {
            return "F(" + subFormula.toString() + ")";
        }

        public LTL getSubFormula() {
            return subFormula;
        }

        public void setSubFormula(LTL subFormula) {
            this.subFormula = subFormula;
        }
    }

    public static class Globally extends LTL {
        private LTL subFormula;

        public Globally(LTL subFormula) {
            this.subFormula = subFormula;
        }

        @Override
        public String toString() {
            return "F(" + subFormula.toString() + ")";
        }

        public LTL getSubFormula() {
            return subFormula;
        }

        public void setSubFormula(LTL subFormula) {
            this.subFormula = subFormula;
        }
    }

    public static class Until extends LTL {
        private LTL left;
        private LTL right;

        public Until(LTL left, LTL right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public String toString() {
            return "(" + left.toString() + " U " + right.toString() + ")";
        }

        public LTL getLeft() {
            return left;
        }

        public void setLeft(LTL left) {
            this.left = left;
        }

        public LTL getRight() {
            return right;
        }

        public void setRight(LTL right) {
            this.right = right;
        }

    }

    public static class Not extends LTL {
        private LTL subFormula;

        public Not(LTL subFormula) {
            this.subFormula = subFormula;
        }

        @Override
        public String toString() {
            return "not(" + subFormula.toString() + ")";
        }

        public LTL getSubFormula() {
            return subFormula;
        }

        public void setSubFormula(LTL subFormula) {
            this.subFormula = subFormula;
        }

    }



}
