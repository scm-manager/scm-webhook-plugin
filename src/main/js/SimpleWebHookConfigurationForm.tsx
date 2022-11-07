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
import styled from "styled-components";
import { confirmAlert, DropDown, Help, InputField, Checkbox } from "@scm-manager/ui-components";
import { WebHookConfiguration } from "./WebHookConfiguration";

const DeleteIcon = styled.a`
  margin: 0.55rem 0 0 0.75rem;
`;

const DropDownWrapper = styled.div`
  margin-right: 1.5rem;
`;

type Props = WithTranslation & {
  webHook: WebHookConfiguration;
  readOnly: boolean;
  onChange: (p: WebHookConfiguration) => void;
  onDelete: (p: WebHookConfiguration) => void;
};

type State = WebHookConfiguration;

class SimpleWebHookConfigurationForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = props.webHook.configuration;
  }

  componentWillReceiveProps(nextProps) {
    // update the webhook in the state if the prop are changed
    // The prop can be modified if webhooks are deleted
    if (nextProps.webHook !== this.props.webHook) {
      this.state = nextProps.webHook.configuration;
    }
  }

  handleChange = (value: any, name: string) => {
    this.setState(
      {
        [name]: value
      },
      () => {
        console.log(this.state)
        this.props.onChange(this.state)
      }
    );
  };

  renderHttpMethodDropDown = () => {
    const { readOnly } = this.props;
    return (
      <div className="field control">
        <DropDown
          options={["GET", "POST", "AUTO", "PUT"]}
          optionSelected={this.handleDropDownChange}
          preselectedOption={this.state.method}
          disabled={readOnly}
        />
        <Help message={this.props.t("scm-webhook-plugin.form.methodHelp")} />
      </div>
    );
  };

  handleDropDownChange = (selection: string) => {
    this.setState({
      ...this.state,
      method: selection
    });
    this.handleChange(selection, "method");
  };

  render() {
    const { readOnly, t } = this.props;
    const { urlPattern, executeOnEveryCommit, sendCommitData } = this.state;
    return (
      <article className="media">
        <DropDownWrapper className="media-left">{this.renderHttpMethodDropDown()}</DropDownWrapper>
        <div className="media-content">
          <InputField
            name="urlPattern"
            placeholder={t("scm-webhook-plugin.form.urlPattern")}
            value={urlPattern}
            onChange={this.handleChange}
            disabled={readOnly}
          />
          <Checkbox
            name="executeOnEveryCommit"
            label={t("scm-webhook-plugin.form.executeOnEveryCommit")}
            checked={executeOnEveryCommit}
            onChange={this.handleChange}
            disabled={readOnly}
            helpText={t("scm-webhook-plugin.form.executeOnEveryCommitHelp")}
          />
          <Checkbox
            name="sendCommitData"
            label={t("scm-webhook-plugin.form.sendCommitData")}
            checked={sendCommitData}
            onChange={this.handleChange}
            disabled={readOnly}
            helpText={t("scm-webhook-plugin.form.sendCommitDataHelp")}
          />
        </div>
        <div>
          <Help message={t("scm-webhook-plugin.form.urlPatternHelp")} />
        </div>
      </article>
    );
  }
}

export default withTranslation("plugins")(SimpleWebHookConfigurationForm);
