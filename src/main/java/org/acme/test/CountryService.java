package org.acme.test;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@ApplicationScoped
public class CountryService {
	@Inject
	EntityManager em; 

	
	public void createCountry(String name) {
		Country c = new Country();
		c.setName(name);
		em.persist(c);
	}

	public List<Country> findCountries() {
		return em.createQuery("SELECT a from Country a", Country.class).getResultList();
	}
}
