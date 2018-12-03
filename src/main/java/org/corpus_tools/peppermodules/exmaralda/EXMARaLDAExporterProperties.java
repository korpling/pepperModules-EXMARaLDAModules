/**
 * Copyright 2009 Humboldt-Universität zu Berlin, INRIA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package org.corpus_tools.peppermodules.exmaralda;

import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.pepper.modules.PepperModuleProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the properties to be used for the {@link EXMARaLDAExporter}.
 * 
 * @author Thomas Krause
 *
 */
@SuppressWarnings("serial")
public class EXMARaLDAExporterProperties extends PepperModuleProperties {
	private static final Logger logger = LoggerFactory.getLogger(EXMARaLDAImporter.class);

	public static final String PROP_DROP_EMPTY_SPEAKER = "dropEmptySpeaker";

	public EXMARaLDAExporterProperties() {
		this.addProperty(PepperModuleProperty.create().withName(PROP_DROP_EMPTY_SPEAKER).withType(Boolean.class)
				.withDescription("Drop speakers which do not have any events").withDefaultValue(false).build());
	}


	public Boolean isDropEmptySpeaker() {
		return ((Boolean) this.getProperty(PROP_DROP_EMPTY_SPEAKER).getValue());
	}

}
