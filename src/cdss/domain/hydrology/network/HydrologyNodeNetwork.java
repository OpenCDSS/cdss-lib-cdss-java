// ----------------------------------------------------------------------------
// HydroBase_NodeNetwork - a representation of a stream node network
// ----------------------------------------------------------------------------

/* NoticeStart

CDSS Java Library
CDSS Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

CDSS Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    CDSS Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with CDSS Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

// Notes:	(1)	This code is meant to support (at least) three
//			CRDSS applications:
//			  makenet - used by planning model to generate network
//			  StateMod GUI - to allow interactive network edits
//			  Admin tool - to allow call analysis, water balance,
//					etc.
//		(2)	Right now, this class has member functions to read and
//			write different network formats.  However, it may make
//			sense to subclass to have, for example, a
//			StateModNodeNetwork and a CWRATNodeNetwork.
//		(3)	ALL PLOTTING CODE IS AT THE BOTTOM OF THE CLASS TO KEEP
//			CODE SEPARATED.
// ----------------------------------------------------------------------------
// History:
//
// 27 Oct 1997	Steven A. Malers,	Initial version - from makenet.
//		Riverside Technology,
//		inc.
// 12 Dec 1997	SAM, RTi		Make so that for the admin tool
//					confluence nodes are treated as
//					baseflow nodes.
// 28 Feb 1998	SAM, RTi		Finish adding routines to support
//					makenet - these use the SMUtil
//					package.
// 27 Aug 1998	SAM, RTi		Fix problem where makenet 5.01 was not
//					printing most downstream node in the
//					river network file.
// 11 Sep 1998	SAM, RTi		Add network PostScript plotting
//					capability for makenet.  Add the check
//					file to be used with makenet (ugly, but
//					preserves legacy output).
// 21 Oct 1998	SAM, RTi		Add check to makenet network code to
//					catch if a symbol direction has not been
//					set.
// 16 Dec 1998	SAM, RTi		Add printCheck to support DMIs.
// 11 Jan 1999  Daniel Weiler, RTi	Add getStationsInNetwork, 
//					getStructuresInNetwork and 
//					setNodeDescriptions.
// 19 Jan 1999	DKW, RTi		A few more modifications to speed up
//					the setting of fancy descriptions.
// 18 Feb 1999	SAM, RTi		Start Java 1.2 upgrades.  This includes
//					more exception handling in some classes.
// 19 Feb 1999	DKW, RTi		Added Wells to network. Cleaned up
//					some WDID issues re: padding with 0
//					in situations when either wd or id
//					are less than 2 and 4/5 respectively.
// 31 Mar 1999	SAM, RTi		Code sweep.
// 07 Apr 1999	SAM, RTi		Add dry river feature to nodes - read
//					from the wis data.
// 27 Jul 1999	SAM, RTi		Add features to handle disconnected
//					streams in the Rio Grande basin using
//					the XCONFL node type.  Trim input lines
//					in makenet input - this allows comments
//					to be indented.  Break out drawNetwork()
//					code so a network GUI can be tested.
//					When creating the .ord file, also
//					create text file(s)with lists of
//					diversion structures, etc.  Rework some
//					of Dan's code like getStationsInNetwork
//					so that the stations are determined
//					in that method so we avoid a global
//					variable (traversing the list does not
//					take very long and if we build a GUI
//					we will need to get a list for the
//					in-memory nodes).  Finalize moving the
//					description set code until after the
//					network has been read.  This is clearly
//					faster based on early tests.
// 20 Sep 1999	SAM, RTi		Add database version comment to output
//					file headers.
// 18 Oct 1999	SAM, RTi		Add X, Y to river order file so the
//					plot positions can be debugged.  This
//					can also be used in the future to
//					display latitude and longitude.  Fixed
//					bug introduced with XCONFL where
//					confluence nodes were not plotting
//					corectly.
// 02 Nov 1999	SAM, RTi		Handle comments anywhere in makenet net
//					file(done previously)but put in check
//					to handle special case of _# being used
//					in some identifiers in data sets.  For
//					this case, put in a warning.
// 08 Nov 1999	SAM, RTi		Change from "Streamflow" to "Streamflow
//					Gage" in the makenet plot.  Change the
//					nodelist.txt file to have header
//					"makenet_id" instead of "id"(simplifies
//					the GIS link).  Add a dashed line where
//					there are disappearing streams.  Add
//					labeling using name.
// 29 Nov 1999	CEN, RTi		Added __autoLabel.
// 06 Dec 1999	CEN, RTi		Added __plotCommands.
// 06 Dec 1999	SAM, RTi		Add D&W node type and add initial well
//					code to get information from well
//					permits.
// 07 Dec 1999	CEN, RTi		Added drawCarrier
// 15 Mar 2000	SAM, RTi		Finalize handling of WELL and D&W nodes
//					for makenet release.
// 17 Apr 2000	SAM, RTi		When getting struct_to_well data,
//					specify the query type for wells for
//					WEL nodes(D&W information will come
//					from structures).
// 30 May 2000	SAM, RTi		Add node type to nodelist file so that
//					it can be used for queries when joined
//					to shapefiles, etc.  Don't print blank
//					or confluence nodes to node list file.
// 06 Feb 2001	SAM, RTi		Update for makenet to allow daily data
//					printout as an option.  Change IO to
//					IOUtil.  Update to reflect changes in
//					SMRiverInfo.  Add __setrinData and
//					__setrisData for resets from makenet.
//					Update so when ris file is output files
//					are also output with a list of potential
//					streamflow time series.
// 08 Jun 2001	SAM, RTi		Add header information to
//					createIndentedRiverNetworkStrings().
//					Change Timer to StopWatch.
// 2002-03-07	SAM, RTi		Update for changes in the GR package.
// 2002-06-16	SAM, RTi		Add readHydroBaseStreamNetwork().
// ----------------------------------------------------------------------------
// 2003-10-08	J. Thomas Sapienza, RTi	Updated to HydroBaseDMI;
// 2003-10-21	JTS, RTi		Updated readWISFormatNetwork().
// 2004-03-15	JTS, RTi		* Uncommented readMakenetNetworkFile()
// 					* Uncommented readMakenetLineTokens()
// 					* Uncommented processMakenetNodes()
// 					* Uncommented setNodeDescriptions()
// 					* Uncommented 
//					  createIndentedRiverNetworkFile()
// 					* Uncommented 
//					  createIndentedRiverNetworkStrings()
// 					* Uncommented 
//					  createRiverBaseflowDataFile()
// 					* Uncommented createRiverOrderFile()
// 					* Uncommented printOrdNode()
// 2004-03-23	JTS, RTi		* Wrapped all __dmi calls with 
//					  __isDatabaseUp checks.
//					* Check for __isDatabaseUp now makes
//					  sure the database is connected.
//					* Removed old drawing code.
// 2004-04	JTS, RTi		Added a few new methods in reaction
//					to needs discovered while working on
//					the network plotting tool:
//					* addNode().
//					* checkUniqueID().
//					* deleteNode().
// 2004-07-02	JTS, RTi		Revised many methods.
// 2004-07-07	JTS, RTi		Revised the XML writing code, in 
//					particular newlines, tabstops, and
//					page layouts.
// 2004-07-11	SAM, RTi		In createStateModRiverNetwork(), handle
//					confluence nodes and other non-physical
//					node types.
// 2004-08-15	SAM, RTi		* Remove createRiverBaseflowDataFile() -
//					  functionality is in StateDMI.
//					* Remove
//					  createRiverStationBaseflowFile() -
//					  functionality is in StateDMI.
//					* Remove createRiverNetworkFile() -
//					  functionality is in StateDMI.
// 2004-08-17	JTS, RTi		* deleteNode() was not calculating the
//					  tributary number properly for the 
//					  nodes upstream of a deleted node.
//					* addNode() was not calculating the 
//					  serial number for a new node properly.
// 2004-08-25	JTS, RTi		For XML networks, the layouts in the 
//					XML file are now read in, processed,
//					and stored in the network for retrieval
//					when the network GUI is opened.
// 2004-10-11	SAM, RTi		Fix so that the call to
//					setNodeDescriptions() does not print
//					exceptions trying to get descriptions
//					for non-WDIDs, etc.  Normally these are
//					non-fatal messages that can be treated
//					as status messages.
// 2004-10-20	JTS, RTi		Changed the names of some variables to
//					represent the fact that the XML no
//					longer stores the width and height of
//					the network, but instead stores the 
//					lower-left and upper-right corner points
//					instead.
// 2004-11-11	JTS, RTi		Networks from XML files now have 
//					finalCheck() called on them to make sure
//					all their nodes are within bounds.
// 2004-11-15	JTS, RTi		Added findNextXConfluenceDownstreamNode.
// 2004-12-20	JTS, RTi		Changed how label positions are numbered
//					so that original Makenet networks 
//					display properly.
// 2005-01-14	JTS, RTi		writeXML(String) now calls the 
//					overloaded version and fills in more
//					parameters, so that it can be used
//					as a simple way to write out an 
//					existing XML network.
// 2005-02-16	JTS, RTi		Converted queries to use stored 
//					procedures.
// 2005-04-19	JTS, RTi		* Removed isFileXML().
//					* Added writeListFile().
// 2005-04-28	JTS, RTi		Added all data members to finalize().
// 2005-05-09	JTS, RTi		* Only HydroBase_StationView objects are
//					  returned from station queries now.
//					* Only HydroBase_WellApplicationView
//					  objects are returned from well app
//					  queries now.
// 2005-06-13	SAM, RTi		* Fix bug handling XCONFL nodes when
//					  creating the StateMod RIN.  If the
//					  downstream node is an XCONFL and has
//					  only one upstream node, print a
//					  blank downstream node in the StateMod
//				  	  file.
//					* Change printStatus(1,...) to level 2.
// 2005-12-21	JTS, RTi		* &, < and > in page layout ID are now
//					  escaped when saving to XML.
//					* Notes added to the XML documentation
//					  detailing how escaping must be done
//					  if editing by hand.
// 2005-12-22	JTS, RTi		* Link IDs are now escaped.
//					* Annotation text is now escaped.
// 2006-01-03	SAM, RTi		* Change getNodesForType() to accept
//					  -1 for node type for all nodes.
//					* Overload writeListFile() to accept
//					  a list of comments.
// 2007-02-18	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------
// EndHeader

package cdss.domain.hydrology.network;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.openwaterfoundation.network.NodeNetwork;

import RTi.GR.GRLimits;
import RTi.GR.GRText;

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;

import RTi.Util.Math.MathUtil;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

/**
This class contains all the information necessary to manage a network of Node nodes,
which represent a hydrologic network (topology) as a linked list of upstream/doLwnstream
nodes.  Node types are those seen in CDSS.
*/
public class HydrologyNodeNetwork implements NodeNetwork
{

/**
Default node and font sizes for populating layout information.
*/
public static final int 
	DEFAULT_FONT_SIZE = 10,
	DEFAULT_NODE_SIZE = 20;

/**
Default paper information for populating layout information.
*/
public static final String
	DEFAULT_PAPER_SIZE = "C",
	DEFAULT_PAGE_ORIENTATION = "Landscape";

/**
Ways to label the network when plotting.
*/
public static final int 
	LABEL_NODES_AREA_PRECIP	= 1,
	LABEL_NODES_COMMONID = 2,
	LABEL_NODES_NETID = 3,
	LABEL_NODES_PF = 4,
	LABEL_NODES_RIVERNODE = 5,
	LABEL_NODES_WATER = 6,
	LABEL_NODES_NAME = 7;

/**
Default starting coordinates for plotting.
TODO (JTS - 2004-11-11) unused
*/
public static final int 
	NETWORK_STARTX	= -999999,
	NETWORK_STARTY	= -999999;

/**
Types of networks.
*/
public static final int 
	NETWORK_MAKENET	= 1,
	NETWORK_WIS	= 2;

/**
Different types of data to search for in an HydroBase_Node.  The following 
matches the wdwater_num that the stream is trib to or trib from, based on 
row type.  This is used when constructing the network from WIS format data.
*/
public static final int NODE_DATA_LINK	= 1;

/**
End-of-basin node ID.
*/
public static final String OUTFLOW_NODE	= "999999999999";

/**
Plotting preferences to set whether to draw rivers shaded.
*/
public static final int PLOT_RIVERS_SHADED = 0x1;

/**
Go to the top or bottom of the system.
*/
public static final int POSITION_ABSOLUTE = 1;

/**
Find the next upstream or downstream node, computation-wise.
*/
public static final int POSITION_COMPUTATIONAL = 2;

/**
Find the most upstream or downstream node in a reach.
*/
public static final int POSITION_REACH = 3;

/**
Find the next upstream or downstream node in the same reach.  Really, this
only applies to going downstream.
*/
public static final int POSITION_RELATIVE = 4;

/**
Get the next node in the reach.
*/
public static final int POSITION_REACH_NEXT = 5;

/**
Flags indicating how to output the network.
*/
public static final int 
	POSITION_UPSTREAM = 1,
	POSITION_DOWNSTREAM = 2;

/**
Used in reading in XML files.
*/
//private static boolean __setInWIS = false;
// FIXME SAM 2008-03-15 Need to remove WIS code

/**
Whether this network is being displayed in a WIS or not.
*/
// FIXME SAM 2008-03-15 Need to remove WIS code
//private boolean __inWIS = true;

/**
 * Identifier for the network.
 */
private String __networkId = "";

/**
 * Name for the network.
 */
private String __networkName = "";

/**
Whether the legend position was read from an XML file.
*/
private boolean __legendPositionSet = false;

/**
FIXME SAM 2008-12-10 Evaluate use - can this be converted to "natural flow"?  For now do it.
Indicates that dry river nodes should be treated as natural flow.  This is used by the WIS.
*/
private boolean __treatDryAsNaturalFlow;

/**
Holds the bounds of the network for use in a network GUI.
*/
private double
	__netLX = -999.0,
	__netBY = -999.0,
	__netRX = -999.0,
	__netTY = -999.0;

/**
The extra edge around drawn data to be used for visualization, in network coordinate units, left, right, top, bottom.
This is highly dependent on the layout.  For example, the buffer on the left and right may need to be
larger to allow for horizontal labels whereas the buffer on the top and bottom can be less because
labels do not have much height.
*/
private double [] __edgeBuffer = { 0.0, 0.0, 0.0, 0.0 };

// FIXME SAM 2008-03-17 The following seems fragile given hand-off between methods.
/**
Used when interpolating node locations.
*/
private double 
	__nodeSpacing = 0,
	__lx = 0,
	__by = 0;

/**
The network title.
*/
private String __title;

/**
Data for graphics.
*/
private double	
	__fontSize,
	// TODO SAM 2007-02-18 Evaluate whether needed
	//__legendX,
	//__legendY,
	//__legendDX,
	//__legendDY,
	__nodeDiam,
	__titleX,
	__titleY;

/**
The name of the input file for this network, or null if it has not been read/saved.
*/
private String __inputName = null;

/**
The lower-left position of the legend as read from an XML file.
*/
private double
	__legendPositionX = 0,
	__legendPositionY = 0;

/**
First node.  The head of the linked list of nodes.
*/
private HydrologyNode __nodeHead;

/**
The label type for plot labeling.
*/
private int __labelType;

/**
The count of nodes in the network.
*/
private int __nodeCount;

/**
Check file to be used by modelers.
*/
private PrintWriter __checkFP = null;

/**
The line separator to be used.
*/
private String __newline = System.getProperty("line.separator");

/**
List of the annotations that are drawn with this network.  Note that these are primitive
annotations like lines, which should not be confused with the run-time annotations applied by
software like the StateMod GUI (which do not persist in the network file).
*/
private List<HydrologyNode> __annotationList = new Vector<HydrologyNode>();

/**
Network labels.
*/
private List<HydrologyNodeNetworkLabel> __labelList = new Vector<HydrologyNodeNetworkLabel>();

// TODO SAM 2011-01-04 The XMin, YMin, XMax, YMax properties might need to be stored with the layout
// rather than the whole network in order to buffer the page boundaries properly.
/**
List for holding layouts read in from an XML file. 
*/
private List<PropList> __layoutList = new Vector<PropList>();

/**
List of links between nodes that are drawn with this network.
The PropList contains FromNodeID and ToNodeID as the properties.
*/
private List<PropList> __linkList = new Vector<PropList>();

/**
Constructor.  Network ID and name are blank and no end node is added.
*/
public HydrologyNodeNetwork() {
	this(false);
}

/**
Constructor.  No end node is added.
@param networkId identifier for network.
@param networkName name for network.
*/
public HydrologyNodeNetwork(String networkId, String networkName ) {
	this(false);
}

/**
Constructor.  Network ID and name are blank.
@param addEndNode if true an end node will automatically be added at initialization.
*/
public HydrologyNodeNetwork(boolean addEndNode)
{
	this("","",false);
}

/**
Constructor.
@param networkId identifier for network.
@param networkName name for network.
@param addEndNode if true an end node will automatically be added at initialization.
*/
public HydrologyNodeNetwork(String networkId, String networkName, boolean addEndNode)
{
	this.__networkId = networkId;
	this.__networkName = networkName;
	initialize();

	if (addEndNode) {
		HydrologyNode endNode = new HydrologyNode();
		// FIXME SAM 2008-03-15 Need to remove WIS code
		//endNode.setInWIS(false);
		endNode.setType(HydrologyNode.NODE_TYPE_END);
		endNode.setCommonID("END");
		endNode.setIsNaturalFlow(false);
		endNode.setIsImport(false);
		endNode.setSerial(1);
		endNode.setNodeInReachNumber(1);
		endNode.setReachCounter(1);
		endNode.setComputationalOrder(1);
		__nodeHead = endNode;
	}
}

/**
Store a label to be drawn on the plot.
@param x X-coordinate of label.
@param y Y-coordinate of label.
@param flag Text orientation.
@param label Text for label.
*/
protected void addLabel(double x, double y, double size, int flag, String label) {
	String routine = "addLabel";

	if (Message.isDebugOn) {
		Message.printDebug(2, routine, "Storing label \"" + label + "\" at " + x + " " + y);
	}
	__labelList.add(new HydrologyNodeNetworkLabel(x, y, size, flag, label));
}

/**
Adds a node the network.  Nodes can be added in any order but if added in random order connections may not be properly built
@param nodeId the id of the node to add.
@param nodeType the kind of node to add.
@param upstreamNodeId the id of the node immediately upstream of this node.  Can be null.
@param downstreamNodeId the id of the node immediately downstream of this node.
@param isNaturalFlow whether the node is a natural flow node or not.
@param isImport whether the node is an import node or not.
@return the node that is added
*/
public HydrologyNode addNode(String nodeId, int nodeType, String upstreamNodeId, String downstreamNodeId, boolean isNaturalFlow, boolean isImport) {
	String routine = getClass().getSimpleName() + ".addNode";
	if ( Message.isDebugOn ) {
		Message.printDebug(1, routine, "Adding node \"" + nodeId + "\", linking upstream to \""
			+ upstreamNodeId + "\" and downstream to \"" + downstreamNodeId + "\", nodeType=" + nodeType +
			" isNaturalFlow=" + isNaturalFlow);
	}
	// Make sure the id is unique within the network
	nodeId = checkUniqueID(nodeId, true);

	// Create the node and set up the fields that are known
	HydrologyNode addNode = new HydrologyNode();
	//addNode.setInWIS(__inWIS);
	// FIXME 2008-03-15 Need to remove WIS code
	addNode.setType(nodeType);
	addNode.setCommonID(nodeId);
	addNode.setIsNaturalFlow(isNaturalFlow);
	addNode.setIsImport(isImport);

	boolean done = false;
	HydrologyNode downstreamNode = null;
	HydrologyNode upstreamNode = null;
	HydrologyNode node = getMostUpstreamNode();
	List<HydrologyNode> v = new Vector<HydrologyNode>();

	// Loop through the network and do two things:
	// 1) Add all the nodes to a list so that they can easily be
	//    manipulated later.  This method needs random accessibility
	//    of the nodes in the network.
	// 2) Find the nodes that have the upstream and downstream ids passed
	//    to this method, and store them in the upstreamNode and downstreamNode, respectively.
	while (!done) {
		if (node == null) {
			done = true;
		}
		else {
			v.add(node);
			if (node.getType() == HydrologyNode.NODE_TYPE_END) {
				done = true;
			}

			if (upstreamNodeId != null && node.getCommonID().equals(upstreamNodeId)) {
				upstreamNode = node;
			}
			else if (node.getCommonID().equals(downstreamNodeId)) {
				downstreamNode = node;
			}
			
			if (!done) {
				node = getDownstreamNode(node, POSITION_COMPUTATIONAL);
			}
		}
	}

	boolean found = false;
	HydrologyNode downStreamNodeUpstreamNode = null;
	String[] upstreamNodeIds = downstreamNode.getUpstreamNodesIDs();

	// If the upstreamNodeId is not null then find that node in the upstream nodes
	// of the downstreamNode, and then replace it in downstreamNode's upstream nodes with the node being added.
	if (upstreamNodeId != null) {
		for (int i = 0; i < upstreamNodeIds.length; i++) {
			if (upstreamNodeIds[i].equals(upstreamNodeId)) {
				downStreamNodeUpstreamNode = downstreamNode.getUpstreamNode(i);
				addNode.setTributaryNumber(downStreamNodeUpstreamNode.getTributaryNumber());
				downstreamNode.replaceUpstreamNode(addNode, i);
				found = true;
				break;
			}
		}
	}

	// If the node was not found upstream of the downstream node then
	// don't worry about, just add the node to be added to upstream nodes of downstreamNode.
	if (!found) {
		addNode.setTributaryNumber(downstreamNode.getNumUpstreamNodes() + 1);
		downstreamNode.addUpstreamNode(addNode);
	}

	// Set downstreamNode to be the node to be added's downstream node
	addNode.setDownstreamNode(downstreamNode);

	// At this point, the following connections have been made:
	// - the node to be added now points downstream to downstreamNode.
	// - downstreamNode node points to the node to be added as one of its upstream nodes.

	// If an upstream node was found, then set the node to be added
	// as the downstream node and the upstream node as the node to be added's only upstream node.
	if (upstreamNode != null) {
		upstreamNode.setDownstreamNode(addNode);
		addNode.addUpstreamNode(upstreamNode);
	}
	
	// Get the computational order value from the downstream node and
	// re-assign it to the node to be added.  The computational order
	// for all the nodes downstream of the node to be added will be recomputed below.
	int comp = downstreamNode.getComputationalOrder();
	addNode.setComputationalOrder(comp);

	// Set downstreamNode's upstream order to now be the node to be added's, too.
	addNode.setUpstreamOrder(downstreamNode.getUpstreamOrder());

	int serial = -1;

	if (!found) {
		// (new tributary) 
		// Look through the network upstream of the downstream node for the largest serial value
		// - the new node will need to be one larger than that value if it is on a new tributary.
		serial = findHighestUpstreamSerial(downstreamNode, downstreamNode.getSerial());
	}
	else {
		// Placed on an existing tributary
		// - upstreamNode still holds the value of the upstream Node
		serial = downStreamNodeUpstreamNode.getSerial();
		// Decrement it because want to find the value of the serial
		// number that is one less than what the new node's serial number will be.
		serial--;
	}

	// Now calculate the plotting location for the new node.

	double x1 = downstreamNode.getX();
	double x2 = 0;
	double y1 = downstreamNode.getY();
	double y2 = 0;

	if (upstreamNode != null) {
		// If there are any upstream nodes, then average the X and Y
		// values of downstreamNode and upstreamNode and set the node to be added there.  
		// It will end up directly in-between both nodes.
		x2 = upstreamNode.getX();
		y2 = upstreamNode.getY();

		addNode.setX((x1 + x2) / 2);
		addNode.setY((y1 + y2) / 2);		
	}
	else {
		// If there is no upstream node then the location can't simply
		// be average out.  See if the downstream node has a downstream node.
		HydrologyNode downstreamNodeDownstreamNode = downstreamNode.getDownstreamNode();

		if (downstreamNodeDownstreamNode == null) {
			// If the downstream node has no downstream then the network only has one node in it.
			// Put this new node almost completely on top of the old one -- it can be moved later.
			addNode.setX(x1 + 0.001);
			addNode.setY(y1 + 0.001);
		}
		else {
			// If the downstream node has a downstream node then
			// get the delta of the position from the downstreamNode and
			// its downstream node, and apply the same delta to downstreamNode to
			// determine the position for this new node.
			x2 = downstreamNodeDownstreamNode.getX();
			y2 = downstreamNodeDownstreamNode.getY();
			double dx = x1 - x2;
			double dy = y1 - y2;
			addNode.setX(x1 + dx);
			addNode.setY(y1 + dy);
		}
	}

	// Loop through the entire network and change the serial counter and
	// computational order value.  All the nodes downstream of the node
	// being added need their computation order value incremented.  All
	// the nodes upstream of the node to be added need their serial counter decremented.
	for (int i = 0; i < v.size(); i++) {
		node = v.get(i);
		if (node.getComputationalOrder() >= comp) {
			node.setComputationalOrder(	node.getComputationalOrder() + 1);
		}
		if (node.getSerial() > serial) {
			node.setSerial(node.getSerial() + 1);
		}
	}

	// Set the serial of the new node
	// - one greater than the serial number found above
	addNode.setSerial(serial + 1);

	// A few changes are left to be made:
	// - the node in reach number counter needs set, as does the reach counter.

	if (upstreamNode == null) {
		if (upstreamNodeIds == null || upstreamNodeIds.length == 0) {
			// If there is no upstream node found with the 
			// specified ID and there are no upstream nodes from 
			// the downstream node then the node in reach number
			// and reach counter can be easily figured from the
			// downstream node.  Basically, this new node is the very last
			// node in an existing reach.
			addNode.setNodeInReachNumber(downstreamNode.getNodeInReachNumber() + 1);
			addNode.setReachCounter(downstreamNode.getReachCounter());		
		}
		else {
			// Otherwise find the highest-numbered reach in the
			// system and then set the new node's reach to be
			// a new reach 1 more than the current highest-numbered
			// reach number.  This new node is the start of a new
			// reach off the downstream node.
			int max = 0;
			for (int i = 0; i < v.size(); i++) {
				node = v.get(i);
				if (node.getReachCounter() > max) {
					max = node.getReachCounter();
				}
			}
			
			addNode.setNodeInReachNumber(1);
			addNode.setReachCounter(max + 1);
		}
	}
	else {
		// If there is an upstream node then this node is in the same
		// reach as the branch between downstreamNode and upstreamNode.  All that needs done
		// is to increment all the nodes downstream of this one
		// so that their 'node in reach number' value is one more.

		int nodeNum = upstreamNode.getNodeInReachNumber();
	
		addNode.setNodeInReachNumber(nodeNum);
	
		int reach = upstreamNode.getReachCounter();
		addNode.setReachCounter(reach);
	
		for (int i = 0; i < v.size(); i++) {
			node = v.get(i);
			if (node.getReachCounter() == reach) {
				if (node.getNodeInReachNumber() >= nodeNum) {
					node.setNodeInReachNumber(node.getNodeInReachNumber()+ 1);
				}
			}
		}
	}
	// Return the node that was added
	return addNode;
}

/**
Fills out the node in reach number, reach counter, tributary number, serial 
number, and computational order number for a network of nodes.  These nodes
were probably read in from an XML file, StateMod_RiverNodeNetwork list, or from a simple list
of nodes (e.g., from network append/merge).
@param nodesV the list of nodes for which to calculate values.
@param endFirst if true, then the first node in the list is the 
most-downstream node in the network (the END node).  If false, the the END
node is the last node in the list.
*/
public void calculateNetworkNodeData(List<HydrologyNode> nodesV, boolean endFirst)
{	String routine = getClass().getSimpleName() + ".calculateNetworkNodeData";
	// First put the nodes into an array for easy and quick traversal. 
	// In the array, make sure the END node is always the first one found.
	int size = nodesV.size();
	HydrologyNode[] nodes = new HydrologyNode[size];
	if (endFirst) {
		for (int i = 0; i < size; i++) {
			nodes[i] = nodesV.get(i);
		}
	}
	else {
		int count = 0;
		for (int i = size - 1; i >= 0; i--) {
			nodes[count++] = nodesV.get(i);
		}
	}

	// Create a hashtable that maps all the node IDs to an integer.  This
	// integer points to the location within the array where the node can be found.
	Hashtable<String,Integer> hash = new Hashtable<String,Integer>(size);
	for (int i = 0; i < size; i++) {
		hash.put(nodes[i].getCommonID(), new Integer(i));
	}

	// certain data for the head node can be set immediately.
	nodes[0].setNodeInReachNumber(1);
	nodes[0].setReachCounter(1);
	nodes[0].setTributaryNumber(1);

	int lastReach = 0;
	int highestReach = 1;
	int nodeInReachNumber = -1;
	int nodeNum = -1;
	int reachCounter = -1;
	int tributaryNumber = -1;
	
	String[] usIDs = nodes[0].getUpstreamNodeIDs();
	int size2 = usIDs.length;
	if (IOUtil.testing()) {
		Message.printStatus(2, "", "Header '" + nodes[0].getCommonID() + "' has " + size2 + " us nodes.");
	}
	for (int i = 0; i < size2; i++) {
		nodeNum = ((Integer)hash.get(usIDs[i])).intValue();
		if (IOUtil.testing()) {
			Message.printStatus(2, "", "Header US: '" + nodes[nodeNum].getCommonID() + "'");
		}
		if (i == (size2 - 1)) {
			// This node is on the same reach -- it's either the
			// last upstream node to be processed or the only
			// node to be processed.  All of its data can be
			// hard-coded in as it's related directly to the head node.
			nodeInReachNumber = 2;
			tributaryNumber = i + 1;	
			reachCounter = 1;
		}
		else if (i == 0 && (size2 > 1)) {
			// This is the first of several nodes to be processed.
			// Its data can also all be hard-coded in, even though
			// it is not on the same reach as the head node.
			nodeInReachNumber = 1;
			tributaryNumber = 1;
			reachCounter = 2;
			highestReach = 2;
		}
		else {
			// This is a node who reach number depends on however
			// many reaches have been processed ahead of it.  
			// That value is returned from the helper method.
			nodeInReachNumber = 1;
			tributaryNumber = i + 1;
			reachCounter = lastReach + 1;
			highestReach = lastReach + 1;
		}
		
		lastReach = calculateNetworkNodeDataHelper(nodeNum, nodes, hash,
			reachCounter, nodeInReachNumber, tributaryNumber, highestReach);
		// keep track of the highest reach generated -- new reaches are built after this one.
		if (lastReach > highestReach) {
			highestReach = lastReach;
		}
	}

	// connect the nodes
	String dsID = null;
	Object nodeNumObject;
	Integer tempI = null;
	for (int i = 0; i < size; i++) {
		dsID = nodes[i].getDownstreamNodeID();
		if (dsID != null && !dsID.equals("") && !dsID.equals("null")) {
			nodeNumObject = hash.get(dsID);
			if ( nodeNumObject == null ) {
				String message = "Processing node \"" + nodes[i].getCommonID() +
				" - downstream ID \"" + dsID + "\" not found in hash.";
				Message.printStatus(2, routine, message );
				throw new RuntimeException ( message );
			}
			nodeNum = ((Integer)nodeNumObject).intValue();
			nodes[i].setDownstreamNode(nodes[nodeNum]);
		}
		usIDs = nodes[i].getUpstreamNodeIDs();
		size2 = usIDs.length;
		for (int j = 0; j < size2; j++) {
			if (usIDs[j] != null && !usIDs[j].equals("") && !usIDs[j].equals("null")) {
				tempI = (Integer)(hash.get(usIDs[j]));
				if (tempI != null) {
					nodeNum = tempI.intValue();	
					nodes[i].addUpstreamNode(nodes[nodeNum]);
				}
			}
		}
	}
	
	List<HydrologyNode> networkV = new Vector<HydrologyNode>(size);
	for (int i = size - 1; i >= 0; i--) {
		networkV.add(nodes[i]);		
	}

	setNetworkFromNodes(networkV);

	HydrologyNode node = getMostUpstreamNode();
	int serialCount = size;

//	if (IOUtil.testing()) {
//		Message.printStatus(2, "", "SerialCount: " + serialCount);
//	}
	int compCount = 1;
	while (true) {
		node.setSerial(serialCount--);
		node.setComputationalOrder(compCount++);
//		if (IOUtil.testing()) {
//			Message.printStatus(2, "", "'" + node.getCommonID() 
//				+ "'  S: " + node.getSerial() + "  C: " 
//				+ node.getComputationalOrder() + "  (" 
//				+ node.getReachCounter() + " " 			
//				+ node.getNodeInReachNumber() + " " 
//				+ node.getTributaryNumber() + ")");
//		}
		if (node.getType() == HydrologyNode.NODE_TYPE_END) {
			return;
		}
		node = getDownstreamNode(node, POSITION_COMPUTATIONAL);
	}
}

/**
A recursive helper method for calculateNetworkNodeData. 
@param nodeNum the nodeNum (in the array) of the current node for which to set up values.
@param nodes an array of all the nodes in the network.
@param hash a Hashtable that matches ids with positions in the array.
@param reachCounter this node's value for reachCounter.
@param nodeInReachNumber this node's value for node in reach number.
@param tributaryNumber this node's value for tributary number.
@param highestReach this node's value for the highest reach number seen so far.
*/
private int calculateNetworkNodeDataHelper(int nodeNum, HydrologyNode[] nodes, 
Hashtable<String,Integer> hash, int reachCounter, int nodeInReachNumber, int tributaryNumber,
int highestReach) {
//	Message.printStatus(2, "", 
//		" fvdh: #" + StringUtil.formatString(nodeNum, "%03d")
//		+ "  " 
//		+ StringUtil.formatString(nodes[nodeNum].getCommonID(), 
//		"%14.14s")
//		+ "  RC: " + StringUtil.formatString(reachCounter, "%03d")
//		+ "  N#: " + StringUtil.formatString(nodeInReachNumber, "%03d")
//		+ "  T#: " + StringUtil.formatString(tributaryNumber, "%03d"));
	nodes[nodeNum].setReachCounter(reachCounter);
	nodes[nodeNum].setNodeInReachNumber(nodeInReachNumber);
	nodes[nodeNum].setTributaryNumber(tributaryNumber);
	int lastReach = reachCounter - 1;
	String[] usIDs = nodes[nodeNum].getUpstreamNodeIDs();
	int size = usIDs.length;
	int currNodeNum = -1;
	for (int i = 0; i < size; i++) {
		if (hash.get(usIDs[i]) == null) {
			break;
		}
		currNodeNum = ((Integer)hash.get(usIDs[i])).intValue();
		if (i == (size - 1)) {
			// This node is on the same reach -- it's either the
			// last upstream node to be processed or the only node to be processed. 
			nodeInReachNumber = nodes[nodeNum].getNodeInReachNumber() + 1;
			tributaryNumber = i + 1;	
			reachCounter = nodes[nodeNum].getReachCounter();
		}
		else if (i == 0 && (size > 1)) {
			// Node on a different reach.  This is the first of several nodes to be processed.
			nodeInReachNumber = 1;
			tributaryNumber = 1;
			reachCounter = highestReach + 1;
			highestReach++;
		}
		else {
			// Node on a different reach.
			// This is a node who reach number depends on however
			// many reaches have been processed ahead of it.  
			// That value is returned from the helper method.
			nodeInReachNumber = 1;
			tributaryNumber = i + 1;
			reachCounter = highestReach + 1;
			highestReach++;
		}

		lastReach = calculateNetworkNodeDataHelper(currNodeNum, nodes, 
			hash, reachCounter, nodeInReachNumber, tributaryNumber,
			highestReach);
		if (lastReach > highestReach) {
			highestReach = lastReach;
		}
	}	
	return highestReach;
}

/**
Check the network integrity by traversing the network.
@param flag one of NETWORK_MAKENET or NETWORK_WIS.
@return false if the network has problems, true if OK.
*/
public boolean checkNetwork(int flag) {
	String routine = "HydroBase_NodeNetwork.checkNetwork";
	String message;
	HydrologyNode nodePt;

	Message.printStatus(2, routine,"Checking river network for validity");

	if (flag == NETWORK_MAKENET) {
		nodePt = getDownstreamNode(__nodeHead, POSITION_ABSOLUTE);
		if (nodePt.getType() != HydrologyNode.NODE_TYPE_END) {
			message = "Bottom-most node \"" + nodePt.getCommonID() + "\" type is not END";
			Message.printWarning(2, routine, message);
			printCheck(routine, 'W', message);
			return false;
		}
	}

	return true;
}

/**
Checks the id to make sure that it is unique in the network.  Called by addNode().
@param id the id to check.
@param first because this method is recursive in how it searches for a 
unique ID, the first time it is called (i.e., not by itself) this parameter
should be true.  When the method calls itself, this parameter is false.
@return an ID that is unique within the network.
*/
private String checkUniqueID(String id, boolean first)
{
	boolean done = false;
	HydrologyNode node = getMostUpstreamNode();
	int count = 0;

	// First go through all the nodes and count how many have the same id as that passed in
	while (!done) {
		if (node == null) {
			done = true;
		}
		else {
			if (node.getCommonID().equals(id)) {
				count++;
			}		
			if (node.getType() == HydrologyNode.NODE_TYPE_END) {
				done = true;
			}

			if (!done) {
				node = getDownstreamNode(node, POSITION_COMPUTATIONAL);
			}
		}
	}

	if (count == 0) {
		// If no other nodes have that id, it is unique and can be returned
		return id;
	}
	else {	
		if (!first) {
			// If this was called by itself then it means that
			// an attempt to check a different id for uniqueness
			// failed at this point.  It needs to return a 
			// different id that that which was passed in for 
			// the code to recognize a failure at trying a new ID.  See below.
			return id + "----------X";
		}

		// The id is not unique.  An attempt will be made to create
		// a unique id by appending "_" and a number to the end of
		// the passed-in ID.  The number will be incremented until a unique value is found.
		String newID = null;
		for (int i = count;;i++) {
			newID = id + "_" + i;
			if (checkUniqueID(newID, false).equals(newID)) {
				return newID;
			}
		}
	}
}

/**
Converts old-style base flow nodes into new style nodes (other node type with an attribute indicating
whether a natural flow and/or import location).
*/
public void convertNodeTypes() {
	List<HydrologyNode> nodes = new Vector<HydrologyNode>();
	String routine = "HydroBase_NodeNetwork.convertNodeTypes";
	HydrologyNode node = getMostUpstreamNode();	
	while (true) {
		nodes.add(node);
		if (node.getType() == HydrologyNode.NODE_TYPE_END) {
			break;
		}		
		node = getDownstreamNode(node, POSITION_COMPUTATIONAL);	
	}

	int size = nodes.size();
	for (int i = 0; i < size; i++) {
		node = nodes.get(i);
		if (node.getType() == HydrologyNode.NODE_TYPE_BASEFLOW) {
			node.setType(HydrologyNode.NODE_TYPE_OTHER);
			node.setIsNaturalFlow(true);
			Message.printStatus(2, routine, 
				"Converting node \"" + node.getCommonID() + "\" from BFL to OTH/IsNaturalFlow=true.");
		}
		else if (node.getType() == HydrologyNode.NODE_TYPE_IMPORT) {
			node.setType(HydrologyNode.NODE_TYPE_OTHER);
			node.setIsImport(true);
			Message.printStatus(2, routine,
				"Converting node\"" + node.getCommonID() + "\" from IMP to OTH/IsImport=true.");
		}
	}

	setNetworkFromNodes(nodes);
}


/**
Create the indented river network as a list of strings
@return list of String containing output.
*/
public List<String> createIndentedRiverNetworkStrings() {
	int dl = 30;
	String routine = "HydroBase_NodeNetwork.createIndentedRiverNetworkStrings";
	List<String> v = new Vector<String>();
	v.add("# Stream Network Data");
	v.add("#");
	v.add("# Data fields are:");
	v.add("#");
	v.add("# The left-most number is the stream number (1 is largest [most downstream] in this network)");
	v.add("# The node name.");
	v.add("# Node type (e.g., STREAM).");
	v.add("# Node identifier (may be used for database queries).");
	v.add("# Network position (e.g., row in table).");
	v.add("#");

	// Go to the bottom of the system so that we can get to the top of the main stem...
	HydrologyNode node = null;
	node = getDownstreamNode(__nodeHead, POSITION_ABSOLUTE);

	// Now traverse downstream, creating the strings...
	int indent = 0;
	int tab = 8;
	StringBuffer buffer = null;
	for (HydrologyNode nodePt = getUpstreamNode(node, POSITION_ABSOLUTE);
		nodePt != null;
		nodePt = getDownstreamNode(nodePt, POSITION_COMPUTATIONAL)) {
		if (Message.isDebugOn) {
			Message.printDebug(dl, routine,
				"Formatting .ind node \"" + nodePt.getNetID() + "\"(\"" + nodePt.getCommonID() + "\")");
		}
		indent = (nodePt.getReachLevel() - 1) * tab;
		buffer = new StringBuffer();
		buffer.append(StringUtil.formatString(nodePt.getReachLevel(), "%2d") + "  ");
		for (int i = 0; i < indent; i++) {
			buffer.append(" ");
		}

		if (nodePt.getDescription().length() != 0) {
			// Have a non-blank node...
			v.add(buffer.toString()
				+ nodePt.getDescription() + "(Type = "
				+ HydrologyNode.getTypeString(nodePt.getType(), 0)
				+ ", ID = \""
				+ nodePt.getCommonID() + "\", Pos = "
				+ nodePt.getSerial() + ")");
		}
		
		if (nodePt.getDownstreamNode() == null) {
			break;
		}
	}

	return v;
}

/**
Create indented text river network file.
@param basename Base file name for output.
@return true if the file was created successfully, false if not.
*/
public boolean createIndentedRiverNetworkFile(String basename) {
	String routine = "HydroBase_NodeNetwork.createIndentedRiverNetwork";
	PrintWriter orderfp;
	String orderFile;

	// Set the output file name...
	if (basename == null) {
		orderFile = "river.ind";
	}
	else {	
		orderFile = basename + ".ind";
	}

	Message.printStatus(2, routine, "Creating indented river network file \"" + orderFile + "\"");

	// Open the output file...

	try {	
		orderfp = new PrintWriter(new FileWriter(orderFile));
	}
	catch (IOException e) {
		Message.printWarning(2, routine, "Error opening indented network file \"" + orderFile + "\"");
		Message.printWarning(2, routine, e);
		return false;
	}

	List<String> v = createIndentedRiverNetworkStrings();
	int size = v.size();
	for (int i = 0; i < size; i++) {
		orderfp.println(v.get(i));
	}
	orderfp.flush();
	orderfp.close();

	return true;
}

/**
Create the .ord river order file.  Create the file indicating the stream 
ordering (used to be called "updown.out").  This is not really needed for
anything, but is useful for debugging, etc.
@param basename Basename for files (can include directory).
@param flag Unused.
@return true if done successfully, false if not.
*/
public boolean createRiverOrderFile(String basename, int flag) {
	String routine = "HydroBase_NodeNetwork.createRiverOrderFile";
	HydrologyNode nodePt;
	int dl = 30;
	PrintWriter nodelist_fp, orderfp;
	String 	nodelistFile, 
		message, 
		orderFile;

	// Set the output file name...

	try {

	if (basename == null) {
		orderFile = "river.ord";
	}
	else {	
		orderFile = basename + ".ord";
	}

	if (basename == null) {
		nodelistFile = "nodelist.txt";
	}
	else {	
		nodelistFile = basename + "_nodelist.txt";
	}

	Message.printStatus(2, routine, "Creating river order file \"" + orderFile + "\"");

	HydrologyNode node = null;
	if ((flag == 0) || (flag == POSITION_DOWNSTREAM)) {
		// For this type of output we start at the bottom of the stream...
		node = getDownstreamNode(__nodeHead, POSITION_ABSOLUTE);
	}
	else {	
		Message.printWarning(2, routine, "Only know how to print the river order starting downstream");
		return false;
	}

	// Open the output file...
	try {	
		orderfp = new PrintWriter(new FileWriter(orderFile));
	}
	catch (Exception e) {
		message = "Error opening order file \"" + orderFile + "\"";
		Message.printWarning(2, routine, message);
		printCheck(routine, 'W', message);
		Message.printWarning(2, routine, e);
		return false;
	}

	try {	
		nodelist_fp = new PrintWriter(new FileWriter(nodelistFile));
	}
	catch (Exception e) {
		message = "Error opening node list file \"" + nodelistFile + "\"";
		Message.printWarning(2, routine, message);
		printCheck(routine, 'W', message);
		Message.printWarning(2, routine, e);
		return false;
	}
	finally {
		if ( orderfp != null ) {
			orderfp.close();
		}
	}

	//try {}
	orderfp.println("# " + orderFile + " - River order file");
	// FIXME SAM 2008-03-15 Need to move to StateDMI and reenable the following
	//printCreatorHeader(__dmi, orderfp, "#", 80, 0);
	orderfp.println("# The nodes are listed from the top of the system to the bottom (the");
	orderfp.println("# reverse order from the .net file).");
	orderfp.println("#");
	orderfp.println("# Column 1:  Node count - this is the order that nodes were added");
	orderfp.println("#            from the .net file.");
	orderfp.println("# Column 2:  River level (main stem=1, tributary off main stem=2, etc.).");
	orderfp.println("# Column 3:  River counter indicating the reach processed from the");
	orderfp.println("#            network file(main stem=1, last reach processed=largest #).");
	orderfp.println("# Column 4:  Node type.");
	orderfp.println("# Column 5:  Node on network schematic (using common IDs).");
	orderfp.println("# Column 6:  River node(model IDs)corresponding to column 1.");
	orderfp.println("# Column 7:  Description for node.");
	orderfp.println("# Column 8:  Area(consistent units).");
	orderfp.println("# Column 9:  Precipitation(consistent units, measure method).");
	orderfp.println("# Column 10: Area x precipitation using consistent units(essentially");
	orderfp.println("#            volume)for area above node.");
	orderfp.println("# Column 11: Downstream node (using common IDs).");
	orderfp.println("# Column 12: Downstream node (using river node IDs).");
	orderfp.println("# Column 13: X plotting coordinate.");
	orderfp.println("# Column 14: Y plotting coordinate.");
	orderfp.println("#---------------------------------------------------"
		+ "----------------------------------------------------------"
		+ "---------------------------------------------------------");
	orderfp.println("# (1)   (2)   (3)     (4) =      (5)      =       "
		+ "(6)     =            (7)           =  (8)    * (9)  =   "
		+ "  (10)    -->     (11)     =    (12)            (13)    "
		+ "       (14)" );
	orderfp.println("#                 =   Common ID  = RiverNode ID = "
		+ "     Node Description    =  Area   * Prec =   Water     "
		+ "--> Downstream   = Down Riv Nod   X-coordinate   "
		+ "Y-coordinate");
	orderfp.println("#---------------------------------------------------"
		+ "----------------------------------------------------------"
		+ "---------------------------------------------------------");
	orderfp.println("# EndHeader");
	
	nodelist_fp.println("makenet_id,name,nodeType,down_id");
	String down_id;
	HydrologyNode down_node = null;
	for (nodePt = getUpstreamNode(node, POSITION_ABSOLUTE);
	    nodePt != null; 
	    nodePt = getDownstreamNode(nodePt, POSITION_COMPUTATIONAL)) {
		if (Message.isDebugOn) {
			Message.printDebug(dl, routine,
				"Printing .ord node \"" + nodePt.getNetID() + "\"(\"" + nodePt.getCommonID() + "\")");
		}
		
		printOrdNode(nodePt.getSerial(), orderfp, nodePt);
		// Print the delimited file...
		down_node = findNextRealDownstreamNode(nodePt);
		if (down_node == null) {
			down_id = "";
		}
		else {	
			down_id = down_node.getCommonID();
		}
		if ((nodePt.getType() != HydrologyNode.NODE_TYPE_BLANK) 
		    && (nodePt.getType() != HydrologyNode.NODE_TYPE_CONFLUENCE)
		    && (nodePt.getType() != HydrologyNode.NODE_TYPE_XCONFLUENCE)) {
			nodelist_fp.println(nodePt.getCommonID() + ",\"" 
				+ nodePt.getDescription() + "\"," 
				+ HydrologyNode.getTypeString(
				nodePt.getType(),1) + "," + down_id);
		}
		
		down_node = nodePt.getDownstreamNode();
		
		if (down_node == null) {
			break;
		}
	}
	orderfp.flush();
	orderfp.close();
	nodelist_fp.flush();
	nodelist_fp.close();
	}
	catch (Exception e) {
		Message.printWarning(2, routine, "Error writing order file");
		Message.printWarning(2, routine, e);
		return false;
	}
	return true;
}

/**
Removes a node from the network, and maintains the connections between the 
upstream and downstream nodes relative to the node that is removed.  This 
code is very inefficient and should not be used often.  
TODO (JTS - 2004-04-14)<br> Inefficient!!  This could really be improved.
@param id the id of the node to delete.
*/
public void deleteNode(String id) {
	boolean done = false;
	HydrologyNode node = getMostUpstreamNode();
	List<HydrologyNode> v = new Vector<HydrologyNode>();

	// First loop through all the nodes and put them in a list so that
	// they can be accessed via a random access method.  
	while (!done) {
		if (node == null) {
			done = true;
		}
		else {
			v.add(node);

			if (node.getType() == HydrologyNode.NODE_TYPE_END) {
				if (node.getCommonID().equals(id)) {
					// can't delete the end node.  
					return;
				}
				done = true;
			}
			if (!done) {
				node = getDownstreamNode(node, POSITION_COMPUTATIONAL);
			}
		}
	}

	int size = v.size();
	// the array will hold all the nodes except the one being deleted
	HydrologyNode[] nodes = new HydrologyNode[size - 1];
	HydrologyNode dsNode = null;
	int count = 0;
	// TODO SAM 2007-02-18 Evaluate whether needed
	//int tribNum = 0;
	//int upCount = 0;
	
	// The ids of the nodes upstream from the node to be deleted
	String[] delUsid = null;
	// The id of the node downstream from the node to be deleted
	String delDsid = null;
	
	int comp = -1;
	int serial = -1;

	// Loop through the list and put the nodes into the array for even faster access.
	for (int i = 0; i < size; i++) {
		node = v.get(i);
		
		if (IOUtil.testing()) {
			Message.printStatus(2, "", "  TSC (" 
				+ node.getCommonID() + "): " 
				+ node.getTributaryNumber() + " - " 
				+ node.getSerial() + " - "
				+ node.getComputationalOrder());
		}

		if (node.getCommonID().equals(id)) {
			// For the node that is to be deleted, store its
			// downstream node and the id of the downstream node, 
			// the IDs of its upstream nodes, its tributary number
			// and the count of the number of upstream nodes.
			dsNode = node.getDownstreamNode();
			delDsid = dsNode.getCommonID();
			
			delUsid = node.getUpstreamNodesIDs();
			//TODO SAM 2007-02-18 Evaluate whether needed
			//tribNum = node.getTributaryNumber();
			//upCount = node.getNumUpstreamNodes();

			comp = node.getComputationalOrder();
			serial = node.getSerial();
//			if (IOUtil.testing()) {
//				Message.printStatus(2, "", "Node comp: " + comp);
//				Message.printStatus(2, "", "Node serial: " + serial);
//			}
		}
		else {
			// for all other nodes, put them in the array
			nodes[count++] = node;
		}
	}

	//	if (IOUtil.testing()) {
	//		Message.printStatus(2, "", "delDsid: '" + delDsid + "'");
	//		if (delUsid != null) {
	//		for (int i = 0; i < delUsid.length; i++) {
	//			Message.printStatus(2, "", "delUsid[" + i + "]: '" + delUsid[i] + "'");
	//			}
	//		}
	//	}

	// to represent the different size of the Vector vs the array
	size--;

	// the ids of the upstream nodes from the node downstream of the node being deleted.
	String[] dsusid = dsNode.getUpstreamNodesIDs();

//	if (IOUtil.testing()) {
//		if (dsusid != null) {
//			for (int i = 0; i < dsusid.length; i++) {
//				Message.printStatus(2, "", "dsusid[" + i + "]: '" + dsusid[i] + "'");
//			}
//		}
//	}

	// Update the tributary number for all the nodes at the same level
	// as the deleted node.  That is, all the nodes upstream of the 
	// deleted node's downstream node.  Just update their tributary 
	// number to account for the deleted node.
	/*
	int tn = 1;	
	for (int i = 0; i < size; i++) {
		for (int j = 0; j < dsusid.length; j++) {
			// note here the comparison is with dsusid[]
			if (nodes[i].getCommonID().equals(dsusid[j])) {
				nodes[i].setTributaryNumber(tn++);
				if (IOUtil.testing()) {
					Message.printStatus(2, "", "1: Node '" + nodes[i].getCommonID() + "'"
						+ " trib set to: " + nodes[i].getTributaryNumber());
				}
			}
		}
	}
	*/
		
	// Update the tributary number for all the nodes upstream of the 
	// deleted node to account for the deleted node.
	/*
	for (int i = 0; i < size; i++) {
		for (int j = 0; j < delUsid.length; j++) {
			// note here the comparison is with delUsid[]
			if (nodes[i].getCommonID().equals(delUsid[j])) {	
				nodes[i].setTributaryNumber(tn++);
				if (IOUtil.testing()) {
					Message.printStatus(2, "", "2: Node '" + nodes[i].getCommonID() + "'"
						+ " trib set to: " +nodes[i].getTributaryNumber());
				}				
			}
		}
	}
	*/

	List<HydrologyNode> tempV = new Vector<HydrologyNode>();
	for (int i = 0; i < delUsid.length; i++) {
		for (int j = 0; j < size; j++) {
			if (nodes[j].getCommonID().equals(delUsid[i])) {
				tempV.add(nodes[j]);
				j = size + 1;
			}
		}
	}
	for (int i = 0; i < dsusid.length; i++) {
		for (int j = 0; j < size; j++) {
			if (nodes[j].getCommonID().equals(dsusid[i])) {
				tempV.add(nodes[j]);
				j = size + 1;
			}
		}
	}

	int[] serials = new int[tempV.size()];
	for (int i = 0; i < serials.length; i++) {
		serials[i] = tempV.get(i).getSerial();
	}

	int[] order = new int[serials.length];

	MathUtil.sort(serials, MathUtil.SORT_QUICK, MathUtil.SORT_ASCENDING, order, true);

	HydrologyNode tempNode = null;

	int tn = 1;
	for (int i = 0; i < order.length; i++) {
		tempNode = tempV.get(order[i]);
		tempNode.setTributaryNumber(tn++);
	}

	HydrologyNode ds = null;
	String[] usid = null;

	// loop through the entire network and change the serial counter and
	// computational order value.  All the nodes downstream of the node
	// being added need their computation order value incremented.  All
	// the nodes upstream of the node to be added need their serial counter decremented.
	for (int i = 0; i < size; i++) {
		if (nodes[i].getComputationalOrder() >= comp) {
			nodes[i].setComputationalOrder( nodes[i].getComputationalOrder() - 1);
		}
		if (nodes[i].getSerial() > serial) {
			nodes[i].setSerial(nodes[i].getSerial() - 1);
		}
	}

	// because the connections to the nodes will be rebuilt, loop through
	// the array and store the IDs of every nodes' upstream and downstream
	// nodes.  These stored IDs will be used to reconnect the network later.
	for (int i = 0; i < size; i++) {
		ds = nodes[i].getDownstreamNode();
		if (ds != null) {
			nodes[i].setDownstreamNodeID(ds.getCommonID());
		}
		usid = nodes[i].getUpstreamNodesIDs();
		if (usid != null) {
			nodes[i].clearUpstreamNodeIDs();
			for (int j = 0; j < usid.length; j++) {
				nodes[i].addUpstreamNodeID(usid[j]);
			}
		}
	}
	
	// break all the connections in the network.
	for (int i = 0; i < size; i++) {
		nodes[i].setDownstreamNode(null);
		nodes[i].setUpstreamNodes(null);
	}

	int pos = -1;

	// the above connections established via setting the ID still 
	// account for the node being deleted.  Loop through all the nodes
	// and break any connection to the deleted node as well, passing the
	// downstream links on the node downstream from the deleted node 
	// and the upstream links to the deleted node's upstream nodes.
	for (int i = 0; i < size; i++) {
		pos = -1;

		if (nodes[i].getDownstreamNodeID() != null 
		    && nodes[i].getDownstreamNodeID().equals(id)) {
			// if the node has a downstream node connection and 
			// that downstream node is the node to be deleted, 
			// then replace it with the id of the node downstream from the deleted node.
			/*
			The following change is made:
			   [DS]<--[ID]<--[US]
			to
			   [DS]<--[US]
			*/
			
//			if (IOUtil.testing()) {
//				Message.printStatus(2, "", "Rewiring " 
//					+ nodes[i].getCommonID() + " DS to " + delDsid + " (skipping " + id + ")");
//			}
			nodes[i].setDownstreamNodeID(delDsid);
		}

		usid = nodes[i].getUpstreamNodeIDs();
		if (usid != null) {
			// if the node has any upstream node connections, and
			// if any of them connect to the node to be deleted
			// record the deleted node's position within the array of upstream ids.
			for (int j = 0; j < usid.length; j++) {
				if (usid[j].equals(id)) {
//					if (IOUtil.testing()) {
//						Message.printStatus(2, "",
//							"" 
//							+ nodes[i].getCommonID()
//							+ " has an us node "
//							+ "(" + j 
//							+ ") that points to " 
//							+ id);
//					}
					pos = j;
				}
			}
		}

		// pos will be > -1 if the node to be deleted is an upstream node of the current node.
		if (pos > -1) {
			// need to reroute upstream connections around the
			// node to be deleted.  The position occupied in
			// the upstream node Vector by the deleted node
			// will in turn be filled by its upstream nodes, like in the following:
			/*
			        [US1.1]  [US2.1]
			       /        /
			   [DS]<----[ID]
			       \        \
			        [US1.2]  [US2.2]

			to
			         [US1.1]
                                /          [US1.2*]
                               /          /
			   [DS]----------<
			       \          \
			        \          [US1.3*]
				 [US1.4*]

			* - note that the nodes are now iterated in the order
			    1.1, 2.1, 2.2, 1.2.  2.1, 2.2 and 1.2 have been
			    renumbered to 1.2, 1.3, and 1.4, respectively, 
			    to account for the new order of the upstream nodes.
			*/			 
//			if (IOUtil.testing()) {
//				Message.printStatus(2, "", "" + id + " is an upstream node to be deleted from " 
//					+ nodes[i].getCommonID());
//			}
			nodes[i].clearUpstreamNodeIDs();
			for (int j = 0; j < pos; j++) {	
				// for all the nodes in the upstream array that come before the deleted node, nothing
				// needs changed.  Add them as normal.  In the above diagram, these nodes are equivalent
				// to 1.1.
				nodes[i].addUpstreamNodeID(usid[j]);

//				if (IOUtil.testing()) {
//					Message.printStatus(2, "", 
//						" [1] - adding " 
//						+ usid[j] + " upstream to " 
//						+ nodes[i].getCommonID());
//				}
			}
			for (int j = 0; j < delUsid.length; j++) {
				// at this point these nodes are being added
				// at the position of the deleted node.  These
				// are the upstream nodes of the deleted node.
				// In the above diagram, these nodes are
				// equivalent to 2.1/1.2 and 2.2/1.3.
				nodes[i].addUpstreamNodeID(delUsid[j]);

//				if (IOUtil.testing()) {
//					Message.printStatus(2, "", 
//						" [2] - adding " 
//						+ delUsid[j] + " upstream to " 
//						+ nodes[i].getCommonID());
//				}
			}
			for (int j = pos + 1; j < usid.length; j++) {
				// now add the rest of the original upstream
				// nodes, which now are after the upstream
				// nodes inherited from the deleted node.
				// In the above diagram, these nodes are
				// equivalent to 1.2/1.4.
				nodes[i].addUpstreamNodeID(usid[j]);

//				if (IOUtil.testing()) {
//					Message.printStatus(2, "", 
//						" [3] - adding " 
//						+ usid[j] + " upstream to " 
//						+ nodes[i].getCommonID());
//				}
			}
		}
	}

	// finally, the nodes are actually re-linked to one another by means
	// of the stored IDs.

	String dsid = null;
	for (int i = 0; i < size; i++) {
		dsid = nodes[i].getDownstreamNodeID();
		usid = nodes[i].getUpstreamNodeIDs();

//		if (IOUtil.testing()) {
//			Message.printStatus(2, "", "  '" 
//				+ nodes[i].getCommonID() + "'");
//			Message.printStatus(2, "", "     DS: '" + dsid + "'");
//			for (int j = 0; j < usid.length; j++) {
//				Message.printStatus(2, "", "     US[" + j 
//					+ "]: '" + usid[j] + "'");
//			}
//		}

		// get the downstream and upstream node IDs and then loop 
		// through all the nodes, connecting the nodes with matching 
		// IDs to the currently-iterating node.

		if (dsid != null && !dsid.equalsIgnoreCase("null")) {
			for (int j = 0; j < size; j++) {
				if (nodes[j].getCommonID().equals(dsid)) {
					nodes[i].setDownstreamNode(nodes[j]);
					j = size + 1;
				}
			}
		}
		
		for (int j = 0; j < usid.length; j++) {
//			if (IOUtil.testing()) {
//				Message.printStatus(2, "", 
//					"Connecting '" + usid[j] + "'");
//			}
			for (int k = 0; k < size; k++) {	
//				if (IOUtil.testing()) {
//					Message.printStatus(2, "", 
//						"    [" + k + "]: '"
//						+ nodes[k].getCommonID() + "'");
//				}
				if (nodes[k].getCommonID().equals(usid[j])) {
					nodes[i].addUpstreamNode(nodes[k]);
					k = size + 1;
				}
			}
		}
		if (IOUtil.testing()) {
			Message.printStatus(2, "", "  TSC (" 
				+ nodes[i].getCommonID() + "): " 
				+ nodes[i].getTributaryNumber() + " - " 
				+ nodes[i].getSerial() + " - "
				+ nodes[i].getComputationalOrder());
		}
	}
	
	// put the nodes into a vector in order to use the setNetworkFromNodes() method.

	v = new Vector<HydrologyNode>();
	for (int i = 0; i < size; i++) {
		v.add(nodes[i]);
	}	

	setNetworkFromNodes(v);
}

/**
Gets the extents of the nodes in the network in the form of GRLimits, in network plotting coordinates
(NOT alternative coordinates).  This does NOT consider the page layout as if editing with the editor.
If any nodes have missing X or Y values, their values will not be considered in 
determining the extents of the network.  If no nodes have locations, the
GRLimits returned will be GRLimits(0, 0, 1, 1);
@return the GRLimits that represent the bounds of the nodes in the network.
*/
public GRLimits determineExtentFromNetworkData( )
{
	double lx = Double.NaN;
	double rx = Double.NaN;
	double by = Double.NaN;
	double ty = Double.NaN;	
	// TODO -- eliminate the need for hold nodes -- they signify an error in the network.
	HydrologyNode holdNode = null;

	HydrologyNode node = getMostUpstreamNode();	

	boolean done = false;

	double x, y;
	while (!done) {
		x = node.getX();
		y = node.getY();
		if ( !Double.isNaN(x) ) {
			if ( Double.isNaN(lx) || (x < lx) ) {
				lx = x;
			}
			if ( Double.isNaN(rx) || (x > rx) ) {
				rx = x;
			}
		}
		if ( !Double.isNaN(y)) {
			if ( Double.isNaN(by) || (y < by) ) {
				by = y;
			}
			if ( Double.isNaN(ty) || (y > ty) ) {
				ty = y;
			}
		}
			
		if (node.getType() == HydrologyNode.NODE_TYPE_END) {
			done = true;
		}		
		else if (node == holdNode) {
			done = true;
		}

		holdNode = node;	
		node = getDownstreamNode(node, POSITION_COMPUTATIONAL);		
	}

	int count = 0;

	// Check to make sure that the bounds were determined properly.
	// If for any reason none of the nodes have valid values, then 
	// the rest of the code screw up.  If any of the following are true ...

	if (lx == Double.MAX_VALUE) {
		count++;
	}
	if (rx == Double.MIN_VALUE) {
		count++;
	}
	if (by == Double.MAX_VALUE) {
		count++;
	}
	if (ty == Double.MIN_VALUE) {
		count++;
	}

	if (count != 0) {
		return new GRLimits(0, 0, 1, 1);
	}

	return new GRLimits(lx, by, rx, ty);
}

/**
Fills in missing locations downstream between the two nodes.
@param node the node from which to fill downstream locations.
@param ds the first downstream node with a valid location, or null if none do.
@param dist the dist between the two nodes (in count of nodes).
*/
protected void fillDownstream(HydrologyNode node, HydrologyNode ds, int dist) {
	boolean done = false;
	HydrologyNode holdNode;
	HydrologyNode wNode = node;

	// get the location of the first node
	double X1 = wNode.getX();
	double Y1 = wNode.getY();
	double X2 = -1;
	double Y2 = -1;

	// if there is a downstream node with locations, get the location of the downstream node
	if (ds != null) {
		X2 = ds.getX();
		Y2 = ds.getY();
	}

	// default to use if no downstream node available
	double xSep = __nodeSpacing;
	double ySep = __nodeSpacing;

	// if a location is known for both the upstream and downstream node,
	// determine the amount of distancing for all the nodes in-between.
	// otherwise, xSep and ySep will be used
	if (X2 >= 0 && Y2 >= 0) {
		xSep = ((double)(X1 - X2) / (double)(dist + 1));
		ySep = ((double)(Y1 - Y2) / (double)(dist + 1));

		if (X1 > X2) {
			if (xSep > 0) {
				xSep *= -1;
			}
		}
		else {
			if (xSep < 0) {
				xSep *= -1;
			}
		}
		if (Y1 > Y2) {
			if (ySep > 0) {
				ySep *= -1;
			}
		}
		else {
			if (ySep < 0) {
				ySep *= -1;
			}
		}
	}

	// now go through all the nodes from node on downstream and fill in the locations

	int count = 1;
	while (!done) {
	// TODO -- eliminate the need for hold nodes -- they signify an error in the network.
		holdNode = wNode;	
		// get the next node
		wNode = wNode.getDownstreamNode();

		if (wNode != holdNode && wNode != ds) {
			wNode.setX(X1 + (xSep * count));
			wNode.setY(Y1 + (ySep * count));
	
			if (wNode.getType() == HydrologyNode.NODE_TYPE_END) {
				done = true;
			}		
		}
		else {
			done = true;
		}

		// used to calculate the spacing between nodes
		count++;
	}
}

/**
Fills in the locations for nodes not on the main stem that are not downstream
of a node with valid locations.  Their locations are filled in by extrapolating
from downstream node locations.
@param node the node to which to extrapolate locations.  Any other nodes between
this node and the first node downstream with a valid location will also have
their locations filled.
*/
protected void fillFromDownstream(HydrologyNode node) {
	HydrologyNode ds = node.getDownstreamNode();

	if (ds == null) {
		// should not happen ...
		return;
	}

	boolean done = false;
	boolean found = false;
	int count = 0;
	while (!done) {
		// count the number of nodes between the passed-in node and
		// the first downstream node with a valid location
		count++;
		if (ds.getX() >= 0 && ds.getY() >= 0) {
			found = true;
			done = true;
		}

		if (!done) {
			// this will hold the first downstream node with a valid location
			ds = ds.getDownstreamNode();
		}

		if (ds == null) {
			done = true;
		}
	}

	// calculate the spacing between the first valid downstream node
	// and its first downstream node.  This delta will be used to 
	// space out the upstream nodes, too.  

	// If for some reason the first valid downstream node doesn't have
	// a downstream node, __SPACING will be used to extrapolate the
	// upstream node locations, instead.


	double dsX = __lx + __nodeSpacing;
	double dsY = __by + __nodeSpacing;
	if (found == true) {
		dsX = ds.getX();
		dsY = ds.getY();
	}
	double dx = 0;
	double dy = 0;

	HydrologyNode dsds = null;
	if (found == true) {
		dsds = ds.getDownstreamNode();
	}
	if (dsds == null) {
		dx = __nodeSpacing;
		dy = __nodeSpacing;
	}
	else {
		dx = dsX - dsds.getX();
		dy = dsY - dsds.getY();
	}
	HydrologyNode temp = node;
	for (int i = 0; i < count; i++) {
		temp.setX(dsX + (dx * (count - i)));
		temp.setY(dsY + (dy * (count - i)));
		temp = temp.getDownstreamNode();
	}
}

/**
Fills the locations of the nodes in the network, interpolating if necessary, 
and looking up from the database if possible.
@param dmi the dmi to use for talking to the database.  Should be open and non-null.
@param interpolate whether node locations should be interpolated, or just
looked up from the database.
@param limits if interpolating, the limits to use as the far bounds of the network.
*/
/* FIXME SAM 2008-03-15 Need to move to StateDMI
public void fillLocations(HydroBaseDMI dmi, boolean interpolate, 
GRLimits limits) {
	double lx, rx, by, ty;
	if (limits == null) {
		limits = getExtents();
	}

	lx = limits.getLeftX();
	by = limits.getBottomY();
	rx = limits.getRightX();
	ty = limits.getTopY();

	// REVISIT -- eliminate the need for hold nodes -- they signify an
	// error in the network.
	HydrologyNode holdNode = null;
	HydrologyNode node = getMostUpstreamNode();	
	boolean done = false;
	double[] loc = null;
	while (!done) {
		loc = lookupStateModNodeLocation(dmi, node.getCommonID());
		if (DMIUtil.isMissing(node.getX())) {
			node.setX(loc[0]);
			node.setDBX(loc[0]);
		}
		
		if (DMIUtil.isMissing(node.getY())) {
			node.setY(loc[1]);
			node.setDBY(loc[1]);
		}	
	
		if (node.getType() == Node.NODE_TYPE_END) {
			done = true;
		}		
		else if (node == holdNode) {
			done = true;
		}

		holdNode = node;	
		node = getDownstreamNode(node, POSITION_COMPUTATIONAL);		
	}

	if (!interpolate) {
		return;
	}

	__lx = lx;
	__by = by;

	if ((rx - lx) > (ty - by)) {
		__SPACING = (ty - by) * 0.06;
	}
	else {
		__SPACING = (rx - lx) * 0.06;
	}

	// fills in any missing locations for all the nodes on the main stream
	// stem.
	fillMainStemLocations();

	// fills in missing locations for any node upstream of the main
	// stem.
	fillUpstreamLocations();

	finalCheck(lx, by, rx, ty);
}
*/

/**
Fills in the missing location for all nodes on the main stem.
*/
protected void fillMainStemLocations() {
	boolean done = false;
	boolean firstFound = false;
	HydrologyNode ds = null;
	HydrologyNode first = null;
	HydrologyNode node = getMostUpstreamNode();
	HydrologyNode holdNode = node;
	int count = 0;
	int dl = 0;
	int upstreamInvalidCount = 0;
	int validCount = 0;
	List<Object> dv;

	while (!done) {
		if (node.getReachLevel() != 1) {
			// ignore node not on the main stem
		}
		else {
			// count all the nodes on the main stem
			count++;

			if (node.getX() >= 0 && node.getY() >= 0) {
				// the first node (going downstream on the 
				// main stem) with a valid location has been found
				if (!firstFound) {
					first = node;
					firstFound = true;
				}

				// get the first valid downstream node -- that 
				// is, the first downstream node with actual locations.
				// See the docs for getValidDownstreamNode() to understand the returned values.
				dv = getValidDownstreamNode(node, true);
				ds = (HydrologyNode)dv.get(0);
				dl = ((Integer)dv.get(1)).intValue();
 
				// if the distance to the first valid downstream
				// node is greater than 1 then there are nodes 
				// with missing location values between this 
				// node and the first valid downstream node.
				if (dl > 1) {
					// fill in the locations for the downstream nodes.
					fillDownstream(node, ds, dl);
				}

				// count all the nodes on the main stem with valid locations
				validCount++;
			}
			else {
				// if firstFound == false then no nodes with valid locations have been found yet
				// on the main stem.
				if (!firstFound) {
					// keep track of the number of nodes with invalid locations upstream of
					// the first valid node on the main stem.
					upstreamInvalidCount++;
				}
			}
		}

		if (node.getType() == HydrologyNode.NODE_TYPE_END) {
			done = true;
		}		

		node = getDownstreamNode(node, POSITION_RELATIVE);
	// TODO -- eliminate the need for hold nodes -- they signify an error in the network.
		if (holdNode == node) {
			done = true;
		}
		holdNode = node;			
	}

	// validCount stores the number of nodes on the main stem with
	// valid locations.  count stores the total number of nodes on the main stem.

	if (validCount < count) {
		// not all nodes were filled.  At this point there may be
		// nodes at the very "front" or "back" of the main stem
		// with missing locations.  Their locations will be extrapolated.
	}
	else {
		// all the main stem nodes have valid locations.  Nothing
		// needs to be inter/extrapolated.  Best-case scenario, and everything is already done.
		return;
	}

	if (validCount == 1) {
		// if only a single node on the main stem has a valid
		// location then nothing can be extrapolated.  The others
		// will simply be spaced out from the single node with
		// a valid location.

		// the node 'first' stores the location of the node on
		// the main stem with a valid location.
	
		// upstreamInvalidCount stores the number of nodes upstream
		// of the one valid node that do not have a valid location.
	
		// as the main stem is traversed, keeps track of whether
		// currently upstream or downstream of the one valid node
		boolean upstream = true;

		int downstreamCount = 0;
	
		node = getMostUpstreamNode();

		done = false;
		while (!done) {
			if (node.getReachLevel() != 1) {
				// ignore nodes not on the main stem
			}
			else {
				if (node.getX() >= 0 && node.getY() >= 0) {
					// ignore -- this is the single valid
					// node.  At this point, the rest
					// of the nodes that get traversed
					// are downstream.
					upstream = false;
				}
				else if (upstream) {
					//Message.printStatus(2, "", 
					//	"  Setting upstream for '" 
					//	+ node.getLabel()+ "'");
					node.setX(first.getX() - (__nodeSpacing * upstreamInvalidCount));
					node.setY(first.getY() - (__nodeSpacing * upstreamInvalidCount));
					upstreamInvalidCount--;
				}
				else if (!upstream) {
					//Message.printStatus(2, "", 
					//	"  Setting downstrm for '" 
					//	+ node.getLabel()+ "'");
					downstreamCount++;
					node.setX(first.getX() + (__nodeSpacing * downstreamCount));
					node.setY(first.getY() + (__nodeSpacing	* downstreamCount));
				}
			}

			node = getDownstreamNode(node, POSITION_RELATIVE);
	
	// TODO -- eliminate the need for hold nodes -- they signify an error in the network.
			if (holdNode == node) {
				done = true;
			}
			holdNode = node;			
		}

		// everything is done -- all the main stem nodes have locations
		return;
	}

	// at this point there are definitely more than a single node with
	// valid locations, so the delta between the first and the node
	// next to it can be used to fill the nodes upstream.  The delta
	// between the last valid node and the one next to it can be used
	// to fill the rest of the downstream nodes

	// All the other locations were filled in above with fillDownstream().
	// The tail (most downstream) or head (most upstrem) of the stem may
	// not have any valid locations but everything in between definitely
	// does.

	if (first == null) {
		// TODO (JTS - 2004-05-25) triggered by a diagram with just one node, I believe.
		return;
	}
	
	// finds the last two (farthest downstream) main stem nodes with 
	// valid locations.  The diagram is guaranteed at this point to always have >1 valid node.
	HydrologyNode[] lasts = findLastMainStemValidNodes();
	HydrologyNode firstNext = first.getDownstreamNode();

	// determine the delta between the first valid node and its neighbor.
	// This will be used to extrapolate the upstream nodes.
	double upDX = first.getX() - firstNext.getX();
	if (upDX == 0) {
		upDX = __nodeSpacing;
	}
	double upDY = first.getY() - firstNext.getY();
	if (upDY == 0) {
		upDY = __nodeSpacing;
	}

	// determine the delta between the last valid node and its neighbor.
	// This will be used to extrapolate the downstream nodes.
	double downDX = lasts[0].getX() - lasts[1].getX();
	if (downDX == 0) {
		downDX = __nodeSpacing;
	}
	double downDY = lasts[0].getY() - lasts[1].getY();
	if (downDY == 0) {
		downDY = __nodeSpacing;
	}

	// the node 'first' stores the location of the node on the main stem with a valid location.
	
	// upstreamInvalidCount stores the number of nodes upstream
	// of the one valid node that do not have a valid location.

	// as the main stem is traversed, keeps track of whether
	// currently upstream or downstream of the one valid node
	boolean upstream = true;

	int downstreamCount = 0;
	
	node = getMostUpstreamNode();
	done = false;
	while (!done) {
		if (node.getReachLevel() != 1) {
			// ignore nodes not on the main stem
		}
		else {
			if (node.getX() >= 0 && node.getY() >= 0) {
				// ignore nodes with valid locations, though 
				// this does mark that the last of the
				// upstream nodes with invalid locations have been traversed.
				upstream = false;
			}
			else if (upstream) {
				//Message.printStatus(2, "", 
				//	"  Setting upstream for '" 
				//	+ node.getLabel()+ "'");
				node.setX(first.getX() - (upDX * upstreamInvalidCount));
				node.setY(first.getY() - (upDY * upstreamInvalidCount));
				upstreamInvalidCount--;
			}
			else if (!upstream) {
				//Message.printStatus(2, "", 
				//	"  Setting downstrm for '" 
				//	+ node.getLabel()+ "'");
				downstreamCount++;
				node.setX(lasts[0].getX() + (downDX * downstreamCount));
				node.setY(lasts[0].getY() + (downDY * downstreamCount));
			}
		}
		node = getDownstreamNode(node, POSITION_RELATIVE);

	// TODO -- eliminate the need for hold nodes -- they signify an error in the network.
		if (holdNode == node) {
			done = true;
		}
		holdNode = node;			
	}
}

/**
Interpolates the locations for nodes downstream of the specified node without valid locations.  
@param node a node on a reach other than the main stem which has a valid location.
*/
protected void fillReachDownstream(HydrologyNode node) {
	HydrologyNode ds = node.getDownstreamNode();

	if (ds == null) {
		// this should not be null ...
		return;
	}

	if (ds.getX() >= 0 && ds.getY() >= 0) {
		// there are no nodes immediately downstream of this node
		// without invalid locations before a node with a valid location is found.  Nothing to do.
		return;
	}
	
	boolean done = false;
	int count = 0;
	while (!done) {
		// count the number of nodes immediately downstream of this one with invalid locations.
		count++;
		if (ds.getX() >= 0 && ds.getY() >= 0) {
			done = true;
		}

		if (!done) {
			// this will store the first node downstream of 
			// the passed-in node with a valid location.
			ds = ds.getDownstreamNode();
			if ( ds == null ) {
				break;
			}
		}
	}

	// get the location of the passed-in node and the location of the
	// first valid node downstream and interpolate the locations for all the intermediate nodes.

	if ( ds == null ) {
		// TODO SAM 2009-01-21 Evalute this and the break in the loop - had to put this code in
		// to avoid null point exceptions but maybe logic can be more robust
		return;
	}
	double x = node.getX();
	double dsX = ds.getX();
	
	double y = node.getY();
	double dsY = ds.getY();

	double dx = (x - dsX) / count;
	double dy = (y - dsY) / count;

	HydrologyNode temp = node.getDownstreamNode();
	// iterate to (count - 1) so that the location for the first valid
	// downstream node is not changed, too.
	for (int i = 0; i < count - 1; i++) {
		temp.setX(node.getX() - (dx * (i + 1)));
		temp.setY(node.getY() - (dy * (i + 1)));
		temp = temp.getDownstreamNode();
	}
}

/**
Fills missing locations for all the nodes upstream of the main stem.
*/
protected void fillUpstreamLocations() {
	boolean done = false;
	HydrologyNode holdNode;
	HydrologyNode node = getMostUpstreamNode();	

	while (!done) {
	// TODO -- eliminate the need for hold nodes -- they signify an error in the network.
		holdNode = node;	
		// get the next node
		node = getDownstreamNode(node, POSITION_COMPUTATIONAL);

		if (node != holdNode) {
			// if this node has a valid location, then try 
			// filling any downstream nodes that do not have
			// valid locations.  All main stem nodes have 
			// valid locations already, so this is always
			// guaranteed to hit a node downstream with a
			// location so that interpolation can be done.
			if (node.getX() >= 0 && node.getY() >= 0) {
				fillReachDownstream(node);
			}
	
			if (node.getType() == HydrologyNode.NODE_TYPE_END) {
				done = true;
			}		
		}
		else {
			done = true;
		}
	}

	done = false;
	node = getMostUpstreamNode();	
	while (!done) {
	// TODO -- eliminate the need for hold nodes -- they signify an error in the network.
		holdNode = node;	
		// get the next node
		node = getDownstreamNode(node, POSITION_COMPUTATIONAL);

		if (node != holdNode) {
			// if this node does not have a valid location then
			// it is upstream of a node with a valid location
			// and was not interpolated downstream.  It will
			// be extrapolated from node locations downstream 
			// of it.
			if (node.getX() < 0 || node.getY() < 0) {
				fillFromDownstream(node);
			}
	
			if (node.getType() == HydrologyNode.NODE_TYPE_END) {
				done = true;
			}		
		}
		else {
			done = true;
		}
	}
}

/**
Performs a final check on all the nodes prior to the diagram being shown to
guarantee that all locations have been assigned.
@param lx the left-most X coordinate
@param by the bottom-most Y coordinate
@param rx the right-most X coordinate
@param ty the top-most Y coordinate
*/
public void finalCheck(double lx, double by, double rx, double ty) {
	finalCheck(lx, by, rx, ty, true);
}

/**
Performs a final check on all the nodes prior to the diagram being shown to
guarantee that all locations have been assigned.
@param lx the left-most X coordinate
@param by the bottom-most Y coordinate
@param rx the right-most X coordinate
@param ty the top-most Y coordinate
@param setBoundsTo0 if true, then the left-most and bottom-most bounds of any
node will not be allowed to be less than 0.
*/
public void finalCheck(double lx, double by, double rx, double ty, boolean setBoundsTo0) {
	HydrologyNode node = getMostUpstreamNode();	
	HydrologyNode holdNode = null;
	boolean done = false;
	double w = rx - lx;
	double h = ty - by;
	double w5p = w * 0.05; // 5% of width
	double h5p = h * 0.05; // 5% of height

	String message = "";
	while (!done) {
		if (node.getType() == HydrologyNode.NODE_TYPE_END) {
			done = true;
		}		
	// TODO -- eliminate the need for hold nodes -- they signify an error in the network.
		if (node == holdNode) {
			done = true;
		}
		else {
			message = "";
			if (setBoundsTo0) {
				if (node.getX() < 0) {
					node.setX(lx + w5p);
					message += "   " + node.getX() + "<0";
				}
				if (node.getY() < 0) {
					node.setY(by + h5p);
					message += "   " + node.getY() + "<0";
				}
			}
			else {
				// This is only done for networks that were read in from XML.
				// TODO (JTS - 2004-11-11)
				// this code was not done originally in 
				// the finalCheck() method, so just to make
				// sure that it doesn't break anything
				// I'm making it not be called for networks
				// that called the original finalCheck().
				// check it out later and see if it can be
				// enabled for all networks, even those that
				// set setBoundsTo0 to true.
				if (node.getX() < lx) {
					message += "   " + node.getX() + " < " + lx;
					node.setX(lx + w5p);
				}
				if (node.getX() > rx) {
					message += "   " + node.getX() + " > " + rx;
					node.setX(rx - w5p);
				}
				if (node.getY() < by) {
					message += "   " + node.getY() + " < " + by;
					node.setY(by + h5p);
				}
				if (node.getY() > ty) {
					message += "   " + node.getY() + " > " + ty;
					node.setY(ty - h5p);
				}
			}

			message = message.trim();

			if (!message.equals("")) {
				Message.printStatus(2, "", "Setting '"
					+ node.getCommonID() + "' in " + "finalCheck (" + message + ")");
			}
		}
		
		holdNode = node;	
		node = getDownstreamNode(node, POSITION_COMPUTATIONAL);		
	}
}

/**
Finalize before garbage collection.
*/
protected void finalize()
throws Throwable {
	__nodeHead = null;
	__checkFP = null;
	__newline = null;
	__annotationList = null;
	__layoutList = null;
	__linkList = null;
	__title = null;

	super.finalize();
}

/**
Find the downstream natural flow node in the reach, given a starting node.  This is
used in WIS to find a downstream node to use for computations.
This method does consider dry nodes as natural flow nodes if the flag has been turned on.
@return HydroBase_Node for upstream natural flow node in a reach.
@param node Starting HydroBase_Node in network (some node in a reach).
@see StateMod_NodeNetwork#treatDryNodesAsNaturalFlow()
*/
public HydrologyNode findDownstreamNaturalFlowNodeInReach(HydrologyNode node) {
	// Just move downstream in the reach until we find a natural flow
	// node.  Return null if we get to the bottom of the reach and there is no natural flow node...

	for (HydrologyNode nodePt = getDownstreamNode(node,POSITION_RELATIVE);
		((nodePt != null) && (nodePt.getReachCounter() == node.getReachCounter()));
		nodePt = getDownstreamNode(nodePt, POSITION_RELATIVE)) {
		if (__treatDryAsNaturalFlow) {
			// First check for a dry node.
			if (nodePt.getIsDryRiver()) {
				return nodePt;
			}
		}
		if (nodePt.getIsNaturalFlow()) {
			return nodePt;
		}
		if (nodePt.getDownstreamNode() == null) {
			break;
		}
	}
	// Unable to find it...
	return null;
}

/**
Finds the downstream flow node for the given node.
@param node the node for which to find the downstream flow node.
@return the downstream flow node for the specified node.
*/
public HydrologyNode findDownstreamFlowNode(HydrologyNode node) {
	String routine = "HydroBase_NodeNetwork.findDownstreamFlowNode";

	HydrologyNode nodeDownstreamFlow = null;
	HydrologyNode nodePrev;
	HydrologyNode nodePt;
	int dl = 12;

	if (Message.isDebugOn) {
		Message.printDebug(dl, routine,
			"Trying to find downstream FLOW node for \"" + node.getCommonID() + "\"");
	}
	
	nodePt = node;
	for ( nodePrev = nodePt, nodePt = getDownstreamNode(nodePt, POSITION_RELATIVE); nodePt != null;
		nodePrev = nodePt,
		nodePt = getDownstreamNode(nodePt, POSITION_RELATIVE)) {
		if (Message.isDebugOn) {
			Message.printDebug(dl, routine,
				"Checking node \"" + nodePt.getCommonID()
				+ "\"(previous \"" + nodePrev.getCommonID()
				+ "\" node_in_reach=" 
				+ nodePrev.getNodeInReachNumber()
				+ ")");
		}
		if (nodePrev.equals(nodePt)) {
			// Have gone to the bottom of the system and there is no downstream flow node.  Return null.
			Message.printDebug(1, routine,
				"Node \"" + node.getCommonID() + "\" has no downstream flow node");
			return null;
		}
		// This originally worked for makenet and should work now for
		// the admin tool since we are using natural flow switch.
		else if ((nodePt.getType() == HydrologyNode.NODE_TYPE_FLOW) && nodePt.getIsNaturalFlow()) {
			// Have a downstream flow node...
			nodeDownstreamFlow = nodePt;
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine, "For \"" + node.getCommonID()
					+ "\", downstream flow node is \"" + nodeDownstreamFlow.getCommonID()
					+ "\" A*P=" + nodeDownstreamFlow.getWater());
			}
			return nodePt;
		}
	}
	return null;
}

