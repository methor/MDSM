/**
 * @author hengxin
 * @date May 11, 2014
 * @description {@link SessionManager} manages user sessions,
 * including login action, login checking, and log-in information storage.
 * 
 * Note: the code is adapted from that in the site: 
 * <url>http://www.androidhive.info/2012/08/android-session-management-using-shared-preferences/</url>
 */
package consistencyinfrastructure.login;


import consistencyinfrastructure.group.member.SystemNode;

public class SessionManager
{

	private static final String TAG = SessionManager.class.getName(); 


	
	// pid [required]
	public static final String KEY_NODE_ID = "PID";
	
	// name [required]
	public static final String KEY_NODE_NAME = "NAME";
	
	// ip [required]
	public static final String KEY_NODE_IP = "IP"; 
	
	// algorithm type [required]
	public static final String KEY_ALG_TYPE = "ALG_TYPE";

	// edit by hms
	public static boolean REDIRECT_TO_WRAPPER = true;

	/**
	 * constructor of {@link SessionManager}
	 * 
	 * @param context {@link Context} in which the {@link SessionManager} works
	 * 	Note: the parameter should be YourActivity.this instead of getApplicationContext(); 
	 *   otherwise you may get the WindowManager$BadTokenException.
	 *  See <url>http://stackoverflow.com/questions/7933206/android-unable-to-add-window-token-null-is-not-for-an-application-exception</url>
	 */
	public static SessionManager getNewInstance()
	// edit by hms; this simply wrap the constructor to avoid use _context
	// so that I also change constructor's access pattern(from public to private),
	// and create a dumb constructor with an int param.
	{
		if (REDIRECT_TO_WRAPPER == true)
		{
			return new SessionManagerWrapper();

		}

		else
			return new SessionManager();

	}

	private SessionManager()
	{

	}

	//
	protected SessionManager(int useless)
	{

	}



	/**
	 * @return the identifier of the logged {@link SystemNode}
	 */
	public int getNodeId()
	{
		return SystemNode.NODE_ID_DEFAULT;
	}
	
	/**
	 * @return the name of the logged {@link SystemNode}
	 */
	public String getNodeName()
	{
		return SystemNode.NODE_NAME_DEFAULT;
	}
	
	/**
	 * @return the ip address of the logged {@link SystemNode}
	 */
	public String getNodeIp()
	{
		return SystemNode.NODE_IP_DEFAULT;
	}




}
