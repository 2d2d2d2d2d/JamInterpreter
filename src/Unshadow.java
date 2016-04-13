import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** Class for unshadowing transformation */
class Unshadow {
    public static AST convert(AST ast) {
        return ast.accept(new UnshadowVisitor(new HashMap<String,Integer>(), 0));
    }
}

/** Traverses the AST and generates a new unshadowed AST */
class UnshadowVisitor implements ASTVisitor<AST> {
    
    /** Environment of the context */
    private HashMap<String,Integer> env;
    
    /** Lexical depth of the current AST  */
    private int depth;
    
    /** Whether we want to keep the original lexical depth aftering entering Let, LetRec or Map */
    private boolean keepDepth;
    
    /** Constructor */
    public UnshadowVisitor(HashMap<String,Integer> env, int depth) {
        this.env = env; this.depth = depth; keepDepth = false;
    }

    /** Constructor */
    public UnshadowVisitor(HashMap<String,Integer> env, int depth, boolean keepDepth) {
        this.env = env; this.depth = depth; this.keepDepth = keepDepth;
    }
    
    /** For bool constant, returns the static object */
    @Override
    public AST forBoolConstant(BoolConstant b) { return b;}

    /** For int constant, returns a new int constant */
    @Override
    public AST forIntConstant(IntConstant i) { return new IntConstant(i.value()); }

    /** For null constant, returns the static object */
    @Override
    public AST forNullConstant(NullConstant n) { return n; }

    /** Generate a new variable with its lexical depth */
    @Override
    public AST forVariable(Variable v) {
        String var_name = getVarName(v.name());
        if (var_name.charAt(0) == ':') {
            return new Variable(var_name);
        }
        return new Variable(var_name + ":" + this.env.get(var_name));
    }

    /** For primitive function, returns the static object */
    @Override
    public AST forPrimFun(PrimFun f) { return f; }

    /** For unary operator app, returns the copy */
    @Override
    public AST forUnOpApp(UnOpApp u) {
        return new UnOpApp(u.rator(), u.arg().accept(this));
    }

    /** For binary operator app, returns the copy, as well as transform "&" and "|" in terms of if-then-else */
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

    /** For Jam app, returns the copy */
    @Override
    public AST forApp(App a) {
        AST rator = a.rator().accept(this);
        List<AST> arg_list = new ArrayList<AST>();
        for(AST arg : a.args()) {
            arg_list.add(arg.accept(this));
        }
        return new App(rator, arg_list.toArray(new AST[0]));
    }

    /** Unshadows Map */
    @Override
    public AST forMap(Map m) {
        if(! keepDepth) depth++;
        
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
        AST body = m.body().accept(new UnshadowVisitor(new_env, depth));
        return new Map(var_list.toArray(new Variable[0]), body);
    }

    /** For if-then-else, returns the copy */
    @Override
    public AST forIf(If i) {
        return new If(i.test().accept(this), i.conseq().accept(this), i.alt().accept(this));
    }

    /** Unshadows Let */
    @Override
    public AST forLet(Let l) {
        if(! keepDepth) depth++;
        
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
            AST exp = def.rhs().accept(new UnshadowVisitor(new_env, depth, true));
            def_list.add(new Def(var, exp));
        }
        
        AST body = l.body().accept(new UnshadowVisitor(new_env, depth));
        return new Let(def_list.toArray(new Def[0]), body);
    }

    /** Unshadows LetRec */
    @Override
    public AST forLetRec(LetRec l) {
        if(! keepDepth) depth++;
        
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
        
        AST body = l.body().accept(new UnshadowVisitor(new_env, depth));
        return new LetRec(def_list.toArray(new Def[0]), body);
    }
    
    /** Unshadows Letcc */
    @Override
    public AST forLetcc(Letcc l) {
        if(! keepDepth) depth++;
        
        HashMap<String,Integer> new_env = new HashMap<String,Integer>();
        new_env.put(getVarName(l.def().name()), depth);
        
        for(String old_var : this.env.keySet()) {
            if(! new_env.containsKey(old_var))
                new_env.put(old_var, this.env.get(old_var));
        }
        
        Variable var = (Variable) l.def().accept(new UnshadowVisitor(new_env, depth));
        AST body = l.body().accept(new UnshadowVisitor(new_env, depth));
        return new Letcc(var, body);
    }

    /** For block, returns the copy */
    @Override
    public AST forBlock(Block b) {
        List<AST> exp_list = new ArrayList<AST>();
        for(AST exp : b.exps()) {
            exp_list.add(exp.accept(this));
        }
        return new Block(exp_list.toArray(new AST[0]));
    }

    /** Returns the name of an unshadowed variable */
    public String getVarName(String var_name) {
        if (var_name.charAt(0) == ':') {
            return var_name;
        }
        if (var_name.contains(":")) {
            var_name = var_name.split(":")[0];
        }
        return var_name;
    }
    
}