/**
Looks up the X and Y coordinates for a record in the geoloc table.
@param geoloc_num the geoloc_num of the record in the geoloc table.
@return a two-element double array, the first element of which is the X location
and the second element of which is the Y location.
*/
/* FIXME SAM 2008-03-15 Need to move to StateDMI
private double[] findGeolocCoordinates(int geoloc_num) {
	double[] coords = new double[2];
	coords[0] = -1;
	coords[1] = -1;

	if (!__isDatabaseUp) {
		return coords;
	}

	try {
		HydroBase_Geoloc g = __dmi.readGeolocForGeoloc_num(geoloc_num);
		if (g != null) {
			coords[0] = g.getUtm_x();
			coords[1] = g.getUtm_y();
		}
		return coords;
	}
	catch (Exception e) {
		Message.printWarning(2, "", e);
		return coords;
	}
}
*/

/**
Finds the highest serial number value in the network, starting from the 
specified node.  Used to determine the serial number for a new node added to the network.
@param node the node from which to begin searching for the highest serial value.  Can be null.
@param highest the highest value so far found in the search.
@return the highest serial number value in the network, starting with the
specified node and going through all the nodes upstream of it.
*/
private int findHighestUpstreamSerial(HydrologyNode node, int highest) {
	if (node == null) {
		return highest;
	}

	int serial = node.getSerial();
	if (serial > highest) {
		highest = serial;
	}

	List<HydrologyNode> v = node.getUpstreamNodes();
	if (v != null) {
		int size = v.size();
		int temp = -1;
		HydrologyNode tempNode = null;
		for (int i = 0; i < size; i++) {
			tempNode = v.get(i);
			temp = findHighestUpstreamSerial(tempNode, highest);
			if (temp > highest) {
				highest = temp;
			}
		}
	}
	return highest;
}

