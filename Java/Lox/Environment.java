package Java.Lox;

import java.util.HashMap;
import java.util.Map;

class Environment {
  private final Map<String, Object> values = new HashMap<String, Object>();
  
  void define(String name, Object value) {
    values.put(name, value);
  }

  // Checking if the variable exists and returns it's value
  Object get(Token name) {
    if (values.containsKey(name.lexeme)) {
      return values.get(name.lexeme);
    } 

    throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
  }
}
