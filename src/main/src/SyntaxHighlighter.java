import org.python.util.PythonInterpreter;

/**
 * Created by Josh-MBP on 07/05/2016.
 */
public class SyntaxHighlighter {
    private final PythonInterpreter pi;
    private static SyntaxHighlighter self;
    private boolean isReady = false;
    private SyntaxHighlighter(){
        pi = PythonInterface.getPI();
    }

    @SuppressWarnings("unused")
    public static SyntaxHighlighter getInstance(){
        if(self == null){
            synchronized (BuilderVisitor.class){
                if(self == null){
                    long start = System.currentTimeMillis();
                    self = new SyntaxHighlighter();
                    self.syntaxHighlight("sudo apt-get install build-essential", "plain.txt");
                    //The first highlight takes forever.
                    //I want the interface to be 'spun up'.
                    long end = System.currentTimeMillis();
                    System.out.println("Syntax Highlighter Spinup: " + (end-start));
                    self.isReady = true;

                }
            }
        }
        return self;
    }
    public String syntaxHighlight(String inputCode, String filename){
        pi.set("code", inputCode);
        pi.set("fname", filename);
        pi.set("exists", true);
        pi.exec("try:\n" +
                "    get_lexer_for_filename(fname)\n" +
                "except:\n" +
                "     exists = False");
        boolean syntaxExists = pi.get("exists", boolean.class);
        if(!syntaxExists){
            System.out.println("Error - No lexer found for source file " + filename);
            pi.set("fname", "plain.txt");
        }

        pi.exec("result = highlight(code, get_lexer_for_filename(fname), HtmlFormatter(noclasses=True))");

        return pi.get("result", String.class);
    }

    public boolean isReady() {
        return  isReady;
    }
}
