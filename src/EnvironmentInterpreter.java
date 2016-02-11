
public class EnvironmentInterpreter implements PureListVisitor<Binding, JamVal> {
    
    private Variable var;
    
    public EnvironmentInterpreter(Variable var) { this.var = var; }
    
    @Override
    public JamVal forEmpty(Empty<Binding> e) {
        throw new EvalException("Variable '" + var.toString() + "' is not binded with any value");
    }

    @Override
    public JamVal forCons(Cons<Binding> c) {
        Binding bind = c.first;
        if (bind.var().equals(this.var))
            return bind.value();
        return c.rest().accept(this);
    }

}