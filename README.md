<img src="https://i.imgur.com/WtTwQtt.png">


# ZAProxy Jenkins _Pipeline_ plugin


<img align="left" src='/src/main/webapp/logo.png'>
This is a Java Jenkins pipeline plugin for OWASP Zed Attack Proxy. This plugin allows you to proxy your tests through ZAP and then generate a report containing new alerts since the previous build. It also allows you to fail the build if there is a new alert with a risk level of your choice. It is made for [Pipeline](https://jenkins.io/doc/book/pipeline/) builds & adds additional functions to your Jenkinsfiles.




# API
```groovy
startZap(host: 127.0.0.1, port: 9095, timeout: 900, failBuild:3, zapHome: "/opt/zaproxy", allowedHosts:['10.0.0.1'])
```

Starts the ZAP process and configures the plugin. 
Host: The host to run the ZAP proxy server on. Passed to ZAP in the -host parameter.
Port: The port to run the proxy on
Timeout: If a scan takes too long it will stop
failBuild: If a new alert with a risk higher than this, the build will fail (0=Info, 1=Low, 2=Medium, 3=High, 4=None)
allowedHosts: Once the active ZAP scan starts, it won't scan any hosts unless they are here. If you don't set this it will only scan if the host is localhost



```groovy
runZapCrawler(host: "https://localhost")
```

Runs the ZAP crawler on a specific URL
Host: The URL to run on



```groovy
runZapAttack()
```
Once you have proxied your tests through ZAP or ran the crawler, this function runs an active scan on all the hosts that have been provided in the allowedHosts parameter in startZap.



```groovy
archiveZap()
```

Reads the alerts found by ZAP, checks if there are any new alerts that are higher than the failBuild parameter (and fails the build if so), and generates a report with differences.




# Proxying your tests
```groovy
sh "mvn verify -Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=9095 -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=9095"
```

You may need to exclude some hosts from the proxy. If so use the -Dhttp.nonProxyHosts parameter, eg -Dhttp.nonProxyHosts=*.com\\|*.co.uk

# Proxying localhost(s)
By default Java will not proxy localhost, 127.0.0.1, or any common loopback addresses. There is no way to disable this unless you set -Dhttp.nonProxyHosts= (empty). This means it's impossible to proxy just localhost without editing project code. You can mitigate this by changing your applications host to localhost.localdomain, which isn't checked by Java 
