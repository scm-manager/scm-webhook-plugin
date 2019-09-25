//@flow
import React from "react";
import { translate } from "react-i18next";
import injectSheet from "react-jss";
import classNames from "classnames";
import {
  confirmAlert,
  DropDown,
  Help,
  InputField,
  Checkbox
} from "@scm-manager/ui-components";
import type { WebHookConfiguration } from "./WebHookConfiguration";

type Props = {
  webHook: WebHookConfiguration,
  readOnly: boolean,
  onChange: WebHookConfiguration => void,
  onDelete: WebHookConfiguration => void,

  // context prop
  classes: any,
  t: string => string
};

type State = WebHookConfiguration;

const styles = {
  deleteIcon: {
    margin: "0.55rem 0 0 0.75rem"
  },
  marginRight: {
    marginRight: "1.5rem"
  }
};

class WebHookConfigurationForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = props.webHook;
  }

  componentWillReceiveProps(nextProps) {
    // update the webhook in the state if the prop are changed
    // The prop can be modified if webhooks are deleted
    if (nextProps.webHook !== this.props.webHook) {
      this.state = nextProps.webHook;
    }
  }

  handleChange = (value: any, name: string) => {
    this.setState(
      {
        [name]: value
      },
      () => this.props.onChange(this.state)
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
    this.setState({ ...this.state, method: selection });
    this.handleChange(selection, "method");
  };

  confirmDelete = () => {
    const { t } = this.props;
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
    const { readOnly, classes, t } = this.props;
    const { urlPattern, executeOnEveryCommit, sendCommitData } = this.state;
    const deleteIcon = readOnly ? (
      ""
    ) : (
      <a
        className={classNames("level-item", classes.deleteIcon)}
        onClick={this.confirmDelete}
      >
        <span className="icon is-small">
          <i className="fas fa-trash" />
        </span>
      </a>
    );
    return (
      <article className="media">
        <div className={classNames("media-left", classes.marginRight)}>
          {this.renderHttpMethodDropDown()}
        </div>
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
          <Help
            message={this.props.t("scm-webhook-plugin.form.urlPatternHelp")}
          />
        </div>
        <div className="media-right">{deleteIcon}</div>
      </article>
    );
  }
}

export default translate("plugins")(
  injectSheet(styles)(WebHookConfigurationForm)
);
