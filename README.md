
[![GitHub release](https://img.shields.io/github/release/vrondakis/zap-jenkins-pipeline-plugin.svg?style=for-the-badge)](https://github.com/vrondakis/zap-jenkins-pipeline-plugin/releases)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/vrondakis/zap-jenkins-pipeline-plugin.svg?logo=lgtm&logoWidth=18&style=for-the-badge)](https://lgtm.com/projects/g/vrondakis/zap-jenkins-pipeline-plugin/context:java)
[![Language grade: JavaScript](https://img.shields.io/lgtm/grade/javascript/g/vrondakis/zap-jenkins-pipeline-plugin.svg?logo=lgtm&logoWidth=18&style=for-the-badge)](https://lgtm.com/projects/g/vrondakis/zap-jenkins-pipeline-plugin/context:javascript)

<br />

<a href='https://www.owasp.org/index.php/OWASP_Zed_Attack_Proxy_Project' align="top"><img align="left" src='https://github.com/vrondakis/zap-jenkins-pipeline-plugin/raw/master/src/main/webapp/logo.png'></a>ZAP Jenkins Plugin for pipeline builds
===
This is a Jenkins pipeline plugin that lets you control <a href="https://www.owasp.org/index.php/OWASP_Zed_Attack_Proxy_Project">OWASP Zed Attack Proxy</a> through Jenkins Pipeline. Unlike the other plugins for ZAP, this generates a report independently which shows new alerts compared to the last build, has the ability to fail a build with user configurable failure parameters, filters out false positives (user configurable) and displays a graph which shows the amount of ZAP alerts over your builds.


<img src="https://i.imgur.com/WtTwQtt.png">
<br><br>

<img src="https://i.imgur.com/R8vkzwy.png">

## Jenkinsfile Usage example
```groovy
pipeline {
    agent any
    stages { 
        stage('Setup') {
            steps {
                script {
                    startZap(host: 127.0.0.1, port: 9091, timeout:500, zapHome: "/opt/zaproxy", sessionPath:"/somewhere/session.session", allowedHosts:['github.com']) // Start ZAP at /opt/zaproxy/zap.sh, allowing scans on github.com
                }
            }
        }
        stage('Build & Test') {
            steps {
                script {
                    sh "mvn verify -Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=9091 -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=9091" // Proxy tests through ZAP
                }
            }
        }
    }
    post {
        always {
            script {
                archiveZap(failAllAlerts: 1, failHighAlerts: 0, failMediumAlerts: 0, failLowAlerts: 0, falsePositivesFilePath: "zapFalsePositives.json")
            }
        }
    }
}
```

This example is a declarative pipeline, but using the functions on a scripted pipeline works the same.

 
## API
**startZap** - Starts the ZAP process and configures the plugin. 
```groovy
startZap(host: 127.0.0.1, port: 9095, timeout: 900, failHighAlert:1, failLowAlert:10, zapHome: "/opt/zaproxy", allowedHosts:['10.0.0.1'], sessionPath:"/path/to/session.session")

host: The host to run the ZAP proxy server on. Passed to ZAP in the -host parameter.
port: The port to run the proxy on
timeout (optional): If a scan takes too long it will stop
failAllBuild (optional): Maximum amount of alerts that can happen in total before a build will fail
failHighBuild (optional): Maximum amount of high risk alerts that can happen before a build will fail
failMediumBuild (optional): Maximum amount of medium risk alerts that can happen before a build will fail
failLowBuild (optional): Maximum amount of low risk alerts that can happen before a build will fail
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

**archiveZap** - Reads the alerts found by ZAP, filters out any false positives if a false positives file is provided in the project, checks if there are any alerts that are higher than the fail build parameters (and fails the build if so), generates a report, and finally shuts down ZAP. This should be the last thing you run.

```groovy
archiveZap(failAllAlerts: 1, failHighAlerts: 0, failMediumAlerts: 0, failLowAlerts: 0, falsePositivesFilePath: "zapFalsePositives.json")
```

-----

## Proxying your tests
```groovy
sh "mvn verify -Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=9095 -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=9095"
```

You may need to exclude some hosts from the proxy. If so use the -Dhttp.nonProxyHosts parameter, eg -Dhttp.nonProxyHosts=*.com\\|*.co.uk

-----

## Proxying localhost
By default Java will not proxy localhost, 127.0.0.1, or any common loopback addresses. There is no way to disable this unless you set -Dhttp.nonProxyHosts= (empty). This means it's impossible to proxy just localhost without editing project code. You can mitigate this by changing your applications host to localhost.localdomain, which isn't checked by Java (NOTE: not all OS's have this hostname as a loopback address by default, you may need to add it to your machine's 'hosts' file).

-----

## Generating ZAP False Positives file
You can provide a JSON file of false positive definitions from your workspace to the zap plugin during the archive step (default is 'zapFalsePositives.json'). The file must consist of a single valid JSON array of 'False Positive' objects. Example:

```json
[
  {
    "name": "Cross Site Scripting (Reflected)",
    "cweid": "79",
    "wascid": "8",
    "uri": "https:\/\/yourdomain.com\/a\/certain\/url",
    "method": "POST",
    "param": "param1",
    "attack": "<script>alert(1);</script>",
    "evidence": "<script>alert(1);</script>"
  },
  {
    "uri": "https:\/\/yourdomain.com\/another/url",
    "method": "GET"
  }
]
```
All alert instances that match to a 'False Positive' object are ignored when judging whether to fail a build, and are initially hidden in the UI report. A match is when ALL fields provided in the False Positive object are equal to that in a given alert instance. It is best practice to be as specific as possible (to not hide future true positives that may occur).

To aid the generation of a false positives file, the UI report provides a 'Copy To Clipboard' button under each instance, that copies the alert instance as JSON, which can be used as a false positive object in the false positive file. 

The false positive URI is a regex string, alert instance URIs will be tested against this.

-----

## Installation
Download the latest release from the [releases](https://github.com/vrondakis/zap-jenkins-pipeline-plugin/releases) section. In Jenkins, go to 'Manage Jenkins' -> 'Manage Plugins', and select the 'Advanced' tab. Then under 'Upload Plugin', choose the downloaded hpi file and click upload.

-----

## License

	The MIT License (MIT)
	
	Copyright (c) 2018 Manolis Vrondakis
	
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
