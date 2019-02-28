############################################################
# Docker file to run Segrada Containers - get image from Web
#
# It will load Segrada and create a server instance (no local db image created).
# Used in Compose file.
#
# Based on JRE8 Image
############################################################

# Set the base image to use to Java 8
FROM java:8-jre

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

RUN set -xe \
	&& for key in $SEGRADA_GPG_KEYS; do \
		gpg --keyserver ha.pool.sks-keyservers.net --recv-keys "$key"; \
	done

ENV SEGRADA_TGZ_URL http://segrada.org/fileadmin/downloads/Segrada.tar.gz

RUN set -xe \
	&& curl -SL "$SEGRADA_TGZ_URL" -o Segrada.tar.gz \
	&& curl -SL "$SEGRADA_TGZ_URL.asc" -o Segrada.tar.gz.asc \
	&& gpg --verify --trust-model always Segrada.tar.gz.asc \
	&& tar -xvf Segrada.tar.gz \
	&& chown -R segrada:segrada . \
	&& mv Segrada/* . \
	&& rm Segrada*.tar.gz*

# Variables of Segrada can be set as defined in environmental variables doc.

# Port to expose (default: 8080)
EXPOSE 8080
VOLUME ["/usr/local/segrada/segrada_data"]
USER segrada
ENTRYPOINT ["/usr/bin/java", "-jar", "./segrada-1.0-SNAPSHOT.jar"]
CMD ["headless"]
