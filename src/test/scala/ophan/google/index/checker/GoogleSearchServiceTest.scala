package ophan.google.index.checker

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import upickle.default._

import java.net.URI

class GoogleSearchServiceTest extends AnyFlatSpec with Matchers {
  it should "cope when the google result has tonnes of extra url params" in {
    val googleResultLink =
      "https://www.theguardian.com/food/2022/sep/15/korean-hotdogs-k-dogs-sausage-cheese-fast-food?utm_term=Autofeed&CMP=twt_gu&utm_medium&utm_source=Twitter"

    val canonicalPageUrl = URI.create("https://www.theguardian.com/food/2022/sep/15/korean-hotdogs-k-dogs-sausage-cheese-fast-food")

    GoogleSearchService.resultMatches(
      canonicalPageUrl,
      SearchResult(
        Document(
          DerivedStructData(googleResultLink)
        )
      )
    ) shouldBe true
  }

  it should "handle empty search results" in {
    val emptyResponse = """{"attributionToken": "token", "summary": {}}"""
    val searchResponse = read[SearchResponse](emptyResponse)
    searchResponse.results shouldBe empty
  }

  it should "not match different paths" in {
    val url1 = URI.create("https://www.theguardian.com/path1")
    val url2 = URI.create("https://www.theguardian.com/path2")

    GoogleSearchService.resultMatches(
      url1,
      SearchResult(Document(DerivedStructData(url2.toString)))
    ) shouldBe false
  }

  it should "parse a search response with results" in {
    val response = """
      {
        "results": [
          {
            "document": {
              "derivedStructData": {
                "link": "https://www.theguardian.com/article1"
              }
            }
          }
        ]
      }
    """
    val searchResponse = read[SearchResponse](response)
    searchResponse.results should have length 1
    searchResponse.results.head.document.derivedStructData.link shouldBe "https://www.theguardian.com/article1"
  }
}