

/** Abstract class of cons that allows lazy evaluation */
abstract class LazyCons extends JamCons {
    
    /** AST of 'first' before being evaluated */
    protected AST firstAst;

    /** AST of 'rest' before being evaluated */
    protected AST restAst;

    /** Current environment */
    protected PureList<Binding> env;

    /** Evaluation policy */
    protected EvaluationPolicy ep;

    /** Constructor */
    public LazyCons(AST firstAst, AST restAst, PureList<Binding> env, EvaluationPolicy ep) {
        super(null, null);
        this.firstAst = firstAst;
        this.restAst = restAst;
        this.env = env;
        this.ep = ep;
    }
    
    /** Evaluate the AST of 'first' */
    protected void evaluateFirst() {
        try {
            this.first = this.firstAst.accept(new ASTInterpreter(this.env, this.ep));
        } catch (StackOverflowError e) {
            throw new EvalException("forward reference");
        }
    }

    /** Evaluate the AST of 'rest' */
    protected void evaluateRest() {
        try {
            JamVal rest_val = this.restAst.accept(new ASTInterpreter(this.env, this.ep));
            if (rest_val instanceof JamList) {
                this.rest = (JamList)rest_val;
            }
            else {
                throw new EvalException("Invalid second argument for 'cons', 'list' expected");
            }
        } catch (StackOverflowError e) {
            throw new EvalException("forward reference");
        }
    }

    /** Convert the lazy cons to string */
    @Override
    public String toString() {
        this.evaluateFirst();
        this.evaluateRest();
        return super.toString();
    }

    /** Helper of the lazy cons' toString method */
    @Override
    public String toStringHelp() {
        this.evaluateFirst();
        this.evaluateRest();
        return super.toStringHelp();
    }

    /** Generate a lazy cons */
    public static LazyCons generate(AST firstAst, AST restAst, PureList<Binding> env, EvaluationPolicy ep) {
        LazyCons lazyCons;
        switch (ep.consType) {
        case BY_VALUE:
            lazyCons = new LazyConsByValue(firstAst, restAst, env, ep); break;
        case BY_NAME:
            lazyCons = new LazyConsByName(firstAst, restAst, env, ep); break;
        case BY_NEED:
            lazyCons = new LazyConsByNeed(firstAst, restAst, env, ep); break;
        default:
            throw new EvalException("Invalid evaluation strategy");
        }
        return lazyCons;
    }
}


/** Lazy cons that is evaluated by value */
class LazyConsByValue extends LazyCons {
    
    /** Constructor where 'first' and 'rest' are evaluated */
    public LazyConsByValue(AST firstAst, AST restAst, PureList<Binding> env, EvaluationPolicy ep) {
        super(firstAst, restAst, env, ep);
        
        super.evaluateFirst();
        super.evaluateRest();
    }

}


/** Lazy cons that is evaluated by name */
class LazyConsByName extends LazyCons {

    /** Constructor where 'first' and 'rest' are not evaluated */
    public LazyConsByName(AST firstAst, AST restAst, PureList<Binding> env, EvaluationPolicy ep) {
        super(firstAst, restAst, env, ep);
    }
    
    /** Lazy evaluation of 'first' (by name) */
    @Override
    public JamVal first() {
        super.evaluateFirst();
        return first;
    }

    /** Lazy evaluation of 'rest' (by name) */
    @Override
    public JamList rest() {
        super.evaluateRest();
        return (JamList)rest;
    }

}


/** Lazy cons that is evaluated by need */
class LazyConsByNeed extends LazyCons {

    /** Constructor where 'first' and 'rest' are not evaluated */
    public LazyConsByNeed(AST firstAst, AST restAst, PureList<Binding> env, EvaluationPolicy ep) {
        super(firstAst, restAst, env, ep);
    }

    /** Lazy evaluation of 'first' (by need) */
    @Override
    public JamVal first() {
        if (first == null) {
            super.evaluateFirst();
        }
        return first;
    }

    /** Lazy evaluation of 'rest' (by need) */
    @Override
    public JamList rest() {
        if (rest == null) {
            super.evaluateRest();
        }
        return (JamList)rest;
    }

}
