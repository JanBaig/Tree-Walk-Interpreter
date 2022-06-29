package Java.Lox;

class Token {
    // The 'final' keyword in the contex of variables are used for declaring constants

    final TokenType type;
    final String lexeme;
    final Object literal; // something like {Var-type : "value"}
    final int line;

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }

}
