---
title: Configuration
---
The SCM-Webhook-Plugin provides a global and a repository specific configuration. The global webhooks affect all repositories on the SCM-Manager instance.

When register a new webhook you can decide between "GET", "POST", "PUT" and "AUTO". If you select "AUTO" most likely a GET request will be sent. But if the webhook contains commit data, then a POST request is gonna be sent instead.
The url must be set to the exact address where the requests should be sent.

Per default the webhooks are triggered an each repository push. But it can also be configured, that a webhook will be triggered for each commit. 
Additionally you can configure if the commit data should be attached to the webhook request.

![Webhook configuration](assets/config.png)
