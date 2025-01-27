# Spring Project

## Introduction

TBD

### Version

- Java `21`
- Kotlin `1.9.23`
- Spring Boot `3.2.2`

### Modules Overview

```
├── bootstrap
├── common
├── core
│   ├── api
│   ├── domain
│   └── infra
```

## Getting Started

### Quick Start

```shell
make run module=<module-name>
```

### Build

```shell
 make build module=<module-name>
```

### Docker Build

```shell
 make docker module=<module-name> tag=<tag-name>
```

### Test

```shell
 make test
```

### Stress Test (with k6)

1. 스크립트 초기화
    ```shell
    make k6-init name=<script-name>
    ```

2. 스크립트 작성 📍`./local/k6/script/<script-name>.js`

3. 스크립트 실행
    ```shell
    make k6-run name=<script-name>
    ```
