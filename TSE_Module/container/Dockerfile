ARG CENTOS_VERSION=7
FROM centos:${CENTOS_VERSION}


# Define Environment Variables
ENV NAME tatc_v2



# Define directories to be created in container
WORKDIR /environment
WORKDIR /tat-c
WORKDIR /flask


# Copy files to container
COPY ./container/environment    /environment
COPY ./demo          /tat-c/demo
COPY ./docs          /tat-c/docs
COPY ./interfaces    /tat-c/interfaces
COPY ./knowledgebase /tat-c/knowledgebase
COPY ./modules       /tat-c/modules
COPY ./tse           /tat-c/tse
COPY ./Makefile      /tat-c/
COPY ./container/flask          /flask



# Install all packages for TAT-C
RUN yum -y --setopt=tsflags=nodocs update && \
	yum -y --setopt=tsflags=nodocs install httpd && \
	yum -y install epel-release && \ 
	yum -y install gcc && \
	yum -y install gcc-gfortran && \
	yum -y install gcc-c++ && \
	yum -y install make && \
	yum -y install which && \
	yum -y install bzip2 && \
	yum -y install wget && \
	yum -y install maven && \
	cd /environment && \
	wget https://repo.anaconda.com/miniconda/Miniconda3-latest-Linux-x86_64.sh && \
	chmod +x Miniconda3-latest-Linux-x86_64.sh && \
	./Miniconda3-latest-Linux-x86_64.sh -b -p /environment/miniconda3


# Create miniconda environment / set environment variables
ENV PATH /environment/miniconda3/bin:$PATH
RUN conda update -n base -c defaults conda -y        && \
	conda env create -f /environment/environment.yml && \
	conda config --set auto_activate_base false
ENV PATH /environment/miniconda3/envs/python3_tatc/bin:$PATH


# Make miniconda env avaliable on container boot
RUN touch /etc/profile.d/miniconda.sh && \
	echo ". /environment/miniconda3/etc/profile.d/conda.sh" >> /etc/profile.d/miniconda.sh && \
	echo "conda activate python3_tatc" >> ~/.bashrc && \
	conda init bash


# Compile TAT-C
RUN   cd /tat-c/ && \
	  make bare && \
	  make all




# Expost port 80 for the TAT-C GUI
EXPOSE 80

# reset execution bit when building from Windows platform
RUN chmod 644 /flask/index.py

# Command when the container starts
CMD python /flask/index.py





