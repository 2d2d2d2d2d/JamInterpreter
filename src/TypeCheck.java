import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class TypeCheck {
    /** type checking */
    public static void check(AST ast) throws SyntaxException {
        ast.accept(new TypeVisitor(new HashMap<String, Type>()));
    }
}

class TypeVisitor implements ASTVisitor<Type> {
    
    /** Environment of the context */
    private HashMap<String, Type> env;
    
    public TypeVisitor(HashMap<String, Type> env) { this.env = env; }
    
    @Override
    public Type forBoolConstant(BoolConstant b) {
        return BoolType.ONLY;
    }

    @Override
    public Type forIntConstant(IntConstant i) {
        return IntType.ONLY;
    }

    @Override
    public Type forNullConstant(NullConstant n) {
        return n.getDataType();
    }

    @Override
    public Type forVariable(Variable v) {
        return this.env.get(v.name());
    }

    @Override
    public Type forPrimFun(PrimFun f) {
        return f.accept(new PrimFunTypeVisitor(this.env, new AST[0]));
    }

    @Override
    public Type forUnOpApp(UnOpApp u) {
        return u.rator().accept(new UnOpTypeVisitor(this.env, u.arg()));
    }

    @Override
    public Type forBinOpApp(BinOpApp b) {
        return b.rator().accept(new BinOpTypeVisitor(this.env, b.arg1(), b.arg2()));
    }

    @Override
    public Type forApp(App a) {
        if (a.rator() instanceof PrimFun) {
            return ((PrimFun)a.rator()).accept(new PrimFunTypeVisitor(this.env, a.args()));
        }
        
        Type ratorType = a.rator().accept(this);
        List<Type> argsTypeArray = new ArrayList<Type>();
        
        for (AST arg : a.args()) {
            argsTypeArray.add(arg.accept(this));
        }
        
        if (ratorType instanceof ClosureType) {
            Type[] ratorArgsType = ((ClosureType)ratorType).getArgTypes();
            if (ratorArgsType.length == argsTypeArray.size()) {
                for (int i = 0; i < ratorArgsType.length; i++) {
                    if (! ratorArgsType[i].equals(argsTypeArray.get(i))) {
                        throw new TypeException("type " + ratorArgsType[i] + " and " + argsTypeArray.get(i)
                        + " do not match in closure");
                    }
                }
                return ((ClosureType)ratorType).getRetType();
            }
            else {
                throw new TypeException("number of arguments does not match");
            }
        }
        throw new TypeException("invalid type for a closure");
    }

    @Override
    public Type forMap(Map m) {
        HashMap<String, Type> new_env = new HashMap<String, Type>();
        List<Type> argsTypeArray = new ArrayList<Type>();
        for (Variable var : m.vars()) {
            new_env.put(var.name(), var.getDataType());
            argsTypeArray.add(var.getDataType());
        }
        new_env.putAll(this.env);
        return new ClosureType(argsTypeArray.toArray(new Type[0]), m.body().accept(new TypeVisitor(new_env)));
    }

    @Override
    public Type forIf(If i) {
        Type test = i.test().accept(this);
        Type conseq = i.conseq().accept(this);
        Type alt = i.alt().accept(this);
        if (test == BoolType.ONLY) {
            if (conseq.equals(alt))
                return conseq;
            throw new TypeException("type " + conseq + " and " + alt + " do not match in if branch");
        }
        throw new TypeException("if condition must be bool type");
    }

    @Override
    public Type forLet(Let l) {
        HashMap<String, Type> new_env = new HashMap<String, Type>();
        for(Def def : l.defs()) {
            new_env.put(def.lhs().name(), def.lhs().getDataType());
        }
        new_env.putAll(this.env);
        
        for(Def def : l.defs()) {
            Type ltype = def.lhs().accept(new TypeVisitor(new_env));
            Type rtype = def.rhs().accept(new TypeVisitor(new_env));
            if (! ltype.equals(rtype))
                throw new TypeException("type " + ltype + " and " + rtype + " do not match in Def");
        }
        
        return l.body().accept(new TypeVisitor(new_env));
    }

    @Override
    public Type forBlock(Block b) {
        Type type = null;
        for(AST exp : b.exps()) {
            type = exp.accept(this);
        }
        return type;
    }
    
}

class PrimFunTypeVisitor implements PrimFunVisitor<Type> {
    
    /** Environment of the context */
    private HashMap<String, Type> env;
    
    private AST[] args;
    
    public PrimFunTypeVisitor(HashMap<String, Type> env, AST[] args) { this.env = env; this.args = args; }

