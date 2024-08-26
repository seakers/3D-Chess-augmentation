# TAT-C Docker Container

This directory contains all the necessary dependancies to build a docker image that runs TAT-C, build a redis docker image, and host a GUI through localhost


## Getting Started

These instructions will build a TAT-C docker image and a redis docker image using docker compose

### Prerequisites

Docker desktop must be installed on your local machine. <br>
Download for your specific OS from: https://www.docker.com/products/docker-desktop

```
Docker Desktop
```

### Building images with docker compose


1. Change working directoy to root directory of TAT-C

```
cd /path/to/tat-c
```

2. Set where you want TAT-C output to be placed!!
    There is a docker-compose.yml file that contains code to set up a network between the tat-c and redis image. This file contains a place to specify where
    you want the TAT-C docker container to place its output. Please see an example of the file with notes below

```
version: '3.7'
services:
  tat-c:
    build:
      context: .
      dockerfile: ./container/Dockerfile
    volumes:
      - type: bind
        source: /Users/gapaza/Output/DockerOutput !!! Here you will specify where you want TAT-C to place its output !!!
        target: /mission
    ports:
      - "80:80"
  redis:
    image: "redislabs/rejson"
```
**WARNING** When you run TAT-C, the docker container deletes everything in the directory that you specified for output!!!

3. Run the docker compose "up" command to start building the images (takes 20-40 min depending on the computer)

```
docker-compose up
```

4. Now the image should be up and running! 


### Viewing / Using TAT-C GUI

Open a web brower (Chrome preferred), and naviage to localhost. Below are the URLs for the different pages

```
Create Run Page:   localhost
```
```
View Results Page: localhost/data
```





