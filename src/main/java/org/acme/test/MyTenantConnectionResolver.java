package org.acme.test;

import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.supplier.AgroalPropertiesReader;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.customized.QuarkusConnectionProvider;
import io.quarkus.hibernate.orm.runtime.tenant.TenantConnectionResolver;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author saka
 */
@ApplicationScoped
@PersistenceUnitExtension
public class MyTenantConnectionResolver implements TenantConnectionResolver {
	private Logger log = Logger.getLogger(MyTenantConnectionResolver.class);
	@ConfigProperty(name = "quarkus.datasource.tenant.username")
	String username;
	@ConfigProperty(name = "quarkus.datasource.tenant.password")
	String password;
	@Override
	public ConnectionProvider resolve(String tenantId) {
		return new QuarkusConnectionProvider(createDatasource(tenantId));
	}

	private AgroalDataSource createDatasource(String tenantId) {
		

		try {
			Map<String, String> props = new HashMap<>();

			props.put(AgroalPropertiesReader.MAX_SIZE, "10");
			props.put(AgroalPropertiesReader.MIN_SIZE, "2");
			props.put(AgroalPropertiesReader.INITIAL_SIZE, "2");
			props.put(AgroalPropertiesReader.MAX_LIFETIME_S, "57");
			props.put(AgroalPropertiesReader.ACQUISITION_TIMEOUT_S, "54");
			props.put(AgroalPropertiesReader.PROVIDER_CLASS_NAME, "org.mariadb.jdbc.Driver");
			props.put(AgroalPropertiesReader.JDBC_URL, "jdbc:mariadb://localhost:3306/country_" + tenantId);
			props.put(AgroalPropertiesReader.PRINCIPAL, username);
			props.put(AgroalPropertiesReader.CREDENTIAL, password);

			AgroalDataSource datasource = AgroalDataSource.from(new AgroalPropertiesReader()
					  .readProperties(props)
					  .get());

//			long activeCount = datasource.getMetrics().activeCount();
//			long availableCount = datasource.getMetrics().availableCount();
			log.infof("datasource for tenant %s objectId %s healthy status %s", tenantId,System.identityHashCode(datasource), datasource.isHealthy(false));
			return datasource;
		} catch (SQLException ex) {
			throw new IllegalStateException("Failed to create a new data source based on the existing datasource configuration",
					  ex);
		}
	}

}

