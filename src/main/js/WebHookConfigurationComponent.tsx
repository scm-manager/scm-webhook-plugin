import React from "react";
import { Configuration, Title, Subtitle } from "@scm-manager/ui-components";
import { WithTranslation, withTranslation } from "react-i18next";
import WebHookConfigurationsForm from "./WebHookConfigurationsForm";
import { Repository } from "@scm-manager/ui-types";

type Props = WithTranslation & {
  repository?: Repository;
  title?: string;
  subtitle?: string;
  link: string;
};

class WebHookConfigurationComponent extends React.Component<Props> {
  render() {
    const { t, link } = this.props;
    return (
      <>
        {this.renderTitle()}
        {this.renderSubtitle()}
        <h2>{t("scm-webhook-plugin.helpText")}</h2>
        <br />
        <Configuration link={link} render={props => <WebHookConfigurationsForm {...props} />} />
      </>
    );
  }

  renderTitle = () => {
    if (!this.props.title) {
      return null;
    }
    return <Title title={this.props.title} />;
  };

  renderSubtitle = () => {
    if (!this.props.subtitle) {
      return null;
    }
    return <Subtitle subtitle={this.props.subtitle} />;
  };
}

export default withTranslation("plugins")(WebHookConfigurationComponent);
