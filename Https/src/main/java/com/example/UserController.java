package com.example;

/**
 * Created by mazhenhua on 2016/12/16.
 */

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class UserController {

    private static final Logger logger = Logger.getLogger(UserController.class);


    @RequestMapping(value = "/get/index.htm", method = RequestMethod.GET)
    public String view(HttpServletRequest request, HttpServletResponse response){
        System.out.println("get_success");
        return "success";

    }

    @RequestMapping(value = "/post/index.htm", method = RequestMethod.POST)
    public String viewPost(HttpServletRequest request, HttpServletResponse response){
        System.out.println("post_success");
        return "success";

    }



    /**
     * 将http的 8082 端口的请求，自动转发到8443的https的接口
     * @return
     */

    @Bean
    public EmbeddedServletContainerFactory servletContainerFactory() {
        TomcatEmbeddedServletContainerFactory factory =
                new TomcatEmbeddedServletContainerFactory() {
                    @Override
                    protected void postProcessContext(Context context) {
                        //SecurityConstraint必须存在，可以通过其为不同的URL设置不同的重定向策略。
                        SecurityConstraint securityConstraint = new SecurityConstraint();
                        securityConstraint.setUserConstraint("CONFIDENTIAL");
                        SecurityCollection collection = new SecurityCollection();
                        collection.addPattern("/*");
                        securityConstraint.addCollection(collection);
                        context.addConstraint(securityConstraint);
                    }
                };
        factory.addAdditionalTomcatConnectors(createHttpConnector());
        return factory;
    }

    private Connector createHttpConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setSecure(false);
        connector.setPort(8082);
        connector.setRedirectPort(8443);
        return connector;
    }
}