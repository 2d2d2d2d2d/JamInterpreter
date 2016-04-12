import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class Unshadow {
    public static AST convert(AST ast) {
        return ast.accept(new UnshadowVisitor(new HashMap<String,Integer>(), 1));
    }
}

class UnshadowVisitor implements ASTVisitor<AST> {
    
    /** Environment of the context */
    private HashMap<String,Integer> env;
    
    private int depth;
    
    public UnshadowVisitor(HashMap<String,Integer> env, int depth) { this.env = env; this.depth = depth; }
    
    @Override
    public AST forBoolConstant(BoolConstant b) { return b;}

    @Override
    public AST forIntConstant(IntConstant i) { return i; }

    @Override
    public AST forNullConstant(NullConstant n) { return n; }

    @Override
    public AST forVariable(Variable v) {
        String var_name = getVarName(v.name());
        v.setName(var_name + ":" + this.env.get(var_name));
        return v;
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
        HashMap<String,Integer> new_env = new HashMap<String,Integer>();
        for(Variable var : m.vars()) {
            new_env.put(getVarName(var.name()), depth);
        }
        for(String old_var : this.env.keySet()) {
            if(! new_env.containsKey(old_var))
                new_env.put(old_var, this.env.get(old_var));
        }
        for(Variable var : m.vars()) {
            var.accept(new UnshadowVisitor(new_env, depth + 1));
        }
        m.body().accept(new UnshadowVisitor(new_env, depth + 1));
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
        HashMap<String,Integer> new_env = new HashMap<String,Integer>();
        for(Def def : l.defs()) {
            new_env.put(getVarName(def.lhs().name()), depth);
        }
        for(String old_var : this.env.keySet()) {
            if(! new_env.containsKey(old_var))
                new_env.put(old_var, this.env.get(old_var));
        }
        
        for(Def def : l.defs()) {
            def.lhs().accept(new UnshadowVisitor(new_env, depth));
            def.rhs().accept(new UnshadowVisitor(new_env, depth));
        }
        
        l.body().accept(new UnshadowVisitor(new_env, depth + 1));
        return l;
    }

    @Override
    public AST forLetRec(LetRec l) {
        HashMap<String,Integer> new_env = new HashMap<String,Integer>();
        for(Def def : l.defs()) {
            new_env.put(getVarName(def.lhs().name()), depth);
        }
        for(String old_var : this.env.keySet()) {
            if(! new_env.containsKey(old_var))
                new_env.put(old_var, this.env.get(old_var));
            System.out.println("var: " + old_var + ", depth: " + new_env.get(old_var));
        }
        
        for(Def def : l.defs()) {
            def.lhs().accept(new UnshadowVisitor(new_env, depth));
            def.rhs().accept(new UnshadowVisitor(new_env, depth + 1));
        }
        
        l.body().accept(new UnshadowVisitor(new_env, depth + 1));
        return l;
    }

    @Override
    public AST forBlock(Block b) {
        for(AST exp : b.exps()) {
            exp.accept(this);
        }
        return b;
    }
    
    public String getVarName(String var_name) {
        if (var_name.contains(":")) {
            var_name = var_name.split(":")[0];
        }
        return var_name;
    }
    
}
