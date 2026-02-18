## [6.5.1](https://github.com/gravitee-io/gravitee-common-elasticsearch/compare/6.5.0...6.5.1) (2026-02-18)


### Bug Fixes

* defer client selection so retries re-check ES availability ([fb12de8](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/fb12de88a09f96329fa96b5cbd1f81fee4a5fc95))

# [6.5.0](https://github.com/gravitee-io/gravitee-common-elasticsearch/compare/6.4.0...6.5.0) (2025-11-26)


### Features

* add after key property to aggregation model ([c79be6b](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/c79be6bd9ec51071577fee1bc005ea2c2351984c))

# [6.4.0](https://github.com/gravitee-io/gravitee-common-elasticsearch/compare/6.3.0...6.4.0) (2025-11-12)


### Features

* add percentiles to aggregation response model ([89af672](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/89af6722a7fa0a81c8fb4fedf8e8feb494e43d03))

# [6.3.0](https://github.com/gravitee-io/gravitee-common-elasticsearch/compare/6.2.0...6.3.0) (2025-09-17)


### Bug Fixes

* **dependencies:** fix dependencies scopes ([abb67b4](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/abb67b4d822665bde4f5acf79d5568d0672497fc))


### Features

* add new EVENT_METRICS type ([4c4ea25](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/4c4ea2563d5ad1744943bdb589b276f5741c0879))
* add support for Elasticsearch data streams ([0b63b85](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/0b63b85daa7083b20e768ee343b2a551ed41f7e8))
* revert changes in HttpClient (not required) ([02d98ff](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/02d98ffc59d5312b88fde85a8ed948e857f0f282))
* support sub aggregations in search response ([666fc91](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/666fc91819b82f72db4bbcdb9f0400c3c1a52f84))

# [6.3.0-alpha.5](https://github.com/gravitee-io/gravitee-common-elasticsearch/compare/6.3.0-alpha.4...6.3.0-alpha.5) (2025-09-02)


### Features

* support sub aggregations in search response ([666fc91](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/666fc91819b82f72db4bbcdb9f0400c3c1a52f84))

# [6.3.0-alpha.4](https://github.com/gravitee-io/gravitee-common-elasticsearch/compare/6.3.0-alpha.3...6.3.0-alpha.4) (2025-08-08)


### Features

* revert changes in HttpClient (not required) ([02d98ff](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/02d98ffc59d5312b88fde85a8ed948e857f0f282))

# [6.3.0-alpha.3](https://github.com/gravitee-io/gravitee-common-elasticsearch/compare/6.3.0-alpha.2...6.3.0-alpha.3) (2025-08-07)


### Features

* add support for Elasticsearch data streams ([0b63b85](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/0b63b85daa7083b20e768ee343b2a551ed41f7e8))

# [6.3.0-alpha.2](https://github.com/gravitee-io/gravitee-common-elasticsearch/compare/6.3.0-alpha.1...6.3.0-alpha.2) (2025-07-17)


### Bug Fixes

* **dependencies:** fix dependencies scopes ([abb67b4](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/abb67b4d822665bde4f5acf79d5568d0672497fc))

# [6.3.0-alpha.1](https://github.com/gravitee-io/gravitee-common-elasticsearch/compare/6.2.0...6.3.0-alpha.1) (2025-07-17)


### Features

* add new EVENT_METRICS type ([4c4ea25](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/4c4ea2563d5ad1744943bdb589b276f5741c0879))

# [6.2.0](https://github.com/gravitee-io/gravitee-common-elasticsearch/compare/6.1.0...6.2.0) (2024-12-05)


### Features

* add new bulk method with simple buffer ([74048d0](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/74048d0210ee7c3313c349f51738a32bb741809e))

# [6.1.0](https://github.com/gravitee-io/gravitee-common-elasticsearch/compare/6.0.0...6.1.0) (2024-06-11)


### Features

* get index field types ([83eac89](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/83eac897d47a62c4801df98e2fc110dd99cc5eef))

# [6.0.0](https://github.com/gravitee-io/gravitee-common-elasticsearch/compare/5.1.0...6.0.0) (2024-05-23)


### Bug Fixes

* **deps:** update dependency io.gravitee:gravitee-bom to 6.0.10 ([ad9ab4f](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/ad9ab4ff7d88231f810b84800aed617e86cdca31))


### Features

* allow index name with placeholders ([cac1184](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/cac11847811bad922306e2a1a69a8a32e6baeb15))


