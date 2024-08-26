# Project:        TAT-C
# Created by:     Joey Gurganus
# Date:           2019.04.30


# Define macros for locations
SUBS = modules demo tse

all install clean bare runtest:
	-X=`pwd`; \
	for i in $(SUBS); \
	do echo '<<<' $$i '>>>'; cd $$X/$$i; make $@; done
