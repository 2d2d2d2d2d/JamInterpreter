import junit.framework.TestCase;
import java.io.*;

/**
 * testing framework for typed jam
 *
 **/
public class Assign6Test extends TestCase {

  public Assign6Test (String name) {
    super(name);
  }
  
 
  private void eagerCheck(String name, String answer, String program) {
      Interpreter interp = new Interpreter(new StringReader(program));
      assertEquals("by-value-value " + name, answer, interp.eval().toString());
  }

  private void cpsCheck(String name, String answer, String program) {
      Interpreter interp = new Interpreter(new StringReader(program));
      assertEquals("by-value-value " + name, answer, interp.cpsEval().toString());
  }
  private void SDEagerCheck(String name, String answer, String program) {
      Interpreter interp = new Interpreter(new StringReader(program));
      assertEquals("by-value-value " + name, answer, interp.SDEval().toString());
  }
  private void SDCpsCheck(String name, String answer, String program) {
      Interpreter interp = new Interpreter(new StringReader(program));
      assertEquals("by-value-value " + name, answer, interp.SDCpsEval().toString());
  }

  private void allCheck(String name, String answer, String program) {
    eagerCheck(name, answer, program);
    cpsCheck(name, answer, program);
    SDEagerCheck(name, answer, program);
    SDCpsCheck(name, answer, program);
  }
  private void nonCPSCheck(String name, String answer, String program) {
    eagerCheck(name, answer, program);
    SDEagerCheck(name, answer, program);
  }
  
  private void unshadowConvert(String name, String answer, String program) {
      Interpreter interp = new Interpreter(new StringReader(program));

      String result = renameVars(interp.unshadow()).toString();
      assertEquals("shadowCheck " + name, answer, result);
  }

  private void cpsConvert(String name, String answer, String program) {
      Interpreter interp = new Interpreter(new StringReader(program));

      String result = renameVars(interp.convertToCPS()).toString();
      assertEquals("shadowCheck " + name, answer, result);
  }

  private void sdConvert(String name, String answer, String program) {
      Interpreter interp = new Interpreter(new StringReader(program));

      String result = renameVars(interp.convertToSD()).toString();
      assertEquals("shadowCheck " + name, answer, result);
  }

  private AST renameVars(AST tree) { return tree; }

  

  public void testBadLetrec() {
    try {
      String output = "!";
      String input = "letrec x:=4; in x";
      allCheck("badLetrec", output, input );

         fail("badLetrec did not throw ParseException exception");
      } catch (ParseException e) {   
         //e.printStackTrace();
      
    } catch (Exception e) {
      e.printStackTrace();
      fail("badLetrec threw " + e);
    }
  } //end of func
  

  public void testBadLet() {
    try {
      String output = "!";
      String input = "let x:= map z to y(z);\n             y:= map z to x(z); in x(5)";
      allCheck("badLet", output, input );

         fail("badLet did not throw SyntaxException exception");
      } catch (SyntaxException e) {   
         //e.printStackTrace();
      
    } catch (Exception e) {
      e.printStackTrace();
      fail("badLet threw " + e);
    }
  } //end of func
  

