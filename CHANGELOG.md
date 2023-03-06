# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## 2.0.0 [2023-03-06]

### Changed

- Only import documents with file ending `.exb` per default. The old behavior
  can be restored by setting the parameter `strictFileType` to `false`.

### Fixed

- NullPointerException when trimming empty event values
- Do not attempt to create meta data label for UD informations without a name