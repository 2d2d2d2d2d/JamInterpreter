import junit.framework.TestCase;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

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
 
    public void testFiles() {
        try {
            String path = "src/test_data_2/";
            String [] input_files = {"in1.txt", "in2.txt"};
            String [] output_files = {"out1.txt", "out2.txt"};
            
            for (int i = 0; i < input_files.length; i++) {
                String input = new String(Files.readAllBytes(Paths.get(path + input_files[i])));
                String output = new String(Files.readAllBytes(Paths.get(path + output_files[i])));
                output = output.replace("\n", "").replace("\r", "");
                allCheck("files", output, input);
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("files threw " + e);
        }
        
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
            fail("arityP did not throw exception");
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
            String output1 = "18";
            String input1 = "2 * 3 + 12";
            String output2 = "60";
            String input2 = "2 + 3 * 12";
            String output3 = "-10";
            String input3 = "-(2+8)";
            allCheck("mathOp", output1, input1 );
            allCheck("mathOp", output2, input2 );
            allCheck("mathOp", output3, input3 );

        } catch (Exception e) {
            e.printStackTrace();
            fail("mathOp threw " + e);
        }
    }
    
    public void testLet() {
        try {
            String output1 = "6";
            String input1 = "let x:=1; y:=2; z:= 3; in x+y+z";
            String output2 = "9";
            String input2 = "let func:=map x to x*x; in func(3)";
            allCheck("let", output1, input1 );
            allCheck("let", output2, input2 );
        } catch (Exception e) {
            e.printStackTrace();
            fail("let threw " + e);
        }
    }
    
    public void testIf() {
        try {
            String output1 = "1";
            String input1 = "if 5<=5 then 1 else 0";
            String output2 = "0";
            String input2 = "if 5>5 then 1 else 0";
            allCheck("if", output1, input1 );
            allCheck("if", output2, input2 );

        } catch (Exception e) {
            e.printStackTrace();
            fail("if threw " + e);
        }
        try {
            String output = "fail";
            String input = "if 0 then 0 else 0";
            allCheck("if", output, input );
            fail("if did not throw exception");
        } catch (EvalException e) {
            
        } catch (Exception e) {
            e.printStackTrace();
            fail("if threw " + e);
        }
    }
    
    public void testClosureEnv() {
        try {
            String output = "34";
            String input = "let y:=17; in let f:=map x to y+y; in let y:=2; in f(0)";
            allCheck("closureEnv", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("closureEnv threw " + e);
        }
    }
    
    public void testEquality() {
        try {
            String output_true = "true";
            String output_false = "false";
            String input1 = "5 = 5";
            String input2 = "5 = 6";
            String input3 = "cons(1,cons(2,null))=cons(1,cons(2,null))";
            String input4 = "cons(1,cons(2,null))=cons(1,cons(3,null))";
            String input5 = "cons(1,cons(2,null))=cons(1,null)";
            String input6 = "cons? = null?";
            String input7 = "cons? = (map x to x)";
            allCheck("equality", output_true, input1 );
            allCheck("equality", output_false, input2 );
            allCheck("equality", output_true, input3 );
            allCheck("equality", output_false, input4 );
            allCheck("equality", output_false, input5 );
            allCheck("equality", output_false, input6 );
            allCheck("equality", output_false, input7 );

        } catch (Exception e) {
            e.printStackTrace();
            fail("equality threw " + e);
        }
    }
    
    public void testClosureEquality() {
        try {
            String output_true = "true";
            String output_false = "false";
            String input1 = "(map x to x) = (map x to x)";
            String input2 = "let m:=(map x to x); in m = m";
            allCheck("closureEquality", output_false, input1 );
            valueCheck("closureEquality", output_true, input2 );
            nameCheck("closureEquality", output_false, input2 );
            needCheck("closureEquality", output_true, input2 );
        } catch (Exception e) {
            e.printStackTrace();
            fail("closureEquality threw " + e);
        }
    }

    public void testShortCircuit() {
        try {
            String output1 = "true";
            String input1 = "5<=5 | (let f:=x*x; in f(1))";
            String output2 = "false";
            String input2 = " cons?=null? & func(a)";
            allCheck("shortCircuit", output1, input1 );
            allCheck("shortCircuit", output2, input2 );
        } catch (Exception e) {
            e.printStackTrace();
            fail("parseException threw " + e);
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
