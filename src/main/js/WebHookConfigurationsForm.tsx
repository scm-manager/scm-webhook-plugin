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
import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { WebHookConfigurations } from "./WebHookConfiguration";
import WebHookConfigurationForm from "./WebHookConfigurationForm";
import { Level, AddButton } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  initialConfiguration: WebHookConfigurations;
  readOnly: boolean;
  onConfigurationChange: (p1: WebHookConfigurations, p2: boolean) => void;
};

type State = WebHookConfigurations & {};

class WebHookConfigurationsForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      ...props.initialConfiguration
    };
  }

  isValid() {
    const { webhooks } = this.state;
    let valid = true;
    webhooks.map(webHook => {
      valid = valid && webHook.urlPattern.trim() !== "" && webHook.method.trim() !== "";
    });
    return valid;
  }

  updateWebHooks(webhooks) {
    this.setState(
      {
        webhooks
      },
      () => this.props.onConfigurationChange(this.state, this.isValid())
    );
  }

  onDelete = deletedWebHook => {
    const { webhooks } = this.state;
    const index = webhooks.indexOf(deletedWebHook);
    webhooks.splice(index, 1);
    this.updateWebHooks(webhooks);
  };

  onChange = (changedWebHook, index) => {
    const { webhooks } = this.state;
    webhooks[index] = changedWebHook;
    this.updateWebHooks(webhooks);
  };

  render() {
    const { webhooks } = this.state;
    const { t, readOnly } = this.props;
    const defaultWebHook = {
      urlPattern: "",
      executeOnEveryCommit: false,
      sendCommitData: false,
      method: "AUTO"
    };

    return (
      <>
        {webhooks.map((webHook, index) => {
          return (
            <WebHookConfigurationForm
              webHook={webHook}
              readOnly={readOnly}
              onDelete={this.onDelete}
              onChange={changedWebHook => this.onChange(changedWebHook, index)}
            />
          );
        })}
        <Level right={<AddButton
            disabled={readOnly}
            label={t("scm-webhook-plugin.add")}
            action={() => {
              webhooks.push(defaultWebHook);
              this.updateWebHooks(webhooks);
            }}
          />}
        />
      </>
    );
  }
}

export default withTranslation("plugins")(WebHookConfigurationsForm);
