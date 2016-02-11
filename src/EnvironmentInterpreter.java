
public class EnvironmentInterpreter implements PureListVisitor<Binding, JamVal> {
    
    private Variable variable;
    
    @Override
    public JamVal forEmpty(Empty<Binding> e) {
        throw new EvalException("Variable '" + variable.toString() + "' is not binded with any value");
    }

    @Override
    public JamVal forCons(Cons<Binding> c) {
        Binding bind = c.first;
        if (bind.var() == this.variable)
            return bind.value();
        return c.rest().accept(this);
    }

}
