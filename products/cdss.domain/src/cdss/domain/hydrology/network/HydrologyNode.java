// ----------------------------------------------------------------------------
// HydrologyNode - a representation of a node on a stream network
// ----------------------------------------------------------------------------
// Notes:	(1)	This code is meant to support (at least) three
//			CRDSS applications:
//
//			  makenet - used by planning model to generate network
//			  StateMod GUI - to allow interactive network edits
//			  Admin tool - to allow call analysis, water balance,
//					etc.
//
//			The data members for each capability are listed
//			separately, where appropriate.
//		(2)	It may be necessary or desirable to make a base class
//			and derive different node types (e.g., StateModNode,
//			WISNode) from that, but for now keep in one class.
// ----------------------------------------------------------------------------
// History:
// 
// 27 Oct 1997	Steven A. Malers,	Initial version - from makenet.
//		Riverside Technology,
//		inc.
// 28 Feb 1998	SAM, RTi		Final cleanup for makenet.
// 19 Feb 1999	Daniel Weiler, RTi	Added Wells.
// 31 Mar 1999	SAM, RTi		Code sweep.
// 07 Apr 1999	SAM, RTi		Add __isDryRiver flag to support WIS
//					(and potentially makene in Rio Grande).
// 27 Jul 1999	SAM, RTi		Update to have XCONFL node type.
// 18 Jul 1999	SAM, RTi		Add isValidType() for use by watright
//					and other applications.
// 15 Nov 1999	CEN, RTi		Added __labelAngle member and assoc.
//					functions.
// 06 Dec 1999	SAM, RTi		Add NODE_TYPE_DIV_AND_WELL to support
//					makenet and add to check method.
//					isValidType() is not used.  Instead, use
//					lookupType() and check for negative
//					return value.
// ----------------------------------------------------------------------------
// 2003-10-08	J. Thomas Sapienza, RTi	Updated to HydroBaseDMI.
// 2003-12-15	JTS, RTi		* Javadoc cleanup.
//					* Now extends DMIDataObject.
//					* Added visible member variable.
//					* Added initial hooks for the node to
//					  draw itself on a GRDrawingArea.
// 2004-04	JTS, RTi		Made numerous additions to class, most
//					in response to new needs dictated by
//					the network diagram drawing and plotting
//					code.
// 2004-05-18	JTS, RTi		Added setPosition().
// 2004-07-09	JTS, RTi		Added isImport.
//		SAM, RTi		Remove some new String() notation - it
//					is not needed.
//					Add some javadoc explaining the node
//					type abbreviations versus full type
//					strings.
// 2004-07-21	JTS, RTi		Added getVerboseWISType() for use with
//					WIS networks.
// 2004-12-20	JTS, RTi		Changed how label positions are numbered
//					so that original Makenet networks 
//					display properly.
// 2005-04-28	JTS, RTi		Added all data members to finalize().
// 2005-12-21	JTS, RTi		&, < and > in any node IDs are now
//					escaped.
// 2007-02-26	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------
// EndHeader

package cdss.domain.hydrology.network;

import java.io.PrintWriter;

import RTi.DMI.DMIDataObject;
import RTi.DMI.DMIUtil;

import RTi.GR.GRColor;
import RTi.GR.GRDrawingAreaUtil;
import RTi.GR.GRJComponentDrawingArea;
import RTi.GR.GRLimits;
import RTi.GR.GRSymbol;
import RTi.GR.GRText;
import RTi.GR.GRUnits;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

import java.awt.Font;
import java.util.List;
import java.util.Vector;

