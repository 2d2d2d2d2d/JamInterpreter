import java.util.HashSet;
import java.util.Set;

/** Context-sensitive checking class */
class ContextCheck {
    
    /** Context-sensitive checking */
    public static void check(AST ast) throws SyntaxException {
        ast.accept(new ContextVisitor(new HashSet<String>()));
    }
    
}


/** Context visitor that traverses ASTs recursively */
class ContextVisitor implements ASTVisitor<AST> {
    
    /** Environment of the context */
    private Set<String> env;
    
    /** Constructor */
    public ContextVisitor(Set<String> env) { this.env = env; }
    
    /** Context-sensitive checking for bool (terminate) */
    @Override
    public AST forBoolConstant(BoolConstant b) { return b; }

    /** Context-sensitive checking for int (terminate) */
    @Override
    public AST forIntConstant(IntConstant i) { return i; }

    /** Context-sensitive checking for null (terminate) */
    @Override
    public AST forNullConstant(NullConstant n) { return n; }

    /** Context-sensitive checking for variables */
    @Override
    public AST forVariable(Variable v) {
        if (this.env.contains(v.name()))
            return v;
        throw new SyntaxException("Variable '" + v.toString() + "' is free in the expression");
    }

    /** Context-sensitive checking for primitive functions (terminate) */
    @Override
    public AST forPrimFun(PrimFun f) { return f; }

    /** Context-sensitive checking for unary operator app */
    @Override
    public AST forUnOpApp(UnOpApp u) {
        u.arg().accept(this);
        return u;
    }

    /** Context-sensitive checking for binary operator app */
    @Override
    public AST forBinOpApp(BinOpApp b) {
        b.arg1().accept(this);
        b.arg2().accept(this);
        return b;
    }

    /** Context-sensitive checking for Jam app */
    @Override
    public AST forApp(App a) {
        a.rator().accept(this);
        for(AST arg : a.args()) {
            arg.accept(this);
        }
        return a;
    }

    /** Context-sensitive checking for map */
    @Override
    public AST forMap(Map m) {
        Set<String> new_env = new HashSet<String>();
        for(Variable var : m.vars()) {
            if(!new_env.contains(var.name()))
                new_env.add(var.name());
            else
                throw new SyntaxException("Variable '" + var.toString() + "' is declared more than once");
        }
        new_env.addAll(this.env);
        m.body().accept(new ContextVisitor(new_env));
        return m;
    }

    /** Context-sensitive checking for if-then-else */
    @Override
    public AST forIf(If i) {
        i.test().accept(this);
        i.conseq().accept(this);
        i.alt().accept(this);
        return i;
    }

    /** Context-sensitive checking for let-in */
    @Override
    public AST forLet(Let l) {
        Set<String> new_env = new HashSet<String>();
        for(Def def : l.defs()) {
            if(!new_env.contains(def.lhs().name())) {
                new_env.add(def.lhs().name());
            }
            else {
                throw new SyntaxException("Variable '" + def.lhs().toString() + "' is declared more than once");
            }
        }
        new_env.addAll(this.env);
        
        for(Def def : l.defs()) {
            def.rhs().accept(new ContextVisitor(new_env));
        }
        
        l.body().accept(new ContextVisitor(new_env));
        return l;
    }
    
    /** Context-sensitive checking for Block */
    @Override
    public AST forBlock(Block b) {
        for(AST exp : b.exps()) {
            exp.accept(this);
        }
        return null;
    }
    
}


