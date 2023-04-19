package org.acme.test;

import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.supplier.AgroalConnectionFactoryConfigurationSupplier;
import io.agroal.api.configuration.supplier.AgroalConnectionPoolConfigurationSupplier;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.agroal.api.configuration.supplier.AgroalPropertiesReader;
import io.agroal.api.security.NamePrincipal;
import io.agroal.api.security.SimplePassword;
import io.agroal.api.transaction.TransactionIntegration;
import io.agroal.narayana.NarayanaTransactionIntegration;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.customized.QuarkusConnectionProvider;
import io.quarkus.hibernate.orm.runtime.tenant.TenantConnectionResolver;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import java.sql.SQLException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * @author saka
 */
@ApplicationScoped
@PersistenceUnitExtension
public class MyTenantConnectionResolver implements TenantConnectionResolver {
	private Logger log = Logger.getLogger(MyTenantConnectionResolver.class);
	@ConfigProperty(name = "quarkus.datasource.username")
	String username;
	@ConfigProperty(name = "quarkus.datasource.password")
	String password;
	@Inject
	TransactionManager transactionManager;
	@Inject
	TransactionSynchronizationRegistry transactionSynchronizationRegistry;

	@Override
	public ConnectionProvider resolve(String tenantId) {
		return new QuarkusConnectionProvider(createDatasource(tenantId));
	}

	private AgroalDataSource createDatasource(String tenantId) {

		try {

			AgroalDataSourceConfigurationSupplier dataSourceConfiguration = new AgroalDataSourceConfigurationSupplier();

			AgroalConnectionPoolConfigurationSupplier poolConfiguration = dataSourceConfiguration.connectionPoolConfiguration();

			TransactionIntegration txIntegration = new NarayanaTransactionIntegration(transactionManager, transactionSynchronizationRegistry, null, false, null);
			poolConfiguration
					  .initialSize(2)
					  .maxSize(10)
					  .minSize(2)
					  .maxLifetime(Duration.of(5, ChronoUnit.MINUTES))
					  .acquisitionTimeout(Duration.of(30, ChronoUnit.SECONDS))
					  .transactionIntegration(txIntegration);

			AgroalConnectionFactoryConfigurationSupplier connectionFactoryConfiguration = poolConfiguration.connectionFactoryConfiguration();

			connectionFactoryConfiguration
					  .jdbcUrl("jdbc:mariadb://localhost:3306/country_" + tenantId)
					  .credential(new NamePrincipal(username))
					  .credential(new SimplePassword(password));


			AgroalDataSource datasource = AgroalDataSource.from(dataSourceConfiguration.get());
			return datasource;
		} catch (SQLException ex) {
			throw new IllegalStateException("Failed to create a new data source based on the existing datasource configuration", ex);
		}
	}

}

