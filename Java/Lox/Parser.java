package Java.Lox;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import static Java.Lox.TokenType.*;

class Parser {
  // Empty custom exception class
  private static class ParseError extends RuntimeException {};

  private final List<Token> tokens;
  private int current = 0;
  
  Parser(List<Token> tokens){
    this.tokens = tokens;
  }
  
  List<Stmt> parse() {
    List<Stmt> statements = new ArrayList<>();
    while (!isAtEnd()) {
      statements.add(declaration());
    }

    return statements;
  } 

  // Variable Declartion Rule

  private Stmt declaration() {
    try {
      if (match(FUN)) return function("function");
      if (match(VAR)) return varDeclaration();
      return statement();
    } catch(ParseError error) {
      synchronize();
      return null;
    }
  }

  private Stmt varDeclaration() {
    Token name = consume(IDENTIFIER, "Expect variable name.");

    // If the user wants to declare but NOT initialize, 'initializer' is set to null (perfectly fine)
    // But, if an equal sign is consumed, 'initializer' is initilzed to the user's expression.

    Expr initializer = null;
    if (match(EQUAL)) {
      // Recall that match() returns previous, but INCREMENTS the current. So the next token is used.
      initializer = expression();
    }
    consume(SEMICOLON, "Expect ')' after variable declaration.");
    return new Stmt.Var(name, initializer);
  }

  private Stmt.Function function(String kind) {
    // The user is DEFINING a function here

    Token name = consume(IDENTIFIER, "Expect " + kind + " name.");

    consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
    List<Token> parameters = new ArrayList<>();
    if (!check(RIGHT_PAREN)) {
      do {
        if (parameters.size() >= 255) {
          error(peek(), "Can't have more than 255 parameters.");
        }

        parameters.add(consume(IDENTIFIER, "Expect parameter name."));

      } while (match(COMMA));
    } 
    consume(RIGHT_PAREN, "Expect ')' after parameters.");

    consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
    List<Stmt> body = block();
    
    return new Stmt.Function(name, parameters, body);
  }

  // Implemneting Epxression Rules

  private Expr expression() {
    return assignment();
  }

  private Expr assignment() {
    Expr expr = or();
    
    if (match(EQUAL)) {
      Token equals = previous();
      Expr value = assignment();

      if (expr instanceof Expr.Variable) {
        Token name = ((Expr.Variable)expr).name; 
        return new Expr.Assign(name, value); 
      }

      error(equals, "Invalid assignmnent target.");
    }

    return expr;
  }

 private Expr or() {
    Expr expr = and();

    while (match(OR)) {
      Token operator = previous();
      Expr right = and();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }

  private Expr and() {
    Expr expr = equality();

    while (match(AND)) {
      Token operator = previous();
      Expr right = equality();
      expr = new Expr.Logical(expr, operator, right);
    } 
    
    return expr;
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

  private Expr unary() {
    if (match(BANG, MINUS)){
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    }

    // return primary();
    return call();
  }

  private Expr call() {
    // Most likely an Identifier if it is a function call
    Expr expr = primary();

    while (true) {
      if (match(LEFT_PAREN)) {
        // Passing the callee
        expr = finishCall(expr);
      } else {
        break;
      }
    } 

    return expr;
  }

  private Expr primary() {
    if (match(FALSE)) return new Expr.Literal(false);
    if (match(TRUE)) return new Expr.Literal(true);
    if (match(NIL)) return new Expr.Literal(null);

    // We updated current from match() -> so we're using previous() now to access the token
    if (match(NUMBER, STRING)) {
      return new Expr.Literal(previous().literal);
    }

    if (match(IDENTIFIER)) {
      return new Expr.Variable(previous());
    }

    if (match(LEFT_PAREN)) {
      Expr expr = expression();
      // Check if the right param is found. If not, fire an error message
      consume(RIGHT_PAREN, "Expect ')' after expression.");
      return new Expr.Grouping(expr);
    }

    throw error(peek(), "Expect expression.");
  } 

  // Implementing Statement Rules

  private Stmt statement() {
    if (match(FOR)) return forStatement();
    if (match(IF)) return ifStatement();
    if (match(PRINT)) return printStatement();
    if (match(WHILE)) return whileStatement();
    if (match(LEFT_BRACE)) return new Stmt.Block(block());

    return expressionStatement();
  }
  
  private Stmt printStatement() {
    Expr value = expression();
    consume(SEMICOLON, "Expect ';' after value.");
    // Emiting the syntax tree
    return new Stmt.Print(value);
  }

  private Stmt expressionStatement() {
    Expr expr = expression();
    consume(SEMICOLON, "Expect ';' after expression.");
    return new Stmt.Expression(expr); 
  }

  private List<Stmt> block() {
    List<Stmt> statements = new ArrayList<>(); 

    while (!check(RIGHT_BRACE) && !isAtEnd()) {
      statements.add(declaration());
    }

    consume(RIGHT_BRACE, "Expect '}' after block.");
    return statements;
  }

  private Stmt ifStatement() {
    consume(LEFT_PAREN, "Expect '(' after 'if'.");
    Expr condition = expression();
    consume(RIGHT_PAREN, "Expect ')' after if condition.");

    // Statement() will take care of the 'Block' brackets
    Stmt thenBranch = statement();
    Stmt elseBranch = null;
    if (match(ELSE)) {
      elseBranch = statement();
    }

    return new Stmt.If(condition, thenBranch, elseBranch);
  }

  private Stmt whileStatement() {
    consume(LEFT_PAREN, "Expect '(' after 'while'.");
    Expr condition = expression();
    consume(RIGHT_PAREN, "Expect ')' after condition.");
    
    // Gets the brackets for the body + evaluates the other body statements
    Stmt body = statement();

    return new Stmt.While(condition, body);
  }

  private Stmt forStatement() {
    consume(LEFT_PAREN, "Expect '(' after 'for'.");

    Stmt initializer;
    if (match(SEMICOLON)) {
      initializer = null;
    } else if (match(VAR)) {
      initializer = varDeclaration();
    } else {
      initializer = expressionStatement();
    } 

    Expr condition = null;
    if (!check(SEMICOLON)) {
      condition = expression();
    } 
    consume(SEMICOLON, "Expect ';' after loop condition.");

    Expr increment = null;
    if (!check(RIGHT_PAREN)) {
      increment = expression();
    } 
    consume(RIGHT_PAREN, "Expect ')' after for clauses.");

    // Contains a block statement
    Stmt body = statement(); 

    //----------

    if (increment != null) {
      // Converting an array -> List
      body = new Stmt.Block(
        Arrays.asList(
          body, 
          new Stmt.Expression(increment)));
    } 

    // Infinite loop if no condition is given
    if (condition == null) condition = new Expr.Literal(true);

    body = new Stmt.While(condition, body);

    if (initializer != null) {
      body = new Stmt.Block(Arrays.asList(initializer, body));
    }

    return body;

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

  private void synchronize() {
    // It discards tokens until it thinks it has found a statement boundary 
    // After catching a ParseError, we'll call this and then we will be back in sync 

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

  private Expr finishCall(Expr callee) {
    List<Expr> arguments = new ArrayList<>();

    if (!check(RIGHT_PAREN)) {
      do {
        if (arguments.size() >= 255) {
          error(peek(), "Can't have more than 255 arguments.");
        }
        arguments.add(expression());

      } while (match(COMMA));
    } 
    
    Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");

    return new Expr.Call(callee, paren, arguments);

  }

} 

