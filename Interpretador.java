import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class Token {
    TokenType type;
    String text;
    int pos;
    int line;

    public Token(String token, int line, int pos) {
        this.text = token;
        this.pos = pos;
        this.type = getTokenType(token);
        this.line = line;
    }

    public TokenType getTokenType(String value) {

        if (isDigit(value)) {
            return TokenType.Number;
        } else if (isPlus(value)) {
            return TokenType.Plus;
        } else if (isMinus(value)) {
            return TokenType.Minus;
        } else if (isTimes(value)) {
            return TokenType.Times;
        } else if (isBracket(value)) {
            return TokenType.Bracket;
        } else if (isRegister(value)) {
            return TokenType.Register;
        }
        return TokenType.Undefined;
    }

    public boolean isRegister(String strNum) {
        if (strNum.contains("r")) {
            return true;
        }
        return false;
    }

    public boolean isBracket(String strNum) {
        if (strNum.equals(")") || strNum.equals("(")) {
            return true;
        }
        return false;
    }

    public boolean isPlus(String strNum) {
        if (strNum.equals("+")) {
            return true;
        }
        return false;
    }

    public boolean isMinus(String strNum) {
        if (strNum.equals("-")) {
            return true;
        }
        return false;
    }

    public boolean isTimes(String strNum) {
        if (strNum.equals("*")) {
            return true;
        }
        return false;
    }

    public boolean isDigit(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}

enum TokenType {
    Number,
    Plus,
    Minus,
    Times,
    Undefined,
    Operation,
    Bracket,
    Register
}

class Arquivo {
    List<String> linhas;

    public Arquivo(List<String> arquivo) {
        linhas = arquivo;
    }

    public Arquivo() {
    }
}

class OperationRegister {
    String name;
    List<Token> tokens;
    public int result;

    public OperationRegister(String name, List<Token> tokens) {
        this.name = name;
        this.tokens = tokens;
    }

    public OperationRegister() {
    }
}

class OrderOperationsTuple {
    public List<Token> tokens;
    public List<OperationRegister> operations;

    OrderOperationsTuple(List<Token> tokens, List<OperationRegister> operations) {
        this.tokens = tokens;
        this.operations = operations;
    }

}

public class Interpretador {
    static File absPath = new File(Interpretador.class.getProtectionDomain().getCodeSource().getLocation().getPath());// new
                                                                                                                      // File("/Users/llage/dev/interpreter/Interpreter-A3");//

    public static String[] listFiles() {
        String[] listaDeArquivos = absPath.list();
        printSeparator();
        print("Path Absoluto: " + absPath.toString());
        printSeparator();
        print("Lista de arquivos no repositorio de:");
        for (int i = 0; i < listaDeArquivos.length; i++) {
            print(listaDeArquivos[i]);
        }
        printSeparator();
        return listaDeArquivos;
    }

    public static Arquivo readFiles(String fileName) throws IOException {
        Path path = Paths.get(fileName);
        List<String> allLines = Files.readAllLines(path);

        Arquivo fileText = new Arquivo(allLines);

        return fileText;
    }

    public static void main(String[] args) {
        run();
    }

    public static void run() {

        // Leitura de arquivos
        String[] files = listFiles();
        String fileToRead = "";

        for (String file : files) {
            if (file.contains(".txt")) {
                fileToRead = file;
                print("lido: " + file);
                printSeparator();
            }
        }

        Arquivo arquivo = new Arquivo();

        try {
            arquivo = readFiles(fileToRead);
        } catch (IOException e) {
            print("Nao foi possivel ler o arquivo. " + e.getLocalizedMessage());
            e.printStackTrace();
        }

        // Analise Lexica - Tokenizacao
        List<List<Token>> tokenList = new ArrayList<>();

        for (int i = 0; i < arquivo.linhas.size(); i++) {
            tokenList.add(tokenize(arquivo.linhas.get(i), i));
        }


        // Analise Sintatica e semantica
        for (List<Token> tokens : tokenList) {
            try {
                analyzeExpression(tokens);
            } catch (Exception e) {
                e.printStackTrace();
                print("Ocorreu um erro na compilacao: " + e.getMessage());
            }
        }

        for (List<Token> tokens : tokenList) {
            if (!(tokens.get(0).text.isEmpty())) {// remover caso nao queira pular linhas em branco e retornar 0
                OrderOperationsTuple tuple = orderOperations(tokens);
                solveOperations(tuple.tokens, tuple.operations);
            }

        }

    }

    public static void solveOperations(List<Token> tokens, List<OperationRegister> operacoes) {

        for (OperationRegister op : operacoes) {
            //print(op.name);
            //printTokens(op.tokens);
            //printSeparator();
        }

        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).type == TokenType.Register) {
                int resultRegister = solveRegister(tokens.get(i), operacoes);
                Token solvedToken = new Token(Integer.toString(resultRegister),  getTokenLine(tokens), -1);
                tokens.set(i, solvedToken);
            }
        }

        int resultadoFinal = calcOperations(tokens);
 
        System.out.print("resultado: ");
        print(resultadoFinal);

    }

    public static int getTokenLine(List<Token> tokens) {
        int result = -1;
        for (Token token : tokens) {
            if(token.line == -1) {
                result = token.line;
            }
        }
        return result;
    }

    public static int solveRegister(Token token, List<OperationRegister> operacoes) {
        int result = 0;
        List<Token> tokens = findRegisterByName(token, operacoes);

        int registerFound = findFirstRegister(tokens);
        while (registerFound != -1) {
            if (registerFound != -1) {
                int foundResult = solveRegister(tokens.get(registerFound), operacoes);
                tokens.set(registerFound, new Token(String.valueOf(foundResult), -1, -1));
            } else {

            }
            registerFound = findFirstRegister(tokens);
        }

        result += calcOperations(tokens);
        return result;
    }

    static int calcOperations(List<Token> tokens) {
        int result = 0;
        tokens = solveTimes(tokens);
        tokens = solvePlus(tokens);
        tokens = solveMinus(tokens);

        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).type == TokenType.Number) {
                result = Integer.parseInt(tokens.get(i).text);
            }
        }

        return result;
    }

        static List<Token> solveMinus(List<Token> tokens) {
        int minusFound = findMinus(tokens);
        //printTokens(tokens);
        while (minusFound != -1) {
            printSeparator();
            printSeparator();
            printSeparator();
            printSeparator();
            //print(minusFound);
            //printTokens(tokens);

            int opResult = doOperation(tokens.get(minusFound - 1).text, tokens.get(minusFound + 1).text,
                    tokens.get(minusFound).text);
            Token toAdd = new Token(String.valueOf(opResult), -1, -1);
            tokens.remove(minusFound + 1);
            tokens.remove(minusFound);
            tokens.set(minusFound - 1, toAdd);

            minusFound = findMinus(tokens);
        }
        // printTokens(tokens);

        return tokens;
    }

    static List<Token> solvePlus(List<Token> tokens) {
        int plusFound = findPlus(tokens);
         //printTokens(tokens);
        while (plusFound != -1) {
          

            int opResult = doOperation(tokens.get(plusFound - 1).text, tokens.get(plusFound + 1).text,
                    tokens.get(plusFound).text);
            Token toAdd = new Token(String.valueOf(opResult), -1, -1);
            tokens.remove(plusFound + 1);
            tokens.remove(plusFound);
            tokens.set(plusFound - 1, toAdd);

            plusFound = findPlus(tokens);
        }
     //printTokens(tokens);

        return tokens;
    }

    static List<Token> solveTimes(List<Token> tokens) {
        int result = 0;
        int timesFound = findTimes(tokens);
         //printTokens(tokens);
        while (timesFound != -1) {
          

            int opResult = doOperation(tokens.get(timesFound - 1).text, tokens.get(timesFound + 1).text,
                    tokens.get(timesFound).text);
            Token toAdd = new Token(String.valueOf(opResult), -1, -1);
            tokens.remove(timesFound + 1);
            tokens.remove(timesFound);
            tokens.set(timesFound - 1, toAdd);
           
            timesFound = findTimes(tokens);
            result = opResult;
        }

        return tokens;
    }

    static int findTimes(List<Token> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).text.equals("*")) {
                return i;
            }
        }
        return -1;
    }

    static int findPlus(List<Token> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).text.equals("+")) {
                return i;
            }
        }
        return -1;
    }

    static int findMinus(List<Token> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).text.equals("-")) {
                return i;
            }
        }
        return -1;
    }

    static List<Token> findRegisterByName(Token token, List<OperationRegister> operacoes) {
        for (int i = 0; i < operacoes.size(); i++) {
            if (operacoes.get(i).name == token.text) {
                return operacoes.get(i).tokens;
            }
        }
        return null;
    }

    static int findFirstRegister(List<Token> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).text.contains("r")) {
                return i;
            }
        }

        return -1;
    }

    public static void analyzeExpression(List<Token> tokens) throws Exception {
        if (tokens.isEmpty())
            return;
        countBrackets(tokens);

        TokenType nextType = TokenType.Number;

        for (Token token : tokens) {
            if (nextType == TokenType.Number) {
                if (token.type != TokenType.Number && token.type != TokenType.Bracket) {
                    throw new Exception("Nesse lugar deveria ser um numero mas tem \"" + token.text
                            + "\". onde ocorreu: " + token.line + ":" + token.pos);
                }
                
                if (token.type == TokenType.Bracket) {
                    nextType = TokenType.Number;
                } else {
                    nextType = TokenType.Operation;
                }
                
            } else {
                if (token.type == TokenType.Bracket) {
                    nextType = TokenType.Operation;
                } else if (!(token.type == TokenType.Plus || token.type == TokenType.Minus || token.type == TokenType.Times)) {
                    throw new Exception("Nesse lugar deveria ser um operador mas tem \"" + token.text
                            + "\". Onde ocorreu: " + token.line + ":" + token.pos);
                }
                if (token.type == TokenType.Bracket) {

                } else {
                    nextType = TokenType.Number;
                }
                
            }
        }

        if (tokens.get(tokens.size() - 1).type == TokenType.Minus || tokens.get(tokens.size() - 1).type == TokenType.Plus || tokens.get(tokens.size() - 1).type == TokenType.Times) {
            throw new Exception(
                    "Não se deve finalizar com um operador." + " Onde ocorreu: linha \"" + tokens.get(0).line + "\"");
        }
        print("linha: " + tokens.get(0).line + " correta");

    }

    class Operation {
        String left;
        int nextOp;
        TokenType operator;
        int priority = 0;

        public Operation(String left, TokenType op) {
            this.left = left;
        }
    }

    public static OrderOperationsTuple orderOperations(List<Token> tokens) {
        List<OperationRegister> operacoes = new ArrayList<>();
        List<Token> updatedTokens = tokens;
        int registerCounter = 0;

        int lastOpenBracketPosition = getLastOpenBracket(updatedTokens);
        int firstCloseBracketPosition = getFirstCloseBracket(updatedTokens, lastOpenBracketPosition);

        while (lastOpenBracketPosition != -1 && firstCloseBracketPosition != -1) {
            String register = "r" + String.valueOf(registerCounter);

            int openBracket = getLastOpenBracket(updatedTokens);
            int closeBracket = getFirstCloseBracket(updatedTokens, openBracket);

            List<Token> sublist = updatedTokens.subList(openBracket, closeBracket + 1);
            OperationRegister newOp = new OperationRegister(register, sublist);
            operacoes.add(newOp);

            updatedTokens = extractFromIndex(updatedTokens, openBracket, closeBracket, register);

            lastOpenBracketPosition = getLastOpenBracket(updatedTokens);
            registerCounter++;
        }

        return new OrderOperationsTuple(updatedTokens, operacoes);
    }

    public static List<Token> extractFromIndex(List<Token> tokens, int start, int end, String register) {
        List<Token> aux = new ArrayList<>();

        for (int i = 0; i < tokens.size(); i++) {
            if ((i >= start) && (i <= end)) {

            } else {
                aux.add(tokens.get(i));
            }
        }
        aux.add(start, new Token(register, -1, -1));
        return aux;
    }

    public static int getLastOpenBracket(List<Token> tokens) {
        int result = -1;

        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).text.equals("(")) {
                result = i;
            }
        }
        return result;
    }

    public static int getFirstCloseBracket(List<Token> tokens, int start) {
        int result = -1;
        if (start == -1)
            return -1;

        for (int i = start; i < tokens.size(); i++) {
            if (tokens.get(i).text.equals(")")) {
                return i;
            }
        }
        return result;
    }

    public static void countBrackets(List<Token> tokens) throws Exception {

        if (tokens.isEmpty())
            return;
        int count = 0;
        for (Token token : tokens) {
            if (token.text.equals("(")) {
                count++;
            } else if (token.text.equals(")")) {
                count--;
            }
        }

        if (count < 0)
            throw new Exception(count + "Falta abrir bracket na linha: " + tokens.get(0).line);
        if (count > 0)
            throw new Exception(count + "Falta fechar bracket na linha: " + tokens.get(0).line);
    }

    public static int doOperation(String left, String right, String operator) {
        int lefti = Integer.parseInt(left);
        int righti = Integer.parseInt(right);
        switch (operator) {
            case "*":
                return lefti * righti;
            case "+":
                return lefti + righti;
            case "-":
                return lefti - righti;
            default:
                return 0;
        }
    }

    public static List<Token> tokenize(String linha, int nlinha) {
        String[] tokens = linha.split(" ");
        List<Token> tokenList = new ArrayList<>();

        for (int i = 0; i < tokens.length; i++) {
            Token nToken = new Token(tokens[i], nlinha, linha.indexOf(tokens[i]));
            tokenList.add(nToken);
        }

        printTokens(tokenList);
        return tokenList;
    }

    public static void print(String[] msg) {
        for (String value : msg) {
            print(value);
        }
    }

    public static void print(List<String> msg) {
        if (msg == null)
            return;
        msg.forEach((v) -> print(v));
    }

    public static void print(String msg) {
        System.out.println(msg);
    }

    public static void print(int msg) {
        System.out.println(msg);
    }

    public static void print(TokenType msg) {
        System.out.println(msg);
    }

    public static void printTokens(List<Token> tokens) {
        if (tokens.isEmpty())
            return;
        for (Token token : tokens) {
            print("tipo:" + token.type + " texto: " + token.text + " posicao: " + token.pos);
        }
    }

    public static void printSeparator() {
        System.out.println("========================");
    }

}