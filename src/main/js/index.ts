/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import { WebhookConfiguration } from "./extensionPoints";
import { ConfigurationBinder as cfgBinder } from "@scm-manager/ui-components";
import GlobalWebhookConfiguration from "./GlobalWebhookConfiguration";
import RepositoryWebhookConfigurationComponent from "./RepositoryWebhookConfiguration";
import { binder } from "@scm-manager/ui-extensions";
import SimpleWebHookConfigurationForm, { SimpleWebHookConfiguration } from "./SimpleWebHookConfigurationForm";

binder.bind<WebhookConfiguration<SimpleWebHookConfiguration>>("webhook.configuration", {
  name: "SimpleWebHook",
  defaultConfiguration: {
    urlPattern: "https://example.com/",
    executeOnEveryCommit: false,
    sendCommitData: false,
    method: "AUTO",
    headers: []
  },
  FormComponent: SimpleWebHookConfigurationForm
});

cfgBinder.bindGlobal("/webhook", "scm-webhook-plugin.nav-link", "webHookConfig", GlobalWebhookConfiguration);

cfgBinder.bindRepositorySetting(
  "/webhook",
  "scm-webhook-plugin.nav-link",
  "webHookConfig",
  RepositoryWebhookConfigurationComponent
);

export { WebhookConfiguration } from "./extensionPoints";
