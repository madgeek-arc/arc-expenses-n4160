package arc.expenses.n4160.config;

import arc.expenses.n4160.config.cache.CachingConfiguration;
import arc.expenses.n4160.config.security.CORSFilter;
import arc.expenses.n4160.config.stateMachine.BudgetStateMachineConfiguration;
import arc.expenses.n4160.config.stateMachine.RequestStateMachineConfiguration;
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
                CachingConfiguration.class,
                RequestStateMachineConfiguration.class,
                BudgetStateMachineConfiguration.class
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