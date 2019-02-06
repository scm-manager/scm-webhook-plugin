// @flow

import {ConfigurationBinder as cfgBinder} from "@scm-manager/ui-components";
import WebHookConfigurationComponent from "./WebHookConfigurationComponent";

cfgBinder.bindGlobal(
  "/webhook",
  "scm-webhook-plugin.nav-link",
  "webHookConfig",
  WebHookConfigurationComponent
);

cfgBinder.bindRepositorySetting(
  "/webhook",
  "scm-webhook-plugin.nav-link",
  "webHookConfig",
  WebHookConfigurationComponent
);
