import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object TypedStatelessActors {

  trait SimpleThing
  case object EatChocolate extends SimpleThing
  case object WashDishes extends SimpleThing
  case object LearnAkka extends SimpleThing

  // State - mutable piece of data

  val emotionalMutableActor: Behavior[SimpleThing] = Behaviors.setup { context =>
    // spin up the actor state
    var happieness = 0



    // behavior of the actor
//    Behaviors.receiveMessage()
    ???
  }

  def main(args: Array[String]): Unit = {

  }
}
