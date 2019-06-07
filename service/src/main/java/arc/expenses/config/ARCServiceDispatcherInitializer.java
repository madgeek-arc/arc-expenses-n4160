package arc.expenses.config;

import arc.expenses.config.cache.CachingConfiguration;
import arc.expenses.config.security.CORSFilter;
import eu.openminted.registry.core.configuration.HibernateConfiguration;
import eu.openminted.registry.core.configuration.JmsConfiguration;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.Filter;

public class ARCServiceDispatcherInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {

        return new Class[]{
                ARCServiceConfiguration.class,
                HibernateConfiguration.class,
                JmsConfiguration.class,
                CachingConfiguration.class
        };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    @Override
    protected Filter[] getServletFilters() {
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");

        return new Filter[]{characterEncodingFilter, new CORSFilter()};
    }

}