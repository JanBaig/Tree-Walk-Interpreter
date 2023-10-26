# Tree-Walk Interpreter
#### Goals
- [x] Scanner
- [x] Print Abstract Syntax Tree (AST)
- [x] Parser
- [x] Variable Resolution Pass

#### Evaluates:
- Expressions & Arithmetic
- Branching & Looping 
- Variables, Functions & Function calls 
- Parameter binding & Function return statements 
- Classes, Inheritance, Instance variables, & Methods

#### Future Features 
- [ ] Ability to evaluate string interpolation
- [ ] Add support for the C-style conditional or “ternary” operator ``?:``.

#### Example Program
```rust
class Cake {
  taste() {
    var adjective = "delicious";
    print "The " + this.flavor + " cake is " + adjective + "!";
  }
}

var cake = Cake();
cake.flavor = "German chocolate";
cake.taste(); // Prints "The German chocolate cake is delicious!".
```

#### BNF Grammer
```css
program        → declaration* EOF ;

declaration    → classDecl
               | funDecl
               | varDecl
               | statement ;

classDecl      → "class" IDENTIFIER ( "<" IDENTIFIER )? "{" function* "}" ;

funDecl        → "fun" function ;
function       → IDENTIFIER "(" parameters? ")" block ;
parameters     → IDENTIFIER ( "," IDENTIFIER )* ;

varDecl        → "var" IDENTIFIER ( "=" expression )? ";" ;

statement      → exprStmt
               | forStmt
               | ifStmt
               | printStmt
               | returnStmt
               | whileStmt
               | block ;

block          → "{" declaration* "}" ;

ifStmt         → "if" "(" expression ")" statement
               ( "else" statement )? ;

whileStmt      → "while" "(" expression ")" statement ;

forStmt        → "for" "(" ( varDecl | exprStmt | ";" )
                 expression? ";"
                 expression? ")" statement ;

exprStmt       → expression ";" ;
printStmt      → "print" expression ";" ;
returnStmt     → "return" expression? ";" ;

expression     → assignment ;
assignment     → ( call ".")? IDENTIFIER "=" assignment
               | logic_or ;

logic_or       → logic_and ( "or" logic_and )* ;
logic_and      → equality ( "and" equality )* ;
equality       → comparison ( ( "!=" | "==" ) comparison )* ;
comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term           → factor ( ( "-" | "+" ) factor )* ;
factor         → unary ( ( "/" | "*" ) unary )* ;
unary          → ( "!" | "-" ) unary | call ;
call           → primary ( "(" arguments? ") | "." IDENTIFIER )* ;
arguments      → expression ( "," expression )* ;

primary        → "true" | "false" | "nil"
               | NUMBER | STRING | IDENTIFIER
               | "(" expression ")"
               | "super" "." IDENTIFIER ;
```

#### Great Learning Resources
- [Crafting Interpreters](https://craftinginterpreters.com/) by Robert Nystrom
- [Building Recursive Descent Parsers](https://www.booleanworld.com/building-recursive-descent-parsers-definitive-guide/#How_does_parsing_work) by Supriyo Biswas

<!-- ![](https://user-images.githubusercontent.com/76413679/178587724-7ec4de45-b3fc-4844-9b46-b153afd2353b.png) -->
