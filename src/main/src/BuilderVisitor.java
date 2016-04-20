import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.Link;
import org.python.util.PythonInterpreter;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BuilderVisitor extends AbstractVisitor {
    private final PythonInterpreter pi;
    private static BuilderVisitor self;
    private BuilderVisitor(){
        pi = new PythonInterpreter();
        pi.exec("from pygments import highlight\n"
                + "from pygments.lexers import get_lexer_for_filename\n"
                + "from pygments.formatters import HtmlFormatter");
    }

    @SuppressWarnings("unused")
    public static BuilderVisitor getInstance(){
        if(self == null)
            self = new BuilderVisitor();
        return self;
    }
    private String syntaxHighlight(String inputCode, String filename){
        pi.set("code", inputCode);
        pi.set("fname", filename);
        pi.exec("result = highlight(code, get_lexer_for_filename(fname), HtmlFormatter(noclasses=True))");
        return pi.get("result", String.class);


    }

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "WeakerAccess"})
    final ArrayList<String> linkList = new ArrayList<String>();
    @Override
    public void visit(Link l){
        linkList.add(l.getDestination());
    }

    @Override
    public void visit(FencedCodeBlock l){
        String codel = l.getLiteral();
        String[] lines = codel.split("\n");
        Matcher m = Pattern.compile("\\$\\$\\{.*?\\}\\$\\$")
                .matcher(lines[0]);

        if (m.matches()) {
            String header = m.group();
            String fname = header.replace("$", "").replace("{", "").replace("}", "");
            HtmlBlock b = new HtmlBlock();
            b.setLiteral(syntaxHighlight(codel.replace(header, ""), fname));
            l.appendChild(b);
            l.insertAfter(b);
            l.unlink();
            //This is janky af.
        }else{
            HtmlBlock b = new HtmlBlock();
            b.setLiteral(syntaxHighlight(codel, "plain.txt")); //Just unify the appearance. Why not.
            l.appendChild(b);
            l.insertAfter(b);
            l.unlink();
        }
    }
}
