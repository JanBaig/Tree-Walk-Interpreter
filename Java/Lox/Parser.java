package Java.Lox;
import java.util.List;
import static Java.Lox.TokenType.*;

class Parser {
  // What does the ParseError even do? Why is it structured like this?
  private static class ParseError extends RuntimeException {};

  private final List<Token> tokens;
  private int current = 0;
  
  Parser(List<Token> tokens){
    this.tokens = tokens;
  }

  Expr parse() {
    try {
      return expression();
    } catch (ParseError error){
      return null;
    }
  }

  // Translate epxression rules to code

  private Expr expression() {
    return equality();
  }

  private Expr equality(){
    Expr expr = comparison();

    while(match(BANG_EQUAL, EQUAL_EQUAL)){
      // Remember the token labels are WORDS (not chars) so previous would equal (BANG_EQUAL) | (EQUAL_EQUAL)
      Token operator = previous();
      Expr right = comparison();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr comparison(){
    Expr expr = term();

    while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)){
      Token operator = previous();
      Expr right = term();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr term() {
    Expr expr = factor();

    while(match(MINUS, PLUS)){
      Token operator = previous();
      Expr right = factor();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr factor(){
    Expr expr = unary();

    while(match(SLASH, STAR)){
      Token operator = previous();
      Expr right = unary();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr unary(){
    if (match(BANG, MINUS)){
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    }

    return primary();
  }

  private Expr primary() {
    if (match(FALSE)) return new Expr.Literal(false);
    if (match(TRUE)) return new Expr.Literal(true);
    if (match(NIL)) return new Expr.Literal(null);

    if (match(NUMBER, STRING)){
      // We updated current -> so we're using previous() now to access the token
      return new Expr.Literal(previous().literal);
    }

    if (match(LEFT_PAREN)) {
      Expr expr = expression();
      // Check if the right param is found. If not, fire an error message
      consume(RIGHT_PAREN, "Expect ')' after expression.");
      return new Expr.Grouping(expr);
    }

    throw error(peek(), "Expect expression.");
  } 

  // Helper Methods

  private boolean match(TokenType... types){
    for (TokenType type : types){
      if (check(type)){
        advance();
        return true;
      }
    }
    return false;
  }

  private boolean check(TokenType type){
    if (isAtEnd()) return false;
    // peek() returns a token, so this is token.type (Check token class)
    return peek().type == type;
  }

  private Token advance(){
    if (!isAtEnd()) current++;
    // Returns the previous but increments current at the same time (Like Post increment)
    return previous();
  }

  private boolean isAtEnd(){
    return peek().type == EOF;
  }

  private Token peek(){
    return tokens.get(current);
  }

  private Token previous(){
    return tokens.get(current - 1); 
  }
 
  private Token consume(TokenType type, String message){
    if (check(type)) return advance();
    throw error(peek(), message);
  }

  private ParseError error(Token token, String message){
    // Reports an error at a given token
    Lox.error(token, message);
    return new ParseError();
  }

  // It discards tokens until it thinks it has found a statement boundary 
  // After catching a ParseError, we'll call this and then we will be back in sync 
  private void synchronize() {
    advance();

    while(!isAtEnd()){
      if (previous().type == SEMICOLON) return;

      switch (peek().type) {
        case CLASS:
        case FUN:
        case VAR:
        case FOR:
        case IF:
        case WHILE:
        case PRINT:
        case RETURN:
          return;
      }

      advance();

    }
  }
} 

