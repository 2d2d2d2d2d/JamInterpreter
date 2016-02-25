import java.util.HashSet;
import java.util.Set;

public class Context {
    
    public static void check(AST ast) throws SyntaxException {
        ast.accept(new ContextVisitor(new HashSet<String>()));
    }
}



class ContextVisitor implements ASTVisitor<AST> {
    
    private Set<String> env;
    
    public ContextVisitor(Set<String> env) { this.env = env; }
    
    @Override
    public AST forBoolConstant(BoolConstant b) { return b; }

    @Override
    public AST forIntConstant(IntConstant i) { return i; }

    @Override
    public AST forNullConstant(NullConstant n) { return n; }

    @Override
    public AST forVariable(Variable v) {
        if (this.env.contains(v.name()))
            return v;
        throw new SyntaxException("Variable '" + v.toString() + "' is free in the expression");
    }

    @Override
    public AST forPrimFun(PrimFun f) { return f; }

    @Override
    public AST forUnOpApp(UnOpApp u) {
        u.arg().accept(this);
        return u;
    }

    @Override
    public AST forBinOpApp(BinOpApp b) {
        b.arg1().accept(this);
        b.arg2().accept(this);
        return b;
    }

    @Override
    public AST forApp(App a) {
        a.rator().accept(this);
        for(AST arg : a.args()) {
            arg.accept(this);
        }
        return a;
    }

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

    @Override
    public AST forIf(If i) {
        i.test().accept(this);
        i.conseq().accept(this);
        i.alt().accept(this);
        return i;
    }

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
    
}


