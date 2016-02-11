import junit.framework.*;
import java.io.*;

public class Assign1Test extends TestCase {

  public Assign1Test (String name) {
    super(name);
  }
  
  protected void checkString(String name, String answer, String program) {
    Parser p = new Parser(new StringReader(program));
    assertEquals(name, answer, p.parse().toString());
  }
  
  
  protected void checkFile(String name, 
               String answerFilename,
               String programFilename) {
    try {
      File answerFile = new File(answerFilename);
      InputStream fin = new BufferedInputStream(new FileInputStream(answerFile));
      
      int size = (int) answerFile.length();
      byte[] data = new byte[size];
      fin.read(data,0,size);
      String answer = new String(data);
      fin.close();
      
      Parser p = new Parser(programFilename);
      assertEquals(name, answer, p.parse().toString());      
    } catch (IOException e) {
      fail("Critical error: IOException caught while reading input file");
      e.printStackTrace();
    }
    
  }

  

  public void testAdd() {
    try {
      String output = "(2 + 3)";
      String input = "2+3";
      checkString("add", output, input );

    } catch (Exception e) {
      fail("add threw " + e);
    }
  } //end of func
  

  public void testPrim  () {
    try {
      String output = "first";
      String input = "first";
      checkString("prim  ", output, input );

    } catch (Exception e) {
      fail("prim   threw " + e);
    }
  } //end of func
  

  public void testParseException() {
    try {
      String output = "doh!";
      String input = "map a, to 3";
      checkString("parseException", output, input );

         fail("parseException did not throw ParseException exception");
      } catch (ParseException e) {   
         //e.printStackTrace();
      
    } catch (Exception e) {
      fail("parseException threw " + e);
    }
  } //end of func
  

  public void testLet() {
    try {
      String output = "let a := 3; in (a + a)";
      String input = "let a:=3; in a + a";
      checkString("let", output, input );

    } catch (Exception e) {
      fail("let threw " + e);
    }
  } //end of func
  

  public void testMap() {
    try {
      String output = "map f to (map x to f(x(x)))(map x to f(x(x)))";
      String input = "map f to (map x to f( x( x ) ) ) (map x to f(x(x)))";
      checkString("map", output, input );

    } catch (Exception e) {
      fail("map threw " + e);
    }
  } //end of func
    

  
  
  
  public void testParseExp_Term() {
      try {
          String output = "true";
          String input = " true ";
          checkString("testParseExp_Term", output, input );
      }
      catch (Exception e) {
          fail("testParseExp_Term threw " + e);
      }
  } 
  
  public void testParseExp_TermBinopExp() {
      try {
          String output = "(a(b) + x(y))";
          String input = "(a)(b)+(x)(y)";
          checkString("testParseExp_TermBinopExp", output, input );
      }
      catch (Exception e) {
          fail("testParseExp_TermBinopExp threw " + e);
      }
  } 
  
  public void testParseExp_If() {
      try {
          String output = "if - x then y else z";
          String input = "if -x then   y else z";
          checkString("testParseExp_If", output, input );
      }
      catch (Exception e) {
          fail("testParseExp_If threw " + e);
      }
  } 
  
  public void testParseExp_Let() {
      try {
          String output = "let y := function?(); in number?";
          String input = "let y:=function?(); in number?";
          checkString("testParseExp_Let", output, input );
      }
      catch (Exception e) {
          fail("testParseExp_Let threw " + e);
      }
  } 
  
  public void testParseExp_Map() {
      try {
          String output = "map x,y,z to a";
          String input = "map x, y, z to a";
          checkString("testParseExp_Map", output, input );
      }
      catch (Exception e) {
          fail("testParseExp_Map threw " + e);
      }
  } 
  
  public void testParseTerm_UnopTerm() {
      try {
          String output = "- function?(f())";
          String input = "-function?(f())";
          checkString("testParseTerm_UnopTerm", output, input );
      }
      catch (Exception e) {
          fail("testParseTerm_UnopTerm threw " + e);
      }
  } 
  
