import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class Sd {
    private AST ast;
    
    public Sd(AST ast) { this.ast = ast; }
    
    public AST convert() {
        return ast.accept(new SdVisitor(new HashMap<String,Coordinate>(), 1));
    }
}

class SdLet extends Let {
    SdLet(Def[] d, AST b) { super(d, b); }
    public String toString() {
        AST[] right_defs = new AST[defs().length];
        for (int i = 0; i < defs().length; i++)
            right_defs[i] = defs()[i].rhs();
        return "let [*" + defs().length + "*] " + ToString.toString(right_defs,"; ") + "; in " + body();
    }
}

class SdLetRec extends LetRec {
    SdLetRec(Def[] d, AST b) { super(d, b); }
    public String toString() {
        AST[] right_defs = new AST[defs().length];
        for (int i = 0; i < defs().length; i++)
            right_defs[i] = defs()[i].rhs();
        return "letrec [*" + defs().length + "*] " + ToString.toString(right_defs,"; ") + "; in " + body();
    }
}

class SdMap extends Map {
    SdMap(Variable[] v, AST b) { super(v, b); }
    public String toString() { 
        return "map [*" + vars().length + "*] to " + body();
    }
}

class SdVariable extends Variable {
    SdVariable(String n, int d, int i) { super(n); distance = d; index = i; }
    private int distance;
    private int index;
    public String toString() { 
        return "[" + distance + "," + index + "]";
    }
}

class SdApp extends App {

    SdApp(AST r, AST[] a) { super(r, a); }
    public String toString() { 
        if (rator() instanceof Variable)
            return "(" +  rator() + ")(" + ToString.toString(args(),", ") + ")"; 
        return super.toString();
    }
}

class Coordinate {
    public Coordinate(int d, int i) { depth = d; index = i; }
    int depth;
    int index;
}


class SdVisitor implements ASTVisitor<AST> {
    
    /** Environment of the context */
    private HashMap<String,Coordinate> env;
    
    private int depth;
    
    private boolean keepDepth;
    
    public SdVisitor(HashMap<String,Coordinate> env, int depth) {
        this.env = env; this.depth = depth; keepDepth = false;
    }
    
    public SdVisitor(HashMap<String,Coordinate> env, int depth, boolean keepDepth) {
        this.env = env; this.depth = depth; this.keepDepth = keepDepth;
    }
    
    @Override
    public AST forBoolConstant(BoolConstant b) { return b;}

    @Override
    public AST forIntConstant(IntConstant i) { return i; }

    @Override
    public AST forNullConstant(NullConstant n) { return n; }

    @Override
    public AST forVariable(Variable v) {
        return new SdVariable(v.name(), depth - this.env.get(v.name()).depth, this.env.get(v.name()).index);
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
        return new SdApp(rator, arg_list.toArray(new AST[0]));
    }

    @Override
    public AST forMap(Map m) {
        if(! keepDepth) depth++;
        
        HashMap<String,Coordinate> new_env = new HashMap<String,Coordinate>();
        for(int i = 0; i < m.vars().length; i++) {
            new_env.put(m.vars()[i].name(), new Coordinate(depth,i));
        }
        for(String old_var : this.env.keySet()) {
            if(! new_env.containsKey(old_var))
                new_env.put(old_var, this.env.get(old_var));
        }
        
        List<Variable> var_list = new ArrayList<Variable>();
        for(Variable var : m.vars()) {
            var_list.add((Variable) var.accept(new SdVisitor(new_env, depth)));
        }
        AST body = m.body().accept(new SdVisitor(new_env, depth));
        return new SdMap(var_list.toArray(new Variable[0]), body);
    }

    @Override
    public AST forIf(If i) {
        return new If(i.test().accept(this), i.conseq().accept(this), i.alt().accept(this));
    }

    @Override
    public AST forLet(Let l) {
        if(! keepDepth) depth++;
        
        HashMap<String,Coordinate> new_env = new HashMap<String,Coordinate>();
        for(int i = 0; i < l.defs().length; i++) {
            new_env.put(l.defs()[i].lhs().name(), new Coordinate(depth,i));
        }
        for(String old_var : this.env.keySet()) {
            if(! new_env.containsKey(old_var))
                new_env.put(old_var, this.env.get(old_var));
        }
        
        List<Def> def_list = new ArrayList<Def>();
        for(Def def : l.defs()) {
            Variable var = (Variable) def.lhs().accept(new SdVisitor(new_env, depth));
            AST exp = def.rhs().accept(new SdVisitor(new_env, depth, true));
            def_list.add(new Def(var, exp));
        }
        
        AST body = l.body().accept(new SdVisitor(new_env, depth));
        return new SdLet(def_list.toArray(new Def[0]), body);
    }

    @Override
    public AST forLetRec(LetRec l) {
        if(! keepDepth) depth++;
        
        HashMap<String,Coordinate> new_env = new HashMap<String,Coordinate>();
        for(int i = 0; i < l.defs().length; i++) {
            new_env.put(l.defs()[i].lhs().name(), new Coordinate(depth,i));
        }
        for(String old_var : this.env.keySet()) {
            if(! new_env.containsKey(old_var))
                new_env.put(old_var, this.env.get(old_var));
        }
        
        List<Def> def_list = new ArrayList<Def>();
        for(Def def : l.defs()) {
            Variable var = (Variable) def.lhs().accept(new SdVisitor(new_env, depth));
            AST exp = def.rhs().accept(new SdVisitor(new_env, depth));
            def_list.add(new Def(var, exp));
        }
        
        AST body = l.body().accept(new SdVisitor(new_env, depth));
        return new SdLetRec(def_list.toArray(new Def[0]), body);
    }

    @Override
    public AST forBlock(Block b) {
        List<AST> exp_list = new ArrayList<AST>();
        for(AST exp : b.exps()) {
            exp_list.add(exp.accept(this));
        }
        return new Block(exp_list.toArray(new AST[0]));
    }
    
}

