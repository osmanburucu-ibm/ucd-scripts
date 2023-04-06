import java.net.URI;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import com.urbancode.ud.client.UDRestClient;
import com.urbancode.ud.client.ApplicationClient;
import com.urbancode.ud.client.ComponentClient;
import com.urbancode.ud.client.EnvironmentClient;
import com.urbancode.ud.client.AgentClient;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.commons.lang.StringUtils;

// Input parameter defaults
userid="";
password="";
weburl=""
queryUser="";
objType="";
objTypeId="";
objName="";
objParent="";

title= "UCD User Effective Permissions V0.5 December 2020"
println Ansi.colour (title, Ansi.BOLD)

// This class contains all of the methods to execute API calls that aren't covered by the standard clients
public class extensionsClient extends UDRestClient
{
    public extensionsClient(final URI url, final String clientUser, final String clientPassword) {
        super(url, clientUser, clientPassword);
    }
    public extensionsClient(final URI url, final String clientUser, final String clientPassword, final boolean trustAllCerts)
    {
        super(url, clientUser, clientPassword, trustAllCerts);
    }
    public extensionsClient(final URI url, final DefaultHttpClient client) {
        super(url, client);
    }
    JSONArray getSecurityRoleActionMappings(final String roleID)
    {
    	// fetch the action mappings for a given role.  This maps permissions to roles including user defined types
	String uri = this.url.toString() + "/security/role/${roleID}/actionMappings";
	return getArrayResponse(uri)
    }
    JSONArray getSecurityRoles()
    {
	// This routine enumerates the set of security roles defined on the server
	String uri = this.url.toString() + "/security/role";
	return getArrayResponse(uri)
    }
    JSONArray getSecurityClassPermissions(final String resourceClass)
    {
	// This routine enumerates the permissions for a given resource class like application / component etc
	String uri = this.url.toString() + "/security/resourceType/${encodePath(resourceClass)}/actions";
	return getArrayResponse(uri)
    }
    JSONArray getResourceRolesforClass(final String resourceClass)
    {
	// This routine gets the list of resource roles (types) that have been defined for a specific resourceClass (object type)
	String uri = this.url.toString() + "/security/resourceType/${encodePath(resourceClass)}/resourceRoles";
	return getArrayResponse(uri)
    }
    JSONArray getSecurityResourceTypes()
    {
	String uri = this.url.toString() + "/security/resourceType";
	return getArrayResponse(uri)
    }
    JSONArray teamRoleMembers(String team)
    {
	// This routine looks up the team members for each role
	String uri = this.url.toString() + "/cli/team/info?team=" + encodePath(team)
	return getObjectResponse(uri).roleMappings
    }
    JSONObject getAgentPool(String pool)
    {
	// This routine looks up the information about an agent pool - this one uses a PUT hence the inline code
	String uri = this.url.toString() + "/cli/agentPool/info?pool=" + encodePath(pool)
	final HttpPut method = new HttpPut(uri);
	final CloseableHttpResponse response = invokeMethod((HttpRequestBase)method);
	final String body =  this.getBody(response) ;
	JSONObject result = new JSONObject(body)
	return result
    }
    JSONObject getApplicationTemplate(String appTemplate)
    {
	// This routine looks up the information about an application template
	String uri = this.url.toString() + "/cli/applicationTemplate/info?applicationTemplate=" + encodePath(appTemplate)
	return getObjectResponse(uri)
    }
    JSONObject getResourceTemplate(String resourceTemplate)
    {
	// This routine looks up the information about an resource template
	String uri = this.url.toString() + "/cli/resourceTemplate?template=" + encodePath(resourceTemplate)
	return getObjectResponse(uri)
    }
    JSONObject getResource(String resourcePath)
    {
	// This routine looks up the information about a resource 
	String uri = this.url.toString() + "/cli/resource/info?resource=" + encodePath(resourcePath)
	return getObjectResponse(uri)
    }
    JSONObject getUserInfo(String user)
    {
	// This routine looks up the information about a user
	String uri = this.url.toString() + "/cli/user/info?user=" + encodePath(user)
	return getObjectResponse(uri)
    }
    JSONObject getComponentTemplate(String id)
    {
	// This routine looks up the information about a component template
	String uri = this.url.toString() + "/rest/deploy/componentTemplate/${id}"
	return getObjectResponse(uri)
    }
    JSONArray getAllComponentTemplates()
    {
	// This routine looks up the information about all component templates
	String uri = this.url.toString() + "/cli/componentTemplate"
	return getArrayResponse(uri)
    }
    JSONObject getProcess(String id)
    {
	// This routine looks up the information about a generic process
	String uri = this.url.toString() + "/rest/process/${id}"
	return getObjectResponse(uri)
    }
    JSONArray getAllProcesses()
    {
	// This routine looks up the information about all generic processes
	String uri = this.url.toString() + "/cli/process"
	return getArrayResponse(uri)
    }
    JSONObject getRelay(String id)
    {
	// This routine looks up the information about a generic process
	String uri = this.url.toString() + "/rest/relay/${id}"
	return getObjectResponse(uri)
    }
    JSONArray getAllRelays()
    {
	// This routine looks up the information about a generic process
	String uri = this.url.toString() + "/cli/relay"
	return getArrayResponse(uri)
    }
    private JSONObject getObjectResponse(String uri)
    {
	final HttpGet method = new HttpGet(uri)
	final CloseableHttpResponse response = invokeMethod((HttpRequestBase)method);
	final String body =   this.getBody(response)
	JSONObject result = new JSONObject(body)
	return result
    }
    private JSONArray getArrayResponse(String uri)
    {
	final HttpGet method = new HttpGet(uri)
	final CloseableHttpResponse response = invokeMethod((HttpRequestBase)method);
	final String body =   this.getBody(response)
	JSONArray result = new JSONArray(body)
	return result
    }
}

