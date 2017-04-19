## Example

A telecomunications company wishes to use Salesforce as a front end for creating new accounts using the Lead to Opportunity process.  The creation of an order is initiated in Salesforce once the opportunity is closed and won, but the back end ERP system will be the data master.  The order must be subsequently saved to the Salesforce opportunity record and the opportunity status changed to indicate that the order was created.

The following constraints apply:
 * The ERP system is capable of participating in a contract-first integration, where its service must implement a Salesforce WSDL interface.  
 * There should be custom development in Salesforce.
 * The user doesn't need to be immediately notified of the order number after the opportunity converts to an order.

This example is best implemented using Salesforce outbound messaging, but does not require the implementation of a proxy service by the remote system:

On the Salesforce side:


