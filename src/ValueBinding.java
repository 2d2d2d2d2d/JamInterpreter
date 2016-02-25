import java.util.ArrayList;
import java.util.List;

/** Binding generator */
class ValueBinding {
    
    public static Binding generate(Variable var, AST ast, ASTInterpreter astInterp) {
        EvaluationPolicy ep = astInterp.ep();
        Binding bind;
        switch (ep.callType) {
        case BY_VALUE:
            bind = new CallByValueBinding(var, ast, astInterp); break;
        case BY_NAME:
            bind = new CallByNameBinding(var, ast, astInterp); break;
        case BY_NEED:
            bind = new CallByNeedBinding(var, ast, astInterp); break;
        default:
            throw new EvalException("Invalid evaluation strategy");
        }
        return bind;
    }
    
    public static PureList<Binding> generate(Variable[] vars, AST[] asts, ASTInterpreter astInterp) {
        PureList<Binding> new_env = astInterp.env();
        List<Binding> added_env = new ArrayList<Binding>();
        
        int length = vars.length;
        for(int i = 0 ; i < length; i++) {
            Variable var = vars[i];
            AST ast = asts[i];
            Binding bind = generate(var, null, astInterp);
            bind.ast = ast;
            new_env = new_env.cons(bind);
            added_env.add(bind);
        }
        
        for (Binding bind : added_env) {
            bind.env = new_env;
        }
        
        for (Binding bind : added_env) {
            bind.evaluate();
        }
        
        return new_env;
    }
}



/** Call-by-value binding strategy */
class CallByValueBinding extends Binding {
    
    EvaluationPolicy ep;
    
    CallByValueBinding(Variable var, AST ast, ASTInterpreter astInterp) {
        super(var, (ast == null)? null: ast.accept(astInterp));
        this.ep = astInterp.ep;
    }
    
    @Override
    public JamVal value() { 
        if (this.value != null) {
            return this.value;
        }
        else if (this.ast != null) {
            this.value = this.ast.accept(new ASTInterpreter(this.env, this.ep));
            return this.value;
        }
        throw new EvalException("Variable '" + this.var.toString() + "' is not binded with any value");
    }
    
    @Override
    public void evaluate() {
        this.value();
    }
}


/** Call-by-name binding strategy */
class CallByNameBinding extends Binding {
    
    EvaluationPolicy ep;
    
    CallByNameBinding(Variable var, AST ast, ASTInterpreter astInterp) {
        super(var, null);
        this.env = astInterp.env();
        this.ast = ast;
        this.ep = astInterp.ep;
    }
    
    @Override
    public JamVal value() { 
        return this.ast.accept(new ASTInterpreter(this.env, this.ep));
    }
}


/** Call-by-need binding strategy */
class CallByNeedBinding extends Binding {
    
    EvaluationPolicy ep;
    
    CallByNeedBinding(Variable var, AST ast, ASTInterpreter astInterp) {
        super(var, null);
        this.env = astInterp.env();
        this.ast = ast;
        this.ep = astInterp.ep;
    }
    
    @Override
    public JamVal value() { 
        if (this.value == null) {
            this.value = this.ast.accept(new ASTInterpreter(this.env, this.ep));
        }
        return this.value; 
    }
}


