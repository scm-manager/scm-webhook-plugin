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
