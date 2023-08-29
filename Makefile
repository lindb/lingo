PHONY: help

# Ref: https://gist.github.com/prwhite/8168133
help:  ## display help
	@awk 'BEGIN {FS = ":.*##"; printf "\nUsage:\n  make \033[36m<target>\033[0m\n\nTargets:\n"} \
		/^[a-zA-Z_-]+:.*?##/ { printf "  \033[36m%-10s\033[0m %s\n", $$1, $$2 }' $(MAKEFILE_LIST)

header: ## check and add license header.
	mvn license:format

test: header## run test cases
	# mvn test -Dtest=WriteImplTest
	mvn test

run: ## run lingo server
	mvn clean -pl lingo-server spring-boot:run

build: header ## build lingo server
	mvn clean package -Dmaven.test.skip

install: ## run lingo server
	mvn clean install -Dmaven.test.skip
