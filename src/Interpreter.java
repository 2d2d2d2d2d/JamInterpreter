import java.io.IOException;
import java.io.Reader;

class Interpreter {
    
    private AST ast;
    
    Interpreter(String fileName) throws IOException { this(new Parser(fileName)); }
   
    Interpreter(Reader reader) { this(new Parser(reader)); }
   
    Interpreter(Parser p) { this(p.parse()); }
   
    Interpreter(AST ast) { this.ast = ast; }

    public JamVal callByValue() {
        return this.ast.accept(new ASTInterpreter(new Empty<Binding>(), EvaluationType.CALL_BY_VALUE));
    }
   
    public JamVal callByName()  {
        return this.ast.accept(new ASTInterpreter(new Empty<Binding>(), EvaluationType.CALL_BY_NAME));
    }
   
    public JamVal callByNeed()  {
        return this.ast.accept(new ASTInterpreter(new Empty<Binding>(), EvaluationType.CALL_BY_NEED));
    }
    
    /*
    public static void main(String [] args) {
        String program = "let y:=17; in let f:=map x to y+y; in let y:=2; in f(0)";
        Interpreter interp = new Interpreter(new StringReader(program));
        JamVal val = interp.callByValue();
        System.out.println(val);
    }
    */
}


@SuppressWarnings("serial")
class EvalException extends RuntimeException {
    EvalException(String msg) { super(msg); }
}


enum EvaluationType { CALL_BY_VALUE, CALL_BY_NAME, CALL_BY_NEED }

abstract class InterpreterBase {
    protected PureList<Binding> env;
    protected EvaluationType type;
    public InterpreterBase(PureList<Binding> env, EvaluationType type) { this.env = env; this.type = type; }
}


class CallByValueBinding extends Binding {

    CallByValueBinding(Variable var, AST ast, ASTInterpreter astInterpreter) {
        super(var, ast.accept(astInterpreter));
    }
    
}

