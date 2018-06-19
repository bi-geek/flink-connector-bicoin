# Bitcoin Flink Connector 
[![Build Status](https://travis-ci.org/bi-geek/flink-connector-bitcoin.svg?branch=master)](https://travis-ci.org/bi-geek/flink-connector-bitcoin) 
[![codecov](https://codecov.io/gh/bi-geek/flink-connector-bitcoin/branch/master/graph/badge.svg)](https://codecov.io/gh/bi-geek/flink-connector-bitcoin) 
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.bi-geek/flink-connector-bitcoin/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.bi-geek/flink-connector-bitcoin) 
[![GitHub stars](https://img.shields.io/github/stars/badges/shields.svg?style=social&label=Star)](https://github.com/bi-geek/flink-connector-bitcoin)

[Flink](https://flink.apache.org/) connector for [Bitcoin](https://bitcoin.org/).

# Table of Contents
 
- [Overview](#overview)
- [Getting started](#getting-started)
- [License](#license)


### Overview

Bitcoin Flink connector provides an InputFormat implementation for reading data from the Bitcoin blockchain.


### Getting started

#### Add dependency

```xml
<dependency>
    <groupId>com.github.bi-geek</groupId>
    <artifactId>flink-connector-bitcoin</artifactId>
    <version>1.0.0</version>
</dependency>

```
#### Code example


```java
public class BitcoinJob {

	private static Logger logger = LoggerFactory.getLogger(BitcoinJob.class);

	public static void main(String[] args) throws Exception {
		final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		DataSource<EthBlock> list = env.createInput(new BitcoinInputSource());
		logger.info("counter: "+ list.count());
		env.execute("Job");


	}
}
```

### License

Bitcoin Flink Connector is licensed under the MIT License. See [LICENSE](LICENSE.md) for details.

Copyright (c) 2018 BI-Geek