// Parse out the command line options 
ParseCommandLine(this.args)

// Create the UCDRestClient objects for api access
DefaultHttpClient myHttpClient = UDRestClient.createHttpClient(userid, password, true) 
myClient = new extensionsClient( new URI(weburl), myHttpClient)
myEnvClient = new EnvironmentClient (new URI(weburl), myHttpClient)
myCompClient = new ComponentClient (new URI(weburl), myHttpClient)
myAppClient = new ApplicationClient (new URI(weburl), myHttpClient)
myAgentClient = new AgentClient( new URI(weburl), myHttpClient)

// Validate parameters that need the udclient rest interfaces
ValidateObjectType(objType)

// So first lets get the names of the teams from the required object
JSONArray objTeams = getTeamsForObject(objType, objName)

// OK, for the given object type, what permissions are available?
JSONArray classPermissions = myClient.getSecurityClassPermissions(objType)
classPermissions = addGrantedAttribute(classPermissions)

// Get some information about a user including the groups they belong to
JSONObject userInfo =  myClient.getUserInfo(queryUser)
if (userInfo.isLockedOut) println Ansi.colour("NOTE:This user account is currently locked.", Ansi.RED + Ansi.BOLD)
JSONArray userGroups = userInfo.groups

println "User ${Ansi.colour(queryUser, Ansi.CYAN+Ansi.BOLD)} gets permissions from the following roles:"
// So now we need to find out what roles the query user has in the teams that are applied to the object
for (idx=0;idx<objTeams.length();idx++)
{
	// get the team name and the resource role applied to the object
	team = objTeams.get(idx).teamLabel
	if (objTeams.get(idx).has("resourceRoleLabel")) 
	{
		resourceRole = objTeams.get(idx).resourceRoleLabel 
		displayRR = resourceRole	
	}
	else 
	{
		resourceRole=""
		displayRR = "Standard"	
	}

	// Now find out what roles in the team the query user belongs to
	JSONArray teamMemberMappings =  myClient.teamRoleMembers(team)
	for ( mapping =0 ; mapping < teamMemberMappings.length(); mapping++)
	{
		isMember = false
		if (teamMemberMappings.get(mapping).has("user"))
		{
			// Deal with individual user mappings
			if (teamMemberMappings.get(mapping).user.name == queryUser)
			{
				isMember = true
				println "${Ansi.colour(teamMemberMappings.get(mapping).role.name, Ansi.BLUE)} " + \
					"in team ${Ansi.colour(team, Ansi.BLUE)} with resource role ${Ansi.colour(displayRR, Ansi.BLUE)}"
			}
		}
		else if (teamMemberMappings.get(mapping).has("group"))
		{
			if (userInGroup(teamMemberMappings.get(mapping).group.name, userGroups))
			{
				isMember = true
				println "${Ansi.colour(teamMemberMappings.get(mapping).role.name, Ansi.BLUE)} " + \
					"in team ${Ansi.colour(team, Ansi.BLUE)} with resource role ${Ansi.colour(displayRR, Ansi.BLUE)} " +
					" via membership of group ${Ansi.colour(teamMemberMappings.get(mapping).group.name, Ansi.BLUE)}"
			}
		}
		else
		{
				println "unexpected team mapping ${teamMemberMappings.get(mapping)}"
				System.exit(1)
		}

		if (isMember)
		{
			// now we need the set of permissions for the role + type for the given object class
			JSONArray roleActions = myClient.getSecurityRoleActionMappings(teamMemberMappings.get(mapping).role.id)

			// So now we need to iterate through the action map to find out
			// a. if the action is for the UCD object class query querying on
			// b. if the resourcerole matches the one that is added to the query object
			// So iterate over the action list
			for ( action = 0; action < roleActions.length(); action++)
			{
				// Check if the action has an explicit resource role
				if ( roleActions.get(action).has("resourceRole"))
				{
					// if it does and it matches the resource role applied to the object
					if (roleActions.get(action).resourceRole.name == resourceRole)
					{
						// Set the permission as granted
						classPermissions = setPermissionGrantedByID(classPermissions, roleActions.get(action).action.id)
					}
				}
				else
				{
					//Doesnt have a resource role so we need to make sure we're not processing a non resource role element
					if (resourceRole == "" )
					{
						//Set the permissions as granted
						classPermissions = setPermissionGrantedByID(classPermissions, roleActions.get(action).action.id)
					}
				}
			}
		}
	}
}
println ""
def subheading = "Effective Permissions for user ${Ansi.colour(queryUser, Ansi.CYAN, Ansi.NORMAL + Ansi.BOLD)} on object " + \
                  "${Ansi.colour(objType,Ansi.CYAN, Ansi.NORMAL + Ansi.BOLD)}:${Ansi.colour(objName,Ansi.CYAN, Ansi.NORMAL+Ansi.BOLD)} "
