FROM centos:7
MAINTAINER "Manuel RUSSO <manuelrusso@laposte.net>"

# Setting French timezone
RUN rm /etc/localtime
RUN ln -s /usr/share/zoneinfo/Europe/Paris /etc/localtime
RUN date

# Install Java
RUN yum install -y java-1.8.0-openjdk-devel
RUN java -version

# Install Maven
RUN yum install -y maven
RUN mvn -version

# Prepare launch
RUN yum install -y mysql
COPY ./data/initdb.sql /initdb.sql
COPY ./docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh
	
# Create a directory where our app will be placed
RUN mkdir -p /usr/src/app

# Change directory so that our commands run inside this new directory
WORKDIR /usr/src/app

# Copy dependency definitions
COPY . /usr/src/app

RUN mvn clean package

ENTRYPOINT ["/docker-entrypoint.sh"]
