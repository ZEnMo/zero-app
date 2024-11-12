FROM caddy:2.8.4

COPY Caddyfile /etc/caddy/Caddyfile
RUN caddy validate --config /etc/caddy/Caddyfile

CMD caddy run --config /etc/caddy/Caddyfile
