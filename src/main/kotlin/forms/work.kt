package forms.work

import javax.ws.rs.core.MultivaluedMap

import javax.xml.validation.SchemaFactory
import javax.xml.XMLConstants

import org.orcid.jaxb.model.record_v2.Work

import org.orcid.jaxb.model.record_v2.Citation
import org.orcid.jaxb.model.record_v2.ExternalIDs
import org.orcid.jaxb.model.record_v2.WorkContributors
import org.orcid.jaxb.model.record_v2.WorkTitle
import org.orcid.jaxb.model.record_v2.WorkType

import org.orcid.jaxb.model.common_v2.Country
import org.orcid.jaxb.model.common_v2.PublicationDate
import org.orcid.jaxb.model.common_v2.Title
import org.orcid.jaxb.model.common_v2.Url

import forms.*
import forms.common.*

val validator = Work::class.java.getResource("/record_2.0/work-2.0.xsd")
    .let { SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(it) }
    .let { it.newValidator() }

fun Work.validate() = this.validate(validator)

fun buildWork(
    title: WorkTitle?,
    journalTitle: Title?,
    shortDescription: String?,
    citation: Citation?,
    type: WorkType?,
    publicationDate: PublicationDate?,
    externalIdentifiers: ExternalIDs?,
    url: Url?,
    contributors: WorkContributors?,
    languageCode: String?,
    country: Country?): Work {

    return Work().apply {
        this.setWorkTitle(title)
        this.setJournalTitle(journalTitle)
        this.setShortDescription(shortDescription?.takeIf { it.isNotBlank() })
        this.setWorkCitation(citation)
        this.setWorkType(type)
        this.setPublicationDate(publicationDate)
        this.setWorkExternalIdentifiers(externalIdentifiers)
        this.setUrl(url)
        this.setWorkContributors(contributors)
        this.setLanguageCode(languageCode?.takeIf { it.isNotBlank() })
        this.setCountry(country)
        this.validate()
    }

}

fun buildWork(map: ParamsWrapper): Work {
    val url = buildUrl(value = map.getFirst("url"))
    val externalID = buildExternalID(
        type = "uri",
        value = url?.getValue(),
        url = url,
        relationship = buildRelationship(value = "self"))
    return buildWork(
        type = buildWorkType(value = map.getFirst("type")),
        title = buildWorkTitle(
            title = buildTitle(content = map.getFirst("title")),
            subtitle = null,
            translatedTitle = null),
        url = null,
        journalTitle = buildTitle(content = map.getFirst("journal-title")),
        shortDescription = null,
        citation = null,
        publicationDate = null,
        externalIdentifiers = buildExternalIDs(listOf(externalID).filterNotNull()),
        contributors = null,
        languageCode = null,
        country = null)
}

fun buildWork(map: MultivaluedMap<String, String>): Work = buildWork(ParamsWrapper(map))