package gentle_introduction

import akka.NotUsed
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport._
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.pattern.ask
import akka.stream.RestartSettings
import akka.stream.scaladsl.{Flow, RestartFlow, Sink, Source}
import com.typesafe.scalalogging.LazyLogging
import gentle_introduction.WeatherStreams.Domain.{Measurement, Temperature}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt
import scala.xml.NodeSeq

object WeatherStreams extends LazyLogging {

  object Domain {
    type Temperature = Double
    type Measurement = Option[Temperature]
  }

  class ChangeDetector extends Actor {

    import ChangeDetector._

    var measurement: Measurement = None

    override def receive: Receive = {
      case Some(temperature: Temperature) =>
        sender() ! detectSetAndReply(temperature)
    }

    private def detectSetAndReply(newTemperature: Temperature): ChangeType =
      if (measurement.isEmpty) {
        measurement = Some(newTemperature)
        Same(newTemperature)
      } else {
        val oldTemperature = measurement.getOrElse(Double.MinValue)
        if (oldTemperature != newTemperature) {
          measurement = Some(newTemperature)
          Changed(oldTemperature, newTemperature)
        } else Same(oldTemperature)
      }
  }

  object ChangeDetector {
    sealed trait ChangeType

    case class Same(temperature: Temperature) extends ChangeType

    case class Changed(from: Temperature, to: Temperature) extends ChangeType
  }

  // Akka http client, for issuing request, dealing with response and XML parsing
  def getMeasurement(implicit system: ActorSystem, ec: ExecutionContext): Future[Measurement] =
    for {
      response <- Http().singleRequest(HttpRequest(uri = "http://localhost"))
      nodes <- Unmarshal(response.entity).to[NodeSeq]
      measurement = (nodes \\ "t").headOption.flatMap(_.text.toDoubleOption)
    } yield measurement

  private val direction: ChangeDetector.Changed => String = {
    case ChangeDetector.Changed(from, to) if to > from => "up 🔼"
    case ChangeDetector.Changed(from, to) if to < from => "down 🔽"
    case _ => "impossible."
  }

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("temperature system")
    import system.dispatcher

    val changeDetector: ActorRef = system.actorOf(
      Props(classOf[ChangeDetector], "change-detector")
    )

    // Source generates content,
    // e.g. file source, db source, web service, in this case a timer that generates tick
    // Flow - transformers from one data shape to another data shape
    // Sink - End output of where the data will flow in


    // This flow is restarted if failures occur
    val getMeasurementsFlow: Flow[String, Measurement, NotUsed] =
      RestartFlow.onFailuresWithBackoff(RestartSettings(10.seconds, 30.seconds, 0.3)) { () =>
        Flow[String].mapAsyncUnordered(1) { _ => getMeasurement }
      }

    Source.tick(1.seconds, 2.seconds, "Tick")
      .via(getMeasurementsFlow) // Pass "Tick" to flow
      .alsoTo(Sink.foreach(v => logger.info(s"Latest measurement is ${v.getOrElse("none")} 🌡"))) // in parallel write to outputs
      .mapAsync(1)(measurement => changeDetector.ask(measurement)(1.second)) // "Ask" actor to detect changes
      .collect { case change: ChangeDetector.Changed => change } // Only care about actual "changes"
      .runWith(Sink.foreach(change =>
        logger.info(s"The temperature has changed. From ${change.from} to ${change.to}, ${direction(change)}."))) // Run and see the changes
  }
}
