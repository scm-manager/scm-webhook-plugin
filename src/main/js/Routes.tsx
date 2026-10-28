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

import { FC } from "react";
import { useTranslation } from "react-i18next";
import { NavLink } from "@scm-manager/ui-components";
import React from "react";
import GlobalWebhookConfiguration from "./GlobalWebhookConfiguration";
import { useRepositoryFromParams } from "@scm-manager/ui-core";
import RepositoryWebhookConfigurationComponent from "./RepositoryWebhookConfiguration";

export const AdminNavLink: FC = () => {
  const [t] = useTranslation("plugins");

  return <NavLink to="/admin/settings/webhook" label={t("scm-webhook-plugin.nav-link")} />;
};

export const AdminRoute = () => ({
  path: "webhook",
  children: [
    {
      path: "*",
      element: <GlobalWebhookConfiguration />,
    },
  ],
});

export const RepositoryNavLink: FC = () => {
  const [t] = useTranslation("plugins");
  const repository = useRepositoryFromParams();

  return (
    <NavLink
      to={`/repo/${repository.namespace}/${repository.name}/settings/webhook`}
      label={t("scm-webhook-plugin.nav-link")}
    />
  );
};

export const RepositoryRoute = () => ({
  path: "webhook",
  children: [
    {
      path: "*",
      element: <RepositoryWebhookConfigurationComponent />,
    },
  ],
});
