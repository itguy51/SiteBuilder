import java.io.File
import java.util.Scanner

import Reaper.{SetGraph, WatchMe}
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.routing.{RoundRobinPool, SmallestMailboxPool}
import org.apache.commons.io.{FileUtils, FilenameUtils}

import scala.collection.mutable.ArrayBuffer

/**
  * Created by Josh on 20/04/2016.
  */
object main {
  def main(args: Array[String]) {

    val workingDir = "C:\\Users\\Josh\\Desktop\\Site\\new"
    val actosystem = ActorSystem("ActorSystem")
    val graph = actosystem.actorOf(Props[GraphActor], name = "Graph")
    val reaper = actosystem.actorOf(Props[Reaper], name="Grim")
    reaper ! SetGraph(graph)

    val source: String = workingDir+ "\\src"
    val out: String = workingDir+ "\\out"
    val head: String = new Scanner(new File(source + "\\head.txt")).useDelimiter("\\Z").next
    val foot: String = new Scanner(new File(source + "\\foot.txt")).useDelimiter("\\Z").next

    val path: Array[File] = new File(source).listFiles()
    var fileList: ArrayBuffer[String] = showFiles(path, source)
    fileList -= ("\\head.txt", "\\foot.txt")
    for(file <- fileList){
      if(FilenameUtils.getExtension(file).equals("md")){
        val currentFileName: String = FilenameUtils.removeExtension(file) + ".html"
        graph ! addNode(currentFileName.replace("\\", "/"))
        val renderEngine = actosystem.actorOf(Props[PageRenderer])
        reaper ! WatchMe(renderEngine)
        renderEngine ! Render(file, source, out, head, foot, graph)
      }else{
        graph ! addNode(file.replace("\\", "/"))
        FileUtils.copyFile(new File(source + file), new File(out + file))
      }
    }

  }

  def showFiles(files: Array[File], trunc: String): ArrayBuffer[String] = {
    var filesList: ArrayBuffer[String] = new ArrayBuffer[String]()
    for (file <- files) {
      if (file.isDirectory) {
        filesList ++= showFiles(file.listFiles, trunc)
      }
      else {
        filesList += file.getPath.replace(trunc, "")
      }
    }
    filesList
  }
}