/**
Finds the two most-downstream consecutive nodes on the main stem that have valid locations.
@return the two most-downstream consecutive nodes with valid locations.  The
array is a two-element array.  The first element is the most-downstream node
with a valid location and the second element is the node immediately upstream
from that node, but still on the main stem.  If the second element is null
then there is only one node in the main stem with a valid location.
*/
private HydrologyNode[] findLastMainStemValidNodes() {
	HydrologyNode node = getMostUpstreamNode();
	HydrologyNode prehold = null;
	HydrologyNode holdValid = null;
	HydrologyNode holdNode = null;

	boolean done = false;
	while (!done) {
		if (node.getReachLevel() != 1) {
			// ignore node
		}
		else {
			if (node.getX() >= 0 && node.getY() >= 0) {
				// prehold holds the value of the node
				// with a valid location immediately upstream
				// of the last node found.
				prehold = holdValid;

				// holdValid holds the last node found with a valid location
				holdValid = node;
			}
		}
		node = getDownstreamNode(node, POSITION_RELATIVE);

	// TODO -- eliminate the need for hold nodes -- they signify an error in the network.
		if (holdNode == node) {
			done = true;
		}
		holdNode = node;			
	}

	HydrologyNode[] nodes = new HydrologyNode[2];
	nodes[0] = holdValid;
	nodes[1] = prehold;
	return nodes;
}

