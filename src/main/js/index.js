// @flow

import { ConfigurationBinder as cfgBinder } from "@scm-manager/ui-components";
import GlobalWebHookConfiguration from "./GlobalWebHookConfiguration";

cfgBinder.bindGlobal(
  "/webhook",
  "scm-webhook-plugin.nav-link",
  "webHookConfig",
  GlobalWebHookConfiguration
);
