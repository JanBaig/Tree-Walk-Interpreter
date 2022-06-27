package Practice;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.zip.InflaterInputStream;

public class Practice {
    
  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.out.println("Usage: jlox [script]");
      System.exit(64);
    } else if (args.length == 1) {
      runFile(args[0]);
    } else {
      runPrompt();
    }
  }


  // Static methods - call without creating object of class
  private static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path));

    //Consructs a new String by decoding the specified array of bytes using the specified charset.
    String newString = new String(bytes, Charset.defaultCharset());
    System.out.println(newString);
    // run func goes here

  }
  
  // want to ask for user input string and then convert that to tokens
  private static void runPrompt() throws IOException {
    // reads byte and decodes to characters
    InputStreamReader input = new InputStreamReader(System.in);
    // Bufferings the chars
    BufferedReader reader = new BufferedReader(input);

    // Unless terminated, this is a n infinite loop
    for (;;) {
      System.out.print("> ");
      String line = reader.readLine();
      if (line == null) {
        break;
      }
      else System.out.printf("This is the line: %s %n", line);
    }

  }
    

}
