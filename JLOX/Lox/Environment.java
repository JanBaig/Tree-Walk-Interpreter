package JLOX.Lox;

import java.util.HashMap;
import java.util.Map;

class Environment {
  final Environment enclosing;
  private final Map<String, Object> values = new HashMap<String, Object>();
  
  Environment() {
    enclosing = null;
  }

  Environment(Environment enclosing) {
    this.enclosing = enclosing;
  }
  
  void define(String name, Object value) {
    // The value can also be a function
    values.put(name, value);
  }

  Object get(Token name) {
    // Checking if the variable exists and returns it's value
    if (values.containsKey(name.lexeme)) {
      return values.get(name.lexeme);
    } 

    if (enclosing != null) return enclosing.get(name);

    
    throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
  }

  void assign(Token name, Object value) {
    if (values.containsKey(name.lexeme)) {
      values.put(name.lexeme, value);
      return;
    }
    
    if (enclosing != null) {
      enclosing.assign(name, value);
      return;
    }

    throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
  } 

  Object getAt(int distance, String name) {
    return ancestor(distance).values.get(name);
  }  

  Environment ancestor(int distance) {
    Environment environment = this;
    for (int i=0; i<distance; i++) { 
      environment = environment.enclosing;
    }

    return environment;
  } 

  void assignAt(int distance, Token name, Object value) {
    ancestor(distance).values.put(name.lexeme, value);
  }

  
}
