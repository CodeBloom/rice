/*
 * Copyright 2005-2006 The Kuali Foundation.
 * 
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.iu.uis.eden.clientapp;

import java.rmi.RemoteException;

import org.kuali.rice.kew.DocumentRouteStatusChange;
import org.kuali.rice.kew.dto.ActionTakenEventDTO;
import org.kuali.rice.kew.dto.AfterProcessEventDTO;
import org.kuali.rice.kew.dto.BeforeProcessEventDTO;
import org.kuali.rice.kew.dto.DeleteEventDTO;
import org.kuali.rice.kew.dto.DocumentRouteLevelChangeDTO;
import org.kuali.rice.kew.dto.DocumentRouteStatusChangeDTO;
import org.kuali.rice.kew.exception.ResourceUnavailableException;



/**
 * A PostProcessor listens for events from the Workflow routing engine for it's DocumentType.
 * It gives clients hooks into the routing process to perform operations at the various stages.
 * Implementations of this Interface are potentially remotable, so this Interface uses 
 * value objects. 
 * 
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 */
public interface PostProcessorRemote {
  
  /**
   * The document has changed route status. The docEvent contains the information about the change.
   * This method should do what ever is appropriate for various route status changes to a document.
   * The method should return true if the change is correct and all application actions as a result
   * of the change are successful. It should return false if the application considers this an
   * incorrect change.
   *
   * The method can throw a ResourceUnavailableException in order to get EDEN to requeue the
   * document and try again later.
   * @param statusChangeEvent
   * @param msg any error message to be propagated back to users should be set here
   * @return true if the status change is correct and application actions are successful
   * later if this exception is thrown
   * @see DocumentRouteStatusChange
   */
  public boolean doRouteStatusChange(DocumentRouteStatusChangeDTO statusChangeEvent) throws RemoteException;

  /**
   * The document has changed route level. The docEvent contains the information about the change.
   * This method should do what ever is appropriate for various route level changes to a document.
   * The method should return true if the change is correct and all application actions as a result
   * of the change are successful. It should return false if the application considers this an
   * incorrect change.
   *
   * The method can throw a ResourceUnavailableException in order to get EDEN to requeue the
   * document and try again later.
   * @param levelChangeEvent
   * @param msg any error message to be propagated back to users should be set here
   * @return true if the status change is correct and application actions are successful
   * @throws java.lang.Exception A general Exception will cause EDEN to put the document into Exception routing
   * @throws ResourceUnavailableException EDEN will requeue the document and try the change again
   * later if this exception is thrown
   * @see DocumentRouteLevelChangeDTO
   */
  public boolean doRouteLevelChange(DocumentRouteLevelChangeDTO levelChangeEvent) throws RemoteException;
  
  /**
   * KEW is signaling that the document should be deleted. The application can reject this by
   * returning false. If the EdenException is thrown the docuemnt will go to exception routing. If
   * a ResourceUnavailableException is thrown, the doc will be requeued and will try again later to
   * delete the document.
   * @param event
   * @param message
   * @return
   * @throws java.lang.Exception A general Exception will cause EDEN to put the document into Exception routing
   * @throws ResourceUnavailableException EDEN will requeue the document and try the change again
   */
  public boolean doDeleteRouteHeader(DeleteEventDTO event) throws RemoteException;
  
  /**
   * KEW is signaling that the document has had an action taken on it by a user
   * 
   * @param event
   * @return
   * @throws RemoteException
   */
  public boolean doActionTaken(ActionTakenEventDTO event) throws RemoteException;
  
  /**
   * The document is about to be processed by the Workflow engine.
   * 
   * @param event - holder for data from the engine's process
   * @return true if the method is successful, false otherwise
   * @throws java.lang.Exception A general Exception will cause EDEN to put the document into Exception routing
   */
  public boolean beforeProcess(BeforeProcessEventDTO event) throws Exception;

  /**
   * The document has just completed processing by the Workflow engine.
   * 
   * @param event - holder for data from the engine's process including whether the engine was successful or not
   * @return true if the method is successful, false otherwise
   * @throws java.lang.Exception A general Exception will cause EDEN to put the document into Exception routing
   */
  public boolean afterProcess(AfterProcessEventDTO event) throws Exception;
}