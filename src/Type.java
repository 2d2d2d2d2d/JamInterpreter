import java.util.Arrays;

interface Type {

}


class UnitType implements Type {
    private UnitType() {}
    public static final Type ONLY = new UnitType();
    public String toString() { return "unit"; }
    public boolean equals(Object other) { return other == ONLY; }
}


class IntType implements Type {
    private IntType() {}
    public static final Type ONLY = new IntType();
    public String toString() { return "int"; }
    public boolean equals(Object other) { return other == ONLY; }
}


class BoolType implements Type {
    private BoolType() {}
    public static final Type ONLY = new BoolType();
    public String toString() { return "bool"; }
    public boolean equals(Object other) { return other == ONLY; }
}


class RefType implements Type {
    private Type type;
    public RefType(Type t) { type = t; }
    public String toString() { return "[ref " + type.toString() + "]"; }
    public Type getRefType() { return type; }
    public boolean equals(Object other) {
        return other != null && other.getClass() == this.getClass() && ((RefType)other).getRefType().equals(this.getRefType());
    }
}


class ListType implements Type {
    private Type type;
    public ListType(Type t) { type = t; }
    public String toString() { return "[list " + type.toString() + "]"; }
    public Type getListType() { return type; }
    public boolean equals(Object other) {
        return other != null && other.getClass() == this.getClass() && ((ListType)other).getListType().equals(this.getListType());
    }
}


class ClosureType implements Type {
    private Type[] args;
    private Type ret;
    public ClosureType(Type[] a, Type r) { args = a; ret = r; }
    public String toString() { return "[" + Arrays.toString(args) + " -> " + ret.toString() + "]"; }
    public Type[] getArgTypes() { return args; }
    public Type getRetType() { return ret; }
    public boolean equals(Object other) {
       if (other != null && other.getClass() == this.getClass()) {
           ClosureType otherClosure = (ClosureType)other;
           Type[] otherArgs = otherClosure.getArgTypes();
           Type otherRet = otherClosure.getRetType();
           if (otherArgs.length != this.args.length) return false;
           for (int i = 0; i < this.args.length; i++) {
               if (! otherArgs[i].equals(this.args[i])) return false;
           }
           if (! otherRet.equals(this.ret)) return false;
           return true;
       }
       return false;
    }
}