if (objParent != "" ) subheading += ", parent ${Ansi.colour(objParent,Ansi.CYAN, Ansi.Normal+Ansi.BOLD)} "
subheading += "are:"
println Ansi.colour(subheading, Ansi.BOLD)
printEffectivePermissions(classPermissions, true)
println ""
println Ansi.colour("Permissions withheld are:", Ansi.BOLD)
printEffectivePermissions(classPermissions, false)

// This method parses the command options to extract connection details
void ParseCommandLine(args)
{
	def idx=0
	def arg=""
	while ( idx < args.size())
	{
		arg = args[idx].toLowerCase();
		switch (arg)
		{
			case "-user":
			case "--user":
				userid=args[++idx];
				break;
			case "-password":
			case "--password":
				password=args[++idx];
				break;
			case "-weburl":
			case "--weburl":
				weburl=args[++idx];
				break;
			case "-foruser":
			case "--foruser":
				queryUser= args[++idx];
				break;
			case "-forobject":
			case "--forobject":
				objType = args[++idx];
				break;
			case "-objectname":
			case "--objectname":
				objName= args[++idx];
				break;
			case "-parent":
			case "--parent":
				objParent=args[++idx];
				break;
			default:
				println  "Unexpected argument ${arg}\n";
				println  "Expected command line -user <ucd admin username> -password <password for admin user>" + \
					 " -weburl https://<host>:<port> -foruser <search user> -forobject <object type> " + \
					 "-objectname <name of object>  {<-parent <name of parent object>}\n"
				System.exit(1);
		}
		idx++;
	}
	if (userid=="" || password=="" || weburl=="" || queryUser =="" || objType =="" || objName =="")
	{
		println "Missing required parameter - Expected parameters -user <ucd admin username> -password <password for admin user>" + \
			" -weburl https://<host>:<port> -foruser <search user> -forobject <object type> -objectname <name of object> " + \
			" {<-parent <name of parent object>}\n"
		System.exit(1)
	}
}
Boolean userInGroup(String searchGroup, JSONArray groups)
{
	// Check groups list to see if user is in the named group
	for (grp = 0 ; grp < groups.length(); grp++)
	{
		if (groups.get(grp).name == searchGroup)
			return true
	}
	return false
}
String ValidateObjectType (String type)
{
	// Find all of the resourcetypes and validate the objcet type parameter
	JSONArray resourceTypes = myClient.getSecurityResourceTypes()
	found=false
	validTypes=""
	for (res=0; res< resourceTypes.length(); res++)
	{
		validTypes+= resourceTypes.get(res).name + ", "
		if (resourceTypes.get(res).name.toLowerCase() == type.toLowerCase())
		{
			// we've got a match in object type, so now copy the internal name and ID
			objType = resourceTypes.get(res).name
			objTypeId = resourceTypes.get(res).id
			found = true
			break;
		}
	}

	if (! found )
	{
		println "${type} is not an object in UCD that can have teams added to them.  Valid types are:"
		println validTypes
		System.exit(1)
	}
	return objType
}

