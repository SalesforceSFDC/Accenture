## Example

A telecomunications company wishes to use Salesforce as a front end for creating new accounts using the Lead to Opportunity process.  The creation of an order is initiated in Salesforce once the opportunity is closed and won, but the back end ERP system will be the data master.  The order must be subsequently saved to the Salesforce opportunity record and the opportunity status changed to indicate that the order was created.

The following constraints apply:
 * The ERP system is capable of participating in a contract-first integration, where its service must implement a Salesforce WSDL interface.  
 * There should be custom development in Salesforce.
 * The user doesn't need to be immediately notified of the order number after the opportunity converts to an order.

This example is best implemented using Salesforce outbound messaging, but does not require the implementation of a proxy service by the remote system:

On the Salesforce side:
 * Create a workflow rule to initiate the outbound message (for example, when the opportunity status changes to "Close-Won")
 * Create an outbound message that sends only the opportunity RecordId and a SessionId for a subsequent call back.
On the remote system side:
 * Create a proxy service that can implement the Salesforce outbound message WSDL interface.
 * The service will receive one or more notifications indicating that the opportunity is to be converted to an order.
 * The service transforms and places the message on a local message queue and on notification of receipt, replies wiht a positive acknowledgement back to the Salesforce outbound message.
 * After the order is successfully created, a separate thread calls back to Salesforce using the SessionId ad the authentication token to update the opportunity wiht an order number and status.  This call back can be done using previously documented pattern solutions, such as the Salesforce SOAP API, REST API, or an Apex Web Service.

This example demonstrates the following:
 * Implementation of a remote process invoked asynchronously
 * End-to-end guranteed delivery
 * Subsequent call back to Salesforce to update the state of the record

## Context 

Salesforce is not the system that processes or holds the orders.  After the order details are captured in Salesforce, an order needs to be created in the remote system, then the remote system manages the order htrough to its conclusion.

Pattern implementation, Salesforce makes a call to the remote system to create the order, but doesnt wait for successful completion of the call.  The remote system can optionally update Salesforce with the new order number and status in a separate transaction.

## Problem

When an event occurs in Salesforce, how do you initiate a process in a remote system and pass the required information to that process without waiting for a response form the remote system?

## Forces

