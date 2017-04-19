## Example

A telecomunications company wishes to use Salesforce as a front end for creating new accounts using the Lead to Opportunity process.  The creation of an order is initiated in Salesforce once the opportunity is closed and won, but the back end ERP system will be the data master.  The order must be subsequently saved to the Salesforce opportunity record and the opportunity status changed to indicate that the order was created.

The following constraints apply:
 * The ERP system is capable of participating in a contract-first integration, where its service must implement a Salesforce WSDL interface.  
 * There should be custom development in Salesforce.
 * The user doesn't need to be immediately notified of the order number after the opportunity converts to an order.

This example is best implemented using Salesforce outbound messaging, but does not require the implementation of a proxy service by the remote system:

On the Salesforce side:
 * Create a workflow rule to initiate the outbound message (for example, when the opportunity status changes to "Closed-Won")
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

There are various forces to consider when applying solutions based on this pattern:
 * Does the call to the remote system require Salesforce to wait for a response before continuing processing?  In other words, is the call to the remote system synchronous request-reply or asynchronous?
 * If the call to the remote system is synchronous, does the response need to be processed by Salesforce as part of the same transaction as the call?
 * Is the message size relatively small?
 * Is the integration based on the occurrence of a specific event such as a button click in the Salesforce user interface or DML-based events?
 * Is guaranteed message delivery from Salesforce to the remote system a requirement?
 * Is the remote system able to particiapte in a contract-first integration where Salesforce specifies the contract?  In some solution variants (for example, outbound messaging), Salesforce specifies a contract that must be implemented by the remote system endpoint.
 * Are declarative configuration methods preferred over custom Apex development?  In this case, solutions such as outbound messaging are preferred over Apex callouts.  
 
## Solution

| Solution | Comments |
| :--------: | -------- |
| Workflow-driven outbound messaging | The remote process is invoked from an insert or update event.  Salesforce provides a workflow-driven outbound messaging capability that allows the sending of SOAP messages to remote systems triggered by an insert or update operation in Salesforce.  These messages are sent asynchronously and are independent of the Salesforce user interface. <br> <br/> The outbound message is sent to a specific remote endpoint.  The remote service must be able to participate in a contract-first integration where Salesforce provides the contract. <br> </br> On receipt of the message, the remote service must respond with a positive acknowledgment, otherwise Salesforce attempts to retry sending the message, thus providing a form of guaranteed delivery.  When using middleware, this becomes a `first-mile` guarantee of delivery. | 
| Outbound messaging and callbacks | Callbacks provide a way to mitigate the impacts of out-of-sequence messaging.  In addition, they handle these scenarios: <br/> 1. __Idempotency__ - Outbound messaging performs retries if an acknowledgment isnt received in a timely fashion, therefore, multiple messages might be sent to the target system.  Using a callback ensures that the data retrieved is at a specific point in time rather than when the message was sent.  <br/> 2. __Retrieving additional data__ - A single outbound message is capable of sending data only for a single object.  A callback can be used to retrieve additional data from other related objects, such as related lists associated with the parent object. <br> <br/> The outbound message provides a unique SessionId that can be used as the authentication token to authenticate and authorize callback using either the SOAP API or the REST API.  This means that the system performing the callback doesnt need to separetely authenticate to Salesforce.  The standard methods of either API can then be used to perform the desired business functions.  <br> </br> A typical use of this variant is the scenario where Salesforce sends an outbound message to a remote system to create a record.  The callback is then used to update the original Salesforce record with the unique key of the record created in the remote system.|

## Sketch

The following diagram illustrates a call from Salesforce to a remote system, where the call is triggered by the create or update operations on a record in Salesforce.

![Remote Process Invocation - Fire and Forget](https://developer.salesforce.com/docs/resources/img/en-us/206.0?doc_id=dev_guides%2Fintegration_patterns%2Fimages%2Fremote_process_invocation_fire_forget.png&folder=integration_patterns_and_practices)
