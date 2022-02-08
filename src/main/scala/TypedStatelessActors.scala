import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors

import scala.concurrent.duration.DurationInt

object TypedStatelessActors {

  trait SimpleThing
  case object EatChocolate extends SimpleThing
  case object WashDishes extends SimpleThing
  case object LearnAkka extends SimpleThing

  // State - mutable piece of data
  val emotionalMutableActor: Behavior[SimpleThing] = Behaviors.setup { context => // Behaviours.setup usually used for stateful setups
    // spin up the actor state
    var happieness = 0 // to help create a stateful actor

    // behavior of the actor
    Behaviors.receiveMessage {
      case EatChocolate =>
        context.log.info(s"($happieness) Eating chocolate, getting a shot of dopamine")
        happieness += 1
        Behaviors.same // Keep the exact same behavior the same to the next message this Actor will receive
      case WashDishes =>
        context.log.info(s"($happieness) Doing a chore, womp, womp...")
        happieness -= 2
        Behaviors.same
      case LearnAkka =>
        context.log.info(s"($happieness) Learning Akka, this is cool!")
        happieness += 100
        Behaviors.same
      case _ =>
        context.log.info(s"($happieness) Received something I don't know")
        Behaviors.same
    }
  }

  // stateless setup - not truly recursive in Akka, it returns immediately with a new Behavior object
  def emotionalFunctionalActor(happieness: Int = 0): Behavior[SimpleThing] = Behaviors.receive { (context, message) =>
    message match {
      case EatChocolate =>
        context.log.info(s"($happieness) Eating chocolate, getting a shot of dopamine")
        emotionalFunctionalActor(happieness + 1) // new behaviour
      case WashDishes =>
        context.log.info(s"($happieness) Doing a chore, womp, womp...")
        emotionalFunctionalActor(happieness - 2)
      case LearnAkka =>
        context.log.info(s"($happieness) Learning Akka, this is cool!")
        emotionalFunctionalActor(happieness + 100)
      case _ =>
        context.log.info(s"($happieness) Received something I don't know")
        Behaviors.same
    }
  }

  def main(args: Array[String]): Unit = {
    // val emotionalActorSystem = ActorSystem(emotionalMutableActor, "EmotionalSystem") // stateful version
    val emotionalActorSystem = ActorSystem(emotionalFunctionalActor(), "EmotionalSystem") // stateless version

    emotionalActorSystem ! EatChocolate
    emotionalActorSystem ! EatChocolate
    emotionalActorSystem ! EatChocolate
    emotionalActorSystem ! WashDishes
    emotionalActorSystem ! LearnAkka

    Thread.sleep(1.seconds.toMillis) // would use some external call back to shut down your system in production
    emotionalActorSystem.terminate()
  }
}
