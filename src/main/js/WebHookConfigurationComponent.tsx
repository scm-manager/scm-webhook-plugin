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
