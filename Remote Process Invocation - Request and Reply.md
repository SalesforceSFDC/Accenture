# Accenture
ABC Software Company integration implementation

 * A high level overview of the proposed architecture
 * A recommendation on the appropriate Salesforce.com licenses
    * [Sales Cloud licenses](https://www.salesforce.com/products/sales-cloud/overview/)
    * [Service Cloud licenses](https://www.salesforce.com/products/community-cloud/overview/)
    * [Community Cloud licenses](https://www.salesforce.com/products/community-cloud/overview/)
 * A description of Salesforce.com functionality and customizations that will be required 
 * The integration and data migration approach
 * A description of the suggested development methodology
 
 
 ## ETL 
  * Extract data from the source system.  This involves data from a number of source systems, and both relational and non-relational structures.
  * Transforming the data to fit operational needs, which can include data quality levels.
  * Loading the data into the target system.

## Remote Process Invocation - Request and Reply
### Context
After the order details are captured in Salesforce, an order needs to be created in the remote system (OMS), and the remote system manages the order through its conclusion.

### Problem
When an event occurs in Salesforce, how do you initiate a process in a remote system, pass the required information to that process, receive a response from the remote system, and then use the response data to make updates within Salesforce?

### Forces
There are various forces to consider when applying solutions based on this pattern:
 * Does the call to the remote system require Salesforce to wait for a response before continuing processing?  (Asynchronous vs synchronous)
 * If the call to the remote system is synchronous, does the response need to be processed by Salesforce as part of the same transaction as the intial call?
 * Is the message size relatively small or large?
 * Is the integration based on the occurence of a specific event, such as a button click in the Salesforce user interface or DML-based events?

### Solution
A custon Visualforce page or button that initiates an Apex HTTP callout in a syncronous manner.
   * Salesforce provides the ability to invoke HTTP services using standard GET, POST, PUT, and DELETE methods.  A number of HTTP classes can be used to integrate with RESTful services, although its also possible to integrate to SOAP based services by manually constructing the SOAP message.  The latter is not recommended as its possible for Salesforce to consume WSDLs to generate proxy classes.
   * A user-initiated action on a Visualforce page then calls an Apex controller action that then executes this proxy Apex class to perform the remote call.  Visualforce pages require customization of the Salesfoce application.

### Sketch
![Remote Process Invocation](https://developer.salesforce.com/docs/resources/img/en-us/206.0?doc_id=dev_guides%2Fintegration_patterns%2Fimages%2Fremote_process_invocation_state.png&folder=integration_patterns_and_practices)

1. The user initiates an action on the Visualforce page (i.e., clicks a button)
2. The browser performs an HTTP POST that in turn performs an action on the corresponding Apex controller.
3. The controller calls a previously-generated Apex web service proxy class.
4. The proxy class performs the actual call to the remote Web service.
5. The response from the remote system is returned to the Apex controller, which then processes the response, updates any data in Salesforce as required, and re-renders the page.  

### Results

The application of the solutions related to this pattern allows for event-initiated remote process invocations, where the result of the transaction needs to be handled by the invoking process in Salesforce.

#### Calling Mechanism

The calling mechanism depends on the solution chosen to implement this pattern.

| Calling Mechanism | Description |
| ----------------- | :--------- |
| Visualforce and Apex controllers | Used when the remote process is to be triggered, as part of an end-to-end process involving the user interface, and the resulting state must be displayed to the end-user and/or updated in a Salesforce record.  For example, the submission of a credit card payment to an external payment gateway, where the payment results are immediately returned and displayed to the user. Integration that is triggered from user interface events usually require the creation of custom Visualforce pages.|
| Apex triggers | Used primarily for invocation of remote processes using Apex callouts from DML-initiated events.  |
| Apex batch classes | Used for invocation of remote processes in batch.  |

#### Error handling and recovery
An error handling and recovery strategy must be considered as part of the overall solution.
 * *Error handling* - When an error occurs (exceptions or error codes are returned to the caller), error handling is managed by the caller.  For example, an error message displayed on the end-user's page or logged to a table requiring further action.
 * *Recovery* - Changed are not commited to Salesforce until a succesful response is received by the caller.  For example, the order status will not be updated in the database until a response that indicates success is received.  If necessary, the caller can retry the operation.

#### Idempotent Design Considerations

#### Security Considerations

### Sidebars

#### Timeliness

Timelines is of significant importance to this pattern.  In most cases:
 * The request is typically invoked from the user interface, therefore, the process should not keep the user waiting.
 * Salesforce has a configurable timeout of up to 60 seconds for calls from Apex.
 * Completion of the remote process should be executed in a timely manner to conclude within the Salesforce timeout limit and/or within user expectations.

#### Data Volumes

#### Endpoint Capability and Standards Support

#### State Manangement

When integrating systems, keys are important for on-going state tracking, for example, if a record gets created in the remote system, in order to support ongoing updates to that record.  There are two options:
 * Salesforce stores the remote system's primary or unique surrogate key for the remote record.
 * The remote system stores the Salesforce unique record ID or some other unique surrogate key.
There are specific considerations for handling integration keys, depending on which system contains the master record.

| Master System | Description |
| ------------- | ----------- |
| Salesforce | In this scenario, the remote system should store either the Salesforce RecordId or some other unique surrogate key from the record. |
| Remote System | In this scenario, the call to the remote process should return the unique key from the application and Salesforce stores that key value in a unique record field. |

#### Complex Integration Scenarios

#### Governor Limits
Due to the multi-tenant nature of the Salesforce platform, there are limits to Apex callouts.
 * Only 10 callouts can be made in a given execution context.
 * A maximum of 60 seconds invocation time for a given callout and 120 seconds of invocaiton time for all callouts in a given execution context.
 * A maximum message size of 3 MB for a given callout request and reponse.
 
#### Middleware Capabilities

## Example 

A utility company uses Salesforce and has a separate system that contains customer billing information.  They want to display the billing history for a customer account without having to store the data in Salesforce.  They have an existing web service that can return a list of bills and their details for a given account number, but cannot otherwise display this data in a browser.

