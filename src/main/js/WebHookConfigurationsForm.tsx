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
import { WebHookConfiguration, WebHookConfigurations } from "./WebHookConfiguration";
import { Button, confirmAlert, Level } from "@scm-manager/ui-components";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import AddWebHookButton from "./AddWebHookButton";

type Props = WithTranslation & {
  initialConfiguration: WebHookConfigurations;
  readOnly: boolean;
  onConfigurationChange: (p1: WebHookConfigurations, p2: boolean) => void;
};

type EditorState = WebHookConfiguration & {
  valid: boolean;
};

type State = {
  editorStates: EditorState[];
};

class WebHookConfigurationsForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      editorStates: props.initialConfiguration.webhooks.map(config => {
        return {
          ...config,
          valid: true
        };
      })
    };
  }

  isValid = () => {
    return this.state.editorStates.findIndex(state => !state.valid) === -1;
  };

  updateWebHooks(editorStates) {
    this.setState(
      {
        editorStates
      },
      () => this.props.onConfigurationChange({ webhooks: this.state.editorStates }, this.isValid())
    );
  }

  confirmDelete = (index: number) => {
    const { t } = this.props;
    confirmAlert({
      title: t("scm-webhook-plugin.confirm-delete.title"),
      message: t("scm-webhook-plugin.confirm-delete.message"),
      buttons: [
        {
          label: t("scm-webhook-plugin.confirm-delete.submit"),
          onClick: () => this.onDelete(index)
        },
        {
          className: "is-info",
          label: t("scm-webhook-plugin.confirm-delete.cancel"),
          onClick: () => null
        }
      ]
    });
  };

  onDelete = index => {
    const { editorStates } = this.state;
    editorStates.splice(index, 1);
    this.updateWebHooks(editorStates);
  };

  onChange = (changedWebHook, index, valid) => {
    const { editorStates } = this.state;
    editorStates[index].configuration = changedWebHook;
    editorStates[index].valid = valid;
    this.updateWebHooks(editorStates);
  };

  render() {
    const { editorStates } = this.state;
    const { readOnly } = this.props;

    return (
      <>
        {editorStates.map((webHook, index) => {
          const deleteIcon = readOnly ? (
            ""
          ) : (
            <Button className="level-item" action={() => this.confirmDelete(index)}>
              <i className="fas fa-trash" />
            </Button>
          );
          return (
            <div className={"columns is-vcentered"}>
              <div className={"column"}>
                <ExtensionPoint
                  name={`webhook.configuration.${webHook.name}`}
                  renderAll={true}
                  props={{
                    webHook: webHook,
                    readOnly: readOnly,
                    onChange: (changedWebHook, valid) => this.onChange(changedWebHook, index, valid)
                  }}
                />
              </div>
              <div className={"colum"}>{deleteIcon}</div>
            </div>
          );
        })}
        <Level
          right={
            <AddWebHookButton
              readOnly={readOnly}
              onAdd={({ name, defaultConfiguration }) => {
                editorStates.push({
                  name: name,
                  configuration: defaultConfiguration,
                  valid: true
                });
                this.updateWebHooks(editorStates);
              }}
            />
          }
        />
      </>
    );
  }
}

export default withTranslation("plugins")(WebHookConfigurationsForm);
