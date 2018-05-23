package com.load.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import com.github.pagehelper.PageHelper;
import lombok.Data;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Properties;

@Configuration
@Data
@MapperScan("com.load.mapper")
@ConfigurationProperties(prefix = "spring.datasource")
public class DruidConfig {

	private Logger logger = LoggerFactory.getLogger(DruidConfig.class);

	private String url;

	private String username;

	private String password;

	private String driverClassName;

	private int initialSize;

	private int minIdle;

	private int maxActive;

	private int maxWait;

	private int timeBetweenEvictionRunsMillis;

	private int minEvictableIdleTimeMillis;

	private String validationQuery;

	private boolean testWhileIdle;

	private boolean testOnBorrow;

	private boolean testOnReturn;

	private String filters;

	private static final String ENUM_PACKAGE = "com.load.enums";

	/**
	 * 设置SQL监控用户名和密码
	 * @return
	 */
	@Bean
	public ServletRegistrationBean<StatViewServlet> druidServlet() {
		ServletRegistrationBean<StatViewServlet> reg = new ServletRegistrationBean<>();
		reg.setServlet(new StatViewServlet());
		reg.addUrlMappings("/druid/*");
		reg.addInitParameter("loginUsername", username);
		reg.addInitParameter("loginPassword", password);
		return reg;
	}

	/**
	 * 设置SQL监控管理平台访问地址
	 * @return
	 */
	@Bean
	public FilterRegistrationBean<WebStatFilter> filterRegistrationBean() {
		FilterRegistrationBean<WebStatFilter> filterRegistrationBean = new FilterRegistrationBean<>();
		filterRegistrationBean.setFilter(new WebStatFilter());
		filterRegistrationBean.addUrlPatterns("/*");
		filterRegistrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
		filterRegistrationBean.addInitParameter("profileEnable", "true");
		return filterRegistrationBean;
	}

	/**
	 * 注入阿里Druid连接池
	 * @return
	 */
	@Bean
	public DataSource druidDataSource() {
		DruidDataSource datasource = new DruidDataSource();
		datasource.setUrl(url);
		datasource.setUsername(username);
		datasource.setPassword(password);
		datasource.setDriverClassName(driverClassName);
		datasource.setInitialSize(initialSize);
		datasource.setMinIdle(minIdle);
		datasource.setMaxActive(maxActive);
		datasource.setMaxWait(maxWait);
		datasource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
		datasource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
		datasource.setValidationQuery(validationQuery);
		datasource.setTestWhileIdle(testWhileIdle);
		datasource.setTestOnBorrow(testOnBorrow);
		datasource.setTestOnReturn(testOnReturn);
		try {
			datasource.setFilters(filters);
		} catch (SQLException e) {
			logger.error("druid configuration initialization filter", e);
		}
		return datasource;
	}

	@Bean
	public SqlSessionFactory sqlSessionFactory() throws Exception {
		SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
		//dataSource
		sqlSessionFactoryBean.setDataSource(druidDataSource());
		//scan mappers
		sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver()
				.getResources("classpath*:mappers/*Mapper.xml"));
		//scan alias
		sqlSessionFactoryBean.setTypeAliasesPackage("com.load.entity");
		//plugins
		sqlSessionFactoryBean.setPlugins(new Interceptor[]{
				//pageHelper
				pageHelper()
		});
		//configuration
		org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
		//handlerRegistry
		TypeHandlerRegistry handlerRegistry = configuration.getTypeHandlerRegistry();
		//register enums
		registerEnums(handlerRegistry);
		//to camelcase
		configuration.setMapUnderscoreToCamelCase(true);
		sqlSessionFactoryBean.setConfiguration(configuration);
		return sqlSessionFactoryBean.getObject();
	}

	/**
	 * 注册所有的枚举
	 * @param handlerRegistry 类型处理注册器
	 */
	private void registerEnums(TypeHandlerRegistry handlerRegistry) throws IOException, ClassNotFoundException {
	    //文件夹路径
		String packageDirName = ENUM_PACKAGE.replace('.', '/');
		Enumeration<URL> dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
		URL url = dirs.nextElement();
		String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
		File enumFiles = new File(filePath);
		for (File file : Objects.requireNonNull(enumFiles.listFiles())) {
			String fileName = file.getAbsolutePath();
			//获取类名
			String className = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf("."));
			//获取类类型
			Class enumClass = Class.forName(ENUM_PACKAGE+"."+className);
			//注册枚举
			handlerRegistry.register(enumClass, new EnumOrdinalTypeHandler<>(enumClass));
		}
	}

    /**
     * 分页插件PageHelper
     * @return
     */
	private PageHelper pageHelper(){
		PageHelper pageHelper = new PageHelper();
		Properties properties = new Properties();
		properties.setProperty("dialect", "mysql");
		pageHelper.setProperties(properties);
		return pageHelper;
	}
}
