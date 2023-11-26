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

            if(isDigit(value)) {
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
            if(strNum.contains("r")) {
                return true;
            }
            return false;            
        }

        public boolean isBracket(String strNum) {
            if(strNum.equals(")") || strNum.equals("(")) {
                return true;
            }
            return false;            
        }

        public boolean isPlus(String strNum) {
            if(strNum.equals("+")) {
                return true;
            }
            return false;            
        }

        public boolean isMinus(String strNum) {
            if(strNum.equals("-")) {
                return true;
            }
            return false;            
        }

        public boolean isTimes(String strNum) {
            if(strNum.equals("*")) {
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

        public Arquivo() {}
    }

    class OperationRegister {
        String name;
        List<Token> tokens;
        public OperationRegister(String name, List<Token> tokens) {
            this.name = name;
            this.tokens = tokens;
        }        
        public OperationRegister(){}
    }

public class Interpretador {
    static File absPath = new File("/Users/llage/dev/interpreter/Interpreter-A3");//new File(Interpretador.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    
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

        //print(arquivo.linhas.get(0));

        // Analise Lexica - Tokenizacao

        List<Token> tokens = tokenize(arquivo.linhas.get(0), 0);

        List<Token> tokens2 = tokenize(arquivo.linhas.get(1), 1);


        // Analise Sintatica

        try {
            analyzeExpression(tokens);
        } catch (Exception e) {
            e.printStackTrace();
            print("Ocorreu um erro na compilacao: " + e.getMessage());
        }        

        try {
            analyzeExpression(tokens2);
        } catch (Exception e) {
            e.printStackTrace();
            print("Ocorreu um erro na compilacao: " + e.getMessage());
        }

        orderOperations(tokens);
    }

    public static void analyzeExpression(List<Token> tokens) throws Exception {
        if (tokens.isEmpty()) return;
        countBrackets(tokens);

        TokenType nextType = TokenType.Number;

        for (Token token : tokens) {
            if(nextType == TokenType.Number) {
                if (token.type != TokenType.Number) {
                    throw new Exception("Nesse lugar deveria ser um numero mas tem \"" + token.text + "\". onde ocorreu: " + token.line + ":" + token.pos );
                }
                nextType = TokenType.Operation;
            } else {
                if (!(token.type == TokenType.Plus || token.type == TokenType.Minus || token.type == TokenType.Times)) {
                     throw new Exception("Nesse lugar deveria ser um operador mas tem \"" + token.text + "\". Onde ocorreu: " + token.line + ":" + token.pos );
                }
                nextType = TokenType.Number;
            }            
        }

        if (tokens.get(tokens.size()).type != TokenType.Number) {
            throw new Exception("Não se deve finalizar com um operador." + " Onde ocorreu: linha \""+ tokens.get(0).line + "\"");
        }
        print("linha: " + tokens.get(0).line + " correta");

    }

    

    class Operation {
        String left;
        int nextOp;
        TokenType operator;
        int priority = 0;
        public Operation (String left, TokenType op) {
            this.left = left;
        }
    }

    //operation.sum.next ()


    //(1 + 2 + 3) + 4
    // 1 * 2 + 3
    // 1 + 2 * 3
    // 1 + 2 + 3 //p1
    // + 4 //p0

    // 1 + (2 + 3 + 4)
    public static void orderOperations(List<Token> tokens) {
        int result = 0;
        List<OperationRegister> operacoes = new ArrayList<>();
        List<Token> updatedTokens = tokens;
        int registerCounter = 0;

        while(getLastOpenBracket(updatedTokens) != -1) {
        String register = "r" + String.valueOf(registerCounter);

        int openBracket = getLastOpenBracket(updatedTokens);
        int closeBracket = getFirstCloseBracket(updatedTokens);

        List<Token> sublist = updatedTokens.subList(openBracket, closeBracket+1);
        OperationRegister newOp = new OperationRegister(register, sublist);
        operacoes.add(newOp);

        updatedTokens = extractFromIndex(updatedTokens, openBracket, closeBracket, register);
        

        printSeparator();
        printTokens(updatedTokens);
        printSeparator();
        printTokens(sublist);
        printSeparator();
        printSeparator();
        registerCounter++;

        }
       


    }

    public static List<Token> extractFromIndex(List<Token> tokens, int start, int end, String register) {
        List<Token> aux = new ArrayList<>();

        for (int i=0; i< tokens.size(); i++) {
            if((i >= start) && (i <= end)) {

            } else {
                aux.add(tokens.get(i));
            }

        }
        aux.add(start, new Token(register, -1, -1));
        return aux;
    }

   

    public static int getNextTimes(List<Token> tokens) {
        int result = -1;

        for (int i=0; i < tokens.size(); i++) {
             if (tokens.get(i).text.equals("*")) {
                return i;
            }           
        }
        return result;
    }

    public static int getLastOpenBracket(List<Token> tokens) {
        int result = -1;

        for (int i=0; i < tokens.size(); i++) {
             if (tokens.get(i).text.equals("(")) {
                result = i;
            }           
        }
        return result;
    }

    public static int getFirstCloseBracket(List<Token> tokens) {
        int result = -1;

        for (int i=0; i < tokens.size(); i++) {
             if (tokens.get(i).text.equals(")")) {
                return i;
            }
        }
        return result;
    }

    public static void countBrackets(List<Token> tokens) throws Exception {

        if (tokens.isEmpty()) return;
        int count = 0;
        for (Token token : tokens) {
            if (token.text.equals("(")) {
                count++;
            } else if (token.text.equals(")")) {
                count--;
            }
        }

        if (count < 0) throw new Exception(count + "Falta abrir chaves na linha: " + tokens.get(0).line);
        if (count > 0) throw new Exception(count + "Falta fechar chaves na linha: " + tokens.get(0).line);
    }

    public static int doOperation(int left, int right, String operator) {
        switch (operator) {
            case "*":
            return left * right;
            case "+":
            return left + right;
            case "-":
            return left - right;
            default:
            return 0;
        }
    }

    public static List<Token> tokenize(String linha, int nlinha) {
        String[] tokens = linha.split(" ");
        List<Token> tokenList = new ArrayList<>();

        for (int i=0; i< tokens.length; i++) {
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
        if (msg == null) return;
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
        if (tokens.isEmpty()) return;
        for (Token token : tokens) {
            print("tipo:" + token.type + " texto: " + token.text + " posicao: " + token.pos);
        }
    }
    
    public static void printSeparator() {
        System.out.println("========================");
    }

}