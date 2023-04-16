package org.acme.test;


import io.quarkus.arc.Unremovable;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;
import io.vertx.ext.web.RoutingContext;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.jboss.logging.Logger;

/**
 *
 * @author saka
 */
@PersistenceUnitExtension
@RequestScoped
@Unremovable
public class MyTenantResolver implements TenantResolver {
	private static final Logger log = Logger.getLogger(MyTenantResolver.class);
	@Inject
	RoutingContext context;

	@Override
	public String getDefaultTenantId() {
		return "0";
	}

	@Override
	public String resolveTenantId() {
		String path = context.request().path();
		String[] parts = path.split("/");

		if (parts.length == 0) {
				// resolve to default tenant config
				return getDefaultTenantId();
		}

		return parts[1];
	}

}