### BREAKING CHANGES

* index name generator signature changes

# [5.1.0](https://github.com/gravitee-io/gravitee-common-elasticsearch/compare/5.0.1...5.1.0) (2023-11-10)


### Features

* add method to put index template ([aef19b3](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/aef19b3185bd17d572bbbdb74b9b1f02971dfb36))

## [5.0.1](https://github.com/gravitee-io/gravitee-common-elasticsearch/compare/5.0.0...5.0.1) (2023-09-12)


### Bug Fixes

* deprecate freemarker component ([3845010](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/3845010f8ac9ca3e5ab5f215b63bab7a0b055813))

# [5.0.0](https://github.com/gravitee-io/gravitee-common-elasticsearch/compare/4.2.0...5.0.0) (2023-08-31)


### Code Refactoring

* move index mappings to reporter common ([36e7fcc](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/36e7fcce227d984180d1827fe785016dc360a8ae))


### BREAKING CHANGES

* index mappings must now be imported from gravitee-reporter-common

# [4.2.0](https://github.com/gravitee-io/gravitee-common-elasticsearch/compare/4.1.0...4.2.0) (2023-08-16)


### Features

* use wildcards when computing elastic query indices ([0155cbb](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/0155cbb16687c4bdf42310d897c99320f48ab7d2))

# [4.1.0](https://github.com/gravitee-io/gravitee-common-elasticsearch/compare/4.0.0...4.1.0) (2023-03-17)


### Bug Fixes

