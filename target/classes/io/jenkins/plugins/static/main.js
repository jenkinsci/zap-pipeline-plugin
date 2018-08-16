var rawNewJson = {"@version":"2.7.0","@generated":"Wed, 11 Jul 2018 16:21:31","site":[{"@name":"http://repo.jenkins-ci.org","@host":"repo.jenkins-ci.org","@port":"80","@ssl":"false","alerts":[{"pluginid":"10021","alert":"X-Content-Type-Options Header Missing","name":"X-Content-Type-Options Header Missing","riskcode":"1","confidence":"2","riskdesc":"Low (Medium)","desc":"<p>The Anti-MIME-Sniffing header X-Content-Type-Options was not set to 'nosniff'. This allows older versions of Internet Explorer and Chrome to perform MIME-sniffing on the response body, potentially causing the response body to be interpreted and displayed as a content type other than the declared content type. Current (early 2014) and legacy versions of Firefox will use the declared content type (if one is set), rather than performing MIME-sniffing.<\/p>","instances":[{"uri":"http://repo.jenkins-ci.org/public/org/eclipse/sisu/org.eclipse.sisu.plexus/0.1.0/org.eclipse.sisu.plexus-0.1.0.jar","method":"GET","param":"X-Content-Type-Options"},{"uri":"http://repo.jenkins-ci.org/public/org/eclipse/jetty/jetty-parent/22/jetty-parent-22.pom.sha1","method":"GET","param":"X-Content-Type-Options"},{"uri":"http://repo.jenkins-ci.org/public/org/eclipse/sisu/sisu-plexus/0.1.0/sisu-plexus-0.1.0.pom","method":"GET","param":"X-Content-Type-Options"},{"uri":"http://repo.jenkins-ci.org/public/org/eclipse/jetty/websocket/javax-websocket-client-impl/9.2.15.v20160210/javax-websocket-client-impl-9.2.15.v20160210.pom","method":"GET","param":"X-Content-Type-Options"},{"uri":"http://repo.jenkins-ci.org/public/org/codehaus/plexus/plexus-classworlds/2.4.2/plexus-classworlds-2.4.2.pom","method":"GET","param":"X-Content-Type-Options"},{"uri":"http://repo.jenkins-ci.org/public/org/eclipse/jetty/jetty-io/9.2.15.v20160210/jetty-io-9.2.15.v20160210.pom.sha1","method":"GET","param":"X-Content-Type-Options"},{"uri":"http://repo.jenkins-ci.org/public/org/eclipse/jetty/jetty-annotations/9.2.15.v20160210/jetty-annotations-9.2.15.v20160210.jar","method":"GET","param":"X-Content-Type-Options"},{"uri":"http://repo.jenkins-ci.org/public/org/eclipse/jetty/websocket/websocket-server/9.2.15.v20160210/websocket-server-9.2.15.v20160210.jar","method":"GET","param":"X-Content-Type-Options"},{"uri":"http://repo.jenkins-ci.org/public/org/eclipse/jetty/orbit/jetty-orbit/1/jetty-orbit-1.pom","method":"GET","param":"X-Content-Type-Options"},{"uri":"http://repo.jenkins-ci.org/public/org/jenkins-ci/plugins/scm-api/1.0/scm-api-1.0.pom","method":"GET","param":"X-Content-Type-Options"},{"uri":"http://repo.jenkins-ci.org/public/org/eclipse/jetty/jetty-xml/9.2.15.v20160210/jetty-xml-9.2.15.v20160210.jar.sha1","method":"GET","param":"X-Content-Type-Options"},{"uri":"http://repo.jenkins-ci.org/public/org/apache/maven/maven-model/3.1.0/maven-model-3.1.0.pom","method":"GET","param":"X-Content-Type-Options"},{"uri":"http://repo.jenkins-ci.org/public/org/codehaus/groovy/groovy-all/1.8.9/groovy-all-1.8.9.jar.sha1","method":"GET","param":"X-Content-Type-Options"},{"uri":"http://repo.jenkins-ci.org/public/org/jenkins-ci/main/jenkins-test-harness/2.22/jenkins-test-harness-2.22.jar","method":"GET","param":"X-Content-Type-Options"},{"uri":"http://repo.jenkins-ci.org/public/org/ow2/asm/asm-commons/5.0.1/asm-commons-5.0.1.pom.sha1","method":"GET","param":"X-Content-Type-Options"},{"uri":"http://repo.jenkins-ci.org/public/org/apache/maven/archetype/maven-archetype/1.0-alpha-4/maven-archetype-1.0-alpha-4.pom.sha1","method":"GET","param":"X-Content-Type-Options"},{"uri":"http://repo.jenkins-ci.org/public/org/eclipse/jetty/websocket/websocket-client/9.2.15.v20160210/websocket-client-9.2.15.v20160210.pom.sha1","method":"GET","param":"X-Content-Type-Options"},{"uri":"http://repo.jenkins-ci.org/public/org/eclipse/sisu/org.eclipse.sisu.inject/0.0.0.M2a/org.eclipse.sisu.inject-0.0.0.M2a.pom.sha1","method":"GET","param":"X-Content-Type-Options"},{"uri":"http://repo.jenkins-ci.org/public/org/jenkins-ci/modules/windows-slave-installer/1.4/windows-slave-installer-1.4.jar.sha1","method":"GET","param":"X-Content-Type-Options"},{"uri":"http://repo.jenkins-ci.org/public/org/eclipse/jetty/websocket/websocket-server/9.2.15.v20160210/websocket-server-9.2.15.v20160210.jar.sha1","method":"GET","param":"X-Content-Type-Options"}],"count":"525","solution":"<p>Ensure that the application/web server sets the Content-Type header appropriately, and that it sets the X-Content-Type-Options header to 'nosniff' for all web pages.<\/p><p>If possible, ensure that the end user uses a standards-compliant and modern web browser that does not perform MIME-sniffing at all, or that can be directed by the web application/web server to not perform MIME-sniffing.<\/p>","otherinfo":"<p>This issue still applies to error type pages (401, 403, 500, etc) as those pages are often still affected by injection issues, in which case there is still concern for browsers sniffing pages away from their actual content type.<\/p><p>At \"High\" threshold this scanner will not alert on client or server error responses.<\/p>","reference":"<p>http://msdn.microsoft.com/en-us/library/ie/gg622941%28v=vs.85%29.aspx<\/p><p>https://www.owasp.org/index.php/List_of_useful_HTTP_headers<\/p>","cweid":"16","wascid":"15","sourceid":"3"},{"pluginid":"2","alert":"Private IP Disclosure","name":"Private IP Disclosure","riskcode":"1","confidence":"2","riskdesc":"Low (Medium)","desc":"<p>A private IP (such as 10.x.x.x, 172.x.x.x, 192.168.x.x) or an Amazon EC2 private hostname (for example, ip-10-0-56-78) has been found in the HTTP response body. This information might be helpful for further attacks targeting internal systems.<\/p>","instances":[{"uri":"http://repo.jenkins-ci.org/public/org/eclipse/jetty/jetty-plus/9.2.15.v20160210/jetty-plus-9.2.15.v20160210.pom","method":"GET","evidence":"10.1.2.1"}],"count":"1","solution":"<p>Remove the private IP address from the HTTP response body.  For comments, use JSP/ASP/PHP comment instead of HTML/JavaScript comment which can be seen by client browsers.<\/p>","otherinfo":"<p>10.1.2.1<\/p><p><\/p>","reference":"<p>https://tools.ietf.org/html/rfc1918<\/p>","cweid":"200","wascid":"13","sourceid":"3"}]},{"@name":"https://ops05.barracuda.com","@host":"ops05.barracuda.com","@port":"443","@ssl":"true","alerts":[{"pluginid":"10021","alert":"X-Content-Type-Options Header Missing","name":"X-Content-Type-Options Header Missing","riskcode":"1","confidence":"2","riskdesc":"Low (Medium)","desc":"<p>The Anti-MIME-Sniffing header X-Content-Type-Options was not set to 'nosniff'. This allows older versions of Internet Explorer and Chrome to perform MIME-sniffing on the response body, potentially causing the response body to be interpreted and displayed as a content type other than the declared content type. Current (early 2014) and legacy versions of Firefox will use the declared content type (if one is set), rather than performing MIME-sniffing.<\/p>","instances":[{"uri":"https://ops05.barracuda.com/cgi-bin/rbm_api.cgi","method":"POST","param":"X-Content-Type-Options"}],"count":"1","solution":"<p>Ensure that the application/web server sets the Content-Type header appropriately, and that it sets the X-Content-Type-Options header to 'nosniff' for all web pages.<\/p><p>If possible, ensure that the end user uses a standards-compliant and modern web browser that does not perform MIME-sniffing at all, or that can be directed by the web application/web server to not perform MIME-sniffing.<\/p>","otherinfo":"<p>This issue still applies to error type pages (401, 403, 500, etc) as those pages are often still affected by injection issues, in which case there is still concern for browsers sniffing pages away from their actual content type.<\/p><p>At \"High\" threshold this scanner will not alert on client or server error responses.<\/p>","reference":"<p>http://msdn.microsoft.com/en-us/library/ie/gg622941%28v=vs.85%29.aspx<\/p><p>https://www.owasp.org/index.php/List_of_useful_HTTP_headers<\/p>","cweid":"16","wascid":"15","sourceid":"3"},{"pluginid":"10015","alert":"Incomplete or No Cache-control and Pragma HTTP Header Set","name":"Incomplete or No Cache-control and Pragma HTTP Header Set","riskcode":"1","confidence":"2","riskdesc":"Low (Medium)","desc":"<p>The cache-control and pragma HTTP header have not been set properly or are missing allowing the browser and proxies to cache content.<\/p>","instances":[{"uri":"https://ops05.barracuda.com/cgi-bin/rbm_api.cgi","method":"POST","param":"Cache-Control"}],"count":"1","solution":"<p>Whenever possible ensure the cache-control HTTP header is set with no-cache, no-store, must-revalidate; and that the pragma HTTP header is set with no-cache.<\/p>","reference":"<p>https://www.owasp.org/index.php/Session_Management_Cheat_Sheet#Web_Content_Caching<\/p>","cweid":"525","wascid":"13","sourceid":"3"}]},{"@name":"https://10.14.0.88","@host":"10.14.0.88","@port":"443","@ssl":"true","alerts":[{"pluginid":"90022","alert":"Application Error Disclosure","name":"Application Error Disclosure","riskcode":"2","confidence":"2","riskdesc":"Medium (Medium)","desc":"<p>This page contains an error/warning message that may disclose sensitive information like the location of the file that produced the unhandled exception. This information can be used to launch further attacks against the web application. The alert could be a false positive if the error message is found inside a documentation page.<\/p>","instances":[{"uri":"https://10.14.0.88:443/management/flyway","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/docs","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/beans","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/loggers","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/health","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/v2/api-docs","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/heapdump","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/trace","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/actuator","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/metrics","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/jolokia","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/auditevents","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/configprops","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/health/availability","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/shutdown","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/health/internal","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/autoconfig","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/env","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/health/external","method":"GET","evidence":"HTTP/1.1 500"}],"count":"29","solution":"<p>Review the source code of this page. Implement custom error pages. Consider implementing a mechanism to provide a unique error reference/identifier to the client (browser) while logging the details on the server side and not exposing them to the user.<\/p>","reference":"<p><\/p>","cweid":"200","wascid":"13","sourceid":"3"},{"pluginid":"10021","alert":"X-Content-Type-Options Header Missing","name":"X-Content-Type-Options Header Missing","riskcode":"1","confidence":"2","riskdesc":"Low (Medium)","desc":"<p>The Anti-MIME-Sniffing header X-Content-Type-Options was not set to 'nosniff'. This allows older versions of Internet Explorer and Chrome to perform MIME-sniffing on the response body, potentially causing the response body to be interpreted and displayed as a content type other than the declared content type. Current (early 2014) and legacy versions of Firefox will use the declared content type (if one is set), rather than performing MIME-sniffing.<\/p>","instances":[{"uri":"https://10.14.0.88:443/api/v1/webui/cloud/authenticate","method":"GET","param":"X-Content-Type-Options"}],"count":"1","solution":"<p>Ensure that the application/web server sets the Content-Type header appropriately, and that it sets the X-Content-Type-Options header to 'nosniff' for all web pages.<\/p><p>If possible, ensure that the end user uses a standards-compliant and modern web browser that does not perform MIME-sniffing at all, or that can be directed by the web application/web server to not perform MIME-sniffing.<\/p>","otherinfo":"<p>This issue still applies to error type pages (401, 403, 500, etc) as those pages are often still affected by injection issues, in which case there is still concern for browsers sniffing pages away from their actual content type.<\/p><p>At \"High\" threshold this scanner will not alert on client or server error responses.<\/p>","reference":"<p>http://msdn.microsoft.com/en-us/library/ie/gg622941%28v=vs.85%29.aspx<\/p><p>https://www.owasp.org/index.php/List_of_useful_HTTP_headers<\/p>","cweid":"16","wascid":"15","sourceid":"3"},{"pluginid":"10015","alert":"Incomplete or No Cache-control and Pragma HTTP Header Set","name":"Incomplete or No Cache-control and Pragma HTTP Header Set","riskcode":"1","confidence":"2","riskdesc":"Low (Medium)","desc":"<p>The cache-control and pragma HTTP header have not been set properly or are missing allowing the browser and proxies to cache content.<\/p>","instances":[{"uri":"https://10.14.0.88:443/api/v1/webui/cloud/authenticate","method":"GET","param":"Cache-Control"}],"count":"1","solution":"<p>Whenever possible ensure the cache-control HTTP header is set with no-cache, no-store, must-revalidate; and that the pragma HTTP header is set with no-cache.<\/p>","reference":"<p>https://www.owasp.org/index.php/Session_Management_Cheat_Sheet#Web_Content_Caching<\/p>","cweid":"525","wascid":"13","sourceid":"3"}]}]}
//var rawOldJson = {"@version":"2.7.0","@generated":"Fri, 6 Jul 2018 15:45:31","site":[{"@name":"https://ops05.barracuda.com","@host":"ops05.barracuda.com","@port":"443","@ssl":"true","alerts":[{"pluginid":"10021","alert":"X-Content-Type-Options Header Missing","name":"X-Content-Type-Options Header Missing","riskcode":"1","confidence":"2","riskdesc":"Low (Medium)","desc":"<p>The Anti-MIME-Sniffing header X-Content-Type-Options was not set to 'nosniff'. This allows older versions of Internet Explorer and Chrome to perform MIME-sniffing on the response body, potentially causing the response body to be interpreted and displayed as a content type other than the declared content type. Current (early 2014) and legacy versions of Firefox will use the declared content type (if one is set), rather than performing MIME-sniffing.<\/p>","instances":[{"uri":"https://ops05.barracuda.com/cgi-bin/rbm_api.cgi","method":"POST","param":"X-Content-Type-Options"}],"count":"1","solution":"<p>Ensure that the application/web server sets the Content-Type header appropriately, and that it sets the X-Content-Type-Options header to 'nosniff' for all web pages.<\/p><p>If possible, ensure that the end user uses a standards-compliant and modern web browser that does not perform MIME-sniffing at all, or that can be directed by the web application/web server to not perform MIME-sniffing.<\/p>","otherinfo":"<p>This issue still applies to error type pages (401, 403, 500, etc) as those pages are often still affected by injection issues, in which case there is still concern for browsers sniffing pages away from their actual content type.<\/p><p>At \"High\" threshold this scanner will not alert on client or server error responses.<\/p>","reference":"<p>http://msdn.microsoft.com/en-us/library/ie/gg622941%28v=vs.85%29.aspx<\/p><p>https://www.owasp.org/index.php/List_of_useful_HTTP_headers<\/p>","cweid":"16","wascid":"15","sourceid":"3"},{"pluginid":"10015","alert":"Incomplete or No Cache-control and Pragma HTTP Header Set","name":"Incomplete or No Cache-control and Pragma HTTP Header Set","riskcode":"1","confidence":"2","riskdesc":"Low (Medium)","desc":"<p>The cache-control and pragma HTTP header have not been set properly or are missing allowing the browser and proxies to cache content.<\/p>","instances":[{"uri":"https://ops05.barracuda.com/cgi-bin/rbm_api.cgi","method":"POST","param":"Cache-Control"}],"count":"1","solution":"<p>Whenever possible ensure the cache-control HTTP header is set with no-cache, no-store, must-revalidate; and that the pragma HTTP header is set with no-cache.<\/p>","reference":"<p>https://www.owasp.org/index.php/Session_Management_Cheat_Sheet#Web_Content_Caching<\/p>","cweid":"525","wascid":"13","sourceid":"3"}]},{"@name":"https://10.14.0.88","@host":"10.14.0.88","@port":"443","@ssl":"true","alerts":[{"pluginid":"90022","alert":"Application Error Disclosure","name":"Application Error Disclosure","riskcode":"2","confidence":"2","riskdesc":"Medium (Medium)","desc":"<p>This page contains an error/warning message that may disclose sensitive information like the location of the file that produced the unhandled exception. This information can be used to launch further attacks against the web application. The alert could be a false positive if the error message is found inside a documentation page.<\/p>","instances":[{"uri":"https://10.14.0.88:443/management/flyway","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/docs","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/beans","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/loggers","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/health","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/v2/api-docs","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/heapdump","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/trace","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/actuator","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/metrics","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/jolokia","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/auditevents","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/configprops","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/health/availability","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/shutdown","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/health/internal","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/autoconfig","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/env","method":"GET","evidence":"HTTP/1.1 500"},{"uri":"https://10.14.0.88:443/management/health/external","method":"GET","evidence":"HTTP/1.1 500"}],"count":"29","solution":"<p>Review the source code of this page. Implement custom error pages. Consider implementing a mechanism to provide a unique error reference/identifier to the client (browser) while logging the details on the server side and not exposing them to the user.<\/p>","reference":"<p><\/p>","cweid":"200","wascid":"13","sourceid":"3"},{"pluginid":"10021","alert":"X-Content-Type-Options Header Missing","name":"X-Content-Type-Options Header Missing","riskcode":"1","confidence":"2","riskdesc":"Low (Medium)","desc":"<p>The Anti-MIME-Sniffing header X-Content-Type-Options was not set to 'nosniff'. This allows older versions of Internet Explorer and Chrome to perform MIME-sniffing on the response body, potentially causing the response body to be interpreted and displayed as a content type other than the declared content type. Current (early 2014) and legacy versions of Firefox will use the declared content type (if one is set), rather than performing MIME-sniffing.<\/p>","instances":[{"uri":"https://10.14.0.88:443/api/v1/webui/cloud/authenticate","method":"GET","param":"X-Content-Type-Options"}],"count":"1","solution":"<p>Ensure that the application/web server sets the Content-Type header appropriately, and that it sets the X-Content-Type-Options header to 'nosniff' for all web pages.<\/p><p>If possible, ensure that the end user uses a standards-compliant and modern web browser that does not perform MIME-sniffing at all, or that can be directed by the web application/web server to not perform MIME-sniffing.<\/p>","otherinfo":"<p>This issue still applies to error type pages (401, 403, 500, etc) as those pages are often still affected by injection issues, in which case there is still concern for browsers sniffing pages away from their actual content type.<\/p><p>At \"High\" threshold this scanner will not alert on client or server error responses.<\/p>","reference":"<p>http://msdn.microsoft.com/en-us/library/ie/gg622941%28v=vs.85%29.aspx<\/p><p>https://www.owasp.org/index.php/List_of_useful_HTTP_headers<\/p>","cweid":"16","wascid":"15","sourceid":"3"},{"pluginid":"10015","alert":"Incomplete or No Cache-control and Pragma HTTP Header Set","name":"Incomplete or No Cache-control and Pragma HTTP Header Set","riskcode":"1","confidence":"2","riskdesc":"Low (Medium)","desc":"<p>The cache-control and pragma HTTP header have not been set properly or are missing allowing the browser and proxies to cache content.<\/p>","instances":[{"uri":"https://10.14.0.88:443/api/v1/webui/cloud/authenticate","method":"GET","param":"Cache-Control"}],"count":"1","solution":"<p>Whenever possible ensure the cache-control HTTP header is set with no-cache, no-store, must-revalidate; and that the pragma HTTP header is set with no-cache.<\/p>","reference":"<p>https://www.owasp.org/index.php/Session_Management_Cheat_Sheet#Web_Content_Caching<\/p>","cweid":"525","wascid":"13","sourceid":"3"}]},{"@name":"http://localhost:3000","@host":"localhost","@port":"3000","@ssl":"false","alerts":[]}]}
var rawOldJson = rawNewJson


