package ophan.google.index.checker

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.customsearch.v1.CustomSearchAPI
import com.google.api.services.customsearch.v1.model.{Result, Search}
import com.gu.contentapi.client.model.SearchQuery
import com.gu.contentapi.client.model.v1.Content
import com.gu.contentapi.client.{ContentApiClient, GuardianContentClient}
import ophan.google.index.checker.logging.Logging
import ophan.google.index.checker.model.ContentSummary
import software.amazon.awssdk.services.s3.model.PutObjectRequest

import java.net.{URI, URLEncoder}
import java.nio.charset.StandardCharsets
import java.nio.charset.StandardCharsets.UTF_8
import java.time.Instant
import java.util
import scala.collection.mutable
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success}
import scala.concurrent.duration._

object Lambda extends Logging {

  val recentContentService: RecentContentService = {
    val capiKey: String = fetchKeyFromParameterStore("/Ophan/dashboard-es7/CODE/ContentApiKey")
    new RecentContentService(new GuardianContentClient(capiKey))
  }

  val googleSearchService: GoogleSearchService = {
    val apiKey = fetchKeyFromParameterStore("/Ophan/Google/CustomSearch/ApiKey")
    new GoogleSearchService(apiKey)
  }

  private def fetchKeyFromParameterStore(value: String): String =
    AWS.SSM.getParameter(_.withDecryption(true).name(value)).parameter.value

  /*
     * Logic handler
     */
  def go(): Unit = {
    val eventual = for {
      contentSummaries <- recentContentService.fetchRecentContent()
      allAvailability <- Future.traverse(contentSummaries)(googleSearchService.contentAvailabilityInGoogleIndex)
    } yield {
      val successfulIndexStateChecks = allAvailability.count(_.indexPresenceByTime.nonEmpty)
      println(s"Search Index checks: $successfulIndexStateChecks/${contentSummaries.size} accessed Google's API without error")
      for {
        worryinglyAbsentContent <- allAvailability.filter(_.contentIsCurrentlyWorryinglyAbsentFromGoogle())
      } {
        val content = worryinglyAbsentContent.contentSummary
        println(s"${content.timeSinceUrlWentPublic().toMinutes}mins ${content.ophanUrl}\n${content.googleSearchUiUrl}\n")
      }
      allAvailability
    }

    Await.result(eventual , 10.seconds)
  }




  /*
   * Lambda's entry point
   */
  def handler(lambdaInput: ScheduledEvent, context: Context): Unit = {
    go()
  }

}