/**
Returns the next downstream node from the specified node that is a physical 
node.  This is used, for example, when outputting StateMod data, which does
not recognize confluence and other nodes that are only used in the diagram.
@param node the node from which to check downstream
@return the next downstream node that is a physical node.
*/
public static HydrologyNode findNextRealDownstreamNode(HydrologyNode node) {
	HydrologyNode nodePt = node;

	while (true) {
		nodePt = getDownstreamNode(nodePt, POSITION_RELATIVE);
		if ((nodePt.getType() != HydrologyNode.NODE_TYPE_BLANK)
			&& (nodePt.getType() != HydrologyNode.NODE_TYPE_CONFLUENCE)
			&& (nodePt.getType() != HydrologyNode.NODE_TYPE_XCONFLUENCE)
			&& (nodePt.getType() != HydrologyNode.NODE_TYPE_STREAM)
			&& (nodePt.getType() != HydrologyNode.NODE_TYPE_LABEL)
			&& (nodePt.getType() != HydrologyNode.NODE_TYPE_FORMULA)) {
			return nodePt;
		}
	}
}

/**
Returns the next downstream node from the specified node that is a physical 
node or an XConfluence node.  This is needed when processing the StateMod river network file.
@param node the node from which to check downstream
@return the next downstream node that is a physical node or an XConfluence node.
*/
public static HydrologyNode findNextRealOrXConfluenceDownstreamNode( HydrologyNode node) {	
	HydrologyNode nodePt = node;

	while (true) {
		nodePt = getDownstreamNode(nodePt, POSITION_RELATIVE);
		if ((nodePt.getType() != HydrologyNode.NODE_TYPE_BLANK)
			&& (nodePt.getType() != HydrologyNode.NODE_TYPE_CONFLUENCE)
			&& (nodePt.getType() != HydrologyNode.NODE_TYPE_STREAM)
			&& (nodePt.getType() != HydrologyNode.NODE_TYPE_LABEL)
			&& (nodePt.getType() != HydrologyNode.NODE_TYPE_FORMULA)) {
			return nodePt;
		}
	}
}

/**
Returns the next downstream node from the specified node that is an XConfluence node.  
@param node the node from which to check downstream
@return the next downstream node that is a physical node or an XConfluence node.
*/
public static HydrologyNode findNextXConfluenceDownstreamNode(HydrologyNode node) {	
	HydrologyNode nodePt = node;

	while (true) {
		nodePt = getDownstreamNode(nodePt, POSITION_RELATIVE);
		if (nodePt == null || nodePt.getType() == HydrologyNode.NODE_TYPE_END) {
			return null;
		}

		if (nodePt.getType() != HydrologyNode.NODE_TYPE_XCONFLUENCE) {
			// do nothing, go on to the next node ...
		}
		else {
			return nodePt;
		}
	}
}

/**
Find a node matching some criteria.  This is a general-purpose node search 
routine that will expand over time as needed.
@return HydroBase_Node that is found or null if none is found.
@param dataTypeToFind Types of node data to find.  See NODE_DATA_* definitions.
*/
public HydrologyNode findNode(int dataTypeToFind, int nodeTypeToFind, String dataValueToFind)
{	
	String routine = "HydroBase_NodeNetwork.findNode";
	int dl = 30;

	// Work from the top of the system...
	if (Message.isDebugOn) {
		Message.printDebug(dl, routine,
			"Finding data type " + dataTypeToFind + " node type " 
			+ HydrologyNode.getTypeString(nodeTypeToFind,1) + " value \"" + dataValueToFind + "\"");
	}
	for (HydrologyNode nodePt = getUpstreamNode(__nodeHead, POSITION_ABSOLUTE);
		nodePt.getDownstreamNode() != null;
		nodePt = getDownstreamNode(nodePt, POSITION_COMPUTATIONAL)) {
		// TODO sam 2017-03-15 why is the following indicated as dead code in Eclipse?
		if (nodePt == null) {
			break;
		}
		// Get the WIS data in case we need it...
		// FIXME SAM 2008-03-15 Need to remove WIS code
		//HydroBase_WISFormat wisFormat = nodePt.getWISFormat();
		if (nodeTypeToFind > 0) {
			// We have specified a node type to find...
			if (dataTypeToFind == NODE_DATA_LINK) {
				// We are searching for a link...
				long link = Long.parseLong(dataValueToFind);
				if ((nodePt.getType() ==nodeTypeToFind)
						){
					//FIXME 2008-03-15 Need to remove WIS code.
					//&& (wisFormat.getWdwater_link() ==link)
					//&& (wisFormat.getWdwater_link() > 0)) {
					if (Message.isDebugOn) {
						Message.printDebug(dl, routine,
							"Found CONF node \"" + nodePt.getCommonID() + "\" with link "+link);
					}
					return nodePt;
				}
				// Else no link matches...
			}
			else {	
				Message.printWarning(1, routine,
					"Don't know how to find node type: "
					+ nodeTypeToFind + " data type: "
					+ dataTypeToFind + " data value: "
					+ dataValueToFind);
			}
		}
		else {	
			Message.printWarning(1, routine,
				"Don't know how to find node type: "
				+ nodeTypeToFind + " data type: "
				+ dataTypeToFind + " data value: "
				+ dataValueToFind);
		}
	}
	return null;
}

/**
Find a node given its common indentifier and a starting node.
@param commonID Common identifier for node.
@param node Starting node in tree.
@return HydroBase_Node that is found or null if not found.
*/
public HydrologyNode findNode(String commonID, HydrologyNode node) {
	HydrologyNode nodePt;

	for ( nodePt = getUpstreamNode(node, POSITION_ABSOLUTE);
		nodePt != null;
		nodePt = getDownstreamNode(nodePt,POSITION_COMPUTATIONAL)) {
		// Break if we are at the end of the list...
		if ((nodePt == null) || (nodePt.getType() == HydrologyNode.NODE_TYPE_END)) {
			break;
		}
		if (nodePt.getCommonID().equalsIgnoreCase(commonID)) {
			return nodePt;
		}
	}

	return null;
}

/**
Find a node given its common identifier.  The node head is used as a starting
point to position the network at the top and then the network is traversed.
@return HydroBase_Node that is found or null if not found.
@param commonID Common identifier for node.
*/
public HydrologyNode findNode(String commonID) {
	HydrologyNode nodePt;
	HydrologyNode tempNode;

	nodePt = getMostUpstreamNode();
	boolean done = false;
	while (!done) {
		if (nodePt.getCommonID().equalsIgnoreCase(commonID)) {
			return nodePt;
		}
		// Break if at the bottom of the network.
		if ((nodePt == null) || (nodePt.getType() == HydrologyNode.NODE_TYPE_END)) {
			done = true;
			break;
		}
		tempNode = nodePt;
		nodePt = getDownstreamNode(nodePt, POSITION_COMPUTATIONAL);
		if (tempNode == nodePt) {
			done = true;
		}		
	}

	return null;
}

/**
Returns the reach's confluence and returns its next computational node (in the next reach)
@return the the reach's confluence and return its next computational node (in the next reach).
*/
public static HydrologyNode findReachConfluenceNext(HydrologyNode node) {
	String routine = "HydroBase_NodeNetwork.findReachConfluenceNext";

	HydrologyNode nodePt;
	HydrologyNode nodePt2;
	int dl = 15;

	// First check to see if we are on the main stem and there are no more
	// upstream nodes.  If there are none, then we are at the end of the system...
	if ((node.getReachCounter() == 1) && (node.getNumUpstreamNodes() == 0)){
		if (Message.isDebugOn) {
			Message.printDebug(dl, routine, "\"" + node.getCommonID()
				+ "\" is at the top of the system (last node on main stem)");
		}
		return node;
	}

	// First get the convergence node of this reach.  Save this reach
	// number so that we can use it to go to the next reach if necessary...
	//TODO SAM evaluate whether reachnum is needed
	//reachnum = node.getTributaryNumber();
	if (Message.isDebugOn) {
		Message.printDebug(dl, routine,
			"At top of reach, get reach start node for \"" + node.getCommonID() + "\"");
	}

	// First get the node off the previous stem...
	nodePt = getDownstreamNode(node, POSITION_REACH);
	if (Message.isDebugOn) {
		Message.printDebug(dl, routine, "Downstream node for reach containing \"" 
			+ node.getCommonID() + "\" is \"" + nodePt.getCommonID()
			+ "\" node_in_reach=" + nodePt.getNodeInReachNumber()
			+ " reachCounter=" + nodePt.getReachCounter());
	}

	// Make sure that this is not the bottom of the system.  If it is then there is no upstream reach...
	if ((nodePt.getReachCounter() == 1) && (nodePt.getNodeInReachNumber() == 1)) {
		if (Message.isDebugOn) {
			Message.printDebug(dl, routine, "\"" + node.getCommonID()
				+ "\" is at the top of the system (last node on main stem)");
		}
		return node;
	}

	// Now get the node on the previous stem.  This will be a confluence node...
	nodePt2 = nodePt.getDownstreamNode();
	if (Message.isDebugOn) {
		Message.printDebug(dl, routine,
			"Next downstream node (CONF parent reach) is \""
			+ nodePt2.getCommonID() + "\" reachCounter="
			+ nodePt2.getReachCounter() + " nupstream="
			+ nodePt2.getNumUpstreamNodes());
	}

	// Make sure that this is not the main stem.  If it is and we have only
	// one upstream node then there is no upstream reach...
	if ((nodePt2.getReachCounter() == 1) && (nodePt2.getNumUpstreamNodes() == 1)) {
		if (Message.isDebugOn) {
			Message.printDebug(dl, routine, "\"" + node.getCommonID()
				+ "\" is at the top of the system (last node on main stem)");
		}
		return node;
	}

	// Now, if we are on the main stem and there are no more upstream
	// nodes, then we are at the end of the system.  Most likely there
	// will not be a confluence at the end of the mainstem, but put in check anyhow...
	if ((nodePt2.getReachCounter() == 1) && (nodePt2.getNumUpstreamNodes() == 0)) {
		if (Message.isDebugOn) {
			Message.printDebug(dl, routine, "\"" + node.getCommonID()
				+ "\" is at the top of the system (last node on main stem)");
		}
		return node;
	}

	if (nodePt2.getNumUpstreamNodes() > nodePt.getTributaryNumber()) {
		// There are other reaches upstream in computational order so
		// move on to them.   Since the array is zero-referenced, just
		// use the reach number of the previous reach to get to the next one...

		// This will probably cause an exception in Java.  Need to check into it...
		// TODO (JTS - 2003-10-21) the above comment was made X years ago ... status?
		if (nodePt2.getUpstreamNode(nodePt.getTributaryNumber()) == null) {
			// Top of the system...
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine, "At top of system node \"" + nodePt2.getCommonID() + "\"");
			}
			return nodePt2;
		}
		// Reset pointer to be appropriate upstream...
		nodePt2 = nodePt2.getUpstreamNode(nodePt.getTributaryNumber());
		if (Message.isDebugOn) {
			Message.printDebug(dl, routine, "Go to next reach (reachnum=" + nodePt2.getTributaryNumber()
				+ ") first node \"" + nodePt2.getCommonID() + "\"");
		}
		return nodePt2;
	}
	else {	
		// There are no other reaches for the confluence node at the
		// beginning of this reach.  We are not at the top of the
		// system because that would have been caught above.
		// Therefore, we need to back up to the parent reach of this reach and then go forward.
		if (Message.isDebugOn) {
			Message.printDebug(dl, routine, "Node \"" + nodePt2.getCommonID() + "\" has nupstream=" 
				+ nodePt2.getNumUpstreamNodes() + " and found most upstream node \""
				+ node.getCommonID() + "\" in reach - recursing");
		}
		return findReachConfluenceNext(nodePt2);
	}
}

/**
Find the upstream natural flow node in the reach, given a starting node.  This is
used in WIS to find an upstream node to use for computations.  If 
a confluence is reached, assume that it also has known flow
values and can be treated as a natural flow node.
This method does consider dry nodes as natural flow nodes if the flag has been turned on.
@param node Starting Hydrology_Node in network (some node in a reach).
@return Hydrology_Node for upstream baseflow node in a reach.
@see StateMod_NodeNetwork#treatDryNodesAsNaturalFlow()
*/
public HydrologyNode findUpstreamNaturalFowNodeInReach(HydrologyNode node) {
	// Just move upstream in the reach until we find an upstream natural flow
	// node.  Return null if we get to the top of the reach and there is no natural flow node...
	for (HydrologyNode nodePt = getUpstreamNode(node,POSITION_REACH_NEXT);
		((nodePt != null) && (nodePt.getReachCounter() == node.getReachCounter()));
		nodePt = getUpstreamNode(nodePt, POSITION_REACH_NEXT)) {
		if (__treatDryAsNaturalFlow) {
			// First check for a dry node.
			if (nodePt.getIsDryRiver()) {
				return nodePt;
			}
		}
		if (nodePt.getIsNaturalFlow()) {
			return nodePt;
		}
		if (nodePt.getUpstreamNode() == null) {
			break;
		}
	}
	// Unable to find it...
	return null;
}

/**
Looks for the first upstream flow node on the current stem and the first 
upstream flow node on any of the tributaries to this stream.  For the initial call,
set 'recursing' to false -- will be set to true if this method recursively calls itself.
@param upstreamFlowNodes an allocated list that will be filled and used internally.
@param node the node from which to look upstream
@param recursing false if calling from outside this method, true if calling recursively.
*/
public List<HydrologyNode> findUpstreamFlowNodes(List<HydrologyNode> upstreamFlowNodes,
	HydrologyNode node, boolean recursing) {
	return findUpstreamFlowNodes(upstreamFlowNodes, node, null, recursing);
}