var alerts = [{"alert":"SQL Injection","confidence":"2","riskCode":"3","description":"<p>SQL injection may be possible.</p>","solution":"<p>Do not trust client side input, even if there is client side validation in place.  </p><p>In general, type check all data on the server side.</p><p>If the application uses JDBC, use PreparedStatement or CallableStatement, with parameters passed by '?'</p><p>If the application uses ASP, use ADO Command Objects with strong type checking and parameterized queries.</p><p>If database Stored Procedures can be used, use them.</p><p>Do *not* concatenate strings into queries in the stored procedure, or use 'exec', 'exec immediate', or equivalent functionality!</p><p>Do not create dynamic SQL queries using simple string concatenation.</p><p>Escape all data received from the client.</p><p>Apply a 'whitelist' of allowed characters, or a 'blacklist' of disallowed characters in user input.</p><p>Apply the principle of least privilege by using the least privileged database user possible.</p><p>In particular, avoid using the 'sa' or 'db-owner' database users. This does not eliminate SQL injection, but minimizes its impact.</p><p>Grant the minimum database access that is necessary for the application.</p>","wascid":"19","instances":[{"uri":"https://10.14.0.88/api/v1/webui/5215263/appliances?query=query%27+AND+%271%27%3D%271%27+--+","method":"GET","param":"query"},{"uri":"https://10.14.0.88:443/api/v1/webui/5215263/configurations?query=query%27+AND+%271%27%3D%271%27+--+","method":"POST","param":"query"},{"uri":"https://10.14.0.88:443/api/v1/webui/5215263/configurations","method":"POST","param":"uuid"},{"uri":"https://10.14.0.88:443/api/v1/webui/5215263/appliances/claim","method":"POST","param":"linkingCode"},{"uri":"https://10.14.0.88:443/api/v1/webui/5215263/configurations","method":"POST","param":"createdUsername"},{"uri":"https://10.14.0.88:443/api/v1/webui/5215263/appliances/claim","method":"POST","param":"linkingCode"},{"uri":"https://10.14.0.88:443/api/v1/webui/5215263/configurations?query=query%27+AND+%271%27%3D%271%27+--+","method":"POST","param":"query"},{"uri":"https://10.14.0.88:443/api/v1/webui/5215263/appliances/claim","method":"POST","param":"linkingCode"},{"uri":"https://10.14.0.88:443/api/v1/webui/5215263/appliances/claim","method":"POST","param":"linkingCode"},{"uri":"https://10.14.0.88:443/api/v1/webui/5215263/appliances/claim","method":"POST","param":"linkingCode"},{"uri":"https://10.14.0.88:443/api/v1/webui/5215263/configurations","method":"POST","param":"checkSum"},{"uri":"https://10.14.0.88:443/api/v1/webui/5215263/configurations","method":"POST","param":"createdUsername"},{"uri":"https://10.14.0.88:443/api/v1/webui/5215263/appliances/claim?query=query%27+AND+%271%27%3D%271%27+--+","method":"POST","param":"query"},{"uri":"https://10.14.0.88:443/api/v1/webui/5215263/appliances/claim","method":"POST","param":"linkingCode"},{"uri":"https://10.14.0.88:443/api/v1/webui/5215263/dashboard/audit/latest?query=query+AND+1%3D1+--+","method":"GET","param":"query"},{"uri":"https://10.14.0.88:443/api/v1/webui/5215263/configurations","method":"POST","param":"createdUsername"},{"uri":"https://10.14.0.88:443/api/v1/webui/5215263/configurations","method":"POST","param":"createdUsername"},{"uri":"https://10.14.0.88/api/v1/webui/5215263/configurations?query=query+AND+1%3D1+--+","method":"GET","param":"query"},{"uri":"https://10.14.0.88:443/api/v1/webui/5215263/configurations","method":"POST","param":"checkSum"},{"uri":"https://10.14.0.88:443/api/v1/webui/5215263/configurations","method":"POST","param":"updatedUsername"}]},{"alert":"Application Error Disclosure","confidence":"2","riskCode":"2","description":"<p>This page contains an error/warning message that may disclose sensitive information like the location of the file that produced the unhandled exception. This information can be used to launch further attacks against the web application. The alert could be a false positive if the error message is found inside a documentation page.</p>","solution":"<p>Review the source code of this page. Implement custom error pages. Consider implementing a mechanism to provide a unique error reference/identifier to the client (browser) while logging the details on the server side and not exposing them to the user.</p>","wascid":"13","instances":[{"uri":"https://10.14.0.88:443/management","method":"GET","evidence":"HTTP/1.1 500"}]},{"alert":"Format String Error","confidence":"2","riskCode":"2","description":"<p>A Format String error occurs when the submitted data of an input string is evaluated as a command by the application. </p>","solution":"<p>Rewrite the background program using proper deletion of bad character strings.  This will require a recompile of the background executable.</p>","wascid":"6","instances":[{"uri":"https://10.14.0.88:443/api/v1/webui/5215263/configurations","method":"POST","param":"contents"},{"uri":"https://10.14.0.88:443/api/v1/appliance/download/reverted","method":"POST","param":"debug"}]}]

