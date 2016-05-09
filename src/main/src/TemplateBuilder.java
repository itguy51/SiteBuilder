/**
 * Created by Josh-MBP on 08/05/2016.
 */
import com.fasterxml.jackson.databind.ObjectMapper;
import j2html.tags.Tag;
import j2html.tags.UnescapedText;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static j2html.TagCreator.*;


public class TemplateBuilder {
    private static TemplateBuilder self;
    private List<Tag> headerTags = new ArrayList<Tag>();
    private List<Tag> endOfBodyTags = new ArrayList<Tag>();
    private TemplateBuilder(String config){
        String titleText = "";
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String,Object> userData = mapper.readValue(new File(config), Map.class);
            if(userData.containsKey("title")){
                titleText = (String)userData.get("title");
            }
            if(userData.containsKey("stylesheets")){
                ArrayList<String> ssheets = (ArrayList<String>) userData.get("stylesheets");
                for (String stylesheet : ssheets){
                    headerTags.add(link().withRel("stylesheet").withHref(stylesheet));
                }
            }
            if(userData.containsKey("inline-style")){
                String inlineStyle = (String) userData.get("inline-style");
                if(!inlineStyle.equals(""))
                    headerTags.add(style().withType("text/css").with(new UnescapedText(inlineStyle)));
            }
            if(userData.containsKey("header-javascripts")){
                ArrayList<String> scripts = (ArrayList<String>) userData.get("header-javascripts");
                for (String script : scripts){
                    headerTags.add(script().withSrc(script)); //See HTML5 spec as to why no "type" attr. Stupid IE.
                }
            }
            if(userData.containsKey("header-inline-javascript")){
                String inlineScript = (String) userData.get("header-inline-javascript");
                if(!inlineScript.equals(""))
                    headerTags.add(script().with(new UnescapedText(inlineScript)));
            }
            if(userData.containsKey("footer-javascripts")){
                ArrayList<String> scripts = (ArrayList<String>) userData.get("footer-javascripts");
                for (String script : scripts){
                    endOfBodyTags.add(script().withSrc(script)); //See HTML5 spec as to why no "type" attr. Stupid IE.
                }
            }
            if(userData.containsKey("footer-inline-javascript")){
                String inlineScript = (String) userData.get("footer-inline-javascript");
                if(!inlineScript.equals(""))
                    endOfBodyTags.add(script().with(new UnescapedText(inlineScript)));
            }

        } catch (IOException e) {
            System.out.println("ERROR: Could not find config.json - Using defaults.");
        }

        headerTags.add(title(titleText));


    }

    public static void init(String config){
        self =  new TemplateBuilder(config);
    }

    @SuppressWarnings("unused")
    public static TemplateBuilder getInstance(){
        return self;
    }

    @SuppressWarnings("unused")
    public String RenderPage(String content){
        List<Tag> stuff = new ArrayList<Tag>(endOfBodyTags);
        Collections.reverse(stuff);
        stuff.add(new UnescapedText(content));
        Collections.reverse(stuff);
        Tag doc = html().with(
                head().with(
                        headerTags
                ),
                body().with(stuff)
        );
        return document().render() + doc.render();
    }
}
