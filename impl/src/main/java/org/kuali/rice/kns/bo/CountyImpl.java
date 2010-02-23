/*
 * Copyright 2009 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.kns.bo;

import java.util.LinkedHashMap;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@IdClass(org.kuali.rice.kns.bo.CountyImplId.class)
@Entity
@Table(name="KR_COUNTY_T")
public class CountyImpl extends PersistableBusinessObjectBase implements Inactivateable, County {

	@Id
    private String postalCountryCode;
	@Id
    private String countyCode;
	@Id
    private String stateCode;
	@Id
	@Column(name="COUNTY_NM")
    private String countyName;
	@Type(type="yes_no")
	@Column(name="ACTV_IND")
    private boolean active;

	@ManyToOne(targetEntity=org.kuali.rice.kns.bo.StateImpl.class,fetch=FetchType.LAZY, cascade={CascadeType.PERSIST})
	@JoinColumns({@JoinColumn(name="POSTAL_CNTRY_CD",insertable=false,updatable=false),@JoinColumn(name="POSTAL_STATE_CD",insertable=false,updatable=false)})
    private State state;
    
    @ManyToOne(targetEntity=org.kuali.rice.kns.bo.CountryImpl.class,fetch=FetchType.LAZY, cascade={CascadeType.PERSIST})
	@JoinColumn(name="POSTAL_CNTRY_CD",insertable=false,updatable=false)
    private Country country;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countName) {
        this.countyName = countName;
    }

    public String getCountyCode() {
        return countyCode;
    }

    public void setCountyCode(String countyCode) {
        this.countyCode = countyCode;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    protected LinkedHashMap toStringMapper() {
        LinkedHashMap m = new LinkedHashMap();
        m.put("countyCode", this.countyCode);
        m.put("stateCode", this.stateCode);
        return m;
    }

    /**
     * Gets the postalCountryCode attribute.
     * 
     * @return Returns the postalCountryCode.
     */
    public String getPostalCountryCode() {
        return postalCountryCode;
    }

    /**
     * Sets the postalCountryCode attribute value.
     * 
     * @param postalCountryCode The postalCountryCode to set.
     */
    public void setPostalCountryCode(String postalCountryCode) {
        this.postalCountryCode = postalCountryCode;
    }

    /**
     * Gets the country attribute.
     * 
     * @return Returns the country.
     */
    public Country getCountry() {
        return country;
    }

    /**
     * Sets the country attribute value.
     * 
     * @param country The country to set.
     */
    public void setCountry(Country country) {
        this.country = country;
    }
}