// TODO SAM 2004-08-15 Need to evaluate how to make prfGageData use more generic.
/**
Looks for the first upstream flow node on the current stem and the first 
upstream flow node on any of the tribs to this stream.  For the initial call,
set 'recursing' to false -- will be set to true if this method recursively calls itself.
@param upstreamFlowNodes a non-null list that will be filled and used internally.
@param node the node from which to look upstream
@param upstreamFlowNodeI interface to evaluate StateMod_PrfGageData - this is needed for special
functionality when processing StateMod_StreamEstimate_Coefficients in StateDMI (indicates nodes
that should be treated as upstream gages).
@param recursing false if calling from outside this method, true if calling recursively.
*/
public List<HydrologyNode> findUpstreamFlowNodes(List<HydrologyNode> upstreamFlowNodes,
	HydrologyNode node, UpstreamFlowNodeI upstreamFlowNodeI, boolean recursing)
{
	String routine = "HydroBase_NodeNetwork.findUpstreamFlowNodes";

	boolean	didRecurse = false;
	HydrologyNode nodePrev = null;
	HydrologyNode nodePt = null;
	HydrologyNode nodeReachTop = null;
	int dl = 12;
	//int reachCounter = 0;

	if (upstreamFlowNodes == null) {
		Message.printWarning(2, routine,"Null upstream flow node vector");
		return null;
	}

	// The node passed in initially will be "a natural flow node that is not a
	// flow node" or a "flow node".  It will NOT be a confluence.
	//
	// If we are recursing, then node passed in is a confluence and
	// we need to increment once before we start to process.  If we
	// immediately come across a confluence, then recurse again.  Should work OK.
	//
	// Then we need to set the pointer to the next upstream
	// computational node which should be the first node on the new reach...
	//
	// If not recursing,
	// Then we need to set the pointer to the next upstream
	// computational node because we do not want to immediately catch a flow node!
	if (Message.isDebugOn) {
		Message.printDebug(dl, routine,
			"Trying to find upstream FLOW nodes in reach starting at \"" + node.getCommonID()
			+ "\" reachCounter=" + node.getReachCounter() + " recursing=" + recursing);
	}

	for ( nodePt = getUpstreamNode(node, POSITION_COMPUTATIONAL),
		// TODO SAM 2007-02-18 Evaluate whether needed
		//reachCounter = nodePt.getReachCounter(),
		nodePt.getReachCounter(), nodePrev = node;
		; nodePrev = nodePt, nodePt = getUpstreamNode(nodePt, POSITION_COMPUTATIONAL)) {
		// Initialize to indicate that we have not yet recursed...
		didRecurse = false;
		// Always use incremented node to figure out the top of the
		// reach.  If the first node is the original node, then the
		// incremented node will still be on that reach, even if it is a confluence node.
		//
		// If the first node is a confluence, then the incremented
		// node will be the first node on the reach that we are
		// interested in.  If there is only one node on a reach, then
		// the SetNodeToUpstream function will return the same node, which is OK.
		if (nodePrev.equals(node)) {
			if (recursing) {
				nodeReachTop = getUpstreamNode(nodePt, POSITION_REACH);
			}
			else {	
				nodeReachTop = getUpstreamNode(node, POSITION_REACH);
			}
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine, "Node at top of reach is \"" + nodeReachTop.getCommonID() + "\"");
			}
			if (!recursing && (node.equals(nodeReachTop))) {
				// The node of interested was at the top of
				// the reach and we have already skipped past
				// it by incrementing the pointer.  Therefore
				// we have actually gone on to the next
				// computational reach!
				//
				// Reset the next upstream node that we have
				// found to be the reach top node.  That way
				// we can check to see if it is a FLOW node...
				//
				// We do not want to reset if recursing
				// because we may have to recurse again.
				if (Message.isDebugOn) {
					Message.printDebug(dl, routine, "Resetting node from loop to be top of reach \""
						+ nodeReachTop.getCommonID() + "\"");
				}
				nodePt = nodeReachTop;
				// Actually, if the initial node is at the top
				// of a reach it does not matter if it is a 
				// flow node because we do not want to add itself!...
				return upstreamFlowNodes;
			}
		}
		if (nodePt == null) {
			// Top of system?
			if (Message.isDebugOn) {
				Message.printWarning(1, routine, "nodePt is NULL?!");
			}
			break;
		}
		if (Message.isDebugOn) {
			Message.printDebug(dl, routine, "Checking node \"" + nodePt.getCommonID()
				+ "\" reachCounter=" + nodePt.getReachCounter() + " node_in_reach=" 
				+ nodePt.getNodeInReachNumber());
		}
		if ((nodePt.getType() == HydrologyNode.NODE_TYPE_CONFLUENCE) 
			|| (nodePt.getType() == HydrologyNode.NODE_TYPE_XCONFLUENCE)) {
			// Then we have started a new reach so recurse on the reach to find its first FLOW gage...
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine,
					"Found a confluence upstream of \"" + nodePt.getDownstreamNode().getCommonID()
					+ "\".  Recursing to find FLOW gage on trib");
			}
			upstreamFlowNodes = findUpstreamFlowNodes( upstreamFlowNodes, nodePt, upstreamFlowNodeI, true);
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine, "Back from recurse");
			}
			didRecurse = true;
			// If we are at the top of the reach, return also.
			// Otherwise, we reprocess the reach and end up
			// bailing out because we are the same as the previous node...
		}
		// This originally worked for makenet and should work for the
		// admin tool now that we are using natural flow nodes...
		else if (((nodePt.getType() == HydrologyNode.NODE_TYPE_FLOW) && nodePt.getIsNaturalFlow()) ||
			(upstreamFlowNodeI.isSetprfTarget( nodePt.getCommonID()) >= 0)) {
			// We are in a reach.  If this is a flow node, then
			// update the list and return since we are done with the reach(tributary)...
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine, "Found upstream tributary gage \"" + nodePt.getCommonID() + "\"");
			}
			upstreamFlowNodes.add(nodePt);
			return upstreamFlowNodes;
		}
		// We always want to check this...
		if (nodePt == nodeReachTop) {
			// Went to the top of the reach and did not find an upstream node(zero upstream nodes).
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine, "At top of reach.  No upstream FLOW node in trib for \""
					+ nodePt.getCommonID() + "\"");
			}
			return upstreamFlowNodes;
		}
		// If we have processed a confluence and we have not returned
		// because we hit the end of the reach, need to set the pointer
		// to the top of the confluence trib so that the next advance
		// will put us at the next node on this trib.
		//
		// We put this check here so that if we come back from a
		// recurse and we are at the top of the reach we do not overstep the reach.
		if (didRecurse) {
			nodePt = getUpstreamNode(getUpstreamNode(nodePt, POSITION_COMPUTATIONAL), POSITION_ABSOLUTE);
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine, "Set current node to \"" + nodePt.getCommonID()
					+ "\" to get to next node on previous reach in next loop");
			}
		}
	}
	return upstreamFlowNodes;
}

/**
Looks for the upstream nodes on the current stem and on any of the tributaries to this stream.
Currently all node types are added, including confluence, blank, etc.
Nodes are added on a reach until a tributary is found, and then recursion occurs.
@param foundNodes a list of found nodes that will be filled, if null will be created and returned.
@param node the node from which to look upstream, can be any node to start and
if called recursively will be the first node on the upstream reach
@param addFirstNode if true, add the node passed in to the found list,
used when handling whether the starting node should be in the results or not,
will be set to true when recursing.
@param upstreamNodeIdsToStop list of upstream node identifiers to include but not go past.
If the node ID is prefixed by "-", do not add the upstream node ID to the list (default is to add).
@return list of found nodes (same list as foundNodes if it was specified)
*/
public List<HydrologyNode> findUpstreamNodes ( List<HydrologyNode> foundNodes,
	HydrologyNode node, boolean addFirstNode, List<String> upstreamNodeIdsToStop )
{	int dl = 1;
	String routine = getClass().getSimpleName() + ".findUpstreamNodes";

	// Loop through upstream nodes.
	// - if the node has no upstream nodes, then return
	// - if a node has multiple upstream nodes, call this method recursively
	
	boolean firstNodeProcessed = false; // Special care is taken for the first node
	while ( true ) {
		if ( node == null ) {
			// Could occur through looping that top of reach is encountered
			break;
		}
		if ( Message.isDebugOn ) {
			Message.printDebug(dl,routine,"Checking reach node \"" + node.getCommonID() + "\"");
		}
		// First add the current node.
		// - add the node and reset the node to the upstream node
		// Check to see if the node should be included
		boolean upstreamNodeIdToStopFound = false;
		boolean shouldIncludeUpstreamNodeToStop = true;
		if ( upstreamNodeIdsToStop != null ) {
			for ( String upstreamNodeIdToStop : upstreamNodeIdsToStop ) {
				String upstreamNodeIdToStop2 = upstreamNodeIdToStop; // ID without leading dash
				if ( upstreamNodeIdToStop.startsWith("-") ) {
					upstreamNodeIdToStop2 = upstreamNodeIdToStop.substring(1); // Remove dash
					shouldIncludeUpstreamNodeToStop = false; // Because of dash
				}
				if ( upstreamNodeIdToStop2.equalsIgnoreCase(node.getCommonID())) {
					upstreamNodeIdToStopFound = true; // Check this first and then shouldIncludeUpstreamNodeToStop
					break; // Found a match so quit searching
				}
			}
		}
		// Handle the cases separately to avoid confusion - a bit redundant but clear
		if ( firstNodeProcessed ) {
			// Have already processed the first node.
			// Always add unless stop node and stop node should be ignored.
			if ( upstreamNodeIdToStopFound ) {
				if ( shouldIncludeUpstreamNodeToStop ) {
					if ( Message.isDebugOn ) {
						Message.printDebug(dl,routine,"Adding upstream reach node to stop \"" + node.getCommonID() + "\"");
					}
					foundNodes.add(node);
				}
				break; // Since at stop node
			}
			else {
				// Have not found upstream node to stop so add node and keep going
				if ( Message.isDebugOn ) {
					Message.printDebug(dl,routine,"Adding reach node \"" + node.getCommonID() + "\"");
				}
				foundNodes.add(node);
			}
		}
		else {
			// First node - do the checks for the upstream node for completeness
			if ( addFirstNode ) {
				if ( upstreamNodeIdToStopFound ) {
					if ( shouldIncludeUpstreamNodeToStop ) {
						if ( Message.isDebugOn ) {
							Message.printDebug(1,routine,"Adding upstream reach node for stop \"" + node.getCommonID() + "\"");
						}
						foundNodes.add(node);
					}
					break; // Since at stop node
				}
				else {
					// Have not found upstream node to stop so add node and keep going
					if ( Message.isDebugOn ) {
						Message.printDebug(dl,routine,"Adding first reach node \"" + node.getCommonID() + "\"");
					}
					foundNodes.add(node);
				}
			}
			// Indicate that have processed the first node for the method call
			firstNodeProcessed = true;
		}
		// Decide whether continuing on reach or branching
		List<HydrologyNode> upstreamNodes = node.getUpstreamNodes();
		if ( (upstreamNodes == null) || (upstreamNodes.size() == 0) ) {
			// No more upstream nodes
			break; // Will cause a return of found nodes
		}
		else if ( upstreamNodes.size() == 1 ) {
			// Continuing on the same reach
			// -set the node to the only upstream node for the next loop iteration
			node = upstreamNodes.get(0);
		}
		else {
			// Have multiple reaches so call recursively and break when back because all upstream nodes will have been processed
			boolean addFirstNode2 = true;
			for ( HydrologyNode upstreamNode : upstreamNodes ) {
				findUpstreamNodes ( foundNodes, upstreamNode, addFirstNode2, upstreamNodeIdsToStop );
			}
			break; // Will cause a return of found nodes
		}
	}
	return foundNodes;
}

/**
Format the WDID, accounting for padded zeros, etc., for StateMod files.
This is used instead of the code in HydroBase_WaterDistrict because it makes a check for node type.
@param wd the wd
@param id the id
@param nodeType the type of the node.
@return the formatted WDID.
*/
public String formatWDID(int wd, int id, int nodeType) {
	String tempID = "" + id;
	String tempWD = "" + wd;

	return formatWDID(tempWD, tempID, nodeType); 
}

/**
Format the WDID, accounting for padded zeros, etc., for StateMod files.
This is used instead of the code in HydroBase_WaterDistrict because it makes a check for node type.
@param wd the wd
@param id the id
@param nodeType the type of the node.
@return the formatted WDID.
*/
public String formatWDID(String wd, String id, int nodeType) {
	String routine = "HydroBase_NodeNetwork.formatWDID";
	
	int dl = 10;
	int idFormatLen = 4;
	int idLen;
	int wdFormatLen = 2;
	String message;
	String wdid;	

	// Wells have a 5 digit id. For now, this is the only type that has anything but a 4 digit id...
	if (nodeType == HydrologyNode.NODE_TYPE_WELL) {
		idFormatLen = 5;
	}

	if (Message.isDebugOn) {
		message = "WD: " + wd + " ID: " + id;
		Message.printDebug(dl, routine, message);  
	}

	wdid = wd;
	idLen = id.length();

	if (idLen > 5) {
		// Long identifiers are assumed to be used as is.
		wdid = id;
	}
	else {	
		// Prepend the WD to the identifier...
		for (int i = 0; i < wdFormatLen - wdid.length(); i++) {
			wdid = "0"+ wdid;
		}

		for (int i = 0; i < idFormatLen - idLen; i++) {
			wdid = wdid.concat("0");
		}
		wdid = wdid.concat(id);
	}

	if (Message.isDebugOn) {
		message = "Finished WDID: " +wdid;
		Message.printDebug(dl, routine, message);  
	}

	return wdid;
}

/**
Returns the list of annotations that accompany this network.  Guaranteed to be non-null.
@return the list of annotations that accompany this network.
*/
public List<HydrologyNode> getAnnotationList() {
	return __annotationList;
}

/**
Returns the list of labels that accompany this network.  Guaranteed to be non-null.
@return the list of labels that accompany this network.
*/
public List<HydrologyNodeNetworkLabel> getLabelList() {
	return __labelList;
}

/**
Returns a list of all the nodes in the network that are natural flow nodes.
@return a list of all the nodes that are natural flow nodes.  The list is guaranteed to be non-null.
*/
public List<HydrologyNode> getBaseflowNodes() {
	List<HydrologyNode> v = new Vector<HydrologyNode>();

	HydrologyNode node = getMostUpstreamNode();
	// TODO -- eliminate the need for hold nodes -- they signify an error in the network.
	HydrologyNode hold = null;
	while (true) {
		if (node == null) {
			return v;
		}

		if (node.getIsNaturalFlow()) {
			v.add(node);
		}

		if (node.getType() == HydrologyNode.NODE_TYPE_END) {
			return v;
		}
		
		hold = node;
		node = getDownstreamNode(node,POSITION_COMPUTATIONAL);

		// to avoid errors when the network is not built properly
		if (hold == node) {
			return v;
		}
	}
}

/**
Returns the bottom Y bound of a network read from XML.
@return the bottom Y bound of a network read from XML.
*/
public double getBY() {
	return __netBY;
}

/**
Find the downstream node for the specified node.
@param node the node from which to find the downstream node
@param flag a flag telling the position to return the node -- POSITION_RELATIVE,
POSITION_ABSOLUTE, POSITION_COMPUTATIONAL.
@return the downstream node.
*/
public static HydrologyNode getDownstreamNode(HydrologyNode node, int flag) {
	String routine = "HydroBase_NodeNetwork.getDownstreamNode";

	HydrologyNode nodePt = node;
	int dl = 20;

	if (node.getDownstreamNode() == null) {
		if (Message.isDebugOn) {
			Message.printDebug(dl, routine, "Found absolute downstream node \""
				+ nodePt.getNetID() + "\" (\"" + nodePt.getCommonID() + "\")");
		}
		return node;
	}

	if (flag == POSITION_RELATIVE) {
		// Just return the downstream node...
		if (node.getDownstreamNode() == null) {
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine, "Node \"" + node.getCommonID()
					+ "\" has downstream node NULL");
			}
		}
		else {	
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine, "Node \"" + node.getCommonID() + "\" has downstream node \""
					+ (node.getDownstreamNode()).getCommonID() +"\"");
			}
		}
		return node.getDownstreamNode();
	}
	else if (flag == POSITION_ABSOLUTE) {
		// It is expected that if you call this you are starting from somewhere on a main stem.
		if (Message.isDebugOn) {
			Message.printDebug(dl, routine,
				"Trying to find absolute downstream starting with \"" + node.getCommonID() + "\"");
		}
		// Think of the traversal as a "left-hand" traversal.  Always
		// follow the first branch added since it is the most downstream
		// computation-wise.  When we have no more downstream nodes we are at the bottom...
		nodePt = node;
		if (nodePt.getDownstreamNode() == null) {
			// Nothing below this node...
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine, "Found absolute downstream: \"" + nodePt.getCommonID() + "\"");
			}
			return nodePt;
		}
		else {	
			// Follow the first reach entered for this node (the most downstream)...
			return getDownstreamNode(nodePt.getDownstreamNode(), POSITION_ABSOLUTE);
		}
	}
	else if (flag == POSITION_COMPUTATIONAL) {
		// We want to find the next computational downstream node.  Note
		// that this may mean that an additional branch above a
		// convergence point is traversed.  The computational order is
		// basically decided by the user.
		if (Message.isDebugOn) {
			Message.printDebug(dl, routine, "Trying to find next computational downstream "
				+ "node for \"" + node.getCommonID() + "\" reachnum=" + node.getTributaryNumber());
		}
		if (Message.isDebugOn) {
			Message.printDebug(dl, routine, "Downstream node \"" + (node.getDownstreamNode()).getCommonID()
				+ "\" has nupstream = " + (node.getDownstreamNode()).getNumUpstreamNodes());
		}
		if (node.getUpstreamOrder()== HydrologyNode.TRIBS_ADDED_FIRST) {
			// Makenet convention...
			if (((node.getDownstreamNode()).getNumUpstreamNodes() == 1) 
				|| (node.getTributaryNumber() == 1)) {
				// Then there is only one downstream node or we
				// are the furthest downstream node in a list of upstream reaches...
				if (Message.isDebugOn) {
					Message.printDebug(dl, routine, "Going to only possible downstream node:  \""
						+ (node.getDownstreamNode()).getCommonID() + "\"");
				}
				return node.getDownstreamNode();
			}
			else {	
				// The downstream node has several upstream
				// nodes and we are one of them.  Go to the next
				// lowest reach number and travel to the top of
				// that reach and use that node for the next
				// downstream node.  This is a computation order.
				HydrologyNode nodeDown = node.getDownstreamNode();
				if (Message.isDebugOn) {
					Message.printDebug(dl, routine, "Going to highest node on next "
						+ "downstream branch starting at \""
						+ (nodeDown.getUpstreamNode(node.getTributaryNumber() - 1 - 1)).getCommonID() + "\"");
				}
				
				return getUpstreamNode(
					nodeDown.getUpstreamNode( node.getTributaryNumber() - 1 - 1), POSITION_ABSOLUTE);
			}
		}
		else {	
			// Admin tool convention...
			// Check to see if we are the furthest downstream by
			// having a trib number that is the count of upstream nodes...
			if (((node.getDownstreamNode()).getNumUpstreamNodes() == 1) 
				|| (node.getTributaryNumber() == node.getDownstreamNode().getNumUpstreamNodes())) {
				// Then there is only one downstream node or we
				// are the furthest downstream node in a list of upstream reaches...
				if (node.getDownstreamNode().getNumUpstreamNodes() == 1) {
					if (Message.isDebugOn) {
						Message.printDebug(dl, routine,
							"Going to only possible downstream node because no branch: \""
							+ (node.getDownstreamNode()).getCommonID() + "\"");
					}
				}
				else {	
					if (Message.isDebugOn) {
						Message.printDebug(dl, routine, "Going to only possible downstream node:  \""
							+ (node.getDownstreamNode()).getCommonID() + "\" because trib "
							+ node.getTributaryNumber() + " is max for downstream branching node");
					}
				}
				return node.getDownstreamNode();
			}
			else {	
				// The downstream node has several upstream
				// nodes and we are one of them.  Go to the next
				// highest reach number and travel to the top of
				// that reach and use that node for the next
				// downstream node.  This is a computation order.
				HydrologyNode nodeDown = node.getDownstreamNode();
				if (Message.isDebugOn) {
					Message.printDebug(dl, routine, "Going to highest node on next "
						+ "downstream branch starting at \""
						+ (nodeDown.getUpstreamNode(node.getTributaryNumber() + 1 - 1)).getCommonID() + "\"");
				}
				
				return getUpstreamNode(
					nodeDown.getUpstreamNode( node.getTributaryNumber() + 1 - 1), POSITION_ABSOLUTE);
			}
		}
	}
	else if (flag == POSITION_REACH) {
		// Find the most downstream node in the reach.  This does NOT
		// include the node on the previous stem, but the first node
		// of the stem in the reach.  Find the node in the current
		// reach with node_in_reach = 1.  This is the node off of the parent river node.
		if (Message.isDebugOn) {
			Message.printDebug(dl, routine,
				"Trying to find downstream node in reach tarting at \"" + node.getCommonID() + "\"");
		}
		nodePt = node;
		while (true) {
			if (nodePt.getNodeInReachNumber() == 1) {
				if (Message.isDebugOn) {
					Message.printDebug(dl, routine, "First node in reach is \"" + nodePt.getCommonID() + "\"");
				}
				return nodePt;
			}
			else {	
				// Reset to the next downstream node...
				nodePt = nodePt.getDownstreamNode();
				if (Message.isDebugOn) {
					Message.printDebug(dl, routine, "Going to downstream node \"" + nodePt.getCommonID() +
						"\" [node_in_reach = " + nodePt.getNodeInReachNumber() + "]");
				}
			}
		}
	}
	else {	
		// Trouble...
		Message.printWarning(1, routine, "Invalid POSITION argument");	
		return null;
	}
}

/**
Return the edge buffer values.
@return the edge buffer values (left, right, top, bottom, in X,Y data units).
*/
public double [] getEdgeBuffer ()
{
	return this.__edgeBuffer;
}

/**
Return the name of the input file for this network, or null if not available (is being created).
@return the name of the input file.
*/
public String getInputName ()
{
	return __inputName;
}

/**
Returns the layout list read in from XML. 
@return the layout list read in from XML.  Can be null.
*/
public List<PropList> getLayoutList() {
	return __layoutList;
}

/**
Returns the location of the network legend as GRLimits.  Only the bottom-left
point of the legend is stored; the rest of the legend positioning is calculated on-the-fly.
@return the location of the network legend as GRLimits.
*/
public GRLimits getLegendLocation() {
	return new GRLimits(getLegendX(), getLegendY(), -999, -999);
}

/**
Returns the left-most X coordinate of the network's legend.  Calling code 
should first call isLegendPositionSet() to see if the position has been set or
not.  If not, then the value returned from this method will always be 0.
@return the left-most X coordinate of the network's legend.  
*/
public double getLegendX() {
	return __legendPositionX;
}

/**
Returns the bottom-most Y coordinate of the network's legend.  Calling code 
should first call isLegendPositionSet() to see if the position has been set or
not.  If not, then the value returned from this method will always be 0.
@return the bottomleft-most Y coordinate of the network's legend.  
*/
public double getLegendY() {
	return __legendPositionY;
}

/**
Returns the list of links between nodes in the network.
@return the list of links between nodes in the network.
*/
public List<PropList> getLinkList() {
	return __linkList;
}

/**
Returns the left X bound of a network read from XML.
@return the left X bound of a network read from XML.
*/
public double getLX() {
	return __netLX;
}

/**
Returns the most upstream node in the network or null if not found.
@return the most upstream node in the network or null if not found.
*/
public HydrologyNode getMostUpstreamNode() {
	// Go to the bottom of the system so that we can get to the top of
	// the main stem...
	HydrologyNode node = null;
	node = getDownstreamNode(__nodeHead, POSITION_ABSOLUTE);

	// Now traverse downstream, creating the strings...
	node = getUpstreamNode(node, POSITION_ABSOLUTE);
	return node;
}

/**
 * Return the network name.
 * @return the network name.
 */
@Override
public String getNetworkName () {
	return this.__networkName;
}

/**
 * Return the network ID.
 * @return the network ID.
 */
@Override
public String getNetworkId () {
	return this.__networkId;
}

/**
Returns the number of nodes in the network.
This does not seem reliable, perhaps used for a specific legacy purpose.
See size().
@return the number of nodes in the network.
*/
public int getNodeCount() {
	return __nodeCount;
}

/**
Returns a list of String, each of which is a line telling how many of
a certain kind of node is present in the network.  
@return a List of Strings, guaranteed to be non-null.
*/
public List<String> getNodeCountsVector() {
	List<String> v = new Vector<String>();
	int total = 12;
	int[] types = new int[total];
	types[0] = HydrologyNode.NODE_TYPE_BASEFLOW;
	types[1] = HydrologyNode.NODE_TYPE_CONFLUENCE;
	types[2] = HydrologyNode.NODE_TYPE_DIV;
	types[3] = HydrologyNode.NODE_TYPE_DIV_AND_WELL;
	types[4] = HydrologyNode.NODE_TYPE_END;
	types[5] = HydrologyNode.NODE_TYPE_IMPORT;
	types[6] = HydrologyNode.NODE_TYPE_ISF;
	types[7] = HydrologyNode.NODE_TYPE_OTHER;
	types[8] = HydrologyNode.NODE_TYPE_PLAN;
	types[9] = HydrologyNode.NODE_TYPE_RES;
	types[10] = HydrologyNode.NODE_TYPE_FLOW;
	types[11] = HydrologyNode.NODE_TYPE_WELL;

	int count = 0;
	String plural = "s";
	for (int i = 0; i < total; i++) {
		plural = "s";
		count = getNodesForType(types[i]).size();
		if (count == 1) {
			plural = "";
		}
		v.add("Network contains " + count + " " 
			+ HydrologyNode.getTypeString(types[i],	HydrologyNode.FULL) + " node" + plural + ".");
	}
	return v;
}

/**
Returns the head node in the network (the most downstream).
@return the head node in the network.
*/
public HydrologyNode getNodeHead() {
	return __nodeHead;
}

