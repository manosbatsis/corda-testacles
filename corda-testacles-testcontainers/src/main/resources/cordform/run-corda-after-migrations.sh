#!/usr/bin/env bash

count=$(grep -c dataSourceProperties /etc/corda/node.conf)

if [ "$count" -eq 0 ]; then
  java -Djava.security.egd=file:/dev/./urandom -Dcorda.dataSourceProperties.dataSource.url="jdbc:h2:file:/etc/corda/persistence;DB_CLOSE_ON_EXIT=FALSE;WRITE_DELAY=0;LOCK_TIMEOUT=10000" -jar /opt/corda/bin/corda.jar run-migration-scripts --core-schemas --app-schemas --base-directory /opt/corda --config-file /etc/corda/node.conf
else
  java -Djava.security.egd=file:/dev/./urandom -jar /opt/corda/bin/corda.jar run-migration-scripts --core-schemas --app-schemas --base-directory /opt/corda --config-file /etc/corda/node.conf
fi

/opt/corda/bin/run-corda
