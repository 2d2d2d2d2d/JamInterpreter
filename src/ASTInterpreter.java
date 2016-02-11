
public class ASTInterpreter implements ASTVisitor<JamVal> {
    
    private PureList<Binding> env;
    
    public ASTInterpreter(PureList<Binding> env) {
        this.env = env;
    }
    
    @Override
    public JamVal forBoolConstant(BoolConstant b) {
        return b;
    }

    @Override
    public JamVal forIntConstant(IntConstant i) {
        return i;
    }

    @Override
    public JamVal forNullConstant(NullConstant n) {
        return JamEmpty.ONLY;
    }

    @Override
    public JamVal forVariable(Variable v) {
        return this.env.accept(new EnvironmentInterpreter());
    }

    @Override
    public JamVal forPrimFun(PrimFun f) {
        return f;
    }

    @Override
    public JamVal forUnOpApp(UnOpApp u) {
        UnOp op = u.rator();
        return op.accept(new UnOpInterpreter(this.env, u.arg()));
    }

    @Override
    public JamVal forBinOpApp(BinOpApp b) {
        BinOp op = b.rator();
        return op.accept(new BinOpInterpreter(this.env, b.arg1(), b.arg2()));
    }

    @Override
    public JamVal forApp(App a) {
        AST rator = a.rator();
        return ((JamFun)rator).accept(new JamFunInterpreter(this.env, a.args()));
    }

    @Override
    public JamVal forMap(Map m) {
        return new JamClosure(m, this.env);
    }

    @Override
    public JamVal forIf(If i) {
        if (i.test().accept(this) == BoolConstant.TRUE) {
            return i.conseq().accept(this);
        }
        else {
            return i.alt().accept(this);
        }
    }

    @Override
    public JamVal forLet(Let l) {
        Def [] defs = l.defs();
        AST body = l.body();
        for (Def def : defs) {
            this.env = this.env.cons(new CallByValueBinding(def.lhs(), def.rhs(), this));
        }        
        return body.accept(this);
    }

}
