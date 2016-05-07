import org.python.util.PythonInterpreter;

/**
 * Created by Josh-MBP on 07/05/2016.
 */
public class PythonInterface {
    private static final PythonInterpreter pi = new PythonInterpreter();
    private static PythonInterface self;
    private PythonInterface(){
        pi.exec("from pygments import highlight\n"
                + "from pygments.lexers import get_lexer_for_filename\n"
                + "from pygments.formatters import HtmlFormatter\n"
                + "result = highlight(\"farts\", get_lexer_for_filename(\"plain.txt\"), HtmlFormatter(noclasses=True))");

    }
    public static PythonInterpreter getPI(){
        if(self == null){
            synchronized (PythonInterface.class){
                if(self == null){
                    self = new PythonInterface();
                }
            }
        }
        return pi;
    }
}