/**
Return a list of all identifiers in the network given the types
of interest.  The common identifiers are returned or, if the identifier contains
a ".", the part before the "." is returned.  It is expected that this method
is only called with real node types(not BLANK, etc.).
The list is determined going from upstream to downstream so any
code that uses this list should also go upstream to downstream for fastest performance.
@param nodeTypes Array of node types to find (see HydroBase_Node.NODE_TYPE_*).
@return a Vector of all identifiers in the network given the types of interest.
@exception Exception if there is an error during the search.
*/
public List<String> getNodeIdentifiersByType(int[] nodeTypes)
throws Exception {
	String routine = "HydroBase_NodeNetwork.getNodeIdentifiersByType";
	List<String> ids = new Vector<String>();

	try {
		// Main try for method

		if (nodeTypes == null) {
			return ids;
		}
	
		boolean nodeTypeMatches = false;
		HydrologyNode nodePt = null;
		int j;
		int nodeType = 0;
		int nnodeTypes = nodeTypes.length;
		String commonID = null;
		List<String> v = null;
	
		// Traverse from upstream to downstream...
		for (nodePt = getUpstreamNode(getDownstreamNode(__nodeHead, POSITION_ABSOLUTE), POSITION_ABSOLUTE);
			nodePt != null;
			nodePt = getDownstreamNode(nodePt, POSITION_COMPUTATIONAL)) {
			nodeType = nodePt.getType();
			// See if the nodeType matches one that we are interested in...
			nodeTypeMatches = false;
			for (j = 0; j < nnodeTypes; j++) {
				if (nodeTypes[j] == nodeType) {
					nodeTypeMatches = true;
					break;
				}
			}
			if (!nodeTypeMatches) {
				// No need to check further...
				if (nodePt.getDownstreamNode() == null) {
					// End...
					break;
				}
				continue;
			}
			// Use the node type of the HydroBase_Node to make decisions about extra checks, etc...
			if (nodeType == HydrologyNode.NODE_TYPE_FLOW) {
				// Just use the common identifier...
				ids.add(nodePt.getCommonID());
			}
			else if ((nodeType == HydrologyNode.NODE_TYPE_DIV) 
				|| (nodeType == HydrologyNode.NODE_TYPE_DIV_AND_WELL)
				|| (nodeType == HydrologyNode.NODE_TYPE_ISF)
				|| (nodeType == HydrologyNode.NODE_TYPE_RES)
				|| (nodeType == HydrologyNode.NODE_TYPE_WELL)
				|| (nodeType == HydrologyNode.NODE_TYPE_IMPORT)) {
				commonID = nodePt.getCommonID();
				if (commonID.indexOf('.') >= 0) {
					// Get the string before the "." in case some
					// ISF or other modified identifier is used...
					v = StringUtil.breakStringList(commonID, ".", 0);
					ids.add(v.get(0));
					// Note that if the _Dwn convention is used,
					// then the structure should be found when the
					// upstream terminus is queried and the
					// information can be reused for the downstream.
				}
				else {	
					// Just add id as is...
					ids.add(nodePt.getCommonID());
				}
			}
			else {	
				// Just use the common identifier...
				ids.add(nodePt.getCommonID());
			}
			if (nodePt.getDownstreamNode() == null) {
				// End...
				break;
			}
		}
	}
	catch (Exception e) {
		String message = "Error getting node identifiers for network";
		Message.printWarning(2, routine, message);
		Message.printWarning(2, routine, e);
		throw new Exception(message);
	}

	// Return the final list...
	return ids;
}

/**
Return a label to use for the specified node.
@param node Node of interest.
@param lt Label type (see LABEL_NODES_*).
@return a label to use for the specified node.
*/
protected String getNodeLabel(HydrologyNode node, int lt) {
	String label = null;
	if (lt == LABEL_NODES_AREA_PRECIP) {
		label = node.getAreaString() + "*" + node.getPrecipString();
	}
	else if (lt == LABEL_NODES_COMMONID) {
		label = node.getCommonID();
	}
	else if (__labelType == LABEL_NODES_NAME) {
		label = node.getDescription();
	}
	else if (lt == LABEL_NODES_PF) {
		if (node.getIsNaturalFlow() && (node.getType() != HydrologyNode.NODE_TYPE_FLOW)) {
			label = StringUtil.formatString(node.getProrationFactor(), "%5.3f");
		}
		else {
			label = "";
		}
	}
	else if (lt == LABEL_NODES_RIVERNODE) {
		label = node.getRiverNodeID();
	}
	else if (lt == LABEL_NODES_WATER) {
		label = node.getWaterString();
	}
	else {	
		label = node.getNetID();
	}
	return label;
}

/**
Returns a list of all the nodes in the network.  This can be used when (re)building networks from raw
node data, such as when merging networks read from XML files.  All nodes are returned, including confluences
and end node (anything that would persist to a file).
@return a list of all the nodes that are the specified type.  The list is
guaranteed to be non-null and is in the order upstream to downstream.
*/
public List<HydrologyNode> getNodeList()
{	List<HydrologyNode> nodeList = new Vector<HydrologyNode>();

	HydrologyNode node = getMostUpstreamNode();
	// TODO -- eliminate the need for hold nodes -- they signify an error in the network.
	HydrologyNode hold = null;
	int nodeType = 0;
	while (true) {
		if ( node == null ) {
			// End of network...
			return nodeList;
		}
		// Add to the list
		nodeList.add(node);
		// Check for the end node
		nodeType = node.getType();
		if ( nodeType == HydrologyNode.NODE_TYPE_END ) {
			return nodeList;
		}
		
		// Increment the node to the next computational downstream...

		hold = node;
		node = getDownstreamNode(node,POSITION_COMPUTATIONAL);
		// to avoid infinite loops when the network is not built properly
		if (hold == node) {
			return nodeList;
		}
	}
}

/**
Return a sequence of nodes that inclusively containing the endspoints that are provided.
Whether node1 is updstream of node2 is irrelevant as both will be tried as the upstream with the search
for the other in downstream fashion.
@param node1 the first node in the sequence
@param node2 the last node in the sequence
@return the list of nodes inclusive of the endpoints, in an upstream to downstream order.
*/
public List<HydrologyNode> getNodeSequence ( HydrologyNode node1, HydrologyNode node2 )
{
	List<HydrologyNode> nodeList = new Vector<HydrologyNode>();
	HydrologyNode node = node1;
	while (true) {
		if ( node == null ) {
			// End of network so have not found the sequence...
			return new Vector<HydrologyNode>();
		}
		else if ( node.getCommonID().equalsIgnoreCase(node2.getCommonID()) ) {
			// Found the end of the sequence
			nodeList.add ( node );
			return nodeList;
		}
		else if ( node.getType() == HydrologyNode.NODE_TYPE_END ) {
			// End of the network without finding node2 so break
			break;
		}
		else {
			// A node in the potential sequence
			nodeList.add ( node );
		}
		// Go to the next downstream node.
		node = getDownstreamNode(node,POSITION_RELATIVE);
	}
	
	// If here did not find node2 so try the other order...
	
	nodeList = new Vector<HydrologyNode>();
	node = node2;
	while (true) {
		if ( node == null ) {
			// End of network so have not found the sequence...
			return new Vector<HydrologyNode>();
		}
		else if ( node.getCommonID().equalsIgnoreCase(node1.getCommonID()) ) {
			// Found the end of the sequence
			nodeList.add ( node );
			return nodeList;
		}
		else if ( node.getType() == HydrologyNode.NODE_TYPE_END ) {
			// End of the network without finding node2 so break
			break;
		}
		else {
			// A node in the potential sequence
			nodeList.add ( node );
		}
		// Go to the next downstream node.
		node = getDownstreamNode(node,POSITION_RELATIVE);
	}
	
	// If here could not find a sequence.  Try moving downstream from each node to see if they meet
	// on a common stream.
	
	List<HydrologyNode> nodeListA = new Vector<HydrologyNode>();
	HydrologyNode nodeA = node1;
	List<HydrologyNode> nodeListB = new Vector<HydrologyNode>();
	HydrologyNode nodeB = null;
	while (true) {
		if ( nodeA == null ) {
			// End of network so have not found the sequence...
			return new Vector<HydrologyNode>();
		}
		else if ( nodeA.getType() == HydrologyNode.NODE_TYPE_END ) {
			// End of the network without finding common node downstream of node2 so break
			break;
		}
		else {
			// Loop through nodeB downstream to see if it intersects with nodeA
			nodeListB.clear();
			nodeB = node2;
			while ( true ) {
				if ( nodeB == null ) {
					// End of network so have not found the sequence...
					return new Vector<HydrologyNode>();
				}
				else if ( nodeA == nodeB ) {
					// Match a node so add the lists together and return
					nodeList.clear();
					nodeList.addAll(nodeListA);
					// Want to add nodeListB but in the reverse order so the sequence is continuous
					Collections.reverse(nodeListB);
					nodeList.addAll(nodeListB);
					return nodeList;
				}
				else if ( nodeB.getType() == HydrologyNode.NODE_TYPE_END ) {
					// End of the network without finding node2 so break
					break;
				}
				else {
					// Continue in the sequence
					nodeListB.add ( nodeB );
				}
				nodeB = getDownstreamNode(nodeB,POSITION_RELATIVE);
			}
			// If here did not match with nodeB sequence so add a node to A and continue downstream
			nodeListA.add ( nodeA );
		}
		// Go to the next downstream node.
		nodeA = getDownstreamNode(nodeA,POSITION_RELATIVE);
	}
	
	return new Vector<HydrologyNode>();
}

/**
Returns a list of all the nodes in the network of a given type.
@param type the type of nodes (as defined in HydroBase_Node.NODE_*) to return.
If -1, return all nodes that are model nodes (do not return confluences, etc.,
which are only used in the network diagram).
@return a list of all the nodes that are the specified type.  The list is
guaranteed to be non-null and is in the order upstream to downstream.
*/
public List<HydrologyNode> getNodesForType(int type)
{	List<HydrologyNode> v = new Vector<HydrologyNode>();

	HydrologyNode node = getMostUpstreamNode();
	// TODO -- eliminate the need for hold nodes -- they signify an error in the network.
	HydrologyNode hold = null;
	int node_type = 0;
	while (true) {
		if ( node == null ) {
			// End of network...
			return v;
		}
		node_type = node.getType();
		if ( node_type == HydrologyNode.NODE_TYPE_END ) {
			// End of network...
			if ( node_type == type ) {
				v.add(node);
			}
			return v;
		}
		else if ( type == -1 ) {
			if ( (node_type == HydrologyNode.NODE_TYPE_FLOW)||
				(node_type == HydrologyNode.NODE_TYPE_DIV)||
				(node_type == HydrologyNode.NODE_TYPE_DIV_AND_WELL)||
				(node_type == HydrologyNode.NODE_TYPE_RES)||
				(node_type == HydrologyNode.NODE_TYPE_ISF)||
				(node_type == HydrologyNode.NODE_TYPE_WELL)||
				(node_type == HydrologyNode.NODE_TYPE_OTHER) ||
				(node_type == HydrologyNode.NODE_TYPE_PLAN)){
				v.add(node);
			}
		}
		else if ( node_type == type ) {
			// Requesting a single node type and it matches...
			v.add(node);
		}
		
		// Increment the node to the next computational downstream...

		hold = node;
		node = getDownstreamNode(node,POSITION_COMPUTATIONAL);
		// to avoid infinite loops when the network is not built properly
		if (hold == node) {
			return v;
		}
	}
}

/**
Returns the right X bound of a network read from XML.
@return the right X bound of a network read from XML.
*/
public double getRX() {
	return __netRX;
}

/**
 * Return the network title.
 */
public String getTitle () {
	return __title;
}

/**
Returns the top Y bound of a network read from XML.
@return the top Y bound of a network read from XML.
*/
public double getTY() {
	return __netTY;
}

/**
Gets the first downstream node from the specified node that has valid X and Y location values.
@param node the node for which to find a valid downstream node.
@return a two-element Vector with the first valid downstream node (first 
element), and the second element is an Integer of the distance from node 
to the downstream node.  If none can be found, the first element is null and the second is -1.
*/
private List<Object> getValidDownstreamNode(HydrologyNode node, boolean main) {
	boolean done = false;
	HydrologyNode ds = null;
	HydrologyNode temp = null;
	int count = 1;
	int reachLevel = node.getReachLevel();
	int currReach = reachLevel - 1;
	List<Object> v = new Vector<Object>();
	
	ds = getDownstreamNode(node, POSITION_RELATIVE);	

	// loop through going downstream of the passed-in node, looking for
	// the first node with valid values.  Also keep track of the number of nodes encountered.
	while (!done) {
		temp = ds;
		currReach = ds.getReachLevel();
		if ( (main && (currReach == 1)) || (!main && (currReach <= reachLevel)) ) {
			reachLevel = currReach;
			if (ds.getX() >= 0 && ds.getY() >= 0) {
				v.add(ds);
				v.add(new Integer(count));
				return v;
			}
		}
		else if (main && currReach > 1) {
			// ignore
		}
		else {			
			// Can go no further!  
			done = true;
		}

		if (!done) {
			ds = getDownstreamNode(ds, POSITION_RELATIVE);
			if (temp == ds) {
				done = true;
			}
		}
		count++;
	}
	v.add(null);
	v.add(new Integer(count - 1));
	return v;
}

/**
Constructs a 2-D Vector of all of the WDWater structures and their 
corresponding structure objects, in the HydroBase_NodeNetwork.
*/
/*
UNDER CONSTRUCTION
public Vector getWDWaterInNetwork()
throws Exception {
String routine = "HydroBase_NodeNetwork.getWDWaterInNetwork", where = "", 
		temp="", wd, id, compos;
	Vector results = null, queries = new Vector(), struct_list = null;
	int dl = 15, i = 0, j = 0, maxquery, size = 0;
	HydroBase_Structure structure;
	HydroBase_WDWater water;

	// Set the maxquery according to what type of database we are 
	// connected with
	if (__isDatabaseUp) {
	if (__dmi.getConnectionSource() == HBSource.MIDDLEWARE_ODBC_ACCESS) {
		maxquery = 25;
	}
	else {	maxquery = 100000;
	}

	// Get the IDs for all WDWaters detected in the network and then
	// copy them into results Vector

	// Need to get a list of all of the RES, DIV, and IMPORT structures
/*
	try {	struct_list = getStructuresInNetwork();
	}
	catch (Exception e) {
		String message = "Errors getting Strucutres in Network";
		throw new Exception(message);
	}
*/
/*
UNDER CONSTRUCTION
if (struct_list == null || struct_list.size() == 0) {
		Message.printWarning(2, routine, "Found 0 Structures "+
		"in network.");
		return(results);
	}
	if (Message.isDebugOn) {
		Message.printDebug(dl, routine, "Number of Res, Div, "+
		"and Import objects:  " + struct_list.size());
	}

	// This string is common to all structures...
	temp = "structure_num in (";
	size = struct_list.size();
	for (i = 0, j = 0; i < size; i++, j++) {
		structure = (HydroBase_Structure)struct_list.elementAt(i);
		if (Message.isDebugOn) {	
			Message.printDebug(dl, routine, 
			"Structure IDs:  "+ structure.getID());
		}

		wd = ""+structure.getWD();
		id = ""+structure.getID();
		//temp = "(structure.wd="+wd+" AND structure.id="+id+")OR "; 
		temp = temp.concat(""+structure.getStructure_num() +",");

		where = where.concat(temp); 
		// If we get into this if block, it means that we have 
		// exceed the max number of queries and need to add an 
		// element to the queries Vector
		if (j >= maxquery || i == size - 1) {
		if (__isDatabaseUp) {
			HBQuery query = new HBWDWaterQuery(__dmi);
			// This strips the final "OR" off and adds a 
			// ")" before the last where conditional is 
			// added...
			/*
			query.addWhereClause("("+where.substring(0, 
				where.length() -3) +")"); 
			*/
/*
UNDER CONSTRUCTION
query.addWhereClause(temp.substring(0, 
				temp.length() - 1) +")");
			queries.add(query);
			where = "";
			temp = "structure_num in (";
			j = 0;
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine, 
				"SQL for Fancy: "+ temp);
			}
		}
	}
	// We need to process the queries in the Vector. There will be one
	// element if we are dealing with anything but Access and _probably_
	// more than one if we are dealing with Access
	results = new Vector();
	Vector tmp_results;
	int k,ii;
	size = queries.size();
	try {
	for (i = 0, ii = 0; i < size; i++) {
		if (__isDatabaseUp) {
		tmp_results = __dmi.processInternalQuery(
			(HBWDWaterQuery)queries.elementAt(i));
		if (Message.isDebugOn) {
			Message.printDebug(dl, routine, "Found " + 
			tmp_results.size() + " WDWater/Structure pairs.");
		}
		 
		int tmp_size = tmp_results.size();
		for (j = 0; j < tmp_size; j++) {
			water = (HydroBase_WDWater)tmp_results.elementAt(j);

			int struct_size = struct_list.size();
			for (k = 0; k < struct_size; k++) {
				structure= 
				(HydroBase_Structure)struct_list.elementAt(k);
				if (Message.isDebugOn) {
					Message.printDebug(dl+5, routine,
					"Trying to match WDWater  WD: "+ 
					water.getWD() + "  ID: "+ 
					water.getID() + " with "+ 
					"Structure WD: "+structure.getWD() +
					" ID: "+structure.getID());
				}
				if (structure.getWD() == water.getWD() &&
					structure.getID() == water.getID()) {
					results.add(new Vector()); 
					((Vector)
					results.elementAt(ii)).add(
					structure);
					((Vector)
					results.elementAt(ii)).add(
					water);
					ii++;

					if (Message.isDebugOn) {
						Message.printDebug(dl+5,
						routine,
						"Matched WDWater  WD: "+ 
						water.getWD() + "  ID: "+ 
						water.getID() + " with "+ 
						"Structure ID: " +
						structure.getID());
					}
					break;
				}
			}
		}
	}
	}
	catch (Exception e) {
		// rethrow this to the calling routine
		throw e;
	}
	return(results);
	return null;
}
*/	

/**
Gets the node upstream from the specified node.
@param node the node for which to get the upstream node.
@param flag a flag telling the position to return the node -- POSITION_RELATIVE,
POSITION_ABSOLUTE, POSITION_COMPUTATIONAL.
@return the upstream node from the specified node.
*/
public static HydrologyNode getUpstreamNode(HydrologyNode node, int flag) {
	HydrologyNode nodePt;
	String routine = "HydroBase_NodeNetwork.getUpstreamNode";
	int	dl = 15, i;

	if (flag == POSITION_ABSOLUTE) {
		// It is expected that if you call this you are starting from
		// somewhere on a main stem.  If not, it will only go to the top of a reach.
		if (Message.isDebugOn) {
			Message.printDebug(dl, routine,
				"Trying to find absolute upstream starting with \"" + node.getCommonID() + "\"");
		}
		// Think of the traversal as a "right-hand" traversal.  Always
		// follow the last branch added since it is the most upstream
		// computation-wise.  When we have no more upstream nodes we are at the top...
		nodePt = node;
		if (nodePt.getNumUpstreamNodes() == 0) {
			// Nothing above this node...
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine, "Found absolute upstream: \"" +
					nodePt.getCommonID() + "\"");
			}
			return nodePt;
		}
		else {	
			// Follow the last reach entered for this node (the most upstream)...
			if (node.getUpstreamOrder() == HydrologyNode.TRIBS_ADDED_FIRST) {
				// We want the last one added (makenet order)...
				try {
				return getUpstreamNode(nodePt.getUpstreamNode(
					(nodePt.getNumUpstreamNodes() - 1)), POSITION_ABSOLUTE);
				}
				catch (Exception e) {
					Message.printWarning(2, routine, e);
					return null;
				}
			}
			else {	
				// We want the first one added...
				return getUpstreamNode(nodePt.getUpstreamNode(0), POSITION_ABSOLUTE);
			}
		}
	}
	else if (flag == POSITION_REACH) {
		// Try to find the upstream node in the reach...
		//
		// This amounts to taking the last reachnum for every
		// convergence(do not take tribs)and stop at the top of
		// the reach.  This is almost the same as POSITION_ABSOLUTE
		// except that POSITION_ABSOLUTE will follow confluences at the
		// top of a reach and POSITION_REACH will not.
		if (Message.isDebugOn) {
			Message.printDebug(dl, routine,
				"Trying to find upstream node in reach starting at \"" + node.getCommonID() + "\"");
		}
		//
		// Think of the traversal as a "right-hand" traversal.  Always
		// follow the last branch added since it is the most upstream
		// computation-wise.  When we have no more upstream nodes we
		// are at the top...
		//
		nodePt = node;
		if (nodePt.getNumUpstreamNodes() == 0) {
			// Nothing above this node...
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine,
					"Found absolute upstream: \""
					+ nodePt.getCommonID() + "\"");
			}
			return nodePt;
		}
		else if ((nodePt.getNumUpstreamNodes() == 1)
			&& ((nodePt.getType() == HydrologyNode.NODE_TYPE_CONFLUENCE)
			|| (nodePt.getType() == HydrologyNode.NODE_TYPE_XCONFLUENCE))){
			// If it is a confluence, then it must be at the top of
			// the reach and we want to stop...
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine,
					"Found reach top is confluence - not "
					+ "following: \""
					+ nodePt.getCommonID() + "\"");
			}
			return nodePt;
		}
		else {	
			// Follow the last reach entered for this node
			// (the most upstream).
			if (node.getUpstreamOrder() == HydrologyNode.TRIBS_ADDED_FIRST) {
				// We want the last one added (makenet order)...
				return getUpstreamNode(nodePt.getUpstreamNode(
					(nodePt.getNumUpstreamNodes() - 1)),
					POSITION_REACH);
			}
			else {	
				// Admin tool style...
				return getUpstreamNode(
					nodePt.getUpstreamNode(0),
					POSITION_REACH);
			}
		}
	}
	else if (flag == POSITION_REACH_NEXT) {
		// Get the next upstream node on the same reach.  This will be
		// a node with the same reach counter.
		// FIXME SAM 2011-01-05 never gets beyond i=0 due to checks.
		for (i = 0; i < node.getNumUpstreamNodes(); ) {
			nodePt = node.getUpstreamNode(i);
			if (nodePt == null) {
				// Should not happen if the number of nodes came back OK...
				Message.printWarning(3, routine, "Null upstream node for \"" + node + "\" should not be");
				return null;
			}
			// Return the node that matches the same reach counter
			if (node.getReachCounter() == nodePt.getReachCounter()){
				return nodePt;
			}
			else {	
				// No match...
				return null;
			}
		}
	}
	else if (flag == POSITION_COMPUTATIONAL) {
		// We want to find the next upstream computational node.  Note
		// that this may mean choosing a branch above a convergence
		// point...
		if (Message.isDebugOn) {
			Message.printDebug(dl, routine,
				"Trying to find computational upstream node "
				+ "starting at \""
				+ node.getCommonID() + "\"");
		}
		if (node.getNumUpstreamNodes() == 0) {
			// Then we are at the end of our reach and we need to
			// to to the next reach if available.
			return findReachConfluenceNext(node);
		}
		else {	// We have at least one node upstream but we are only
			// interested in the first one or there is only one
			// upstream node (i.e., we are interested in the next
			// computational node upstream)...
			if (node.getUpstreamOrder() == HydrologyNode.TRIBS_ADDED_FIRST) {
				// We want the first one added (makenet order)...
				if (Message.isDebugOn) {
					Message.printDebug(dl, routine,
						"Going to first upstream node "
						+ "\"" + node.getUpstreamNode(0)
						.getCommonID() + "\"");
				}
				return node.getUpstreamNode(0);
			}
			else {	
				// We want the last one added (admin tool order)
				if (Message.isDebugOn) {
					Message.printDebug(dl, routine,
						"Going to last upstream node \""
						+ node.getUpstreamNode(node
						.getNumUpstreamNodes() - 1)
						.getCommonID() + "\"");
				}
				return node.getUpstreamNode(
					node.getNumUpstreamNodes() - 1);
			}
		}
	}
	else {	
		// Not called correctly...
		Message.printWarning(1, routine,
			"POSITION flag " + flag + " is incorrect");
	}
	return null;
}

/**
Increment the node in reach number for all nodes including this node and 
upstream nodes on the same reach.
@param node the node for which to update the node in reach number for.
*/
public void incrementNodeInReachNumberForReach(HydrologyNode node) {
	if (node == null) {
		return;
	}
	for (HydrologyNode nodePt = node; ;nodePt = nodePt.getUpstreamNode(0)){
		if (nodePt == null) {
			break;
		}
		if (nodePt.getReachCounter() != node.getReachCounter()) {
			break;
		}
		nodePt.setNodeInReachNumber(nodePt.getNodeInReachNumber() + 1);
	}
}

/**
Initialize data.
*/
private void initialize() {
	//In StateMod...__closeCount = 0;
	//In StateMod...__isDatabaseUp = false;
	//In StateMod...__createOutputFiles = true;
	//In StateMod...__createFancyDescription = false;
	__fontSize = 10.0;
	// FIXME 2008-03-15 Need to move to StateDMI
	//__dmi = null;
	__labelType = LABEL_NODES_NETID;
	//__legendDX = 1.0;
	//__legendDY = 1.0;
	//__legendX = 0.0;
	//__legendY = 0.0;
	//In StateMod...__line = 1;
	__nodeCount = 1;
	__nodeDiam = 10.0;
	__nodeHead = null;
	//In StateMod...__openCount = 0;
	//In StateMod...__reachCounter = 0;
	__title = "Node Network";
	__titleX = 0.0;
	__titleY = 0.0;
	__treatDryAsNaturalFlow = false;
}

// insertDownstreamNode - insert a node downstream from a given node
/**
Insert a node downstream from a specified node.  
@param upstreamNode the upstream node from which to insert a downstream node.
@param downstreamNode the downstream node to insert.
*/
public void insertDownstreamNode(HydrologyNode upstreamNode, HydrologyNode downstreamNode) {
	String routine = "HydroBase_NodeNetwork.insertDownstreamNode";

	if (downstreamNode == null) {
		Message.printWarning(2, routine, "Downstream node is null.  Unable to insert");
		return;
	}

	if (upstreamNode == null) {
		if (Message.isDebugOn) {
			Message.printDebug(10, routine, "Setting head of list to downstream node");
		}
		__nodeHead = downstreamNode;
		return;
	}

	// Else we should be able to insert the node...

	if (Message.isDebugOn) {
		Message.printDebug(10, routine, "Adding \"" + downstreamNode.getCommonID()
			+ "\" downstream of \"" + upstreamNode.getCommonID() + "\"");
	}
	upstreamNode.addDownstreamNode(downstreamNode);
} 

