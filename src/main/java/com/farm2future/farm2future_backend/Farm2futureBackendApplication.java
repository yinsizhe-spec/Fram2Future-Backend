package com.farm2future.farm2future_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Farm2Future 后端项目启动类。
 *
 * <p>
 * 该类是整个 Spring Boot 应用程序的入口。
 * 运行 main 方法后，Spring Boot 会启动内置 Web 服务器，
 * 加载项目中的 Controller、Service、Mapper、配置类等 Bean。
 * </p>
 *
 * <p>
 * {@link SpringBootApplication} 是一个组合注解，
 * 它通常包含以下能力：
 * </p>
 *
 * <ul>
 *     <li>开启 Spring Boot 自动配置</li>
 *     <li>开启组件扫描</li>
 *     <li>声明当前类为配置类</li>
 * </ul>
 */
@SpringBootApplication
public class Farm2futureBackendApplication {

	/**
	 * 项目启动入口方法。
	 *
	 * <p>
	 * 当运行该方法时，Spring Boot 会启动整个后端服务。
	 * 默认情况下，如果配置了 Web 依赖，会启动内置 Tomcat。
	 * </p>
	 *
	 * @param args 启动参数
	 */
	public static void main(String[] args) {
		SpringApplication.run(Farm2futureBackendApplication.class, args);
	}

}