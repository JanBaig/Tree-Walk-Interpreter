package Java.Lox;
import java.util.List; 

abstract class Expr {

  // The visitor Interface where <R> is a string
  interface Visitor<R> {
    R visitBinaryExpr(Binary expr);

    R visitGroupingExpr(Grouping expr);

    R visitLiteralExpr(Literal expr);

    R visitUnaryExpr(Unary expr);
  }

  static class Binary extends Expr {
    Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      // 'visitor' is the interface 
      System.out.println("From inside the Expr.Binary class!");
      return visitor.visitBinaryExpr(this);
    }

    final Expr left;
    final Token operator;
    final Expr right;
  }

  static class Grouping extends Expr {
    Grouping(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      System.out.println("From inside the Expr.Grouping class!");
      return visitor.visitGroupingExpr(this);
    }

    final Expr expression;
  }

  static class Literal extends Expr {
    Literal(Object value) {
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      System.out.println("From inside the Expr.Literal class!");
      return visitor.visitLiteralExpr(this);
    }

    final Object value;
  }

  static class Unary extends Expr {
    Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      System.out.println("From inside the Expr.Unary class!");
      return visitor.visitUnaryExpr(this);
    }

    final Token operator;
    final Expr right;
  }

  // Given some pastry, how do we route it to the correct method on the visitor based on its type? Polymorphism
  // Expr.accept([pass in the specific visitor]) -> subclass's accept() method is invoked -> redirects to interface's specific method
  // The specific visitor we pass in has expession TYPES that automatically trigger the spcific type class's accept() method
  abstract <R> R accept(Visitor<R> visitor);

}