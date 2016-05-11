# Dependency-analysis

## Build a project
  - mvn clean package

### Build a project and run integration tests in testsuite module
  - mvn clean verify -DtestsuiteContainer=/path/to/EAP-6.4/dir
  - You can exclude tests, which use remote services, using -PexcludeRemoteTests

### Deploy/work with local jboss instance

  - make sure your instance of jboss is running (/path/to/EAP-6.4/bin/standalone.sh)

  - DEPLOY: mvn clean package jboss-as:deploy -PdeployApplicationEar
      - deploys 'application' module (application/target/dependency-analysis.ear)
      - deploys to standalone/data instead of standalone/deployments, this is normal
      - default endpoints http://localhost:8080/da/ http://localhost:8080/da-bcg/
      - swagger provides a generated UI to test the endpoints http://localhost:8080/da/doc/doc http://localhost:8080/da-bcg/doc

  - UNDEPLOY: mvn jboss-as:undeploy -PdeployApplicationEar
  - NOTE: deploying through IDE via 'Run on Server' (e.g. JBDS) seems to corrupt expected endpoints

### Run/debug integration tests in 'testsuite' through IDE

  - modify testsuite/src/test/resources/arquillian.xml, setting:
      - ${jboss.port} to 9154
      - ${jboss.home} to /path/to/EAP-6.4/dir
      - keep these changes for yourself/do not push
  - for test classes annotated @RunWith(Arquillian.class): Run/Debug As -> JUnit Test

### Dealing with IDE errors in workspace:

  - Java errors:
      - Project DA uses lombok.jar to generate boilerplate getters/setters/constructors via annotations
      - Download/install into your IDE as per https://projectlombok.org/download.html

  - Other errors:
      - Safe to ignore as long as Maven build completes successfully from command line

