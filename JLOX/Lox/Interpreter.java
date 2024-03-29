package JLOX.Lox;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
  // The class declares that it's a visitor

  // Fixed reference to outermost global env
  final Environment globals = new Environment();
  // This env changes as we enter/exit local scopes
  private Environment environment = globals; 
  private final Map<Expr, Integer> locals = new HashMap<>();
  
  // Constructor Method
  Interpreter() {

    globals.define("clock", new LoxCallable() {

      @Override 
      // The clock function takes no arguments, so arity is  0
      public int arity() { return 0; }

      @Override 
      public Object call(Interpreter interpreter, List<Object> arguments) {
        return (double)System.currentTimeMillis() / 1000.0;
      } 

      @Override 
      public String toString() { return "<native fn>"; }
    });

  }

  void interpret(List<Stmt> statements) {
    try {
      for (Stmt statement : statements) {
        execute(statement);
      }
    } catch (RuntimeError error) {
      Lox.runtimeError(error);
    }
  }  

  @Override 
  public Object visitSuperExpr(Expr.Super expr) {
    int distance = locals.get(expr);
    LoxClass superclass = (LoxClass)environment.getAt(distance, "super"); 

    // Currently in the super class and move back in distance to get 'this' in the child class?
    LoxInstance object = (LoxInstance)environment.getAt(distance - 1,"this"); 

    LoxFunction method = superclass.findMethod(expr.method.lexeme);

    if (method == null) {
      throw new RuntimeError(expr.method, "Undefined property '" + expr.method.lexeme + "'.");
    }

    // Binding 'this'
    return method.bind(object);

  }

  @Override 
  public Object visitThisExpr(Expr.This expr) {
    return lookUpVariable(expr.keyword, expr);
  }

  @Override 
  public Object visitSetExpr(Expr.Set expr) {
    Object object = evaluate(expr.object);

    if (!(object instanceof LoxInstance)) {
      throw new RuntimeError(expr.name,  "Only instances have fields.");
    } 

    Object value = evaluate(expr.value);
    ((LoxInstance)object).set(expr.name, value);
    return value;

  }

  @Override 
  public Object visitGetExpr(Expr.Get expr) {
    Object object = evaluate(expr.object);
    if (object instanceof LoxInstance) {
      return ((LoxInstance)object).get(expr.name);
    } 

    throw new RuntimeError(expr.name, "Only instances have properties.");
  }

  @Override 
  public Void visitClassStmt(Stmt.Class stmt) {
    Object superclass = null;
    if (stmt.superclass != null) {
      superclass = evaluate(stmt.superclass);
      if (!(superclass instanceof LoxClass)) {
        throw new RuntimeError(stmt.superclass.name, "Superclass must be a class.");
      }
    }

    environment.define(stmt.name.lexeme, null); 

    if (stmt.superclass != null) {
      environment = new Environment(environment);
      environment.define("super", superclass);

    }

    Map<String, LoxFunction> methods = new HashMap<>();
    for (Stmt.Function method : stmt.methods) {
      LoxFunction function = new LoxFunction(method, environment, method.name.lexeme.equals("init"));
      methods.put(method.name.lexeme, function);
    }

    LoxClass klass = new LoxClass(stmt.name.lexeme, (LoxClass)superclass, methods); 

    if (superclass != null) {
      environment = environment.enclosing;
    }
     
    environment.assign(stmt.name, klass);
    return null;

  }

  @Override 
  public Void visitReturnStmt(Stmt.Return stmt) {
    Object value = null;
    if (stmt.value != null) value = evaluate(stmt.value);

    throw new Return(value);
  }

  @Override 
  public Void visitFunctionStmt(Stmt.Function stmt) {
    // Stmt.Function is a syntax node (Compile time) and LoxFunction is the runtime rep of that function
    LoxFunction function =  new LoxFunction(stmt, environment, false);
    environment.define(stmt.name.lexeme, function);
    return null;
  }

  @Override 
  public Object visitCallExpr(Expr.Call expr) {
    // Expecting an identifier
    Object callee = evaluate(expr.callee);

    List<Object> arguments = new ArrayList<>();
    for (Expr argument : expr.arguments) {
      arguments.add(evaluate(argument));
    }

    // Callee is an instance of LoxFunction (Therefore also instance of the interface that LoxFunction implements)
    if (!(callee instanceof LoxCallable)) {
      throw new RuntimeError(expr.paren, "Can only call functions and classes.");
    } 

    // The actual declared function 
    LoxCallable function = (LoxCallable)callee;

    // Checking if the argument list len matches the callable's arity
    if (arguments.size() != function.arity()) {
      throw new RuntimeError(expr.paren, "Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
    }

    return function.call(this, arguments);
  }

  @Override
  public Void visitWhileStmt(Stmt.While stmt) {
    while (isTruthy(evaluate(stmt.condition))) {
      // for FOR loops, the body contains the incrementer and the statements
      execute(stmt.body);
    }

    return null;
  }

  @Override 
  public Void visitIfStmt(Stmt.If stmt) {
    if (isTruthy(evaluate(stmt.condition))) {
      execute(stmt.thenBranch);
    } else if (stmt.elseBranch != null) {
      execute(stmt.elseBranch);
    } 

    return null;
  }
  
  @Override
  public Object visitLogicalExpr(Expr.Logical expr) {
    Object left = evaluate(expr.left);

    if (expr.operator.type == TokenType.OR) {
      if (isTruthy(left)) return left;
    } else {
      // For an AND expression
      if (!isTruthy(left)) return left;
    } 

    return evaluate(expr.right);
  }
  
  @Override
  public Void visitBlockStmt(Stmt.Block stmt) {
    executeBlock(stmt.statements, new Environment(environment));
    return null;
  }

  @Override
  public Object visitAssignExpr(Expr.Assign expr) {
    Object value = evaluate(expr.value);
    //environment.assign(expr.name, value);

    Integer distance = locals.get(expr);
    if (distance != null) {
      environment.assignAt(distance, expr.name, value);
    } else {
      globals.assign(expr.name, value);
    }

    return value;
  }

  @Override 
  public Void visitVarStmt(Stmt.Var stmt) {
    Object value = null;
    if (stmt.initializer != null) {
      value = evaluate(stmt.initializer); 
    }

    environment.define(stmt.name.lexeme, value);
    return null;
  } 

  @Override 
  public Object visitVariableExpr(Expr.Variable expr) {
    // return environment.get(expr.name);
    return lookUpVariable(expr.name, expr);
  }

  @Override
  public Object visitLiteralExpr(Expr.Literal expr) {
    return expr.value;
  } 

  @Override
  public Object visitGroupingExpr(Expr.Grouping expr) {
    return evaluate(expr.expression);
  } 

  @Override
  public Object visitUnaryExpr(Expr.Unary expr) {
    Object right = evaluate(expr.right);

    switch (expr.operator.type) {
      case BANG: 
        return !isTruthy(right);
      case MINUS: 
        checkNumberOperand(expr.operator, right);
        return -(double)right;
    }

    return null;
  }

  @Override
  public Object visitBinaryExpr(Expr.Binary expr) {
    Object left = evaluate(expr.left);
    Object right = evaluate(expr.right);

    // Evlauating in a left-right fashion
    switch (expr.operator.type) {
      case BANG_EQUAL: 
        return !isEqual(left, right);
      case EQUAL_EQUAL: 
        return isEqual(left, right);
      case GREATER: 
        checkNumberOperands(expr.operator, left, right);
        return (double)left > (double)right;
      case GREATER_EQUAL: 
        checkNumberOperands(expr.operator, left, right);
        return (double)left >= (double)right;
      case LESS: 
        checkNumberOperands(expr.operator, left, right);
        return (double)left < (double)right;
      case LESS_EQUAL: 
        checkNumberOperands(expr.operator, left, right);
        return (double)left <= (double)right;
      case MINUS: 
        checkNumberOperands(expr.operator, left, right);
        return (double)left - (double)right;
      case PLUS: 
        if (left instanceof Double && right instanceof Double) {
          return (double)left + (double)right;
        }
        // Concatanating strings
        if (left instanceof String && right instanceof String) {
          return (String)left + (String)right;
        }
        throw new RuntimeError(expr.operator, "Operants must be two numbers or two strings.");
      case SLASH: 
        checkNumberOperands(expr.operator, left, right);
        return (double)left / (double)right;
      case STAR: 
        checkNumberOperands(expr.operator, left, right);
        return (double)left * (double)right;
    } 

    return null;

  } 

  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt) {
    evaluate(stmt.expression);
    return null;
  } 

  @Override
  public Void visitPrintStmt(Stmt.Print stmt) {
    Object value = evaluate(stmt.expression);
    System.out.println(stringify(value));
    return null;
  }

  // Helper Methods

  private Object evaluate(Expr expr) {
    return expr.accept(this);
  } 

  private boolean isTruthy(Object object) {
    // Basically, for non-booleans -> everything that isn't null is TRUE
    if (object == null) return false;
    if (object instanceof Boolean) return (boolean)object;
    return true;
  } 

  private boolean isEqual(Object a, Object b) {
    if (a == null && b == null) return true;
    if (a == null) return false;

    return a.equals(b);

  }

  private void checkNumberOperand(Token operator, Object operand) {
    if (operand instanceof Double) return;
    throw new RuntimeError(operator, "Operand must be a number.");
  }

  private void checkNumberOperands(Token operator, Object left, Object right) {
    if (left instanceof Double && right instanceof Double) return;
    throw new RuntimeError(operator, "Operands must be numbers.");
  }

  private String stringify(Object object) {
    if (object == null) return "nil";
    
    if (object instanceof Double) {
      String text = object.toString();
      if (text.endsWith(".0")) {
        text = text.substring(0, text.length() - 2);
      }
      return text;
    }

    return object.toString();

  }

  private void execute(Stmt stmt) {
    stmt.accept(this);
  }

  void executeBlock(List<Stmt> statements, Environment environment) {
    Environment previous = this.environment;
    // Delete this later on
    Map<Expr, Integer> NEWLOCALS = locals;

    try {
      // The current env is set to the NEW env made for the block
      this.environment = environment;
      
      for (Stmt statement : statements) {
        execute(statement);
      }
    } finally {
      // Since we're done with the block statements, we set the env 
      // back to the global one
      this.environment = previous;
    }

  }

  void resolve(Expr expr, int depth) {
    locals.put(expr, depth);
  }

  private Object lookUpVariable(Token name, Expr expr) {
    Integer distance = locals.get(expr);
    if (distance != null) {
      return environment.getAt(distance, name.lexeme);
    } else {
      return globals.get(name);
    }
  } 

}
