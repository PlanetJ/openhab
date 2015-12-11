/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.isy;

import java.util.Collection;

import org.openhab.binding.isy.internal.ISYGenericBindingProvider.ISYBindingConfig;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.binding.BindingProvider;

/**
 * @author Jason Marchioni
 * @since 1.8.0-SNAPSHOT
 */
public interface ISYBindingProvider extends BindingProvider {

	public ISYBindingConfig getItemConfig(String itemName);
	
	public ISYBindingConfig getItemConfigByAddress(String addr);

	Collection<BindingConfig> getConfiguredItems();
	
}
