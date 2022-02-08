package gentle_introduction

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}

import scala.concurrent.duration.DurationInt

// https://www.youtube.com/watch?v=RfKxoW8HQUY
object ClassicIceCreamActors {

  // actor that represents a person
  class PersonActor(firstName: String, lastName: String) extends Actor with ActorLogging {
    // business logic
    override def receive: Receive = {
      case "eat-ice-cream" =>
        log.info(s"$firstName has started eating ice cream 🍦")
        Thread.sleep(5.seconds.toMillis)
        log.info(s"$firstName has eaten his ice cream ✅")
    }
  }

  def main(args: Array[String]): Unit = {

    // Actor system initialisation
    val system = ActorSystem("first-system")

    // spawn actors, returns ActorRef - like a pointer to the Actor system (like a unique URL)
    val actor1: ActorRef = system.actorOf(Props(classOf[PersonActor], "John", "Smith"), "john")
    val actor2: ActorRef = system.actorOf(Props(classOf[PersonActor], "Bob", "Smith"), "bob")
    val actor3: ActorRef = system.actorOf(Props(classOf[PersonActor], "Richard", "Smith"), "richard")

    // sending messages to the actor's mailbox, using Tell, sent async
    actor1 ! "eat-ice-cream"
    actor1 ! "eat-ice-cream"
    actor1 ! "eat-ice-cream"

    actor2 ! "eat-ice-cream"

    // this will end up in the special mailbox - dead letters mailbox - messages that do not get delivered
    actor3 ! "drink-beer"
  }
}