  public void testUuop() {
    try {
      String output = "(3 + 3)";
      String input = "3 + 3";
      unshadowConvert("Uuop", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Uuop threw " + e);
    }
  } //end of func
  

  public void testSuop() {
    try {
      String output = "(3 + 3)";
      String input = "3 + 3";
      sdConvert("Suop", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Suop threw " + e);
    }
  } //end of func
  

  public void testCuop() {
    try {
      String output = "(map x to x)((3 + 3))";
      String input = "3 + 3";
      cpsConvert("Cuop", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Cuop threw " + e);
    }
  } //end of func
  

  public void testUop() {
    try {
      String output = "6";
      String input = "3 + 3";
      allCheck("uop", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("uop threw " + e);
    }
  } //end of func
  

  public void testUdeep() {
    try {
      String output = "let x:1 := map x:1 to letrec x:2 := map x:3 to x:3; in x:2(x:2); y:1 := let x:1 := 5; in x:1; in x:1(y:1)";
      String input = "let x:= map x to \n     letrec x:=map x to x; in x(x);\n    y:= let x:=5; in x;\n  in x(y)";
      unshadowConvert("Udeep", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Udeep threw " + e);
    }
  } //end of func
  

  public void testSdeep() {
    try {
      String output = "let [*2*] map [*1*] to letrec [*1*] map [*1*] to [0,0]; in ([0,0])([0,0]); let [*1*] 5; in [0,0]; in ([0,0])([0,1])";
      String input = "let x:= map x to \n     letrec x:=map x to x; in x(x);\n    y:= let x:=5; in x;\n  in x(y)";
      sdConvert("Sdeep", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Sdeep threw " + e);
    }
  } //end of func
  

  public void testUmap() {
    try {
      String output = "map z:1 to z:1";
      String input = "map z to z";
      unshadowConvert("Umap", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Umap threw " + e);
    }
  } //end of func
  

  public void testSmap() {
    try {
      String output = "map [*1*] to [0,0]";
      String input = "map z to z";
      sdConvert("Smap", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Smap threw " + e);
    }
  } //end of func
  

  public void testCmap() {
    try {
      String output = "(map x to x)(map z:1,:0 to :0(z:1))";
      String input = "map z to z";
      cpsConvert("Cmap", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Cmap threw " + e);
    }
  } //end of func
  

  public void testCarity() {
    try {
      String output = "(map x to x)(map x,k to k((arity(x) - 1)))";
      String input = "arity";
      cpsConvert("Carity", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Carity threw " + e);
    }
  } //end of func
  

  public void testCfirst() {
    try {
      String output = "(map x to x)(map x,k to k(first(x)))";
      String input = "first";
      cpsConvert("Cfirst", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Cfirst threw " + e);
    }
  } //end of func
  

  public void testCcons() {
    try {
      String output = "(map x to x)(map x,y,k to k(cons(x, y)))";
      String input = "cons";
      cpsConvert("Ccons", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Ccons threw " + e);
    }
  } //end of func
  

  public void testClist() {
    try {
      String output = "(map x to x)(first(rest(rest(cons(1, cons(2, cons(3, null)))))))";
      String input = "first(rest(rest(cons(1, cons(2, cons(3, null))))))";
      cpsConvert("Clist", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Clist threw " + e);
    }
  } //end of func
  

  public void testUappend() {
    try {
      String output = "letrec append:1 := map x:2,y:2 to if (x:2 = null) then y:2 else cons(first(x:2), append:1(rest(x:2), y:2)); in let s:2 := cons(1, cons(2, cons(3, null))); in append:1(s:2, s:2)";
      String input = "letrec append := map x,y to\n          if x = null then y else cons(first(x), append(rest\n(x), y));\n            in let s := cons(1,cons(2,cons(3,null)));\n          in append(s,s)";
      unshadowConvert("Uappend", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Uappend threw " + e);
    }
  } //end of func
  

  public void testSappend() {
    try {
      String output = "letrec [*1*] map [*2*] to if ([0,0] = null) then [0,1] else cons(first([0,0]), ([1,0])(rest([0,0]), [0,1])); in let [*1*] cons(1, cons(2, cons(3, null))); in ([1,0])([0,0], [0,0])";
      String input = "letrec append := map x,y to\n          if x = null then y else cons(first(x), append(rest\n(x), y));\n            in let s := cons(1,cons(2,cons(3,null)));\n          in append(s,s)";
      sdConvert("Sappend", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Sappend threw " + e);
    }
  } //end of func
  

  public void testCappend() {
    try {
      String output = "letrec append:1 := map x:2,y:2,:0 to if (x:2 = null) then :0(y:2) else let :1 := first(x:2); in append:1(rest(x:2), y:2, map :3 to :0(let :2 := :3; in cons(:1, :2))); in let s:2 := cons(1, cons(2, cons(3, null))); in append:1(s:2, s:2, map x to x)";
      String input = "letrec append := map x,y to\n          if x = null then y else cons(first(x), append(rest\n(x), y));\n            in let s := cons(1,cons(2,cons(3,null)));\n          in append(s,s)";
      cpsConvert("Cappend", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Cappend threw " + e);
    }
  } //end of func
  

  public void testAppend() {
    try {
      String output = "(1 2 3 1 2 3)";
      String input = "letrec append := map x,y to\n          if x = null then y else cons(first(x), append(rest\n(x), y));\n            in let s := cons(1,cons(2,cons(3,null)));\n          in append(s,s)";
      allCheck("append", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("append threw " + e);
    }
  } //end of func
  

  public void testUappend1() {
    try {
      String output = "letrec appendz1:1 := map xz2:2,yz2:2,z0:2 to if (xz2:2 = null) then z0:2(yz2:2) else let z1:3 := first(xz2:2); in appendz1:1(rest(xz2:2), yz2:2, map z3:4 to z0:2(let z2:5 := z3:4; in cons(z1:3, z2:5))); in let sz2:2 := cons(1, cons(2, cons(3, null))); in appendz1:1(sz2:2, sz2:2, map x:3 to x:3)";
      String input = "letrec appendz1 := map xz2,yz2,z0 to if (xz2 =null) then z0(yz2) else let z1 := first(xz2); in appendz1(rest(xz2), yz2, map z3 to z0(let z2 := z3; in cons(z1, z2))); in let sz2 := cons(1, cons(2, cons(3, null))); in appendz1(sz2, sz2, map x to x)";
      unshadowConvert("Uappend1", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Uappend1 threw " + e);
    }
  } //end of func
  

  public void testCappend1() {
    try {
      String output = "letrec appendz1:1 := map xz2:2,yz2:2,z0:2,:0 to if (xz2:2 = null) then z0:2(yz2:2, :0) else let z1:3 := first(xz2:2); in appendz1:1(rest(xz2:2), yz2:2, map z3:4,:1 to z0:2(let z2:5 := z3:4; in cons(z1:3, z2:5), :1), :0); in let sz2:2 := cons(1, cons(2, cons(3, null))); in appendz1:1(sz2:2, sz2:2, map x:3,:2 to :2(x:3), map x to x)";
      String input = "letrec appendz1 := map xz2,yz2,z0 to if (xz2 =null) then z0(yz2) else let z1 := first(xz2); in appendz1(rest(xz2), yz2, map z3 to z0(let z2 := z3; in cons(z1, z2))); in let sz2 := cons(1, cons(2, cons(3, null))); in appendz1(sz2, sz2, map x to x)";
      cpsConvert("Cappend1", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Cappend1 threw " + e);
    }
  } //end of func
  

  public void testSfact() {
    try {
      String output = "let [*1*] 6; in letrec [*1*] map [*1*] to let [*1*] map [*1*] to ([1,0])(map [*1*] to (([1,0])([1,0]))([0,0])); in ([0,0])([0,0]); in let [*1*] map [*1*] to map [*1*] to if ([0,0] = 0) then 1 else ([0,0] * ([1,0])(([0,0] - 1))); in (([1,0])([0,0]))([2,0])";
      String input = "let n:= 6; in\n   letrec Y := map f to let g := map x to f(map z to (x(x))(z)); in g(g);\n   in \n    let \n       FACT := map f to map n to if n = 0 then 1 else n * f(n - 1);\n      in (Y(FACT))(n)";
      sdConvert("Sfact", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Sfact threw " + e);
    }
  } //end of func
  

  public void testCfact() {
    try {
      String output = "let Y:1 := map f:1,:0 to let g:2 := map x:2,:1 to f:1(map z:3,:2 to x:2(x:2, map :5 to let :3 := :5; in let :4 := z:3; in :3(:4, :2)), :1); in g:2(g:2, :0); in let FACT:1 := map f:1,:6 to :6(map n:2,:7 to if (n:2 = 0) then :7(1) else let :8 := n:2; in f:1((n:2 - 1), map :10 to :7(let :9 := :10; in (:8 * :9)))); in Y:1(FACT:1, map :13 to let :11 := :13; in let :12 := 6; in :11(:12, map x to x))";
      String input = "let Y := map f to let g := map x to f(map z to (x(x))(z)); in g(g);\n         FACT := map f to map n to if n = 0 then 1 else n * f(n - 1);\n      in (Y(FACT))(6)";
      cpsConvert("Cfact", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Cfact threw " + e);
    }
  } //end of func
  

  public void testFact() {
    try {
      String output = "720";
      String input = "let n:= 6; in\n   letrec Y := map f to let g := map x to f(map z to (x(x))(z)); in g(g);\n   in \n    let \n       FACT := map f to map n to if n = 0 then 1 else n * f(n - 1);\n      in (Y(FACT))(n)";
      allCheck("fact", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("fact threw " + e);
    }
  } //end of func
  

  public void testUand() {
    try {
      String output = "map x:1 to if map y:2 to y:2 then x:1 else false";
      String input = "map x to (map y to y) & x";
      unshadowConvert("Uand", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Uand threw " + e);
    }
  } //end of func
  

  public void testUor() {
    try {
      String output = "map x:1 to if map y:2 to y:2 then true else x:1";
      String input = "map x to (map y to y) | x";
      unshadowConvert("Uor", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Uor threw " + e);
    }
  } //end of func
  

  public void testUletcc() {
    try {
      String output = "letcc x:1 in if true then x:1(5) else 3";
      String input = "letcc x in  if true then  x(5)  else 3";
      unshadowConvert("Uletcc", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Uletcc threw " + e);
    }
  } //end of func
  

  public void testCletcc() {
    try {
      String output = "let x:1 := map :0,:1 to (map x to x)(:0); in if true then x:1(5, map x to x) else (map x to x)(3)";
      String input = "letcc x in  if true then  x(5)  else 3";
      cpsConvert("Cletcc", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Cletcc threw " + e);
    }
  } //end of func

  

  public void testNumberP() {
    try {
      String output = "number?";
      String input = "number?";
      eagerCheck("numberP", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("numberP threw " + e);
    }
  } //end of func
  

  public void testMathOp() {
    try {
      String output = "18";
      String input = "2 * 3 + 12";
      allCheck("mathOp", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("mathOp threw " + e);
    }
  } //end of func
  

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
  } //end of func
  

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
  } //end of func
  

  public void testAppendOld() {
    try {
      String output = "(1 2 3 1 2 3)";
      String input = "let Y    := map f to              let g := map x to f(map z1,z2 to (x(x))(z1,z2));     in g(g);  APPEND := map ap to            map x,y to               if x = null then y else cons(first(x), ap(rest(x), y)); l      := cons(1,cons(2,cons(3,null))); in (Y(APPEND))(l,l)";
      allCheck("append", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("append threw " + e);
    }
  } //end of func
  

  public void testEmptyBlock() {
    try {
      String output = "0";
      String input = "{ }";
      allCheck("emptyBlock", output, input );

         fail("emptyBlock did not throw ParseException exception");
      } catch (ParseException e) {   
         //e.printStackTrace();
      
    } catch (Exception e) {
      e.printStackTrace();
      fail("emptyBlock threw " + e);
    }
  } //end of func
  

  public void testBlock() {
    try {
      String output = "1";
      String input = "{3; 2; 1}";
      allCheck("block", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("block threw " + e);
    }
  } //end of func
  

  public void testDupVar() {
    try {
      String output = "ha!";
      String input = "let x:=3; x:=4; in x";
      allCheck("dupVar", output, input );

         fail("dupVar did not throw SyntaxException exception");
      } catch (SyntaxException e) {   
         //e.printStackTrace();
      
    } catch (Exception e) {
      e.printStackTrace();
      fail("dupVar threw " + e);
    }
  } //end of func
  

  public void testRefApp() {
    try {
      String output = "(ref 17)";
      String input = "let x := ref 10; in {x <- 17; x}";
      allCheck("refApp", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("refApp threw " + e);
    }
  } //end of func
  

  public void testRefException() {
    try {
      String output = "bomb";
      String input = "let x := ref 10; in {!x <- 17}";
      allCheck("refException", output, input );
      
      fail("refException did not throw EvalException exception");
    } catch (EvalException e) {   
       //e.printStackTrace();
    
    } catch (Exception e) {
      e.printStackTrace();
      fail("refException threw " + e);
    }
  } //end of func
  

  public void testRefref() {
    try {
      String output = "(ref (ref 4))";
      String input = "let x:= ref 4; in let y:= ref x; in y";
      allCheck("refref", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("refref threw " + e);
    }
  } //end of func
  

  public void testRefP() {
    try {
        String input = "ref? (let x:= ref 4; in let y:= ref x; in !y)";
        allCheck("refP", "true", input );
        String input2 = "ref? (let x:= ref 4; in let y:= ref x; in !!y)";
        allCheck("refP", "false", input2 );

    } catch (Exception e) {
      e.printStackTrace();
      fail("refP threw " + e);
    }
  } //end of func
  

  public void testBangApp() {
    try {
      String output = "10";
      String input = "let x := ref 10; in !x";
      allCheck("bangApp", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("bangApp threw " + e);
    }
  } //end of func
  

  public void testBangException() {
    try {
      String output = "10";
      String input = "let x := ref 10; in !!x";
      allCheck("bangException", output, input );

      fail("bangException did not throw EvalException exception");
    } catch (EvalException e) {
        
    } catch (Exception e) {
      e.printStackTrace();
      fail("bangException threw " + e);
    }
  } //end of func
  

  public void testUnit() {
    try {
      String output = "unit";
      String input = "let x := ref 10; in x<-20";
      allCheck("bangApp", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("unit threw " + e);
    }
  } //end of func
  

  public void testRefCompare() {
    try {
      String output = "false";
      String input = "let x := ref 10; y := ref 10; in x = y";
      allCheck("refCompare", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("refCompare threw " + e);
    }
  } //end of func
  

  public void testRefCompare2() {
    try {
      String input = "let x := ref 10; in x = x";
      allCheck("refCompare2", "true", input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("refCompare2 threw " + e);
    }
  } //end of func
  

  public void testRefCompare3() {
    try {
      String input = "let x := ref ref 10; in let y := x; in { !x <- 20; !y <- 30; x = y}";
      allCheck("refCompare3", "true", input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("refCompare3 threw " + e);
    }
  } //end of func
  
  
  
  
}





