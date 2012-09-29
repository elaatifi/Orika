/*
 * Orika - simpler, better and faster Java bean mapping
 * 
 * Copyright (C) 2011 Orika authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ma.glasnost.orika.metadata;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import ma.glasnost.orika.DefaultFieldMapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.property.PropertyResolverStrategy;

/**
 * ClassMapBuilderForMaps is a custom ClassMapBuilder instance which is
 * used for mapping standard JavaBeans to Map instances.
 *
 * @param <A>
 * @param <B>
 */
public class ClassMapBuilderForMaps<A, B> extends ClassMapBuilder<A,B> {
    
	
	public static class Factory extends ClassMapBuilderFactory {

		/* (non-Javadoc)
		 * @see ma.glasnost.orika.metadata.ClassMapBuilderFactory#newClassMapBuilder(ma.glasnost.orika.metadata.Type, ma.glasnost.orika.metadata.Type, ma.glasnost.orika.property.PropertyResolverStrategy, ma.glasnost.orika.DefaultFieldMapper[])
		 */
        @Override
		protected <A, B> ClassMapBuilder<A,B> newClassMapBuilder(
				Type<A> aType, Type<B> bType,
				MapperFactory mapperFactory,
				PropertyResolverStrategy propertyResolver,
				DefaultFieldMapper[] defaults) {
			
			return new ClassMapBuilderForMaps<A,B>(aType, bType, mapperFactory, propertyResolver, defaults);
		}
	}
	
	private final Set<String> nestedTypesUsed = new HashSet<String>();
	private final boolean aTypeIsMap;
	
    /**
     * @param aType
     * @param bType
     * @param propertyResolver
     * @param defaults
     */
    protected ClassMapBuilderForMaps(Type<A> aType, Type<B> bType, MapperFactory mapperFactory, PropertyResolverStrategy propertyResolver, DefaultFieldMapper... defaults) {
	    super(aType, bType, mapperFactory, propertyResolver, defaults);
	    aTypeIsMap = aType.isMap();
	}
       
    protected ClassMapBuilderForMaps<A, B> self() {
        return this;
    }           
    
    /**
     * Configures this class-map builder to employ the default property mapping
     * behavior to any properties that have not already been mapped or excluded; 
     * if any DefaultFieldMapper instances are passed, they will be used (instead of
     * those configured on the builder) to attempt a property name match if a direct 
     * match is not found.
     * 
     * @param withDefaults zero or more DefaultFieldMapper instances to apply during the default mapping;
     * if none are supplied, the configured DefaultFieldMappers for the builder (if any) should be used.
     * @return this ClassMapBuilder instance
     */
    public ClassMapBuilderForMaps<A, B> byDefault(DefaultFieldMapper... withDefaults) {
    	
    	Set<String> remainingProperties;
    	if (aTypeIsMap) {
    	    remainingProperties = new LinkedHashSet<String>(getPropertiesForTypeB());
    	    remainingProperties.removeAll(getMappedPropertiesForTypeB());
    	} else {
    	    remainingProperties = new LinkedHashSet<String>(getPropertiesForTypeA());
    	    remainingProperties.removeAll(getMappedPropertiesForTypeA());
    	}
    	remainingProperties.remove("class");
    	
        for (final String propertyName : remainingProperties) {  
            /*
             * Try to avoid mapping properties for which we've already
             * mapped a nested property
             */
            if (!nestedTypesUsed.contains(propertyName)) {                
                fieldMap(propertyName, propertyName, true).add();
            }
        }
        
        return self();
    }

    private String getParentExpression(String epxression) {
        String[] parts = epxression.split("[.]");
        StringBuilder name = new StringBuilder();
        for (int i=0; i < parts.length - 1; ++i) {
            name.append(parts[i] + ".");
        }
        return name.substring(0, name.length()-1);
    }
    
    public FieldMapBuilder<A, B> fieldMap(String fieldNameA, String fieldNameB, boolean byDefault) {
        if (aTypeIsMap && isNestedPropertyExpression(fieldNameB)) {
            nestedTypesUsed.add(getParentExpression(fieldNameB));
        } else if (!aTypeIsMap && isNestedPropertyExpression(fieldNameA)) {
            nestedTypesUsed.add(getParentExpression(fieldNameA));
        }
        return super.fieldMap(fieldNameA, fieldNameB, byDefault);
    }
    
    /**
     * Resolves a property for the particular type, based on the provided property expression
     * 
     * @param type the type to resolve
     * @param expr the property expression to resolve
     * @return
     */
    protected Property resolveProperty(java.lang.reflect.Type type, String expr) {
        
        if (TypeFactory.valueOf(type).isMap()) {
            return new MapKeyProperty(expr);
        } else {
            return super.resolveProperty(type, expr);
        }
    }
    
    /**
     * MapKeyProperty is a special Property instance used to represent a value
     * which associated with a key within a Map.
     * 
     * @author matt.deboer@gmail.com
     *
     */
    public static final class MapKeyProperty extends Property {
        
        public MapKeyProperty(String key) {
            setName(key);
            setExpression(key);
            setGetter("get(\"" + key + "\")");
            setSetter("put(\"" + key + "\",%s)");
            setType(TypeFactory.TYPE_OF_OBJECT);
        }
        
        public boolean isMapKey() {
            return true;
        }
    }
}