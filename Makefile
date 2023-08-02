PHONY: help

# Ref: https://gist.github.com/prwhite/8168133
help:  ## display help
	@awk 'BEGIN {FS = ":.*##"; printf "\nUsage:\n  make \033[36m<target>\033[0m\n\nTargets:\n"} \
		/^[a-zA-Z_-]+:.*?##/ { printf "  \033[36m%-10s\033[0m %s\n", $$1, $$2 }' $(MAKEFILE_LIST)

run: ## run lingo server
	mvn clean -pl lingo-server spring-boot:run
install: ## run lingo server
	mvn clean install -Dmaven.test.skip
