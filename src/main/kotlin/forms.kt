package forms

import javax.xml.bind.JAXBContext
import javax.xml.bind.util.JAXBSource
import javax.xml.validation.Validator

import org.orcid.jaxb.model.common_v2.Contributor
import org.orcid.jaxb.model.common_v2.Subtitle
import org.orcid.jaxb.model.common_v2.Title
import org.orcid.jaxb.model.common_v2.TranslatedTitle
import org.orcid.jaxb.model.common_v2.Url

import org.orcid.jaxb.model.record_v2.Citation
import org.orcid.jaxb.model.record_v2.CitationType
import org.orcid.jaxb.model.record_v2.ExternalID
import org.orcid.jaxb.model.record_v2.ExternalIDs
import org.orcid.jaxb.model.record_v2.Relationship
import org.orcid.jaxb.model.record_v2.SequenceType
import org.orcid.jaxb.model.record_v2.WorkContributors
import org.orcid.jaxb.model.record_v2.WorkTitle
import org.orcid.jaxb.model.record_v2.WorkType

class Error(msg: String) : Throwable(msg)

data class ParamsWrapper(val inner: Map<String, List<String>>) : Map<String, List<String>> by inner {
    fun forPrefix(prefix: String): ParamsWrapper {
        val out = this.filterKeys { it.startsWith(prefix) }.mapKeys { (k, _) -> k.removePrefix(prefix) }
        return ParamsWrapper(out)
    }
    fun getFirst(key: String): String? = this.get(key)?.first()
}

fun <T: Any> T.validate(validator: Validator) {
    val context = JAXBContext.newInstance(this::class.java)
    val source = JAXBSource(context, this)
    try {
        validator.validate(source)
    } catch (e: Throwable) {
        val msg = when (e) {
            is org.xml.sax.SAXParseException -> e
                .getException()
                .let { it as? javax.xml.bind.MarshalException }
                ?.getLinkedException()
                ?.getLocalizedMessage() ?: "Unknown error"
            else -> e.getLocalizedMessage()
        }
        throw Error(msg)
    }
}

fun buildCitation(citationType: CitationType?, citation: String?): Citation? {
    if (citation.isNullOrBlank()) { return null }
    return Citation().apply {
        this.setWorkCitationType(citationType)
        // this.citationType = citationType
        this.citation = citation
    }
}

fun buildCitationType(value: String?): CitationType? {
    if (value.isNullOrBlank()) { return null }
    return CitationType.fromValue(value)
}

fun buildExternalID(type: String?, value: String?, url: Url?, relationship: Relationship?): ExternalID? {
    if (type.isNullOrBlank() && value.isNullOrBlank()) { return null }
    return ExternalID().apply {
        this.type = type?.takeIf { it.isNotBlank() }
        this.value = value?.takeIf { it.isNotBlank() }
        this.url = url
        this.relationship = relationship
    }
}

fun buildExternalIDs(externalIdentifiers: List<ExternalID>): ExternalIDs {
    return ExternalIDs().apply {
        this.getExternalIdentifier().addAll(externalIdentifiers.filterNotNull())
    }
}

fun buildRelationship(value: String?): Relationship? {
    if (value.isNullOrBlank()) { return null }
    return Relationship.fromValue(value)
}

fun buildSequenceType(value: String?): SequenceType? {
    if (value.isNullOrBlank()) { return null }
    return SequenceType.fromValue(value)
}

fun buildWorkContributors(contributor: List<Contributor>): WorkContributors {
    return WorkContributors(contributor.filterNotNull())
}

fun buildWorkTitle(title: Title?, subtitle: Subtitle?, translatedTitle: TranslatedTitle?): WorkTitle? {
    if (title == null) { return null }
    return WorkTitle().apply {
        this.title = title
        this.subtitle = subtitle
        this.translatedTitle = translatedTitle
    }
}

fun buildWorkType(value: String?): WorkType? {
    if (value.isNullOrBlank()) { return null }
    return WorkType.fromValue(value)
}
