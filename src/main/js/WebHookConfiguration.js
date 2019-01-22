// @flow

export type WebHookConfiguration = {
  urlPattern: string,
  executeOnEveryCommit: boolean,
  sendCommitData: boolean,
  method: string
};

export type WebHookConfigurations = {
  webhooks: WebHookConfiguration[]
};
