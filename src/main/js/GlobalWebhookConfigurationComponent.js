// @flow
import React from 'react';
import WebHookConfigurationComponent from "./WebHookConfigurationComponent";
import { translate } from "react-i18next";

type Props = {
  t: string => string,
  link: string
}

class GlobalWebhookConfigurationComponent extends React.Component<Props> {
  render() {
    const props  = this.props;
    const { t } = props;

    return (
      <WebHookConfigurationComponent title={t("scm-webhook-plugin.form.header")} {...props} />
    );
  }
}

export default translate("plugins")(GlobalWebhookConfigurationComponent);
