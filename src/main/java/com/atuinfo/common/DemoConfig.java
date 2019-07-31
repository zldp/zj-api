package com.atuinfo.common;


import com.atuinfo.Interceptors.ExceptionInterceptor;
import com.atuinfo.Interceptors.SignInterceptor;
import com.atuinfo.common.model._MappingKit;
import com.atuinfo.routes.IndexRoutes;
import com.atuinfo.routes.UserRoutes;
import com.jfinal.config.*;
import com.jfinal.json.JacksonFactory;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.render.ViewType;
import com.jfinal.server.undertow.UndertowServer;
import com.jfinal.template.Engine;

/**
 * 本 atuinfo 仅表达最为粗浅的 jfinal 用法，更为有价值的实用的企业级用法
 * 详见 JFinal 俱乐部: http://jfinal.com/club
 * 
 * API 引导式配置
 */
public class DemoConfig extends JFinalConfig {
	
	static Prop p;
	
	/**
	 * 启动入口，运行此 main 方法可以启动项目，此 main 方法可以放置在任意的 Class 类定义中，不一定要放于此
	 */
	public static void main(String[] args) {
		UndertowServer.start(DemoConfig.class);
	}
	
	/**
	 * 先加载开发环境配置，然后尝试加载生产环境配置，生产环境配置不存在时不会抛异常
	 * 在生产环境部署时后动创建 atuinfo-config-pro.txt，添加的配置项可以覆盖掉
	 * atuinfo-config-dev.txt 中的配置项
	 */
	static void loadConfig() {
		if (p == null) {
			p = PropKit.use("demo-config-dev.txt").appendIfExists("atuinfo-config-pro.txt");
		}
	}
	
	/**
	 * 配置常量
	 */
	public void configConstant(Constants me) {
		loadConfig();
		
		me.setDevMode(p.getBoolean("devMode", false));
		
		// 支持 Controller、Interceptor 之中使用 @Inject 注入业务层，并且自动实现 AOP
		//支持jsp模板
		me.setViewType(ViewType.JSP);


		me.setJsonFactory(new JacksonFactory());

		me.setInjectDependency(true);
	}
	
	/**
	 * 配置路由
	 */
	public void configRoute(Routes me) {
		//配置访问路由
		me.add(new IndexRoutes());//index的路由
		me.add(new UserRoutes());//user的路由
	}


	public void configEngine(Engine me) {
		//me.addSharedFunction("/common/_layout.html");
		//me.addSharedFunction("/common/_paginate.html");
	}
	
	/**
	 * 配置插件
	 */
	public void configPlugin(Plugins me) {
		// 配置 druid 数据库连接池插件
		DruidPlugin druidPlugin = new DruidPlugin(p.get("jdbcUrl"), p.get("user"), p.get("password").trim(),PropKit.get("driverClass"));
		me.add(druidPlugin);



		// 配置ActiveRecord插件
		ActiveRecordPlugin arp = new ActiveRecordPlugin(druidPlugin);
		// 所有映射在 MappingKit 中自动化搞定
		_MappingKit.mapping(arp);

		me.add(arp);
	}
	
	public static DruidPlugin createDruidPlugin() {
		loadConfig();
		
		return new DruidPlugin(p.get("jdbcUrl"), p.get("user"), p.get("password").trim());
	}
	
	/**
	 * 配置全局拦截器
	 */
	public void configInterceptor(Interceptors me) {
		me.add(new ExceptionInterceptor());
		//me.add(new SignInterceptor());
	}
	
	/**
	 * 配置处理器
	 */
	public void configHandler(Handlers me) {
		
	}
}
