/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import { WebhookConfiguration } from "./extensionPoints";
import { Repository } from "@scm-manager/ui-types";
import { binder } from "@scm-manager/ui-extensions";
import SimpleWebHookConfigurationForm, { SimpleWebHookConfiguration } from "./SimpleWebHookConfigurationForm";
import SimpleWebhookOverviewCardTop from "./SimpleWebhookOverviewCardTop";
import SimpleWebhookOverviewCardBottom from "./SimpleWebhookOverviewCardBottom";
import { AdminNavLink, AdminRoute, RepositoryNavLink, RepositoryRoute } from "./Routes";

binder.bind<WebhookConfiguration<SimpleWebHookConfiguration>>("webhook.configuration", {
  name: "SimpleWebHook",
  defaultConfiguration: {
    urlPattern: "",
    executeOnEveryCommit: false,
    sendCommitData: false,
    method: "AUTO",
    headers: [],
  },
  FormComponent: SimpleWebHookConfigurationForm,
  OverviewCardTop: SimpleWebhookOverviewCardTop,
  OverviewCardBottom: SimpleWebhookOverviewCardBottom,
});

const webhookSupportedPredicate = (props: { repository: Repository }) => {
  return props.repository && !!props.repository._links.webHookConfig;
};

binder.bind("admin.setting", AdminNavLink);

binder.bind("admin.settings.route", AdminRoute);

binder.bind("repository.setting", RepositoryNavLink, webhookSupportedPredicate);

binder.bind("repo-config.route", RepositoryRoute);

export { WebhookConfiguration } from "./extensionPoints";
