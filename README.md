# Interpreter
### Goals - *A work in progress*
- [x] Scanner
- [x] Print Abstract Syntax Tree (AST)
- [ ] Parser
- [ ] Add descriptive errors messages

```
> 2 + 56
(+ 2.0 56.0)
> 2 +
[line 1] Error at end: Expect expression.
> (3 * 4) * 10
(* (group (* 3.0 4.0)) 10.0)
```
*Correctly prints the Abstract Syntax Tree (AST) and handles errors*

### Challenges
- Understanding the workings of the parser
  - Eg. operator precedence, grasping recursive decent parsing etc 
- Understanding and implementing the visitor pattern

### Great Learning Resources
- [Crafting Interpreters](https://craftinginterpreters.com/) by Robert Nystrom
- [Building Recursive Descent Parsers](https://www.booleanworld.com/building-recursive-descent-parsers-definitive-guide/#How_does_parsing_work) by Supriyo Biswas

<!-- ![](https://user-images.githubusercontent.com/76413679/178587724-7ec4de45-b3fc-4844-9b46-b153afd2353b.png) -->
