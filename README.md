# Accenture
ABC Software Company integration implementation

 * A high level overview of the proposed architecture
 * A recommendation on the appropriate Salesforce.com licenses
    * Sales Cloud licenses
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
   * Salesforce provides the ability to invoke HTTP services using  

