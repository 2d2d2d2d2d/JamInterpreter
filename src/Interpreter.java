import java.io.IOException;
import java.io.Reader;

class Interpreter {
    
    private Parser parser;
    
    Interpreter(String fileName) throws IOException { this(new Parser(fileName)); }
   
    Interpreter(Reader reader) { this(new Parser(reader)); }
   
    Interpreter(Parser p) { parser = p; }

    public JamVal callByValue() {
        return null;
    }
   
    public JamVal callByName()  {
        return null;
    }
   
    public JamVal callByNeed()  {
        return null;
    }
}



class EvalException extends RuntimeException {
    EvalException(String msg) { super(msg); }
}