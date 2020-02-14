package com.test.restapi.dozer.repository.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.data.repository.config.DefaultRepositoryBaseClass;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;

import com.github.dozermapper.core.Mapper;
import com.test.restapi.dozer.repository.support.DozerRepositoryFactoryBean;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(DozerRepositoriesRegistrar.class)
public @interface EnableDozerRepositories {
	/**
	 * Alias for the {@link #basePackages()} attribute. Allows for more concise annotation declarations e.g.:
	 * {@code @EnableDozerRepositories("org.my.pkg")} instead of {@code @EnableDozerRepositories(basePackages="org.my.pkg")}.
	 */
	String[] value() default {};

	/**
	 * Base packages to scan for annotated components. {@link #value()} is an alias for (and mutually exclusive with) this
	 * attribute. Use {@link #basePackageClasses()} for a type-safe alternative to String-based package names.
	 */
	String[] basePackages() default {};

	/**
	 * Type-safe alternative to {@link #basePackages()} for specifying the packages to scan for annotated components. The
	 * package of each class specified will be scanned. Consider creating a special no-op marker class or interface in
	 * each package that serves no purpose other than being referenced by this attribute.
	 */
	Class<?>[] basePackageClasses() default {};

	/**
	 * Specifies which types are eligible for component scanning. Further narrows the set of candidate components from
	 * everything in {@link #basePackages()} to everything in the base packages that matches the given filter or filters.
	 */
	Filter[] includeFilters() default {};

	/**
	 * Specifies which types are not eligible for component scanning.
	 */
	Filter[] excludeFilters() default {};

	/**
	 * Returns the postfix to be used when looking up custom repository implementations. Defaults to {@literal Impl}. So
	 * for a repository named {@code PersonRepository} the corresponding implementation class will be looked up scanning
	 * for {@code PersonRepositoryImpl}.
	 *
	 * @return
	 */
	String repositoryImplementationPostfix() default "Impl";

	/**
	 * Configures the location of where to find the Spring Data named queries properties file. Will default to
	 * {@code META-INF/dozer-named-queries.properties}.
	 *
	 * @return
	 */
	String namedQueriesLocation() default "";

	/**
	 * Returns the key of the {@link QueryLookupStrategy} to be used for lookup queries for query methods. Defaults to
	 * {@link Key#CREATE_IF_NOT_FOUND}.
	 *
	 * @return
	 */
	Key queryLookupStrategy() default Key.CREATE_IF_NOT_FOUND;

	/**
	 * Returns the {@link FactoryBean} class to be used for each repository instance. Defaults to
	 * {@link DozerRepositoryFactoryBean}.
	 *
	 * @return
	 */
	Class<?> repositoryFactoryBeanClass() default DozerRepositoryFactoryBean.class;

	/**
	 * Configure the repository base class to be used to create repository proxies for this particular configuration.
	 *
	 * @return
	 * @since 1.9
	 */
	Class<?> repositoryBaseClass() default DefaultRepositoryBaseClass.class;

	/**
	 * Configures the name of the {@link Mapper} bean definition to be used to create repositories
	 * discovered through this annotation. Defaults to {@code dozerMapper}.
	 *
	 * @return
	 */
	String dozerMapperRef() default "dozerMapper";

	/**
	 * Configures when the repositories are initialized in the bootstrap lifecycle. {@link BootstrapMode#DEFAULT}
	 * (default) means eager initialization except all repository interfaces annotated with {@link Lazy},
	 * {@link BootstrapMode#LAZY} means lazy by default including injection of lazy-initialization proxies into client
	 * beans so that those can be instantiated but will only trigger the initialization upon first repository usage (i.e a
	 * method invocation on it). This means repositories can still be uninitialized when the application context has
	 * completed its bootstrap. {@link BootstrapMode#DEFERRED} is fundamentally the same as {@link BootstrapMode#LAZY},
	 * but triggers repository initialization when the application context finishes its bootstrap.
	 * 
	 * @return
	 * @since 2.1
	 */
	BootstrapMode bootstrapMode() default BootstrapMode.DEFAULT;

	/**
	 * Configures what character is used to escape the wildcards {@literal _} and {@literal %} in derived queries with
	 * {@literal contains}, {@literal startsWith} or {@literal endsWith} clauses.
	 * 
	 * @return a single character used for escaping.
	 */
	char escapeCharacter() default '\\';
}
