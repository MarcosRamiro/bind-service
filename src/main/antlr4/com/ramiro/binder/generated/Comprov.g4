grammar Comprov;

programa: expressao;

expressao : 
           expressao op=( IGUAL | DIFERENTE ) expressao                                             #comparativo
 		  | expressao op=( MAIORIGUAL | MENORIGUAL | DIFERENTE | MAIOR | MENOR ) expressao          #relacional
          | teste=expressao '?' verdadeiro=expressao ':' falso=expressao                            #if
          | expressao AND expressao                                     	            			#andExpr
          | expressao OR expressao                                     				                #orExpr
          | expressao MAIS expressao                                     				            #concatenar
          | NUMERO                                                                       			#numero
          | 'json' '(' expressao ')'                                                     			#json
          | 'capitalize' '(' expressao ')'                                                 			#capitalize
          | 'uncapitalize' '(' expressao ')'                                                 		#uncapitalize
          | 'touppercase' '(' expressao ')'                                                 		#toUpperCase
          | 'cnpj' '(' expressao ')'                                                 		        #cnpj
          | 'date' '(' value=expressao ',' masc_e=expressao ',' masc_s=expressao ',' lang_country=expressao  ')'    #date
          | 'iscnpj' '(' expressao ')'                                                 		        #isCnpj
          | 'cpf' '(' expressao ')'                                                 		        #cpf
          | 'iscpf' '(' expressao ')'                                                 		        #isCpf
          | 'tolowercase' '(' expressao ')'                                                 		#toLowerCase
          | 'tonumber' '(' expressao ')'                                                 		    #toNumber
          | 'contains' '(' str=expressao ',' sequence=expressao ')'                                 #contains
          | 'formatcurrency' '(' value=expressao ',' lang_country=expressao ')'                     #formatCurrency
          | 'abbreviate' '(' str=expressao ',' lower=expressao ',' upper=expressao ')'              #abbreviate
          | 'initials' '(' expressao ')'                                              		        #initials
          | STRING                                                                        		    #string
          | NULL                                                                        		    #null
          | BOOLEANO                                                                        		#booleano
          | '[' expressao ']'                                                         				#colchetes
          ;
          

BOOLEANO: ('true' | 'false');
NULL: 'null';
STRING: '"' (ESC| .)*? '"';
NUMERO: (DIGITO+ | FLOAT);
IGUAL: '==' ;
MAIORIGUAL: '>=' ;
MENORIGUAL: '<=' ;
DIFERENTE: '!=' ;
MAIOR: '>' ;
MENOR: '<' ;
OR : '||';
AND : '&&';
MAIS: '+';

fragment FLOAT: DIGITO+ '.' DIGITO*;
fragment DIGITO: [0-9];
fragment ESC: '\\"'|'\\\\';

WS: [ \t\n\r]+ -> skip ;