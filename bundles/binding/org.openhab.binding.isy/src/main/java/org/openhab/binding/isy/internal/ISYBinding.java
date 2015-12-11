/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.isy.internal;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.isy.ISYBindingProvider;
import org.openhab.binding.isy.internal.ISYGenericBindingProvider.ISYBindingConfig;
import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
	

/**
 * Implement this class if you are going create an actively polling service
 * like querying a Website/Device.
 * 
 * @author Jason Marchioni
 * @since 1.8.0-SNAPSHOT
 */
public class ISYBinding extends AbstractActiveBinding<ISYBindingProvider> {

	private static final Logger logger = 
		LoggerFactory.getLogger(ISYBinding.class);

	/**
	 * The BundleContext. This is only valid when the bundle is ACTIVE. It is set in the activate()
	 * method and must not be accessed anymore once the deactivate() method was called or before activate()
	 * was called.
	 */
	private BundleContext bundleContext;

	
	/** 
	 * the refresh interval which is used to poll values from the ISY
	 * server (optional, defaults to 60000ms)
	 */
	private long refreshInterval = 60000;
	
	private ISYClient isyClient;
	
	public ISYBinding() {			
	}
		
	
	/**
	 * Called by the SCR to activate the component with its configuration read from CAS
	 * 
	 * @param bundleContext BundleContext of the Bundle that defines this component
	 * @param configuration Configuration properties for this component obtained from the ConfigAdmin service
	 */
	public void activate(final BundleContext bundleContext, final Map<String, Object> configuration) {
		this.bundleContext = bundleContext;

		// the configuration is guaranteed not to be null, because the component definition has the
		// configuration-policy set to require. If set to 'optional' then the configuration may be null
		
		if( configuration.containsKey("host")){
			String host = configuration.get("host").toString().trim();
			int port = Integer.valueOf(configuration.getOrDefault("port", 80).toString().trim());
			String username = null;
			String password = null;
			if(configuration.containsKey("username") && configuration.containsKey("password")){
				username = configuration.get("username").toString().trim();
				password = configuration.get("password").toString().trim();
			}
			
			isyClient = new ISYClient(host, port, username, password);
			isyClient.subscribe();
		}
		
		// to override the default refresh interval one has to add a 
		// parameter to openhab.cfg like <bindingName>:refresh=<intervalInMs>
		String refreshIntervalString = (String) configuration.get("refresh");
		if (StringUtils.isNotBlank(refreshIntervalString)) {
			refreshInterval = Long.parseLong(refreshIntervalString);
		}
		
		// read further config parameters here ...		
		setProperlyConfigured(isyClient != null);
	}
	
	/**
	 * Called by the SCR when the configuration of a binding has been changed through the ConfigAdmin service.
	 * @param configuration Updated configuration properties
	 */
	public void modified(final Map<String, Object> configuration) {
		isyClient = null;
	}
	
	/**
	 * Called by the SCR to deactivate the component when either the configuration is removed or
	 * mandatory references are no longer satisfied or the component has simply been stopped.
	 * @param reason Reason code for the deactivation:<br>
	 * <ul>
	 * <li> 0 – Unspecified
     * <li> 1 – The component was disabled
     * <li> 2 – A reference became unsatisfied
     * <li> 3 – A configuration was changed
     * <li> 4 – A configuration was deleted
     * <li> 5 – The component was disposed
     * <li> 6 – The bundle was stopped
     * </ul>
	 */
	public void deactivate(final int reason) {
		this.bundleContext = null;
		// deallocate resources here that are no longer needed and 
		// should be reset when activating this binding again
		isyClient.unsubscribe();
		isyClient = null;
	}

	
	/**
	 * @{inheritDoc}
	 */
	@Override
	protected long getRefreshInterval() {
		return refreshInterval;
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	protected String getName() {
		return "ISY";
	}
	
	/**
	 * @{inheritDoc}
	 */
	@Override
	protected void execute() {
		EventPublisher eventPublisher = lookupEventPublisher();
		if(eventPublisher != null && isyClient != null){
			Map<String,String> statusAll = isyClient.statusAll();
			for(Entry<String,String> itemStatus: statusAll.entrySet()){
				for(ISYBindingProvider provider: this.providers){
					ISYBindingConfig isyItem = provider.getItemConfigByAddress(itemStatus.getKey());
					if(isyItem != null){
						int status = 0;
						try{
							status = Integer.valueOf(itemStatus.getValue());
						}catch(NumberFormatException e){
							logger.error("Not able to parse status {} for {}", itemStatus.getValue(), isyItem.getItemName());
						}
						if(logger.isDebugEnabled())
							logger.debug("Status Update {}:{}", isyItem.getItemName(), status);							
						if(isyItem.getItemType().equals(DimmerItem.class)){
							eventPublisher.postUpdate(isyItem.getItemName(), new PercentType((int) ((status / 255f) * 100)));
						}else if(isyItem.getItemType().equals(SwitchItem.class)){
							eventPublisher.postUpdate(isyItem.getItemName(), status > 0 ? OnOffType.ON : OnOffType.OFF);
						}else if(isyItem.getItemType().equals(ContactItem.class)){
							eventPublisher.postUpdate(isyItem.getItemName(), status > 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
						}
					}
				}
			}			
		}
	}
	
	private EventPublisher lookupEventPublisher(){
		if(bundleContext == null)
			return null;					
		ServiceReference serviceRef = bundleContext.getServiceReference("org.openhab.core.events.EventPublisher");
		return serviceRef != null ? (EventPublisher) bundleContext.getService(serviceRef) : null;
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	protected void internalReceiveCommand(String itemName, Command command) {
		// the code being executed when a command was sent on the openHAB
		// event bus goes here. This method is only called if one of the 
		// BindingProviders provide a binding for the given 'itemName'.
		logger.debug("internalReceiveCommand({},{}) is called!", itemName, command);
		
		for(ISYBindingProvider provider: this.providers){
			ISYBindingConfig config = provider.getItemConfig(itemName);
			if(config != null){
				logger.debug("Command {} {}", config.getItemName(), command.toString());
				if(command instanceof OnOffType){
					OnOffType action = (OnOffType) command;
					if(action.equals(OnOffType.ON)){
						isyClient.on(config.getAddress());
					}else{
						isyClient.off(config.getAddress());
					}
				}else if(command instanceof PercentType){
					PercentType action = (PercentType) command;
					isyClient.dim(config.getAddress(), (int)((action.intValue() / 100f) * 255));
				}else if(command instanceof IncreaseDecreaseType){
					
				}
				break;
			}
		}
		
	}
	
	/**
	 * @{inheritDoc}
	 */
	@Override
	protected void internalReceiveUpdate(String itemName, State newState) {
		// the code being executed when a state was sent on the openHAB
		// event bus goes here. This method is only called if one of the 
		// BindingProviders provide a binding for the given 'itemName'.
		logger.debug("internalReceiveUpdate({},{}) is called!", itemName, newState);
	}

}
