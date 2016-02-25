

/** Generic interpreter that interprets an AST to a JamVal */
public class ASTInterpreter extends InterpreterBase implements ASTVisitor<JamVal> {

    /** Constructor */
    public ASTInterpreter(PureList<Binding> env, EvaluationPolicy ep) {
        super(env, ep);
    }

    /** Interprets a boolean */
    @Override
    public JamVal forBoolConstant(BoolConstant b) {
        return b;
    }

    /** Interprets an integer */
    @Override
    public JamVal forIntConstant(IntConstant i) {
        return i;
    }

    /** Interprets a null constant */
    @Override
    public JamVal forNullConstant(NullConstant n) {
        return JamEmpty.ONLY;
    }

    /** Interprets a variable */
    @Override
    public JamVal forVariable(Variable v) {
        return this.env.accept(new EnvironmentInterpreter(v));
    }

    /** Interprets a primitive function */
    @Override
    public JamVal forPrimFun(PrimFun f) {
        return f;
    }

    /** Interprets an unary operator app */
    @Override
    public JamVal forUnOpApp(UnOpApp u) {
        UnOp op = u.rator();
        return op.accept(new UnOpInterpreter(this.env, u.arg(), this.ep));
    }

    /** Interprets a binary operator app */
    @Override
    public JamVal forBinOpApp(BinOpApp b) {
        BinOp op = b.rator();
        return op.accept(new BinOpInterpreter(this.env, b.arg1(), b.arg2(), this.ep));
    }

    /** Interprets a Jam app */
    @Override
    public JamVal forApp(App a) {
        JamVal rator_val = a.rator().accept(this);
        if (rator_val instanceof JamFun)
            return ((JamFun)rator_val).accept(new JamFunInterpreter(this.env, a.args(), this.ep));
        throw new EvalException(a.rator().toString() + " is not a valid Jam Function");
    }

    /** Interprets a map */
    @Override
    public JamVal forMap(Map m) {
        return new JamClosure(m, this.env);
    }

    /** Interprets if-then-else */
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

    /** Interprets let-in */
    @Override
    public JamVal forLet(Let l) {
        Def [] defs = l.defs();
        AST body = l.body();
      
        int length = defs.length;
        Variable[] vars = new Variable[length];
        AST[] asts = new AST[length];
        for (int i = 0; i < length; i++) {
            vars[i] = defs[i].lhs();
            asts[i] = defs[i].rhs();
        }
        PureList<Binding> new_env = ValueBinding.generate(vars, asts, this);
        
        return body.accept(new ASTInterpreter(new_env, this.ep));
    }

}
