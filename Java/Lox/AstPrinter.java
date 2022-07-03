package Java.Lox;

// Operation class that implements the visitor interface
class AstPrinter implements Expr.Visitor<String> {
  
  // Calling the accept method of the subclass and passing in this operation 'AstPrinter'. 
  // By polymorphsm, the parent class (Expr) triggers the accept call to the subclass's accept() method
  // The accept() method called from within the subclass then routes to the correct visitior interface's methods
  // My understanding so far

  String print(Expr expr) {
    return expr.accept(this);
  }

  @Override
  public String visitBinaryExpr(Expr.Binary expr) {
    // 'expr' is the binary class instance
    return parenthesize(expr.operator.lexeme, expr.left, expr.right);
  }

  @Override
  public String visitGroupingExpr(Expr.Grouping expr) {
    return parenthesize("group", expr.expression);
  }

  @Override
  public String visitLiteralExpr(Expr.Literal expr) {
    if (expr.value == null)
      return "nil";
    return expr.value.toString();
  }

  @Override
  public String visitUnaryExpr(Expr.Unary expr) {
    return parenthesize(expr.operator.lexeme, expr.right);
  }

  // Unlike literals, the other expressions have subexpressions, so they use this parenthesize() helper method
  private String parenthesize(String name, Expr... exprs) {
    // 'Expr... exprs' - different # of variables can be accepted here

    // A mutable sequence of characters
    StringBuilder builder = new StringBuilder();

    builder.append("(").append(name);
    for (Expr expr : exprs) {
      builder.append(" ");
      // Why did we add this?
      builder.append(expr.accept(this));
    }

    builder.append(")");
    return builder.toString();

  }

  public static void main(String[] args) {

    Expr expression = new Expr.Binary(
      new Expr.Unary(
        new Token(TokenType.MINUS, "-", null, 1),
        new Expr.Literal(123)),

      new Token(TokenType.STAR, "*", null, 1),
      
      new Expr.Grouping(
        new Expr.Literal(45.67))
    );

    String returnString = new AstPrinter().print(expression);
    
    System.out.println(returnString);
    // Expected Result: (* (- 123) (group 45.67))

  }



}
