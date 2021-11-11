############################################################
# Dockerfile to run Segrada Containers - get locally created image e.g. via maven
#
# Based on JRE8 Image
# Get Docker image via "docker pull ronix/segrada"
# You can mount a volume /segrada_data to make data persistent
# Build using docker build --rm -t ronix/segrada .
# Run like this:
# docker run --name segrada -p 8080:8080 ronix/segrada
# or
# docker run --name segrada -p 8080:8080 -v path_to/segrada_data:/usr/local/segrada/segrada_data ronix/segrada
# or (using environmental variables)
# docker run -e "SEGRADA_ORIENTDB_URL=remote:localhost/Segrada" ronix/segrada
############################################################

# Set the base image to use to Java 8
FROM openjdk:8-jre-slim

# add our user and group first to make sure their IDs get assigned consistently, regardless of whatever dependencies get added
RUN groupadd -r segrada && useradd -r -g segrada segrada

ENV SEGRADA_HOME /usr/local/segrada
ENV PATH $SEGRADA_HOME:$PATH
RUN mkdir -p "$SEGRADA_HOME"
WORKDIR $SEGRADA_HOME

# Set the file maintainer
MAINTAINER Maximilian Kalus

ENV SEGRADA_GPG_KEYS \
	#4096R/975157FB 2019-02-28 Maximilian Kalus <info@segrada.org>
	C79663C6388988836529B05AEF4C437F975157FB

RUN apt-get update && apt-get install -y --no-install-recommends gpg gpg-agent dirmngr curl

RUN set -xe \
	&& for key in $SEGRADA_GPG_KEYS; do \
		gpg --keyserver keyserver.ubuntu.com --recv-keys "$key"; \
	done

ENV SEGRADA_DB_TGZ_URL http://segrada.org/fileadmin/downloads/SegradaEmptyDB.tar.gz

RUN set -xe \
	&& curl -SL "$SEGRADA_DB_TGZ_URL" -o SegradaEmptyDB.tar.gz \
	&& curl -SL "$SEGRADA_DB_TGZ_URL.asc" -o SegradaEmptyDB.tar.gz.asc \
	&& gpg --verify --trust-model always SegradaEmptyDB.tar.gz.asc \
	&& tar -xvf SegradaEmptyDB.tar.gz \
	&& chown -R segrada:segrada . \
	&& rm Segrada*.tar.gz*

RUN set -xe && apt-get remove -y --purge gpg dirmngr curl && rm -rf /var/lib/apt/lists/*

ADD target/Segrada.tar.gz .

RUN set -xe \
	&& chown -R segrada:segrada Segrada \
	&& rm Segrada/*.exe && mv Segrada/* . \
	&& rmdir Segrada

# Variables of Segrada can be set as defined in environmental variables doc.

# Port to expose (default: 8080)
EXPOSE 8080
VOLUME ["/usr/local/segrada/segrada_data"]
USER segrada
ENTRYPOINT ["/usr/local/openjdk-8/bin/java", "-jar", "./segrada-1.0-SNAPSHOT.jar"]
CMD ["headless"]
