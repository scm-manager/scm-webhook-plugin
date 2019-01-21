//@flow
import React from "react";
import {confirmAlert, DropDown, InputField, Help} from "@scm-manager/ui-components";
import {translate} from "react-i18next";
import type {WebHookConfiguration} from "./WebHookConfiguration";
import Checkbox from "@scm-manager/ui-components/src/forms/Checkbox";

type Props = {
  webHook: WebHookConfiguration,
  onChange: (WebHookConfiguration) => void,
  onDelete: (WebHookConfiguration) => void,
  // context prop
  t: string => string
};

type State = WebHookConfiguration;

class WebHookConfigurationForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = props.webHook;
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.webHook !== this.props.webHook) {
      this.state = nextProps.webHook;
    }
  }

  handleChange = (value: any, name: string) => {
    this.setState({
      [name]: value
    }, () => this.props.onChange(this.state));
  };

  renderHttpMethodDropDown = () => {
    return (
      <div className="field">
        <div className="control">
        <DropDown
          options={["GET", "POST", "AUTO", "PUT"]}
          optionSelected={this.handleDropDownChange}
          preselectedOption={this.state.method}
          /><Help message={this.props.t("scm-webhook-plugin.form.methodHelp")}/>
        </div>
      </div>
    );
  };

  handleDropDownChange = (selection: string) => {
    this.setState({...this.state, method: selection});
    this.handleChange(selection, "method");
  };

  confirmDelete = () => {
    const {t} = this.props;
    confirmAlert({
      title: t("scm-webhook-plugin.confirm-delete.title"),
      message: t("scm-webhook-plugin.confirm-delete.message"),
      buttons: [
        {
          label: t("scm-webhook-plugin.confirm-delete.submit"),
          onClick: () => this.props.onDelete(this.state)
        },
        {
          label: t("scm-webhook-plugin.confirm-delete.cancel"),
          onClick: () => null
        }
      ]
    });
  };

  render() {
    const {t} = this.props;
    const {urlPattern, executeOnEveryCommit, sendCommitData} = this.state;
    const deleteIcon =
      <a className="level-item"
         onClick={this.confirmDelete}
      >
        <span className="icon is-small">
          <i className="fas fa-trash">
          </i>
        </span>
      </a>
    ;

    return (
      <article className="media">
        <div className="media-left">
          {this.renderHttpMethodDropDown()}
        </div>
        <div className="media-content">
          <div className="content">
            <InputField
              name={"urlPattern"}
              placeholder={t("scm-webhook-plugin.form.urlPattern")}
              value={urlPattern}
              onChange={this.handleChange}
            />
            <Checkbox
              name={"executeOnEveryCommit"}
              label={t("scm-webhook-plugin.form.executeOnEveryCommit")}
              checked={executeOnEveryCommit}
              onChange={this.handleChange}
              helpText={t("scm-webhook-plugin.form.executeOnEveryCommitHelp")}
            />
            <Checkbox
              name={"sendCommitData"}
              label={t("scm-webhook-plugin.form.sendCommitData")}
              checked={sendCommitData}
              onChange={this.handleChange}
              helpText={t("scm-webhook-plugin.form.sendCommitDataHelp")}
            />
          </div>
        </div>
        <div>
          <Help message={this.props.t("scm-webhook-plugin.form.urlPatternHelp")} />
        </div>
        <div className="media-right">
          {deleteIcon}
        </div>
      </article>
    );
  };
}

export default translate("plugins")(WebHookConfigurationForm);
