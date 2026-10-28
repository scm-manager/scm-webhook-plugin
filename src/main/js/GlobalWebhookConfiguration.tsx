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

import React, { FC } from "react";
import MasterDetailsView from "./MasterDetailsView";
import { useTranslation } from "react-i18next";
import { useIndexLink } from "@scm-manager/ui-api";
import { Loading } from "@scm-manager/ui-core";

const GlobalWebhookConfiguration: FC = (props) => {
  const [t] = useTranslation("plugins");
  const link = useIndexLink("webHookConfig");

  if (!link) {
    return <Loading />;
  }

  return (
    <MasterDetailsView
      title={t("scm-webhook-plugin.config.title")}
      baseRoute="/admin/settings/webhook"
      link={link}
      {...props}
    />
  );
};

export default GlobalWebhookConfiguration;
