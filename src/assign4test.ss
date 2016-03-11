(require (lib "test.ss" "schemeunit"))
(load "eval.ss")


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

(define valueValueCheck
  (lambda (output input)
    (assert equal? output (tostringV ((make-eval-string input) 'value 'value)))))
(define valueNameCheck
  (lambda (output input)
    (assert equal? output (tostringV ((make-eval-string input) 'value 'name)))))
(define valueNeedCheck
  (lambda (output input)
    (assert equal? output (tostringV ((make-eval-string input) 'value 'need)))))

(define nameValueCheck
  (lambda (output input)
    (assert equal? output (tostringV ((make-eval-string input) 'name 'value)))))
(define nameNameCheck
  (lambda (output input)
    (assert equal? output (tostringV ((make-eval-string input) 'name 'name)))))
(define nameNeedCheck
  (lambda (output input)
    (assert equal? output (tostringV ((make-eval-string input) 'name 'need)))))

(define needValueCheck
  (lambda (output input)
    (assert equal? output (tostringV ((make-eval-string input) 'need 'value)))))
(define needNameCheck
  (lambda (output input)
    (assert equal? output (tostringV ((make-eval-string input) 'need 'name)))))
(define needNeedCheck
  (lambda (output input)
    (assert equal? output (tostringV ((make-eval-string input) 'need 'need)))))
	
(define allCheck
  (lambda (output input)
    (begin
      (valueValueCheck output input)
      (valueNameCheck output input)
      (valueNeedCheck output input)
      (nameValueCheck output input)
      (nameNameCheck output input)
      (nameNeedCheck output input)
      (needValueCheck output input)
      (needNameCheck output input)
      (needNeedCheck output input)
)))

(define noNameCheck
  (lambda (output input)
    (begin
      (valueValueCheck output input)
      (valueNameCheck output input)
      (valueNeedCheck output input)
      (needValueCheck output input)
      (needNameCheck output input)
      (needNeedCheck output input)
)))

(define lazyCheck
  (lambda (output input)
    (begin
      (valueNameCheck output input)
      (valueNeedCheck output input)
      (nameNameCheck output input)
      (nameNeedCheck output input)
      (needNameCheck output input)
      (needNeedCheck output input)
)))
(define needCheck
  (lambda (output input)
    (begin
      (needValueCheck output input)
      (needNeedCheck output input)
)))


(define assign4test
  (make-test-suite
   "assign4test"

   (make-test-case
    "interface: make-eval-string"
    (assert-true (procedure? make-eval-string)))

   (make-test-case
    "interface: make-eval-file"
    (assert-true (procedure? make-eval-file)))
   
   

        
   (make-test-case
       "numberP" 
       (allCheck "number?"
                    "number?" )  )

        
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
       "evalException" (with-handlers ([exn:user? (lambda (x)
                           (if (or (regexp-match "eval" (exn-message x))
) #t (raise x))
                         )])
       (allCheck "mojo"
                    "1 + number?" ) ) )

        
   (make-test-case
       "append" 
       (allCheck "(1 2 3 1 2 3)"
                    "let Y    := map f to              let g := map x to f(map z1,z2 to (x(x))(z1,z2));     in g(g);  APPEND := map ap to            map x,y to               if x = null then y else cons(first(x), ap(rest(x), y)); l      := cons(1,cons(2,cons(3,null))); in (Y(APPEND))(l,l)" )  )

        
   (make-test-case
       "letRec" 
       (allCheck "(1 2 3 1 2 3)"
                    "let append :=       map x,y to          if x = null then y else cons(first(x), append(rest(x), y));    l      := cons(1,cons(2,cons(3,null))); in append(l,l)" )  )

        
   (make-test-case
       "lazyCons" 
       (lazyCheck "0"
                    "let zeroes := cons(0,zeroes);in first(rest(zeroes))" )  )

        
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
                    "let x:=3; x:=4; in x" ) ) )

        
   (make-test-case
       "refApp" 
       (noNameCheck "(ref 17)"
                    "let x := ref 10; in {x <- 17; x}" )  )

        
   (make-test-case
       "refref" 
       (allCheck "(ref (ref 4))"
                    "let x:= ref 4; y:= ref x; in y" )  )

        
   (make-test-case
       "bangApp" 
       (allCheck "10"
                    "let x := ref 10; in !x" )  )
   
   ))

(require (lib "text-ui.ss" "schemeunit"))
(test/text-ui assign4test)

