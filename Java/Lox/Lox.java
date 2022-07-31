package Java.Lox;

import java.io.BufferedReader; 
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
  private static final Interpreter interpreter = new Interpreter();
  static boolean hadError = false;
  static boolean hadRuntimeError = false;

  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.out.println("Usage: jlox [script]");
      // Exit code 64 - command used incorrectly/wrong parameters (to main) 
      System.exit(64);
    } else if (args.length == 1) {
      // Run the file for interpreting
      runFile(args[0]);
    } else {
      // Run interactively evaluating one line at a time of user input
      runPrompt();
    }
  }

  private static void runFile(String path) throws IOException {

    // Contents of file read into a byte array
    byte[] bytes = Files.readAllBytes(Paths.get(path));

    // Byte array -> decode to a string (encode was string -> byte)
    run(new String(bytes, Charset.defaultCharset()));
    if (hadError) System.exit(65);
    if (hadRuntimeError) System.exit(70);
  }

  private static void runPrompt() throws IOException {

    // reads byte and decodes to characters
    InputStreamReader input = new InputStreamReader(System.in);

    // Bufferings the chars
    BufferedReader reader = new BufferedReader(input);

    // Unless terminated, this is an infinite loop
    for (;;) {
      System.out.print("> ");
      String line = reader.readLine();
      if (line == null) break;
      run(line);
      hadError = false;
    }
  }

  private static void run(String source) {
    
    // Our custom scanner
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();

    Parser parser = new Parser(tokens);
    
    // Returns syntax tree nodes
    List<Stmt> statements = parser.parse();

    // Stop if there is a syntax error
    if (hadError) return;

    interpreter.interpret(statements);

  }

  static void error(int line, String message) {
    report(line, "", message);
  }

  private static void report(int line, String where, String message) {
    System.err.println("[line " + line + "] Error" + where + ": " + message);
    hadError = true;
  }
    
  static void error(Token token, String message){
    if (token.type == TokenType.EOF){
      report(token.line, " at end", message);
    } else {
      report(token.line, " at '" + token.lexeme + "'", message);
    }
  }

  static void runtimeError(RuntimeError error) {
    System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
    hadRuntimeError = true;
  }

}
