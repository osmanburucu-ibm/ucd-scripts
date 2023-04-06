# Documenting the Permission Model

***NOTE***: issues with added types. actions are missing - needs investigation.

## Links to articles/blogs

* <https://community.ibm.com/community/user/wasdevops/blogs/ibm-ibm-devops-expert/2022/05/09/urbancode-deploy-10-minute-tip-documenting-the-per>
* <https://community.ibm.com/community/user/wasdevops/blogs/laurel-dickson-bull1/2020/12/02/urbancode-deploy-10-minute-tips-so-what-permission>
* <https://community.ibm.com/community/user/wasdevops/blogs/laurel-dickson-bull1/2020/12/16/urbancode-deploy-10-minute-tip-documenting-a-ucd-p>

## Extract Permissions

* prereq:
  * The tool is executed via a shell script and you will need a copy of the uDeployRestClient.jar file in the same directory.  You can find this jar in any of the UCD plugins that provide services to automate UCD itself.
  * You will also need to setup the environment variables JAVA_HOME pointing at your JRE and also GROOVY_HOME pointing at a groovy installation.  (UCD agents have one of these in their opt sub-directory.)

run with

~~~sh
./extractPermissions.sh -user admin -password admin -weburl https://localhost:8443 > permissons.out
~~~

## Show the effective Permissions for given user and object

* prereq:
  * run as admin

### example

So, in this example, we're asking the tool what permissions user harry has over the component sec-tests and where the permissions arise from.

~~~sh
# ./effectivePermissions.sh -user admin -password admin -weburl https://localhost:8443 --foruser harry --forobject  component --objectname sec-tests
UCD User Effective Permissions V0.5 December 2020
User harry gets permissions from the following roles:
data in team data team with resource role Standard
data in team data team with resource role prod
dba in team database team with resource role Standard
deployer in team database team with resource role Standard  via membership of group sec-group-test

Effective Permissions for user harry on object Component:sec-tests are:
Create: Create Components :: Create new components for this team.
Create: Create Components From Template :: Create components from a component template
Edit: Delete :: Delete components.
Edit: Edit Basic Settings :: Edit basic settings for components.
Edit: Manage Process Lock :: Manage lock on process draft.
Edit: Manage Processes :: Manage component processes.
Edit: Manage Properties :: Manage properties for components.
Edit: Manage Teams :: Manage teams for components.
Edit: Manage Version Status :: Manage the status of versions
Edit: Manage Versions :: Manage versions for components.
View: View Components :: View components in this team.

Permissions withheld are:
Edit: Approve Promotion :: Approve promotion of process draft.
Edit: Manage Configuration Templates :: Install and manage configuration templates for components.
# 
~~~
