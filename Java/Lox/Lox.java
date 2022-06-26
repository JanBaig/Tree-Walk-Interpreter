package Java.Lox;

import java.io.BufferedReader; 
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Lox {

  public static void main(String[] args) throws IOException  {
    if (args.length > 1) {
      System.out.println("Usage: jlox [script]");
      // Exit code 64 - command used incorrectly/wrong parameters (to main) 
      System.exit(64);
    } else if (args.length == 1) {
      // Run the file for interpreting
      runFile(args[0]);
    } else {
      // No file given - run interatively evaluating one line at a time of user input
      runPrompt();
    }
  }

  private static void runFile(String path) throws IOException {
    // Read contents of file given and returns a byte array
    byte[] bytes = Files.readAllBytes(Paths.get(path));
    // 
    run(new String(bytes, Charset.defaultCharset()));
  }

  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);
    for (;;) {
      System.out.println("> ");
      String line = reader.readLine();
      if (line == null)
        break;
      run(line);

    }

  }
  
  // Need to define Tokens...
  private static void run(String source) {
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();

    for (Token token : tokens) {
      System.out.println(token);
    }
  }

    
}
