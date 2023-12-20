import "source-map-support/register";
import { GuRoot } from "@guardian/cdk/lib/constructs/root";
import type { App } from "aws-cdk-lib";
import { GoogleSearchIndexChecker } from "../lib/google-search-index-checker";

const app: App = new GuRoot();
new GoogleSearchIndexChecker(app, "GoogleSearchIndexChecker-PROD", {
    stack: "ophan",
    stage: "PROD",
    env: { region: "eu-west-1" },
    withBackup: true
});
