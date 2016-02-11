
public class JamFunInterpreter implements JamFunVisitor<JamVal> {
    
    private PureList<Binding> env;
    
    private AST [] args;
    
    public JamFunInterpreter(PureList<Binding> env, AST [] args) {
        this.env = env;
        this.args = args;
    }
    
    @Override
    public JamVal forJamClosure(JamClosure c) {
        Map map = c.body();
        int vars_num = map.vars().length;
        int args_num = this.args.length;
        if (vars_num == args_num) {
            PureList<Binding> closure_env = this.env;
            for (int i = 0; i < vars_num; i++) {
                closure_env = closure_env.cons(new CallByValueBinding(map.vars()[i], this.args[i], new ASTInterpreter(this.env)));
            }
            return map.body().accept(new ASTInterpreter(closure_env));
        }
        
        throw new EvalException("Jam closure '" + c.body() + "' takes exactly " + vars_num + " arguments");
    }

    @Override
    public JamVal forPrimFun(PrimFun pf) {
        return pf.accept(new PrimFunInterpreter(this.env, this.args));
    }

}
