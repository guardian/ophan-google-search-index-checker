package ophan.google.index.checker.model

import com.gu.contentapi.client.model.v1.Content

import java.net.{URI, URLEncoder}
import java.nio.charset.StandardCharsets
import java.nio.charset.StandardCharsets.UTF_8
import java.time.Clock.systemUTC
import java.time.temporal.ChronoUnit
import java.time.temporal.ChronoUnit.MINUTES
import java.time.{Clock, Duration, Instant}
import scala.math.Ordering.Implicits._

case class ContentSummary(
  id: String,
  firstPublished: Instant,
  webUrl: URI,
  webTitle: String
) {
  /**
   * This string should be something that, when you type it into Google, you
   * reliably should get this content as one of the top hits. The headline of
   * the article would be one candidate for the value, but the headlines can
   * contain characters that are difficult to escape, eg quotes & double-quotes.
   *
   * We previously used the quoted path of the webUrl, but that failed
   * to find results when the path contained double-dashes (eg "england--bond") like
   * https://www.theguardian.com/business/live/2022/oct/11/bank-of-england--bond-markets-gilts-uk-unemployment-ifs-spending-cuts-imf-outlook-business-live
   *
   * We also tried searching with just the full URL, as recommended by google: "For a missing page:
   * Search Google for the full URL of your page." - https://support.google.com/webmasters/answer/7474347?hl=en
   * however we found cases on other sites where this didn't work:
   *  - https://www.nytimes.com/interactive/2021/us/martin-indiana-covid-cases.html
   *  - https://www.nytimes.com/video/middle-east
   *
   * From testing, we've discovered you can search with the full webUrl, and the path in quotes,
   * and this seems to cover the situations where each of the methods failed individually.
   *
   */
  val reliableSearchTerm: String = s"${webUrl.toString} \"${webUrl.getPath}\""

  val googleSearchUiUrl: URI = URI.create(s"https://www.google.com/search?q=${URLEncoder.encode(reliableSearchTerm, UTF_8)}")

  val ophanUrl: URI = URI.create(s"https://dashboard.ophan.co.uk/info?capi-id=$id")

  def timeSinceUrlWentPublic()(implicit clock: Clock = systemUTC): Duration =
    Duration.between(firstPublished, clock.instant())

  def shouldBeCheckedNowGivenExisting(availabilityRecord: AvailabilityRecord)(implicit clock: Clock): Boolean = {
    !availabilityRecord.contentHasBeenFound &&
      !availabilityRecord.missing.maxOption.exists(_ > clock.instant().minus(3, MINUTES))
  }
}

object ContentSummary {
  def from(content: Content): Option[ContentSummary] = for {
    fields <- content.fields
    firstPublished <- fields.firstPublicationDate
  } yield ContentSummary(content.id, Instant.ofEpochMilli(firstPublished.dateTime), URI.create(content.webUrl), content.webTitle)

}