    @Override
    public Type forConsPPrim() {
        if (this.args.length == 1) {
            if (this.args[0].accept(new TypeVisitor(this.env)) instanceof ListType)
                return BoolType.ONLY;
            throw new TypeException("'cons?' requires a list type of argument");
        }
        throw new TypeException("'cons?' takes exactly 1 argument");
    }

    @Override
    public Type forNullPPrim() {
        if (this.args.length == 1) {
            if (this.args[0].accept(new TypeVisitor(this.env)) instanceof ListType)
                return BoolType.ONLY;
            throw new TypeException("'cons?' requires a list type of argument");
        }
        throw new TypeException("'cons?' takes exactly 1 argument");
    }

    @Override
    public Type forConsPrim() {
        if (this.args.length == 2) {
            Type type1 = this.args[0].accept(new TypeVisitor(this.env));
            Type type2 = this.args[1].accept(new TypeVisitor(this.env));
            if (type2 instanceof ListType) {
                if (type1.equals(((ListType) type2).getListType())) {
                    return type2;
                }
                throw new TypeException("two arguments of 'cons?' do not match: " + type1 + " and " + type2);
            }
            throw new TypeException("the second argument of 'cons?' requires a list type");
        }
        throw new TypeException("'cons?' takes exactly 2 arguments");
    }

    @Override
    public Type forFirstPrim() {
        if (this.args.length == 1) {
            Type type = this.args[0].accept(new TypeVisitor(this.env));
            if (type instanceof ListType) {
                return ((ListType) type).getListType();
            }
            throw new TypeException("'first?' requires a list type of argument");
        }
        throw new TypeException("'first?' takes exactly 1 argument");
    }

    @Override
    public Type forRestPrim() {
        if (this.args.length == 1) {
            Type type = this.args[0].accept(new TypeVisitor(this.env));
            if (type instanceof ListType) {
                return type;
            }
            throw new TypeException("'rest?' requires a list type of argument");
        }
        throw new TypeException("'rest?' takes exactly 1 argument");
    }
    
}


class UnOpTypeVisitor implements UnOpVisitor<Type> {
    
    /** Environment of the context */
    private HashMap<String, Type> env;

    /** AST of the right operand */
    private AST arg;
    
    public UnOpTypeVisitor(HashMap<String, Type> env, AST arg) { this.env = env; this.arg = arg; }

    @Override
    public Type forUnOpPlus(UnOpPlus op) {
        Type type = arg.accept(new TypeVisitor(env));
        if (type == IntType.ONLY)
            return IntType.ONLY;
        throw new TypeException("expected int type, but found " + type);
    }

    @Override
    public Type forUnOpMinus(UnOpMinus op) {
        Type type = arg.accept(new TypeVisitor(env));
        if (type == IntType.ONLY)
            return IntType.ONLY;
        throw new TypeException("expected int type, but found " + type);
    }

    @Override
    public Type forOpTilde(OpTilde op) {
        Type type = arg.accept(new TypeVisitor(env));
        if (type == BoolType.ONLY)
            return BoolType.ONLY;
        throw new TypeException("expected bool type, but found " + type);
    }

    @Override
    public Type forOpBang(OpBang op) {
        Type type = arg.accept(new TypeVisitor(env));
        if (type instanceof RefType)
            return ((RefType)type).getRefType();
        throw new TypeException("expected ref type, but found " + type);
    }

    @Override
    public Type forOpRef(OpRef op) {
        Type type = arg.accept(new TypeVisitor(env));
        return new RefType(type);
    }
    
}


class BinOpTypeVisitor implements BinOpVisitor<Type> {
    
    /** Environment of the context */
    private HashMap<String, Type> env;
    
    /** AST of the left operand */
    private AST arg1;

    /** AST of the right operand */
    private AST arg2;
    
    public BinOpTypeVisitor(HashMap<String, Type> env, AST arg1, AST arg2) { this.env = env; this.arg1 = arg1; this.arg2 = arg2; }
    
    @Override
    public Type forBinOpPlus(BinOpPlus op) {
        Type type1 = arg1.accept(new TypeVisitor(env));
        Type type2 = arg2.accept(new TypeVisitor(env));
        if (type1 != IntType.ONLY)
            throw new TypeException("expected int type, but found " + type1);
        if (type2 != IntType.ONLY)
            throw new TypeException("expected int type, but found " + type2);
        return IntType.ONLY;
    }

    @Override
    public Type forBinOpMinus(BinOpMinus op) {
        Type type1 = arg1.accept(new TypeVisitor(env));
        Type type2 = arg2.accept(new TypeVisitor(env));
        if (type1 != IntType.ONLY)
            throw new TypeException("expected int type, but found " + type1);
        if (type2 != IntType.ONLY)
            throw new TypeException("expected int type, but found " + type2);
        return IntType.ONLY;
    }

