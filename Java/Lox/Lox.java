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
  static boolean hadError = false;

  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.out.println("Usage: jlox [script]");
      // Exit code 64 - command used incorrectly/wrong parameters (to main) 
      System.exit(64);
    } else if (args.length == 1) {
      // Run the file for interpreting
      runFile(args[0]);
    } else {
      // Run interatively evaluating one line at a time of user input
      runPrompt();
    }
  }

  private static void runFile(String path) throws IOException {

    // Contents of file read into a byte array
    byte[] bytes = Files.readAllBytes(Paths.get(path));

    // Byte array ->  decode to a string (encode was string -> byte)
    run(new String(bytes, Charset.defaultCharset()));
    if (hadError) System.exit(65);
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

    for (Token token : tokens) {
      // Apprently .toString() does not work...
      System.out.println(token);
    }
  }

  static void error(int line, String message) {
    report(line, "", message);
  }

  private static void report(int line, String where, String message) {
    System.err.println("[line " + line + "] Error" + where + ": " + message);
    hadError = true;
  }
    
}