/**
Insert upstream HydroBase streams into the network (as nodes).
*/
/*
UNDER CONSTRUCTION
private void insertUpstreamHydroBaseStreams(	HydroBaseDMI dmi,
						HydroBase_Node downstreamNode,
					HydroBase_Stream downstream_stream) {
Message.printStatus(2, "", "SAMX Adding streams upstream of " +
		downstream_stream.getStreamNumber() + " = " +
		downstream_stream.getStream_name());	
	// Query nodes that trib to the specified stream...
	long stream_num_down = downstream_stream.getStreamNumber();
//	HydroBase_StreamQuery q = new HydroBase_StreamQuery(dmi);
	q.addWhereClause("Stream.str_trib_to = " + stream_num_down);
	q.addOrderByClause("Stream.str_mile");
	q.addOrderByClause("Stream.stream_name");
	Vector upstream_streams = dmi.processInternalQuery(q);
	// Add the streams and recurse...
	int size = 0;
	if (upstream_streams != null) {
		size = upstream_streams.size();
	}
	// Remove redundant streams (due to the fact that HydroBase_Stream is 
	// joined
	// with Legacy_stream, which splits streams by WD and may have
	// duplicates for stream numbers).  Also, for some reason some of the
	// streams have themselves included...
	long stream_num_prev = -1, stream_num;
	HydroBase_Stream stream = null;
	for (int i = 0; i < size; i++) {
		stream = (HydroBase_Stream)upstream_streams.elementAt(i);
		stream_num = stream.getStreamNumber();
		if (	(stream_num == stream_num_prev) ||
			(stream_num == stream_num_down)) {
			upstream_streams.removeElementAt(i);
			--i;
			--size;
		}
		stream_num_prev = stream_num;
	}
	Message.printStatus(2, "", "SAMX " + size + " streams trib to " +
		downstream_stream.getStream_name());
	HydroBase_Node node = null;
	for (int i = 0; i < size; i++) {
		stream = (HydroBase_Stream)upstream_streams.elementAt(i);
		Message.printStatus(2, "", "SAMX processing " +
			stream.getStreamNumber() + " " +
			stream.getStream_name());
		node = new HydroBase_Node();
		node.setType(HydroBase_Node.NODE_TYPE_STREAM);
		node.setStreamMile(stream.getStreamMile());
		//node.setLink(currentRow.getWdwater_link());
		node.setDescription(stream.getStream_name());
		node.setCommonID("" + stream.getStreamNumber());
		//node.setSerial(__nodes.size() + 1);

		node.setReachCounter(i + 1);
		node.setTributaryNumber(i + 1);
		node.setReachLevel(downstreamNode.getReachLevel() + 1);
		node.setNodeInReachNumber(i + 1);
		Message.printStatus(2, "", "SAMX - added " + node.toString());
		downstreamNode.addUpstreamNode(node);
		insertUpstreamHydroBaseStreams(dmi, node, stream);
	}
}
*/	

/**
Returns whether the legend position was set from an XML file or not.
@return whether the legend position was set from an XML file or not.
*/
public boolean isLegendPositionSet() {
	return __legendPositionSet;
}

/**
Checks for whether the specified node is the most upstream in the reach.
@param node the node to check
@return true if the node is the most upstream in the reach, false if not.
*/
public boolean isMostUpstreamNodeInReach(HydrologyNode node) {
	String routine = "HydroBase_NodeNetwork.isMostUpstreamNodeInReach";

	// Loop starting upstream of the specified node and moving upstream.
	// Assume that we are the most upstream and try to prove otherwise.
	int nodeType;
	for (HydrologyNode nodePt = getUpstreamNode(node,POSITION_REACH_NEXT);
		((nodePt != null)
		&& (nodePt.getReachCounter() == node.getReachCounter()));
		nodePt = getUpstreamNode(nodePt, POSITION_REACH_NEXT)) {
		nodeType = nodePt.getType();
		if (Message.isDebugOn) {
			Message.printDebug(10, routine,	"Checking node:  " + nodePt.toString());
		}
		if ((nodeType == HydrologyNode.NODE_TYPE_OTHER)
			|| (nodeType == HydrologyNode.NODE_TYPE_DIV)
			|| (nodeType == HydrologyNode.NODE_TYPE_DIV_AND_WELL)
			|| (nodeType == HydrologyNode.NODE_TYPE_WELL)
			|| (nodeType == HydrologyNode.NODE_TYPE_RES)
			|| (nodeType == HydrologyNode.NODE_TYPE_ISF)
			|| (nodeType == HydrologyNode.NODE_TYPE_FLOW)
			|| (nodeType == HydrologyNode.NODE_TYPE_PLAN)) {
			// We found a higher node...
			return false;
		}
		if (nodePt.getUpstreamNode() == null) {
			break;
		}
	}
	return true;
}

/**
Checks to see if a file is an XML file.  Does two simple tests -- if the file
ends with .xml, it is xml.  If the tag "&gt;StateMod_Network" can be found on
a single line, it's a StateMod XML file.
@param filename the name of the file to check.
@return true if the file is an XML net file, false otherwise.
*/
public static boolean isXML(String filename) {
	String routine = "StateMod_Network_JFrame.isXML";
	
	filename = filename.trim();
	if (StringUtil.endsWithIgnoreCase(filename, ".xml")) {
		return true;
	}

	try {
		BufferedReader in =new BufferedReader(new FileReader(filename));
		String line = in.readLine();
	
		while (line != null) {
			line = line.trim();
			if (line.equalsIgnoreCase("<StateMod_Network")) {
				in.close();
				return true;
			}
			line = in.readLine();
		}
		in.close();
	}
	catch (Exception e) {
		Message.printWarning(1, routine, "Error reading from makenet file.  Errors may occur.");
		Message.printWarning(2, routine, e);
	}
	return false;
}

/**
Looks up the location of a structure in the database.
@param identifier the identifier of the structure.
@return a two-element array, the first element of which is the X location
and the second of which is the Y location.  If none can be found, both values will be -1.
*/
// FIXME SAM 2008-03-15 Fix for StateDMI code
/*
public static double[] lookupStateModNodeLocation(HydroBaseDMI dmi, 
String identifier) {
	String routine = "HydroBase_NodeNetwork.lookupStateModNodeLocation";
	double[] loc = new double[2];
	loc[0] = -999.00;
	loc[1] = -999.00;

	String id = identifier;
	int index = id.indexOf(":");
	if (index > -1) {
		id = id.substring(index + 1);
	}

	HydroBase_StructureView structure = null;
	try {
		int[] wdid = HydroBase_WaterDistrict.parseWDID(id);
		structure = dmi.readStructureViewForWDID(wdid[0], wdid[1]);
	}
	catch (Exception e) {
		Message.printWarning(2, routine, "Error reading WDID data.");
		Message.printWarning(2, routine, e);
	}

	if (structure != null) {
		try {
			HydroBase_Geoloc geoloc = dmi.readGeolocForGeoloc_num(
				structure.getGeoloc_num());
			loc[0] = geoloc.getUtm_x();
			loc[1] = geoloc.getUtm_y();
		}
		catch (Exception e) {
			Message.printWarning(2, routine, 
				"Error reading WDID data.");
			Message.printWarning(2, routine, e);
		}
	}

	if (loc[0] > -999.0 && loc[1] > -999.0) {
		return loc;
	}
	
	try {
		HydroBase_StationView station = 
			dmi.readStationViewForStation_id(id);
		if (station == null) {
			return loc;
		}

		HydroBase_Geoloc geoloc = dmi.readGeolocForGeoloc_num(
			station.getGeoloc_num());
		loc[0] = geoloc.getUtm_x();
		loc[1] = geoloc.getUtm_y();
	}
	catch (Exception e) {
		Message.printWarning(2, routine, "Error reading station data.");
		Message.printWarning(2, routine, e);
	}		

	if (loc[0] > -999.0 && loc[1] > -999.0) {
		return loc;
	}
	
	Message.printWarning(2, routine, "Couldn't find locations for "
		+ "identifier '" + identifier + "'; tried WDID and "
		+ "station ID.");

	return loc;
}
*/

/**
Print a message to the data check file.
@param routine Name of routine printing message.
@param type Type of message(W=warning, S=status).
@param message Message to be printed to the file.
*/
public void printCheck(String routine, char type, String message) {
	// Return if the check file is null...
	if (__checkFP == null) {
		return;
	}

	// Format with our normal prefix and suffix...
	String prefix = null;
	if ((type == 'W') || (type == 'w')) {
		prefix = "Warning";
	}
	else {	
		prefix = "Status";
	}

	// Currently don't use the routine...
	__checkFP.print(prefix + ": " + message + __newline);
}

/**
Print a node in the .ord file format.
@param nodeCount Count of nodes (from loop).
@param orderfp PrintWriter to receive output.
@param node Node to print.
@return true if successful, false if not
*/
public boolean printOrdNode(int nodeCount, PrintWriter orderfp, HydrologyNode node) {
	String node_downstream_commonID,
		node_downstream_rivernodeid,
		stype;

	if (node == null) {
		return false;
	}

	stype = HydrologyNode.getTypeString(node.getType(), 1);

	if (node.getDownstreamNode() == null) {
		// We are at the bottom of the system.  Need to set some fields to empty strings...
		node_downstream_commonID = new String("");
		node_downstream_rivernodeid = new String("");
	}
	else {	
		// Use what we have...
		node_downstream_commonID = new String( node.getDownstreamNode().getCommonID());
		node_downstream_rivernodeid = new String( node.getDownstreamNode().getRiverNodeID());
	}
	//try {
		orderfp.println(
		StringUtil.formatString(nodeCount, "%-4d") + " " +
		StringUtil.formatString(node.getReachLevel(), "%-3d") + " " +
		StringUtil.formatString(node.getReachCounter(), "%-4d") + " " +
		StringUtil.formatString(stype, "%-3.3s") + " = " +
		StringUtil.formatString(node.getCommonID(), "%-12.12s") + " = " +
		StringUtil.formatString(node.getRiverNodeID(), "%-12.12s") + " = " +
		StringUtil.formatString(node.getDescription(), "%-24.24s") + " = " +
		StringUtil.formatString(node.getAreaString(), "%-8.8s") + "*" +
		StringUtil.formatString(node.getPrecipString(), "%-6.6s") + "=" +
		StringUtil.formatString(node.getWaterString(), "%-12.12s") + " --> " +
		StringUtil.formatString(node_downstream_commonID, "%-12.12s") + " = " +
		StringUtil.formatString(node_downstream_rivernodeid, "%-12.12s") + " " +
		StringUtil.formatString(node.getX(), "%14.6f") + " " +
		StringUtil.formatString(node.getY(), "%14.6f"));
		//+ StringUtil.formatString(node.getNumUpstreamNodes(), "%3d"));
	//}
	//catch (IOException e) {
	//	Message.printWarning(1, routine,
	//	"Error writing order file");
	//	return 1;
	//}
	return true;
}

/**
Call the IOUtil.printCreatorHeader() method and then append comments indicating the database version.
@param dmi HydroBaseDMI instance.
@param fp PrintWriter to receive output.
@param comment Comment character for header.
@param width Maximum number of characters in header.
@param flag Unused.
*/
/* FIXME SAM 2008-03-15 Move to StateDMI
public static void printCreatorHeader(HydroBaseDMI dmi, PrintWriter fp,
String comment, int width, int flag) {	
	if ((dmi == null) || (fp == null)) {
		return;
	}

	IOUtil.printCreatorHeader(fp, "#", 80, 0);
	try {
		String v[] = dmi.getVersionComments();
		int size = 0;
		if (v != null) {
			size = v.length;
			for (int i = 0; i < size; i++) {
				fp.println(comment + " " + v[i]);
			}
		}
		v = null;
	}
	catch (Exception e) {
		Message.printWarning(2, "", e);
	}
}
*/

/**
Resets the computational order information for each node.
*/
public void resetComputationalOrder() {
	String routine = getClass().getSimpleName() + ".resetComputationalOrder";
	int dl = 30;

	Message.printDebug(dl, routine, "Resetting computational order for nodes");

	// Go to the bottom of the system so that we can get to the top of
	// the main stem...
	HydrologyNode node = null;
	node = getDownstreamNode(__nodeHead, POSITION_ABSOLUTE);

	// Now traverse downstream, creating the strings...
	int order = 1;
	for (HydrologyNode nodePt = getUpstreamNode(node, POSITION_ABSOLUTE);
		nodePt != null;
		nodePt = getDownstreamNode(nodePt, POSITION_COMPUTATIONAL), ++order) {
		nodePt.setComputationalOrder(order);
		if (nodePt.getDownstreamNode() == null) {
			break;
		}
	}
}

/**
Sets the annotations associated with this network.
@param annotationList the list of annotations associated with this network.
*/
public void setAnnotationList(List<HydrologyNode> annotationList) {
	if ( annotationList == null ) {
		__annotationList = new Vector<HydrologyNode>();
	}
	else {
		__annotationList = annotationList;
	}
}

/**
Sets the bounds of the network.
@param lx the left X bound
@param by the bottom Y bound
@param rx the right X bound
@param ty the topy Y bound
*/
protected void setBounds(double lx, double by, double rx, double ty) {
	__netLX = lx;
	__netBY = by;
	__netRX = rx;
	__netTY = ty;
}

/**
 * Set the __by (used when filling in nodes).
 * @param by.
 */
public void setBy ( double by )
{
	__by = by;
}

/**
Set the check file writer.  This is used, for example, by makenet to direct check messages.
@param checkfp PrintWriter to write check message.s
*/
public void setCheckFile(PrintWriter checkfp) {
	__checkFP = checkfp;
}

/**
Set the edge buffer values.
@param edgeBuffer the edge buffer values (left, right, top, bottom, in X,Y data units).
*/
public void setEdgeBuffer ( double [] edgeBuffer )
{
	__edgeBuffer = edgeBuffer;
}

/**
Set the font to use for drawing.
@param font Font name (currently not set)
@param size Size in data units.
*/
protected void setFont(String font, double size) {
		__fontSize = size;
}

/**
Set the name of the input, typically a filename from which the network was read.  It is useful
to save this information so that file choosers to re-save can use the same name.
@param inputName name of the input file that was read for the network.
*/
public void setInputName ( String inputName )
{
	__inputName = inputName;
}

/**
Sets whether this network is in a WIS or not.
@param inWis whether this network is in a WIS or not.
*/
/*
public void setInWIS(boolean inWis) {
	__inWIS = inWis;
	if (__nodeHead == null) {
		return;
	}
	HydrologyNode node = getDownstreamNode(__nodeHead, POSITION_ABSOLUTE);
	for (HydrologyNode nodePt = getUpstreamNode(node, POSITION_ABSOLUTE);
		nodePt != null;
		nodePt = getDownstreamNode(nodePt, POSITION_COMPUTATIONAL)) {
		nodePt.setInWIS(inWis);
		if (nodePt.getDownstreamNode() == null) {
			break;
		}
	}
}
*/
// FIXME SAM 2008-03-15 Need to remove WIS code

/**
Set the label type for plot labelling (see LABEL_NODES_*).
@param label_type Label type.
*/
public void setLabelType(int label_type) {
	__labelType = label_type;
}

/**
Sets the layout list read in from XML.
@param layoutList the layout list read in from XML.
*/
protected void setLayoutList(List<PropList> layoutList)
{
	if ( layoutList == null ) {
		__layoutList = new Vector<PropList>();
	}
	else {
		__layoutList = layoutList;
	}
}

/**
Sets the position of the legend as read from an XML file.
@param legendX the lower-left X coordinate of the legend.
@param legendY the lower-left Y coordinate of the legend.
*/
public void setLegendPosition(double legendX, double legendY) {
	__legendPositionX = legendX;
	__legendPositionY = legendY;
	__legendPositionSet = true;
}

/**
Sets the list of links between nodes in the network.
@param linkList the list of links between nodes in the network.
*/
public void setLinkList(List<PropList> linkList)
{
	if ( linkList == null ) {
		__linkList = new Vector<PropList>();
	}
	else {
		__linkList = linkList;
	}
}

/**
 * Set the __lx (used when filling in nodes).
 * @param lx.
 */
public void setLx ( double lx )
{
	__lx = lx;
}

/**
Sets up the linked list network based on a list of the nodes in the network.
The nodes are checked to see which one is the head of the network and that node
is stored internally as __nodeHead.
@param nodes the list of nodes that comprise a network.
*/
public void setNetworkFromNodes(List<HydrologyNode> nodes) {
	__nodeCount = nodes.size();

	HydrologyNode ds = null;
	for (HydrologyNode node : nodes) {
		ds = node.getDownstreamNode();
		
		if ( (ds == null) || (node.getType() == HydrologyNode.NODE_TYPE_END) ) {
			__nodeHead = node;
			return;
		}
	}
}

// TODO SAM 2008-03-16 The node could should really only be set by addNode(), etc.
/**
 * Set the node count.
 *@param nodeCount Node count for network.
 */
public void setNodeCount ( int nodeCount )
{
	__nodeCount = nodeCount;
}

/**
Set the node diameter used in plotting (data units).
@param node_diam Node symbol diameter.
*/
public void setNodeDiam(double node_diam) {
	__nodeDiam = node_diam;
}

/**
 * Set the head node (most downstream) for the network.
 * @param nodeHead The head node (most downstream) for the network.
 */
public void setNodeHead ( HydrologyNode nodeHead )
{
	__nodeHead = nodeHead;
}

/**
 * Set the node spacing (used when filling in nodes).
 * @param nodeSpacing Node spacing.
 */
public void setNodeSpacing ( double nodeSpacing )
{
	__nodeSpacing = nodeSpacing;
}

/**
 * Set the title for the network.
 * @param title title for the network.
 */
public void setTitle ( String title )
{
	__title = title;
}

/**
 * Set the title X coordinate.
 * @param titleX title X-coordinate.
 */
public void setTitleX ( double titleX )
{
	__titleX = titleX;
}

/**
 * Set the title Y coordinate.
 * @param titleY title Y-coordinate.
 */
public void setTitleY ( double titleY )
{
	__titleY = titleY;
}

/**
 * Return the number of nodes, including the end node.
 * This is reliable whereas getNodeCount() is not (legacy behavior does not always work).
 * @return number of nodes in the network, including the end node.
 */
public int size () {
	int nodeCount = 0;
	for (HydrologyNode node = HydrologyNodeNetwork.getUpstreamNode(getNodeHead(), HydrologyNodeNetwork.POSITION_ABSOLUTE);
        node.getDownstreamNode() != null;
        node = HydrologyNodeNetwork.getDownstreamNode(node, HydrologyNodeNetwork.POSITION_COMPUTATIONAL)) {
        ++nodeCount;
	}
	return nodeCount;
}

/**
Store a stream reach label for plotting later.
The coordinates are stored as the network is read and the information is plotted in createPlotFile().
@param label Label string to store.
@param x1 Starting x-coordinate for reach.
@param y1 Starting y-coordinate for reach.
@param x2 Ending x-coordinate for reach.
@param y2 Ending y-coordinate for reach.
@return true if the label was stored correctly (or was skipped) and false 
if it was not stored correctly.
*/
protected boolean storeLabel(String label, double x1, double y1, double x2,
double y2) {
	String routine = "HydroBase_NodeNetwork.storeLabel";
	// TODO SAM 2007-02-18 Evaluate whether needed
	//double x;
	double y;
	double lx;
	double ly;
	int dl = 20;
	int lflag;

	// Allow special labels to not be printed...
	if (label.equals("?") || label.equalsIgnoreCase("con")) {
		return true;
	}
	if (x1 == x2) {
		// Vertical line...
		if (y2 < y1) {
			// Label at bottom...
			y = y2 - 3 * __nodeDiam;
			ly = y2 - __nodeDiam;
			lflag = GRText.TOP | GRText.CENTER_X;
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine, "Label at bottom:  y=" + y + " \"" + label + "\"");
			}
		}
		else {	
			// Label at top...
			y = y2 + 2 * __nodeDiam;
			ly = y2 + __nodeDiam;
			lflag = GRText.BOTTOM|GRText.CENTER_X;
			if (Message.isDebugOn) {
				Message.printDebug(dl, routine, "Label at top:  y=" + y + " \"" + label + "\"");
			}
		}
		//x = 	x1 - label.length() * 0.25 * __fontSize;
		lx = 	x2;
	}
	else if (y1 == y2) {
		// Horizontal line...
		if (x2 < x1) {
			//x = 	x2 -label.length() * 0.5 * __fontSize - 2 
				//* __nodeDiam;
			lx = 	x2 - __nodeDiam;
			lflag = GRText.RIGHT|GRText.CENTER_Y;
		}
		else {	
			//x = 	x2 + 2 * __fontSize;
			lx = 	x2 + __nodeDiam;
			lflag = GRText.LEFT | GRText.CENTER_Y;
		}
		y = 	y1 - 0.275 * __fontSize;
		ly = 	y2;
	}
	else if (x2 > x1) {
		// Angle to the right.  Put label on right...
		lx = 	x2 + 2 * __nodeDiam;
		ly = 	y2;
		lflag = GRText.LEFT | GRText.CENTER_Y;
	}
	else if (x2 < x1) {
		// Angle to the left.  Put label on left...
		lx	= x2 - 2 * __nodeDiam;
		ly	= y2;
		lflag	= GRText.RIGHT | GRText.CENTER_Y;
	}
	else {	
		Message.printWarning(2, routine,
		"	Do not know how to position label for \"" 
			+ label + "\"");
		return false;
	}

	// Save the label so we can draw in other routine...

	addLabel(lx, ly, __fontSize, lflag, label);
	return true;
}

/**
Indicate whether dry river nodes should be treated as natural flow nodes.  This is
useful, for example, when working with WIS.  It may not be as useful,
for example, when working with StateMod files.  This setting is not migrated
to the HydroBase_Node level.  It is used primarily by the network methods 
that search for natural flow nodes.  See the documentation for each of those 
methods to verify whether dry nodes have the option of being treated as baseflow nodes.
@return true if dry nodes are to be treated as baseflow nodes for select network methods.
*/
public boolean treatDryNodesAsNaturalFlow() {
	return __treatDryAsNaturalFlow;
}

/**
Set whether dry river nodes should be treated as natural flow nodes.
@param treatDryAsNaturalFlow true if dry nodes are to be treated as
natural flow nodes for select methods, false if not.
@return true Always.
*/
public boolean treatDryNodesAsNaturalFlow(boolean treatDryAsNaturalFlow) {
	__treatDryAsNaturalFlow = treatDryAsNaturalFlow;
	return true;
}

/**
Writes a list of Hydrology_Node Objects to a list file.
@param filename the name of the file to write.
@param delimiter the delimiter to use for separating fields (use comma if null).
@param update if true, an existing file will be updated and its header 
maintained.  Otherwise the file will be overwritten.
@param nodes list of HydroBase_Node to write.
@param comments Comment strings to add to the header (null or zero length if no comments should be added).
@param verbose if true, write out columns for all node data; if false, only write the node ID and name
@throws Exception if an error occurs.
*/
public static void writeListFile ( String filename, String delimiter, boolean update,
	List<HydrologyNode> nodes, String [] comments, boolean verbose )
throws Exception
{	if ( delimiter == null ) {
		delimiter = ",";
	}
	
	String[] namesShort = { 
		"ID", 
		"Name" 
	};
	String[] formatsShort = {
		"%-20.20s",
		"%-80.80s"
	};
	String[] names = namesShort;
	String[] formats = formatsShort;
	if ( verbose ) {
		String[] namesVerbose = { 
			"ID", 
			"Name",
			"Type",
			"X",
			"Y",
			"DownstreamCommonID",
			"UpstreamCommonID",
			"DownstreamID",
			"UpstreamID"			
		};
		String[] formatsVerbose = {
			"%-20.20s",
			"%-80.80s",
			"%s",
			"%.6f",
			"%.6f",
			"%s",
			"%s",
			"%s",
			"%s"
		};
		names = namesVerbose;
		formats = formatsVerbose;
	}
	int fieldCount = names.length;
	
	String oldFile = null;	
	if (update) {
		oldFile = IOUtil.getPathUsingWorkingDir(filename);
	}
	
	int j = 0;
	PrintWriter out = null;
	String[] commentString = { "#" };
	String[] ignoreCommentString = { "#>" };
	String[] line = new String[fieldCount];
	StringBuffer buffer = new StringBuffer();

	try {	
		out = IOUtil.processFileHeaders(
			oldFile,
			IOUtil.getPathUsingWorkingDir(filename), 
			comments, commentString, ignoreCommentString, 0);

		for (int iField = 0; iField < fieldCount; iField++) {
			buffer.append("\"" + names[iField] + "\"");
			if (iField < (fieldCount - 1)) {
				buffer.append(delimiter);
			}
		}

		out.println(buffer.toString());
		
		HydrologyNode downstreamNode;
		List<HydrologyNode> upstreamNodes;
		String s;
		StringBuffer b = new StringBuffer();
		String [] upstreamIDs;
		int iField;
		for ( HydrologyNode node: nodes ) {
			iField = 0;
			
			line[iField] = StringUtil.formatString(node.getCommonID(), formats[iField]).trim();
			++iField;
			line[iField] = StringUtil.formatString(node.getDescription(), formats[iField]).trim();
			++iField;
			if ( verbose ) {
				// Get more information
				line[iField] = StringUtil.formatString(HydrologyNode.getVerboseType(node.getType()), formats[iField]).trim();
				++iField;
				line[iField] = StringUtil.formatString(node.getX(), formats[iField]).trim();
				++iField;
				line[iField] = StringUtil.formatString(node.getY(), formats[iField]).trim();
				++iField;
				// Downstream common ID
				downstreamNode = node.getDownstreamNode();
				s = "";
				if ( downstreamNode != null ) {
					s = downstreamNode.getCommonID();
				}
				line[iField] = StringUtil.formatString(s, formats[iField]).trim();
				++iField;
				// Upstream common ID
				upstreamNodes = node.getUpstreamNodes();
				b.setLength(0);
				if ( upstreamNodes != null ) {
					for ( HydrologyNode node2: upstreamNodes ) {
						if ( node2 != null ) {
							if ( b.length() > 0 ) {
								b.append(",");
							}
							b.append(node2.getCommonID());
						}
					}
				}
				line[iField] = StringUtil.formatString(b.toString(), formats[iField]).trim();
				++iField;
				// Downstream ID
				s = node.getDownstreamNodeID();
				if ( s == null ) {
					s = "";
				}
				line[iField] = StringUtil.formatString(s, formats[iField]).trim();
				++iField;
				// Upstream IDs
				b.setLength(0);
				upstreamIDs = node.getUpstreamNodeIDs();
				if ( upstreamIDs != null ) {
					for ( int k = 0; k < upstreamIDs.length; k++ ) {
						if ( upstreamIDs[k] != null ) {
							if ( b.length() > 0 ) {
								b.append(",");
							}
							b.append(upstreamIDs[k]);
						}
					}
				}
				line[iField] = StringUtil.formatString(b.toString(), formats[iField]).trim();
				++iField;
			}

			buffer = new StringBuffer();	
			for (j = 0; j < fieldCount; j++) {
				if (line[j].indexOf(delimiter) > -1) {
					line[j] = "\"" + line[j] + "\"";
				}
				buffer.append(line[j]);
				if (j < (fieldCount - 1)) {
					buffer.append(delimiter);
				}
			}

			out.println(buffer.toString());
		}
	}
	finally {
		if (out != null) {
			out.flush();
			out.close();
		}
	}
}

