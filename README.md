# Ophan Google Search Index Checker

_Checking if Guardian content appears in Google search_

Guardian staff: For more background on this issue, see this Guardian-scoped [document](https://docs.google.com/document/d/1lWOM-6mkGaPsI0YpF2HjrkI--6X1AlinaeIOhfmCy4I/edit?hl=en-GB&forcehl=1).

## Steps performed by the checker

1. Hits the Guardian's Content API (CAPI) for stories published in the last few hours.
2. Hits [Discovery Engine API (service is named Google Vertex Agent Builder)](https://cloud.google.com/generative-ai-app-builder/docs/reference/rest)
   to check if our stories are available in Google search.
   [API Consumption](https://console.cloud.google.com/gen-app-builder/monitoring?inv=1&invt=AbigZA&project=ophan-reborn-2017) &
   [Cost 💰💰💰](https://console.cloud.google.com/apis/api/customsearch.googleapis.com/cost?project=ophan-reborn-2017)
   for this can be monitored in the Google Cloud console.
3. Stores whether each article is available (or not) in an AWS DynamoDb table.

## Vertex Agent Builder

When setting up search functionality in the GCP Agent Builder, we need to create both an app and a dataStore in the Agent Builder for each website we want to search (in this case theguardian.com, while our [separate indexing system](https://github.com/guardian/google-search-indexing-observatory/tree/main) handles BBC, DailyMail, and NYT).
While GCP's interface suggests this process creates a new search engine with its own database, this isn't actually what happens. Instead, it creates a filtered view of Google Search results, limited to the specific website URL we specify.
_Note:_ Even though our code doesn't directly reference the App ID, you must still create both the app and the dataStore for each website - creating just the dataStore isn't sufficient and leads to API errors.

## Running the Checker locally

### Pre-requisites

These mostly match [the pre-requisites for running Ophan locally](https://github.com/guardian/ophan/blob/main/docs/developing-ophan/running-ophan-locally.md#pre-requisites) -
specifically Java 11 & `sbt`, but also especially the requirement to have
[`ophan` AWS credentials](https://janus.gutools.co.uk/credentials?permissionId=ophan-dev)
from [Janus](https://janus.gutools.co.uk/).

### Running the Lambda locally

Execute this on the command line:

```bash
$ sbt run
```
