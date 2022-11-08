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
import React, { FC, useState } from "react";
import { AddButton, DropDown } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { useBinder, ExtensionPointDefinition } from "@scm-manager/ui-extensions";

type Props = {
  readOnly: boolean;
  onAdd: (webHookName: string) => void;
};

export type WebHookConfiguration = ExtensionPointDefinition<
  "webhook.configurations",
  {
    name: string;
    defaultConfiguration: unknown;
  }
>;

const AddWebHookButton: FC<Props> = ({ readOnly, onAdd }) => {
  const { t } = useTranslation("plugins");
  const binder = useBinder();
  const [selectedWebHook, setSelectedWebHook] = useState(0);

  const availableWebHooks = binder.getExtensions<WebHookConfiguration>("webhook.configurations");

  return (
    <>
      <DropDown
        options={availableWebHooks.map(hook => t(`scm-webhook-plugin.name.${hook.name}`))}
        optionValues={availableWebHooks.map(hook => hook.name)}
        preselectedOption={t(`scm-webhook-plugin.name.${availableWebHooks[selectedWebHook].name}`)}
        optionSelected={selectedName =>
          setSelectedWebHook(availableWebHooks.findIndex(hook => hook.name === selectedName))
        }
      />
      <AddButton
        disabled={readOnly}
        label={t("scm-webhook-plugin.add")}
        action={() => onAdd(availableWebHooks[selectedWebHook])}
      />
    </>
  );
};

export default AddWebHookButton;