    @Override
    public Type forOpTimes(OpTimes op) {
        Type type1 = arg1.accept(new TypeVisitor(env));
        Type type2 = arg2.accept(new TypeVisitor(env));
        if (type1 != IntType.ONLY)
            throw new TypeException("expected int type, but found " + type1);
        if (type2 != IntType.ONLY)
            throw new TypeException("expected int type, but found " + type2);
        return IntType.ONLY;
    }

    @Override
    public Type forOpDivide(OpDivide op) {
        Type type1 = arg1.accept(new TypeVisitor(env));
        Type type2 = arg2.accept(new TypeVisitor(env));
        if (type1 != IntType.ONLY)
            throw new TypeException("expected int type, but found " + type1);
        if (type2 != IntType.ONLY)
            throw new TypeException("expected int type, but found " + type2);
        return IntType.ONLY;
    }

    @Override
    public Type forOpEquals(OpEquals op) {
        Type type1 = arg1.accept(new TypeVisitor(env));
        Type type2 = arg2.accept(new TypeVisitor(env));
        if (type1.equals(type2))
            return BoolType.ONLY;
        throw new TypeException("type " + type1 + " and " + type2 + " do not match");
    }

    @Override
    public Type forOpNotEquals(OpNotEquals op) {
        Type type1 = arg1.accept(new TypeVisitor(env));
        Type type2 = arg2.accept(new TypeVisitor(env));
        if (type1.equals(type2))
            return BoolType.ONLY;
        throw new TypeException("type " + type1 + " and " + type2 + " do not match");
    }

    @Override
    public Type forOpLessThan(OpLessThan op) {
        Type type1 = arg1.accept(new TypeVisitor(env));
        Type type2 = arg2.accept(new TypeVisitor(env));
        if (type1 != IntType.ONLY)
            throw new TypeException("expected int type, but found " + type1);
        if (type2 != IntType.ONLY)
            throw new TypeException("expected int type, but found " + type2);
        return IntType.ONLY;
    }

    @Override
    public Type forOpGreaterThan(OpGreaterThan op) {
        Type type1 = arg1.accept(new TypeVisitor(env));
        Type type2 = arg2.accept(new TypeVisitor(env));
        if (type1 != IntType.ONLY)
            throw new TypeException("expected int type, but found " + type1);
        if (type2 != IntType.ONLY)
            throw new TypeException("expected int type, but found " + type2);
        return IntType.ONLY;
    }

    @Override
    public Type forOpLessThanEquals(OpLessThanEquals op) {
        Type type1 = arg1.accept(new TypeVisitor(env));
        Type type2 = arg2.accept(new TypeVisitor(env));
        if (type1 != IntType.ONLY)
            throw new TypeException("expected int type, but found " + type1);
        if (type2 != IntType.ONLY)
            throw new TypeException("expected int type, but found " + type2);
        return IntType.ONLY;
    }

    @Override
    public Type forOpGreaterThanEquals(OpGreaterThanEquals op) {
        Type type1 = arg1.accept(new TypeVisitor(env));
        Type type2 = arg2.accept(new TypeVisitor(env));
        if (type1 != IntType.ONLY)
            throw new TypeException("expected int type, but found " + type1);
        if (type2 != IntType.ONLY)
            throw new TypeException("expected int type, but found " + type2);
        return IntType.ONLY;
    }

    @Override
    public Type forOpAnd(OpAnd op) {
        Type type1 = arg1.accept(new TypeVisitor(env));
        Type type2 = arg2.accept(new TypeVisitor(env));
        if (type1 != BoolType.ONLY)
            throw new TypeException("expected bool type, but found " + type1);
        if (type2 != BoolType.ONLY)
            throw new TypeException("expected bool type, but found " + type2);
        return BoolType.ONLY;
    }

    @Override
    public Type forOpOr(OpOr op) {
        Type type1 = arg1.accept(new TypeVisitor(env));
        Type type2 = arg2.accept(new TypeVisitor(env));
        if (type1 != BoolType.ONLY)
            throw new TypeException("expected bool type, but found " + type1);
        if (type2 != BoolType.ONLY)
            throw new TypeException("expected bool type, but found " + type2);
        return BoolType.ONLY;
    }

    @Override
    public Type forOpGets(OpGets op) {
        Type type1 = arg1.accept(new TypeVisitor(env));
        Type type2 = arg2.accept(new TypeVisitor(env));
        if (type1 instanceof RefType) {
            if (((RefType)type1).getRefType().equals(type2)) {
                return UnitType.ONLY;
            }
            throw new TypeException("the types of operands do not match: " + type1 + " and " + type2);
        }
        throw new TypeException("expected ref type, but found " + type1);
    }
    
}


