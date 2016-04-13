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
        return new Variable(var_name + ":" + this.env.get(var_name));
    }

    @Override
    public AST forPrimFun(PrimFun f) { return f; }

    @Override
    public AST forUnOpApp(UnOpApp u) {
        return new UnOpApp(u.rator(), u.arg().accept(this));
    }

    @Override
    public AST forBinOpApp(BinOpApp b) {
        if (b.rator() == OpAnd.ONLY) {
            return new If(b.arg1().accept(this), b.arg2().accept(this), BoolConstant.FALSE);
        }
        else if (b.rator() == OpOr.ONLY) {
            return new If(b.arg1().accept(this), BoolConstant.TRUE, b.arg2().accept(this));
        }
        else {
            return new BinOpApp(b.rator(), b.arg1().accept(this), b.arg2().accept(this));
        }
    }

    @Override
    public AST forApp(App a) {
        AST rator = a.rator().accept(this);
        List<AST> arg_list = new ArrayList<AST>();
        for(AST arg : a.args()) {
            arg_list.add(arg.accept(this));
        }
        return new App(rator, arg_list.toArray(new AST[0]));
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
        
        List<Variable> var_list = new ArrayList<Variable>();
        for(Variable var : m.vars()) {
            var_list.add((Variable) var.accept(new UnshadowVisitor(new_env, depth)));
        }
        AST body = m.body().accept(new UnshadowVisitor(new_env, depth + 1));
        return new Map(var_list.toArray(new Variable[0]), body);
    }

    @Override
    public AST forIf(If i) {
        return new If(i.test().accept(this), i.conseq().accept(this), i.alt().accept(this));
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
        
        List<Def> def_list = new ArrayList<Def>();
        for(Def def : l.defs()) {
            Variable var = (Variable) def.lhs().accept(new UnshadowVisitor(new_env, depth));
            AST exp = def.rhs().accept(new UnshadowVisitor(new_env, depth));
            def_list.add(new Def(var, exp));
        }
        
        AST body = l.body().accept(new UnshadowVisitor(new_env, depth + 1));
        return new Let(def_list.toArray(new Def[0]), body);
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
        }
        
        List<Def> def_list = new ArrayList<Def>();
        for(Def def : l.defs()) {
            Variable var = (Variable) def.lhs().accept(new UnshadowVisitor(new_env, depth));
            AST exp = def.rhs().accept(new UnshadowVisitor(new_env, depth + 1));
            def_list.add(new Def(var, exp));
        }
        
        AST body = l.body().accept(new UnshadowVisitor(new_env, depth + 1));
        return new LetRec(def_list.toArray(new Def[0]), body);
    }

    @Override
    public AST forBlock(Block b) {
        List<AST> exp_list = new ArrayList<AST>();
        for(AST exp : b.exps()) {
            exp_list.add(exp.accept(this));
        }
        return new Block(exp_list.toArray(new AST[0]));
    }
    
    public String getVarName(String var_name) {
        if (var_name.contains(":")) {
            var_name = var_name.split(":")[0];
        }
        return var_name;
    }
    
}
