package db

import java.sql.ResultSet
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper
import org.skife.jdbi.v2.sqlobject.*
import org.skife.jdbi.v2.sqlobject.customizers.*

import org.hibernate.validator.constraints.NotEmpty
import com.fasterxml.jackson.annotation.JsonProperty

interface AuthorizedOrcidDao {
    @SqlQuery("SELECT count(*) FROM authorized_orcids WHERE id = :id")
    fun countForId(@Bind("id") id: String): Int
}

class TokenRecord() {

    @NotEmpty @JsonProperty("access_token") lateinit var accessToken: String
    @NotEmpty @JsonProperty("token_type") lateinit var tokenType: String
    @NotEmpty @JsonProperty("refresh_token") lateinit var refreshToken: String
    @NotEmpty @JsonProperty("scope") lateinit var scope: String
    @NotEmpty @JsonProperty("name") lateinit var name: String
    @NotEmpty @JsonProperty("orcid") lateinit var orcid: String
    @JsonProperty("expires_in") var expiresIn: Long = 0

    val expiresAt: org.joda.time.DateTime by lazy {
        org.joda.time.DateTime.now().plusSeconds(expiresIn.toInt())
    }

}

class TokenRecordMapper : ResultSetMapper<TokenRecord> {
    override fun map(index: Int, r: ResultSet, ctx: StatementContext) =
        TokenRecord().apply {
            accessToken = r.getString("access_token")
            tokenType = r.getString("token_type")
            refreshToken = r.getString("refresh_token")
            scope = r.getString("scope")
            name = r.getString("name")
            orcid = r.getString("orcid")
            expiresIn = r.getLong("expires_in")
        }
}

abstract class TokenRecordDao {

    @SqlQuery("SELECT * FROM token_records WHERE orcid = :id")
    @Mapper(TokenRecordMapper::class)
    abstract fun findByOrcid(@Bind("id") id: String): TokenRecord?

    @SqlQuery("SELECT * FROM token_records")
    @Mapper(TokenRecordMapper::class)
    abstract fun all(): List<TokenRecord>

    @SqlUpdate("""UPDATE token_records SET
        access_token = :s.accessToken,
        token_type = :s.tokenType,
        refresh_token = :s.refreshToken,
        scope = :s.scope,
        name = :s.name,
        expires_in = :s.expiresIn
        WHERE orcid = :s.orcid""")
    abstract fun updateByOrcid(@BindBean("s") s: TokenRecord)

    @SqlUpdate("""INSERT INTO token_records
        (access_token, token_type, refresh_token, scope, name, orcid, expires_in) VALUES
        (:s.accessToken, :s.tokenType, :s.refreshToken, :s.scope, :s.name, :s.orcid, :s.expiresIn)""")
    abstract fun insert(@BindBean("s") s: TokenRecord)

    fun insertOrUpdate(s: TokenRecord) {
        if (findByOrcid(s.orcid) == null) {
            insert(s)
        } else {
            updateByOrcid(s)
        }
    }

}

