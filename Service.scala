package Job_Segmentation

import Job_Segmentation.Repository._
import Job_Segmentation.RepositoryContext._
import scala.util.parsing.json.JSONObject
import scala.concurrent.Future
import scala.util.{Failure, Success}
import com.fasterxml.jackson.databind.JsonNode
import spray.json.DefaultJsonProtocol
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

//import Job_Segmentation.PublisherJobJson._
//
//object PublisherJobJson extends SprayJsonSupport with DefaultJsonProtocol{
//  implicit val jobFormat = jsonFormat12(Job.apply)
//  implicit val publisherJobFormat = jsonFormat2(PublisherJob.apply)
//}

object Service extends App with Repository {

  def jobToMap(job : Job): Map[String, Any] =
    (Map[String, Any]() /: job.getClass.getDeclaredFields) {
      (a, f) =>
        f.setAccessible(true)
        a + (f.getName -> f.get(job))
    }

  def isJobRuleMatch(jobConvertedToMap : Map[String, Any], rule : Rule) = {
    if(rule.operator == "equals") {
      jobConvertedToMap(rule.field) == rule.value
    }
    else if(rule.operator == "startsWith") {
      jobConvertedToMap(rule.field).toString.startsWith(rule.value)
    }
    else if((rule.operator == "endsWith")){
      jobConvertedToMap(rule.field).toString.endsWith(rule.value)
    }
    else{
      jobConvertedToMap(rule.field).toString.contains(rule.value)
    }

  }

  def isMatch(job: Job, group: JobGroup): Boolean = {
    val jobConvertedToMap = jobToMap(job)
    val matchingRules = group.rules.filter(rule => isJobRuleMatch(jobConvertedToMap,rule))
    matchingRules == group.rules
  }

  def findMatchingGroup(job : Job): Future[JobGroup] = {
    for{
      allJobGroups <- getAllGroups()
      jobGroup = (allJobGroups.find(group => isMatch(job, group)))
    } yield jobGroup.get
  }

  def buildGroupWithJobsSeq(jobs: Seq[Job]): Future[Seq[(JobGroup, Job)]] = {
    val result = jobs.map{job =>
      val group = for {
        groupFound <- findMatchingGroup(job)
      } yield groupFound
      group.map(group => (group,job))
    }
    Future.sequence(result)
  }

  def buildGroupAndJobMap(seq : Seq[(JobGroup, Job)]): Future[Map[JobGroup, Seq[Job]]] = Future{
    seq.map(value => value._1 -> seq.map(va => if(va._1 == value._1) va._2 else null)).toMap
  }


  def getGroupMap(): Future[Map[JobGroup, Seq[Job]]] = {
    for{
      allJobs <- getAllJobs()

      seqOfGroupWithJobs <- buildGroupWithJobsSeq(allJobs)
      groupJobMap <- buildGroupAndJobMap(seqOfGroupWithJobs)
    } yield groupJobMap
  }


  def buildPublisherAndJobMap(groupJobSeqMap: Map[JobGroup, Seq[Job]], publishers : Seq[Publisher], publisherJobGroupMap : Map[Publisher, Seq[JobGroup]]) = Future{
    publishers.map{publisher =>
      val publisherAssociatedGroups = publisherJobGroupMap(publisher)
      (publisher, publisherAssociatedGroups.filter(group => groupJobSeqMap.contains(group)).flatMap(groupJobSeqMap(_)))
    }
  }

  def getPublisherAndJobGroupMap(publishers : Seq[Publisher], groups : Seq[JobGroup]): Future[Map[Publisher, Seq[JobGroup]]] = Future{
    publishers.map{publisher =>
      publisher -> groups.filter(group => group.sponsoredPublishers.contains(publisher))
    }.toMap
  }

  def getPublisherAndJobSeq(): Future[Seq[(Publisher, Seq[Job])]] = {
    for{
      allPublishers <- getAllPublishers()
      allJobGroups <- getAllGroups()
      groupsJobMap <- getGroupMap()

      publisherJobGroupMap <- getPublisherAndJobGroupMap(allPublishers, allJobGroups)

      publisherJobMap <- buildPublisherAndJobMap(groupsJobMap, allPublishers, publisherJobGroupMap)
    } yield publisherJobMap
  }

  def getPulisherAndJobMap(): Future[Map[String, Seq[Job]]] = {
    for{
      publisherJobSeq <- getPublisherAndJobSeq()
      publisherJobMap = publisherJobSeq.map(value => value._1.name -> value._2.filter(job => job != null)).toMap
    } yield publisherJobMap
  }

  def getPublisherJobJSON() = {
    for{
      publisherJobMap <- getPulisherAndJobMap
    } yield JSONObject(publisherJobMap)
  }

  val publisherJobMap = getPulisherAndJobMap()

  publisherJobMap.onComplete{
    case Success(value) => println(value("Glassdoor").size)
    case Failure(exception) => println(exception.getMessage)
  }

//  val groupJobMap = getGroupMap()
//
//  groupJobMap.onComplete{
//    case Success(value) => println(value)
//    case Failure(exception) => println(exception.getMessage)
//  }

}
