package Job_Segmentation

import org.mongodb.scala.bson.BsonDocument

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.duration._
import akka_tutorial.RepositoryContext._
import net.liftweb.json
import net.liftweb.json.DefaultFormats
import org.mongodb.scala.model.Filters._

import java.time.LocalDateTime
import org.mongodb.scala.model.Sorts._


object Repository{

  case class Client(id: String, name: String, inboundFeedUrl: String, jobGroups: List[JobGroup])

  case class Rule(field: String, operator: String, value: String)

  case class JobGroup(id: String, rules: List[Rule], sponsoredPublishers: List[Publisher], createdOn: LocalDateTime)

  case class Publisher(id: String, name: String, isActive: Boolean, clientId: String, outboundFileName: Option[String])

  case class Job(title: String,
                 company: String,
                 city: String,
                 state: String,
                 country: String,
                 text: Option[String],
                 description: String,
                 referencenumber: Int,
                 url: String,
                 date: String,
                 category: String,
                 department: Option[String],
                )

  case class PublisherJob(publisherName : String, assignedJobs : List[Job])

  implicit val formats: DefaultFormats.type = DefaultFormats

  val entitySrc = scala.io.Source.fromFile("/Users/rohitchandwani/Desktop/10-jobs.json")
  val entityStr = try entitySrc.mkString finally entitySrc.close()
  val jobSeq = json.parse(entityStr).extract[List[Job]]

  /// populate Jobs - jobSeq into database
  Await.result(db.jobs.insertMany(jobSeq).toFuture(), 10 seconds)

}

trait Repository {

  import Repository._

  def getAllGroups(): Future[Seq[JobGroup]] = {
    db.jobGroups.find().sort(descending("createdOn")).toFuture()
  }

  def getAllJobs(): Future[Seq[Job]] = {
    db.jobs.find().toFuture()
  }

  def getAllRules(): Future[Seq[Rule]] = {
    db.rules.find().toFuture()
  }

  def getAllPublishers(): Future[Seq[Publisher]] = {
    db.publishers.find().toFuture()
  }

  def newJob(job : Job) = {
    db.jobs.insertOne(job).toFuture()
  }

  def updateJob(id : Int, job : Job) = {
    val oldJob = BsonDocument(("referencenumber" , id))
    db.jobs.findOneAndReplace(oldJob, job).toFuture()
  }
  
  def getPublisherByName(name : String) = {
    db.publishers.find(equal("name",name)).first().toFuture()
  }


  def updatePublisher(publisher : Publisher, fileName : String) = {
    val currPublisherBson = BsonDocument(("id" , publisher.id))
    val updatedPublisher = Publisher(publisher.id,publisher.name,true,publisher.clientId,Some(fileName))
//    db.publishers.findOneAndReplace(currPublisherBson, updatedPublisher).toFuture()
    db.publishers.updateOne(equal("name",publisher.name),set("outboundFileName",fileName)).toFuture()
  }

}

object db{

  import Job_Segmentation.Repository._
  import org.bson.codecs.configuration.CodecRegistries
  import org.bson.codecs.configuration.CodecRegistries._
  import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
  import org.mongodb.scala.bson.codecs.Macros._
  import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}

  private val codecs = fromProviders(classOf[Client],
    classOf[Rule],
    classOf[JobGroup],
    classOf[Publisher],
    classOf[Job])

  private val codecReg = fromRegistries(codecs,
    DEFAULT_CODEC_REGISTRY)

  private val dataB:  MongoDatabase = MongoClient().getDatabase("Job_Segmentation").withCodecRegistry(codecReg)

  val clients: MongoCollection[Client] = dataB.getCollection("Clients")
  val rules: MongoCollection[Rule] = dataB.getCollection("Rules")
  val jobGroups: MongoCollection[JobGroup] = dataB.getCollection("JobGroups")
  val publishers: MongoCollection[Publisher] = dataB.getCollection("Publishers")
  val jobs: MongoCollection[Job] = dataB.getCollection("Jobs")

}
