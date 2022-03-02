[//]: # ([![GitHub release]&#40;https://img.shields.io/github/release/vrondakis/zap-jenkins-pipeline-plugin.svg?style=for-the-badge&#41;]&#40;https://github.com/vrondakis/zap-jenkins-pipeline-plugin/releases&#41;)

[//]: # ([![Language grade: Java]&#40;https://img.shields.io/lgtm/grade/java/g/vrondakis/zap-jenkins-pipeline-plugin.svg?logo=lgtm&logoWidth=18&style=for-the-badge&#41;]&#40;https://lgtm.com/projects/g/vrondakis/zap-jenkins-pipeline-plugin/context:java&#41;)

[//]: # ([![Language grade: JavaScript]&#40;https://img.shields.io/lgtm/grade/javascript/g/vrondakis/zap-jenkins-pipeline-plugin.svg?logo=lgtm&logoWidth=18&style=for-the-badge&#41;]&#40;https://lgtm.com/projects/g/vrondakis/zap-jenkins-pipeline-plugin/context:javascript&#41;)

<br />

<a href='https://www.owasp.org/index.php/OWASP_Zed_Attack_Proxy_Project' align="top"><img align="left" src='https://github.com/vrondakis/zap-jenkins-pipeline-plugin/raw/master/src/main/webapp/logo.png'></a>
ZAP Jenkins Plugin for pipeline builds
===
Please see the [ZAP pipeline plugin page](https://plugins.jenkins.io/zap-pipeline) for more information.

<br><br>
## Building the plugin

This will generate a .hpi file in the target directory that you can install on your Jenkins installation. 

```groovy
mvn clean install
```

## Installation
Copy the ./target/zap-pipeline.hpi file to the $JENKINS_HOME/plugins directory and restart Jenkins.
You can also use the plugin management console (Manage Jenkins -> Manage Plugins -> Advanced -> Upload Plugin)

Below is a shell script you can use to automatically build and install the plugin

```sh
sudo service jenkins stop
mvn install
cp ./target/zap-pipeline.hpi /var/lib/jenkins/plugins/zap-pipeline.hpi
sudo service jenkins start
```




## Contributing to the plugin
New feature proposals and bug fix proposals should be submitted as [pull requests](https://help.github.com/articles/creating-a-pull-request). Fork the repository, prepare your change on your forked copy, and submit a pull request. Your pull request will be evaluated by a developer and merged


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
