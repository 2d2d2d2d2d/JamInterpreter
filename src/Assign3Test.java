import junit.framework.TestCase;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Assign3Test extends TestCase {

    public Assign3Test (String name) {
        super(name);
    }
  
    /**
     * The following 9 check methods create an interpreter object with the
     * specified String as the program, invoke the respective evaluation
     * method (valueValue, valueName, valueNeed, etc.), and check that the 
     * result matches the (given) expected output.  
     */

    private void valueValueCheck(String name, String answer, String program) {
        Interpreter interp = new Interpreter(new StringReader(program));
        assertEquals("by-value-value " + name, answer, interp.valueValue().toString());
    }

    private void valueNameCheck(String name, String answer, String program) {
        Interpreter interp = new Interpreter(new StringReader(program));
        assertEquals("by-value-name " + name, answer, interp.valueName().toString());
    }

    private void valueNeedCheck(String name, String answer, String program) {
        Interpreter interp = new Interpreter(new StringReader(program));
        assertEquals("by-value-need " + name, answer, interp.valueNeed().toString());
    }

    private void nameValueCheck(String name, String answer, String program) {
        Interpreter interp = new Interpreter(new StringReader(program));
        assertEquals("by-value " + name, answer, interp.nameValue().toString());
    }

    private void nameNameCheck(String name, String answer, String program) {
        Interpreter interp = new Interpreter(new StringReader(program));
        assertEquals("by-name " + name, answer, interp.nameName().toString());
    }

    private void nameNeedCheck(String name, String answer, String program) {
        Interpreter interp = new Interpreter(new StringReader(program));
        assertEquals("by-need " + name, answer, interp.nameNeed().toString());
    }

    private void needValueCheck(String name, String answer, String program) {
        Interpreter interp = new Interpreter(new StringReader(program));
        assertEquals("by-value " + name, answer, interp.needValue().toString());
    }

    private void needNameCheck(String name, String answer, String program) {
        Interpreter interp = new Interpreter(new StringReader(program));
        assertEquals("by-name " + name, answer, interp.needName().toString());
    }
   
    private void needNeedCheck(String name, String answer, String program) {
        Interpreter interp = new Interpreter(new StringReader(program));
        assertEquals("by-need " + name, answer, interp.needNeed().toString());
    }

    private void allCheck(String name, String answer, String program) {
        valueValueCheck(name, answer, program);
        valueNameCheck(name, answer, program);
        valueNeedCheck(name, answer, program);
        nameValueCheck(name, answer, program);
        nameNameCheck(name, answer, program);
        nameNeedCheck(name, answer, program);
        needValueCheck(name, answer, program);
        needNameCheck(name, answer, program);
        needNeedCheck(name, answer, program);
    }

    private void noNameCheck(String name, String answer, String program) {
        valueValueCheck(name, answer, program);
        valueNameCheck(name, answer, program);
        valueNeedCheck(name, answer, program);
        needValueCheck(name, answer, program);
        needNameCheck(name, answer, program);
        needNeedCheck(name, answer, program);
    }

    private void needCheck(String name, String answer, String program) {
        needValueCheck(name, answer, program);
        needNeedCheck(name, answer, program);
    }


    private void lazyCheck(String name, String answer, String program) {
        valueNameCheck(name, answer, program);
        valueNeedCheck(name, answer, program);
        nameNameCheck(name, answer, program);
        nameNeedCheck(name, answer, program);
        needNameCheck(name, answer, program);
        needNeedCheck(name, answer, program);
    }



    public void testNumberP() {
        try {
            String output = "number?";
            String input = "number?";
            allCheck("numberP", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("numberP threw " + e);
        }
    }


    public void testMathOp() {
        try {
            String output = "18";
            String input = "2 * 3 + 12";
            allCheck("mathOp", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("mathOp threw " + e);
        }
    }


    public void testParseException() {
        try {
            String output = "haha";
            String input = " 1 +";
            allCheck("parseException", output, input );

            fail("parseException did not throw ParseException exception");
        } catch (ParseException e) {   
            //e.printStackTrace();      
        } catch (Exception e) {
            e.printStackTrace();
            fail("parseException threw " + e);
        }
    }


    public void testEvalException() {
        try {
            String output = "mojo";
            String input = "1 + number?";
            allCheck("evalException", output, input );

            fail("evalException did not throw EvalException exception");
        } catch (EvalException e) {   
            //e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            fail("evalException threw " + e);
        }
    }


    public void testFreeVariableLet() {
        try {
            String output = "gyjj";
            String input = "let x := 1; in y";
            allCheck("freeVariableLet", output, input );

            fail("freeVariableLet did not throw FreeVariableLet exception");
        } catch (SyntaxException e) {   
            //e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            fail("freeVariableLet threw " + e);
        }
    }


    public void testFreeVariableMap() {
        try {
            String output = "jygg";
            String input = "map x to y";
            allCheck("freeVariableMap", output, input );

            fail("freeVariableMap did not throw FreeVariableMap exception");
        } catch (SyntaxException e) {   
            //e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            fail("freeVariableMap threw " + e);
        }
    }


    public void testDuplicateVariable() {
        try {
            String output = "gyjj";
            String input = "let x := 1; x := 2; in x";
            allCheck("duplicateVariable", output, input );

            fail("duplicateVariable did not throw DuplicateVariable exception");
        } catch (SyntaxException e) {   
            //e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            fail("duplicateVariable threw " + e);
        }
    }


    public void testDivideByZero() {
        try {
            String output = "crash";
            String input = "5 / 0";
            allCheck("divideByZero", output, input);

            fail("divideByZero did not throw DivideByZero exception");
        } catch (EvalException e) {   
            //e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            fail("divideByZero threw " + e);
        }
    }


    public void testAppend() {
        try {
            String output = "(1 2 3 1 2 3)";
            String input = "let Y    := map f to              let g := map x to f(map z1,z2 to (x(x))(z1,z2));     in g(g);  APPEND := map ap to            map x,y to               if x = null then y else cons(first(x), ap(rest(x), y)); l      := cons(1,cons(2,cons(3,null))); in (Y(APPEND))(l,l)";
            allCheck("append", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("append threw " + e);
        }
    }


    public void testLetRec() {
        try {
            String output = "(1 2 3 1 2 3)";
            String input = "let append :=       map x,y to          if x = null then y else cons(first(x), append(rest(x), y));    l      := cons(1,cons(2,cons(3,null))); in append(l,l)";
            allCheck("letRec", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("letRec threw " + e);
        }
    }


    public void testLazyCons() {
        try {
            String output = "0";
            String input = "let zeroes := cons(0,zeroes);in first(rest(zeroes))";
            lazyCheck("lazyCons", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("lazyCons threw " + e);
        }
    }


    public void testConsCompare() {
        try {
            String input = "let c := cons(1, a); a := cons(2, null); b := cons(1,cons(2, null)); in b = c ";
            allCheck("consCompare", "true", input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("consCompare threw " + e);
        }
    }


    public void testNameCheck() {
        try {
            String input = "let a := map x to x; b := map x to x; in a = a";
            noNameCheck("nameCheck", "true", input );
            nameValueCheck("nameCheck", "false", input );
            nameNameCheck("nameCheck", "false", input );
            nameNeedCheck("nameCheck", "false", input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("nameCheck threw " + e);
        }
    }
 

    public void testFib() {
        try {
            String input = new String(Files.readAllBytes(Paths.get("test_data_3/name.CBNFib.in")));
            String output = new String(Files.readAllBytes(Paths.get("test_data_3/name.CBNFib.out")));
            output = output.replace("\n", "").replace("\r", "");
            needCheck("fib", output, input);

        } catch (Exception e) {
            e.printStackTrace();
            fail("fib threw " + e);
        }
        
    }
    
    
    public void testSieve() {
        try {
            String input = new String(Files.readAllBytes(Paths.get("test_data_3/name.sieve.in")));
            String output = new String(Files.readAllBytes(Paths.get("test_data_3/name.sieve.out")));
            output = output.replace("\n", "").replace("\r", "");
            needCheck("sieve", output, input);

        } catch (Exception e) {
            e.printStackTrace();
            fail("sieve threw " + e);
        }
        
    }
    
}




