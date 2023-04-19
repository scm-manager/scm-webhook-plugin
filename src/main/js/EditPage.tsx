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

import { Form } from "@scm-manager/ui-forms";
import React, { FC, useMemo } from "react";
import { WebhookConfiguration } from "./extensionPoints";
import { WebHookConfiguration } from "./types";
import { useHistory, useParams } from "react-router-dom";
import { Subtitle } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { Button } from "@scm-manager/ui-buttons";

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
  const webhook = useMemo(() => webhooks.find(wh => wh.id === id), [id, webhooks]);
  const extension = useMemo(() => webhookMap[webhook.name], [webhook.name, webhookMap]);
  const history = useHistory();

  return (
    <>
      <Subtitle>{t("scm-webhook-plugin.config.editSubtitle", { name: webhook.name})}</Subtitle>
      <Form
        onSubmit={formValue => onUpdate({ ...webhook, configuration: formValue }).then(() => history.push(baseRoute))}
        defaultValues={webhook.configuration}
        translationPath={["plugins", "scm-webhook-plugin.config.form.webhooks.configuration"]}
      >
        {({ watch }) => React.createElement(extension.FormComponent, { webhook: watch() })}
      </Form>
      <hr/>
      <div className="level-right">
        <Button variant="signal" onClick={() => onDelete(webhook).then(() => history.push(baseRoute))}>
          {t("Delete webhook")}
        </Button>
      </div>
    </>
  );
};

export default EditPage;
