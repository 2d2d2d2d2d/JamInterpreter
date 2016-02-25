


abstract class LazyCons extends JamCons {
    
    protected AST firstAst;
    
    protected AST restAst;
    
    protected PureList<Binding> env;
    
    protected EvaluationPolicy ep;
    
    public LazyCons(AST firstAst, AST restAst, PureList<Binding> env, EvaluationPolicy ep) {
        super(null, null);
        this.firstAst = firstAst;
        this.restAst = restAst;
        this.env = env;
        this.ep = ep;
    }
    
    protected void evaluateFirst() {
        this.first = this.firstAst.accept(new ASTInterpreter(this.env, this.ep));
    }
    
    protected void evaluateRest() {
        JamVal rest_val = this.restAst.accept(new ASTInterpreter(this.env, this.ep));
        if (rest_val instanceof JamList) {
            this.rest = (JamList)rest_val;
        }
        else {
            throw new EvalException("Invalid second argument for 'cons', 'list' expected");
        }
    }
    
    public String toString() {
        this.evaluateFirst();
        this.evaluateRest();
        return super.toString();
    }
    
    public String toStringHelp() {
        this.evaluateFirst();
        this.evaluateRest();
        return super.toStringHelp();
    }

    
    public static LazyCons generate(AST firstAst, AST restAst, PureList<Binding> env, EvaluationPolicy ep) {
        LazyCons lazyCons;
        switch (ep.consType) {
        case CALL_BY_VALUE:
            lazyCons = new LazyConsByValue(firstAst, restAst, env, ep); break;
        case CALL_BY_NAME:
            lazyCons = new LazyConsByName(firstAst, restAst, env, ep); break;
        case CALL_BY_NEED:
            lazyCons = new LazyConsByNeed(firstAst, restAst, env, ep); break;
        default:
            throw new EvalException("Invalid evaluation strategy");
        }
        return lazyCons;
    }
}

class LazyConsByValue extends LazyCons {

    public LazyConsByValue(AST firstAst, AST restAst, PureList<Binding> env, EvaluationPolicy ep) {
        super(firstAst, restAst, env, ep);
        
        super.evaluateFirst();
        super.evaluateRest();
    }

}


class LazyConsByName extends LazyCons {

    public LazyConsByName(AST firstAst, AST restAst, PureList<Binding> env, EvaluationPolicy ep) {
        super(firstAst, restAst, env, ep);
    }
    
    @Override
    public JamVal first() {
        super.evaluateFirst();
        return first;
    }

    @Override
    public JamList rest() {
        super.evaluateRest();
        return (JamList)rest;
    }

}


class LazyConsByNeed extends LazyCons {

    public LazyConsByNeed(AST firstAst, AST restAst, PureList<Binding> env, EvaluationPolicy ep) {
        super(firstAst, restAst, env, ep);
    }
    
    @Override
    public JamVal first() {
        if (first == null) {
            super.evaluateFirst();
        }
        return first;
    }

    @Override
    public JamList rest() {
        if (rest == null) {
            super.evaluateRest();
        }
        return (JamList)rest;
    }

}
