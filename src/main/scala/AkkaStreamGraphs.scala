import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.ClosedShape
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source, Zip}

// https://www.youtube.com/watch?v=8XLB28KDtgg
object AkkaStreamGraphs {
  implicit val system: ActorSystem[_] = ActorSystem(Behaviors.empty, "StreamGraphs")

  // a source that "emits" elements
  val source = Source(1 to 1000)
  // a flow - receives elements, then transforms them and emits their results
  val flow = Flow[Int].map(_ * 2)
  // a sink - receiver of elements
  val sink = Sink.foreach[Int](println)

  // plug these together like pipes, using a graph
  val graph = source.via(flow).to(sink) // description of an akka stream

  // Graph DSL - level 2 of akka streams!
  // source of ints -> 2 independent "hard" computations -> stitch the results in a tuple -> print the tuples

  // step 1 - the frame
  val specialGraph = GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] => // Builder is mutable
    import GraphDSL.Implicits._

    // step 2 - create the building blocks
    val input = builder.add(Source(1 to 1000))
    val incrementer = builder.add(Flow[Int].map(_ + 1)) // "hard" computation #1
    val multiplier = builder.add(Flow[Int].map(_ * 10)) // "hard" computation #2
    val output = builder.add(Sink.foreach[(Int, Int)](println))

    val broadcast = builder.add(Broadcast[Int](2)) // takes a single input and has 2 outputs, it duplicates the single input into both its outputs
    val zip = builder.add(Zip[Int, Int]) // takes 2 inputs and outputs the type (Int, Int)

    // step 3 - glue the components together
    input ~> broadcast // input feeds into broadcast

    broadcast.out(0) ~> incrementer ~> zip.in0 // first output of broadcast feeds into incrementer which feeds into zip 0
    broadcast.out(1) ~> multiplier ~> zip.in1

    zip.out ~> output // output

    // step 4 - closing
    ClosedShape // singleton that validates your graph
  }

  def main(args: Array[String]): Unit = {
//    graph.run()
    RunnableGraph.fromGraph(specialGraph).run()
  }
}