  public void testParseTerm_Factor() {
      try {
          String output = "a(b)";
          String input = "(a)(b)";
          checkString("testParseTerm_Factor", output, input );
      }
      catch (Exception e) {
          fail("testParseTerm_Factor threw " + e);
      }
  } 
  
  public void testParseTerm_FactorExpList() {
      try {
          String output = "(if x then y else z)(a, b0, _c?)";
          String input = "(if x then y else z) (a,b0,_c?)";
          checkString("testParseTerm_FactorExpList", output, input );
      }
      catch (Exception e) {
          fail("testParseTerm_FactorExpList threw " + e);
      }
  } 
  
  public void testParseTerm_Constant() {
      try {
          String output = "null";
          String input = "null ";
          checkString("testParseTerm_Constant", output, input );
      }
      catch (Exception e) {
          fail("testParseTerm_Constant threw " + e);
      }
  } 
  
  public void testParseFactor_Exp() {
      try {
          String output = "if x then y else z";
          String input = "if x then y else z";
          checkString("testParseFactor_Exp", output, input );
      }
      catch (Exception e) {
          fail("testParseFactor_Exp threw " + e);
      }
  } 
  
  public void testParseFactor_Prim() {
      try {
          String output = "function?";
          String input = "function?";
          checkString("testParseFactor_Prim", output, input );
      }
      catch (Exception e) {
          fail("testParseFactor_Prim threw " + e);
      }
  } 
  
  public void testParseFactor_Id() {
      try {
          String output = "_x0";
          String input = "_x0";
          checkString("testParseFactor_Id", output, input );
      }
      catch (Exception e) {
          fail("testParseFactor_Id threw " + e);
      }
  }
  
  public void testParseException_1() {
      try {
          String output = "fail";
          String input = "x := y";
          checkString("testParseException_1", output, input );
          fail("parseException did not throw ParseException exception");
      }
      catch (ParseException e) {
      }
      catch (Exception e) {
          fail("testParseException_1 threw " + e);
      }
  }
  
  public void testParseException_2() {
      try {
          String output = "fail";
          String input = "if a then b else;";
          checkString("testParseException_2", output, input );
          fail("parseException did not throw ParseException exception");
      }
      catch (ParseException e) {
      }
      catch (Exception e) {
          fail("testParseException_2 threw " + e);
      }
  }
  
  public void testParseException_3() {
      try {
          String output = "fail";
          String input = "map x,y, to z";
          checkString("testParseException_3", output, input );
          fail("parseException did not throw ParseException exception");
      }
      catch (ParseException e) {
      }
      catch (Exception e) {
          fail("testParseException_3 threw " + e);
      }
  }
  
  public void testParseHard() {
      try {
          String output = "let f := map n to if (n = 0) then 1 else (n * f((n - 1))); "
                        + "in let f := map n,m,k to if ((((n <= 0) & n) >= 0) | "
                        + "(((((n < 0) & n) > 0) & n) != 0)) then number? else (m / f((k + 1))); "
                        + "in let x := 3; y := 4; z := cons?(function?((x * ~ y)), cons(- arity(x))); "
                        + "in let x := 3; y := 4; z := g(); in (g(x, y, z))(null?(true), list?(false), first(null))";
          String input = "let \r\n f := map n to if n = 0 then 1 else n * f(n - 1); "
                       + "\r\n in \r\n let \r\n f := map n,m,k to if (n <= 0 & n >= 0)"
                       + "\r\n | (n < 0 & n > 0 & n != 0) then number?\r\n else m / f(k + 1);"
                       + "\r\n in \r\n  let x:=3;\r\n   y:=4;\r\n  z:=cons?(function?(x * ~y),"
                       + " cons(-arity(x)));\r\n  in \r\n let x:=3;\r\n y:=4;\r\n  z:=g();\r\n"
                       + "  in \r\n (g(x,y,z))(null?(true),list?(false),first(null))";
          checkString("testParseException_3", output, input );
      }
      catch (Exception e) {
          fail("testParseException_3 threw " + e);
      }
  }

}