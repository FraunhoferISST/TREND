#
# Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
#
# This work is licensed under the Fraunhofer License (on the basis of the MIT license)
# that can be found in the LICENSE file.
#

# Build webinterface
FROM eclipse-temurin:21.0.6_7-jdk-alpine-3.21 AS build

COPY watermarker /app/watermarker
COPY webinterface /app/webinterface

RUN apk add findutils
WORKDIR /app/watermarker
RUN ./gradlew publishToMavenLocal

WORKDIR /app/webinterface
RUN ./gradlew kotlinNodeJsSetup
RUN apk add --no-cache nodejs
RUN ln -s /usr/bin/node ~/.gradle/nodejs/node-v22.0.0-linux-x64/bin/node -f
RUN ./gradlew clean zip -x kotlinNodeJsSetup

# Use unprivileged base image for final execution
FROM nginxinc/nginx-unprivileged:alpine
COPY --from=build /app/webinterface/build/libs/*.zip /usr/share/nginx/html/trend-webinterface.zip

USER root
WORKDIR /usr/share/nginx/html
RUN apk --update add openssl unzip
RUN unzip -o trend-webinterface.zip
RUN rm -rf trend-webinterface.zip
RUN chown -R 101:101 /usr/share/nginx/html/
USER 101

CMD ["nginx", "-g", "daemon off;"]