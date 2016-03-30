(require (lib "test.ss" "schemeunit"))
(load "eval.ss")

;; testing framework for typed jam
;;
;;

(define sa string-append)  
(define los2 
  (lambda (list pre in post)
    (letrec [(helper (lambda (list pre in) 
                       (if (null? list)  ""
                           (sa pre (car list) 
                               (helper (cdr list) in in)))))]
      (sa pre (helper list "" in) post))))
  
(define toStringV
  (lambda (val)
    (cond [(number? val) (number->string val)]
          [(list? val) (los2 (map toStringV val) "(" " " ")")]
          [(symbol? val) (symbol->string val)]
          [(boolean? val) (if val
                              "true"
                              "false")]
          [(box? val) (sa "(ref " (toStringV (unbox val)) ")")]
          [else (tostring val)]
          )))

(define eagerCheck
  (lambda (output input)
    (assert equal? output (tostringV ((make-eval-string input) 'eager)))))
(define lazyCheck
  (lambda (output input)
    (assert equal? output (tostringV ((make-eval-string input) 'lazy)))))
	
(define allCheck
  (lambda (output input)
    (begin
      (eagerCheck output input)
      (lazyCheck output input)
)))


(define assign5test
  (make-test-suite
   "assign5test"

   (make-test-case
    "interface: make-eval-string"
    (assert-true (procedure? make-eval-string)))

   (make-test-case
    "interface: make-eval-file"
    (assert-true (procedure? make-eval-file)))
   
   

        
   (make-test-case
       "mathOp" 
       (allCheck "30"
                    "2 * 3 + 12" )  )

        
   (make-test-case
       "parseException" (with-handlers ([exn:user? (lambda (x)
                           (if (or (regexp-match "(parse|Parse)" (exn-message x))
) #t (raise x))
                         )])
       (allCheck "haha"
                    " 1 +" ) ) )

        
   (make-test-case
       "append" 
       (allCheck "(1 2 3 1 2 3)"
                    "let append:  (list int, list int -> list int) :=       map x: list int, y: list int to         if x = null: int then y else cons(first(x), append(rest(x), y));     s: list int := cons(1,cons(2,cons(3,null: int))); in append(s,s)" )  )

        
   (make-test-case
       "null" 
       (allCheck "()"
                    "null: list (int, bool, list ref int -> unit)" )  )

        
   (make-test-case
       "emptyBlock" (with-handlers ([exn:user? (lambda (x)
                           (if (or (regexp-match "(parse|Parse)" (exn-message x))
) #t (raise x))
                         )])
       (allCheck "0"
                    "{ }" ) ) )

        
   (make-test-case
       "block" 
       (allCheck "1"
                    "{3; 2; 1}" )  )

        
   (make-test-case
       "dupVar" (with-handlers ([exn:user? (lambda (x)
                           (if (or (regexp-match "syntax" (exn-message x))
) #t (raise x))
                         )])
       (allCheck "ha!"
                    "let x: int :=3; x:int :=4; in x" ) ) )

        
   (make-test-case
       "refApp" 
       (allCheck "(ref 17)"
                    "let x: ref int := ref 10; in {x <- 17; x}" )  )

        
   (make-test-case
       "bangApp" 
       (allCheck "10"
                    "let x: ref int := ref 10; in !x" )  )

        
   (make-test-case
       "assign" (with-handlers ([exn:user? (lambda (x)
                           (if (or (regexp-match "type" (exn-message x))
) #t (raise x))
                         )])
       (allCheck "true"
                    "let x: int :=5; y: bool :=true; in x !=y" ) ) )

        
   (make-test-case
       "badAssign" (with-handlers ([exn:user? (lambda (x)
                           (if (or (regexp-match "type" (exn-message x))
) #t (raise x))
                         )])
       (allCheck "0"
                    "let x: int := 10; in x <- 5" ) ) )
   
   ))

(require (lib "text-ui.ss" "schemeunit"))
(test/text-ui assign5test)

