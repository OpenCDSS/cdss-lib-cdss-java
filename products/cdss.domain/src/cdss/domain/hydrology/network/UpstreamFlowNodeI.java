package cdss.domain.hydrology.network;

/**
Interface to allow an application to indicate whether a node should be treated
as the upstream flow node for a requested node.  This interface is used to connect the more
generic HydroBase_NodeNetwork.findUpstreamFlowNodes() method with the StateMod_PrfGageData
instances used by StateDMI when processing the stream estimate coefficient data.  Using this
interface allows HydrologyNodeNetwork to NOT import StateMod package resources.
*/
public interface UpstreamFlowNodeI {

	/**
	Checks to see if the node is one where a "neighboring gage" is to be used for the upstream
	flow node.  See the StateDMI SetStreamEstimateCoefficientsPFGage() command.
	@param commonID the id of the node to check
	@return the number of the node if it is to receive information, or -1 if
	the node wasn't found or is not to receive information.
	*/
	public int isSetprfTarget ( String commonID );
}