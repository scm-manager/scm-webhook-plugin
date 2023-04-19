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
import React, { FC, useMemo, useState } from "react";
import { WebhookConfiguration } from "./extensionPoints";
import { SelectField } from "@scm-manager/ui-forms";
import { useHistory } from "react-router-dom";
import { Subtitle } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";

type Props = {
  webhookMap: Record<string, WebhookConfiguration["type"]>;
  typeOptions: any;
  onCreate: (name: string, configuration: unknown) => Promise<unknown>;
  baseRoute: string;
};

const CreatePage: FC<Props> = ({ webhookMap, onCreate, typeOptions, baseRoute }) => {
  const [t] = useTranslation("plugins");
  const [type, setType] = useState("");
  const extension = useMemo(() => webhookMap[type], [type, webhookMap]);
  const history = useHistory();

  const options = useMemo(() => [{ label: "", value: "" }, ...typeOptions], [typeOptions]);

  return (
    <>
      <Subtitle>{t("scm-webhook-plugin.config.createSubtitle")}</Subtitle>
      <SelectField label={t("scm-webhook-plugin.config.type.label")} value={type} onChange={event => setType(event.target.value)} options={options} />
      {type ? (
        <>
          <hr />
          <Form
            onSubmit={formValue => onCreate(type, formValue).then(() => history.push(baseRoute))}
            defaultValues={extension.defaultConfiguration}
            translationPath={["plugins", "scm-webhook-plugin.config.form.webhooks.configuration"]}
          >
            {({ watch }) => React.createElement(extension.FormComponent, { webhook: watch() })}
          </Form>
        </>
      ) : null}
    </>
  );
};

export default CreatePage;
