/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.isy.internal;

import java.util.Collection;
import java.util.Map;

import org.openhab.binding.isy.ISYBindingProvider;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.types.Type;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;

import jersey.repackaged.com.google.common.collect.BiMap;
import jersey.repackaged.com.google.common.collect.Lists;
import jersey.repackaged.com.google.common.collect.Maps;

/**
 * This class is responsible for parsing the binding configuration.
 * 
 * @author Jason Marchioni
 * @since 1.8.0-SNAPSHOT
 */
public class ISYGenericBindingProvider extends AbstractGenericBindingProvider implements ISYBindingProvider {

	private Map<String, ISYBindingConfig> itemsByAddress = Maps.newHashMap();

	/**
	 * {@inheritDoc}
	 */
	public String getBindingType() {
		return "isy";

	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	public void validateItemType(Item item, String bindingConfig) throws BindingConfigParseException {
		if (!(item instanceof SwitchItem || item instanceof DimmerItem || item instanceof ContactItem)) {
			throw new BindingConfigParseException("item '" + item.getName() + "' is of type '"
					+ item.getClass().getSimpleName()
					+ "', only Switch, Dimmer and Contact Items are allowed - please check your *.items configuration");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processBindingConfiguration(String context, Item item, String bindingConfig)
			throws BindingConfigParseException {
		super.processBindingConfiguration(context, item, bindingConfig);
		try {
			addBindingConfig(item, ISYBindingConfig.create(item, bindingConfig));
		} catch (Exception e) {
			throw new BindingConfigParseException("Error parsing binding config");
		}
	}
	
	@Override
	protected void addBindingConfig(Item item, BindingConfig config) {
		super.addBindingConfig(item, config);
		ISYBindingConfig isyConfig = (ISYBindingConfig)config;
		itemsByAddress.put(isyConfig.getAddress(), isyConfig);
	}

	@Override
	public ISYBindingConfig getItemConfig(String itemName) {
		return (ISYBindingConfig) this.bindingConfigs.get(itemName);
	}

	@Override
	public ISYBindingConfig getItemConfigByAddress(String addr) {
		return itemsByAddress.get(addr);
	}

	@Override
	public Collection<BindingConfig> getConfiguredItems() {
		return this.bindingConfigs.values();
	}

	/**
	 * This is a helper class holding binding specific configuration details
	 * 
	 * @author Jason Marchioni
	 * @since 1.8.0-SNAPSHOT
	 */
	public static class ISYBindingConfig implements BindingConfig {
		// put member fields here which holds the parsed values

		private String itemName;
		private String address;
		private Class<? extends Item> itemType;

		public String getItemName() {
			return itemName;
		}

		public void setItemName(String itemName) {
			this.itemName = itemName;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public Class<? extends Item> getItemType() {
			return itemType;
		}

		public void setItemType(Class<? extends Item> itemType) {
			this.itemType = itemType;
		}

		public static ISYBindingConfig create(Item item, String bindingConfig) {
			ISYBindingConfig config = new ISYBindingConfig();

			config.setItemName(item.getName());
			config.setItemType(item.getClass());

			// Parse parameters
			String[] params = bindingConfig.split(",");
			for (String param : params) {
				String[] kv = param.split("=");
				if (kv[0].equals("addr")) {
					// Parse address in the form XX.XX.XX.XX
					config.setAddress(kv[1].replaceAll("\\.", " "));
				}
			}

			return config;
		}

	}

}
