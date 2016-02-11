
/** Evaluation strategies */
enum EvaluationType { CALL_BY_VALUE, CALL_BY_NAME, CALL_BY_NEED }


/** Binding generator */
class ValueBinding {
    
    public static Binding generate(Variable var, AST ast, ASTInterpreter astInterp) {
        EvaluationType type = astInterp.type();
        Binding bind;
        switch (type) {
        case CALL_BY_VALUE:
            bind = new CallByValueBinding(var, ast, astInterp); break;
        case CALL_BY_NAME:
            bind = new CallByNameBinding(var, ast, astInterp); break;
        case CALL_BY_NEED:
            bind = new CallByNeedBinding(var, ast, astInterp); break;
        default:
            throw new EvalException("Invalid evaluation strategy");
        }
        return bind;
    }
    
}


/** Call-by-value binding strategy */
class CallByValueBinding extends Binding {
    CallByValueBinding(Variable var, AST ast, ASTInterpreter astInterp) {
        super(var, ast.accept(astInterp));
    }
    
    @Override
    public JamVal value() { 
        return this.value;
    }
}


/** Call-by-name binding strategy */
class CallByNameBinding extends Binding {
    
    private AST ast;
    
    private PureList<Binding> env;
    
    CallByNameBinding(Variable var, AST ast, ASTInterpreter astInterp) {
        super(var, null);
        this.env = astInterp.env();
        this.ast = ast;
    }
    
    @Override
    public JamVal value() { 
        return this.ast.accept(new ASTInterpreter(this.env, EvaluationType.CALL_BY_NAME));
    }
}


/** Call-by-need binding strategy */
class CallByNeedBinding extends Binding {
    
    private AST ast;
    
    private PureList<Binding> env;
    
    CallByNeedBinding(Variable var, AST ast, ASTInterpreter astInterp) {
        super(var, null);
        this.env = astInterp.env();
        this.ast = ast;
    }
    
    @Override
    public JamVal value() { 
        if (this.value == null) {
            this.value = this.ast.accept(new ASTInterpreter(this.env, EvaluationType.CALL_BY_NEED));
        }
        return this.value; 
    }
}


