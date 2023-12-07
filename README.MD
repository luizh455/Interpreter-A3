Interpreter in Java

This is a simple interpreter written in Java that performs lexical analysis (tokenization), syntactic and semantic analysis, and expression interpretation. It can handle basic arithmetic operations and register operations.

How to Use

Clone the Repository:

bash

Copy code

git clone https://github.com/luizh455/interpreter.git

cd interpreter

Run the Interpreter:


bash

Copy code

javac Interpretador.java

java Interpretador

Providing Input:


The interpreter will list the files in the specified directory.

It will automatically select a file with a .txt extension for interpretation.

The program will then tokenize and interpret the expressions in the file.

Features

Tokenization:


Tokens are generated for numbers, operators (+, -, *), parentheses, and registers (e.g., r1).
Syntactic and Semantic Analysis:


Checks the correctness of expressions, ensuring they follow valid syntax and semantics.
Identifies and handles brackets appropriately.

Expression Interpretation:


Performs basic arithmetic operations.

Supports ordered operations (e.g., 2 * (3 + 22)).

File Structure

Interpretador.java: Main Java file containing the interpreter code.

Token.java: Represents a token in the interpreter.

Arquivo.java: Represents a file containing lines of code.

OperationRegister.java: Represents an operation with a name and a list of tokens.

OrderOperationsTuple.java: Represents a tuple containing a list of tokens and a list of operation registers.

Example

java

Copy code

// Sample expression in the input file

2 * (3 + 22)

License


This interpreter is licensed under the MIT License.


Feel free to use and modify the code according to your needs. If you encounter any issues or have suggestions for improvements, feel free to open an issue or submit a pull request.