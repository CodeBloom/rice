/*
 * Copyright 2007 The Kuali Foundation
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
package org.kuali.rice.kim.document.rule;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.kim.bo.entity.KimPrincipal;
import org.kuali.rice.kim.bo.types.dto.AttributeSet;
import org.kuali.rice.kim.bo.types.impl.KimAttributeImpl;
import org.kuali.rice.kim.bo.ui.KimDocumentRoleMember;
import org.kuali.rice.kim.bo.ui.KimDocumentRoleQualifier;
import org.kuali.rice.kim.bo.ui.PersonDocumentAffiliation;
import org.kuali.rice.kim.bo.ui.PersonDocumentBoDefaultBase;
import org.kuali.rice.kim.bo.ui.PersonDocumentEmploymentInfo;
import org.kuali.rice.kim.bo.ui.PersonDocumentGroup;
import org.kuali.rice.kim.bo.ui.PersonDocumentRole;
import org.kuali.rice.kim.document.IdentityManagementPersonDocument;
import org.kuali.rice.kim.document.authorization.IdentityManagementKimDocumentAuthorizer;
import org.kuali.rice.kim.rule.event.ui.AddGroupEvent;
import org.kuali.rice.kim.rule.event.ui.AddRoleEvent;
import org.kuali.rice.kim.rule.ui.AddGroupRule;
import org.kuali.rice.kim.rule.ui.AddRoleRule;
import org.kuali.rice.kim.rules.ui.PersonDocumentGroupRule;
import org.kuali.rice.kim.rules.ui.PersonDocumentRoleRule;
import org.kuali.rice.kim.service.IdentityService;
import org.kuali.rice.kim.service.KIMServiceLocator;
import org.kuali.rice.kim.service.support.KimTypeService;
import org.kuali.rice.kim.util.KIMPropertyConstants;
import org.kuali.rice.kim.util.KimCommonUtils;
import org.kuali.rice.kns.datadictionary.AttributeDefinition;
import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.rules.TransactionalDocumentRuleBase;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.KNSServiceLocator;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KNSConstants;
import org.kuali.rice.kns.util.ObjectUtils;
import org.kuali.rice.kns.util.RiceKeyConstants;

/**
 * This is a description of what this class does - shyu don't forget to fill this in. 
 * 
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 *
 */
public class IdentityManagementPersonDocumentRule extends TransactionalDocumentRuleBase implements AddGroupRule,AddRoleRule {

	private static final Logger LOG = Logger.getLogger( IdentityManagementPersonDocumentRule.class );
	
	private AddGroupRule addGroupRule;
	private AddRoleRule  addRoleRule;
	private IdentityManagementKimDocumentAuthorizer authorizer;
	private BusinessObjectService businessObjectService;
	private IdentityService identityService;
	private Class<? extends AddGroupRule> addGroupRuleClass = PersonDocumentGroupRule.class;
	private Class<? extends AddRoleRule> addRoleRuleClass = PersonDocumentRoleRule.class;
	
	
    @Override
    protected boolean processCustomSaveDocumentBusinessRules(Document document) {
        if (!(document instanceof IdentityManagementPersonDocument)) {
            return false;
        }

        IdentityManagementPersonDocument personDoc = (IdentityManagementPersonDocument)document;
        boolean valid = true;

        GlobalVariables.getErrorMap().addToErrorPath(KNSConstants.DOCUMENT_PROPERTY_NAME);

        //KNSServiceLocator.getDictionaryValidationService().validateDocument(document);
        getDictionaryValidationService().validateDocumentAndUpdatableReferencesRecursively(document, getMaxDictionaryValidationDepth(), true, false);
        valid &= checkMultipleDefault (personDoc.getAffiliations(), "affiliations");
        valid &= checkMultipleDefault (personDoc.getNames(), "names");
        valid &= checkMultipleDefault (personDoc.getAddrs(), "addrs");
        valid &= checkMultipleDefault (personDoc.getPhones(), "phones");
        valid &= checkMultipleDefault (personDoc.getEmails(), "emails");
        valid &= checkPrimaryEmploymentInfo (personDoc.getAffiliations());
        valid &= checkAffiliationTypeChange (personDoc.getAffiliations());
        valid &= checkUniqueAffiliationTypePerCampus(personDoc.getAffiliations());
        // kimtypeservice.validateAttributes is not working yet.
        valid &= validateRoleQualifier (personDoc.getRoles());
        if (StringUtils.isNotBlank(personDoc.getPrincipalName())) { 
        	valid &= doesPrincipalNameExist (personDoc.getPrincipalName(), personDoc.getPrincipalId());
        }
        
        valid &= validActiveDatesForRole (personDoc.getRoles());
        valid &= validActiveDatesForGroup (personDoc.getGroups());


        // all failed at this point.
//        valid &= checkUnassignableRoles(personDoc);
//        valid &= checkUnpopulatableGroups(personDoc);
        
        GlobalVariables.getErrorMap().removeFromErrorPath(KNSConstants.DOCUMENT_PROPERTY_NAME);

        return valid;
    }
    
    
    
