import java.io.File
import java.nio.file.{Files, Paths}
import java.util
import java.util.Scanner

import akka.actor.{Actor, ActorRef}
import org.apache.commons.io.FilenameUtils
import org.commonmark.Extension
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.html.HtmlRenderer
import org.commonmark.node.Node
import org.commonmark.parser.Parser

import scala.collection.JavaConversions._

/**
  * Created by Josh on 20/04/2016.
  */
case class Render(relpath: String, source: String, out: String, head: String, foot: String, graph: ActorRef)

class PageRenderer extends Actor{
  override def receive: Receive = {
    case Render(rel, source, out, head, foot, graph) =>
      val startTime: Long = System.nanoTime()
      val extensions: util.List[Extension] = util.Arrays.asList(TablesExtension.create, AutolinkExtension.create, StrikethroughExtension.create)
      val parser: Parser = Parser.builder().extensions(extensions).build()
      val renderer: HtmlRenderer = HtmlRenderer.builder().extensions(extensions).build()

      val inputSourceFile: String = source + rel
      val currentFileName: String = FilenameUtils.removeExtension(rel) + ".html"
      val inputSourceCode: String = new Scanner(new File(inputSourceFile)).useDelimiter("\\Z").next()
      val document: Node = parser.parse(inputSourceCode)
      val bv: BuilderVisitor = new BuilderVisitor()
      document.accept(bv)
      val links = bv.linkList
      for (i <- links) {
        if (!i.contains("http")) {
          graph ! addLink(currentFileName.replace("\\", "/"), i.replace("\\", "/"))
        }
      }

      var output: String = renderer.render(document)
      val outPath: String = out + currentFileName
      val f: File = new File(new File(outPath).getParent)
      if(!f.isDirectory){
        if(!f.mkdirs()){
          println("Woah. We hit a bump.")
        }
      }

      val endTime: Long = System.nanoTime()
      output = TemplateBuilder.getInstance().RenderPage(output) + "\n<!-- Rendered from " + rel + " in " + (endTime - startTime) / 1000000 + "ms -->"
      Files.write(Paths.get(out + currentFileName), output.getBytes())
      context.stop(self)
  }
}
