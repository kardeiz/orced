package forms.common

import org.orcid.jaxb.model.common_v2.*
import org.orcid.jaxb.model.record_v2.SequenceType

fun buildContributor(
    contributorOrcid: ContributorOrcid?,
    creditName: CreditName?,
    contributorEmail: ContributorEmail?,
    contributorAttributes: ContributorAttributes?): Contributor? {

    if (contributorOrcid == null && creditName == null && contributorEmail == null && contributorAttributes == null) {
        return null
    }

    return Contributor().apply {
        this.contributorOrcid = contributorOrcid
        this.creditName = creditName
        this.contributorEmail = contributorEmail
        this.contributorAttributes = contributorAttributes
    }
}

fun buildContributorAttributes(contributorSequence: SequenceType?, contributorRole: ContributorRole?):
    ContributorAttributes? {
    if (contributorSequence == null && contributorRole == null) { return null }
    return ContributorAttributes().apply {
        this.contributorSequence = contributorSequence
        this.contributorRole = contributorRole
    }
}

fun buildContributorEmail(value: String?): ContributorEmail? {
    if (value.isNullOrBlank()) { return null }
    return ContributorEmail().apply {
        this.value = value
    }
}

fun buildContributorOrcid(path: String?): ContributorOrcid? {
    if (path.isNullOrBlank()) { return null }
    return ContributorOrcid(path)
}

fun buildContributorRole(value: String?): ContributorRole? {
    if (value.isNullOrBlank()) { return null }
    return ContributorRole.fromValue(value)
}

fun buildCountry(value: Iso3166Country?): Country? {
    if (value == null) { return null }
    return Country().apply {
        this.value = value
    }
}

fun buildCreditName(content: String?): CreditName? {
    if (content.isNullOrBlank()) { return null }
    return CreditName().apply {
        this.content = content
    }
}

fun buildFuzzyDate(year: String?, month: String?, day: String?): FuzzyDate? {
    val yearInt = year?.toIntOrNull()
    val monthInt = month?.toIntOrNull()
    val dayInt = day?.toIntOrNull()

    if ((yearInt == null) && (monthInt == null) && (dayInt == null)) { return null }

    return FuzzyDate.valueOf(yearInt, monthInt, dayInt)
}

fun buildIso3166Country(value: String?): Iso3166Country? {
    if (value.isNullOrBlank()) { return null }
    return Iso3166Country.fromValue(value)
}

fun buildMediaType(value: String?): MediaType? {
    if (value.isNullOrBlank()) { return null }
    return MediaType.fromValue(value)
}

fun buildOrganization(name: String?, address: OrganizationAddress?): Organization? {
    if (name.isNullOrBlank() && address == null) { return null }
    return Organization().apply {
        this.name = name?.takeIf { it.isNotBlank() }
        this.address = address
    }
}

fun buildOrganizationAddress(city: String?, region: String?, countryCode: String?): OrganizationAddress? {
    if (city.isNullOrBlank() && region.isNullOrBlank() && countryCode.isNullOrBlank()) { return null }
    return OrganizationAddress().apply {
        this.city = city?.takeIf { it.isNotBlank() }
        this.region = region?.takeIf { it.isNotBlank() }
        this.country = buildIso3166Country(countryCode)
    }
}

fun buildPublicationDate(date: FuzzyDate?, mediaType: MediaType?): PublicationDate? {
    if (date == null) { return null }
    return PublicationDate(date).apply {
        this.mediaType = mediaType
    }
}

fun buildSubtitle(content: String?): Subtitle? {
    if (content.isNullOrBlank()) { return null }
    return Subtitle().apply {
        this.content = content
    }
}

fun buildTitle(content: String?): Title? {
    if (content.isNullOrBlank()) { return null }
    return Title().apply {
        this.content = content
    }
}

fun buildTranslatedTitle(content: String?, languageCode: String?): TranslatedTitle? {
    if (content.isNullOrBlank()) { return null }
    return TranslatedTitle().apply {
        this.content = content
        this.languageCode = languageCode?.takeIf { it.isNotBlank() }
    }
}

fun buildUrl(value: String?): Url? {
    if (value.isNullOrBlank()) { return null }
    return Url().apply {
        this.value = value
    }
}