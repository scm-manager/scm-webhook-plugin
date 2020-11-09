# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased
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

