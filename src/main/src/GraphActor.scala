import java.util

import akka.actor.Actor
import akka.actor.Actor.Receive
import org.jgrapht.DirectedGraph
import org.jgrapht.graph.DefaultDirectedGraph

import scala.collection.JavaConverters._

/**
  * Created by Josh on 20/04/2016.
  */
case class addNode(node: String)

case class addLink(nodeA: String, nodeB: String)

case class printReport()

class GraphActor extends Actor {
  val dg: DirectedGraph[String, String] = new DefaultDirectedGraph[String, String](classOf[String])

  override def receive: Receive = {
    case addNode(nodePath) =>
      if(!dg.containsVertex(nodePath)) {
        dg.addVertex(nodePath)

      }

    case addLink(a, b) =>
      if(!dg.containsEdge(a, b)) {
        if (dg.containsVertex(a) && dg.containsVertex(b)) {
          dg.addEdge(a, b, "Link from " + a + " to " + b)
        }
        else if (!dg.containsVertex(b) && !dg.containsVertex(a)) {
          dg.addVertex(a)
          dg.addVertex(b)
          dg.addEdge(a, b, "Link from " + a + " to " + b)
        }
        else if (!dg.containsVertex(a)) {
          dg.addVertex(a)
          dg.addEdge(a, b, "Link from " + a + " to " + b)
        }
        else if (!dg.containsVertex(b)) {
          dg.addVertex(b)
          dg.addEdge(a, b, "Link from " + a + " to " + b)
        }
      }
    case printReport() =>
      val vs: util.Set[String] = dg.vertexSet()
      for(i: String <- vs.asScala){
        var inc: util.Set[String] = dg.incomingEdgesOf(i)
        if(inc.size() < 1){
          if(!i.contains("index")){
            println("WARN: No incoming links to page " + i)
          }
        }
      }
  }
}
