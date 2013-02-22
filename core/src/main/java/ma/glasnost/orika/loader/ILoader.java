package ma.glasnost.orika.loader;

import ma.glasnost.orika.MapperFactory;

import javax.xml.stream.events.XMLEvent;

/**
 * @author  ikozar
 * date     13.02.13
 */
public interface ILoader {
    ILoader startElement(MapperFactory factory, ILoader parent, XMLEvent event);
    ILoader character(MapperFactory factory, ILoader parent, XMLEvent event);
    ILoader endElement(MapperFactory factory, ILoader parent, XMLEvent event);
}
