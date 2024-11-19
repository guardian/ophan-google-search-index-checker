package ophan.google.index.checker

import ophan.google.index.checker.GoogleSearchService.resultMatches
import upickle.default.*

import java.net.URI
import java.net.http.{HttpClient, HttpRequest}
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future, blocking}
import scala.util.Try
import ophan.google.index.checker.model.{CheckReport, ContentSummary}

case class DerivedStructData(link: String)

object DerivedStructData {
  implicit val rw: ReadWriter[DerivedStructData] = macroRW
}

case class Document(derivedStructData: DerivedStructData)

object Document {
  implicit val rw: ReadWriter[Document] = macroRW
}

case class SearchResult(document: Document)

object SearchResult {
  implicit val rw: ReadWriter[SearchResult] = macroRW
}

case class SearchResponse(results: List[SearchResult])

object SearchResponse {
  implicit val rw: ReadWriter[SearchResponse] = macroRW
}

class GoogleSearchService(
                           projectId: String,
                           location: String,
                           dataStoreId: String,
                           apiKey: String
                         )(implicit ec: ExecutionContext) {

  private val baseUrl = s"https://discoveryengine.googleapis.com/v1/projects/$projectId/locations/$location/dataStores/$dataStoreId/servingConfigs/default_config:searchLite"

  private val httpClient = HttpClient.newHttpClient()

  def contentAvailabilityInGoogleIndex(content: ContentSummary): Future[CheckReport] = Future {
    blocking {
      val requestBody = ujson.Obj(
        "query" -> content.reliableSearchTerm,
        "pageSize" -> 10
      )

      val request = HttpRequest.newBuilder()
        .uri(URI.create(s"$baseUrl?key=$apiKey"))
        .header("Content-Type", "application/json")
        .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36")
        .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
        .build()

      CheckReport(Instant.now, accessGoogleIndex = Try {
        val response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString())

        val searchResponse = read[SearchResponse](response.body())

        searchResponse.results.exists { result =>
          resultMatches(content.webUrl, result)
        }
      })
    }
  }
}

object GoogleSearchService {
  def resultMatches(webUrl: URI, result: SearchResult): Boolean = {
    val resultUri = URI.create(result.document.derivedStructData.link)
    resultUri.getHost == webUrl.getHost && resultUri.getPath == webUrl.getPath
  }
}