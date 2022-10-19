package ophan.google.index.checker

import com.gu.contentapi.client.ContentApiClient
import com.gu.contentapi.client.model.SearchQuery
import ophan.google.index.checker.model.ContentSummary

import java.time.Instant
import java.time.temporal.ChronoUnit.HOURS
import scala.concurrent.{ExecutionContext, Future}

class RecentContentService(
  client: com.gu.contentapi.client.ContentApiClient
)(implicit 
  ec: ExecutionContext
) {
  
  def query(): SearchQuery = ContentApiClient.search
    .orderBy("newest").useDate("first-publication")
    .showFields("firstPublicationDate")
    .fromDate(Some(Instant.now().minus(4, HOURS)))
    .tag("-tone/sponsoredfeatures,-type/crossword,-extra/extra,-tone/advertisement-features") // copied from https://github.com/guardian/frontend/blob/9a69dd4fdc2f09cbc2459dedd933b03215d2cad7/applications/app/services/NewsSiteMap.scala#L65
    .pageSize(100)
  
  def fetchRecentContent(): Future[Seq[ContentSummary]] = client.getResponse(query()) map { resp =>
    val results = resp.results.toSeq
    println(s"Found ${results.size}")
    results.flatMap(ContentSummary.from)
  }

}
