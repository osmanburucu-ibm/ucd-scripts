import java.net.URI;
import java.util.LinkedHashMap;
import com.urbancode.ud.client.UDRestClient;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;

userid="";
password="";
weburl="";

System.err << "UCD Role Permissions Extractor V3.0 December 2020\n"

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

    // fetch the action mappings for a given role.  This maps permissions to roles including user defined types
    JSONArray getSecurityRoleActionMappings(final String roleID)
    {
	String uri = this.url.toString() + "/security/role/${roleID}/actionMappings";
	final HttpGet method = new HttpGet(uri);
	final CloseableHttpResponse response = invokeMethod((HttpRequestBase)method);
	final String body =  this.getBody(response) ;
	return new JSONArray(body);
    }
    JSONArray getSecurityRoles()
    {
	String uri = this.url.toString() + "/security/role";
	final HttpGet method = new HttpGet(uri);
	final CloseableHttpResponse response = invokeMethod((HttpRequestBase)method);
	final String body =  this.getBody(response) ;
	return new JSONArray(body);
    }
    JSONArray getSecurityClassPermissions(final String resourceClass)
    {
	String uri = this.url.toString() + "/security/resourceType/${resourceClass}/actions";
	final HttpGet method = new HttpGet(uri);
	final CloseableHttpResponse response = invokeMethod((HttpRequestBase)method);
	final String body =  this.getBody(response) ;
	return new JSONArray(body);
    }
    JSONArray getResourceRolesforClass(final String resourceClass)
    {
	String uri = this.url.toString() + "/security/resourceType/${resourceClass}/resourceRoles";
	final HttpGet method = new HttpGet(uri);
	final CloseableHttpResponse response = invokeMethod((HttpRequestBase)method);
	final String body =  this.getBody(response) ;
	return new JSONArray(body);
    }
    JSONArray getSecurityResourceTypes()
    {
	String uri = this.url.toString() + "/security/resourceType";
	final HttpGet method = new HttpGet(uri);
	final CloseableHttpResponse response = invokeMethod((HttpRequestBase)method);
	final String body =  this.getBody(response) ;
	return new JSONArray(body);
    }
}

ParseCommandLine(this.args);
myClient = new extensionsClient( new URI(weburl), userid , password, true);

// Permissions Extraction
// OK, lets start the exercise by getting the list of role permissions classifications eg, agent, agent pool, application and so on.
// Map contains the permission classification and its ID
LinkedHashMap <String, String> Role_Class_Info = getNameAndID(myClient.getSecurityResourceTypes());
Role_Class_Info.sort{it}

// Now build a map of the roles and ID's
LinkedHashMap <String, String> Role_Info = getNameAndID(myClient.getSecurityRoles());

// print static part of column header line
print "Permission Category\tPermission Name\tPermission Description\tResource Role"
NumRoles=Role_Class_Info.size();

// Output the names of all the roles
Role_Info.each {print "\t" + it.key };

// Terminate column headings line
println  "";

// Now get the permissions map for all of the roles against each role there is a JSON array of permissions
LinkedHashMap<String, JSONArray>  RoleMaps = getRoleMaps(Role_Info);

// Now process each of the role permission classifications
Role_Class_Info.each 
{
	// Get values for loop
	EncodedClassName = it.key.replace(' ', '%20');
	CLASS_NAME = it.key;
	CLASS_ID = it.value;

	// Get the permissions details for each permission in the class category
	JSONArray ClassPermissions = myClient.getSecurityClassPermissions(EncodedClassName);

	// get the names of the resourcetypes available for the category 
	// So we will end up with lists of all of the types for each permission category and also the IDs of those
	JSONArray resourceRoles = myClient.getResourceRolesforClass(EncodedClassName);

	// see if there are any additional to the standard type
	RESOURCE_ROLE_NAMES= ["standard"];
	RESOURCE_ROLE_IDS = ["NA"];
	if ( resourceRoles.length() > 0 )
	{
		for (int i= 0 ; i< resourceRoles.length() ; ++i)
		{
			JSONObject propObject = (JSONObject)resourceRoles.get(i);
			RESOURCE_ROLE_NAMES = RESOURCE_ROLE_NAMES << propObject.get("name");
			RESOURCE_ROLE_IDS = RESOURCE_ROLE_IDS << propObject.get("id");
		}
	}

	lastPermID=""
	lastPermName=""
	permCount=0
	// Now iterate over each permission/type combination and find out what roles have it 
        for (int i = 0; i < ClassPermissions.length(); ++i) 
	{
		final JSONObject propObject = (JSONObject)ClassPermissions.get(i);
		PERM_NAME = propObject.get("name");
		PERM_ID = propObject.get("id");
		PERM_DESC = propObject.get("description");

		for (int j=0; j<RESOURCE_ROLE_NAMES.size(); ++j)
		{
			// Print the static data for each permission
			print "${CLASS_NAME}\t${PERM_NAME}\t${PERM_DESC}\t"+RESOURCE_ROLE_NAMES[j]

			// Check to see if a permissions wasn't assigned to any role
			if ( lastPermID != PERM_ID)
			{
				if (lastPermID != "")
					if ( permCount == 0 ) System.err << " The permission \'${lastPermName}\' of Class \'${CLASS_NAME}\' was not assigned to any role\n"
				lastPermID= PERM_ID
				lastPermName = PERM_NAME
				permCount=0	
			}
				
			// Add the assigned permission information to the output line
			Role_Info.each
			{
				ROLE = it.key;
				JSONArray MapForRole = RoleMaps[ROLE];
				assigned =  searchJSONArrayforKey(MapForRole, PERM_ID, RESOURCE_ROLE_NAMES[j]);
				if (assigned.contains("X")) permCount++
				print assigned
			}
			println "";
		}
	}
}
System.err << "Extract Complete\n"

// This method searches the action map for a role looking to see if the role has the permission we're interested in
// It returns either an 'X' for present or blank for missing
String searchJSONArrayforKey (JSONArray actionsMap, String id , String resourceRole)
{
	// Search the JSON array given for an oject containing the specific value of a given key
	for ( int i = 0 ; i < actionsMap.length(); ++i)
	{
		JSONObject obj = actionsMap.get(i);
		try
		{
			if ( resourceRole == "standard" )
				if ( obj.find {it.action.id == id} != null ) return "\tX";
			else
				if (obj.find {it.resourceRole.name == resourceRole}  != null ) return "\tX";
		}
		catch (Exception JSONException)
		{
		}
	}
	return "\t";
}

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
			default:
				System.err << "Unexpected argument ${arg}\n";
				System.err << "Expected command line -user <ucdd admin username> -passsword <password for admin user> -weburl https://<host>:<port> \n"
				System.exit(1);
		}
		idx++;
	}
}
LinkedHashMap<String, String> getNameAndID(JSONArray roleInfo) throws IOException, JSONException 
{
        final Map<String, String> result = new LinkedHashMap<String, String>();
        for (int i = 0; i < roleInfo.length(); ++i) 
	{
            final JSONObject propObject = (JSONObject)roleInfo.get(i);
            result.put((String)propObject.get("name"), (String)propObject.get("id"));
        }
        return result;
}
LinkedHashMap<String, JSONArray> getRoleMaps(LinkedHashMap <String, String> RoleInfo )
{
	final Map<String, JSONArray> result = new LinkedHashMap<String, JSONArray>();
	RoleInfo.each
	{
		result.put((String)it.key, (JSONArray) myClient.getSecurityRoleActionMappings(it.value));
	}
	return result;
}