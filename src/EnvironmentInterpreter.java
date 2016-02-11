
/** assigns a value to a given variable */
public class EnvironmentInterpreter implements PureListVisitor<Binding, JamVal> {
    
    private Variable var;
    
    /** Constructor */
    public EnvironmentInterpreter(Variable var) { this.var = var; }
    

    /** For empty environment */
    @Override
    public JamVal forEmpty(Empty<Binding> e) {
        throw new EvalException("Variable '" + var.toString() + "' is not binded with any value");
    }

    /** For non-empty environment */
    @Override
    public JamVal forCons(Cons<Binding> c) {
        Binding bind = c.first;
        if (bind.var().equals(this.var))
            return bind.value();
        return c.rest().accept(this);
    }

}
