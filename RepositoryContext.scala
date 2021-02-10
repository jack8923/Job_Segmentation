package Job_Segmentation

import akka.actor.ActorSystem

object RepositoryContext {
  lazy val actorSystem = ActorSystem("RepositoryContext")
  lazy val scheduler = actorSystem.scheduler
  implicit lazy val executionContext = actorSystem.dispatcher
}