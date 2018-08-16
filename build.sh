sudo service jenkins stop

sudo mvn install
sudo cp ./target/zap-comp.hpi /var/lib/jenkins/plugins/

sudo service jenkins start