/**
Writes the network out as an XML network file.  This method should be used
when writing out a network that was already read from an XML file.  
@param filename the name of the file to which to write.
@param networkExtent the extent of the network corresponding to the XMin, Ymin, XMax, YMax properties
in the XML file, which should consider the page layout.  If null, the extent will be computed from data.
@throws Exception if there is an error writing the network.
*/
public void writeXML(String filename ) 
throws Exception
{
	// FIXME SAM 2011-01-03 Problem is that if a network is read from XML and then
	// immediately written without supplying limits, the limits are recomputed from data and not the
	// page layout.
	// What could be done is to get the extent from the data (including legend and annotations?) and
	// then center on the page limits with some buffer around the edge nodes to allow for labels, and growth.
	writeXML(filename, determineExtentFromNetworkData(), getLayoutList(), getAnnotationList(),
		getLinkList(), getLegendLocation(), getEdgeBuffer() );
}

/**
Writes the network out as an XML network file.  This method will be used
primarily by programs that need to write out a new XML network, or which
read in an old (non-XML) network file and want to write out an XML version.
@param filename the name of the file to which to write.
@param limits the limits of the network.  They will be used to write out the
extents of the network.  Can be null, in which case they will be calculated by a call to getExtents().
@param layouts the page layouts to be written.  Each layout in this Vector 
will be written as a separate PageLayout section.  Can be null or empty, in 
which case no layouts will be written to the file.
@param annotations the annotations to be written.  Each annotation in this 
Vector will be written as a separate Annotation section.  Can be null or empty,
in which case no annotations will be written to the file.
@param links the links to be written.  Each link in this Vector represents a 
line joining two nodes in the network, and will be written as a separate
Link section.  Can be null or empty, in which case no links will be written to the file.
@param legendLimits the legend limits to be written.  These limits will be
used to set the lower-left position of the legend in the network.  Can be null,
in which case no legend limits will be written and the legend will be 
automatically placed on the network next time the network is opened from the file.
@throws Exception if there is an error writing the network.
*/
public void writeXML(String filename, GRLimits limits, List<PropList> layouts,
		List<HydrologyNode> annotations, List<PropList> links, GRLimits legendLimits, double [] edgeBuffer ) 
throws Exception
{	String routine = getClass().getName() + ".writeXML";
	Message.printStatus(2, routine, "Writing XML network with limits=" + limits );
	filename = IOUtil.getPathUsingWorkingDir(filename);
	if (limits == null) {
		limits = determineExtentFromNetworkData();
		Message.printStatus(2, routine, "Calculated XML network limits=" + limits );
	}
	PrintWriter out = new PrintWriter(new FileOutputStream(filename));

	String n = System.getProperty("line.separator");

	String comment = "#> ";
	out.print("<!--" + n);
	out.print(comment + "" + n);
	out.print(comment + " StateMod XML Network File" + n);
	out.print(comment + "" + n);

	PropList props = new PropList("");
	props.set("IsXML=true");

	IOUtil.printCreatorHeader(out, comment, 80, 0, props);

String format = 
  comment + n
+ comment + "The StateMod XML network file is a generalized representation" + n
+ comment + "of the StateMod network.  It includes some of the information" + n
+ comment + "in the StateMod river network file (*.rin) but also includes" + n
+ comment + "spatial layout information needed to produce a diagram of the" + n
+ comment + "network.  The XML includes top-level properties for the" + n
+ comment + "network, and data elements for each node in the network." +n
+ comment + "Each network node is represented as a single XML element" + n
+ comment + "Node properties are stored as property = \"value\"." + n
+ comment + "" + n
+ comment + "Node connections are specified by either a " + n
+ comment + "     <DownstreamNode ID = \"Node ID\"/> " + n
+ comment + "or" + n
+ comment + "     <UpstreamNode ID = \"Node ID\"/>" + n
+ comment + "tag.  There may be more than one upstream node, but at most " + n
+ comment + "one downstream node." + n
+ comment + n;
	out.print(format);	
format = 
  comment + "The XML network is typically created in one of three ways:" + n
+ comment + "" + n
+ comment + "1) An old \"makenet\" (*.net) file is read and converted to " + n
+ comment + "XML (e.g., in StateDMI).  In this case, some internal" + n
+ comment + "identifiers (e.g., for confluence nodes) will be defaulted in" + n
+ comment + "order to have unique identifiers, and the coordinates will be" + n
+ comment + "those from the Makenet file, in order to preserve the diagram" + n
+ comment + "appearance from in the original Makenet file." + n
+ comment + n;
	out.print(format);	
format = 
  comment + "2) A StateMod river network file (*.rin) file is converted to" + n
+ comment + "XML (e.g., by StateDMI).  In this case, confluence nodes will" + n
+ comment + "not be present and StateDMI can be used to set the coordinates" + n
+ comment + "to actual physical coordinates (e.g., UTM).  The coordinates" + n
+ comment + "in the diagram will need to be repositioned to match a" + n
+ comment + "straight-line representation, if such a representation is" + n
+ comment + "desired." + n
+ comment + n
+ comment + "3) A new network is created entirely within the StateDMI or" + n
+ comment + "StateModGUI interface.  In this case, the positioning of nodes" + n
+ comment + "can occur as each node is defined in the network, or can occur" + n
+ comment + "at the end." + n
+ comment + n
+ comment + "Once a generalized XML network is available, StateDMI can be" + n
+ comment + "used to create StateMod station files.  The node type and the" +n
+ comment + "\"IsNaturalFlow\" property are used to determine lists of" + n
+ comment + "stations for various files." + n
+ comment + n;
	out.print(format);	
format = 
  comment + "The following properties are used in this file.  Elements are" + n
+ comment + "indicated in <angle brackets> with element properties listed" + n
+ comment + "below each element." + n
+ comment + n
+ comment + "NOTE:" + n
+ comment + n
+ comment + "If any of the following have an ampersand (&), greater than (>)" +n
+ comment + "or less than (<) in them, these values MUST be escaped (see" + n
+ comment + "below):" + n
+ comment + "   - Page Layout ID" + n
+ comment + "   - Node ID" + n
+ comment + "   - Downstream Node ID" + n
+ comment + "   - Upstream Node ID" + n
+ comment + "   - Link Upstream Node ID" + n
+ comment + "   - Link Downstream Node ID" + n
+ comment + "   - Annotation Text" + n
+ comment + n
+ comment + "The escape values are the following.  These are automatically " + n
+ comment + "inserted by the network-saving software, if the characters are" + n
+ comment + "inserted when editing a network programmatically, but if the " + n
+ comment + "network is edited by hand they must be inserted manually." + n
+ comment + n
+ comment + "   &   ->   &amp;" + n
+ comment + "   >   ->   &gt;" + n
+ comment + "   <   ->   &lt;" + n
+ comment + n
+ comment + "<StateMod_Network>     Indicates the bounds of network" + n
+ comment + "                       definition, based on node data placed" + n
+ comment + "                       to fit on the page size for the layout." + n
+ comment + n
+ comment + "   XMin                The minimum X coordinate used to " + n
+ comment + "                       display the network, determined from"  +n
+ comment + "                       node coordinates." + n
+ comment + n
+ comment + "   YMin                The minimum Y coordinate used to " + n
+ comment + "                       display the network, determined from"  +n
+ comment + "                       node coordinates." + n
+ comment + n
+ comment + "   XMax                The maximum X coordinate used to " + n
+ comment + "                       display the network, determined from"  +n
+ comment + "                       node coordinates." + n
+ comment + n
+ comment + "   YMax                The maximum Y coordinate used to " + n
+ comment + "                       display the network, determined from"  +n
+ comment + "                       node coordinates." + n
+ comment + n
+ comment + "   EdgeBufferLeft      Extra edge in node coordinate units to add" + n
+ comment + "                       to the left of the left-most " + n
+ comment + "                       node, to allow for labels and annotations." + n
+ comment + n
+ comment + "   EdgeBufferRight     Extra edge in node coordinate units to add" + n
+ comment + "                       to the right of the left-most " + n
+ comment + "                       node, to allow for labels and annotations." + n
+ comment + n
+ comment + "   EdgeBufferTop       Extra edge in node coordinate units to add above" + n
+ comment + "                       the top-most node, to allow for labels and annotations." + n
+ comment + n
+ comment + "   EdgeBufferRight     Extra edge in node coordinate units to add below" + n
+ comment + "                       the bottom-most node, to allow for labels and annotations." + n
+ comment + n
+ comment + "   LegendX             The X coordinate of the lower-left point" +n
+ comment + "                       of the legend." + n
+ comment + n
+ comment + "   LegendY             The Y coordinate of the lower-left point" +n
+ comment + "                       of the legend." + n
+ comment + n
+ comment + "   <PageLayout>        Indicates properties for a page layout," + n
+ comment + "                       resulting in a reasonable representation"+ n
+ comment + "                       of the network in hard copy.  One or " + n
+ comment + "                       more page layouts may be provided in " + n
+ comment + "                       order to support printing on various" + n
+ comment + "                       sizes of paper." + n
+ comment + n
+ comment + "      IsDefault        Indicates whether the page layout is the" +n
+ comment + "                       one that should be loaded automatically" + n
+ comment + "                       when the network is first displayed.  " +n
+ comment + "                       Only one PageLayout should have " + n
+ comment + "                       this with a value of \"True\"." + n
+ comment + "                       Recognized values are: " + n
+ comment + "                          True" + n
+ comment + "                          False" + n
+ comment + n
+ comment + "      PaperSize        Indicates the paper size for a page " + n
+ comment + "                       layout.  Recognized values are:" + n
+ comment + "                          11x17     - 11x17 inches" + n
+ comment + "                          A         - 8.5x11 inches" + n
+ comment + "                          B         - 11x17 inches" + n
+ comment + "                          C         - 17x22 inches" + n
+ comment + "                          D         - 22x34 inches" + n
+ comment + "                          E         - 34x44 inches" + n
+ comment + "                          Executive - 7.5x10 inches" + n
+ comment + "                          Legal     - 8.5x14 inches" + n
+ comment + "                          Letter    - 8.5x11 inches" + n
+ comment + n;
	out.print(format);	
format = 
  comment + "      PageOrientation  Indicates the orientation of the printed" +n
+ comment + "                       page.  Recognized values are: " + n
+ comment + "                          Landscape" + n
+ comment + "                          Portrait" + n
+ comment + n
+ comment + "      NodeLabelFontSize Indicates the size (in points) of the" + n
+ comment + "                       font used for node labels.  72 points = 1 inch." + n
+ comment + n
+ comment + "      NodeSize         Indicates the size (in points) of the" + n
+ comment + "                       symbol used to represent a node.  72 points = 1 inch." + n
+ comment + n
+ comment + "   <Node>              Data element for a node in the network." + n
+ comment + n
+ comment + "      ID               Identifier for the node, matching the " + n
+ comment + "                       label on the diagram and the identifier" + n
+ comment + "                       in the StateMod files.  It is assumed" + n
+ comment + "                       that the station identifier and river" + n
+ comment + "                       node identifier are the same.  The " + n
+ comment + "                       identifier usually matches a State of " + n
+ comment + "                       Colorado WDID, USGS gage ID, or other " + n
+ comment + "                       standard identifier that can be queried."+ n
+ comment + "                       Aggregate or \"other\" nodes use" + n
+ comment + "                       identifiers as per modeling procedures." + n
+ comment + n
+ comment + "      Area             The natural flow contributing area." + n
+ comment + n;
	out.print(format);	
format = 
  comment + "      AlternateX       The physical coordinates for the node," + n
+ comment + "      AlternateY       typically the UTM coordinate taken from" + n
+ comment + "                       HydroBase or another data source." + n
+ comment + n
+ comment + "      Description      A description/name for the node, " + n
+ comment + "                       typically taken from HydroBase or" + n
+ comment + "                       another data source." + n
+ comment + n
+ comment + "      IsNaturalFlow    If \"true\", then the node is a location" +n
+ comment + "                       where stream flows will be estimated" + n
+ comment + "                       (and a station will be listed in the" + n
+ comment + "                       StateMod stream estimate station file)." + n
+ comment + "                       This property replaces the old IsBaseflow property." + n
+ comment + n
+ comment + "      IsImport         If \"true\", then the node is an import" +n
+ comment + "                       node, indicating that water will be" + n
+ comment + "                       introduced into the stream network at" + n
+ comment + "                       the node.  This is commonly used to" + n
+ comment + "                       represent transbasin diversions.  This" + n
+ comment + "                       property is only used to indicate how" + n
+ comment + "                       the node should be displayed in the" + n
+ comment + "                       network diagram." + n
+ comment + n
+ comment + "      LabelPosition    The position of the node label, relative"+ n
+ comment + "                       to the node symbol.  Recognized values" + n
+ comment + "                       are:" + n
+ comment + "                          AboveCenter" + n
+ comment + "                          UpperRight" + n
+ comment + "                          Right" + n
+ comment + "                          LowerRight" + n
+ comment + "                          BelowCenter" + n
+ comment + "                          LowerLeft" + n
+ comment + "                          Left" + n
+ comment + "                          UpperLeft" + n
+ comment + "                          Center" + n
+ comment + n
+ comment + "      Precipitation    The natural flow contributing area precipitation ." + n
+ comment + n;
	out.print(format);	
format = 
  comment + "      Type             The node type.  This information is used"+ n
+ comment + "                       by software like StateDMI to extract" + n
+ comment + "                       lists of nodes, for data processing." + n
+ comment + "                       Recognized values are:" + n
+ comment + "                          Confluence" + n
+ comment + "                          Diversion" + n
+ comment + "                          Diversion and Well" + n
+ comment + "                          End" + n
+ comment + "                          Instream Flow" + n
+ comment + "                          Other" + n
+ comment + "                          Reservoir" + n
+ comment + "                          Streamflow" + n
+ comment + "                          Well" + n
+ comment + "                          XConfluence" + n
+ comment + n;
	out.print(format);	
format = 
  comment + "      X                The coordinates used to display the node"+ n
+ comment + "      Y                in the diagram.  These coordinates may" + n
+ comment + "                       match the physical coordinates exactly," + n
+ comment + "                       may be interpolated from the coordinates"+ n
+ comment + "                       of neighboring nodes, or may be the " + n
+ comment + "                       result of an edit." + n
+ comment + n
+ comment + "      <DownstreamNode> Information about nodes downstream " + n
+ comment + "                       from the current node.  This information"+ n
+ comment + "                       is used to connect the nodes in the" + n
+ comment + "                       network and is equivalent to the " + n
+ comment + "                       StateMod river network file (*.rin)" + n
+ comment + "                       \"cstadn\" data.  Currently only one" + n
+ comment + "                       downstream node is allowed." + n
+ comment + n
+ comment + "         ID            Identifier for the node downstream from" + n
+ comment + "                       the current node." + n
+ comment + n
+ comment + "      <UpstreamNode>   Information about nodes upstream from the"+n
+ comment + "                       current node.  Repeat for all nodes " + n
+ comment + "                       upstream of the current node." + n
+ comment + n
+ comment + "         ID            Identifier for the node upstream from" + n
+ comment + "                       the current node." + n
+ comment + n;
	out.print(format);	
format = 
  comment + "   <Annotation>        Data element for a network annotation." + n
+ comment + n
+ comment + "         FontName      The name of the font in which the " + n
+ comment + "                       annotation is drawn.  Recognized values" + n
+ comment + "                       are:" + n
+ comment + "                          Arial" + n
+ comment + "                          Courier" + n
+ comment + "                          Helvetica" + n
+ comment + n
+ comment + "         FontSize      The size of the font in which the " + n    
+ comment + "                       annotation is drawn." + n
+ comment + n
+ comment + "         FontStyle     The style of the font in which the " + n
+ comment + "                       annotation is drawn.  Recognized values" + n
+ comment + "                       are: " + n
+ comment + "                          Plain" + n
+ comment + "                          Italic" + n
+ comment + "                          Bold" + n
+ comment + "                          BoldItalic" + n
+ comment + n;
	out.print(format);	
format = 
  comment + "         Point         The point at which to draw the " + n
+ comment + "                       annotation.  The value of \"Point\"" + n
+ comment + "                       must be two numeric values separated by" + n
+ comment + "                       a single comma.  E.g:" + n
+ comment + "                          Point=\"77.44,9.0\"" + n
+ comment + n
+ comment + "         ShapeType     The type of shape of the annotation." + n
+ comment + "                       The only recognized value is:" + n
+ comment + "                          Text" + n
+ comment + n
+ comment + "         Text          The text to be drawn on the network." + n
+ comment + n
+ comment + "         TextPosition  The position the text will be drawn, " + n
+ comment + "                       relative to the \"Point\" value." + n
+ comment + "                       Recognized values are: " + n
+ comment + "                          AboveCenter" + n
+ comment + "                          UpperRight" + n
+ comment + "                          Right" + n
+ comment + "                          LowerRight" + n
+ comment + "                          BelowCenter" + n
+ comment + "                          LowerLeft" + n
+ comment + "                          Left" + n
+ comment + "                          UpperLeft" + n
+ comment + "                          Center" + n
+ comment + n;
	out.print(format);	
format = 
  comment + "   <Link>              Data element for a network link, used to" + n
+ comment + "                       indicate relationships between nodes but are" + n
+ comment + "                       not used by StateMod." + n
+ comment + n
+ comment + "         ID            The link identifier, for editing purposes." + n
+ comment + n              
+ comment + "         ShapeType     The type of shape being drawn.  The only"+ n
+ comment + "                       recognized value is: " + n
+ comment + "                          Link" + n
+ comment + n
+ comment + "         FromNodeID    The ID of the node from which the link" + n
+ comment + "                       is drawn." + n
+ comment + n  
+ comment + "         ToNodeID      The ID of the node to which the link" + n
+ comment + "                       is drawn." + n
+ comment + n
+ comment + "         LineStyle     The style in which the link line is " + n
+ comment + "                       drawn.  Recognized values are: " + n
+ comment + "                          Dashed" + n
+ comment + "                          Solid" + n
+ comment + n
+ comment + "         FromArrowStyle" + n
+ comment + "                       The arrow on the FromNodeID side: " + n
+ comment + "                          None" + n
+ comment + "                          Solid" + n
+ comment + n
+ comment + "         ToArrowStyle" + n
+ comment + "                       The arrow on the ToNodeID side: " + n
+ comment + "                          None" + n
+ comment + "                          Solid" + n
+ comment + n;
	out.print(format);	

	out.print(comment + "" + n);
	out.print(comment + "EndHeader" + n);
	out.print("-->" + n);

	// the next line must ALWAYS be formatted like this -- it is used
	// to check in isXML() to tell if the net file is in XML format.
	out.print("<StateMod_Network " + n);

	// If the limits are all zeros, set the xmax and ymax to 1, just to have something to
	// visualize and prevent divide by zeros
	if ( limits != null ) {
		if ( (limits.getLeftX() == 0.0) && (limits.getRightX() == 0.0) ) {
			limits.setRightX(1.0);
		}
		if ( (limits.getBottomY() == 0.0) && (limits.getTopY() == 0.0) ) {
			limits.setTopY(1.0);
		}
	}
	if ( limits != null ) {
		out.print("    XMin = \"" 
			+ StringUtil.formatString(limits.getLeftX(), "%13.6f").trim() + "\"" + n);
		out.print("    YMin = \"" 
			+ StringUtil.formatString(limits.getBottomY(), "%13.6f").trim() + "\"" + n);
		out.print("    XMax = \"" 
			+ StringUtil.formatString(limits.getRightX(), "%13.6f").trim() + "\"" + n);
		out.print("    YMax = \"" 
			+ StringUtil.formatString(limits.getTopY(), "%13.6f").trim() + "\"");
	}
	if ( edgeBuffer != null ) {
		out.print(n + "    EdgeBufferLeft = \"" 
			+ StringUtil.formatString(edgeBuffer[0], "%13.6f").trim() + "\"" + n);
		out.print("    EdgeBufferRight = \"" 
			+ StringUtil.formatString(edgeBuffer[1], "%13.6f").trim() + "\"" + n);
		out.print("    EdgeBufferTop = \"" 
			+ StringUtil.formatString(edgeBuffer[2], "%13.6f").trim() + "\"" + n);
		out.print("    EdgeBufferBottom = \"" 
			+ StringUtil.formatString(edgeBuffer[3], "%13.6f").trim() + "\"");
	}
	if ( legendLimits != null ) {
		out.print(n + "    LegendX = \"" 
			+ StringUtil.formatString(legendLimits.getLeftX(), "%13.6f").trim()+ "\"" + n);
		out.print("    LegendY = \"" 
			+ StringUtil.formatString(legendLimits.getBottomY(), "%13.6f").trim()+ "\"" );
	}
	// Close the StateMod_Network element
	out.print(">" + n );

	if ( (layouts == null) || (layouts.size() == 0) ) { 
		// Add a default layout...
		out.print("    <PageLayout ID = \"Page Layout #1\"" + n);
		out.print("        PaperSize = \"C\"" + n);
		out.print("        PageOrientation = \"Landscape\"" + n);
		out.print("        NodeLabelFontSize = \"10\"" + n);
		out.print("        NodeSize = \"20\"/>" + n);
		out.print("        IsDefault = \"True\"/>" + n);
	}
	else {
		String id = null;
		String isDefault = null;
		String paperFormat = null;
		String orient = null;
		String sFontSize = null;
		String sNodeSize = null;
		for ( PropList layout: layouts ) {
			id = layout.getValue("ID");
			isDefault = layout.getValue("IsDefault");
			paperFormat = layout.getValue("PaperSize");
			int index = paperFormat.indexOf("-");
			if (index > -1) {
				paperFormat = paperFormat.substring(0, index);
				paperFormat = paperFormat.trim();
			}
			orient = layout.getValue("PageOrientation");
			sFontSize = layout.getValue("NodeLabelFontSize");
			sNodeSize = layout.getValue("NodeSize");
			id = StringUtil.replaceString(id, "&", "&amp;");
			id = StringUtil.replaceString(id, "<", "&lt;");
			id = StringUtil.replaceString(id, ">", "&gt;");
			out.print("    <PageLayout ID = \"" + id + "\"" + n);
			out.print("        IsDefault = \"" + isDefault + "\"" + n);
			out.print("        PaperSize = \"" + paperFormat + "\"" + n);
			out.print("        PageOrientation = \"" + orient + "\"" + n);
			out.print("        NodeLabelFontSize = \"" + sFontSize + "\"" + n);
			out.print("        NodeSize = \"" + sNodeSize + "\"/>" + n);		
		}
	}

	HydrologyNode holdNode = null;
	HydrologyNode node = getMostUpstreamNode();	
	boolean done = false;
	while (!done) {
		node.writeNodeXML(out, false);
		if (node.getType() == HydrologyNode.NODE_TYPE_END) {
			done = true;
		}		
		else if (node == holdNode) {
			done = true;
		}

		holdNode = node;	
		node = getDownstreamNode(node, POSITION_COMPUTATIONAL);	
	}

	String text = null;
	if ( (annotations != null) && (annotations.size() > 0)) {
		int size = annotations.size();
		PropList p = null;
		for (int i = 0; i < size; i++) {
			node = annotations.get(i);
			p = (PropList)node.getAssociatedObject();
			out.print("    <Annotation" + n);
			out.print("         ShapeType=\"Text\"" + n);

			text = p.getValue("Text");
			text = StringUtil.replaceString(text, "&", "&amp;");
			text = StringUtil.replaceString(text, "<", "&lt;");
			text = StringUtil.replaceString(text, ">", "&gt;");
			out.print("         Text=\"" + text + "\"" + n);
			out.print("         Point=\"" + p.getValue("Point")	+ "\"" + n);
			out.print("         TextPosition=\"" + p.getValue("TextPosition") + "\"" + n);
			out.print("         FontName=\"" + p.getValue("FontName") + "\"" + n);
			out.print("         FontStyle=\"" + p.getValue("FontStyle") + "\"" + n);
			out.print("         FontSize=\"" + p.getValue("OriginalFontSize") + "\"/>" + n);
		}
	}

	String linkData = null;

	if ( (links != null) && (links.size() > 0) ) {
		out.print("" + n);
		for ( PropList p : links ) {
			out.print("    <Link" + n);
			out.print("         ShapeType=\"Link\"" + n);

			linkData = p.getValue("ID");
			if ( linkData == null ) {
				linkData = "";
			}
			linkData = StringUtil.replaceString(linkData, "&", "&amp;");
			linkData = StringUtil.replaceString(linkData, "<", "&lt;");
			linkData = StringUtil.replaceString(linkData, ">", "&gt;");
			out.print("         ID=\"" + linkData + "\"" + n);
			
			linkData = p.getValue("FromNodeID");
			if ( linkData == null ) {
				linkData = "";
			}
			linkData = StringUtil.replaceString(linkData, "&", "&amp;");
			linkData = StringUtil.replaceString(linkData, "<", "&lt;");
			linkData = StringUtil.replaceString(linkData, ">", "&gt;");
			out.print("         FromNodeID=\"" + linkData + "\"" + n);

			linkData = p.getValue("ToNodeID");
			if ( linkData == null ) {
				linkData = "";
			}
			linkData = StringUtil.replaceString(linkData, "&", "&amp;");
			linkData = StringUtil.replaceString(linkData, "<", "&lt;");
			linkData = StringUtil.replaceString(linkData, ">", "&gt;");
			out.print("         ToNodeID=\"" + linkData + "\"" + n);
			
			linkData = p.getValue("LineStyle");
			if ( linkData == null ) {
				linkData = "";
			}
			linkData = StringUtil.replaceString(linkData, "&", "&amp;");
			linkData = StringUtil.replaceString(linkData, "<", "&lt;");
			linkData = StringUtil.replaceString(linkData, ">", "&gt;");
			out.print("         LineStyle=\"" + linkData + "\"" + n);
			
			linkData = p.getValue("FromArrowStyle");
			if ( linkData == null ) {
				linkData = "";
			}
			linkData = StringUtil.replaceString(linkData, "&", "&amp;");
			linkData = StringUtil.replaceString(linkData, "<", "&lt;");
			linkData = StringUtil.replaceString(linkData, ">", "&gt;");
			out.print("         FromArrowStyle=\"" + linkData + "\"" + n);
			
			linkData = p.getValue("ToArrowStyle");
			if ( linkData == null ) {
				linkData = "";
			}
			linkData = StringUtil.replaceString(linkData, "&", "&amp;");
			linkData = StringUtil.replaceString(linkData, "<", "&lt;");
			linkData = StringUtil.replaceString(linkData, ">", "&gt;");
			out.print("         ToArrowStyle=\"" + linkData + "\"/>" + n); // Close element
		}
	}

	out.print("" + n);
	out.print("</StateMod_Network>" + n);
	out.close();
}

}
