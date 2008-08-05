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
package edu.iu.uis.eden.docsearch;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.kew.WorkflowPersistable;

import edu.iu.uis.eden.routeheader.DocumentRouteHeaderValue;
import edu.iu.uis.eden.util.Utilities;

/**
 *
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 */
@Entity
@Table(name="EN_DOC_HDR_EXT_FLT_T")
public class SearchableAttributeFloatValue implements WorkflowPersistable, SearchableAttributeValue {

    private static final long serialVersionUID = -6682101853805320760L;

    private static final String ATTRIBUTE_DATABASE_TABLE_NAME = "EN_DOC_HDR_EXT_FLT_T";
    private static final boolean DEFAULT_WILDCARD_ALLOWANCE_POLICY = false;
    private static final boolean ALLOWS_RANGE_SEARCH = true;
    private static final boolean ALLOWS_CASE_INSENSITIVE_SEARCH = false;
    private static final String DEFAULT_VALIDATION_REGEX_EXPRESSION = "[-+]?[0-9]*\\.?[0-9]+";
    private static final String ATTRIBUTE_XML_REPRESENTATION = SearchableAttribute.DATA_TYPE_FLOAT;
    private static final String DEFAULT_FORMAT_PATTERN = "";

    @Id
	@Column(name="DOC_HDR_EXT_ID")
	private Long searchableAttributeValueId;
    @Column(name="DOC_HDR_EXT_VAL_KEY")
	private String searchableAttributeKey;
    @Column(name="DOC_HDR_EXT_VAL")
	private BigDecimal searchableAttributeValue;
    @Transient
    protected String ojbConcreteClass; // attribute needed for OJB polymorphism - do not alter!

    @Column(name="DOC_HDR_ID")
	private Long routeHeaderId;
    @ManyToOne(fetch=FetchType.EAGER, cascade={CascadeType.PERSIST})
	@JoinColumn(name="DOC_HDR_ID", insertable=false, updatable=false)
	private DocumentRouteHeaderValue routeHeader;

    /**
     * Default constructor.
     */
    public SearchableAttributeFloatValue() {
        super();
        this.ojbConcreteClass = this.getClass().getName();
    }

    /* (non-Javadoc)
     * @see edu.iu.uis.eden.docsearch.SearchableAttributeValue#setupAttributeValue(java.lang.String)
     */
    public void setupAttributeValue(String value) {
        this.setSearchableAttributeValue(convertStringToBigDecimal(value));
    }

    private BigDecimal convertStringToBigDecimal(String value) {
        if (Utilities.isEmpty(value)) {
            return null;
        } else {
            return new BigDecimal(value);
        }
    }

	/* (non-Javadoc)
	 * @see edu.iu.uis.eden.docsearch.SearchableAttributeValue#setupAttributeValue(java.sql.ResultSet, java.lang.String)
	 */
	public void setupAttributeValue(ResultSet resultSet, String columnName) throws SQLException {
		this.setSearchableAttributeValue(resultSet.getBigDecimal(columnName));
	}

    /* (non-Javadoc)
     * @see edu.iu.uis.eden.docsearch.SearchableAttributeValue#getSearchableAttributeDisplayValue()
     */
    public String getSearchableAttributeDisplayValue() {
        return getSearchableAttributeDisplayValue(new HashMap<String,String>());
    }

    /* (non-Javadoc)
     * @see edu.iu.uis.eden.docsearch.SearchableAttributeValue#getSearchableAttributeDisplayValue(java.util.Map)
     */
    public String getSearchableAttributeDisplayValue(Map<String,String> displayParameters) {
	    NumberFormat format = DecimalFormat.getInstance();
	    ((DecimalFormat)format).toPattern();
	    ((DecimalFormat)format).applyPattern(getFormatPatternToUse(displayParameters.get(DISPLAY_FORMAT_PATTERN_MAP_KEY)));
	    return format.format(getSearchableAttributeValue().doubleValue());
	}

    private String getFormatPatternToUse(String parameterFormatPattern) {
        if (StringUtils.isNotBlank(parameterFormatPattern)) {
            return parameterFormatPattern;
        }
        return DEFAULT_FORMAT_PATTERN;
    }

