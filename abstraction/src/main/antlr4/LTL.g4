grammar LTL;

ltlExpr : ('!' | 'not') child=ltlExpr                                  # Negation
     //temporal operators
     | ('next' | 'X') child=ltlExpr                                    # Next
     | ('eventually' | 'F') child=ltlExpr                              # Eventually
     | ('always' | 'G') child=ltlExpr                                  # Always
     | left=ltlExpr ('until' | 'U') right=ltlExpr                      # Until
     // boolean operators
     | left=ltlExpr ('&&' | 'and') right=ltlExpr                       # Conjunction
     | left=ltlExpr ('||' | 'or') right=ltlExpr                        # Disjunction
     | left=ltlExpr ('->' | 'implies') right=ltlExpr                   # Implies
     | '(' ltlExpr ')'                                                 # Grouping
     | child=atomExpr                                                  # Evaluation
     ;

atomExpr : ATOM;

ATOM : [_a-zA-Z][_a-zA-Z0-9]*;

WS        : [ \t\r\n\u000C]+ -> skip;

