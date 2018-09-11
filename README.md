
<img src="https://i.imgur.com/WtTwQtt.png">


<br />

<a href='https://www.owasp.org/index.php/OWASP_Zed_Attack_Proxy_Project' align="top"><img align="left" src='https://github.com/vrondakis/zap-jenkins-pipeline-plugin/raw/master/src/main/webapp/logo.png'></a>ZAP Jenkins Plugin for pipeline builds
===
This is a Jenkins pipeline plugin that let's you control <a href="https://www.owasp.org/index.php/OWASP_Zed_Attack_Proxy_Project">OWASP Zed Attack Proxy</a> through Jenkins Pipeline. It also generates a good-looking report with new alerts (compared to the previous build) and optionally fails the build if any new high-risk alerts are found, and more!
  
  -----
 
## API
**startZap** - Starts the ZAP process and configures the plugin. 
```groovy
startZap(host: 127.0.0.1, port: 9095, timeout: 900, failBuild:3, zapHome: "/opt/zaproxy", allowedHosts:['10.0.0.1'], sessionPath:"/path/to/session.session")

host: The host to run the ZAP proxy server on. Passed to ZAP in the -host parameter.
port: The port to run the proxy on
timeout (optional): If a scan takes too long it will stop
failBuild (optional): If a new alert with a risk higher than this, the build will fail (0=Info, 1=Low, 2=Medium, 3=High, 4=None)
allowedHosts (optional): Once the active ZAP scan starts, it won't scan any hosts unless they are here. If you don't set this it will only scan if the host is localhost
sessionPath (optional): If you want to load a previous ZAP session that you have expored, you can do that here. Useful when you want to run a scan but don't want to run all your tests through ZAP.
```
<br>

**runZapCrawler** - Runs the ZAP crawler on a specific URL
```groovy
runZapCrawler(host: "https://localhost")
```

<br>
**importZapScanPolicy** - Loads a specific ZAP attack policy from the path you specify (Scan Policy Manager -> Export), to be used with runZapAttack

```groovy
importZapScanPolicy(policyPath: "/home/you/yourattackpolicy.policy")
```
<br>

**importZapUrls** - Imports a list of URLs to ZAP. You need the "Import files containing URLs" plugin for this to work.
```groovy
importZapUrls(path: "/path/to/your/urls")
```
<br>

**runZapAttack** - Once you have proxied your tests through ZAP or ran the crawler, this function runs an active scan on all the hosts that have been provided in the allowedHosts parameter in startZap.
```groovy
runZapAttack(userId: 5, scanPolicyName: "yourScanPolicy")

userId (optional): Run the scan with a specific user, loaded from the session
scanPolicyName (optional): The attack policy to use when running the scan. Loaded with importScanPolicy
```
<br>

**archiveZap** - Reads the alerts found by ZAP, checks if there are any new alerts that are higher than the failBuild parameter (and fails the build if so), generates a report with differences, and finally shuts down ZAP. This should be the last thing you run.

```groovy
archiveZap()
```

-----

## Proxying your tests
```groovy
sh "mvn verify -Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=9095 -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=9095"
```

You may need to exclude some hosts from the proxy. If so use the -Dhttp.nonProxyHosts parameter, eg -Dhttp.nonProxyHosts=*.com\\|*.co.uk

-----

## Proxying localhost
By default Java will not proxy localhost, 127.0.0.1, or any common loopback addresses. There is no way to disable this unless you set -Dhttp.nonProxyHosts= (empty). This means it's impossible to proxy just localhost without editing project code. You can mitigate this by changing your applications host to localhost.localdomain, which isn't checked by Java 

-----

## License

	The MIT License (MIT)
	
	Copyright (c) 2016 Goran Sarenkapa (JordanGS), and a number of other of contributors
	
	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:
	
	The above copyright notice and this permission notice shall be included in all
	copies or substantial portions of the Software.
	
	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
	SOFTWARE.
