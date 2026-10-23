FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-27@sha256:885babd6714bd6776d43723be30334ec67aad52be5804d9318e6f2b94f950f89

ENV TZ="Europe/Oslo"

COPY build/install/*/lib /app/lib

ENTRYPOINT ["java", "-cp", "/app/lib/*", "no.nav.dagpenger.vaktmester.mellomlagring.AppKt"]