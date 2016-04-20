import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, Terminated}
import akka.util.Timeout

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

object Reaper {
  // Used by others to register an Actor for watching
  case class WatchMe(ref: ActorRef)
  case class SetGraph(ref: ActorRef)
}

class Reaper extends Actor {
  import Reaper._

  var graphRef: ActorRef = null;
  // Keep track of what we're watching
  val watched = ArrayBuffer.empty[ActorRef]

  // Derivations need to implement this method.  It's the
  // hook that's called when everything's dead
  def allSoulsReaped(): Unit = context.system.shutdown();

  // Watch and check for termination
  final def receive = {
    case SetGraph(ref) =>
      graphRef = ref;
    case WatchMe(ref) =>
      context.watch(ref)
      watched += ref
    case Terminated(ref) =>
      watched -= ref
      if (watched.isEmpty){
        graphRef ! printReport();
        allSoulsReaped();
      }
  }
}