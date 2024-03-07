#
# Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
#
# This work is licensed under the Fraunhofer License (on the basis of the MIT license)
# that can be found in the LICENSE file.
#

# Build webinterface
FROM openjdk:17 as build

COPY watermarker /app/watermarker
COPY webinterface /app/webinterface

WORKDIR /app/watermarker
RUN microdnf install findutils
RUN ./gradlew publishToMavenLocal

WORKDIR /app/webinterface
RUN ./gradlew clean zip

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
