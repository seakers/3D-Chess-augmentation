###############################################################################
# Project:        TATC for TSE
# Created by:     Joey Gurganus
# Date:           2019.04.24
###############################################################################

PROJECT := tatc_tse

# Define the command line invocatoin of Maven if necessary
ifeq ($(MVN),)
    MVN := mvn
endif

HAS_MAVEN :=$(shell which $(MVN))
ifeq ($(HAS_MAVEN),)
    $(error "No maven installed.   Please install it")
endif

INSTALLED_FLAG1 =  -Dfile=./lib/mopAOS-1.0.jar -DgroupId=seakers \
                 -DartifactId=mopAOS -Dversion=1.0 -Dpackaging=jar

INSTALLED_CMD1 = $(MVN) install:install-file $(INSTALLED_FLAG1)

INSTALLED_FLAG2 =  -Dfile=./lib/conMOP-1.0.jar -DgroupId=seakers \
                 -DartifactId=conMOP -Dversion=1.0 -Dpackaging=jar

INSTALLED_CMD2 = $(MVN) install:install-file $(INSTALLED_FLAG2)

all: install
	$(MVN) package

install:
	@$(INSTALLED_CMD1)
	@$(INSTALLED_CMD2)
	@echo "Installing Python setup..."
	@pip install -e .

runtest:
	@echo "Running test - TBD"
	$(MVN) test 

clean:
	@echo "Cleaning up..."
	@find . -name "*.pyc" -delete
	@find . -type d -name __pycache__ -print0 | xargs -0 rm -rf
	@$(MVN) clean

bare: clean
	@echo "Removing the python package and problems/arch-* directories"
	@pip uninstall -y $(PROJECT) 
	@rm -rf $(PROJECT).egg-info .eggs
	@find problems -type d -name arch\* -exec rm -rf {} \; 2> /dev/null