    @Override
	protected boolean processCustomRouteDocumentBusinessRules(Document document) {
		super.processCustomRouteDocumentBusinessRules(document);
        IdentityManagementPersonDocument personDoc = (IdentityManagementPersonDocument)document;
        boolean valid = true;
        GlobalVariables.getErrorMap().addToErrorPath(KNSConstants.DOCUMENT_PROPERTY_NAME);
        valid &= validateRoleQualifierRequired( personDoc.getRoles() );
        valid &= validateAffiliationAndName( personDoc );
        valid &= checkAffiliationEithOneEMpInfo (personDoc.getAffiliations());
        GlobalVariables.getErrorMap().removeFromErrorPath(KNSConstants.DOCUMENT_PROPERTY_NAME);

        return valid;
	}


	private boolean checkMultipleDefault (List <? extends PersonDocumentBoDefaultBase> boList, String listName) {
    	boolean valid = true;
    	boolean isDefaultSet = false;
    	int i = 0;
    	for (PersonDocumentBoDefaultBase item : boList) {
     		if (item.isDflt()) {
     			if (isDefaultSet) {
     				GlobalVariables.getErrorMap().putError(listName+"[" + i + "].dflt",RiceKeyConstants.ERROR_MULTIPLE_DEFAULT_SELETION);
     				valid = false;
     			} else {
     				isDefaultSet = true;
     			}
     		}
     		i++;
    	}
    	if (!boList.isEmpty() && !isDefaultSet) {
				GlobalVariables.getErrorMap().putError(listName+"[0].dflt",RiceKeyConstants.ERROR_NO_DEFAULT_SELETION);    		
    	}
    	return valid;
    }
    
    private boolean checkPrimaryEmploymentInfo (List <PersonDocumentAffiliation> affiliations) {
    	boolean valid = true;
    	int i = 0;
    	int firstAfflnCounter = -1;
    	boolean isPrimarySet = false;
    	for (PersonDocumentAffiliation affiliation : affiliations) {
    		int j = 0;
    		for (PersonDocumentEmploymentInfo empInfo : affiliation.getEmpInfos()) {
     			if(firstAfflnCounter==-1)
     				firstAfflnCounter = i;
    			if (empInfo.isPrimary()) {
	     			if (isPrimarySet) {
	     				// primary per principal or primary per affiliation ?
	     				GlobalVariables.getErrorMap().putError("affiliations[" + i + "].empInfos["+ j +"].primary",RiceKeyConstants.ERROR_MULTIPLE_PRIMARY_EMPLOYMENT);
	     				valid = false;
	     			} else {
	     				isPrimarySet = true;
	     			}
	     			j++;
	     		}
    		}
     		i++;
    	}
    	if(!isPrimarySet && firstAfflnCounter!=-1){
    		GlobalVariables.getErrorMap().putError("affiliations[" + firstAfflnCounter + "].empInfos[0].primary",RiceKeyConstants.ERROR_NO_PRIMARY_EMPLOYMENT);
    		valid = false;
    	}
    	return valid;
    }
    
