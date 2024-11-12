# syntax=docker/dockerfile:1.7-labs
# for COPY --exclude support
FROM gradle:8.10.0-jdk21 AS gradle
# TODO: copies too much, breaks caching
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN --mount=type=cache,target=/home/gradle/.gradle/caches gradle zummon:jsBrowserProductionLibraryDistribution --no-daemon

FROM node:22 AS node
ARG VITE_ZTOR_URL

RUN mkdir -p /app/frontend
RUN mkdir -p /app/build/js/packages
WORKDIR /app/frontend
COPY --from=gradle /home/gradle/src/build/js /app/build/js

# first do this so it is cached in a docker layer
COPY frontend/package.json frontend/package-lock.json ./
RUN --mount=type=cache,target=/root/.npm npm install

COPY --exclude=frontend/dist --exclude=frontend/node_modules frontend* ./
RUN npm run build

FROM caddy:2.8.4

COPY docker/production/frontend/Caddyfile /etc/caddy/Caddyfile
COPY --from=node /app/frontend/dist /srv
RUN caddy validate --config /etc/caddy/Caddyfile

CMD caddy run --config /etc/caddy/Caddyfile
