package org.acme.test;

import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


@Path("{tenantId}/country")
@Produces(MediaType.APPLICATION_JSON)
public class CountryResource {

	@Inject
	CountryService countryService;

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String hello() {
		return "Hello from RESTEasy Reactive";
	}

	@GET
	@Path("create/{name}")
	@Transactional
	public List<Country> createCountry(@PathParam("name") String name) {
		countryService.createCountry(name);
		return countryService.findCountries();
	}
	
	@GET
	@Path("create_error/{name}")
	@Transactional
	public List<Country> createCountryError(@PathParam("name") String name) {
		countryService.createCountry(name);
		if (true) {
			throw new RuntimeException("Testing database rollback");
		}
		return countryService.findCountries();
	}

	@GET
	@Path("list")
	public List<Country> findAll() {
		return countryService.findCountries();
	}

	
}