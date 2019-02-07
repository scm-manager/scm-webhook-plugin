// @flow

import {ConfigurationBinder as cfgBinder} from "@scm-manager/ui-components";
import GlobalWebhookConfigurationComponent from "./GlobalWebhookConfigurationComponent";
import RepositoryWebhookConfigurationComponent from "./RepositoryWebhookConfigurationComponent";

cfgBinder.bindGlobal(
  "/webhook",
  "scm-webhook-plugin.nav-link",
  "webHookConfig",
  GlobalWebhookConfigurationComponent
);

cfgBinder.bindRepositorySetting(
  "/webhook",
  "scm-webhook-plugin.nav-link",
  "webHookConfig",
  RepositoryWebhookConfigurationComponent
);
