FROM tomee:10.1.0-jre17-alpine-plume

# Copy the WAR file
COPY target/banking-change-1.0-SNAPSHOT.war /usr/local/tomee/webapps/ROOT.war

COPY src/main/tomee/conf/* /usr/local/tomee/conf/

# Expose ports
EXPOSE 8080 8005

# Set environment variables
ENV CATALINA_OPTS="-Xmx512m -Dtomee.remote.support=true -Dopenejb.cdi.activated=true -Dtomee.serialization.class.blacklist=- -Dopenejb.system.apps=true"