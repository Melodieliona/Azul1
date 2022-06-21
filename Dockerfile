# This is a Docker image that can be used by projects
# as base for the Docker image in which their tests are run.
# It should be pushed to the repository's Docker registry.
#
# Commands for updating the image:
# docker login registry.gitlab.com -u <username>
# docker build -t registry.gitlab.com/sosy-lab/teaching/sep-template-group-project .
# docker push registry.gitlab.com/sosy-lab/teaching/sep-template-group-project

FROM openjdk:18-jdk-slim

RUN apt-get update && apt-get install -y \
  wget \
  unzip

# download latest JavaFX, SpotBugs, and CheckStyle and install them into /opt

RUN mkdir -p opt

 RUN cd /opt \
   && wget https://download2.gluonhq.com/openjfx/18.0.1/openjfx-18.0.1_linux-x64_bin-sdk.zip \
   && unzip openjfx-18.0.1_linux-x64_bin-sdk.zip \
   && rm openjfx-18.0.1_linux-x64_bin-sdk.zip

ENV JAVAFX_PATH=/opt/javafx-sdk-18.0.1/lib/
ENV CLASSPATH="$JAVAFX_PATH*":$CLASSPATH

RUN cd /opt \
  && wget https://github.com/spotbugs/spotbugs/releases/download/4.7.0/spotbugs-4.7.0.zip \
  && unzip spotbugs-4.7.0.zip \
  && rm spotbugs-4.7.0.zip \
  && chmod a+x spotbugs-4.7.0/bin/spotbugs

ENV PATH="/opt/spotbugs-4.7.0/bin:$PATH"

RUN cd /opt \
  && wget https://github.com/checkstyle/checkstyle/releases/download/checkstyle-10.3/checkstyle-10.3-all.jar \
  && wget https://raw.githubusercontent.com/checkstyle/checkstyle/master/src/main/resources/google_checks.xml \
  && sed -i 's/warning/error/g' google_checks.xml

RUN cd /opt \
  && wget https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.8.2/junit-platform-console-standalone-1.8.2.jar

ENV JUNIT5_PATH=/opt/junit-platform-console-standalone-1.8.2.jar
ENV CLASSPATH=$JUNIT5_PATH:$CLASSPATH

