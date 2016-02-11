
public class JamFunInterpreter implements JamFunVisitor<JamVal> {
    
    private PureList<Binding> env;
    
    private AST [] args;
    
    public JamFunInterpreter(PureList<Binding> env, AST [] args) {
        this.env = env;
        this.args = args;
    }
    
    @Override
    public JamVal forJamClosure(JamClosure c) {
        Variable [] vars = c.body().vars();
        AST body = c.body().body();
        //c.
        return c.body().accept(new ASTInterpreter(c.env()));
    }

    @Override
    public JamVal forPrimFun(PrimFun pf) {
        return pf.accept(new PrimFunInterpreter(this.env, this.args));
    }

}
