package com.motd.be.common.filter.security_filter;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class CustomAuthentication implements Authentication {

	private final String id;
	private final Collection<? extends GrantedAuthority> authorities;
	private boolean isAuthenticated;

	public CustomAuthentication(String id, String role) {
		this.id = id;
		this.authorities = Collections.singleton(new SimpleGrantedAuthority(role));
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public Object getCredentials() {
		return null;
	}

	@Override
	public Object getDetails() {
		return null;
	}

	@Override
	public Object getPrincipal() {
		return getName();
	}

	@Override
	public boolean isAuthenticated() {
		return isAuthenticated;
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
		this.isAuthenticated = isAuthenticated;
	}

	@Override
	public String getName() {
		return id;
	}

}
