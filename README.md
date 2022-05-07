# Moscow是什么?
   一个优秀的微服务框架.
  
  
# 安装
  下载或编译
  可选 1 - 从这里下载预构建包：https://github.com/sihaihou/moscow/releases
  
# 快速开始
  开始您的第一个项目非常容易。

### 第一步: 引入依赖
      
<pre>
&#60;dependency&#62;
   &#60;groupId&#62;com.reyco.moscow&#60;/groupId&#62;
   &#60;artifactId&#62;moscow-discovery-spring-boot-starter&#60;/artifactId&#62;
   &#60;version&#62;0.0.1-SNAPSHOT&#60;/version&#62;
&#60;/dependency&#62;
</pre>

### 第二步: 在application.yaml配置文件中配置moscow地址和微服务名称
<pre>
spring:
   cloud:
      moscow:
         discovery: 
            serverAddr: 127.0.0.1:8999
            serviceName: orderService
</pre>

### 第三步: 配置Ribbon,  RestTemplate被@LoadBalanced修饰
<pre>
@Configuration
public class RestTemplateConfig {

	@LoadBalanced
	@Bean("restTemplate")
	public RestTemplate restTemplate(ClientHttpRequestFactory clientHttpRequestFactory) {
		return new RestTemplate(clientHttpRequestFactory);
	}
	
	@Bean
    	public ClientHttpRequestFactory simpleClientHttpRequestFactory(){
        	SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        	factory.setReadTimeout(5000);//单位为ms
        	factory.setConnectTimeout(5000);//单位为ms
        	return factory;
        }
}
</pre>

### 第四步: 调用远程服务。       
<pre>
@GetMapping
public Object test() throws MoscowException {
	String result = restTemplate.getForObject("http://orderProvider/test", String.class);
	return result;
}
</pre>
# 文档
所有最新和长期的通知也可以从Github 通知问题这里找到。

# 贡献
欢迎贡献者加入 moscow 项目。请联系18307200213@163.com 以了解如何为此项目做出贡献。 



