#use "lexer.ml"
#use "parser.ml"
#use "eval.ml"
#use "oUnit.ml"

(* You should have this stuff in eval.ml
 *
type mode = VALUE | NAME | NEED;;
type value =
   VNum of int
 | VList of value list
 | VBool of bool
 | VPrim of string
 | ... (* add your own types here *)
;;
*)
(* need to define function string_of_value: value -> string *)

let valueValueCheck =
  fun output input ->
    assert_equal output (string_of_value (eval_string input VALUE VALUE));;

let valueNameCheck =
  fun output input ->
    assert_equal output (string_of_value (eval_string input VALUE NAME));;

let valueNeedCheck =
  fun output input ->
    assert_equal output (string_of_value (eval_string input VALUE NEED));;


let nameValueCheck =
  fun output input ->
    assert_equal output (string_of_value (eval_string input NAME VALUE));;

let nameNameCheck =
  fun output input ->
    assert_equal output (string_of_value (eval_string input NAME NAME));;

let nameNeedCheck =
  fun output input ->
    assert_equal output (string_of_value (eval_string input NAME NEED));;


let needValueCheck =
  fun output input ->
    assert_equal output (string_of_value (eval_string input NEED VALUE));;

let needNameCheck =
  fun output input ->
    assert_equal output (string_of_value (eval_string input NEED NAME));;

let needNeedCheck =
  fun output input ->
    assert_equal output (string_of_value (eval_string input NEED NEED));;

let allCheck output input = 
  begin
    valueValueCheck output input;
    valueNameCheck output input;
    valueNeedCheck output input;

    nameValueCheck output input;
    nameNameCheck output input;
    nameNeedCheck output input;

    needValueCheck output input;
    needNameCheck output input;
    needNeedCheck output input;
  end
;;

let noNameCheck output input = 
  begin
    valueValueCheck output input;
    valueNameCheck output input;
    valueNeedCheck output input;

    needValueCheck output input;
    needNameCheck output input;
    needNeedCheck output input;
  end
;;

let lazyCheck output input = 
  begin
    valueNameCheck output input;
    valueNeedCheck output input;

    nameNameCheck output input;
    nameNeedCheck output input;

    needNameCheck output input;
    needNeedCheck output input;
  end
;;

let needCheck output input = 
  begin
    needNameCheck output input;
    needNeedCheck output input;
  end
;;

let tests = "assign3test" >::: [

   "numberP" >:: (fun _ -> 
	
	allCheck
		"number?"
		"number?"
	
	
	);

	

   "mathOp" >:: (fun _ -> 
	
	allCheck
		"30"
		"2 * 3 + 12"
	
	
	);

	

   "parseException" >:: (fun _ -> 
	try
                  
	allCheck
		"haha"
		" 1 +"
	with | ParserError(x) -> ()
 
	
	);

	

   "evalException" >:: (fun _ -> 
	try
                  
	allCheck
		"mojo"
		"1 + number?"
	with | EvalError(x) -> ()
 
	
	);

	

   "append" >:: (fun _ -> 
	
	allCheck
		"(1 2 3 1 2 3)"
		"let Y    := map f to              let g := map x to f(map z1,z2 to (x(x))(z1,z2));     in g(g);  APPEND := map ap to            map x,y to               if x = null then y else cons(first(x), ap(rest(x), y)); l      := cons(1,cons(2,cons(3,null))); in (Y(APPEND))(l,l)"
	
	
	);

	

   "letRec" >:: (fun _ -> 
	
	allCheck
		"(1 2 3 1 2 3)"
		"let append :=       map x,y to          if x = null then y else cons(first(x), append(rest(x), y));    l      := cons(1,cons(2,cons(3,null))); in append(l,l)"
	
	
	);

	

   "lazyCons" >:: (fun _ -> 
	
	lazyCheck
		"0"
		"let zeroes := cons(0,zeroes);in first(rest(zeroes))"
	
	
	);

	]

let _ = run_test_tt_main tests
