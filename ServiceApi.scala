package Job_Segmentation

import Job_Segmentation.Repository._
import Job_Segmentation.Service._
import spray.json.DefaultJsonProtocol
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._

import java.time.LocalDateTime
import scala.util.{Failure, Success}

object ServiceApi {

  object ModelJson extends SprayJsonSupport with DefaultJsonProtocol{
    implicit val rulesJson = jsonFormat3(Rule.apply)
    implicit val jobJson = jsonFormat12(Job.apply)
    implicit val publisherJson = jsonFormat5(Publisher.apply)
  }

  import Job_Segmentation.ServiceApi.ModelJson._

  lazy val routes: Route = pathPrefix("segmentation"){
    get {
      path("jobs"){
        onComplete(getAllJobs()) {
          _ match {
            case Success(c) => complete(StatusCodes.OK, c)
            case Failure(e) => complete(StatusCodes.NotFound, "No employee found")
          }
        }
      } ~ path("map"){
        onComplete(getPulisherAndJobMap()) {
          _ match{
            case Success(m) => complete(StatusCodes.OK, m)
          }
        }
      }
    } ~ post {
      path("jobs") {
        entity(as[Job]) {job : Job =>
          onComplete(newJob(job)) {
            _ match {
              case Success(job) => complete(StatusCodes.OK, "job added")
              case Failure(exception) => complete(StatusCodes.InternalServerError, exception.getMessage)
            }
          }
        }
      }
    } ~ put {
      path(Segment){id =>
        entity(as[Job]) {job : Job =>
          onComplete(updateJob(id.toInt, job)) {
            _ match {
              case Success(s) => complete(StatusCodes.OK, "")
            }
          }
        }
      }
    }
  }
}