    private boolean checkAffiliationTypeChange (List <PersonDocumentAffiliation> affiliations) {
    	boolean valid = true;
    	int i = 0;
    	for (PersonDocumentAffiliation affiliation : affiliations) {
    		if (affiliation.getAffiliationType() != null && !affiliation.getAffiliationTypeCode().equals(affiliation.getAffiliationType().getAffiliationTypeCode())) {
    			PersonDocumentAffiliation copiedAffiliation = (PersonDocumentAffiliation)ObjectUtils.deepCopy(affiliation);
    			copiedAffiliation.refreshReferenceObject("affiliationType");
    			if (!copiedAffiliation.getAffiliationType().isEmploymentAffiliationType() && affiliation.getAffiliationType().isEmploymentAffiliationType() && !copiedAffiliation.getEmpInfos().isEmpty()) {
		     		GlobalVariables.getErrorMap().putError("affiliations[" + i + "].affiliationTypeCode",RiceKeyConstants.ERROR_NOT_EMPLOYMENT_AFFILIATION_TYPE,new String[] {affiliation.getAffiliationType().getAffiliationTypeName(), copiedAffiliation.getAffiliationType().getAffiliationTypeName()});
		     		valid = false;
	    		}
    		}
        	i++;
    	}
    	return valid;
    }
    
    private boolean checkUniqueAffiliationTypePerCampus (List <PersonDocumentAffiliation> affiliations) {
    	boolean valid = true;
    	int i = 0;
    	for (PersonDocumentAffiliation affiliation : affiliations) {
    		int j = 0;
        	for (PersonDocumentAffiliation affiliation1 : affiliations) {
	    		if (j > i && affiliation.getAffiliationTypeCode() .equals(affiliation1.getAffiliationTypeCode()) && affiliation.getCampusCode().equals(affiliation1.getCampusCode())) {
			     		GlobalVariables.getErrorMap().putError("affiliations[" + j + "].affiliationTypeCode",RiceKeyConstants.ERROR_NOT_UNIQUE_AFFILIATION_TYPE_PER_CAMPUE, affiliation.getAffiliationType().getAffiliationTypeName());
			     		valid = false;	    		
	    		}
	    		j++;
        	}
        	i++;
    	}
    	return valid;
    }
    
    private boolean checkAffiliationEithOneEMpInfo (List <PersonDocumentAffiliation> affiliations) {
    	boolean valid = true;
    	int i = 0;
    	for (PersonDocumentAffiliation affiliation : affiliations) {
	    		if (affiliation.getAffiliationType() .isEmploymentAffiliationType() && affiliation.getEmpInfos().isEmpty()) {
			     		GlobalVariables.getErrorMap().putError("affiliations[" + i + "].affiliationTypeCode",RiceKeyConstants.ERROR_ONE_ITEM_REQUIRED, "Employment Information");
			     		valid = false;	    		
	    		}
        	i++;
    	}
    	return valid;
    }
    
    /*
     * Verify at least one affiliation and one default name
     */
    private boolean validateAffiliationAndName(IdentityManagementPersonDocument personDoc) {
    	boolean valid = true;
    	if (personDoc.getAffiliations().isEmpty()) {
     		GlobalVariables.getErrorMap().putError("affiliations[0]",RiceKeyConstants.ERROR_ONE_ITEM_REQUIRED, "affiliation");
     		valid = false;
    	}	
    	if (personDoc.getNames().isEmpty()) {
     		GlobalVariables.getErrorMap().putError("names[0]",RiceKeyConstants.ERROR_ONE_ITEM_REQUIRED, "name");
     		valid = false;
    	}	
    	return valid;
    }
    
