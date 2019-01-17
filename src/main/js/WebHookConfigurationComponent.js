// @flow
import React from "react";
import {Configuration, Title} from "@scm-manager/ui-components";
import {translate} from "react-i18next";
import WebHookConfigurationsForm from "./WebHookConfigurationsForm";
import type {Repository} from "@scm-manager/ui-types";

type Props = {
  repository?: Repository,
  link: string,
  t: string => string
};

class WebHookConfigurationComponent extends React.Component<Props, State> {

  constructor(props: Props) {
    super(props);
  };

  render() {
    const {t, link} = this.props;
    return (
      <>
        <Title title={t("scm-webhook-plugin.form.header")} />
        <h2 >{t("scm-webhook-plugin.helpText")}</h2>
        <br/>
        <Configuration link={link} render={props => <WebHookConfigurationsForm {...props} />}/>
      </>
    );
  };
}

export default translate("plugins")(WebHookConfigurationComponent);
