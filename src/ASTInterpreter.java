
public class ASTInterpreter extends InterpreterBase implements ASTVisitor<JamVal> {
    
    public ASTInterpreter(PureList<Binding> env, EvaluationType type) {
        super(env, type);
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
        return this.env.accept(new EnvironmentInterpreter(v));
    }

    @Override
    public JamVal forPrimFun(PrimFun f) {
        return f;
    }

    @Override
    public JamVal forUnOpApp(UnOpApp u) {
        UnOp op = u.rator();
        return op.accept(new UnOpInterpreter(this.env, u.arg(), this.type));
    }

    @Override
    public JamVal forBinOpApp(BinOpApp b) {
        BinOp op = b.rator();
        return op.accept(new BinOpInterpreter(this.env, b.arg1(), b.arg2(), this.type));
    }

    @Override
    public JamVal forApp(App a) {
        JamVal rator_val = a.rator().accept(this);
        if (rator_val instanceof JamFun)
            return ((JamFun)rator_val).accept(new JamFunInterpreter(this.env, a.args(), this.type));
        throw new EvalException(a.rator().toString() + " is not a valid Jam Function");
    }

    @Override
    public JamVal forMap(Map m) {
        return new JamClosure(m, this.env);
    }

    @Override
    public JamVal forIf(If i) {
        JamVal test_result = i.test().accept(this);
        if (test_result instanceof BoolConstant) {
            if (test_result == BoolConstant.TRUE) {
                return i.conseq().accept(this);
            }
            else {
                return i.alt().accept(this);
            }
        }
        throw new EvalException("The condition of If-Else-Then should be a boolean value");
    }

    @Override
    public JamVal forLet(Let l) {
        Def [] defs = l.defs();
        AST body = l.body();
        PureList<Binding> let_env = this.env;
        for (Def def : defs) {
            let_env = let_env.cons(new CallByValueBinding(def.lhs(), def.rhs(), this));
        }        
        return body.accept(new ASTInterpreter(let_env, this.type));
    }

}
