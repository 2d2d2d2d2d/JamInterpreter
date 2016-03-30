#use "lexer.ml"
#use "parser.ml"
#use "eval.ml"
#use "oUnit.ml"

(* You should have this stuff in eval.ml
 *
type mode = EAGER | LAZY;;
type value =
   VNum of int
 | VList of value list
 | VBool of bool
 | VPrim of string
 | ... (* add your own types here *)
;;
*)
(* need to define function string_of_value: value -> string *)

let eagerCheck =
  fun output input ->
    assert_equal output (string_of_value (eval_string input EAGER));;

let lazyCheck =
  fun output input ->
    assert_equal output (string_of_value (eval_string input LAZY));;

let allCheck output input = 
  begin
    eagerCheck output input;
    lazyCheck output input;
  end
;;


let tests = "assign5test" >::: [

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

	

   "append" >:: (fun _ -> 
	
	allCheck
		"(1 2 3 1 2 3)"
		"let append:  (list int, list int -> list int) :=       map x: list int, y: list int to         if x = null: int then y else cons(first(x), append(rest(x), y));     s: list int := cons(1,cons(2,cons(3,null: int))); in append(s,s)"
	
	
	);

	

   "null" >:: (fun _ -> 
	
	allCheck
		"()"
		"null: list (int, bool, list ref int -> unit)"
	
	
	);

	

   "emptyBlock" >:: (fun _ -> 
	try
                  
	allCheck
		"0"
		"{ }"
	with | ParserError(x) -> ()
 
	
	);

	

   "block" >:: (fun _ -> 
	
	allCheck
		"1"
		"{3; 2; 1}"
	
	
	);

	

   "dupVar" >:: (fun _ -> 
	try
                  
	allCheck
		"ha!"
		"let x: int :=3; x:int :=4; in x"
	with | SyntaxError(x) -> ()
 
	
	);

	

   "refApp" >:: (fun _ -> 
	
	allCheck
		"(ref 17)"
		"let x: ref int := ref 10; in {x <- 17; x}"
	
	
	);

	

   "bangApp" >:: (fun _ -> 
	
	allCheck
		"10"
		"let x: ref int := ref 10; in !x"
	
	
	);

	

   "assign" >:: (fun _ -> 
	try
                  
	allCheck
		"true"
		"let x: int :=5; y: bool :=true; in x !=y"
	with | TypeError(x) -> ()
 
	
	);

	

   "badAssign" >:: (fun _ -> 
	try
                  
	allCheck
		"0"
		"let x: int := 10; in x <- 5"
	with | TypeError(x) -> ()
 
	
	);

	]

let _ = run_test_tt ~verbose:true tests