var currentBuildParsed;
var lastBuild;


var parseRawBuild = (build) => {
	var alerts = []

	// If there is only one site, it's put into build.site by zap, otherwise [build.site]
	if(!Array.isArray(build.site)) { build.site = [build.site] }

	build.site.forEach((data) => {
		if(data.alerts.length != 0){
			data.alerts.forEach((alert) => {
				
				var alert_ = {
					alert:alert.alert,
					confidence: alert.confidence,
					riskCode: alert.riskcode,
					description: alert.desc,
					solution: alert.solution,
					wascid: alert.wascid,
					instances: []            
				}

				alert.instances.forEach((instance) => {
	
					alert_.instances.push({
						uri: instance.uri,
						method: instance.method,
						param: instance.param,
						evidence: instance.evidence,
					})                    
				})

				alerts.push(alert_)
			})
		}
	})
	return alerts
}

var getNew = () => {//(rawNewJson, rawOldJson) => {
	var currentBuildParsed = parseRawBuild(rawNewJson)

	if(rawOldJson){

		var newAlerts = []
		var newAlertInstances = []

		var lastBuild = parseRawBuild(rawOldJson)
		currentBuildParsed.forEach((alert) => {
			
			var foundAlert = false
			lastBuild.forEach((oldAlert) => {
				if(oldAlert.wascid == alert.wascid && oldAlert.alert == alert.alert && !alert.done && !oldAlert.done){
					alert.done = true;
					oldAlert.done = true;

					foundAlert = true

					alert.instances.forEach((instance) => {
						var newInstance = true
						oldAlert.instances.forEach((lastInstance) => {

							if(instance.uri.split("?")[0] == lastInstance.uri.split("?")[0]){
								newInstance = false
			
							}
						})

						if(newInstance){
							if(!newAlertInstances[alert.uri]){
								newAlertInstances[alert.uri] = angular.copy(alert)
								newAlertInstances[alert.uri].instances = []
								newAlertInstances[alert.uri].hasNewInstances = true
							}

							newAlertInstances[alert.uri].instances.push(instance)
						}
					})
				}
			})

			if(!foundAlert){
				alert.isNew = true
				newAlerts.push(alert)
			}
		})

		newAlertInstances = Object.values(newAlertInstances)
		var res = newAlerts.concat(newAlertInstances)



		return newAlerts.concat(newAlertInstances);
	} else{
		$scope.alerts = parsedCurrentBuild
	}
}


var App = angular.module("zap", [])
App.controller('mainController', function($scope, $rootScope, $http){
	$scope.counts = {
		high: 0,
		medium: 0,
		low: 0
	}

	$scope.colors = ['low-alert', 'medium-alert', 'high-alert']

	$scope.parseAlerts = () => {
		Object.keys($scope.counts).forEach((k) => { $scope.counts[k] = 0})

			$scope.alerts.sort((a, b) => {
				return a.riskCode < b.riskCode
			})


		$scope.alerts.forEach((data) => {
			switch(data.riskCode){
				case "3":
					$scope.counts.high++;
					break;
				case "2":
					$scope.counts.medium++;
					break;
				case "1":
					$scope.counts.low++;
					break;
			}
		})
	}

	$scope.load = () => {
		// Load JSON here and put into rawNewJson and rawOldJson
		$scope.parseAlerts()
		$scope.alerts = getNew()
	}

	$scope.showAll = false
	$scope.loadAll = () => {
		$scope.alerts = parseRawBuild(rawNewJson)
		$scope.showAll = true

		$scope.parseAlerts();
	}	

}).filter('to_trusted', ['$sce', function($sce){
    return function(text) {
    	return $sce.trustAsHtml(text);
    };
}]);