# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 3.0.0 - 2024-09-12
### Changed
- Changeover to AGPLv3 license

## 2.4.0 - 2023-06-09
### Changed
- BREAKING - Change webhooks layout to master details multi-page design

## 2.3.3 - 2022-12-09
### Fixed
- Execution of repository webhooks

## 2.3.2 - 2022-11-28
### Fixed
- Error on webhook modifications in repositories ([#53](https://github.com/scm-manager/scm-webhook-plugin/pull/53))

## 2.3.1 - 2022-11-24
### Fixed
- Exception for new configuration ([#52](https://github.com/scm-manager/scm-webhook-plugin/pull/52))

## 2.3.0 - 2022-11-22
### Added
- Extensible webhooks ([#46](https://github.com/scm-manager/scm-webhook-plugin/pull/46))

## 2.2.1 - 2022-06-02
### Fixed
- Enlarge color difference between options in Confirm Alert ([#41](https://github.com/scm-manager/scm-webhook-plugin/pull/41))

## 2.2.0 - 2020-11-09
### Changed
- Set span kind for http requests (for Trace Monitor)

## 2.1.1 - 2020-08-13
### Fixed
- Fix migration error if http method is missing ([#12](https://github.com/scm-manager/scm-webhook-plugin/pull/12))

## 2.1.0 - 2020-07-23
### Added
- Documentation in German ([#5](https://github.com/scm-manager/scm-webhook-plugin/pull/5))

### Changed
- Replaced `URLConnection` based http client with `AdvancedHttpClient` ([#9](https://github.com/scm-manager/scm-webhook-plugin/pull/9))
- Use expression language parser from scm-el-plugin ([#10](https://github.com/scm-manager/scm-webhook-plugin/pull/10))

## 2.0.0 - 2020-06-04
### Changed
- Changeover to MIT license ([#4](https://github.com/scm-manager/scm-webhook-plugin/pull/4))
- Rebuild for api changes from core

## 2.0.0-rc2 - 2020-03-13
### Added
- Add swagger rest annotations to generate openAPI specs for the scm-openapi-plugin ([#3](https://github.com/scm-manager/scm-webhook-plugin/pull/3))

