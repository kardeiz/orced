package forms.employment

import javax.ws.rs.core.MultivaluedMap

import javax.xml.validation.SchemaFactory
import javax.xml.XMLConstants

import org.orcid.jaxb.model.common_v2.Organization
import org.orcid.jaxb.model.common_v2.FuzzyDate

import org.orcid.jaxb.model.record_v2.Employment

import forms.*
import forms.common.*

val validator = Employment::class.java.getResource("/record_2.0/employment-2.0.xsd")
    .let { SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(it) }
    .let { it.newValidator() }

fun Employment.validate() = this.validate(validator)

fun buildEmployment(
    departmentName: String?,
    roleTitle: String?,
    organization: Organization?,
    startDate: FuzzyDate?,
    endDate: FuzzyDate?): Employment {

    return Employment().apply {
        this.setDepartmentName(departmentName?.takeIf { it.isNotBlank() })
        this.setRoleTitle(roleTitle?.takeIf { it.isNotBlank() })
        this.setOrganization(organization)
        this.setStartDate(startDate)
        this.setEndDate(endDate)
        this.validate()
    }
}

fun buildEmployment(map: ParamsWrapper): Employment =
    buildEmployment(
        departmentName = map.getFirst("department-name"),
        roleTitle = map.getFirst("role-title"),
        organization = buildOrganization(
            name = map.getFirst("organization.name"),
            address = buildOrganizationAddress(
                city = map.getFirst("organization.address.city"),
                region = map.getFirst("organization.address.region"),
                countryCode = map.getFirst("organization.address.country"))),
        startDate = buildFuzzyDate(
            year = map.getFirst("start-date.year.value"),
            month = map.getFirst("start-date.month.value"),
            day = map.getFirst("start-date.day.value")),
        endDate = buildFuzzyDate(
            year = map.getFirst("end-date.year.value"),
            month = map.getFirst("end-date.month.value"),
            day = map.getFirst("end-date.day.value")))

fun buildEmployment(map: MultivaluedMap<String, String>): Employment = buildEmployment(ParamsWrapper(map))