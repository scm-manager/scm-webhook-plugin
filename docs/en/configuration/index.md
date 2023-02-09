---
title: Configuration
---
## Configuration Form
The SCM-Webhook-Plugin provides a global and a repository specific configuration. The global webhooks are executed for
all repositories on the SCM-Manager instance.

Other plugins may provide further kinds of webhooks. These will be described in the documentation of the providing plugin.
The webhook plugin itself only provides a simple webhook:

To register a new webhook you can select between "GET", "POST", "PUT", and "AUTO". If you select "AUTO", normally a GET
request will be sent to the given URL. Only if the webhook contains commit data, a POST request will be sent instead.
The URL must be set to the exact address where the requests should be sent to.

By default, the webhooks are triggered an each repository push. If needed, a webhook can also be configured to be
triggered for each commit. In addition, you can configure that the commit data should be attached to the webhook request.

![Webhook configuration](assets/config.png)

## Webhook URL Builder
You can use context-sensitive fields in the webhook url. We currently provide two main context objects which can be accessed as following:

### Repository

Example: `example.com/${repository.namespace}/${repository.name}/${repository.type}`
Find all supported fields [here](https://ecosystem.cloudogu.com/scm/repo/scm-manager-plugins/scm-el-plugin/code/sources/develop/src/main/java/com/cloudogu/scm/el/env/ImmutableEncodedRepository.java/).

### Commit / Changeset
Commit and changeset are synonym and both can be used to access the same context.

Example: `example.com/trigger/${changeset.id}/${changeset.author.name}`
Find all supported fields [here](https://ecosystem.cloudogu.com/scm/repo/scm-manager-plugins/scm-el-plugin/code/sources/develop/src/main/java/com/cloudogu/scm/el/env/ImmutableEncodedChangeset.java/).

## Webhook Payload
### List of commits

Example:
```json
{
  "changeset": [
    {
      "author": {
        "mail": "scm-admin@scm-manager.org",
        "name": "SCM Administrator",
        "valid": true
      },
      "branches": [
        "main"
      ],
      "date": 1675686308000,
      "description": "My repo commit description",
      "id": "4fa9832c50e0ac57183023a30195d0f11a9dc0ef",
      "parents": [
        "74fb9da82be3d878be25a3c1c70a69fcbd3c0d66"
      ],
      "tags": null,
      "contributors": [
        {
          "type": "Committed-by",
          "person": {
            "mail": "noreply@scm-manager.org",
            "name": "SCM-Manager",
            "valid": true
          }
        }
      ],
      "signatures": [
        {
          "keyId": "0x1348937E258263F4",
          "type": "gpg",
          "status": "VERIFIED",
          "owner": {
            "empty": false,
            "present": true
          },
          "contacts": [
            {
              "mail": "scm-admin@scm-manager.org",
              "name": "SCM Administrator",
              "valid": true
            }
          ]
        }
      ]
    }
  ]
}
```

### Single commit

Example:
```json
{
  "author": {
    "mail": "scm-admin@scm-manager.org",
    "name": "SCM Administrator",
    "valid": true
  },
  "branches": [
    "main"
  ],
  "date": 1675686490000,
  "description": "My commit desc",
  "id": "73757af953dd6b66c48aeae51407a5296ba9f9fa",
  "parents": [
    "4fa9832c50e0ac57183023a30195d0f11a9dc0ef"
  ],
  "tags": null,
  "contributors": [
    {
      "type": "Committed-by",
      "person": {
        "mail": "noreply@scm-manager.org",
        "name": "SCM-Manager",
        "valid": true
      }
    }
  ],
  "signatures": [
    {
      "keyId": "0x1348937E258263F4",
      "type": "gpg",
      "status": "VERIFIED",
      "owner": {
        "empty": false,
        "present": true
      },
      "contacts": [
        {
          "mail": "scm-admin@scm-manager.org",
          "name": "SCM Administrator",
          "valid": true
        }
      ]
    }
  ]
}
```
