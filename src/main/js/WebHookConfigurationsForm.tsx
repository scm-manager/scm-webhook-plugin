import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { WebHookConfigurations } from "./WebHookConfiguration";
import WebHookConfigurationForm from "./WebHookConfigurationForm";
import { AddButton } from "@scm-manager/ui-components";

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
        <article className="media">
          <AddButton
            disabled={readOnly}
            label={t("scm-webhook-plugin.add")}
            action={() => {
              webhooks.push(defaultWebHook);
              this.updateWebHooks(webhooks);
            }}
          />
        </article>
      </>
    );
  }
}

export default withTranslation("plugins")(WebHookConfigurationsForm);