* **deps:** upgrade gravitee-bom & alpha version ([3b188fc](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/3b188fc126f0310d95ce5e087b8fa68a230cf8bc))
* fix error-count template ([9e21f86](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/9e21f86f44754b1f19db035a650f1b08ff980c10))
* fix v4 metrics index ([0aac478](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/0aac478ded74397bd827cb59fc21a00dd6e155f9))
* rename metrics for message ([36acc92](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/36acc927b3cd9105d8a4765ec9a26523fb7b6c0a))
* rename v4 metrics ([c8d562a](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/c8d562a3c5f7f32e21fbc6c44dcdfa2e24f878f0))
* rename v4 metrics template ([25493b4](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/25493b43df9bd05df51426b8665d91b440e800fa))
* use custom body analyzer ([c172dc8](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/c172dc821aa9c69ae28c38cc1215ce107745ac68)), closes [gravitee-io/issues#8470](https://github.com/gravitee-io/issues/issues/8470)


### Features

* add error status on message metrics ([471c5e4](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/471c5e48ee225cced1a15f060566f15f3260ae50))
* add support of ES 8.x ([f774e86](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/f774e86863b5af2d989d08f19812fb4cfde828d3))
* add v4 files templates for index and mapping ([a486f68](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/a486f6836c34c855b83dd91c7a3675ce90086ad8))
* check if ES handle `interval` in aggregation ([23a49d4](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/23a49d48eb4539870f7f9f6f5d2fe9a1827945eb))
* define template for log-v4 and request-v4 ([058cb9e](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/058cb9e1ff225b0dcf8973d774cb2bb2721b4ac3))
* share all ES templates in gravitee-common-elasticsearch ([4d8f890](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/4d8f89046bf860b9ae9a61d249e55347de9edfcc))
* useless ([2403cb3](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/2403cb38b69740579c903f5d26ae3de95003419a))

# [4.1.0-alpha.9](https://github.com/gravitee-io/gravitee-common-elasticsearch/compare/4.1.0-alpha.8...4.1.0-alpha.9) (2023-03-16)


### Bug Fixes

* use custom body analyzer ([0e698a7](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/0e698a7b5108a0159ef3b4461e747544e5793c93)), closes [gravitee-io/issues#8470](https://github.com/gravitee-io/issues/issues/8470)

# [4.1.0-alpha.8](https://github.com/gravitee-io/gravitee-common-elasticsearch/compare/4.1.0-alpha.7...4.1.0-alpha.8) (2023-03-10)


### Features

* add support of ES 8.x ([3c65b1f](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/3c65b1f25ae5ce4739946c4e52aa40b8466b15c0))
* add support of ES 8.x ([e53dcd7](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/e53dcd73d6cf29a0f1bf51979521de45960120bb))
* add v4 files templates for index and mapping ([db0912e](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/db0912eb49a7892bf9487c7c08425b4cbff73827))
* check if ES handle `interval` in aggregation ([223981f](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/223981fa27aab18c5d4dd41586ef55f5e13e2d5a))
* useless ([cb056ee](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/cb056ee55073974dd3b234ef6f3b830affcbc64a))

# [4.1.0-alpha.7](https://github.com/gravitee-io/gravitee-common-elasticsearch/compare/4.1.0-alpha.6...4.1.0-alpha.7) (2023-02-01)


### Bug Fixes

* fix error-count template ([23d31c5](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/23d31c53f02604334a2de0370a1f79ee158f3ec5))


### Features

* add error status on message metrics ([b733bb2](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/b733bb2ad53cf99a2df4055d91d8f5eebebe0a68))

# [4.1.0-alpha.6](https://github.com/gravitee-io/gravitee-common-elasticsearch/compare/4.1.0-alpha.5...4.1.0-alpha.6) (2023-01-27)


### Bug Fixes

* fix v4 metrics index ([f2d6686](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/f2d6686746cb89fa579827ec192bfb9604a76cd7))

# [4.1.0-alpha.5](https://github.com/gravitee-io/gravitee-common-elasticsearch/compare/4.1.0-alpha.4...4.1.0-alpha.5) (2023-01-25)


### Bug Fixes

* rename v4 metrics template ([2a01e36](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/2a01e36c881fd33f95300f3ff1cfd35d1fe58a51))

# [4.1.0-alpha.4](https://github.com/gravitee-io/gravitee-common-elasticsearch/compare/4.1.0-alpha.3...4.1.0-alpha.4) (2023-01-25)


### Bug Fixes

* rename v4 metrics ([ccfd7b2](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/ccfd7b21f94b7306e2126a27aba35661d2da94fa))

# [4.1.0-alpha.3](https://github.com/gravitee-io/gravitee-common-elasticsearch/compare/4.1.0-alpha.2...4.1.0-alpha.3) (2023-01-24)


### Bug Fixes

* rename metrics for message ([78fc4ac](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/78fc4ac3fd47cfef7f26d7f83960c9a4c9f89233))

# [4.1.0-alpha.2](https://github.com/gravitee-io/gravitee-common-elasticsearch/compare/4.1.0-alpha.1...4.1.0-alpha.2) (2023-01-16)


### Features

* define template for log-v4 and request-v4 ([d768735](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/d7687352ed4d4a5245ee24233bfdfc4587b86032))

# [4.1.0-alpha.1](https://github.com/gravitee-io/gravitee-common-elasticsearch/compare/4.0.0...4.1.0-alpha.1) (2022-12-22)


### Features

* share all ES templates in gravitee-common-elasticsearch ([cfa5eb5](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/cfa5eb594e61f9c24822818e238ab64beb3b0378))

# [4.0.0](https://github.com/gravitee-io/gravitee-common-elasticsearch/compare/3.12.1...4.0.0) (2022-12-09)


### chore

* bump to rxJava3 ([139088f](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/139088f8f1f1845a6f8f29e2f219576484c86bde))


### BREAKING CHANGES

* rxJava3 required

# [4.0.0-alpha.1](https://github.com/gravitee-io/gravitee-common-elasticsearch/compare/3.12.1...4.0.0-alpha.1) (2022-10-18)


### chore

* bump to rxJava3 ([139088f](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/139088f8f1f1845a6f8f29e2f219576484c86bde))


### BREAKING CHANGES

* rxJava3 required

## [3.12.1](https://github.com/gravitee-io/gravitee-common-elasticsearch/compare/3.12.0...3.12.1) (2022-05-17)


### Bug Fixes

* add method to manage alias ([51c0fa4](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/51c0fa437570b9d4b19ee6610b62bc67c03b43de)), closes [gravitee-io/issues#7110](https://github.com/gravitee-io/issues/issues/7110)
* add modification regarding PR feedbacks ([fc2bdd3](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/fc2bdd3e0e50816759b4a055a60401a97b18f934))

## [3.8.5](https://github.com/gravitee-io/gravitee-common-elasticsearch/compare/3.8.4...3.8.5) (2022-05-17)


### Bug Fixes

* add method to manage alias ([51c0fa4](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/51c0fa437570b9d4b19ee6610b62bc67c03b43de)), closes [gravitee-io/issues#7110](https://github.com/gravitee-io/issues/issues/7110)
* add modification regarding PR feedbacks ([fc2bdd3](https://github.com/gravitee-io/gravitee-common-elasticsearch/commit/fc2bdd3e0e50816759b4a055a60401a97b18f934))