    /* (non-Javadoc)
	 * @see edu.iu.uis.eden.docsearch.SearchableAttributeValue#getAttributeDataType()
	 */
	public String getAttributeDataType() {
		return ATTRIBUTE_XML_REPRESENTATION;
	}

	/* (non-Javadoc)
	 * @see edu.iu.uis.eden.docsearch.SearchableAttributeValue#getAttributeTableName()
	 */
	public String getAttributeTableName() {
		return ATTRIBUTE_DATABASE_TABLE_NAME;
	}

    /* (non-Javadoc)
	 * @see edu.iu.uis.eden.docsearch.SearchableAttributeValue#allowsWildcardsByDefault()
	 */
	public boolean allowsWildcards() {
		return DEFAULT_WILDCARD_ALLOWANCE_POLICY;
	}

    /* (non-Javadoc)
	 * @see edu.iu.uis.eden.docsearch.SearchableAttributeValue#allowsCaseInsensitivity()
	 */
	public boolean allowsCaseInsensitivity() {
		return ALLOWS_CASE_INSENSITIVE_SEARCH;
	}

    /* (non-Javadoc)
	 * @see edu.iu.uis.eden.docsearch.SearchableAttributeValue#allowsRangeSearches()
	 */
	public boolean allowsRangeSearches() {
		return ALLOWS_RANGE_SEARCH;
	}

	/* (non-Javadoc)
	 * @see edu.iu.uis.eden.docsearch.SearchableAttributeValue#isPassesDefaultValidation()
	 */
    public boolean isPassesDefaultValidation(String valueEntered) {
        Pattern pattern = Pattern.compile(DEFAULT_VALIDATION_REGEX_EXPRESSION);
        Matcher matcher = pattern.matcher(valueEntered);
        return (matcher.matches());
    }

    /* (non-Javadoc)
     * @see edu.iu.uis.eden.docsearch.SearchableAttributeValue#isRangeValid(java.lang.String, java.lang.String)
     */
    public Boolean isRangeValid(String lowerValue, String upperValue) {
        if (allowsRangeSearches()) {
            BigDecimal lower = convertStringToBigDecimal(lowerValue);
            BigDecimal upper = convertStringToBigDecimal(upperValue);
            if ( (lower != null) && (upper != null) ) {
                return (lower.compareTo(upper) <= 0);
            }
            return true;
        }
        return null;
    }

	public String getOjbConcreteClass() {
        return ojbConcreteClass;
    }

    public void setOjbConcreteClass(String ojbConcreteClass) {
        this.ojbConcreteClass = ojbConcreteClass;
    }

    public DocumentRouteHeaderValue getRouteHeader() {
        return routeHeader;
    }

    public void setRouteHeader(DocumentRouteHeaderValue routeHeader) {
        this.routeHeader = routeHeader;
    }

    public Long getRouteHeaderId() {
        return routeHeaderId;
    }

    public void setRouteHeaderId(Long routeHeaderId) {
        this.routeHeaderId = routeHeaderId;
    }

    public String getSearchableAttributeKey() {
        return searchableAttributeKey;
    }

    public void setSearchableAttributeKey(String searchableAttributeKey) {
        this.searchableAttributeKey = searchableAttributeKey;
    }

    public BigDecimal getSearchableAttributeValue() {
        return searchableAttributeValue;
    }

    public void setSearchableAttributeValue(BigDecimal searchableAttributeValue) {
        this.searchableAttributeValue = searchableAttributeValue;
    }

    /**
     * @deprecated USE method setSearchableAttributeValue(BigDecimal) instead
     */
    public void setSearchableAttributeValue(Float floatValueToTranslate) {
        this.searchableAttributeValue = null;
        if (floatValueToTranslate != null) {
            this.searchableAttributeValue = new BigDecimal(floatValueToTranslate.toString());
        }
    }

    public Long getSearchableAttributeValueId() {
        return searchableAttributeValueId;
    }

    public void setSearchableAttributeValueId(Long searchableAttributeValueId) {
        this.searchableAttributeValueId = searchableAttributeValueId;
    }

    /* (non-Javadoc)
     * @see edu.iu.uis.eden.WorkflowPersistable#copy(boolean)
     */
    @Deprecated
    public Object copy(boolean preserveKeys) {
        return null;
    }
}

