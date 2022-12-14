package ophan.google.index.checker

import com.google.api.client.http.HttpRequest
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.customsearch.v1.CustomSearchAPI
import com.google.api.services.customsearch.v1.model.{Result, Search}
import ophan.google.index.checker.GoogleSearchService.resultMatches
import ophan.google.index.checker.model.{CheckReport, ContentSummary}

import java.net.URI
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future, blocking}
import scala.jdk.CollectionConverters._
import scala.util.Try

class GoogleSearchService(
  apiKey: String
)(implicit
  ec: ExecutionContext
) {
  val search =
    new CustomSearchAPI.Builder(
      new NetHttpTransport,
      new GsonFactory,
      (request: HttpRequest) => {
        request.getHeaders.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36")

      }
    ).setApplicationName("search-index-checker").build()

  def contentAvailabilityInGoogleIndex(content: ContentSummary): Future[CheckReport] = Future { blocking {
      val listRequest = search.cse.siterestrict.list()
        .setKey(apiKey)
        .setCx("415ef252844d240a7") // see https://programmablesearchengine.google.com/controlpanel/all
        .setQ(content.reliableSearchTerm)
      CheckReport(Instant.now, accessGoogleIndex = Try(listRequest.execute()).map { googleSearchResponse =>
        findContentMatchInGoogleSearchResponse(googleSearchResponse, content.webUrl).isDefined
      })
    }
  }

  def findContentMatchInGoogleSearchResponse(googleSearchResponse: Search, webUrl: URI): Option[com.google.api.services.customsearch.v1.model.Result] = {
    Option(googleSearchResponse.getItems).flatMap { items =>
      items.asScala.toSeq.find { result => resultMatches(webUrl, result) }
    }
  }

}

object GoogleSearchService {
  def resultMatches(webUrl: URI, result: Result): Boolean = Option(result.getLink).exists { link =>
    val resultUri = URI.create(link)
    resultUri.getHost == webUrl.getHost && resultUri.getPath == webUrl.getPath
  }
}
