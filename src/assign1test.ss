(require (lib "test.ss" "schemeunit"))
(require (file "lexer.ss"))
(load "parser.ss")



(define checkString 
  (lambda (output input)
    (assert equal? output (tostring (parse-string input)))))

;not sure what to do about this one yet
(define checkFile 
  (lambda (output input)
    (assert equal? output (deconvert (parse-file input)))))


(define assign1test
  (make-test-suite
   "assign1test"
   
   
   (make-test-case
    "interface: parse-string"
    (assert-true (procedure? parse-string)))
   
   (make-test-case
    "interface: parse-file"
    (assert-true (procedure? parse-file)))
   
   

        
   (make-test-case
       "add" 
       (checkString "(2 + 3)"
                    "2+3" )  )

        
   (make-test-case
       "prim  " 
       (checkString "first"
                    "first" )  )

        
   (make-test-case
       "parseException" (with-handlers ([exn:user? (lambda (x)
                           (if (or (regexp-match "(parse|Parse)" (exn-message x))
) #t (raise x))
                         )])
       (checkString "doh!"
                    "map a, to 3" ) ) )

        
   (make-test-case
       "let" 
       (checkString "let a := 3; in (a + a)"
                    "let a:=3; in a + a" )  )

        
   (make-test-case
       "map" 
       (checkString "map f to (map x to f(x(x)))(map x to f(x(x)))"
                    "map f to (map x to f( x( x ) ) ) (map x to f(x(x)))" )  )
   
   ))

(require (lib "text-ui.ss" "schemeunit"))
(test/text-ui assign1test)