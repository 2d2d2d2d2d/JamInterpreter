import java.io.IOException;
import java.io.Reader;

class Interpreter {
    
    private AST ast;
    
    Interpreter(String fileName) throws IOException { this(new Parser(fileName)); }
   
    Interpreter(Reader reader) { this(new Parser(reader)); }
   
    Interpreter(Parser p) { this(p.parse()); }
   
    Interpreter(AST ast) { this.ast = ast; }

    public JamVal callByValue() {
        //System.out.println(ast.toString());
        ASTInterpreter astinterp = new ASTInterpreter(new Empty<Binding>());
        JamVal val = this.ast.accept(astinterp);
        return val;
    }
   
    public JamVal callByName()  {
        return callByValue();
    }
   
    public JamVal callByNeed()  {
        return callByValue();
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


abstract class InterpreterBase {
    protected PureList<Binding> env;
    public InterpreterBase(PureList<Binding> env) { this.env = env; }
}


class CallByValueBinding extends Binding {

    CallByValueBinding(Variable var, AST ast, ASTInterpreter astInterpreter) {
        super(var, ast.accept(astInterpreter));
    }
    
}