    private boolean doesPrincipalNameExist (String principalName, String principalId) {
    	KimPrincipal principal = getIdentityService().getPrincipalByPrincipalName(principalName);
    	if (principal != null && (StringUtils.isBlank(principalId) || !principal.getPrincipalId().equals(principalId))) {
        	GlobalVariables.getErrorMap().putError(KIMPropertyConstants.Person.PRINCIPAL_NAME,RiceKeyConstants.ERROR_EXIST_PRINCIPAL_NAME, principalName);
			return false;
    	}
    	return true;
    }

    private static Map<String,KimAttributeImpl> attributeDefinitionMap = new HashMap<String,KimAttributeImpl>();
    
    private KimAttributeImpl getAttributeDefinition( String id ) {
    	KimAttributeImpl attributeImpl = attributeDefinitionMap.get( id );
    	
    	if ( attributeImpl == null ) {
			Map<String,String> criteria = new HashMap<String,String>();
			criteria.put( "kimAttributeId", id );
			attributeImpl = (KimAttributeImpl)getBusinessObjectService().findByPrimaryKey( KimAttributeImpl.class, criteria );
			attributeDefinitionMap.put( id, attributeImpl );
    	}
    	return attributeImpl;
    }
    
	private AttributeSet convertQualifiersToMap( List<KimDocumentRoleQualifier> qualifiers ) {
		AttributeSet m = new AttributeSet();
		for ( KimDocumentRoleQualifier data : qualifiers ) {
			KimAttributeImpl attrib = getAttributeDefinition( data.getKimAttrDefnId() );
			if ( attrib != null ) {
				m.put( attrib.getAttributeName(), data.getAttrVal() );
			} else {
				LOG.error("Unable to get attribute name for ID:" + data.getKimAttrDefnId() );
			}
		}
		return m;
	}
    
	private AttributeSet convertQualifiersToAttrIdxMap( List<KimDocumentRoleQualifier> qualifiers ) {
		AttributeSet m = new AttributeSet();
		int i = 0;
		for ( KimDocumentRoleQualifier data : qualifiers ) {
			KimAttributeImpl attrib = getAttributeDefinition( data.getKimAttrDefnId() );
			if ( attrib != null ) {
				m.put( attrib.getAttributeName(), Integer.toString(i) );
			} else {
				LOG.error("Unable to get attribute name for ID:" + data.getKimAttrDefnId() );
			}
			i++;
		}
		return m;
	}

	private AttributeSet convertErrors(String errorPath, AttributeSet attrIdxMap, AttributeSet localErrors) {
		AttributeSet errors = new AttributeSet();
		for ( String key : localErrors.keySet() ) {
			errors.put(errorPath+".qualifiers["+attrIdxMap.get(key)+"].attrVal", localErrors.get(key));
		}
		return errors;

	}
	
    private boolean validateRoleQualifier( List<PersonDocumentRole> roles ) {

		AttributeSet validationErrors = new AttributeSet();
        GlobalVariables.getErrorMap().removeFromErrorPath(KNSConstants.DOCUMENT_PROPERTY_NAME);
        int i = 0;
    	for(PersonDocumentRole role : roles ) {
	        KimTypeService kimTypeService = KimCommonUtils.getKimTypeService( role.getKimRoleType() );
	        if ( kimTypeService != null ) {
		        int j = 0;
	        	for ( KimDocumentRoleMember rolePrincipal : role.getRolePrncpls() ) {
	        		AttributeSet localErrors = kimTypeService.validateAttributes( convertQualifiersToMap( rolePrincipal.getQualifiers() ) );
			        validationErrors.putAll( convertErrors("roles["+i+"].rolePrncpls["+j+"]",convertQualifiersToAttrIdxMap(rolePrincipal.getQualifiers()),localErrors) );
			        j++;
		        }
	        }
        	i++;
    	}
        GlobalVariables.getErrorMap().addToErrorPath(KNSConstants.DOCUMENT_PROPERTY_NAME);
    	if (validationErrors.isEmpty()) {
    		return true;
    	} else {
    		// FIXME: This does not use the correct error path yet - may need to be moved up so that the error path is known
    		// Also, the above code would overwrite messages on the same attributes (namespaceCode) but on different rows
    		for ( String key : validationErrors.keySet() ) {
        		String[] errorMsg = StringUtils.split(validationErrors.get( key ), ":");
        		
    			GlobalVariables.getErrorMap().putError( key, errorMsg[0], errorMsg.length > 1 ? StringUtils.split(errorMsg[1], ";") : new String[] {} );
    		}
    		return false;
    	}
    }

