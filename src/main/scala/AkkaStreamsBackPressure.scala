import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}

// https://www.youtube.com/watch?v=L5FAyCCWGL0
object AkkaStreamsBackPressure {

  implicit val system: ActorSystem[_] = ActorSystem(Behaviors.empty, "StreamsSystem")

  // Implements the reactive stream specification
  // components of akka streams
  // Publisher - emits elements async (Sources)
  // Subscriber - consumes elements async (Sinks)
  // Transformer - transforms things along the way (Flows)
  // []         ->  []       ->  []
  // publisher  processor   subscriber
  // source     flow        sink

  val source = Source(1 to 1000)
  val flow = Flow[Int].map(_ * 10) // receives elements types Int and transforms then emits the results downstream to the Sink
  val sink = Sink.foreach[Int](println)

  val graph = source.via(flow).to(sink) // blueprint of the akka stream

  // Backpressure
    val slowSink = Sink.foreach[Int] { x =>
      Thread.sleep(1000) // mimics the sink running slowly
      println(x)
    }

  val debuggingFlow = Flow[Int].map { x =>
    println(s"[flow] $x")
    x
  }

  def demoNoBackpressure(): Unit = {
    // "fusions" - these 3 components work on the same actor
    source.via(debuggingFlow).to(slowSink).run()
  }

  def demoBackpressure(): Unit = {
    source
      .via(debuggingFlow) // this bits fast
      .async // make an async boundary here, everything after this point runs on a separate actor
      .to(slowSink)
      .run()
  }

  def demoDrophead(): Unit = {
    // [1, 2, 3, 4, 5, 6, 7, 8, 9, 10] <- 11
    // [11, 2, 3, 4, 5, 6, 7, 8, 9, 10] <- 12
    source
      .via(debuggingFlow.buffer(10, OverflowStrategy.dropHead)) // this bits fast
      .async // make an async boundary here, everything after this point runs on a separate actor
      .to(slowSink)
      .run()
  }


  def main(args: Array[String]): Unit = {
//    graph.run() // does the work
    demoDrophead()
  }
}
