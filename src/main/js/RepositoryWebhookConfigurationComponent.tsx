import React from "react";
import WebHookConfigurationComponent from "./WebHookConfigurationComponent";
import { WithTranslation, withTranslation } from "react-i18next";

type Props = WithTranslation & {
  link: string;
};

class RepositoryWebhookConfigurationComponent extends React.Component<Props> {
  render() {
    const props = this.props;
    const { t } = props;

    return <WebHookConfigurationComponent subtitle={t("scm-webhook-plugin.form.header")} {...props} />;
  }
}

export default withTranslation("plugins")(RepositoryWebhookConfigurationComponent);