    private boolean validateRoleQualifierRequired( List<PersonDocumentRole> roles ) {

    	boolean valid = true;
		int i = 0;
		for (PersonDocumentRole role : roles) {
			if (!role.getDefinitions().isEmpty()
					&& CollectionUtils.isEmpty(role.getRolePrncpls())) {
				boolean roleQualifierRequired = false;
				for (String key : role.getDefinitions().keySet()) {
					AttributeDefinition definition = role.getDefinitions().get(
							key);
					if (definition.isRequired()) {
						roleQualifierRequired = true;
						break;
					}
				}
				if (roleQualifierRequired) {
					valid = false;
					GlobalVariables.getErrorMap().putError("roles[" + i + "]",
							RiceKeyConstants.ERROR_ROLE_QUALIFIER_REQUIRED,
							new String[] {role.getNamespaceCode(), role.getRoleName()});
				}
			}
		}
		i++;
		return valid;
	}
    

    private boolean validActiveDatesForRole (List<PersonDocumentRole> roles ) {
    	boolean valid = true;
		int i = 0;
    	for(PersonDocumentRole role : roles ) {
			int j = 0;
    		for (KimDocumentRoleMember principal : role.getRolePrncpls()) {
    			valid &= validateActiveDate("roles["+i+"].rolePrncpls["+j+"].activeToDate",principal.getActiveFromDate(), principal.getActiveToDate());
    			j++;    			
    		}
    		i++;
    	}
    	return valid;
    }
    
    private boolean validActiveDatesForGroup (List<PersonDocumentGroup> groups ) {
    	boolean valid = true;
		int i = 0;
    	for(PersonDocumentGroup group : groups ) {
     		valid &= validateActiveDate("groups["+i+"].activeToDate",group.getActiveFromDate(), group.getActiveToDate());
    		i++;
    	}
    	return valid;
    }
    
	private boolean validateActiveDate(String errorPath, Date activeFromDate, Date activeToDate) {
		// TODO : do not have detail bus rule yet, so just check this for now.
		boolean valid = true;
		if (activeFromDate != null && activeToDate !=null && activeToDate.before(activeFromDate)) {
	        GlobalVariables.getErrorMap().putError(errorPath, RiceKeyConstants.ERROR_ACTIVE_TO_DATE_BEFORE_FROM_DATE);
            valid = false;
			
		}
		return valid;
	}
	
	private boolean checkUnassignableRoles(IdentityManagementPersonDocument document) {
		boolean valid = true;
    	Map<String,Set<String>> unassignableRoles = getAuthorizer( document ).getUnassignableRoles(document, GlobalVariables.getUserSession().getPerson());
        for (String namespaceCode : unassignableRoles.keySet()) {
        	for (String roleName : unassignableRoles.get(namespaceCode)) {
        		int i = 0;
        		for (PersonDocumentRole role : document.getRoles()) {
        			if (namespaceCode.endsWith(role.getNamespaceCode()) && roleName.equals(role.getRoleName())) {
        				GlobalVariables.getErrorMap().putError("roles["+i+"].roleId", RiceKeyConstants.ERROR_ASSIGN_ROLE, new String[] {namespaceCode, roleName});
        			}
        			i++;
        		}
        	}
        	valid = false;
        }
        return valid;
	}
	
