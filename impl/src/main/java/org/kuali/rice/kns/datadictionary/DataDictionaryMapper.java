package org.kuali.rice.kns.datadictionary;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Maps one Document type to other document Type.
 * 
 * This interface can be used to implement KNS to workflow document type
 * mapping relationships other than one-to-one.
 * 
 * @author mpk35
 *
 */
public interface DataDictionaryMapper {
	/**
	 * This method gets the business object entry for a concrete class
	 * 
	 * @param className
	 * @return
	 */
	public BusinessObjectEntry getBusinessObjectEntryForConcreteClass(DataDictionaryIndex index, String className);
	
	/**
	 * @return List of businessObject classnames
	 */
	public List<String> getBusinessObjectClassNames(DataDictionaryIndex index);

	/**
	 * @param className
	 * @return BusinessObjectEntry for the named class, or null if none exists
	 */
	public BusinessObjectEntry getBusinessObjectEntry(DataDictionaryIndex index, String className );

	/**
	 * @return Map of (classname, BusinessObjectEntry) pairs
	 */
	public Map<String, BusinessObjectEntry> getBusinessObjectEntries(DataDictionaryIndex index);
	
	/**
	 * @param className
	 * @return DataDictionaryEntryBase for the named class, or null if none
	 *         exists
	 */
	public DataDictionaryEntry getDictionaryObjectEntry(DataDictionaryIndex index, String className);
	
	/**
	 * Returns the KNS document entry for the given lookup key.  The documentTypeDDKey is interpreted
	 * successively in the following ways until a mapping is found (or none if found):
	 * <ol>
	 * <li>KEW/workflow document type</li>
	 * <li>business object class name</li>
	 * <li>maintainable class name</li>
	 * </ol>
	 * This mapping is compiled when DataDictionary files are parsed on startup (or demand).  Currently this
	 * means the mapping is static, and one-to-one (one KNS document maps directly to one and only
	 * one key).
	 * 
	 * @param documentTypeDDKey the KEW/workflow document type name
	 * @return the KNS DocumentEntry if it exists
	 */
	public DocumentEntry getDocumentEntry(DataDictionaryIndex index, String documentTypeDDKey);

	/**
	 * Note: only MaintenanceDocuments are indexed by businessObject Class
	 * 
	 * This is a special case that is referenced in one location. Do we need
	 * another map for this stuff??
	 * 
	 * @param businessObjectClass
	 * @return DocumentEntry associated with the given Class, or null if there
	 *         is none
	 */
	public MaintenanceDocumentEntry getMaintenanceDocumentEntryForBusinessObjectClass(DataDictionaryIndex index, Class businessObjectClass);
		
	public Map<String, DocumentEntry> getDocumentEntries(DataDictionaryIndex index);

	public Set<InactivationBlockingMetadata> getAllInactivationBlockingMetadatas(DataDictionaryIndex index, Class blockedClass);
	
	/**
	 * Returns mapped document type based on the given document type.
	 * 
	 * @param documentType
	 * @return new document type or null if given documentType was not found.
	 */
	public String getDocumentTypeName(DataDictionaryIndex index, String documentTypeName);
	
	/**
	 * Returns mapped document type class based on the given document type.
	 * 
	 * @param documentType
	 * @return the class of the mapped document type or null if given documentType was not found.
	 */
	//public Class getDocumentTypeClass(String documentTypeName);
}
