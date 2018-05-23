package com.load.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import com.github.pagehelper.PageHelper;
import com.load.enums.Status;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.*;

@Configuration
@MapperScan("com.load.mapper")
public class DruidConfig {

	private Logger logger = LoggerFactory.getLogger(DruidConfig.class);

	@Value("${spring.datasource.url}")
	private String dbUrl;

	@Value("${spring.datasource.username}")
	private String username;

	@Value("${spring.datasource.password}")
	private String password;

	@Value("${spring.datasource.driver-class-name}")
	private String driverClassName;

	@Value("${spring.datasource.initialSize}")
	private int initialSize;

	@Value("${spring.datasource.minIdle}")
	private int minIdle;

	@Value("${spring.datasource.maxActive}")
	private int maxActive;

	@Value("${spring.datasource.maxWait}")
	private int maxWait;

	@Value("${spring.datasource.timeBetweenEvictionRunsMillis}")
	private int timeBetweenEvictionRunsMillis;

	@Value("${spring.datasource.minEvictableIdleTimeMillis}")
	private int minEvictableIdleTimeMillis;

	@Value("${spring.datasource.validationQuery}")
	private String validationQuery;

	@Value("${spring.datasource.testWhileIdle}")
	private boolean testWhileIdle;

	@Value("${spring.datasource.testOnBorrow}")
	private boolean testOnBorrow;

	@Value("${spring.datasource.testOnReturn}")
	private boolean testOnReturn;

	@Value("${spring.datasource.filters}")
	private String filters;

	private static final String ENUM_PACKAGE = "com.load.enums";

	/**
	 * =================================================================
	 *功 能： 设置SQL监控用户名和密码
	--------------------------------------------------------------------
	 *修改记录 ：
	 *日 期  版本 修改人 修改内容
	 *2017年9月8日 v1.0 lanlong.li 创建
	====================================================================
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
	 * =================================================================
	 *功 能： 设置SQL监控管理平台访问地址
	--------------------------------------------------------------------
	 *修改记录 ：
	 *日 期  版本 修改人 修改内容
	 *2017年9月8日 v1.0 lanlong.li 创建
	====================================================================
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
	 * =================================================================
	 *功 能： 注入阿里Druid连接池
	--------------------------------------------------------------------
	 *修改记录 ：
	 *日 期  版本 修改人 修改内容
	 *2017年9月8日 v1.0 lanlong.li 创建
	====================================================================
	 */
	@Bean
	public DataSource druidDataSource() {
		DruidDataSource datasource = new DruidDataSource();
		datasource.setUrl(dbUrl);
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
//		handlerRegistry.register(Status.class, new EnumOrdinalTypeHandler<>(Status.class));
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