	private boolean checkUnpopulatableGroups(IdentityManagementPersonDocument document) {
		boolean valid = true;
    	Map<String,Set<String>> unpopulatableGroups = getAuthorizer( document ).getUnpopulateableGroups(document, GlobalVariables.getUserSession().getPerson());
        for (String namespaceCode : unpopulatableGroups.keySet()) {
        	for (String groupName : unpopulatableGroups.get(namespaceCode)) {
        		int i = 0;
        		for (PersonDocumentGroup group : document.getGroups()) {
        			if (namespaceCode.endsWith(group.getNamespaceCode()) && groupName.equals(group.getGroupName())) {
        				GlobalVariables.getErrorMap().putError("groups["+i+"].groupId", RiceKeyConstants.ERROR_POPULATE_GROUP, new String[] {namespaceCode, groupName});
        			}
        			i++;
        		}
        	}
        	valid = false;
        }
        return valid;
	}
	
    public boolean processAddGroup(AddGroupEvent addGroupEvent) {
        return getAddGroupRule().processAddGroup(addGroupEvent);    
    }

    public boolean processAddRole(AddRoleEvent addRoleEvent) {
        return getAddRoleRule().processAddRole(addRoleEvent);    
    }



	public IdentityService getIdentityService() {
		if ( identityService == null ) {
			identityService = KIMServiceLocator.getIdentityService();
		}
		return identityService;
	}



	public IdentityManagementKimDocumentAuthorizer getAuthorizer(IdentityManagementPersonDocument document) {
		if ( authorizer == null ) {
			authorizer = (IdentityManagementKimDocumentAuthorizer)KNSServiceLocator.getDocumentHelperService().getDocumentAuthorizer(document);
		}
		return authorizer;
	}



	/**
	 * @return the addGroupRuleClass
	 */
	public Class<? extends AddGroupRule> getAddGroupRuleClass() {
		return this.addGroupRuleClass;
	}



	/**
	 * Can be overridden by subclasses to indicate the rule class to use when adding groups.
	 * 
	 * @param addGroupRuleClass the addGroupRuleClass to set
	 */
	public void setAddGroupRuleClass(Class<? extends AddGroupRule> addGroupRuleClass) {
		this.addGroupRuleClass = addGroupRuleClass;
	}



	/**
	 * @return the addRoleRuleClass
	 */
	public Class<? extends AddRoleRule> getAddRoleRuleClass() {
		return this.addRoleRuleClass;
	}



	/**
	 * Can be overridden by subclasses to indicate the rule class to use when adding roles.
	 * 
	 * @param addRoleRuleClass the addRoleRuleClass to set
	 */
	public void setAddRoleRuleClass(Class<? extends AddRoleRule> addRoleRuleClass) {
		this.addRoleRuleClass = addRoleRuleClass;
	}



	/**
	 * @return the addGroupRule
	 */
	public AddGroupRule getAddGroupRule() {
		if ( addGroupRule == null ) {
			try {
				addGroupRule = addGroupRuleClass.newInstance();
			} catch ( Exception ex ) {
				throw new RuntimeException( "Unable to create AddGroupRule instance using class: " + addGroupRuleClass, ex );
			}
		}
		return addGroupRule;
	}



	/**
	 * @return the addRoleRule
	 */
	public AddRoleRule getAddRoleRule() {
		if ( addRoleRule == null ) {
			try {
				addRoleRule = addRoleRuleClass.newInstance();
			} catch ( Exception ex ) {
				throw new RuntimeException( "Unable to create AddRoleRule instance using class: " + addRoleRuleClass, ex );
			}
		}
		return addRoleRule;
	}



	/**
	 * @return the businessObjectService
	 */
	public BusinessObjectService getBusinessObjectService() {
		if ( businessObjectService == null ) {
			businessObjectService = KNSServiceLocator.getBusinessObjectService();
		}
		return businessObjectService;
	}

}
