package gentle_introduction

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}

import scala.concurrent.duration.DurationInt

// https://www.youtube.com/watch?v=RfKxoW8HQUY
object TypedIceCreamActors {
  // has two different states
  // 1. idle -> EatIceCream
  // 2. eatingIceCream -> FinishedEatingIceCream

  trait PersonCommand

  final case object EatIceCream extends PersonCommand

  final case object FinishedEatingIceCream extends PersonCommand

  object PersonCommand {

    // idle state
    def idle(firstName: String): Behavior[PersonCommand] =
      Behaviors.receiveMessage[PersonCommand] {
        case EatIceCream => eatingIceCream(firstName) // move to next state
        case _ => Behaviors.same
      }

    // eating ice cream state
    def eatingIceCream(firstName: String): Behavior[PersonCommand] =
      Behaviors.setup { context =>
        Behaviors.withTimers[PersonCommand] { timers =>
          context.log.info(s"$firstName has started eating his ice cream. 🍦")
          timers.startSingleTimer(FinishedEatingIceCream, 3.seconds)

          Behaviors.receiveMessage {
            case FinishedEatingIceCream =>
              context.log.info(s"$firstName is done with ice cream. 🍦")
              idle(firstName) // move to next state
            case _ =>
              context.log.info(s"$firstName: Sorry I am still eating")
              Behaviors.same
          }
        }
      }

    def apply(firstName: String): Behavior[PersonCommand] = idle(firstName)
  }

  def main(args: Array[String]): Unit = {
    // Actor system initialisation
    val system = ActorSystem[TypedIceCreamActors.PersonCommand](PersonCommand("Parent Person"), "first-system")

    val actor1: ActorRef[PersonCommand] = system.systemActorOf(PersonCommand.idle("Bob"), "Bob")
    val actor2: ActorRef[PersonCommand] = system.systemActorOf(PersonCommand.idle("Richard"), "Richard")
    val actor3: ActorRef[PersonCommand] = system.systemActorOf(PersonCommand.idle("John"), "John")

    // sending messages to the actor's mailbox, using Tell, sent async
    actor1 ! EatIceCream
    actor1 ! EatIceCream
    actor1 ! EatIceCream

    actor2 ! EatIceCream

    // can no longer drink beer as we are typed
    actor3 ! EatIceCream
  }
}
