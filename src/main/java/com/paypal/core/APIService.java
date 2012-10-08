package com.paypal.core;

import java.io.IOException;
import java.util.Map;

import com.paypal.core.credential.CertificateCredential;
import com.paypal.exception.ClientActionRequiredException;
import com.paypal.exception.HttpErrorException;
import com.paypal.exception.InvalidCredentialException;
import com.paypal.exception.InvalidResponseDataException;
import com.paypal.exception.MissingCredentialException;
import com.paypal.exception.SSLConfigurationException;
import com.paypal.sdk.exceptions.OAuthException;

/**
 * Wrapper class for api calls
 * 
 */
public class APIService {

	/**
	 * Service Name
	 */
	private String serviceName;

	/**
	 * Service Endpoint
	 */
	private String endPoint;

	/**
	 * Configuration Manager
	 */
	private ConfigManager config = null;

	/**
	 * HttpConfiguration
	 */
	private HttpConfiguration httpConfiguration = new HttpConfiguration();

	/**
	 * Set all the HTTP related parameters from the configutation file
	 * 
	 * @param serviceName
	 *            Service name
	 * @throws SSLConfigurationException
	 * @throws NumberFormatException
	 */
	public APIService(String serviceName) {
		this.serviceName = serviceName;
		config = ConfigManager.getInstance();
		endPoint = config.getValue(Constants.END_POINT);
		httpConfiguration.setGoogleAppEngine(Boolean.parseBoolean(config
				.getValue(Constants.GOOGLE_APP_ENGINE)));
		if (Boolean.parseBoolean(config.getValue(Constants.USE_HTTP_PROXY))) {
			httpConfiguration.setProxyPort(Integer.parseInt(config
					.getValue(Constants.HTTP_PROXY_PORT)));
			httpConfiguration.setProxyHost(config
					.getValue(Constants.HTTP_PROXY_HOST));
			httpConfiguration.setProxyUserName(config
					.getValue(Constants.HTTP_PROXY_USERNAME));
			httpConfiguration.setProxyPassword(config
					.getValue(Constants.HTTP_PROXY_PASSWORD));
		}
		httpConfiguration.setConnectionTimeout(Integer.parseInt(config
				.getValue(Constants.HTTP_CONNECTION_TIMEOUT)));
		httpConfiguration.setMaxRetry(Integer.parseInt(config
				.getValue(Constants.HTTP_CONNECTION_RETRY)));
		httpConfiguration.setReadTimeout(Integer.parseInt(config
				.getValue(Constants.HTTP_CONNECTION_READ_TIMEOUT)));
		httpConfiguration.setMaxHttpConnection(Integer.parseInt(config
				.getValue(Constants.HTTP_CONNECTION_MAX_CONNECTION)));
	}

	/**
	 * Makes a request to API service
	 * 
	 * @param apiCallPreHandler
	 *            API Call specific handler
	 * @return Response from API as string
	 * @throws InvalidResponseDataException
	 * @throws HttpErrorException
	 * @throws ClientActionRequiredException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws InvalidCredentialException
	 * @throws MissingCredentialException
	 * @throws OAuthException
	 * @throws SSLConfigurationException
	 */
	public String makeRequestUsing(APICallPreHandler apiCallPreHandler)
			throws InvalidResponseDataException, HttpErrorException,
			ClientActionRequiredException, IOException, InterruptedException,
			InvalidCredentialException, MissingCredentialException,
			OAuthException, SSLConfigurationException {

		/*
		 * The implementation is transparent to API request format NVP or SOAP,
		 * the headers, payload and endpoints are fed by the corresponding
		 * apiCallPreHandlers
		 */
		String response = null;
		Map<String, String> headers = null;
		ConnectionManager connectionMgr = ConnectionManager.getInstance();
		HttpConnection connection = connectionMgr
				.getConnection(httpConfiguration);
		String url = apiCallPreHandler.getEndPoint();
		httpConfiguration.setEndPointUrl(url);
		headers = apiCallPreHandler.getHeader();
		String payLoad = apiCallPreHandler.getPayLoad();
		if (apiCallPreHandler.getCredential() instanceof CertificateCredential) {
			CertificateCredential credential = (CertificateCredential) apiCallPreHandler
					.getCredential();
			String certPath = credential.getCertificatePath();
			String certKey = credential.getCertificateKey();
			connection.setDefaultSSL(false);
			connection.setupClientSSL(certPath, certKey);
		}
		connection.createAndconfigureHttpConnection(httpConfiguration);
		response = connection.execute(url, payLoad, headers);
		LoggingManager.info(APIService.class, response);
		return response;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getEndPoint() {
		return endPoint;
	}

}
