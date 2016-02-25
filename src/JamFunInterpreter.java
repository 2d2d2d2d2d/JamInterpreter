
/** Interprets Jam functions (Map and primitive functions) to a JamVal */
public class JamFunInterpreter extends InterpreterBase implements JamFunVisitor<JamVal> {
    
    /** Arguments of the Jam function */
    private AST [] args;
    
    /** Constructor */
    public JamFunInterpreter(PureList<Binding> env, AST [] args, EvaluationPolicy ep) {
        super(env, ep);
        this.args = args;
    }

    /** Interprets a Jam closure (map) */
    @Override
    public JamVal forJamClosure(JamClosure c) {
        Map map = c.body();
        int vars_num = map.vars().length;
        int args_num = this.args.length;
        if (vars_num == args_num) {
            PureList<Binding> closure_env = c.env();
            for (int i = 0; i < vars_num; i++) {
                closure_env = closure_env.cons(ValueBinding.generate(map.vars()[i], this.args[i], new ASTInterpreter(this.env, this.ep)));
            }
            return map.body().accept(new ASTInterpreter(closure_env, this.ep));
        }
        
        throw new EvalException("Jam closure '" + c.body() + "' takes exactly " + vars_num + " arguments");
    }

    /** Interprets a primitive function */
    @Override
    public JamVal forPrimFun(PrimFun pf) {
        return pf.accept(new PrimFunInterpreter(this.env, this.args, this.ep));
    }

}
