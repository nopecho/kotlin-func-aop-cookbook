NOW := $(shell date +"%y%m%dT%H%M%S")
BUILD_PATH := $(CURDIR)/bootstrap
K6_SCRIPT_PATH := $(CURDIR)/local/k6/script
K6_REPORT_PATH := $(CURDIR)/local/k6/report
LOCAL_DOCKER_COMPOSE := $(CURDIR)/local/docker-compose.yml

k6-init:
ifdef name
	@docker run \
	-v $(K6_SCRIPT_PATH):/script \
	-w /script \
	--rm -i grafana/k6 new $(name).js
else
	@echo "Usage: make k6-init name=<script-name>"
endif
.PHONY: k6-init

k6-run:
ifdef name
	@docker run \
	-v $(K6_REPORT_PATH):/report \
	-e K6_WEB_DASHBOARD=true \
	-e K6_WEB_DASHBOARD_EXPORT=/report/$(NOW)-$(name).html \
	--rm -i grafana/k6 run - <$(K6_SCRIPT_PATH)/$(name).js \
	--summary-trend-stats="p(99),p(95),avg,min,max" && \
	open $(K6_REPORT_PATH)/$(NOW)-$(name).html
else
	@echo "Usage: make k6-run name=<script-name>"
endif
.PHONY: k6-run

up:
	docker-compose -f $(LOCAL_DOCKER_COMPOSE) up -d

down:
	docker-compose -f $(LOCAL_DOCKER_COMPOSE) down

run: up
ifdef module
	$(CURDIR)/gradlew :bootstrap:$(module):bootRun
else
	@echo "Usage: make run module=<module-name>"
	@exit 1
endif
.PHONY: run

code:
	$(CURDIR)/gradlew clean kaptKotlin
.PHONY: code

clean:
	$(CURDIR)/gradlew clean

test:
	$(CURDIR)/gradlew clean test
.PHONY: test

build: clean
ifdef module
	$(CURDIR)/gradlew test :bootstrap:$(module):build
else
	@echo "Usage: make build module=<module-name>"
	@exit 1
endif
.PHONY: build

docker: build
ifdef module
ifdef tag
	cd $(BUILD_PATH)/$(module) && \
 	docker build --tag $(tag) .
else
	@echo "Usage: make docker module=<module-name> tag=<image-tag-name>"
	@exit 1
endif
else
	@echo "Usage: make docker module=<module-name> tag=<image-tag-name>"
	@exit 1
endif
.PHONY: docker

default: up