def getTeamsForObject(String objType, String objectid)
{
	// Get the teams for a given object type
	switch (objType.toLowerCase())
	{
		case "application":
			JSONObject data = myAppClient.getApplication(objectid)
			return data.extendedSecurity.teams
		case "application template":
			JSONObject data = myClient.getApplicationTemplate(objectid)
			return data.extendedSecurity.teams
		case "component":
			JSONObject data = myCompClient.getComponent(objectid)
			return data.extendedSecurity.teams
		case "environment":
			JSONObject data = myEnvClient.getEnvironment(objectid, objParent)
			return data.extendedSecurity.teams
		case "resource":
			JSONObject data = myClient.getResource(objectid)
			return data.extendedSecurity.teams
		case "resource template":
			JSONObject data = myClient.getResourceTemplate(objectid)
			return data.extendedSecurity.teams
		case "agent":
			JSONObject data = myAgentClient.getAgent(objectid)
			return data.extendedSecurity.teams
		case "agent pool":
			JSONObject data = myClient.getAgentPool(objectid)
			return data.extendedSecurity.teams
		case "version":
			println "This object type doesn't have independent permissions ie you can't add teams to it.  See parent object"
			System.exit(1)
		case "component template":
			JSONArray allTemplates = myClient.getAllComponentTemplates()
			JSONObject data = myClient.getComponentTemplate(lookupIDFromName(objectid, allTemplates,true))
			return data.extendedSecurity.teams
		case "process":
			JSONArray allProcesses = myClient.getAllProcesses()
			JSONObject data = myClient.getProcess(lookupIDFromName(objectid, allProcesses,true))
			return data.extendedSecurity.teams
		case "agent relay":
		  	JSONArray allRelays = myClient.getAllRelays()
			JSONObject data = myClient.getRelay(lookupIDFromName(objectid, allRelays,false))
			return data.extendedSecurity.teams
		case "agent configuration template":
			// nothing found in API
		case "cloud connection":
			// nothing found in API
		case "environment template":
			// Doesnt seem to be a way to get the environment ID which is needed to get the details of env template security
		case "external approval":
			// nothing found in API
		case "server configuration":
			// server config only for admins really
		case "web ui":
			// webui is about access not team based permisisons
			println "Can't work out effective permissions for this object type - No known / published API available."
			System.exit(1)
		default:
			println "Unknown object type : $objType"
			System.exit(1)
	}
}
String lookupIDFromName(String name, JSONArray response, Boolean includeVersion )
{
	for (i=0; i< response.length(); i++)
	{
		if (response.get(i).name == name )
		{
			if (includeVersion)
				return response.get(i).id+"/"+response.get(i).version
			else
				return response.get(i).id
		}
	}
	println "Object ${objectid} not found."
	System.exit (1)
}
JSONArray addGrantedAttribute(JSONArray classPermissions)
{
	// The classPermissions is a JSON array of records and we need to add a new attribute to
	// each record to record if the permission is granted or not.  We set them all to ungranted
	// and then change to granted if we find that is has been
	for (perm=0; perm<classPermissions.length(); perm ++)
	{
		classPermissions.get(perm).put("granted", false) 
	}
	return classPermissions
}
JSONArray setPermissionGrantedByName (JSONArray classPermissions, String forPermissionName)
{
	// this routine sets the granted flag to X for the named permission
	// may need to do this by ID in case there are sub matches on name
	for (perm=0; perm<classPermissions.length(); perm ++)
	{
		if (classPermissions.get(perm).name == forPermissionName)
		{
			classPermissions.get(perm).put("granted",true)
			break
		}
	}
	return classPermissions
}
JSONArray setPermissionGrantedByID (JSONArray classPermissions, String forPermissionID)
{
	// this routine sets the granted flag to X for the named permission
	// may need to do this by ID in case there are sub matches on name
	for (perm=0; perm<classPermissions.length(); perm ++)
	{
		if (classPermissions.get(perm).id == forPermissionID)
		{
			classPermissions.get(perm).put("granted",true)
			break
		}
	}
	return classPermissions
}
def printEffectivePermissions(JSONArray classPermissions, Boolean granted)
{
	for (perm=0; perm<classPermissions.length(); perm ++)
	{
		if (classPermissions.get(perm).granted == granted)
		{
			if (classPermissions.get(perm).has("category"))
				println "${classPermissions.get(perm).category}: ${classPermissions.get(perm).name} :: ${classPermissions.get(perm).description}"
			else
				println "View: ${classPermissions.get(perm).name} :: ${classPermissions.get(perm).description}"
		}
	}
}
class Ansi 
{
    static final String NORMAL          = "\u001B[0m"
    static final String	BOLD            = "\u001B[1m"
    static final String	ITALIC	        = "\u001B[3m"
    static final String	UNDERLINE       = "\u001B[4m"
    static final String	BLACK           = "\u001B[30m"
    static final String	RED             = "\u001B[31m"
    static final String	GREEN           = "\u001B[32m"
    static final String	YELLOW          = "\u001B[33m"
    static final String	BLUE            = "\u001B[34m"
    static final String	MAGENTA         = "\u001B[35m"
    static final String	CYAN            = "\u001B[36m"
    static String colour(String text, String ansiValue, String revertTo = NORMAL) 
    {
	return ansiValue + text + revertTo
    }
}
