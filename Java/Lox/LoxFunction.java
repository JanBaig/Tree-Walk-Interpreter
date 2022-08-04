package Java.Lox;

import java.util.List;

class LoxFunction implements LoxCallable {
  private final Stmt.Function declaration;
  private final Environment closure;

  LoxFunction(Stmt.Function declaration, Environment closure) {
    this.closure = closure;
    this.declaration = declaration;
  } 

  LoxFunction bind(LoxInstance instance) {
    Environment environment = new Environment(closure);
    environment.define("this", instance);
    return new LoxFunction(declaration, environment);
  
  }

  @Override 
  public Object call(Interpreter interpreter, List<Object> arguments) {
    // Each function CALL gets its own env (new local scope) NOT each func declaration
    // interpreter.globals -> getting the global env to enclose the new function's scope
    // changing globals to CLOSURE now
    Environment environment = new Environment(closure);

    for (int i = 0; i < declaration.params.size(); i++) {
      // dec params is like 'a' and the arguments is the user's value for 'a' Eg 19
      environment.define(declaration.params.get(i).lexeme, arguments.get(i));
    } 

    try {
      interpreter.executeBlock(declaration.body, environment);
    } catch (Return returnValue) {
      return returnValue.value;
    }

    return null;
  } 

  @Override 
  public int arity() {
    return declaration.params.size();
  } 

  @Override 
  public String toString() {
    return "<fn " + declaration.name.lexeme + ">";
  }

}
