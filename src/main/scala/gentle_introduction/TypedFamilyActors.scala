package gentle_introduction

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import gentle_introduction.TypedIceCreamActors.{EatIceCream, PersonCommand}

// https://www.youtube.com/watch?v=RfKxoW8HQUY
object FamilyActors {
  sealed trait FamilyCommand

  final case class AddPerson(firstName: String) extends FamilyCommand

  final case class EatCake(firstName: String) extends FamilyCommand

  object FamilyCommand {
    def apply(): Behavior[FamilyCommand] = Behaviors.receive {
      case (context, AddPerson(firstName)) =>
        // create child
        context.spawn(PersonCommand(firstName), firstName)
        Behaviors.same

      case (context, EatCake(firstName)) =>
        // Try to find a child from this ID (firstName)
        // EatIceCream is sent to child with given name
        context.child(firstName)
          .map(_.asInstanceOf[ActorRef[PersonCommand]])
          .foreach(ref => ref ! EatIceCream)

        Behaviors.same
      case _ => Behaviors.unhandled
    }
  }

  def main(args: Array[String]): Unit = {
    val system = ActorSystem[FamilyCommand](FamilyCommand(), "family")

    system ! AddPerson("John")
    system ! AddPerson("Bob")
    system ! AddPerson("Richard")

    system ! EatCake("John")
    system ! EatCake("Bob")
    system ! EatCake("John")
    system ! EatCake("Bob")

    //    does not compile
    //    system ! "drink-beer"
  }
}
