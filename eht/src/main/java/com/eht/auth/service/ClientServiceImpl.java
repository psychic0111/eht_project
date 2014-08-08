package com.eht.auth.service;

import java.util.List;

import org.springframework.security.oauth2.provider.ClientAlreadyExistsException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.ClientRegistrationService;
import org.springframework.security.oauth2.provider.NoSuchClientException;

public class ClientServiceImpl implements ClientDetailsService, ClientRegistrationService{

	@Override
	public void addClientDetails(ClientDetails paramClientDetails) throws ClientAlreadyExistsException {
		
	}

	@Override
	public void updateClientDetails(ClientDetails paramClientDetails) throws NoSuchClientException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateClientSecret(String paramString1, String paramString2) throws NoSuchClientException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeClientDetails(String paramString) throws NoSuchClientException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<ClientDetails> listClientDetails() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientDetails loadClientByClientId(String paramString) throws ClientRegistrationException {
		// TODO Auto-generated method stub
		return null;
	}

}