/**
This class stores basic node information for use with HydroBase_NodeNetwork.
*/
public class HydrologyNode extends DMIDataObject
{

/**
Class name.
*/
private final String __CLASS = "HydrologyNode";

/**
Blank node for spacing nodes on the Makenet plot.
*/
public static final int NODE_TYPE_BLANK	= 0;

/**
Diversion node.
*/
public static final int NODE_TYPE_DIV = 1;

/**
Streamflow node (stream gage).
*/
public static final int NODE_TYPE_FLOW = 2;

/**
Confluence node.
*/
public static final int NODE_TYPE_CONFLUENCE = 3;

/**
Instream flow node.
*/
public static final int NODE_TYPE_ISF = 4;

/**
Reservoir node.
*/
public static final int NODE_TYPE_RES = 5;

/**
Import node.
*/
public static final int NODE_TYPE_IMPORT = 6;

/**
TODO SAM 2008-12-10 Remove when natural flow is fully enabled and "other" node type use is confirmed
in user files.
Baseflow node (stream estimate).  Note that this should not typically be used but is retained
for historical reasons.  DO NOT CONVERT THIS TO NATURAL_FLOW as there is no natural flow node type.
*/
public static final int NODE_TYPE_BASEFLOW = 7;

/**
Node at the bottom of a network.
*/
public static final int NODE_TYPE_END = 8;

/**
Other node type (unclassified).
*/
public static final int NODE_TYPE_OTHER = 9;

/**
Unknown node type for initialization and StateMod .rin files that do not have the node type.
*/
public static final int NODE_TYPE_UNKNOWN = 10;

/**
Used for top of stream reaches.
*/
public static final int NODE_TYPE_STREAM = 11;

/**
Used for label points.
*/
public static final int NODE_TYPE_LABEL = 12;

/**
Used for formula cells.
*/
public static final int NODE_TYPE_FORMULA = 13;

/**
Well node (no connection to surface water ditch).
*/
public static final int NODE_TYPE_WELL = 14;

/**
Confluence node for downstream end of off-channel stream.
*/
public static final int NODE_TYPE_XCONFLUENCE = 15;

/**
Well node associated with surface water ditch.
*/
public static final int NODE_TYPE_DIV_AND_WELL = 16;

/**
Node only used in the network drawing code.  This is a node that functions as
a user-defined label on the screen.
*/
public static final int NODE_TYPE_LABEL_NODE = 17;

/**
Plan station, used with StateMod only.
*/
public static final int NODE_TYPE_PLAN = 18;

/**
Indicate how upstream nodes are constructed.  The default is to add tributaries
first (like makenet) but CWRAT adds the main stem first.
*/
public static final int 
	TRIBS_ADDED_FIRST = 1,
	TRIBS_ADDED_LAST = 2;

/**
To determine the kind of names to return.
*/
public final static int
	FULL = 0,
	ABBREVIATION = 1;

/**
Whether to draw the node text (the label like station ID) or not.
*/
private static boolean __drawText = true;

/**
Whether the node is being drawn in the WIS network display.
*/
/*
private boolean __inWis = true;
*/
//FIXME SAM 2008-03-15 Need to remove WIS from this general class

/**
Whether the node is a natural flow location.
*/
private	boolean __isNaturalFlow;

/**
Whether the node is an import node or not. 
*/
private boolean __isImport;

/**
Whether the river is dry at the node.  WIS-specific.
*/
private boolean __isDryRiver;

/**
Area as float.  Makenet-specific.
*/
private	double __area;
	
/**
Angle to print label at.  0 == East.  Makenet-specific.
*/
private double __labelAngle;

/**
Precipitation as float.  Makenet-specific.
*/
private double __precip;

/**
The proration factor to distribute the gain.  Makenet-specific.
*/
private double __prorationFactor;

/**
Stream mile for structure (overall from border).  WIS-specific.
*/
private	double __streamMile;

/**
Area * precip as float.  Makenet-specific.
*/
private double __water;

/**
Plotting X coordinate for node, in virtual page space.
*/
private double __x;
/**
Plotting Y coordinate for node, in virtual page space.
*/
private double __y;

/**
Pointer to the node directly downstream of this node.
*/
private HydrologyNode __downstream;

/**
WIS format row associated with this node.  WIS-specific.
*/
//private HydroBase_WISFormat __wisFormat;
// FIXME SAM 2008-03-15 Need to remove WIS from this general class

/**
The size of the icon in drawing units (points for printing, pixels for screen).
This is a space that will be filled by the specific symbol that is chosen for the node.
*/
private int __iconDiameter = 20;

/**
The extra space around the node icon in drawing units for displaying the extra shape decorator
showing that it is a natural flow node (big circle) or import (big square).  For example, if
the icon diameter is 20, the extra diameter is generally 1/3 of this value (to be added to the icon diameter.
*/
private int __decoratorDiameter = 6;

/**
Computational order of nodes, with 1 being most upstream.  This generally has 
to be set after the entire network has been populated. 
*/
private int __computationalOrder = -1;

/**
Label direction (for plotting network).  Makenet-specific.
*/
private int __labelDir;

/**
The node number in the reach (starting at one).
*/
private int __nodeInReachNum;

/**
The reach number counting the total number of streams in the system.  Therefore
the first reach in the system is 1, the next reach added is 2, etc.
*/
private int __reachCounter;

/**
The level of the river with 1 being the main stem.
*/
private int __reachLevel;

/**
Serial integer used to keep a running count of the nodes in the network.
*/
private int __serial;

/**
The number of tribs to the parent stream (starting at one).  In other words,
if the downstream node has multiple upstream nodes, this is the counter for
those nodes.  That allows a search coming from upstream to know which reach
it is coming from.  Mainly important on nodes above a confluence.
*/
private int __tributaryNum;

/**
Node type.
*/
private int __type;

/**
How upstream nodes are constructed.  See TRIBS_*.
*/
private int __upstreamOrder;

/**
Link data for confluences.
*/
private long __link;

/**
Equivalent to wdwater_num or stream_num (when that gets implemented).  
WIS-specific.
*/
private long __streamNum;

/**
An object of any kind that can be associated with a node.
*/
private Object __associatedObject;

/**
Area as string.  Makenet-specific.
*/
private	String __areaString;

/**
Node description.
*/
private String __desc;

/**
Common ID.
*/
private String __commonID;

/**
ID from the net file.  Makenet-specific.
*/
private String __netID;

/**
Precipitation as string.  Makenet-specific.
*/
private String __precipString;

/**
River node to use in output.  Makenet-specific.
*/
private String __riverNodeID;

/**
User description as set during the processMakenet process.  Makenet-specific.
*/
private	String __userDesc;

/**
Area * precip as String.  Makenet-specific.
*/
private String __waterString;

/**
List of nodes directly upstream of this node.
*/
private List<HydrologyNode> __upstream = new Vector();

//--------------------------------------------------------------------------
// Data members used solely with the network drawing code.
//--------------------------------------------------------------------------

/**
Whether this nodes drawing bounds have been calculated or not.
*/
private boolean __boundsCalculated = false;

/**
Whether this node was selected with a mouse drag.
*/
private boolean __isSelected = false;

/**
Whether this node was already stored in the database or was generated new
for the current network drawer.
*/
private boolean __readFromDB = false;

/**
Whether this node is visible or not.
*/
private boolean __visible = true;

/**
Whether calls should be shown.
*/
private boolean __showCalls = false;

/**
Whether the delivery flow data should be shown.
*/
private boolean __showDeliveryFlow = false;

/**
Whether the natural flow data should be shown.
*/
private boolean __showNaturalFlow = false;

/**
Whether the point flow data should be shown.
*/
private boolean __showPointFlow = false;

/**
Whether the rights should be shown.
*/
private boolean __showRights = false;

/**
The delivery flow value.
*/
private double __deliveryFlow = DMIUtil.MISSING_DOUBLE;

/**
The original UTM X and Y values read for this node's structure from the database.
*/
private double 
	__dbX = DMIUtil.MISSING_DOUBLE,
	__dbY = DMIUtil.MISSING_DOUBLE;

/**
The height of the area drawn by this node.
*/
private double __height;

/**
The natural flow value.
*/
private double __naturalFlow = DMIUtil.MISSING_DOUBLE;

/**
The point flow value.
*/
private double __pointFlow = DMIUtil.MISSING_DOUBLE;

/**
The width of the area drawn by this node.
*/
private double __width;

/**
The symbol drawn along with this node.  Depends on the type of node it is. 
Can be null, in which case no symbol will be drawn.
*/
private GRSymbol __symbol = null;

/**
A secondary symbol drawn in conjunction with the primary symbol.  If null, then it won't be drawn.
For example, the end node has a primary symbol of a circle and a secondary symbol of an iX.
Reservoirs also have a primary symbol of a circle, with a secondary symbol of an inner triangle.
*/
private GRSymbol __secondarySymbol = null;

/**
The wis num of the wis that this node is associated with.
*/
private int __wisNum = DMIUtil.MISSING_INT;

/**
Call information.
*/
private String __call = DMIUtil.MISSING_STRING;

/**
The unique identifier for this node.  Similar to the common ID.
*/
private String __identifier = null;

/**
The label drawn along with this node.
*/
private String __label = null;

/**
The verbose kind of node this is, as a string.
*/
private String __nodeType = null;

/**
Right information.
*/
private String __right = DMIUtil.MISSING_STRING;

/**
The text to be drawn inside the node symbol.
*/
private String __symText = null;

/**
The id of the node immediately downstream of this node.
*/
private String __downstreamNodeID = null;

/**
The ids of all the nodes immediately upstream of this node.
*/
private List<String> __upstreamNodeIDs = null;

/**
Constructor.  
Constructs node and initializes to reasonable values(primarily empty strings and zero or -1 values.
*/
public HydrologyNode() {
	initialize();
}

/**
Add a node downstream from this node.
@param downstream_node Downstream node to add.
@return true if successful, false if not.
*/
public boolean addDownstreamNode(HydrologyNode downstream_node) {
	String routine = __CLASS + ".addDownstreamNode";
	int dl = 50;

	try {

	if (Message.isDebugOn) {
		Message.printDebug(dl, routine,
			"Adding \"" + downstream_node.getCommonID()
			+ "\" downstream of \"" + getCommonID() + "\"");
	}
	
	HydrologyNode oldDownstreamNode = __downstream;

	if (__downstream != null) {
		// There is a downstream node and we need to reconnect it...

		// For the original downstream node, reset its upstream reference to the new node.
		// Use the common identifier to find the element to reset...
		int pos = __downstream.getUpstreamNodePosition(getCommonID());
		if (pos >= 0) {
			List downstreamUpstream = __downstream.getUpstreamNodes();
			if (downstreamUpstream != null) {
				downstreamUpstream.set(pos,downstream_node);
			}
		}
		// Connect the new downstream node to this node.
		__downstream = downstream_node;
		
		// Set the upstream node of the new downstream node to point to this node.
		// For now, assume that the node that is being inserted is a new node...
		if (downstream_node.getNumUpstreamNodes() > 0) {
			Message.printWarning(1, routine,
				"Node \"" + downstream_node.getCommonID() + "\" has #upstream > 0");
		}
		
		// Set the new downstream node data...
		downstream_node.setDownstreamNode(oldDownstreamNode);
		downstream_node.addUpstreamNode(this);
		// Set the new current node data...
		__tributaryNum = downstream_node.getNumUpstreamNodes();
	}
	else {	
		// We always need to do this step...
		downstream_node.addUpstreamNode(this);
	}
	
	String downstreamCommonid = null;
	if (downstream_node.getDownstreamNode() != null) {
		downstreamCommonid = oldDownstreamNode.getCommonID();
	}
	if (Message.isDebugOn) {
		Message.printDebug(dl, routine,
			"\"" + downstream_node.getCommonID() + "\" is downstream of \""
			+ getCommonID() + "\" and upstream of \"" + downstreamCommonid + "\"");
	}
	return true;
	
	}
	catch (Exception e) {
		Message.printWarning(2,routine,"Error adding downstream node.");
		Message.printWarning(2, routine, e);
		return false;
	}
}

/**
Adds a node upstream of this node.  This method is used by the network 
plotting code and simply appends this node to the end of the __upstream 
Vector, which is why it is used in lieu of the addUpstreamNode() method.
@param node the node to add.
*/
public void addUpstream(HydrologyNode node) {
	__upstream.add(node);
}

/**
Add a node upstream from this node.
@param upstream_node Node to add upstream.
@return true if successful, false if not.
*/
public boolean addUpstreamNode(HydrologyNode upstream_node) {
	String routine = __CLASS + ".addUpstreamNode";
	int dl = 50;

	// Add the node to the vector...
	try {
		if (Message.isDebugOn) {
			Message.printDebug(dl, routine,
				"Adding \"" + upstream_node.getCommonID() + "\" upstream of \"" + getCommonID() + "\"");
		}
		if (__upstream == null) {
			// Need to allocate space for it...
			__upstream = new Vector();
		}
		__upstream.add(upstream_node);
	
		// Make so the upstream node has this node as its downstream node...
		upstream_node.setDownstreamNode(this);
		if (Message.isDebugOn) {
			Message.printDebug(dl, routine, "\""
				+ upstream_node.getCommonID() + "\" downstream is \"" + getCommonID() + "\"");
		}
		
		return true;
	}
	catch (Exception e) {
		Message.printWarning(3, routine, "Error adding upstream node.");
		Message.printWarning(3, routine, e);
		return false;
	}
}

/**
Adds an id to the list of upstream node ids.  Used by the network drawing code.
@param id the id to add to the upstream node vector.  If the list is null it will first be created.
*/
public void addUpstreamNodeID(String id) {
	if (__upstreamNodeIDs == null) {
		__upstreamNodeIDs = new Vector();
	}
	__upstreamNodeIDs.add(id);
}

/**
Calculates the extent occupied by this node on the drawing area.
@param da the drawing area on which to calculate bounds.
*/
public void calculateExtents(GRJComponentDrawingArea da)
{
	// FIXME SAM 2008-03-15 Need to remove WIS from this general class
	/*
	if (__inWis) {
		calculateWISBounds(da);
	}
	else {
	*/
		calculateNodeExtentForNetwork(da);
	/*
	}
	*/
}

/**
Calculates extent of this node when rendered in the Network editor display.
@param da the drawing area on which to calculate bounds.
*/
private void calculateNodeExtentForNetwork(GRJComponentDrawingArea da) {
	__symText = null;
	__symbol = null;
	__nodeType = null;
	if (__nodeType == null) {
		switch (__type) {
			case NODE_TYPE_BLANK: 	
				__nodeType = "Blank";
				break;
			case NODE_TYPE_DIV:	
				__nodeType = "Diversion";
				break;
			case NODE_TYPE_FLOW:	
				__nodeType = "Streamflow";
				break;
			case NODE_TYPE_CONFLUENCE: 
				__nodeType = "Confluence";
				break;
			case NODE_TYPE_ISF:	
				__nodeType = "Instream Flow";
				break;
			case NODE_TYPE_RES:	
				__nodeType = "Reservoir";
				break;
			case NODE_TYPE_IMPORT:	
				__nodeType = "Import";
				break;
			case NODE_TYPE_BASEFLOW:
				__nodeType = "Baseflow";
				break;
			case NODE_TYPE_END:	
				__nodeType = "End";
				break;
			case NODE_TYPE_OTHER:	
				__nodeType = "Other";
				break;
			case NODE_TYPE_UNKNOWN:	
				__nodeType = "Unknown";
				break;
			case NODE_TYPE_STREAM:	
				__nodeType = "StreamTop";
				break;
			case NODE_TYPE_LABEL:	
				__nodeType = "Label";
				break;
			case NODE_TYPE_WELL:	
				__nodeType = "Well";
				break;
			case NODE_TYPE_XCONFLUENCE: 
				__nodeType = "XConfluence";
				break;
			case NODE_TYPE_DIV_AND_WELL:
				__nodeType = "DiversionAndWell";
				break;
			case NODE_TYPE_PLAN:
				__nodeType = "Plan";
				break;
			default:
				Message.printStatus(1, "", "Unrecognized node "	+ "type: " + __type);
				__nodeType = null;
				break;
		}
	}
	
	if (__symbol == null) {
		// Symbol has not been defined for the node so do it (and adjust the size if necessary)
		setSymbolFromNodeType ( lookupType(__nodeType), true );	
	}

	String label = __label;

	if (__symbol != null) {
		double width = getIconDiameter();
		double height = getIconDiameter();
	
		if (__label != null) {
			GRLimits limits = GRDrawingAreaUtil.getTextExtents(da, label, GRUnits.DEVICE);
			if (limits.getHeight() > height) {
				height = limits.getHeight();
			}
			width += 4 + limits.getWidth();
		}
	}
	
	__boundsCalculated = true;
}

/**
Calculates bounds for nodes in the WIS Network display.
@param da the drawing area on which to calculate bounds.
*/
private void calculateWISBounds(GRJComponentDrawingArea da) {
	if (__symbol == null) {
		int style = GRSymbol.TYPE_POLYGON;
		GRColor black = GRColor.black;
		int iconDiameter = getIconDiameter();

		// The secondary symbol is used to blank out the area behind the primary symbol's node icon.  

		if (__nodeType.equalsIgnoreCase("Reservoir")) {
			__symbol = new GRSymbol(GRSymbol.SYM_RTRI,
				style, black, black, iconDiameter*2/3, iconDiameter*2/3);
			__secondarySymbol = new GRSymbol(GRSymbol.SYM_FRTRI,
				style, black, black, iconDiameter*2/3, iconDiameter*2/3);
			__symText = null;
		}
		else if (__nodeType.equalsIgnoreCase("Stream")) {
			__symbol = new GRSymbol(GRSymbol.SYM_CIR,
				style, black, black, iconDiameter*2/3, iconDiameter*2/3);
			__secondarySymbol = new GRSymbol(GRSymbol.SYM_FCIR,
				style, black, black, iconDiameter*2/3, iconDiameter*2/3);
			__symText = "O";
		}
		else if (__nodeType.equalsIgnoreCase("Confluence")) {
			__symbol = new GRSymbol(GRSymbol.SYM_CIR, style, black, black, iconDiameter*2/3, iconDiameter*2/3);
			__secondarySymbol = new GRSymbol(GRSymbol.SYM_FCIR,
				style, black, black, iconDiameter*2/3, iconDiameter*2/3);
			__symText = "C";
		}
		else if (__nodeType.equalsIgnoreCase("Station") || __nodeType.equalsIgnoreCase("Streamflow")) {
			__symbol = new GRSymbol(GRSymbol.SYM_CIR,
				style, black, black, iconDiameter*2/3, iconDiameter*2/3);
			__secondarySymbol = new GRSymbol(GRSymbol.SYM_FCIR,
				style, black, black, iconDiameter*2/3, iconDiameter*2/3);
			__symText = "B";
		}
		else if (__nodeType.equalsIgnoreCase("Diversion")) {
			__symbol = new GRSymbol(GRSymbol.SYM_CIR,
				style, black, black, iconDiameter*2/3, iconDiameter*2/3);
			__secondarySymbol = new GRSymbol(GRSymbol.SYM_FCIR,
				style, black, black, iconDiameter*2/3, iconDiameter*2/3);
			__symText = "D";
		}
		else if (__nodeType.equalsIgnoreCase("MinFlow")) {
			__symbol = new GRSymbol(GRSymbol.SYM_CIR,
				style, black, black, iconDiameter*2/3, iconDiameter*2/3);
			__secondarySymbol = new GRSymbol(GRSymbol.SYM_FCIR,
				style, black, black, iconDiameter*2/3, iconDiameter*2/3);
			__symText = "M";
		}
		else if (__nodeType.equalsIgnoreCase("Other")) {
			__symbol = new GRSymbol(GRSymbol.SYM_CIR,
				style, black, black, iconDiameter*2/3, iconDiameter*2/3);
			__secondarySymbol = new GRSymbol(GRSymbol.SYM_FCIR,
				style, black, black, iconDiameter*2/3, iconDiameter*2/3);
			__symText = "O";
		}
		else if (__nodeType.equalsIgnoreCase("Plan")) {
			__symbol = new GRSymbol(GRSymbol.SYM_CIR,
				style, black, black, iconDiameter*2/3, iconDiameter*2/3);
			__secondarySymbol = new GRSymbol(GRSymbol.SYM_FSQ,
				style, black, black, iconDiameter*2/3, iconDiameter*2/3);
			__symText = "PL";
		}
		else if (__nodeType.equalsIgnoreCase("End")) {
			__symbol = new GRSymbol(GRSymbol.SYM_FCIR,
				style, black, black, iconDiameter*2/3, iconDiameter*2/3);
			__symText = null;
		}	
		else {
			Message.printStatus(2, "", "Unknown node type: " + __nodeType);
		}
	}

	GRLimits data = da.getDataLimits();
	// TODO (JTS - 2004-05-19)
	// the following will need changed if we move from a scrollable area to a jumping area
	GRLimits draw = da.getDrawingLimits();

	// calculate the multiplier necessary to convert data units to device units.
	double mod = 1;
	if (draw.getWidth() > draw.getHeight()) {
		mod = data.getWidth() / draw.getWidth();
	}
	else {
		mod = data.getHeight() / draw.getHeight();
	}

	__height = getIconDiameter()*2/3 * mod;
	__width = getIconDiameter()*2/3 * mod;
	
	__boundsCalculated = true;
}

/**
Clears the upstream node ids stored in the __upstreamNodeIDs list.  The list is set to null.
*/
public void clearUpstreamNodeIDs()
{
	if ( __upstreamNodeIDs != null ) {
		__upstreamNodeIDs.clear();
	}
}

/**
Checks to see if the specified point is contained in the area of the node 
symbol.  Only checks the symbol of the node along with a tiny area outside the symbol as well.
@return true if the point is contained, false if not.
*/
public boolean contains(double x, double y) {
	if ((x >= __x - 2 - (__width / 2)) && (x <= (__x + (__width / 2 )+ 2))){
		if ((y >= __y - 2 - (__width/2)) && (y <= (__y + (__height / 2) + 2))) {
			return true;
		}
	}
	return false;
}

/**
Break the link with an upstream node.
@param upstreamNode Upstream node to disconnect from the network.
@return true if successful, false if not.
*/
public boolean deleteUpstreamNode(HydrologyNode upstreamNode) {
	String routine = __CLASS + ".deleteUpstreamNode";

	// Find a matching node.  Just check addresses...
	try {

		for (int i = 0; i < __upstream.size(); i++) {
			if (upstreamNode.equals( __upstream.get(i))) {
				// Found a match.  Delete the element...
				__upstream.remove(i);
				return true;
			}
		}
		return false;

	}
	catch (Exception e) {
		Message.printWarning(2,routine,"Error deleting upstream node.");
		return false;
	}
}

/**
Draws this node on the specified drawing area.
@param da the drawing area on which to draw the node.
*/
public void draw(GRJComponentDrawingArea da) {
	if (!__boundsCalculated) {
		calculateExtents(da);
	}

	// FIXME SAM 2008-03-15 Need to remove WIS from this general class
	/*
	if (__inWis) {
		drawWIS(da);
	}
	else {
	*/
		drawNodeForNetwork(da);
		/*
	}
	*/
}

/**
Draws this node on the network editor display.
@param da the GRJComponentDrawingArea on which to draw the node.
*/
private void drawNodeForNetwork(GRJComponentDrawingArea da) {
	String routine = "HydrologyNode.drawNodeForNetwork";

	double symbolSize = 0;
	// Symbol to be drawn as core - others may be drawn to decorate and reservoir symbol orientation
	// may be reset.
	int symbol = GRSymbol.SYM_NONE;

	// if there is a symbol to be drawn
	if (__symbol != null) {
		if ( __symbol.getType() == GRSymbol.SYM_CIR ) {
			// Fill in the background with white so the node can't be seen through.  For example, if
			// the symbol draws over something, the symbol needs to be clearly visible.
			GRDrawingAreaUtil.setColor(da, GRColor.white);
			GRDrawingAreaUtil.drawSymbol(da, GRSymbol.SYM_FCIR, __x, __y, __symbol.getSize(), GRUnits.DEVICE,0);
		}
		
		symbolSize = __symbol.getSize();

		if (__type == NODE_TYPE_RES) {
			// Need special care with reservoirs to orient the symbol the proper way.
			int labeldir = getLabelDirection()/10;
			if (labeldir == 1) {
				symbol = GRSymbol.SYM_FUTRI;
			}
			else if (labeldir == 2) {
				symbol = GRSymbol.SYM_FDTRI;
			}
			else if (labeldir == 3) {
				symbol = GRSymbol.SYM_FLTRI;
			}
			else if (labeldir == 4) {
				symbol = GRSymbol.SYM_FRTRI;
			}
			else {	
				symbol = GRSymbol.SYM_FRTRI;
			}			
		}
		else {
			// The symbol does not need special attention - get it from the node data
			symbol = __symbol.getType();
		}
	}

	if (__drawText) {
		// There is a label (e.g., station ID) that needs to be drawn outside the symbol
		String label = null;
		int labelPos = 0;
		double labelAngle = 0;
		if ( (__type == NODE_TYPE_BLANK) || (__type == NODE_TYPE_CONFLUENCE) ||
			(__type == NODE_TYPE_XCONFLUENCE) ) {
		   	// do nothing
		}    
		else if (__type == NODE_TYPE_DIV 
		    || __type == NODE_TYPE_DIV_AND_WELL 
		    || __type == NODE_TYPE_WELL 
		    || __type == NODE_TYPE_IMPORT
		    || __type == NODE_TYPE_FLOW
		    || __type == NODE_TYPE_END
		    || __type == NODE_TYPE_BASEFLOW
		    || __type == NODE_TYPE_ISF
		    || __type == NODE_TYPE_OTHER
		    || __type == NODE_TYPE_RES
		    || __type == NODE_TYPE_PLAN ) {
			label = getNodeLabel( HydrologyNodeNetwork.LABEL_NODES_COMMONID);
		}
		else {
			Message.printWarning(3, routine, "No text specified!");
			label = "";
		}

		int dir = getLabelDirection();
		dir = dir % 10;
		
		if (dir == 1) {
			labelPos = GRText.BOTTOM | GRText.CENTER_X;	
		}
		else if (dir == 7) {
			labelPos = GRText.BOTTOM | GRText.LEFT;
		}
		else if (dir == 4) {
			labelPos = GRText.LEFT | GRText.CENTER_Y;
		}
		else if (dir == 8) {
			labelPos = GRText.LEFT | GRText.TOP;
		}
		else if (dir == 2) {
			labelPos = GRText.TOP | GRText.CENTER_X;
		}
		else if (dir == 5) {
			labelPos = GRText.TOP | GRText.RIGHT;
		}
		else if (dir == 3) {
			labelPos = GRText.RIGHT | GRText.CENTER_Y;
		}
		else if (dir == 6) {
			labelPos = GRText.BOTTOM | GRText.RIGHT;
		}
		else if (dir == 9) {
			labelPos = GRText.CENTER_X | GRText.CENTER_Y;
		}
		else {
			labelPos = GRText.TOP | GRText.CENTER_X;
		}

		if (label == null) {
			label = "";
		}

		// Draw the text without a symbol, at the position that is appropriate for the largest symbol
		// that will be shown.  Do not draw a symbol so that the logic below can be simpler and
		// to avoid redundant drawing.  Text should always be black.
		if (__isSelected) {
			// Draw in the selection color
			GRDrawingAreaUtil.setColor(da, GRColor.cyan);
		}
		else {
			// Draw in black
			GRDrawingAreaUtil.setColor(da, GRColor.black);
		}
		if ( getIsNaturalFlow() || getIsImport() ) {
			// Draw the label text slightly offset because the decorator symbol takes up more room.
			Font oldFont = null;
			if ( getIsNaturalFlow() ) {
				// Reset to bold...
				oldFont = da.getFont();
				da.setFont( oldFont.getName(), Font.BOLD, oldFont.getSize());
			}
			GRDrawingAreaUtil.drawSymbolText(da, GRSymbol.SYM_NONE, 
				__x, __y, symbolSize + getDecoratorDiameter(), label, labelAngle, labelPos, GRUnits.DEVICE, 0);
			if ( getIsNaturalFlow() ) {
				// Set back to old font.
				da.setFont( oldFont.getName(), oldFont.getStyle(), oldFont.getSize());
			}
		}
		else {
			// No need to offset the text - draw close to the normal symbol.
			GRDrawingAreaUtil.drawSymbolText(da, GRSymbol.SYM_NONE, __x, __y, 
				symbolSize, label, labelAngle, labelPos, GRUnits.DEVICE, 0);
		}
	}
	
	// Draw the normal symbol

	if (__isSelected) {
		// Draw in the selection color
		GRDrawingAreaUtil.setColor(da, GRColor.cyan);
	}
	else {
		// Draw in black unless a different color has been specified for the symbol
		if ( __symbol != null ) {
			GRDrawingAreaUtil.setColor(da, __symbol.getColor());
		}
		else {
			GRDrawingAreaUtil.setColor(da, GRColor.black);
		}
	}
	//Message.printStatus(2, routine, "Drawing symbol " + symbol + " at " + __x + "," + __y + " size="+ symbolSize );
	GRDrawingAreaUtil.drawSymbol(da, symbol, __x, __y, symbolSize, GRUnits.DEVICE,0);

	// Draw a larger circle around decorator nodes
	if (__isSelected) {
		// Draw in the selection color
		GRDrawingAreaUtil.setColor(da, GRColor.cyan);
	}
	else {
		// Draw in black
		GRDrawingAreaUtil.setColor(da, GRColor.black);
	}
	if ( (__symbol != null) && getIsNaturalFlow()) {
		GRDrawingAreaUtil.drawSymbol(da, GRSymbol.SYM_CIR,
			__x, __y, __symbol.getSize() + getDecoratorDiameter(), GRUnits.DEVICE, 0);
	}
	
	// Draw a larger square around import nodes - same color as above
	if ( (__symbol != null) && getIsImport() ) {
		//GRDrawingAreaUtil.drawSymbol(da, GRSymbol.SYM_FARR1,
		GRDrawingAreaUtil.drawSymbol(da, GRSymbol.SYM_SQ,
			__x, __y, __symbol.getSize() + getDecoratorDiameter(), GRUnits.DEVICE, 0);
	}

	// Draw the secondary symbol, for example the inner X in the end node..
	if ( __secondarySymbol != null ) {
		GRDrawingAreaUtil.drawSymbol(da, __secondarySymbol.getType(), 
			__x, __y, __secondarySymbol.getSize(), GRUnits.DEVICE, 0);
	}
	
	// If used, draw text in the middle of the symbol (e.g., "D" for diversion node) - do last so
	// that it draws on top.  This should be in the select color or black.
	if (__isSelected) {
		// Draw in the selection color
		GRDrawingAreaUtil.setColor(da, GRColor.cyan);
	}
	else {
		// Draw in black
		GRDrawingAreaUtil.setColor(da, GRColor.black);
	}
	if (__symText != null){
		GRDrawingAreaUtil.drawText(da, __symText, __x, __y, 0, GRText.CENTER_Y | GRText.CENTER_X);
	}
}

/**
Draws this node for the WIS network display.
@param da the GRJComponentDrawingArea on which to draw the node.
*/
private void drawNodeForWIS(GRJComponentDrawingArea da) {
	// Format the label that accompanies the text
	String label = __label;
	if (__showDeliveryFlow) {
//		label += " DF: " + StringUtil.formatString(__deliveryFlow, "%10.1f").trim();
	}
	if (__showNaturalFlow) {
//		label += " NF: " + StringUtil.formatString(__naturalFlow, "%10.1f").trim();
	}
	if (__showPointFlow) {
		double pf = __pointFlow;
		if (!DMIUtil.isMissing(pf)) {
			label += " PF: " + StringUtil.formatString(pf, "%10.1f").trim();
		}
	}
	if (__showCalls) {
		if (!DMIUtil.isMissing(__call)) {
			label += __call;
		}
	}
	if (__showRights) {
		if (!DMIUtil.isMissing(__right)) {
			label += __right;
		}
	}

	if (__symbol != null) {
		GRDrawingAreaUtil.setColor(da, GRColor.white);
		GRDrawingAreaUtil.drawSymbol(da, __secondarySymbol.getType(), 
			__x, __y, __secondarySymbol.getSize(), GRUnits.DEVICE,
			GRSymbol.SYM_CENTER_X | GRSymbol.SYM_CENTER_Y);
	
		// Stored in the node with a +1 modifier, necessary to ensure some backwards compatibility.
		int dir = getLabelDirection() - 1;
		if (getType() == NODE_TYPE_RES) {
			dir = dir % 10;
		}
		
		int labelPos = GRText.TOP;

		if (dir == 0) {		//  above
			labelPos = GRText.BOTTOM | GRText.CENTER_X;
		}
		else if (dir == 1) {	// below
			labelPos = GRText.TOP | GRText.CENTER_X;
		}
		else if (dir == 2) {	// center
			labelPos = GRText.CENTER_X | GRText.CENTER_Y;
		}
		else if (dir == 3) {	// left
			labelPos = GRText.RIGHT | GRText.CENTER_Y;
		}
		else if (dir == 4) {	// lower left
			labelPos = GRText.RIGHT | GRText.TOP;
		}
		else if (dir == 5) {	// lower right
			labelPos = GRText.LEFT | GRText.TOP;
		}
		else if (dir == 6) {	// right
			labelPos = GRText.LEFT | GRText.CENTER_Y;
		}
		else if (dir == 7) {	// upper left
			labelPos = GRText.RIGHT | GRText.BOTTOM;
		}
		else if (dir == 8) {	// upper right
			labelPos = GRText.LEFT | GRText.BOTTOM;
		}

		GRDrawingAreaUtil.setColor(da, GRColor.black);
		GRDrawingAreaUtil.drawSymbolText(da, __symbol.getType(), 
			__x, __y, __symbol.getSize(), 
			label, GRColor.black, 0, labelPos, GRUnits.DEVICE,
			GRSymbol.SYM_CENTER_X | GRSymbol.SYM_CENTER_Y);
	}

	if (__symText != null) { 
		GRDrawingAreaUtil.drawText(da, __symText,
			__x, __y, 0, GRText.CENTER_Y | GRText.CENTER_X);
	}		
}

/**
Return a label to use for the specified node.
@param lt Label type(see HydroBase_NodeNetwork.LABEL_NODES_*).
@return a label to use for the specified node.
*/
protected String getNodeLabel(int lt) {
	String label = null;
	if (lt == HydrologyNodeNetwork.LABEL_NODES_AREA_PRECIP) {
		label = getAreaString() + "*" + getPrecipString();
	}
	else if (lt == HydrologyNodeNetwork.LABEL_NODES_COMMONID) {
		label = getCommonID();
	}
	else if (lt == HydrologyNodeNetwork.LABEL_NODES_PF) {
		if (getIsNaturalFlow() && (getType() != HydrologyNode.NODE_TYPE_FLOW)) {
			label = StringUtil.formatString( getProrationFactor(), "%5.3f");
		}
		else {	
			label = "";
		}
	}
	else if (lt == HydrologyNodeNetwork.LABEL_NODES_RIVERNODE) {
		label = getRiverNodeID();
	}
	else if (lt == HydrologyNodeNetwork.LABEL_NODES_WATER) {
		label = getWaterString();
	}
	else {	
		label = getNetID();
	}
	return label;
}

/**
Finalize before garbage collection.
*/
protected void finalize()
throws Throwable {
	__downstream = null;
	// FIXME SAM 2008-03-15 Need to remove WIS from this general class
	//__wisFormat = null;
	__associatedObject = null;
	__areaString = null;
	__desc = null;
	__commonID = null;
	__netID = null;
	__precipString = null;
	__riverNodeID = null;
	__userDesc = null;
	__waterString = null;
	__upstream = null;
	__symbol = null;
	__secondarySymbol = null;
	__call = null;
	__identifier = null;
	__label = null;
	__nodeType = null;
	__right = null;
	__symText = null;
	__downstreamNodeID = null;
	__upstreamNodeIDs = null;
	super.finalize();
}

/**
Returns the makenet area.  Makenet-specific.
@return the makenet area.
*/
public double getArea() {
	return __area;
}

/**
Returns the makenet area as a String.  Makenet-specific.
@return the makenet area as a String.
*/
public String getAreaString() {
	return __areaString;
}

/**
Returns the object associated with a node.
@return the object associated with a node.
*/
public Object getAssociatedObject() {
	return __associatedObject;
}

/**
Returns the node common id.
@return the node common id.
*/
public String getCommonID() {
	return __commonID;
}

/**
Returns the computational order of nodes.
@return the computational order of nodes.
*/
public int getComputationalOrder() {
	return __computationalOrder;
}

/**
Returns the diameter of the icon drawn for this node.  Used by the network plotting tools.
TODO (JTS - 2004-05-20) might not be necessary -- many other methods -- would they work?
@return the diameter of the icon drawn for this node.
*/
public double getDataDiameter() {
	return __width;
}

/**
Returns the decorator diameter.
@return the decorator diameter.
*/
public int getDecoratorDiameter() {
	return __decoratorDiameter;
}

/**
Returns the delivery flow.
@return the delivery flow.
*/
public double getDeliveryFlow() {
	return __deliveryFlow;
}

/**
Returns the node description.
@return the node description.
*/
public String getDescription() {
	return __desc;
}

/**
Returns the UTM X value for the node's structure as stored in the database.
@return the UTM X value for the node's structure as stored in the database.
*/
public double getDBX() {
	return __dbX;
}

/**
Returns the UTM Y value for the node's structure as stored in the database.
@return the UTM Y value for the node's structure as stored in the database.
*/
public double getDBY() {
	return __dbY;
}

/**
Returns the node immediately downstream from this node.
@return the node immediately downstream from this node.
*/
public HydrologyNode getDownstreamNode() {
	return __downstream;
}

/**
Returns the id of the node immediately downstream from this node.  Used by
the network drawing code.  This method only returns the id stored in the
__downstreamNodeID data member.  It doesn't check the downstream node to get
the ID.  If setDownstreamNodeID() has not been called first, it will return null.
@return the id of the node immediately downstream from this node.
*/
public String getDownstreamNodeID() {
	return __downstreamNodeID;
}

/**
Returns the point at which network connections should be drawn for the node.
Currently only returns the x, y values.  
@return the point at which network connections should be drawn for the node.
*/
public double[] getDrawToPoint(GRJComponentDrawingArea da) {
	double[] d = new double[2];
	d[0] = __x;
	d[1] = __y;
	return d;
}

/**
Returns the height of the drawing area occupied by the node.
@return the height of the drawing area occupied by the node.
*/
public double getHeight() {
	return __height;
}

/**
Returns the icon diameter.
@return the icon diameter.
*/
public int getIconDiameter() {
	return __iconDiameter;
}

/**
Returns the network node drawer unique node identifier.
@return the network node drawer unique node identifier.
*/
public String getIdentifier() {
	return __identifier;
}

/**
Returns whether this node is a dry river or not.  WIS-specific.
@return whether this node is a dry river or not.
*/
public boolean getIsDryRiver() {
	return __isDryRiver;
}

/**
Returns whether this node is an import node.
@return whether this node is an import node.
*/
public boolean getIsImport() {
	return __isImport;
}

/**
Returns whether this node is a natural flow node (attribute on node, not node type).
@return whether this node is a natural flow node (attribute on node, not node type).
*/
public boolean getIsNaturalFlow() {
	return __isNaturalFlow;
}

/**
Returns the label drawn for this node.
@return the label drawn for this node.
*/
public String getLabel() {
	if (__label == null || __label.trim().equals("")) {
		return "";
	}
	return __label;
}

/**
Returns the angle at which to print the label.  Makenet-specific.
@return the angle at which to print the label.
*/
public double getLabelAngle() {
	return __labelAngle;
}

/**
Returns the direction to print the label at.  Makenet-specific.
@return the direction to print the label at.
*/
public int getLabelDirection() {
	return __labelDir;
}

/**
Returns the limits of the area occupied by the drawn node.  The limits only 
encompass the area occupied by the node icon.
@return the limits of the area occupied by the drawn node.
*/
public GRLimits getLimits() {
	return new GRLimits(__x - (__width / 2), __y - (__height / 2), 
		__x + (__width / 2), __y + (__height / 2));
}

/**
Returns the link data for confluences.
@return the link data for confluences.
*/
public long getLink() {
	return __link;
}

/**
Returns the natural flow.
@return the natural flow.
*/
public double getNaturalFlow() {
	return __naturalFlow;
}

/**
Returns the ID from the net file.  Makenet-specific.
@return the ID from the net file.
*/
public String getNetID() {
	return __netID;
}

/**
Returns the node number in the reach.
@return the node number in the reach.
*/
public int getNodeInReachNumber() {
	return __nodeInReachNum;
}

/**
Returns the type of node this is.
@return the type of node this is.
*/
public String getNodeType() {
	return __nodeType;
}

/**
Returns the number of upstream nodes.
@return the number of upstream nodes.
*/
public int getNumUpstreamNodes() {
	if (__upstream == null) {
		return 0;
	}
	else {	
		return __upstream.size();
	}
}

/**
Returns the point flow.
@return the point flow.
*/
public double getPointFlow() {
	return __pointFlow;
}

/**
Returns the precip.  Makenet-specific.
@return the precip.
*/
public double getPrecip() {
	return __precip;
}

/**
Returns the precip as a String.  Makenet-specific.
@return the precip as a String.
*/
public String getPrecipString() {
	return __precipString;
}

/**
Returns the gain proration factor.  Makenet-specific.
@return the gain proration factor.
*/
public double getProrationFactor() {
	return __prorationFactor;
}

/**
Returns the reach number.
@return the reach number.
*/
public int getReachCounter() {
	return __reachCounter;
}

/**
Returns the reach level.
@return the reach level.
*/
public int getReachLevel() {
	return __reachLevel;
}

/**
Returns the river node ID.  Makenet-specific.
@return the river node ID.
*/
public String getRiverNodeID() {
	return __riverNodeID;
}

/**
Returns the river node count.
@return the river node count.
*/
public int getSerial() {
	return __serial;
}

/**
Returns the structure stream mile.  WIS-specific.
@return the structure stream mile.
*/
public double getStreamMile() {
	return __streamMile;
}

/**
Returns the stream num.  WIS-specific.
@return the stream num.
*/
public long getStreamNumber() {
	return __streamNum;
}

/**
Return the symbol used with the node.
*/
public GRSymbol getSymbol ()
{
	return __symbol;
}

/**
Returns the label direction in a format that a proplist will know -- one of
the string values from GRText.  This assumes the string format is between 
1 and 9.  Any other values will result in "AboveCenter" being returned.
@return the label direction in a string format.
*/
public String getTextPosition() {
	// the text position as compared to the values in GRText is actually
	// stored +1 for some backwards compatibility issues.
	String[] positions = GRText.getTextPositions();
	if (__labelDir <= 0 || __labelDir > 9) {
		return positions[0];
	}
	else {
		return positions[(__labelDir - 1)];
	}
}		

/**
Returns the tributary number.
@return the tributary number.
*/
public int getTributaryNumber() {
	return __tributaryNum;
}

/**
Returns the node type.
@return the node type.
*/
public int getType() {
	return __type;
}

/**
Returns the elements in the __upstreamNodeIDs list as an array of Strings.
The list contains the IDs of all the nodes immediately upstream from this
node.  This is used by network drawing code.
@return a String array of all the ids of nodes immediately upstream from this node.
*/
public String[] getUpstreamNodeIDs() {
	int size = 0;
	if (__upstreamNodeIDs != null) {
		size = __upstreamNodeIDs.size();
	}
	String[] ids = new String[size];
	for (int i = 0; i < size; i++) {
		ids[i] = __upstreamNodeIDs.get(i);
	}
	return ids;
}

/**
Returns the IDs of all the nodes immediately upstream of this node.  Used by
drawing code.  This goes to each of the upstream nodes in the __upstream 
list and pulls out their ID to place in the String.
@return a String array of all the nodes' ids in the order the nodes are found in the __upstream list.
*/
public String[] getUpstreamNodesIDs() {
	if (__upstream == null || __upstream.size() == 0) {
		return new String[0];
	}

	String[] ids = new String[__upstream.size()];
	for (int i = 0; i < __upstream.size(); i++) {
		ids[i] = __upstream.get(i).getCommonID();
	}
	return ids;
}

/**
Returns the user description.  Makenet-specific.
@return the user description.
*/
public String getUserDescription() {
	return __userDesc;
}

/**
Returns the String node type from the integer type.
@param type the type of the node.
@param flag if FULL, return the full String (used in the network file).  If
ABBREVIATION, return the 3 letter abbreviation (used in StateMod station names).
@return the String node type from the integer type.
*/
public static String getTypeString(int type, int flag) {
	String stype = "";

	if (flag == ABBREVIATION) {
		// Abbreviations...
		if (type == NODE_TYPE_BLANK) {
			stype = "BLK";
		}
		else if (type == NODE_TYPE_DIV) {
			stype = "DIV";
		}
		else if (type == NODE_TYPE_DIV_AND_WELL) {
			stype = "D&W";
		}
		else if (type == NODE_TYPE_WELL) {
			stype = "WEL";
		}
		else if (type == NODE_TYPE_FLOW) {
			stype = "FLO";
		}
		else if (type == NODE_TYPE_CONFLUENCE) {
			stype = "CON";
		}
		else if (type == NODE_TYPE_ISF) {
			stype = "ISF";
		}
		else if (type == NODE_TYPE_RES) {
			stype = "RES";
		}
		else if (type == NODE_TYPE_IMPORT) {
			stype = "IMP";
		}
		else if (type == NODE_TYPE_BASEFLOW) {
			stype = "BFL";
		}
		else if (type == NODE_TYPE_END) {
			stype = "END";
		}
		else if (type == NODE_TYPE_OTHER) {
			stype = "OTH";
		}
		else if (type == NODE_TYPE_UNKNOWN) {
			stype = "UNK";
		}
		else if (type == NODE_TYPE_STREAM) {
			stype = "STR";
		}
		else if (type == NODE_TYPE_LABEL) {
			stype = "LAB";
		}
		else if (type == NODE_TYPE_FORMULA) {
			stype = "FOR";
		}
		else if (type == NODE_TYPE_XCONFLUENCE) {
			stype = "XCN";
		}
		else if (type == NODE_TYPE_LABEL_NODE) {
			stype = "LBN";
		}
		else if (type == NODE_TYPE_PLAN) {
			stype = "PLN";
		}
	}
	else {
		// Full name...
		if (type == NODE_TYPE_BLANK) {
			stype = "BLANK";
		}
		else if (type == NODE_TYPE_DIV) {
			stype = "DIV";
		}
		else if (type == NODE_TYPE_DIV_AND_WELL) {
			stype = "D&W";
		}
		else if (type == NODE_TYPE_WELL) {
			stype = "WELL";
		}
		else if (type == NODE_TYPE_FLOW) {
			stype = "FLOW";
		}
		else if (type == NODE_TYPE_CONFLUENCE) {
			stype = "CONFL";
		}
		else if (type == NODE_TYPE_ISF) {
			stype = "ISF";
		}
		else if (type == NODE_TYPE_RES) {
			stype = "RES";
		}
		else if (type == NODE_TYPE_IMPORT) {
			stype = "IMPORT";
		}
		else if (type == NODE_TYPE_BASEFLOW) {
			stype = "BFL";
		}
		else if (type == NODE_TYPE_END) {
			stype = "END";
		}
		else if (type == NODE_TYPE_OTHER) {
			stype = "OTH";
		}
		else if (type == NODE_TYPE_UNKNOWN) {
			stype = "UNKNOWN";
		}
		else if (type == NODE_TYPE_STREAM) {
			stype = "STREAM";
		}
		else if (type == NODE_TYPE_LABEL) {
			stype = "LABEL";
		}
		else if (type == NODE_TYPE_FORMULA) {
			stype = "FORMULA";
		}
		else if (type == NODE_TYPE_XCONFLUENCE) {
			stype = "XCONFL";
		}
		else if (type == NODE_TYPE_LABEL_NODE) { 
			stype = "LABELNODE";
		}
		else if (type == NODE_TYPE_PLAN) { 
			stype = "PLAN";
		}
	}
	return stype;
}

/**
Returns the first upstream node.
@return the reference to the first upstream node or null if not found.
*/
public HydrologyNode getUpstreamNode() {
	if (__upstream == null) {
		return null;
	}
	// Return the first one...
	return getUpstreamNode(0);
}

/**
Return the upstream node at the specific position.
@param position 0-index position of upstream node.
@return the reference to the upstream node for the specified position, or null if not found.
*/
public HydrologyNode getUpstreamNode(int position) {
	String routine = __CLASS + ".getUpstreamNode";

	if (__upstream == null) {
		return null;
	}
	if (__upstream.size() < (position + 1)) {
		Message.printWarning(1, routine,
			"Upstream position [" + position + "] is not found(max " + __upstream.size() + ")");
		return null;
	}
	// Return the requested one...
	return(HydrologyNode)__upstream.get(position);
}

/**
Returns an upstream node given its common id.
@param commonID the commonID for which to find an upstream node.
@return an upstream node position given its common ID, or -1 if not found.
*/
public int getUpstreamNodePosition(String commonID) {
	if (__upstream == null) {
		return -1;
	}
	int size = __upstream.size();
	HydrologyNode upstream;
	for (int i = 0; i < size; i++) {
		// Return the first one that matches...
		upstream = __upstream.get(i);
		if (commonID.equalsIgnoreCase(upstream.getCommonID())) {
			return i;
		}
	}
	return -1;
}

/**
Returns the list of upstream nodes.  The internal list is returned (not a new reference).
@return the list of upstream nodes.
*/
public List<HydrologyNode> getUpstreamNodes() {
	return __upstream;
}

/**
Returns how upstream nodes are constructed.  See TRIBS_*.
@return how upstream nodes are constructed.
*/
public int getUpstreamOrder() {
	return __upstreamOrder;
}

/**
Returns the verbose node type description string.
@param type the type of node for which to return the verbose type.
@return the verbose node type description string.
*/
public static String getVerboseType(int type) {
	String stype = "";
	// Abbreviations...
	if (type == NODE_TYPE_BLANK) {
		stype = "Blank";
	}
	else if (type == NODE_TYPE_DIV) {
		stype = "Diversion";
	}
	else if (type == NODE_TYPE_DIV_AND_WELL) {
		stype = "Diversion and Well";
	}
	else if (type == NODE_TYPE_WELL) {
		stype = "Well";
	}
	else if (type == NODE_TYPE_FLOW) {
		stype = "Streamflow";
	}
	else if (type == NODE_TYPE_CONFLUENCE) {
		stype = "Confluence";
	}
	else if (type == NODE_TYPE_ISF) {
		stype = "Instream Flow";
	}
	else if (type == NODE_TYPE_RES) {
		stype = "Reservoir";
	}
	else if (type == NODE_TYPE_IMPORT) {
		stype = "Import";
	}
	else if (type == NODE_TYPE_BASEFLOW) {
		stype = "Baseflow";
	}
	else if (type == NODE_TYPE_END) {
		stype = "End";
	}
	else if (type == NODE_TYPE_OTHER) {
		stype = "Other";
	}
	else if (type == NODE_TYPE_UNKNOWN) {
		stype = "Uknown";
	}
	else if (type == NODE_TYPE_STREAM) {
		stype = "Stream";
	}
	else if (type == NODE_TYPE_LABEL) {
		stype = "Label";
	}
	else if (type == NODE_TYPE_FORMULA) {
		stype = "Formula";
	}
	else if (type == NODE_TYPE_XCONFLUENCE) {
		stype = "XConfluence";
	}
	else if (type == NODE_TYPE_LABEL_NODE) {
		stype = "LabelNode";
	}
	else if (type == NODE_TYPE_PLAN) {
		stype = "Plan";
	}
	return stype;
}

/**
Returns the verbose node type description string.
@param type the type of node for which to return the verbose type.
@return the verbose node type description string.
*/
public static String getVerboseWISType(int type) {
	String stype = "";
	// Abbreviations...
	if (type == NODE_TYPE_BLANK) {
		stype = "Blank";
	}
	else if (type == NODE_TYPE_DIV) {
		stype = "Diversion";
	}
	else if (type == NODE_TYPE_DIV_AND_WELL) {
		stype = "Diversion and Well";
	}
	else if (type == NODE_TYPE_WELL) {
		stype = "Well";
	}
	else if (type == NODE_TYPE_FLOW) {
		stype = "Station";
	}
	else if (type == NODE_TYPE_CONFLUENCE) {
		stype = "Confluence";
	}
	else if (type == NODE_TYPE_ISF) {
		stype = "Instream Flow";
	}
	else if (type == NODE_TYPE_RES) {
		stype = "Reservoir";
	}
	else if (type == NODE_TYPE_IMPORT) {
		stype = "Import";
	}
	else if (type == NODE_TYPE_BASEFLOW) {
		stype = "Baseflow";
	}
	else if (type == NODE_TYPE_END) {
		stype = "End";
	}
	else if (type == NODE_TYPE_OTHER) {
		stype = "Other";
	}
	else if (type == NODE_TYPE_UNKNOWN) {
		stype = "Uknown";
	}
	else if (type == NODE_TYPE_STREAM) {
		stype = "Stream";
	}
	else if (type == NODE_TYPE_LABEL) {
		stype = "Label";
	}
	else if (type == NODE_TYPE_FORMULA) {
		stype = "Formula";
	}
	else if (type == NODE_TYPE_XCONFLUENCE) {
		stype = "XConfluence";
	}
	else if (type == NODE_TYPE_LABEL_NODE) {
		stype = "LabelNode";
	}
	else if (type == NODE_TYPE_PLAN) {
		stype = "Plan";
	}
	return stype;
}

/**
Returns area * precip as a float.  Makenet-specific.
@return area * precip.
*/
public double getWater() {
	return __water;
}

/**
Returns area * precip as a String.  Makenet-specific.
@return area * precip as a String.
*/
public String getWaterString() {
	return __waterString;
}

/**
Returns the width of the drawing area occupied by this node.
@return the width of the drawing area occupied by this node.
*/
public double getWidth() {
	return __width;
}

/**
Returns the wis format associated with this node.
@return the wis format associated with this node.
*/
/*
public HydroBase_WISFormat getWISFormat() {
	return __wisFormat;
}
*/
//FIXME SAM 2008-03-15 Need to remove WIS from this general class

/**
Returns the wis num associated with this node.
@return the wis num associated with this node.
*/
/*
public int getWis_num() {
	return __wisNum;
}
*/
//FIXME SAM 2008-03-15 Need to remove WIS from this general class

/**
Returns the node X coordinate.  Makenet-specific.
@return the node X coordinate.
*/
public double getX() {
	return __x;
}

/**
Returns the node Y coordinate.  Makenet-specific.
@return the node Y coordinate.
*/
public double getY() {
	return __y;
}

/**
Inserts an upstream node into the Vector of upstream nodes.  Used by the network diagramming tools.
@param node the node to insert.
@param pos the position at which to insert the node.
*/
public void insertUpstreamNode(HydrologyNode node, int pos) {
	__upstream.add(pos,node);
}

/**
Inserts multiple nodes into the list of upstream nodes.  Used by network diagramming tools.
@param nodes a non-null list of nodes to be inserted upstream of this node.
@param pos the position at which to insert the nodes.
*/
public void insertUpstreamNodes(List<HydrologyNode> nodes, int pos) {
	for (int i = nodes.size() - 1; i >= 0; i--) {
		__upstream.add(pos,nodes.get(i));
	}
}

/**
Initialize data members.
*/
private void initialize() {
	__areaString = "";
	__area = 0.0;
	__precipString = "";
	__precip = 0.0;
	__waterString = "";
	__water = 0.0;
	__prorationFactor = 0.0;
	// TODO SAM 2011-01-05 Evaluate whether missing values should be NaN
	__x = 0.0;
	__y = 0.0;
	__labelAngle = 45;

	__desc = "";
	__userDesc = "";

	__commonID = "";
	__netID = "";
	__riverNodeID = "";
	__isNaturalFlow = false;
	__labelDir = 1;
	__serial = 0;
	__computationalOrder = -1;
	__type = NODE_TYPE_BLANK;
	__tributaryNum = 1;
	__reachCounter = 0;
	__reachLevel = 0;
	__nodeInReachNum = 1;
	__downstream = null;
	__upstream = null;
	__upstreamOrder = TRIBS_ADDED_FIRST;

	// WIS data...
	__streamMile = DMIUtil.MISSING_DOUBLE;
	__streamNum = DMIUtil.MISSING_LONG;
	// FIXME SAM 2008-03-15 Need to remove WIS from this general class
	//__wisFormat = 		null;
	__link = DMIUtil.MISSING_LONG;
	__isDryRiver = false;
}

/**
Returns whether this node was read from the database or generated from a 
network during the latest invocation of the network drawing code.
@return whether this node was read from the database or not.
*/
public boolean isReadFromDB() {
	return __readFromDB;
}

/**
Returns whether this node was selected on the network diagram.
@return whether this node was selected on the netowrk diagram.
*/
public boolean isSelected() {
	return __isSelected;
}

/**
Returns whether this node is visible or not.
@return whether this node is visible or not.
*/
public boolean isVisible() {
	return __visible;
}

/**
Returns whether this node is within the specified limits or not.
@param limits the limits to check
@param scale the scale value of the drawing area
@return true if the node is within the limits, false if not.
*/
public boolean isWithinLimits(GRLimits limits, double scale) {
	return limits.contains(__x * scale, __y * scale, 
		(__x + __width) * scale, (__y + __height) * scale, false);
}

/**
Lookup the type from a string.  The string can be any recognized full or
abbreviated string used for the network or CWRAT.  It can also be the
abbreviated string used in StateMod station names and node diagram.
@return the integer node type or -1 if not recognized.
*/
public static int lookupType(String typeString)
{
	if (typeString.equalsIgnoreCase("BFL")) {
		return NODE_TYPE_BASEFLOW;
	}
	else if (typeString.equalsIgnoreCase("BLANK") || typeString.equalsIgnoreCase("BLK")) {
		return NODE_TYPE_BLANK;
	}
	else if (typeString.equalsIgnoreCase("confluence") || typeString.equalsIgnoreCase("CON")) {
		return NODE_TYPE_CONFLUENCE;
	}
	else if (typeString.equalsIgnoreCase("diversion") || typeString.equalsIgnoreCase("DIV")) {
		return NODE_TYPE_DIV;
	}
	else if (typeString.equalsIgnoreCase("D&W") || typeString.equalsIgnoreCase("DW") ||
			typeString.equalsIgnoreCase("DiversionAndWell")) {
		return NODE_TYPE_DIV_AND_WELL;
	}
	else if (typeString.equalsIgnoreCase("END")) {
		return NODE_TYPE_END;
	}
	else if (typeString.equalsIgnoreCase("FLOW") || typeString.equalsIgnoreCase("FLO") ||
		typeString.equalsIgnoreCase("Streamflow")) {
		return NODE_TYPE_FLOW;
	}
	else if (typeString.equalsIgnoreCase("formula")) {
		return NODE_TYPE_FORMULA;
	}
	else if (typeString.equalsIgnoreCase("ISF") || typeString.equalsIgnoreCase("Instream Flow") ) {
		return NODE_TYPE_ISF;
	}
	else if (typeString.equalsIgnoreCase("IMP")) {
		return NODE_TYPE_IMPORT;
	}
	else if (typeString.equalsIgnoreCase("LabelNode")) {
		return NODE_TYPE_LABEL_NODE;
	}
	else if (typeString.equalsIgnoreCase("minflow") || typeString.equalsIgnoreCase("ISF")) {
		return NODE_TYPE_ISF;
	}
	else if (typeString.equalsIgnoreCase("other") || typeString.equalsIgnoreCase("OTH")) {
		return NODE_TYPE_OTHER;
	}
	else if (typeString.equalsIgnoreCase("Plan")) {
		return NODE_TYPE_PLAN;
	}
	else if (typeString.equalsIgnoreCase("reservoir") || typeString.equalsIgnoreCase("RES")) {
		return NODE_TYPE_RES;
	}
	else if (typeString.equalsIgnoreCase("station") || typeString.equalsIgnoreCase("FLO")) {
		return NODE_TYPE_FLOW;
	}
	else if (typeString.equalsIgnoreCase("stream") || typeString.equalsIgnoreCase("STR")) {
		return NODE_TYPE_STREAM;
	}
	else if (typeString.equalsIgnoreCase("string")) {
		return NODE_TYPE_LABEL;
	}
	else if (typeString.equalsIgnoreCase("UNKNOWN")) {
		return NODE_TYPE_UNKNOWN;
	}
	else if (typeString.equalsIgnoreCase("well") || typeString.equalsIgnoreCase("WEL")) {
		return NODE_TYPE_WELL;
	}
	else if (typeString.equalsIgnoreCase("XCONFL") || typeString.equalsIgnoreCase("XCN")) {
		return NODE_TYPE_XCONFLUENCE;
	}
	else {
		String routine = "HydrologyNode.lookupType";
		Message.printWarning(3, routine,
			"Unable to convert node type \"" + typeString + "\" to internal type.");
		return -1;
	}
}

/**
Breaks apart proration information and saves in the node.  
@param string0 the input string.  Assumed to be either xxx*yyy or xxxx. 
@return true if successful, false if not.
*/
public boolean parseAreaPrecip(String string0) {
	String routine = __CLASS + ".getNodeAreaPrecip";

	// If the proration information is empty, use empty strings for other parts...
	try {
	setAreaString("");
	setPrecipString("");
	setWaterString("");

	if (string0 == null) {
		return true;
	}
	if (string0.length() == 0) {
		return true;
	}

	String string = string0;
	char theOperator = '\0';
	int length = string.length();
	int nfields = 1;
	for (int i = 0; i < length; i++) {
		if ( (string.charAt(i) == '-')
			|| (string.charAt(i) == '*')
			|| (string.charAt(i) == '+')
			|| (string.charAt(i) == '/')) {
			theOperator = string.charAt(i);
			nfields = 2;
			// Set to a space so that we can parse later...
			string = string.substring(0,i) + " " + string.substring((i + 1));
			if (Message.isDebugOn) {
				Message.printDebug(10, routine, "String=\"" + string + "\"");
			}
		}
	}

	String area = "";
	String precip = "";
	if (nfields == 1) {
		// Just use the original string but set the precip to one so that we can print it if we want...
		setAreaString(string0);
		setPrecipString("1");
		setWaterString(string0);
		return true;
	}
	else if (nfields == 2) {
		// Assume that we have a valid theOperator and do the math...
		List v = StringUtil.breakStringList(string, " \t", 0);
		area = (String)v.get(0);
		precip = (String)v.get(1);
		double a = (new Double(area)).doubleValue();
		double p = (new Double(precip)).doubleValue();
		double water = 0;
		if (theOperator == '*') {
			water = a*p;
		}
		else if ((theOperator == '/') || (theOperator == '+') || (theOperator == '-')) {
			Message.printWarning(1, routine,
				"Operator " + theOperator + " not allowed - use * for proration");
			return false;
			//water = a/p;
			//water = a + p;
			//water = a - p;
		}
		else {	
			Message.printWarning(1, routine, "String \"" + string0 + "\" cannot be converted to proration");
			return false;
		}
		// Now save values in the node in both string and floating point form...
		setAreaString(area);
		setPrecipString(precip);
		setWaterString(StringUtil.formatString(water, "%12.2f"));
		setArea((new Double(area)).doubleValue());
		setPrecip((new Double(precip)).doubleValue());
		setWater(water);
		if (getWater() > 0.0) {
			// This is a natural flow node...
			setIsNaturalFlow(true);
		}
		return true;
	}

	Message.printWarning(1, routine, "String \"" + string0 + "\" cannot be converted to proration");
	return false;

	}
	catch (Exception e) {
		Message.printWarning(1, routine, "String \"" + string0 + "\" cannot be converted to proration");
		Message.printWarning(2, routine, e);
		return false;
	}
}

/**
Removes a node from the upstream of this node.  Used by the network drawing code.
@param pos the position in the __upstream list of the node to be removed.
*/
public void removeUpstreamNode(int pos) {	
	__upstream.remove(pos);
}

/**
Replaces one of this node's upstream nodes with another node.  Used by the network drawing code.
@param node the node to replace the upstream node with.
@param pos the position in the __upstream list of the node to be replaced.
*/
public void replaceUpstreamNode(HydrologyNode node, int pos) {
	__upstream.set(pos,node);
}

/**
Reset some internal node things when reusing a node (for use in the network editor layout).
This seems to be used only by the legend drawing, but is retained for utility.
The following resets are done:
<ol>
<li>verbose node type (string) is looked up from the node type</li>
<li>symbol is set to the default for the type, considering the natural flow and import flags</li>
<li>secondary symbol is set to null</li>
<li>boundscCalculated is set to false</li>
</ol>
@param type the new type to assign the node
@param isNaturalFlow indicates whether the node is a baseflow node
@param isImport indicates whether the node is an import node
*/
public void resetNode(int type, boolean isNaturalFlow, boolean isImport ) {
	__type = type;
	__boundsCalculated = false;
	__isNaturalFlow = isNaturalFlow;
	__isImport = isImport;
	__nodeType = getTypeString(type, FULL);
	__secondarySymbol = null;
	setSymbolFromNodeType ( type, false );
}

/**
Sets the makenet area.
@param area value to put in the makenet area.
*/
public void setArea(double area) {
	__area = area;
}

/**
Sets the makenet area as a String.
@param areaString value to put into the makenet area String.
*/
public void setAreaString(String areaString) {
	if (areaString != null) {
		__areaString = areaString;
	}
}

/**
Sets the object associated with this node.
@param o the object associated with the node.
*/
public void setAssociatedObject(Object o) {
	__associatedObject = o;
}

/**
Sets whether the bounds have been calculated or not.  If set to false, then
the next time the node is drawn it will recalculate all its bounds.
@param calculated whether the drawing bounds have been calculated or not.
*/
public void setBoundsCalculated(boolean calculated) {
	__boundsCalculated = calculated;
}

/**
Sets the call information to be displayed by the node label.
@param call the information to display.
*/
public void setCall(String call) {
	__call = call;
}

/**
Sets the node's common id.
@param commonid the value to put into the node's common id.
*/
public void setCommonID(String commonid) {
	if (commonid != null) {
		__commonID = commonid;
	}
}

/**
Sets the computational order of nodes.
@param computationalOrder the value to set the computation order to.
*/
public void setComputationalOrder(int computationalOrder) {
	__computationalOrder = computationalOrder;
}

/**
Sets the diameter of the node in data units (for use in determing what points are contained).
@param diam the diam of the node in data units.
*/
public void setDataDiameter(double diam) {
	__width = diam;
	__height = diam;
}

/**
Sets the UTM X value stored in the database.
@param x the x value to set.
*/
public void setDBX(double x) {
	__dbX = x;
}

/**
Sets the UTM Y value stored in the database.
@param y the y value to set.
*/
public void setDBY(double y) {
	__dbY = y;
}

/**
Sets the decorator diameter.  The decorator icon diameter will be computed accordingly.
@param decoratorDiameter size of the decorator diameter in pixels.  
*/
public void setDecoratorDiameter(int decoratorDiameter)
{
	__decoratorDiameter = decoratorDiameter;
}

/**
Sets the delivery flow.
@param deliveryFlow value to put in the delivery flow.
*/
public void setDeliveryFlow(double deliveryFlow) {
	__deliveryFlow = deliveryFlow;
}

/**
Sets the description based on the node type.
*/
public void setDescription() {
	setDescription(getTypeString(__type,1));
}

/**
Sets the node description.
@param desc the node description to store in the node.
*/
public void setDescription(String desc) {
	if (desc != null) {
		__desc = desc;
	}
}

/**
Sets the node immediately downstream from this node.
@param downstream the node immediately downstream from this node.
*/
public void setDownstreamNode(HydrologyNode downstream) {
	__downstream = downstream;
}

/**
Sets the id of the node downstream from this node.  Used in the network drawing code.
@param id the id of the node downstream from this node.
*/
public void setDownstreamNodeID(String id) {
	__downstreamNodeID = id;
}

/**
Sets whether text labels should be drawn along with the node in the network editor.
@param drawText whether to draw text labels.
*/
public static void setDrawText(boolean drawText) {
	__drawText = drawText;
}

/**
Sets the icon diameter.  The decorator icon diameter will be computed accordingly.
@param iconDiameter size of the icon diameter in drawing units (points for printing, pixels for screen).  
*/
public void setIconDiameter ( int iconDiameter )
{
	__iconDiameter = iconDiameter;
	int third = __iconDiameter / 3;
	if ((third % 2) == 1) {
		third++;
	}
	setDecoratorDiameter ( third );
}

/**
Sets the node drawing code identifier for the node.
@param identifier the identifier to set.
*/
public void setIdentifier(String identifier) {
	__identifier = identifier;
}

/**
Set whether the node is being drawn in the WIS network display.
@param inWis whether the node is being drawn in the WIS network display.
*/
/*
public void setInWis(boolean inWis) {
	__inWis = inWis;
}
*/
//FIXME SAM 2008-03-15 Need to remove WIS from this general class

/*
public void setInWIS(boolean inWis) {
	__inWis = inWis;
}
*/
//FIXME SAM 2008-03-15 Need to remove WIS from this general class

/**
Sets whether this node is a dry river or not.  WIS-specific.
@param isDryRiver whether this node is a dry river or not.
*/
public void setIsDryRiver(boolean isDryRiver) {
	__isDryRiver = isDryRiver;
}

/**
Sets whether this node is an import node or not.
@param isImport whether this node is an import node or not.
*/
public void setIsImport(boolean isImport) {
	__isImport = isImport;
}

/**
Sets whether this node is a natural flow node or not.
@param isNaturalFlow whether this node is a natural flow node or not.
*/
public void setIsNaturalFlow(boolean isNaturalflow) {
	__isNaturalFlow = isNaturalflow;
}

/**
Sets the node label.
@param label the label to set.
*/
public void setLabel(String label) {
	__label = label;
}

/**
Sets the angle to print the label at.  Makenet-specific.
@param labelAngle the angle to print the label at.
*/
public void setLabelAngle(double labelAngle) {
	__labelAngle = labelAngle;
}

/**
Sets the direction to print the label in.  Makenet-specific.
@param labelDir the direction to print the label in.
*/
public void setLabelDirection(int labelDir) {
	__labelDir = labelDir;
}

/**
Sets the link data for confluences.
@param link the value to set the link data to.
*/
public void setLink(long link) {
	__link = link;
}

/**
Sets the natural flow.
@param naturalFlow value to put into the natural flow.
*/
public void setNaturalFlow(double naturalFlow) {
	__naturalFlow = naturalFlow;
}

/**
Sets the point flow.
@param pointFlow value to put in point flow.
*/
public void setPointFlow(double pointFlow) {
	__pointFlow = pointFlow;
}

/**
Sets the ID from the net file.  Makenet-specific.
@param netid the value to set the net file ID to.
*/
public void setNetID(String netid) {
	if (netid != null) {
		__netID = netid;
	}
}

/**
Sets the node number in the reach.
@param nodeInReachNum the node number in the reach.
*/
public void setNodeInReachNumber(int nodeInReachNum) {
	__nodeInReachNum = nodeInReachNum;
}

/**
Sets the type of node this is.
@param nodeType the type of node this is.
*/
public void setNodeType(String nodeType) {
	__nodeType = nodeType;
}

/**
Sets this node's position on the network diagram.
@param x the x location.
@param y the y location.
@param width the width of the node.
@param height the height of the node.
*/
public void setPosition(double x, double y, double width, double height) {
	__x = x;
	__y = y;
	__width = width;
	__height = height;
}

/**
Sets the precip.  Makenet-specific.
@param precip the precip to set.
*/
public void setPrecip(double precip) {	
	__precip = precip;
}

/**
Sets the precip as a String.  Makenet-specific.
@param precipString value to set the precip string to.
*/
public void setPrecipString(String precipString) {
	if (precipString != null) {
		__precipString = precipString;
	}
}

/**
Sets the gain proration factor.  Makenet-specific.
@param prorationFactor value to set the proration factor to.
*/
public void setProrationFactor(double prorationFactor) {
	__prorationFactor = prorationFactor;
}

/**
Sets the reach number.
@param reachCounter value to set the reach number to.
*/
public void setReachCounter(int reachCounter) {
	__reachCounter = reachCounter;
}

/**
Sets the reach level.
@param reachLevel value to set the reach level to.
*/
public void setReachLevel(int reachLevel) {
	__reachLevel = reachLevel;
}

/**
Sets whether this node's data was read from the database or not.
@param read whether this node's data was read from the database.
*/
public void setReadFromDB(boolean read) {
	__readFromDB = read;
}

/**
Sets the right to be show in the node label.
@param right the right information to show in the node label.
*/
public void setRight(String right) {
	__right = right;
}

/**
Sets the river node ID.
@param riverNodeID the value to set the river node ID to.
*/
public void setRiverNodeID(String riverNodeID) {
	if (riverNodeID != null) {
		__riverNodeID = riverNodeID;
	}
}

/**
Sets whether this node was selected on the network diagram.
@param selected whether this node was selected on the network diagram.
*/
public void setSelected(boolean selected) {
	__isSelected = selected;
}

/**
Sets the node count.
@param serial the value to set the node count to.
*/
public void setSerial(int serial) {
	__serial = serial;
}

/**
Sets whether to show call information.
@param showCalls whether to show call information.
*/
public void setShowCalls(boolean showCalls) {
	__showCalls = showCalls;
}

/**
Sets whether to show the delivery flow.
@param showDeliveryFlow whether to show the delivery flow.
*/
public void setShowDeliveryFlow(boolean showDeliveryFlow) {
	__showDeliveryFlow = showDeliveryFlow;
}

/**
Sets whether to show the natural flow.
@param showNaturalFlow whether to show the natural flow.
*/
public void setShowNaturalFlow(boolean showNaturalFlow) {
	__showNaturalFlow = showNaturalFlow;
}

/**
Sets whether to show the point flow.
@param showPointFlow whether to show the point flow.
*/
public void setShowPointFlow(boolean showPointFlow) {
	__showPointFlow = showPointFlow;
}

/**
Sets whether to show right information.
@param showRights whether to show right information.
*/
public void setShowRights(boolean showRights) {
	__showRights = showRights;
}

/**
Sets the structure stream mile.  WIS-specific.
@param streamMile value to set the stream mile to.
*/
public void setStreamMile(double streamMile) {
	__streamMile = streamMile;
}

/**
Sets the stream number.  WIS-specific.
@param streamNumber value to put into the stream number.
*/
public void setStreamNumber(long streamNumber) {
	__streamNum = streamNumber;
}

/**
Sets the symbol to use for the node.
@param symbol the GRSymbol to use.
*/
public void setSymbol(GRSymbol symbol) {
	__symbol = symbol;
}

/**
Set the node symbol given the node type.
@param nodeType the node type to be used to determine the symbol.
@param computeSize if true the size is computed.
*/
private void setSymbolFromNodeType ( int nodeType, boolean computeSize )
{	int symbolStyle = GRSymbol.TYPE_POLYGON;
	GRColor symbolColor = GRColor.black;
	int iconDiameter = getIconDiameter();
	if ( (nodeType == NODE_TYPE_CONFLUENCE) || (nodeType == NODE_TYPE_XCONFLUENCE) ) {
		__symbol = new GRSymbol(GRSymbol.SYM_FCIR, symbolStyle, symbolColor, symbolColor, iconDiameter/2, iconDiameter/2);
		__symText = null;
		if ( computeSize ) {
			__width /= 2;
			__height /= 2;
		}
	}
	else if ( nodeType == NODE_TYPE_DIV ) {
		__symbol = new GRSymbol(GRSymbol.SYM_CIR, symbolStyle, symbolColor, symbolColor, iconDiameter, iconDiameter);
		__symText = "D";
	}
	else if ( nodeType == NODE_TYPE_DIV_AND_WELL ) {
		__symbol = new GRSymbol(GRSymbol.SYM_CIR, symbolStyle, symbolColor, symbolColor, iconDiameter, iconDiameter);
		__symText = "DW";
	}
	else if ( nodeType == NODE_TYPE_END ) {
		__symbol = new GRSymbol (GRSymbol.SYM_EX, symbolStyle, symbolColor, symbolColor, iconDiameter, iconDiameter);
		__secondarySymbol = new GRSymbol(GRSymbol.SYM_CIR, symbolStyle, symbolColor, symbolColor, iconDiameter, iconDiameter);
		__symText = null;
	}
	else if ( nodeType == NODE_TYPE_FLOW ) {
		__symbol = new GRSymbol(GRSymbol.SYM_FCIR, symbolStyle, symbolColor, symbolColor, iconDiameter, iconDiameter);
		__symText = null;
	}
	else if ( nodeType == NODE_TYPE_ISF ) {
		__symbol = new GRSymbol(GRSymbol.SYM_CIR, symbolStyle, symbolColor, symbolColor, iconDiameter, iconDiameter);
		//__symText = "I";
		__symText = "M";
	}
	else if ( nodeType == NODE_TYPE_LABEL ) {
		__symbol = null;
		__symText = null;
	}
	else if ( nodeType == NODE_TYPE_OTHER ) {
		__symbol = new GRSymbol(GRSymbol.SYM_CIR, symbolStyle, symbolColor, symbolColor, iconDiameter, iconDiameter);
		__symText = "O";
	}
	else if ( nodeType == NODE_TYPE_PLAN ) {
		__symbol = new GRSymbol(GRSymbol.SYM_FCIR, symbolStyle, GRColor.lightGray, symbolColor, iconDiameter, iconDiameter);
		__symText = "PL";
	}
	else if ( nodeType == NODE_TYPE_RES ) {
		__symbol = new GRSymbol(GRSymbol.SYM_FRTRI, symbolStyle, symbolColor, symbolColor, iconDiameter, iconDiameter);
		__symText = null;
	}
	else if ( nodeType == NODE_TYPE_WELL ) {
		__symbol = new GRSymbol(GRSymbol.SYM_CIR, symbolStyle, symbolColor, symbolColor, iconDiameter, iconDiameter);
		__symText = "W";
	}
	else {
		Message.printWarning(2, "", "Unknown symbol for node type " + nodeType);
		__symbol = null;
		__symText = null;
	}	
}

/**
Sets the tributary number.
@param tributaryNumber value to put into tributary number.
*/
public void setTributaryNumber(int tributaryNumber) {
	__tributaryNum = tributaryNumber;
}

/**
Sets the node type.
@param type value to set the node type to.
*/
public void setType(int type) {
	__type = type;
}

/**
Sets the node type based on the abbreviated String type.  The abbreviated
string is used in the StateMod station names and the network diagram.
@param type the abbreviated string specifying what type of node this node is.
*/
public void setTypeAbbreviation(String type) {
	if (type.equalsIgnoreCase("BLK")) {
		setType(NODE_TYPE_BLANK);
	}
	else if (type.equalsIgnoreCase("DIV")) {
		setType(NODE_TYPE_DIV);
	}
	else if (type.equalsIgnoreCase("D&W")
		|| type.equalsIgnoreCase("DW")) {
		setType(NODE_TYPE_DIV_AND_WELL);
	}
	else if (type.equalsIgnoreCase("WEL")) {
		setType(NODE_TYPE_WELL);
	}
	else if (type.equalsIgnoreCase("FLO")) {
		setType(NODE_TYPE_FLOW);
	}
	else if (type.equalsIgnoreCase("CON")) {
		setType(NODE_TYPE_CONFLUENCE);
	}
	else if (type.equalsIgnoreCase("ISF")) {
		setType(NODE_TYPE_ISF);
	}
	else if (type.equalsIgnoreCase("RES")) {
		setType(NODE_TYPE_RES);
	}
	else if (type.equalsIgnoreCase("IMP")) {
		setType(NODE_TYPE_IMPORT);
	}
	else if (type.equalsIgnoreCase("BFL")) {
		setType(NODE_TYPE_BASEFLOW);
	}
	else if (type.equalsIgnoreCase("END")) {
		setType(NODE_TYPE_END);
	}
	else if (type.equalsIgnoreCase("OTH")) {
		setType(NODE_TYPE_OTHER);
	}
	else if (type.equalsIgnoreCase("UNK")) {
		setType(NODE_TYPE_UNKNOWN);
	}
	else if (type.equalsIgnoreCase("STR")) {
		setType(NODE_TYPE_STREAM);
	}
	else if (type.equalsIgnoreCase("LAB")) {
		setType(NODE_TYPE_LABEL);
	}
	else if (type.equalsIgnoreCase("FOR")) {
		setType(NODE_TYPE_FORMULA);
	}
	else if (type.equalsIgnoreCase("XCN")) {
		setType(NODE_TYPE_XCONFLUENCE);
	}
	else if (type.equalsIgnoreCase("LBN")) {
		setType(NODE_TYPE_LABEL_NODE);
	}
	else if (type.equalsIgnoreCase("PLN")) {
		setType(NODE_TYPE_PLAN);
	}
}

/**
Sets the list of nodes upstream of this node.  Used by the network diagramming tools.
@param v the list of upstream nodes.  If null, then there are no nodes upstream of this node.
*/
public void setUpstreamNodes(List<HydrologyNode> v)
{
	if ( v == null ) {
		__upstream = new Vector();
	}
	else {
		__upstream = v;
	}
}

/**
Sets the user description.  Makenet-specific.
@param desc value to put in the user description.
*/
public void setUserDescription(String desc) {
	if (desc == null) {
		__userDesc = "";
	}
	else {	
		__userDesc = desc;
	}
}

/**
Set the type from a string.  This is used in CWRAT WIS.
@param type the type to set.
*/
public void setType(String type) {
	if (type.equalsIgnoreCase("diversion")) {
		setType(NODE_TYPE_DIV);
	}
	else if (type.equalsIgnoreCase("well")) {
		setType(NODE_TYPE_WELL);
	}
	else if (type.equalsIgnoreCase("confluence")) {
		setType(NODE_TYPE_CONFLUENCE);
	}
	else if (type.equalsIgnoreCase("formula")) {
		setType(NODE_TYPE_FORMULA);
	}
	else if (type.equalsIgnoreCase("minflow")) {
		setType(NODE_TYPE_ISF);
	}
	else if (type.equalsIgnoreCase("other")) {
		setType(NODE_TYPE_OTHER);
	}
	else if (type.equalsIgnoreCase("reservoir")) {
		setType(NODE_TYPE_RES);
	}
	else if (type.equalsIgnoreCase("station")) {
		setType(NODE_TYPE_FLOW);
	}
	else if (type.equalsIgnoreCase("stream")) {
		setType(NODE_TYPE_STREAM);
	}
	else if (type.equalsIgnoreCase("string")) {
		setType(NODE_TYPE_LABEL);
	}
	else if (type.equalsIgnoreCase("labelNode")) {
		setType(NODE_TYPE_LABEL_NODE);
	}
	else if (type.equalsIgnoreCase("Plan")) {
		setType(NODE_TYPE_PLAN);
	}
}

/**
Sets how upstream nodes are constructed.  See TRIBS_*.
@param upstreamOrder how upstream nodes are constructed.
*/
public void setUpstreamOrder(int upstreamOrder) {
	__upstreamOrder = upstreamOrder;
}

/**
Returns the verbose node type description string.
@param type the type of node for which to return the verbose type.
@return the verbose node type description string.
*/
public void setVerboseType(String stype) {
	// Abbreviations...
	if (stype.equalsIgnoreCase("Blank")) {
		__type = NODE_TYPE_BLANK;
	}
	else if (stype.equalsIgnoreCase("Diversion")) {
		__type = NODE_TYPE_DIV;
	}
	else if (stype.equalsIgnoreCase("Diversion and Well")) {
		__type = NODE_TYPE_DIV_AND_WELL;
	}
	else if (stype.equalsIgnoreCase("Well")) {
		__type = NODE_TYPE_WELL;
	}

// The following is done because flow types have different names in WIS
// and non-WIS screens.  Unfortunately, this causes a lot of problems.  
// A far far better solution would be to rename one of the flow types, 
// e.g. for WIS make a NODE_TYPE_STATION node that uses "Station" and leave
// NODE_TYPE_FLOW for "Streamflow".  There's lots of work to be done in 
// revising these classes, though.  It'll have to wait.
	
	// FIXME SAM 2008-03-15 Need to remove WIS from this general class
	else if (//!__inWis &&
			stype.equalsIgnoreCase("Streamflow")) {
		__type = NODE_TYPE_FLOW;
	}
	// FIXME SAM 2008-03-15 Need to remove WIS from this general class
	else if (//__inWis && 
			stype.equalsIgnoreCase("Station")) {
		__type = NODE_TYPE_FLOW;
	}
	else if (stype.equalsIgnoreCase("Confluence")) {
		__type = NODE_TYPE_CONFLUENCE;
	}
	else if (stype.equalsIgnoreCase("Instream Flow")) {
		__type = NODE_TYPE_ISF;
	}
	else if (stype.equalsIgnoreCase("Reservoir")) {
		__type = NODE_TYPE_RES;
	}
	else if (stype.equalsIgnoreCase("Import")) {
		__type = NODE_TYPE_IMPORT;
	}
	else if (stype.equalsIgnoreCase("Baseflow")) {
		__type = NODE_TYPE_BASEFLOW;
	}
	else if (stype.equalsIgnoreCase("End")) {
		__type = NODE_TYPE_END;
	}
	else if (stype.equalsIgnoreCase("Other")) {
		__type = NODE_TYPE_OTHER;
	}
	else if (stype.equalsIgnoreCase("Uknown")) {
		__type = NODE_TYPE_UNKNOWN;
	}
	else if (stype.equalsIgnoreCase("Stream")) {
		__type = NODE_TYPE_STREAM;
	}
	else if (stype.equalsIgnoreCase("Label")) {
		__type = NODE_TYPE_LABEL;
	}
	else if (stype.equalsIgnoreCase("Formula")) {
		__type = NODE_TYPE_FORMULA;
	}
	else if (stype.equalsIgnoreCase("XConfluence")) {
		__type = NODE_TYPE_XCONFLUENCE;
	}
	else if (stype.equalsIgnoreCase("LabelNode")) {
		__type = NODE_TYPE_LABEL_NODE;
	}
	else if (stype.equalsIgnoreCase("Plan")) {
		__type = NODE_TYPE_PLAN;
	}
	else {
		Message.printWarning(2, "setVerboseType", "Unknown type: '" + stype + "'");
		__type = NODE_TYPE_BLANK;
	}
}

/**
Sets whether this node is visible or not.
@param visible whether this node is visible or not.
*/
public void setVisible(boolean visible) {
	__visible = visible;
}

/**
Sets area * precip.  Makenet-specific.
@param water the value to put in area * precip.
*/
public void setWater(double water) {
	__water = water;
}

/**
Sets area * precip as a String.  Makenet-specific.
@param waterString value of area * precip as a String.
*/
public void setWaterString(String waterString) {
	if (waterString != null) {
		__waterString = waterString;
	}
}

/**
Sets the wis format associated with this node.
@param wisFormat the wis format associated with this node.
*/
/*
public void setWISFormat(HydroBase_WISFormat wisFormat) {
	__wisFormat = wisFormat;
}
*/
//

/**
Sets the number of the wis sheet this node is associated with.
@param wis_num the number of the wis sheet this node is associated with.
*/
/*
public void setWis_num(int wis_num) {
	__wisNum = wis_num;
}
*/
//FIXME SAM 2008-03-15 Need to remove WIS from this general class

/**
Sets the node X coordinate.
@param x value to put into X
*/
public void setX(double x) {
	__x = x;
}

/**
Sets the node Y coordinate.
@param y value to put into Y.
*/
public void setY(double y) {
	__y = y;
}

/**
Returns whether calls should be shown.
@return whether calls should be shown.
*/
public boolean showCalls() {
	return __showCalls;
}

/**
Returns whether the delivery flow should be shown.
@return whether the delivery flow should be shown.
*/
public boolean showDeliveryFlow() {
	return __showDeliveryFlow;
}

/**
Returns whether the natural flow should be shown.
@return whether the natural flow should be shown.
*/
public boolean showNaturalFlow() {
	return __showNaturalFlow;
}

/**
Returns whether the point flow should be shown.
@return whether the point flow should be shown.
*/
public boolean showPointFlow() {
	return __showPointFlow;
}

/**
Returns whether to show the rights.
@return whether the rights should be shown.
*/
public boolean showRights() {
	return __showRights;
}

/**
Returns a String representation of the object suitable for debugging a network.
@return a String representation of the object suitable for debugging a network.
*/
public String toNetworkDebugString() {
	String down = "";
	String up = "";
	
	if (__downstream != null) {
		down = __downstream.getCommonID();
	}
	else {	
		down = "null";
	}
	
	if (__upstream != null) {
		for (int i = 0; i < getNumUpstreamNodes(); i++) {
			up = up + " [" + i + "]:\"" + getUpstreamNode(i).getCommonID() + "\"";
		}
	}
	else {	
		up = "null";
	}

	return "[" + __label + "]  US: '" + up + "'   DS: '" + down + "'" + "  RL: " + __reachLevel;
}

/**
Returns information about the node in a format useful for debugging node layouts in the network editor.
@return information about the node in a format useful for debugging node layouts in the network editor.
*/
public String toNetworkString() {
	return 
		"Identifier: '" + __identifier + "'\n" +
		"Node type:  '" + __nodeType   + "'\n" +
		"X:           " + __x 		+ "\n" + 
		"DBX:         " + __dbX		+ "\n" + 
		"Y:           " + __y		+ "\n" +
		"DBY:         " + __dbY		+ "\n" + 
		"Description:'" + __desc        + "'\n" +
		"User desc:  '" + __userDesc    + "'\n" +
		"CommonID:   '" + __commonID    + "'\n" +
		"IsVisible:   " + __visible 	+ "\n";
}

/**
Returns a verbose String representation of the object.
@return a verbose String representation of the object.
*/
public String toString() {
	String down = "";
	String up = "";
	
	if (__downstream != null) {
		down = __downstream.getCommonID();
	}
	else {	
		down = "null";
	}
	
	if (__upstream != null) {
		for (int i = 0; i < getNumUpstreamNodes(); i++) {
			up = up + " [" + i + "]:\"" + getUpstreamNode(i).getCommonID() + "\"";
		}
	}
	else {	
		up = "null";
	}

	return "\"" + getCommonID() + "\" T=" + getTypeString(__type,1) 
		+ " T#=" + __tributaryNum + " RC=" + __reachCounter + " RL="
		+ __reachLevel + " #=" + __serial + " #inR=" 
		+ __nodeInReachNum + " CO=" + __computationalOrder 
		+ " DWN=\"" + down + "\" #up=" + getNumUpstreamNodes() + " UP=" 
		+ up;
}

/**
Returns a String representation of the DB table data from the node.
@return a String representation of the DB table data from the node.
*/
public String toTableString() {
	return 
		"Wis_num:     " + __wisNum 	+ "\n" + 
		"Identifier: '" + __identifier 	+ "'\n" + 
		"Node type:  '" + __nodeType 	+ "'\n" + 
		"X:           " + __x 		+ "\n" + 
		"DBX:         " + __dbX		+ "\n" + 
		"Y:           " + __y		+ "\n" +
		"DBY:         " + __dbY		+ "\n" + 
		"Label:      '" + __label;
}

/**
Writes the node out to the given PrintWriter as XML.
@param out the PrintWrite to write the node out to.
@param verbose if true, then all the information about the node will be written out.
*/
public String writeNodeXML(PrintWriter out, boolean verbose) {
	String xml = "    <Node ";
	String n = System.getProperty("line.separator");	

	String id = getCommonID();
	id = StringUtil.replaceString(id, "&", "&amp;");
	id = StringUtil.replaceString(id, "<", "&lt;");
	id = StringUtil.replaceString(id, ">", "&gt;");	

	xml += "ID = \""  + id + "\"" + n;
	xml += "         AlternateX = \"" + __dbX + "\"" + n;
	xml += "         AlternateY = \"" + __dbY + "\"" + n;
	String desc = __desc;
	desc = StringUtil.replaceString(desc, "&", "&amp;");
	desc = StringUtil.replaceString(desc, "<", "&lt;");
	desc = StringUtil.replaceString(desc, ">", "&gt;");
	xml += "         Description = \"" + desc + "\"" + n;
//	xml += "         Identifier = \"" + __identifier + "\"" + n;
	// FIXME SAM 2008-12-10 Need to convert to IsNaturalFlow when old version of StateDMI can
	// be phased out.  For now write both with the same value so that the network will work with
	// old and new software.
	xml += "         IsBaseflow = \"" + __isNaturalFlow + "\"" + n;
	xml += "         IsNaturalFlow = \"" + __isNaturalFlow + "\"" + n;
	xml += "         IsImport = \"" + __isImport + "\"" + n;
//	xml += "         LabelAngle = \"" + __labelAngle + "\"" + n;

	if (__isNaturalFlow) {
		xml += "         Area = \"" + __area + "\"" + n;
		xml += "         Precipitation = \"" + __precip + "\"" + n;
	}

	String sdir = null;
	int dir = getLabelDirection();
	dir = dir % 10;
	
	if (dir == 1) {
		sdir = "AboveCenter";
	}
	else if (dir == 7) {
		sdir = "UpperRight";
	}
	else if (dir == 4) {
		sdir = "Right";
	}
	else if (dir == 8) {
		sdir = "LowerRight";
	}
	else if (dir == 2) {
		sdir = "BelowCenter";
	}
	else if (dir == 5) {
		sdir = "LowerLeft";
	}
	else if (dir == 3) {
		sdir = "Left";
	}
	else if (dir == 6) {
		sdir = "UpperLeft";
	}
	else if (dir == 9) {
		sdir = "Center";
	}
	else {
		sdir = "BelowCenter";
	}

	if (sdir != null) {
		xml += "         LabelPosition = \"" + sdir + "\"" + n;
	}

	if (getType() == NODE_TYPE_RES) {
		sdir = null;
		dir = getLabelDirection();
		dir = dir / 10;
		if (dir == 2) {
			sdir = "Up";
		}
		else if (dir == 1) {
			sdir = "Down";
		}
		else if (dir == 4) {
			sdir = "Left";
		}
		else if (dir == 3) {
			sdir = "Right";
		}
		else {
			sdir = "Left";
		}
		if (sdir != null) {
			xml += "         ReservoirDir = \"" + sdir + "\"" + n;
		}
	}
	
//	String type = getTypeString(__type, 1);
	String type = getVerboseType(__type);
//	if (type.equalsIgnoreCase("D&W")) {
//		type = "DW";
//	}
	xml += "         Type = \"" + type + "\"" + n;

	if (verbose) {
		xml += "         ComputationalOrder = \"" + __computationalOrder
			+"\"" + n;
		xml += "         NodeInReachNum = \"" + __nodeInReachNum + "\""
			+ "" + n;
		xml += "        ReachCounter = \"" + __reachCounter + "\"" + n;
		xml += "         Serial = \"" + __serial + "\"" + n;
		xml += "         TributaryNum = \"" + __tributaryNum + "\"" + n;
		xml += "         UpstreamOrder = \"" + __upstreamOrder + "\"" 
			+ n;
	}

	xml += "         X = \"" + StringUtil.formatString(__x, "%13.6f").trim()
		+ "\"" + n;
	xml += "         Y = \"" + StringUtil.formatString(__y, "%13.6f").trim()
		+ "\">" + n;

	if (__downstream != null) {
		String down = __downstream.getCommonID();
		down = StringUtil.replaceString(down, "&", "&amp;");
		down = StringUtil.replaceString(down, "<", "&lt;");
		down = StringUtil.replaceString(down, ">", "&gt;");
		xml += "        <DownstreamNode ID = \"" + down + "\"/>" + n;
	}

	String up = "";
	int num = getNumUpstreamNodes();
	if (__upstream != null) {
		for (int i = 0; i < num; i++) {
			up = getUpstreamNode(i).getCommonID();
			up = StringUtil.replaceString(up, "&", "&amp;");
			up = StringUtil.replaceString(up, "<", "&lt;");
			up = StringUtil.replaceString(up, ">", "&gt;");
			xml += "        <UpstreamNode ID = \"" + up + "\"/>" + n;
		}
	}
	xml += "    </Node>" + n;

	if (out != null) {
		out.print(xml + "" + n);
		return null;
	}
	else {
		return xml;
	}
}

}