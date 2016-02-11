import java.util.StringTokenizer;
import junit.framework.TestCase;
import java.io.*;

public class Assign2Test extends TestCase {

    public Assign2Test (String name) {
        super(name);
    }
  
    /**
     * The following 3 check methods create an interpreter object with the
     * specified String as the program, invoke the respective evaluation
     * method (callByValue, callByName, callByNeed), and check that the 
     * result matches the (given) expected output.  If the test fails,
     * the method prints a report as to which test failed and how many
     * points should be deducted.
     */
 
    private void valueCheck(String name, String answer, String program) {
        Interpreter interp = new Interpreter(new StringReader(program));
        assertEquals("by-value " + name, answer, interp.callByValue().toString());
    }

    private void nameCheck(String name, String answer, String program) {
        Interpreter interp = new Interpreter(new StringReader(program));
        assertEquals("by-name " + name, answer, interp.callByName().toString());
    }
   
    private void needCheck(String name, String answer, String program) {
        Interpreter interp = new Interpreter(new StringReader(program));
        assertEquals("by-need " + name, answer, interp.callByNeed().toString());
    }

    private void allCheck(String name, String answer, String program) {
        valueCheck(name, answer, program);
        nameCheck(name, answer, program);
        needCheck(name, answer, program);
    }
 


    public void testFunctionP() {
        try {
            String output1 = "function?";
            String input1 = "function?";
            String output2 = "true";
            String input2 = "function?(number?)";
            allCheck("functionP", output1, input1 );
            allCheck("functionP", output2, input2 );
        } catch (Exception e) {
            e.printStackTrace();
            fail("functionP threw " + e);
        }
    }

    public void testNumberP() {
        try {
            String output1 = "number?";
            String input1 = "number?";
            String output2 = "true";
            String input2 = "number?(123)";
            allCheck("numberP", output1, input1 );
            allCheck("numberP", output2, input2 );
        } catch (Exception e) {
            e.printStackTrace();
            fail("numberP threw " + e);
        }
    }

    public void testListP() {
        try {
            String output1 = "list?";
            String input1 = "list?";
            String output2 = "true";
            String input2 = "list?(cons(1,cons(2,null)))";
            String output3 = "true";
            String input3 = "list?(null)";
            String output4 = "false";
            String input4 = "list?(1)";
            allCheck("listP", output1, input1 );
            allCheck("listP", output2, input2 );
            allCheck("listP", output3, input3 );
            allCheck("listP", output4, input4 );
        } catch (Exception e) {
            e.printStackTrace();
            fail("listP threw " + e);
        }
    }

    public void testConsP() {
        try {
            String output1 = "cons?";
            String input1 = "cons?";
            String output2 = "true";
            String input2 = "cons?(cons(1,cons(2,null)))";
            String output3 = "false";
            String input3 = "cons?(null)";
            String output4 = "false";
            String input4 = "cons?(1)";
            allCheck("consP", output1, input1 );
            allCheck("consP", output2, input2 );
            allCheck("consP", output3, input3 );
            allCheck("consP", output4, input4 );
        } catch (Exception e) {
            e.printStackTrace();
            fail("consP threw " + e);
        }
    }

    public void testNullP() {
        try {
            String output1 = "null?";
            String input1 = "null?";
            String output2 = "false";
            String input2 = "null?(cons(1,cons(2,null)))";
            String output3 = "true";
            String input3 = "null?(null)";
            String output4 = "false";
            String input4 = "null?(1)";
            allCheck("nullP", output1, input1 );
            allCheck("nullP", output2, input2 );
            allCheck("nullP", output3, input3 );
            allCheck("nullP", output4, input4 );
        } catch (Exception e) {
            e.printStackTrace();
            fail("nullP threw " + e);
        }
    }

    public void testArityP() {
        try {
            String output1 = "arity";
            String input1 = "arity";
            String output2 = "1";
            String input2 = "arity(function?)";
            String output3 = "2";
            String input3 = "arity(cons)";
            String output4 = "3";
            String input4 = "arity(map x,y,z to 1)";
            allCheck("arityP", output1, input1 );
            allCheck("arityP", output2, input2 );
            allCheck("arityP", output3, input3 );
            allCheck("arityP", output4, input4 );
        } catch (Exception e) {
            e.printStackTrace();
            fail("arityP threw " + e);
        }
        try {
            String output1 = "fail";
            String input1 = "arity(123)";
            allCheck("arityP", output1, input1 );
        } catch (EvalException e) {
            
        } catch (Exception e) {
            e.printStackTrace();
            fail("arityP threw " + e);
        }
    }

    public void testCons() {
        try {
            String output1 = "cons";
            String input1 = "cons";
            String output2 = "(1 2)";
            String input2 = "cons(1,cons(2,null))";
            allCheck("cons", output1, input1 );
            allCheck("cons", output2, input2 );
        } catch (Exception e) {
            e.printStackTrace();
            fail("cons threw " + e);
        }
    }

    public void testFirst() {
        try {
            String output1 = "first";
            String input1 = "first";
            String output2 = "1";
            String input2 = "first(cons(1,cons(2,null)))";
            allCheck("first", output1, input1 );
            allCheck("first", output2, input2 );
        } catch (Exception e) {
            e.printStackTrace();
            fail("first threw " + e);
        }
    }

    public void testRest() {
        try {
            String output1 = "rest";
            String input1 = "rest";
            String output2 = "(2)";
            String input2 = "rest(cons(1,cons(2,null)))";
            allCheck("rest", output1, input1 );
            allCheck("rest", output2, input2 );
        } catch (Exception e) {
            e.printStackTrace();
            fail("rest threw " + e);
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

    public void testMathOp2() {
        try {
            String output = "60";
            String input = "2 + 3 * 12";
            allCheck("mathOp2", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("mathOp2 threw " + e);
        }
    }
    
    public void testLet() {
        try {
            String output = "6";
            String input = "let x:=1; y:=2; z:= 3; in x+y+z";
            allCheck("let", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("let threw " + e);
        }
    }
    
    public void testLet2() {
        try {
            String output = "9";
            String input = "let func:=map x to x*x; in func(3)";
            allCheck("let", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("let threw " + e);
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
  

    public void testAppend() {
        try {
            String output = "(1 2 3 1 2 3)";
            String input = "let Y    := map f to              "
                         + "let g := map x to f(map z1,z2 to (x(x))(z1,z2));     "
                         + "in g(g);  APPEND := map ap to            map x,y to               "
                         + "if x = null then y else cons(first(x), ap(rest(x), y)); l      "
                         + ":= cons(1,cons(2,cons(3,null))); in (Y(APPEND))(l,l)";
            allCheck("append", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("append threw " + e);
        }
    }
    

 
}
