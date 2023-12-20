import "source-map-support/register";
import {GuRootExperimental} from "@guardian/cdk/lib/experimental/constructs/root";
import type { App } from "aws-cdk-lib";
import { GoogleSearchIndexChecker } from "../lib/google-search-index-checker";

const app: App = new GuRootExperimental();
new GoogleSearchIndexChecker(app, "GoogleSearchIndexChecker-PROD", {
    stack: "ophan",
    stage: "PROD",
    env: { region: "eu-west-1" },
    withBackup: true
});
