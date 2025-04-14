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

import { Form } from "@scm-manager/ui-forms";
import React, { FC, useMemo } from "react";
import { WebhookConfiguration } from "./extensionPoints";
import { WebHookConfiguration } from "./types";
import { useHistory, useParams } from "react-router-dom";
import { Subtitle, useDocumentTitle } from "@scm-manager/ui-core";
import { useTranslation } from "react-i18next";
import { Button } from "@scm-manager/ui-buttons";
import { useRepositoryContext } from "@scm-manager/ui-api";

type Props = {
  webhookMap: Record<string, WebhookConfiguration["type"]>;
  webhooks: WebHookConfiguration[];
  onUpdate: (webhook: WebHookConfiguration) => Promise<unknown>;
  onDelete: (webhook: WebHookConfiguration) => Promise<unknown>;
  baseRoute: string;
};

const EditPage: FC<Props> = ({ webhookMap, webhooks, onUpdate, onDelete, baseRoute }) => {
  const [t] = useTranslation("plugins");
  const { id } = useParams<{ id: string }>();
  const webhook = useMemo(() => webhooks.find((wh) => wh.id === id), [id, webhooks]);
  const extension = useMemo(() => webhookMap[webhook.name], [webhook.name, webhookMap]);
  const history = useHistory();
  const repository = useRepositoryContext();

  // We can't use useDocumentTitleForRepository here since this would mean conditional hooks.
  // TODO Needs to be fixed with a future SCM-Core version.
  useDocumentTitle(
    t("scm-webhook-plugin.config.editSubtitle", { name: webhook.name })
  );

  return (
    <>
      <Subtitle>{t("scm-webhook-plugin.config.editSubtitle", { name: webhook.name })}</Subtitle>
      <Form
        onSubmit={(formValue) => onUpdate({ ...webhook, configuration: formValue }).then(() => history.push(baseRoute))}
        defaultValues={webhook.configuration}
        translationPath={["plugins", "scm-webhook-plugin.config.form.webhooks.configuration"]}
      >
        {({ watch }) => React.createElement(extension.FormComponent, { webhook: watch() })}
      </Form>
      <hr />
      <div className="level-right">
        <Button variant="signal" onClick={() => onDelete(webhook).then(() => history.push(baseRoute))}>
          {t("Delete webhook")}
        </Button>
      </div>
    </>
  );
};

export default EditPage;
