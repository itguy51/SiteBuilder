import org.commonmark.node.*;
import org.python.util.PythonInterpreter;

import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BuilderVisitor extends AbstractVisitor {


    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "WeakerAccess"})
    final ArrayList<String> linkList = new ArrayList<String>();
    @Override
    public void visit(Link l){
        linkList.add(l.getDestination());
    }

    @Override
    public void visit(Image i){ linkList.add(i.getDestination()); }

    @Override
    public void visit(FencedCodeBlock l){
        while(!SyntaxHighlighter.getInstance().isReady()){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        String codel = l.getLiteral();
        String[] lines = codel.split("\n");
        Matcher m = Pattern.compile("\\$\\$\\{.*?\\}\\$\\$")
                .matcher(lines[0]);

        HtmlBlock b = new HtmlBlock();
        if (m.matches()) {
            String header = m.group();
            String fname = header.replace("$", "").replace("{", "").replace("}", "");
            b.setLiteral(SyntaxHighlighter.getInstance().syntaxHighlight(codel.replace(header, ""), fname));
            //This is janky af.
        }else{
            b.setLiteral(SyntaxHighlighter.getInstance().syntaxHighlight(codel, "plain.txt")); //Just unify the appearance. Why not.
        }
        l.appendChild(b);
        l.insertAfter(b);
        l.unlink();
    }
}
