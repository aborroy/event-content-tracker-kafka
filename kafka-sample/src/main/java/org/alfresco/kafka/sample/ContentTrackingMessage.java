package org.alfresco.kafka.sample;

/**
 * Sample Kafka message to be expressed in JSON notation.
 * 
 * {
 *   "txId":"1",
 *   "nodeId":"2"
 * }
 * 
 * @author aborroy
 *
 */
public class ContentTrackingMessage
{

	private String txId;
	private String nodeId;

	public String getTxId()
	{
		return txId;
	}

	public void setTxId(String txId)
	{
		this.txId = txId;
	}

	public String getNodeId()
	{
		return nodeId;
	}

	public void setNodeId(String nodeId)
	{
		this.nodeId = nodeId;
	}

	@Override
	public String toString()
	{
		return "ContentTrackingMessage [txId=" + txId + ", nodeId=" + nodeId + "]";
	}

}