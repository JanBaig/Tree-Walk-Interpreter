# Tree-Walk Interpreter
#### Goals
- [x] Scanner
- [x] Print Abstract Syntax Tree (AST)
- [x] Parser
- [ ] Support more features

#### Evaluates:
- Expressions & Arithmetic
- Branching & Looping 
- Variables, Functions & Function calls 
- Parameter binding & Function return statements 
- Classes, Instance variables, & Methods
  
#### Example Program
```C
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

#### Great Learning Resources
- [Crafting Interpreters](https://craftinginterpreters.com/) by Robert Nystrom
- [Building Recursive Descent Parsers](https://www.booleanworld.com/building-recursive-descent-parsers-definitive-guide/#How_does_parsing_work) by Supriyo Biswas

<!-- ![](https://user-images.githubusercontent.com/76413679/178587724-7ec4de45-b3fc-4844-9b46-b153afd2353b.png) -->
