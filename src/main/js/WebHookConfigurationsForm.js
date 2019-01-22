//@flow
import React from "react";
import {translate} from "react-i18next";
import type {WebHookConfigurations} from "./WebHookConfiguration";
import WebHookConfigurationForm from "./WebHookConfigurationForm";
import Button from "@scm-manager/ui-components/src/buttons/Button";


type Props = {
  initialConfiguration: WebHookConfigurations,
  readOnly: boolean,
  onConfigurationChange: (WebHookConfigurations, boolean) => void,
  // context prop
  t: (string) => string
};

type State = WebHookConfigurations & {};

class WebHookConfigurationsForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {...props.initialConfiguration};
  };

  isValid() {
    const {webhooks} = this.state;
    let valid = true;
    webhooks.map((webHook) => {
      valid = valid && webHook.urlPattern.trim() != "" && webHook.method.trim() != "";
    });
    return valid;
  }

  updateWebHooks(webhooks) {
    this.setState({webhooks}, () => this.props.onConfigurationChange(this.state, this.isValid()));
  }

  onDelete = (deletedWebHook) => {
    const {webhooks} = this.state;
    let index = webhooks.indexOf(deletedWebHook);
    webhooks.splice(index, 1);
    this.updateWebHooks(webhooks);
  };

  onChange = (changedWebHook , index) => {
    const {webhooks} = this.state;
    webhooks[index] = changedWebHook;
    this.updateWebHooks(webhooks);
  };

  render() {
    const {webhooks} = this.state;
    const {t, readOnly} = this.props;
    let defaultWebHook = {
      urlPattern: "",
      executeOnEveryCommit: false,
      sendCommitData: false,
      method: "AUTO"
    };

    return (
      <>
        {webhooks.map((webHook, index) => {
          return <WebHookConfigurationForm
            webHook={webHook}
            readOnly={readOnly}
            onDelete={this.onDelete}
            onChange={(changedWebHook) => this.onChange(changedWebHook, index)}
          />
        })}
        <article className="media">
          <Button  disabled={readOnly} label={t("scm-webhook-plugin.add")} action={() => {
            webhooks.push(defaultWebHook);
            this.updateWebHooks(webhooks);
          }
          }/>
        </article>
      </>
    );
  }
}

export default translate("plugins")(WebHookConfigurationsForm);
