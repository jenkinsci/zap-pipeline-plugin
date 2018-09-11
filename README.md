<img src="https://i.imgur.com/WtTwQtt.png">


<a href='https://www.owasp.org/index.php/OWASP_Zed_Attack_Proxy_Project' align="top"><img align="left" src='https://github.com/vrondakis/zap-jenkins-pipeline-plugin/raw/master/src/main/webapp/logo.png'></a>

<br />

OWASP Zed Attack Proxy Jenkins Plugin for pipeline builds
==============================================

<br />

This is a Jenkins pipeline plugin that let's you control <a href="https://www.owasp.org/index.php/OWASP_Zed_Attack_Proxy_Project">OWASP Zed Attack Proxy</a> through Jenkins Pipeline. It can also generate a good-looking report with new alerts (compared to the previous build) and optionally fail the build if any new high-risk alerts are found, and more!
  
### API
```groovy
startZap(host: 127.0.0.1, port: 9095, timeout: 900, failBuild:3, zapHome: "/opt/zaproxy", allowedHosts:['10.0.0.1'], sessionPath:"/path/to/session.session")

host: The host to run the ZAP proxy server on. Passed to ZAP in the -host parameter.
port: The port to run the proxy on
timeout (optional): If a scan takes too long it will stop
failBuild (optional): If a new alert with a risk higher than this, the build will fail (0=Info, 1=Low, 2=Medium, 3=High, 4=None)
allowedHosts (optional): Once the active ZAP scan starts, it won't scan any hosts unless they are here. If you don't set this it will only scan if the host is localhost
sessionPath (optional): If you want to load a previous ZAP session that you have expored, you can do that here. Useful when you want to run a scan but don't want to run all your tests through ZAP.
```
Starts the ZAP process and configures the plugin. 

<br>

```groovy
runZapCrawler(host: "https://localhost")
```

Runs the ZAP crawler on a specific URL

<br>

```groovy
importZapScanPolicy(policyPath: "/home/you/yourattackpolicy.policy")
```

Loads a specific ZAP attack policy from the path you specify (Scan Policy Manager -> Export), to be used with runZapAttack

<br>

```groovy
importZapUrls(path: "/path/to/your/urls")
```

Imports a list of URLs to ZAP. You need the "Import files containing URLs" plugin for this to work.

<br>


```groovy
runZapAttack(userId: 5, scanPolicyName: "yourScanPolicy")

userId (optional): Run the scan with a specific user, loaded from the session
scanPolicyName (optional): The attack policy to use when running the scan. Loaded with importScanPolicy
```

Once you have proxied your tests through ZAP or ran the crawler, this function runs an active scan on all the hosts that have been provided in the allowedHosts parameter in startZap.

<br>


```groovy
archiveZap()
```

Reads the alerts found by ZAP, checks if there are any new alerts that are higher than the failBuild parameter (and fails the build if so), and generates a report with differences.



### Proxying your tests
```groovy
sh "mvn verify -Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=9095 -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=9095"
```

You may need to exclude some hosts from the proxy. If so use the -Dhttp.nonProxyHosts parameter, eg -Dhttp.nonProxyHosts=*.com\\|*.co.uk

### Proxying localhost
By default Java will not proxy localhost, 127.0.0.1, or any common loopback addresses. There is no way to disable this unless you set -Dhttp.nonProxyHosts= (empty). This means it's impossible to proxy just localhost without editing project code. You can mitigate this by changing your applications host to localhost.localdomain, which isn't checked by Java 
