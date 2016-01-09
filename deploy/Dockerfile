############################################################
# Dockerfile to run Segrada Containers
# Based on JRE8 Image
# Get Docker image via "docker pull ronix/segrada"
############################################################

# Set the base image to use to Java 8
FROM java:8-jre

# add our user and group first to make sure their IDs get assigned consistently, regardless of whatever dependencies get added
RUN groupadd -r segrada && useradd -r -g segrada segrada

ENV SEGRADA_HOME /usr/local/segrada
ENV PATH $SEGRADA_HOME/bin:$PATH
RUN mkdir -p "$SEGRADA_HOME"
WORKDIR $SEGRADA_HOME

# Set the file maintainer
MAINTAINER Maximilian Kalus

ENV SEGRADA_GPG_KEYS \
	#2048R/9CEB26EE 2016-01-09 Maximilian Kalus <info@segrada.org>
	D94CE3401E6E76AE

RUN set -xe \
	&& for key in $SEGRADA_GPG_KEYS; do \
		gpg --keyserver ha.pool.sks-keyservers.net --recv-keys "$key"; \
	done

ENV SEGRADA_TGZ_URL http://segrada.org/fileadmin/downloads/Segrada.tgz

RUN set -xe \
	&& curl -SL "$SEGRADA_TGZ_URL" -o Segrada.tgz \
	&& curl -SL "$SEGRADA_TGZ_URL.asc" -o Segrada.tgz.asc \
	&& gpg --verify Segrada.tgz.asc \
	&& tar -xvf Segrada.tgz \
	&& mv Segrada db \
	&& chown -R segrada:segrada db \
	&& rm Segrada.tgz*

# Port to expose (default: 8080)
EXPOSE 8080
USER segrada
ENTRYPOINT ["/usr/local/segrada/db/start.sh"]
CMD ["headless"]
